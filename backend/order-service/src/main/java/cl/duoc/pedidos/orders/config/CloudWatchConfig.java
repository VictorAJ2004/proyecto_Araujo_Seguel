package cl.duoc.pedidos.orders.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class CloudWatchConfig {

    @Bean
    public CloudWatchClient cloudWatchClient(@Value("${aws.region:us-east-1}") String region) {
        return CloudWatchClient.builder()
                .region(Region.of(region))
                .build();
    }
}