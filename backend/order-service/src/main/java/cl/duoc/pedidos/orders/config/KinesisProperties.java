package cl.duoc.pedidos.orders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws")
public class KinesisProperties {
    private String region;
    private Kinesis kinesis = new Kinesis();

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Kinesis getKinesis() { return kinesis; }
    public void setKinesis(Kinesis kinesis) { this.kinesis = kinesis; }

    public static class Kinesis {
        private Streams streams = new Streams();
        private int shards;
        private int retentionHours;
        private ConsumerGroups consumerGroups = new ConsumerGroups();

        public Streams getStreams() { return streams; }
        public void setStreams(Streams streams) { this.streams = streams; }
        public int getShards() { return shards; }
        public void setShards(int shards) { this.shards = shards; }
        public int getRetentionHours() { return retentionHours; }
        public void setRetentionHours(int retentionHours) { this.retentionHours = retentionHours; }
        public ConsumerGroups getConsumerGroups() { return consumerGroups; }
        public void setConsumerGroups(ConsumerGroups consumerGroups) { this.consumerGroups = consumerGroups; }
    }

    public static class Streams {
        private String pedidos;
        private String usuarios;
        private String dlq;

        public String getPedidos() { return pedidos; }
        public void setPedidos(String pedidos) { this.pedidos = pedidos; }
        public String getUsuarios() { return usuarios; }
        public void setUsuarios(String usuarios) { this.usuarios = usuarios; }
        public String getDlq() { return dlq; }
        public void setDlq(String dlq) { this.dlq = dlq; }
    }

    public static class ConsumerGroups {
        private String analitica;
        private String auditoria;

        public String getAnalitica() { return analitica; }
        public void setAnalitica(String analitica) { this.analitica = analitica; }
        public String getAuditoria() { return auditoria; }
        public void setAuditoria(String auditoria) { this.auditoria = auditoria; }
    }
}