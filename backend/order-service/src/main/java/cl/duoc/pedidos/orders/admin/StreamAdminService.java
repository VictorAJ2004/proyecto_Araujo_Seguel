package cl.duoc.pedidos.orders.admin;

import cl.duoc.pedidos.orders.streaming.CheckpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StreamAdminService {

    private static final Logger log = LoggerFactory.getLogger(StreamAdminService.class);

    private final KinesisClient kinesisClient;
    private final CheckpointService checkpointService;

    public StreamAdminService(KinesisClient kinesisClient, CheckpointService checkpointService) {
        this.kinesisClient = kinesisClient;
        this.checkpointService = checkpointService;
    }

    public List<String> listarStreams() {
        return kinesisClient.listStreams().streamNames();
    }

    public Map<String, Object> describirStream(String streamName) {
        StreamDescriptionSummary summary = kinesisClient.describeStreamSummary(
                DescribeStreamSummaryRequest.builder().streamName(streamName).build()
        ).streamDescriptionSummary();

        List<String> shards = kinesisClient.listShards(
                ListShardsRequest.builder().streamName(streamName).build()
        ).shards().stream().map(Shard::shardId).toList();

        Map<String, Object> info = new HashMap<>();
        info.put("streamName", summary.streamName());
        info.put("status", summary.streamStatusAsString());
        info.put("openShardCount", summary.openShardCount());
        info.put("retentionPeriodHours", summary.retentionPeriodHours());
        info.put("streamMode", summary.streamModeDetails().streamModeAsString());
        info.put("shards", shards);
        return info;
    }

    public String crearStream(String streamName, int shardCount) {
        kinesisClient.createStream(CreateStreamRequest.builder()
                .streamName(streamName)
                .shardCount(shardCount)
                .streamModeDetails(StreamModeDetails.builder().streamMode(StreamMode.PROVISIONED).build())
                .build());
        log.info("Stream creado: {} con {} shards", streamName, shardCount);
        return "Stream '" + streamName + "' en proceso de creación con " + shardCount + " shards.";
    }

    public Map<String, Object> modificarShards(String streamName, int targetShardCount) {
        UpdateShardCountResponse response = kinesisClient.updateShardCount(
                UpdateShardCountRequest.builder()
                        .streamName(streamName)
                        .targetShardCount(targetShardCount)
                        .scalingType(ScalingType.UNIFORM_SCALING)
                        .build()
        );
        log.info("Shards actualizados en {}: {} -> {}", streamName, response.currentShardCount(), response.targetShardCount());
        return Map.of(
                "streamName", response.streamName(),
                "currentShardCount", response.currentShardCount(),
                "targetShardCount", response.targetShardCount()
        );
    }

    public String modificarRetencion(String streamName, int horas) {
        try {
            StreamDescriptionSummary summary = kinesisClient.describeStreamSummary(
                    DescribeStreamSummaryRequest.builder().streamName(streamName).build()
            ).streamDescriptionSummary();

            int actual = summary.retentionPeriodHours();
            if (horas > actual) {
                kinesisClient.increaseStreamRetentionPeriod(
                        IncreaseStreamRetentionPeriodRequest.builder()
                                .streamName(streamName)
                                .retentionPeriodHours(horas)
                                .build());
            } else if (horas < actual) {
                kinesisClient.decreaseStreamRetentionPeriod(
                        DecreaseStreamRetentionPeriodRequest.builder()
                                .streamName(streamName)
                                .retentionPeriodHours(horas)
                                .build());
            }
            return "Retención de '" + streamName + "' actualizada a " + horas + " horas.";
        } catch (KinesisException e) {
            // Manejo limpio para la restricción SCP del AWS Learner Lab
            log.warn("No se pudo modificar retención en {} (posible restricción SCP): {}", streamName, e.getMessage());
            return "Operación bloqueada por política de AWS (SCP Learner Lab): " + e.awsErrorDetails().errorMessage();
        }
    }

    public String eliminarStream(String streamName) {
        kinesisClient.deleteStream(DeleteStreamRequest.builder()
                .streamName(streamName)
                .enforceConsumerDeletion(true)
                .build());
        log.info("Stream eliminado: {}", streamName);
        return "Stream '" + streamName + "' marcado para eliminación.";
    }

    public Map<String, Object> inspeccionarConsumidores(String streamName) {
        List<String> shards = kinesisClient.listShards(
                ListShardsRequest.builder().streamName(streamName).build()
        ).shards().stream().map(Shard::shardId).toList();

        Map<String, String> checkpoints = checkpointService.obtenerTodosLosCheckpoints();

        // Obtenemos una muestra rápida del retraso (millisBehindLatest) en el primer shard
        long millisBehindLatest = 0L;
        if (!shards.isEmpty()) {
            try {
                String iterator = kinesisClient.getShardIterator(GetShardIteratorRequest.builder()
                        .streamName(streamName)
                        .shardId(shards.get(0))
                        .shardIteratorType(ShardIteratorType.LATEST)
                        .build()).shardIterator();
                GetRecordsResponse records = kinesisClient.getRecords(
                        GetRecordsRequest.builder().shardIterator(iterator).limit(1).build()
                );
                if (records.millisBehindLatest() != null) {
                    millisBehindLatest = records.millisBehindLatest();
                }
            } catch (Exception e) {
                log.warn("No se pudo medir millisBehindLatest en {}: {}", streamName, e.getMessage());
            }
        }

        return Map.of(
                "streamName", streamName,
                "shardsActivos", shards,
                "checkpointsPorGrupoYShard", checkpoints,
                "retrasoConsumoMs", millisBehindLatest
        );
    }
}