package com.activityual.tracking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Configuration
@ConditionalOnProperty(
        name = "app.outbox.publisher.enabled",
        havingValue = "true")
public class EventBridgeConfig {

    @Bean
    public EventBridgeClient eventBridgeClient(
            @Value("${app.aws.region:ap-south-1}") String region) {
        return EventBridgeClient.builder()
                .region(Region.of(region))
                .build();
    }
}
