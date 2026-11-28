package com.activityual.analytics.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public AnalyticsService(
            DynamoDbClient dynamoDbClient,
            @Value("${app.dynamodb.analytics-table}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public List<Map<String, AttributeValue>> getUserAnalytics(String userId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("userId = :userId")
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.builder().s(userId).build()))
                .build();

        return dynamoDbClient.query(request).items();
    }

    public Map<String, AttributeValue> getActivityAnalytics(
            String userId, String activityId) {

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("userId = :userId AND activityId = :activityId")
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.builder().s(userId).build(),
                        ":activityId", AttributeValue.builder().s(activityId).build()))
                .limit(1)
                .build();

        return dynamoDbClient.query(request).items()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Analytics not found for activity: " + activityId));
    }
}
