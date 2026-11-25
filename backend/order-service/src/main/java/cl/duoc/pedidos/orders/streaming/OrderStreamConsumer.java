package cl.duoc.pedidos.orders.streaming;

import cl.duoc.pedidos.orders.config.KinesisProperties;
import cl.duoc.pedidos.orders.domain.PedidoAnaliticaService;
import cl.duoc.pedidos.orders.domain.PedidoAuditoriaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.awssdk.services.kinesis.model.Record;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class OrderStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStreamConsumer.class);
    private static final int MAX_REINTENTOS = 3;

    private final KinesisClient kinesisClient;
    private final KinesisProperties properties;
    private final ObjectMapper objectMapper;
    private final CheckpointService checkpointService;
    private final DlqEventProducer dlqEventProducer;
    private final PedidoAnaliticaService analiticaService;
    private final PedidoAuditoriaService auditoriaService;
    private final CloudWatchMetricsService metricsService;

    // Guarda el iterador actual por cada "consumerGroup:shardId"
    private final Map<String, String> shardIterators = new ConcurrentHashMap<>();

    public OrderStreamConsumer(KinesisClient kinesisClient,
                               KinesisProperties properties,
                               ObjectMapper objectMapper,
                               CheckpointService checkpointService,
                               DlqEventProducer dlqEventProducer,
                               PedidoAnaliticaService analiticaService,
                               PedidoAuditoriaService auditoriaService,
                               CloudWatchMetricsService metricsService) {
        this.kinesisClient = kinesisClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.checkpointService = checkpointService;
        this.dlqEventProducer = dlqEventProducer;
        this.analiticaService = analiticaService;
        this.auditoriaService = auditoriaService;
        this.metricsService = metricsService;
    }

    @Scheduled(fixedDelay = 3000)
    public void consumirEventos() {
        String streamName = properties.getKinesis().getStreams().getPedidos();
        String groupAnalitica = properties.getKinesis().getConsumerGroups().getAnalitica();
        String groupAuditoria = properties.getKinesis().getConsumerGroups().getAuditoria();

        try {
            List<Shard> shards = kinesisClient.listShards(
                    ListShardsRequest.builder().streamName(streamName).build()
            ).shards();

            for (Shard shard : shards) {
                leerShardParaGrupo(streamName, shard.shardId(), groupAnalitica, analiticaService::procesarEventoAnalitica);
                leerShardParaGrupo(streamName, shard.shardId(), groupAuditoria, auditoriaService::registrarAuditoria);
            }
        } catch (Exception e) {
            log.error("Error listando shards de {}: {}", streamName, e.getMessage());
        }
    }

    private void leerShardParaGrupo(String streamName, String shardId, String consumerGroup, Consumer<OrderEventDTO> logicaNegocio) {
        String key = consumerGroup + ":" + shardId;

        String iterator = shardIterators.computeIfAbsent(key, k -> obtenerIteradorInicial(streamName, shardId, consumerGroup));
        if (iterator == null) return;

        try {
            GetRecordsResponse response = kinesisClient.getRecords(
                    GetRecordsRequest.builder().shardIterator(iterator).limit(10).build()
            );

            for (Record record : response.records()) {
                procesarRegistroConResiliencia(record, consumerGroup, shardId, logicaNegocio);
                // Avanzamos el checkpoint después de procesar (o enviar a DLQ) para no bloquear el shard
                checkpointService.guardarCheckpoint(consumerGroup, shardId, record.sequenceNumber());
            }

            if (response.nextShardIterator() != null) {
                shardIterators.put(key, response.nextShardIterator());
            }
        } catch (ExpiredIteratorException e) {
            log.warn("Iterador expirado para {}:{}, renovando desde último checkpoint...", consumerGroup, shardId);
            shardIterators.remove(key);
        } catch (Exception e) {
            log.error("Error leyendo registros en {}:{}: {}", consumerGroup, shardId, e.getMessage());
        }
    }

    private String obtenerIteradorInicial(String streamName, String shardId, String consumerGroup) {
        String ultimoCheckpoint = checkpointService.obtenerCheckpoint(consumerGroup, shardId);

        GetShardIteratorRequest.Builder builder = GetShardIteratorRequest.builder()
                .streamName(streamName)
                .shardId(shardId);

        if (ultimoCheckpoint != null) {
            builder.shardIteratorType(ShardIteratorType.AFTER_SEQUENCE_NUMBER)
                   .startingSequenceNumber(ultimoCheckpoint);
        } else {
            builder.shardIteratorType(ShardIteratorType.LATEST);
        }

        return kinesisClient.getShardIterator(builder.build()).shardIterator();
    }

    private void procesarRegistroConResiliencia(Record record, String consumerGroup, String shardId, Consumer<OrderEventDTO> logicaNegocio) {
        String payload = record.data().asUtf8String();
        OrderEventDTO evento;

        // 1. Deserialización (si el JSON viene corrupto es irrecuperable -> directo a DLQ)
        try {
            evento = objectMapper.readValue(payload, OrderEventDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Evento corrupto (irrecuperable) | group={} | shard={} | causa={}", consumerGroup, shardId, e.getMessage());
            dlqEventProducer.enviarADlq("JSON_INVALIDO", payload, "Error deserializando JSON: " + e.getMessage(), 1, consumerGroup);
            metricsService.registrarEventoDlq(consumerGroup);
            return;
        }

        // 2. Idempotencia (tolerancia a duplicados)
        if (evento.getEventId() != null && checkpointService.yaFueProcesado(consumerGroup, evento.getEventId())) {
            log.warn("Evento duplicado ignorado | group={} | eventId={}", consumerGroup, evento.getEventId());
            return;
        }

        // 3. Ejecución con reintentos exponenciales para errores transitorios
        int intento = 0;
        while (intento < MAX_REINTENTOS) {
            intento++;
            try {
                logicaNegocio.accept(evento);
                if (evento.getEventId() != null) {
                    checkpointService.marcarComoProcesado(consumerGroup, evento.getEventId());
                }
                metricsService.registrarEventoProcesado(consumerGroup);
                return; // Éxito
            } catch (IllegalArgumentException e) {
                // Error de validación de negocio -> irrecuperable, no sirve reintentar
                log.error("Error de validación (irrecuperable) | eventId={} | group={} | intento={} | causa={}",
                        evento.getEventId(), consumerGroup, intento, e.getMessage());
                dlqEventProducer.enviarADlq(evento.getEventId(), payload, e.getMessage(), intento, consumerGroup);
                metricsService.registrarEventoDlq(consumerGroup);
                return;
            } catch (Exception e) {
                // Error transitorio -> reintentar con espera creciente (backoff)
                log.warn("Error transitorio | eventId={} | group={} | intento={}/{} | causa={}",
                        evento.getEventId(), consumerGroup, intento, MAX_REINTENTOS, e.getMessage());
                if (intento >= MAX_REINTENTOS) {
                    dlqEventProducer.enviarADlq(evento.getEventId(), payload, "Reintentos agotados: " + e.getMessage(), intento, consumerGroup);
                    metricsService.registrarEventoDlq(consumerGroup);
                } else {
                    try {
                        Thread.sleep(500L * intento); // Espera creciente: 500ms, 1000ms...
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}