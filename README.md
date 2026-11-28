# Activityual — Cloud-Native Personal Habit Tracking Platform

Activityual is a cloud-native habit and activity tracking application. Users can register, manage recurring activities, record completion or missed entries, view analytics, and receive recommendations.

## Architecture

The platform uses independently deployable Spring Boot microservices, a SvelteKit frontend, Lit web components, PostgreSQL, DynamoDB, and AWS managed services.

| Component | Responsibility | Local port |
|---|---|---:|
| Auth Service | Registration, login, JWT issuance and validation | 8081 |
| Activity Service | User-scoped activity CRUD | 8082 |
| Tracking Service | Completion, missed and tracking records; transactional outbox | 8083 |
| Analytics Service | Activity analytics and consistency summaries | 8084 |
| Recommendation Service | Recommendation retrieval and acceptance | 8085 |
| SvelteKit frontend | Dashboard and user experience | 3000 |
| PostgreSQL | Relational service data | 5432 |
| DynamoDB Local | Analytics, recommendations and event idempotency data | 8000 |

Lit web components provide reusable activity cards, analytics charts, recommendation banners and streak badges.

### Service interactions

1. The frontend authenticates through Auth Service and receives a JWT.
2. The frontend sends the JWT to protected Activity, Tracking, Analytics and Recommendation endpoints.
3. Activity Service stores user-owned activities in PostgreSQL.
4. Tracking Service stores tracking records and writes event data to its transactional outbox.
5. The outbox publisher can publish activity events to the configured Amazon EventBridge bus.
6. An EventBridge rule can invoke the Python Activity Event Processor Lambda.
7. The Lambda uses DynamoDB transactions and a processed-event table to prevent duplicate processing while updating analytics and recommendation data.
8. Analytics and Recommendation services read their respective DynamoDB data.

**Event-driven integration status:** the Lambda source and local packaging artifacts exist, and AWS services have been deployed. The complete EventBridge → Lambda → DynamoDB → Analytics/Recommendation workflow still requires end-to-end verification. Do not interpret healthy ECS targets as proof that event processing works.

### Recommendation capability

Recommendation Service exposes APIs to list, retrieve and accept recommendations. The current Lambda documentation describes a rule-based consistency suggestion following completed activity events. It is not yet a demonstrated predictive time-of-day recommendation engine. The frontend displays available suggestions and supports acceptance.

## Repository structure

```text
backend/
  auth-service/
  activity-service/
  tracking-service/
  analytics-service/
  recommendation-service/
frontend/
  activityual-web/
  web-components/
database/
  init/
contracts/
  events/
  openapi/
infrastructure/
  aws/
  docker/
  lambda/activity-event-processor/
docs/
  api/
  architecture/
  deployment/
  diagrams/
  screenshots/
  user-walkthrough/
scripts/
```

## Local setup

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js and npm
- Docker Engine and Docker Compose
- AWS CLI only if testing against AWS rather than local DynamoDB

### 1. Configure environment

```bash
cp env.example .env
```

Review `.env` and use local-only credentials. Never commit `.env`.

### 2. Start local infrastructure

```bash
docker compose up -d
docker compose ps
```

The root Compose file starts PostgreSQL and DynamoDB Local. It does not automatically start the six application services.

### 3. Build and run backend services

In separate terminals, from the repository root:

```bash
cd backend/auth-service && mvn spring-boot:run
cd backend/activity-service && mvn spring-boot:run
cd backend/tracking-service && mvn spring-boot:run
cd backend/analytics-service && mvn spring-boot:run
cd backend/recommendation-service && mvn spring-boot:run
```

Configure each service's database, JWT secret, and DynamoDB settings using its environment variables. The relational databases are initialized by `database/init/01-create-databases.sql`.

### 4. Start the frontend

```bash
cd frontend/activityual-web
npm ci
npm run dev -- --host 0.0.0.0
```

Open the URL printed by Vite, normally `http://localhost:5173`.

### 5. Stop local infrastructure

```bash
docker compose down
```

This preserves named Docker volumes. To remove local database data as well, explicitly use `docker compose down -v`.

### Local infrastructure endpoints

| Component | URL |
|---|---|
| PostgreSQL | `localhost:5432` |
| DynamoDB Local | `http://localhost:8000` |
| Auth | `http://localhost:8081` |
| Activity | `http://localhost:8082` |
| Tracking | `http://localhost:8083` |
| Analytics | `http://localhost:8084` |
| Recommendation | `http://localhost:8085` |

Actuator health endpoints are exposed at `/actuator/health` where enabled. Swagger UI is configured in services that include Springdoc; availability should be checked against each running service.

## API documentation

OpenAPI specifications are maintained under `contracts/openapi/`. Recommendation endpoints are included.

- Auth: `/api/v1/auth/register`, `/login`, `/me`, `/validate`
- Activity: `/api/v1/activities`
- Tracking: `/api/v1/tracking`
- Analytics: `/api/v1/analytics`
- Recommendation: `/api/v1/recommendations`

All protected operations require `Authorization: Bearer <JWT>`.

## Sample user walkthrough

1. Register a user and sign in.
2. Create an activity such as "Read for 20 minutes", category "Learning", frequency `DAILY`.
3. Mark the activity `COMPLETED` or `MISSED` for the current date.
4. Open the dashboard and analytics page to inspect available tracking summaries.
5. Open Recommendations to view suggestions returned by Recommendation Service.
6. Accept a recommendation using its accept action.
7. Refresh the page and inspect the updated recommendation state.

Analytics and recommendations depend on their backing DynamoDB data being populated. Event-driven updates must be verified in the deployed environment before presenting them as a successful live workflow.

## AWS deployment

The deployed environment uses AWS in `ap-south-1`. The ECS cluster is `activityual-cluster`. The six ECS services are Auth, Activity, Tracking, Analytics, Recommendation and Frontend. Images are stored in ECR. An Application Load Balancer routes traffic to service target groups. Relational data is hosted in RDS PostgreSQL; DynamoDB stores analytics, recommendation and processed-event records. EventBridge and Lambda provide the event-processing path.

The current deployment verification recorded all six ECS services at desired count 1, running count 1 and pending count 0. The Tracking target group was verified healthy after correcting the Actuator dependency packaging issue. This is deployment/health evidence, not proof of complete business-flow or event integration.

See:
- [Network architecture](docs/diagrams/activityual-network.drawio)
- [Cloud deployment evidence checklist](docs/deployment/aws-deployment.md)
- [Demo recording plan](docs/deployment/demo-script.md)
- [User walkthrough](docs/user-walkthrough/end-to-end-scenario.md)

## Security and configuration

- Do not commit `.env`, AWS credentials, private keys, JWT secrets or database passwords.
- Use AWS IAM roles for deployed workloads where possible.
- Use HTTPS at the public entry point.
- Restrict database and DynamoDB access to application security groups and IAM permissions.
- Replace development secrets before production use.

## Project status

The six ECS services have been deployed and their running task counts verified. Tracking's ALB health check is healthy. Phase 3 event-driven integration still requires end-to-end verification. Final submission evidence and demo recording must be captured from the actual AWS environment.
