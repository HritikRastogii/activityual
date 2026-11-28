import json
import logging
import os
from datetime import datetime, timezone

import boto3
from botocore.exceptions import ClientError

logger = logging.getLogger()
logger.setLevel(logging.INFO)

dynamodb = boto3.resource("dynamodb")

analytics_table = dynamodb.Table(
    os.environ.get("ANALYTICS_TABLE", "ActivityAnalytics")
)
recommendations_table = dynamodb.Table(
    os.environ.get("RECOMMENDATIONS_TABLE", "ActivityRecommendations")
)
processed_table = dynamodb.Table(
    os.environ.get("PROCESSED_EVENTS_TABLE", "ActivityProcessedEvents")
)


def lambda_handler(event, context):
    detail = event.get("detail", {})

    if isinstance(detail, str):
        detail = json.loads(detail)

    event_id = detail.get("eventId") or event.get("id")
    event_type = detail.get("eventType") or event.get("detail-type")

    user_id = detail.get("userEmail") or detail.get("userId")
    activity_id = str(detail.get("activityId", ""))
    tracking_date = str(detail.get("trackingDate", ""))
    status = str(detail.get("status", "")).upper()

    if not all([event_id, user_id, activity_id, tracking_date]):
        raise ValueError("Event is missing eventId, userId, activityId or trackingDate")

    if event_type not in ("ActivityCompleted", "ActivityMissed"):
        logger.info("Ignoring unsupported event type: %s", event_type)
        return {"statusCode": 200, "body": "Ignored unsupported event"}

    if status not in ("COMPLETED", "MISSED"):
        status = "COMPLETED" if event_type == "ActivityCompleted" else "MISSED"

    now = datetime.now(timezone.utc).isoformat()
    counter = "completedTotal" if status == "COMPLETED" else "missedTotal"

    analytics_update = {
        "TableName": analytics_table.name,
        "Key": {
            "userId": {"S": user_id},
            "activityId": {"S": activity_id}
        },
        "UpdateExpression": (
            "SET lastStatus = :status, lastTrackingDate = :trackingDate, "
            "updatedAt = :updatedAt, #counter = :one"
        ),
        "ExpressionAttributeNames": {
            "#counter": counter
        },
        "ExpressionAttributeValues": {
            ":status": {"S": status},
            ":trackingDate": {"S": tracking_date},
            ":updatedAt": {"S": now},
            ":one": {"N": "1"}
        }
    }


    try:
        # Check whether this event was already processed.
        existing = processed_table.get_item(
            Key={"eventId": event_id},
            ConsistentRead=True
        ).get("Item")

        if existing:
            logger.info("Duplicate event ignored: %s", event_id)
            return {"statusCode": 200, "body": "Duplicate event ignored"}

        # Update analytics using native Python values.
        analytics_table.update_item(
            Key={"userId": user_id, "activityId": activity_id},
            UpdateExpression=(
                "SET lastStatus = :status, "
                "lastTrackingDate = :trackingDate, "
                "updatedAt = :updatedAt "
                "ADD #counter :one"
            ),
            ExpressionAttributeNames={"#counter": counter},
            ExpressionAttributeValues={
                ":status": status,
                ":trackingDate": tracking_date,
                ":updatedAt": now,
                ":one": 1
            }
        )

        # Create or refresh the recommendation.
        if status == "COMPLETED":
            recommendation_id = f"consistency-{activity_id}-{tracking_date}"
            recommendations_table.put_item(
                Item={
                    "userId": user_id,
                    "recommendationId": recommendation_id,
                    "activityId": activity_id,
                    "recommendationType": "CONSISTENCY",
                    "suggestedAction": (
                        "Continue this activity consistently and maintain your routine."
                    ),
                    "reason": "Generated after recording a completed activity.",
                    "status": "PENDING",
                    "createdAt": now
                }
            )

        # Record successful processing last.
        processed_table.put_item(
            Item={"eventId": event_id, "processedAt": now},
            ConditionExpression="attribute_not_exists(eventId)"
        )

        logger.info("Successfully processed event %s", event_id)
        return {"statusCode": 200, "body": "Event processed"}

    except ClientError as exc:
        logger.error("DynamoDB error response: %s", json.dumps(exc.response, default=str))
        logger.exception("Event processing failed")
        raise
