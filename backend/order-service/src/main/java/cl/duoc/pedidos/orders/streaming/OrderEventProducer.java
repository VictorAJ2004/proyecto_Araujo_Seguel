package cl.duoc.pedidos.orders.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.pedidos.orders.config.KinesisProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

@Service
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KinesisClient kinesisClient;
    private final KinesisProperties properties;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(KinesisClient kinesisClient,
                              KinesisProperties properties,
                              ObjectMapper objectMapper) {
        this.kinesisClient = kinesisClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void publicarEventoPedido(OrderEventDTO evento) {
        try {
            String json = objectMapper.writeValueAsString(evento);
            String streamName = properties.getKinesis().getStreams().getPedidos();

            PutRecordRequest request = PutRecordRequest.builder()
                    .streamName(streamName)
                    .partitionKey(evento.getOrderId()) // Garantiza orden de eventos del mismo pedido
                    .data(SdkBytes.fromUtf8String(json))
                    .build();

            PutRecordResponse response = kinesisClient.putRecord(request);
            log.info("Evento publicado en {} | eventId={} | shardId={} | sequenceNumber={}",
                    streamName, evento.getEventId(), response.shardId(), response.sequenceNumber());

        } catch (Exception e) {
            log.error("Error publicando evento eventId={} en Kinesis: {}", evento.getEventId(), e.getMessage(), e);
            throw new RuntimeException("No se pudo publicar el evento en Kinesis", e);
        }
    }
}