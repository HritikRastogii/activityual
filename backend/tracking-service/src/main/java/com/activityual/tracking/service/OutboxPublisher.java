package com.activityual.tracking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "app.outbox.publisher.enabled",
        havingValue = "true")
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final JdbcTemplate jdbcTemplate;
    private final EventBridgeClient eventBridgeClient;
    private final String eventBusName;
    private final int batchSize;

    public OutboxPublisher(
            JdbcTemplate jdbcTemplate,
            EventBridgeClient eventBridgeClient,
            @Value("${app.outbox.publisher.event-bus-name:activityual-events}")
            String eventBusName,
            @Value("${app.outbox.publisher.batch-size:10}")
            int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventBridgeClient = eventBridgeClient;
        this.eventBusName = eventBusName;
        this.batchSize = Math.min(batchSize, 10);
    }

    @Scheduled(fixedDelayString =
            "${app.outbox.publisher.interval-ms:5000}")
    public void publishPendingEvents() {
        try {
            List<Map<String, Object>> events = jdbcTemplate.queryForList(
                    "SELECT event_id, event_type, payload " +
                    "FROM outbox_events " +
                    "WHERE published_at IS NULL " +
                    "ORDER BY created_at ASC LIMIT ?",
                    batchSize);

            for (Map<String, Object> event : events) {
                publish(event);
            }
        } catch (Exception ex) {
            log.error("Failed to process transactional outbox batch", ex);
        }
    }

    private void publish(Map<String, Object> event) {
        Object rawId = event.get("event_id");
        String eventId = String.valueOf(rawId);
        String eventType = String.valueOf(event.get("event_type"));
        String payload = String.valueOf(event.get("payload"));

        try {
            PutEventsResponse response = eventBridgeClient.putEvents(
                    PutEventsRequest.builder()
                            .entries(PutEventsRequestEntry.builder()
                                    .eventBusName(eventBusName)
                                    .source("com.activityual.tracking")
                                    .detailType(eventType)
                                    .detail(payload)
                                    .build())
                            .build());

            if (response.failedEntryCount() != null
                    && response.failedEntryCount() > 0) {
                var failure = response.entries().get(0);
                log.error("EventBridge rejected event {}: {} - {}",
                        eventId, failure.errorCode(), failure.errorMessage());
                return;
            }

            jdbcTemplate.update(
                    "UPDATE outbox_events SET published_at = CURRENT_TIMESTAMP " +
                    "WHERE event_id = ?",
                    UUID.fromString(eventId));

            log.info("Published outbox event {} ({})", eventId, eventType);

        } catch (Exception ex) {
            log.error("Unable to publish outbox event {}", eventId, ex);
        }
    }
}
