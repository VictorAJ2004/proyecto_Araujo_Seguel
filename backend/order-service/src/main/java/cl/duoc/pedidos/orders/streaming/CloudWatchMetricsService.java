package cl.duoc.pedidos.orders.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

@Service
public class CloudWatchMetricsService {

    private static final Logger log = LoggerFactory.getLogger(CloudWatchMetricsService.class);
    private static final String NAMESPACE = "Pedidos360/Streaming";

    private final CloudWatchClient cloudWatchClient;

    public CloudWatchMetricsService(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public void registrarEventoProcesado(String consumerGroup) {
        enviarMetrica("EventosProcesados", consumerGroup, 1.0);
    }

    public void registrarEventoDlq(String consumerGroup) {
        enviarMetrica("EventosEnviadosDLQ", consumerGroup, 1.0);
    }

    private void enviarMetrica(String metricName, String consumerGroup, double valor) {
        try {
            Dimension dimension = Dimension.builder()
                    .name("ConsumerGroup")
                    .value(consumerGroup)
                    .build();

            MetricDatum datum = MetricDatum.builder()
                    .metricName(metricName)
                    .unit(StandardUnit.COUNT)
                    .value(valor)
                    .dimensions(dimension)
                    .build();

            cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
                    .namespace(NAMESPACE)
                    .metricData(datum)
                    .build());
        } catch (Exception e) {
            // Nunca detenemos el procesamiento del stream si falla el envío de métricas
            log.warn("No se pudo enviar métrica {} a CloudWatch: {}", metricName, e.getMessage());
        }
    }
}