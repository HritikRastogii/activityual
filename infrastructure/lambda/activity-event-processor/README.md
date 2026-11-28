# Activityual Activity Event Processor

## Purpose
Processes Tracking Service events delivered by Amazon EventBridge.

Supported events:
- ActivityCompleted
- ActivityMissed

## DynamoDB tables

The Lambda expects these tables:

| Table | Partition key | Sort key |
|---|---|---|
| ActivityAnalytics | userId (String) | activityId (String) |
| ActivityRecommendations | userId (String) | recommendationId (String) |
| ActivityProcessedEvents | eventId (String) | None |

## Lambda environment variables

- ANALYTICS_TABLE=ActivityAnalytics
- RECOMMENDATIONS_TABLE=ActivityRecommendations
- PROCESSED_EVENTS_TABLE=ActivityProcessedEvents

## Runtime and handler

- Runtime: Python 3.12
- Handler: lambda_function.lambda_handler
- Architecture: x86_64 or arm64

Boto3 is included in the standard AWS Lambda Python runtime.

## IAM permissions

The Lambda execution role needs:
- dynamodb:TransactWriteItems
- CloudWatch Logs permissions

Restrict DynamoDB permissions to the three Activityual tables.

## Event structure

The Tracking Service outbox event detail should contain:
- eventId
- userEmail (or userId)
- activityId
- trackingDate
- status

The EventBridge detail-type must be ActivityCompleted or ActivityMissed.

## Processing behavior

The processor atomically:
1. Records the event ID in ActivityProcessedEvents.
2. Updates the user's activity analytics counters and latest status.
3. Creates a consistency recommendation after a completed activity.

The transaction prevents duplicate event processing from incrementing analytics twice.

## Important limitation

The initial recommendation is a rule-based consistency suggestion, not a time-of-day prediction. Timing/frequency personalization will be enhanced when activity schedules and historical completion patterns are available.
