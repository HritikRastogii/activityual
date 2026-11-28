#!/usr/bin/env bash
set -euo pipefail

aws dynamodb create-table \
  --table-name ActivityProcessedEvents \
  --attribute-definitions AttributeName=eventId,AttributeType=S \
  --key-schema AttributeName=eventId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region "${AWS_REGION:-ap-south-1}"
