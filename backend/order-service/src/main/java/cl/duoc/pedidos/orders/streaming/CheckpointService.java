package cl.duoc.pedidos.orders.streaming;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CheckpointService {

    // Clave: "consumerGroup:shardId" -> último sequenceNumber procesado
    private final Map<String, String> checkpoints = new ConcurrentHashMap<>();

    // Clave: "consumerGroup:eventId" -> para garantizar idempotencia (tolerancia a duplicados)
    private final Set<String> eventosProcesados = ConcurrentHashMap.newKeySet();

    public void guardarCheckpoint(String consumerGroup, String shardId, String sequenceNumber) {
        checkpoints.put(consumerGroup + ":" + shardId, sequenceNumber);
    }

    public String obtenerCheckpoint(String consumerGroup, String shardId) {
        return checkpoints.get(consumerGroup + ":" + shardId);
    }

    public Map<String, String> obtenerTodosLosCheckpoints() {
        return Map.copyOf(checkpoints);
    }

    public boolean yaFueProcesado(String consumerGroup, String eventId) {
        return eventosProcesados.contains(consumerGroup + ":" + eventId);
    }

    public void marcarComoProcesado(String consumerGroup, String eventId) {
        eventosProcesados.add(consumerGroup + ":" + eventId);
    }
}