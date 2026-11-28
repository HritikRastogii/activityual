package com.activityual.recommendation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;

@Service
public class RecommendationService {
    private final DynamoDbClient dynamoDb;
    private final String tableName;

    public RecommendationService(
            DynamoDbClient dynamoDb,
            @Value("${app.dynamodb.recommendations-table}") String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    public List<Map<String, Object>> getRecommendations(String userId) {
        var result = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("userId = :userId")
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.builder().s(userId).build()))
                .build());

        return result.items().stream().map(this::convertItem).toList();
    }

    public Map<String, Object> getRecommendation(String userId, String id) {
        var result = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(key(userId, id))
                .build());

        if (!result.hasItem() || result.item().isEmpty()) {
            throw new RecommendationNotFoundException(
                    "Recommendation not found: " + id);
        }
        return convertItem(result.item());
    }

    public Map<String, Object> acceptRecommendation(String userId, String id) {
        try {
            dynamoDb.updateItem(UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(key(userId, id))
                    .updateExpression("SET #status = :status, acceptedAt = :acceptedAt")
                    .conditionExpression("attribute_exists(userId)")
                    .expressionAttributeNames(Map.of("#status", "status"))
                    .expressionAttributeValues(Map.of(
                            ":status", AttributeValue.builder().s("ACCEPTED").build(),
                            ":acceptedAt", AttributeValue.builder()
                                    .s(Instant.now().toString()).build()))
                    .build());
        } catch (ConditionalCheckFailedException ex) {
            throw new RecommendationNotFoundException(
                    "Recommendation not found: " + id);
        }
        return getRecommendation(userId, id);
    }

    private Map<String, AttributeValue> key(String userId, String id) {
        return Map.of(
                "userId", AttributeValue.builder().s(userId).build(),
                "recommendationId", AttributeValue.builder().s(id).build());
    }

    private Map<String, Object> convertItem(Map<String, AttributeValue> item) {
        Map<String, Object> result = new LinkedHashMap<>();
        item.forEach((key, value) -> result.put(key, convertValue(value)));
        return result;
    }

    private Object convertValue(AttributeValue value) {
        if (value.s() != null) return value.s();
        if (value.n() != null) return value.n();
        if (value.bool() != null) return value.bool();
        if (Boolean.TRUE.equals(value.nul())) return null;
        if (value.hasM()) return convertItem(value.m());
        if (value.hasL()) return value.l().stream().map(this::convertValue).toList();
        if (value.hasSs()) return value.ss();
        if (value.hasNs()) return value.ns();
        return null;
    }
}
