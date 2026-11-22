package cl.duoc.pedidos.orders.streaming;

import cl.duoc.pedidos.orders.config.KinesisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

import java.time.Instant;
import java.util.Map;

@Service
public class DlqEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DlqEventProducer.class);

    private final KinesisClient kinesisClient;
    private final KinesisProperties properties;
    private final ObjectMapper objectMapper;

    public DlqEventProducer(KinesisClient kinesisClient,
                            KinesisProperties properties,
                            ObjectMapper objectMapper) {
        this.kinesisClient = kinesisClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void enviarADlq(String eventId, String payloadOriginal, String causa, int intentos, String consumerGroup) {
        try {
            String dlqStream = properties.getKinesis().getStreams().getDlq();
            Map<String, Object> dlqPayload = Map.of(
                    "eventId", eventId != null ? eventId : "DESCONOCIDO",
                    "consumerGroup", consumerGroup,
                    "causa", causa,
                    "intentos", intentos,
                    "timestamp", Instant.now().toString(),
                    "payloadOriginal", payloadOriginal
            );

            String json = objectMapper.writeValueAsString(dlqPayload);
            PutRecordRequest request = PutRecordRequest.builder()
                    .streamName(dlqStream)
                    .partitionKey(eventId != null ? eventId : "dlq-key")
                    .data(SdkBytes.fromUtf8String(json))
                    .build();

            kinesisClient.putRecord(request);
            log.warn("Evento enviado a DLQ ({}) | eventId={} | group={} | intentos={} | causa={}",
                    dlqStream, eventId, consumerGroup, intentos, causa);
        } catch (Exception e) {
            log.error("Error crítico enviando evento a DLQ: {}", e.getMessage(), e);
        }
    }
}