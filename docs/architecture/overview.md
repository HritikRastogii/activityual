# Architecture Overview

The system is organized into five Spring Boot backend microservices and one SvelteKit frontend. Authentication is JWT-based. Activity and Tracking use PostgreSQL. Analytics and Recommendation read DynamoDB. Tracking's transactional outbox is the intended source for asynchronous activity events. EventBridge routes events to a Python Lambda processor, which uses DynamoDB transactions and an idempotency table.

The network diagram in `../diagrams/activityual-network.drawio` shows the logical AWS topology. Confirm actual subnet, security group, NAT, internet gateway and endpoint configuration in the AWS Console before adding low-level network claims to the submission.
