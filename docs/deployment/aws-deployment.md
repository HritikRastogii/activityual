# AWS Cloud Deployment and Evidence

## Environment recorded for deployment

- AWS Region: `ap-south-1`
- ECS cluster: `activityual-cluster`
- ECS services: `activityual-auth-service`, `activityual-activity-service`, `activityual-tracking-service`, `activityual-analytics-service`, `activityual-recommendation-service`, `activityual-frontend`
- ECR repositories: one per service
- Application Load Balancer: routes to service target groups
- RDS PostgreSQL: relational persistence
- DynamoDB: analytics, recommendation and processed-event data
- EventBridge: activity event bus
- Lambda: Python 3.12 Activity Event Processor

## Recorded deployment verification

On 28 November 2026, the six ECS services were observed at desired count 1, running count 1 and pending count 0. Tracking's target group was observed healthy at `10.0.27.1:8083` after the Actuator dependency was moved into the actual Maven dependencies and the Tracking image was rebuilt and redeployed.

Capture fresh screenshots before submission; these recorded observations are not a substitute for current console evidence.

## Screenshots to capture

Save genuine screenshots under `docs/screenshots/`. Do not fabricate or edit screenshots to imply a configuration that was not verified.

1. ECS cluster overview showing all six services.
2. ECS service details and task health for each service.
3. ECR repository list and Tracking image tag.
4. Load Balancer listeners and routing rules.
5. Target groups showing healthy targets.
6. RDS database instance status and connectivity configuration (hide credentials).
7. DynamoDB tables and key schemas.
8. EventBridge custom bus and event rules.
9. Lambda runtime, handler, environment variable names and execution role.
10. CloudWatch Lambda logs showing a successfully processed event and duplicate/idempotency behavior, if verified.
11. Frontend running in the browser.

## Security when recording

Do not expose passwords, JWT signing secrets, AWS access keys, session tokens, database connection passwords, private IP allowlists or sensitive personal information. Show IAM role names and policy intent, not secret values.

## Final cloud verification

- Confirm all six ECS services are stable.
- Confirm all ALB targets are healthy.
- Test registration, login, activity creation, tracking and the frontend.
- Verify a real EventBridge event invokes Lambda.
- Verify the processed-event record and analytics/recommendation writes.
- Replay the same event and confirm it is not processed twice.
- Verify Analytics and Recommendation APIs return the expected updated records.
