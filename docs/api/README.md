# API Documentation

OpenAPI 3.0 specifications are in `../../contracts/openapi/`.

| Service | Specification |
|---|---|
| Auth | `auth-service.yaml` |
| Activity | `activity-service.yaml` |
| Tracking | `tracking-service.yaml` |
| Analytics | `analytics-service.yaml` |
| Recommendation | `recommendation-service.yaml` |

Protected endpoints use a JWT bearer token. Request and response details should be kept synchronized with the service controllers and DTOs when APIs change. The Analytics and Recommendation services return DynamoDB-backed maps; their precise response properties depend on the stored item schema.
