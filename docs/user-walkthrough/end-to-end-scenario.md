# Activityual — End-to-End User Walkthrough

## Scenario
A new user wants to build a daily reading habit and review progress.

1. Open the Activityual frontend and register with a name, email and password.
2. Sign in. Auth Service issues a JWT used for protected API requests.
3. Create an activity:
   - Name: Read for 20 minutes
   - Category: Learning
   - Frequency: DAILY
   - Notes: Read before bedtime
4. Record a tracking entry for today's date. Choose `COMPLETED` when the habit was performed or `MISSED` when it was not.
5. Open Analytics to view the available activity and consistency data.
6. Open Recommendations to retrieve suggestions from Recommendation Service.
7. Open a recommendation and use its accept action.
8. Refresh the dashboard to see the latest data returned by the services.

## API sequence

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/activities`
- `POST /api/v1/tracking`
- `GET /api/v1/analytics`
- `GET /api/v1/recommendations`
- `POST /api/v1/recommendations/{recommendationId}/accept`

Protected requests use `Authorization: Bearer <accessToken>`.

## Event-driven path (verification required)

Tracking Service writes an outbox event. The intended cloud flow is EventBridge bus → matching rule → Lambda processor → DynamoDB transaction. The Lambda records the event ID for idempotency, updates analytics, and may create a consistency recommendation.

This sequence must be demonstrated with a real event and matching DynamoDB records before claiming the complete event-driven workflow is operational. A successful API response or healthy ECS task alone does not prove this path.
