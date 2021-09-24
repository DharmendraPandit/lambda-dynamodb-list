#!/bin/bash
set -eo pipefail
BUCKET=$(aws cloudformation describe-stack-resource --stack-name s3-java --logical-resource-id bucket --query 'StackResourceDetail.PhysicalResourceId' --output text)
aws s3 cp images/test.html s3://$BUCKET/
