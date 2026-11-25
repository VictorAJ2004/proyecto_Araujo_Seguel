package cl.duoc.pedidos.orders.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CheckpointService {

    private static final Logger log = LoggerFactory.getLogger(CheckpointService.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    // Registro de eventIds procesados para idempotencia rápida
    private final Set<String> eventosProcesados = ConcurrentHashMap.newKeySet();

    public CheckpointService(DynamoDbClient dynamoDbClient,
                             @Value("${aws.dynamodb.checkpoints-table:pedidos360-kinesis-checkpoints}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public void guardarCheckpoint(String consumerGroup, String shardId, String sequenceNumber) {
        String key = consumerGroup + ":" + shardId;
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("consumerGroupShard", AttributeValue.builder().s(key).build());
            item.put("consumerGroup", AttributeValue.builder().s(consumerGroup).build());
            item.put("shardId", AttributeValue.builder().s(shardId).build());
            item.put("sequenceNumber", AttributeValue.builder().s(sequenceNumber).build());
            item.put("updatedAt", AttributeValue.builder().s(Instant.now().toString()).build());

            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build());
        } catch (Exception e) {
            log.error("Error guardando checkpoint en DynamoDB para {}: {}", key, e.getMessage());
        }
    }

    public String obtenerCheckpoint(String consumerGroup, String shardId) {
        String key = consumerGroup + ":" + shardId;
        try {
            GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("consumerGroupShard", AttributeValue.builder().s(key).build()))
                    .build());

            if (response.hasItem() && response.item().containsKey("sequenceNumber")) {
                return response.item().get("sequenceNumber").s();
            }
        } catch (Exception e) {
            log.error("Error leyendo checkpoint en DynamoDB para {}: {}", key, e.getMessage());
        }
        return null;
    }

    public Map<String, String> obtenerTodosLosCheckpoints() {
        Map<String, String> resultado = new HashMap<>();
        try {
            ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName(tableName)
                    .build());

            for (Map<String, AttributeValue> item : response.items()) {
                if (item.containsKey("consumerGroupShard") && item.containsKey("sequenceNumber")) {
                    resultado.put(item.get("consumerGroupShard").s(), item.get("sequenceNumber").s());
                }
            }
        } catch (Exception e) {
            log.error("Error escaneando checkpoints en DynamoDB: {}", e.getMessage());
        }
        return resultado;
    }

    public boolean yaFueProcesado(String consumerGroup, String eventId) {
        return eventosProcesados.contains(consumerGroup + ":" + eventId);
    }

    public void marcarComoProcesado(String consumerGroup, String eventId) {
        eventosProcesados.add(consumerGroup + ":" + eventId);
    }
}