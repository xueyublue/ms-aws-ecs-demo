# ECS Deployment Guide

This guide deploys the Todo Spring Boot app to **Amazon ECS Fargate** using **ECR** and **AWS CLI**.

## 1) Prerequisites

- AWS account and IAM permissions for ECR, ECS, EC2, CloudWatch Logs, IAM
- AWS CLI v2 configured (`aws configure`)
- Docker installed and running
- Existing VPC and at least 2 private subnets (recommended)
- Security group allowing inbound app traffic (for example, from ALB to port 8080)

## 2) Set environment variables

Use your own values before running commands.

```bash
export AWS_REGION=ap-southeast-1
export ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export APP_NAME=todo-app
export CLUSTER_NAME=todo-ecs-cluster
export SERVICE_NAME=todo-ecs-service
export IMAGE_TAG=v1
export ECR_REPO_URI=${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${APP_NAME}
```

PowerShell version:

```powershell
$env:AWS_REGION="ap-southeast-1"
$env:ACCOUNT_ID=(aws sts get-caller-identity --query Account --output text)
$env:APP_NAME="todo-app"
$env:CLUSTER_NAME="todo-ecs-cluster"
$env:SERVICE_NAME="todo-ecs-service"
$env:IMAGE_TAG="v1"
$env:ECR_REPO_URI="$env:ACCOUNT_ID.dkr.ecr.$env:AWS_REGION.amazonaws.com/$env:APP_NAME"
```

## 3) Build and push Docker image to ECR

Create ECR repository (first time only):

```bash
aws ecr create-repository --repository-name ${APP_NAME} --region ${AWS_REGION}
```

Login, build, tag, and push:

```bash
aws ecr get-login-password --region ${AWS_REGION} \
  | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

docker build -t ${APP_NAME}:${IMAGE_TAG} .
docker tag ${APP_NAME}:${IMAGE_TAG} ${ECR_REPO_URI}:${IMAGE_TAG}
docker push ${ECR_REPO_URI}:${IMAGE_TAG}
```

## 4) Create CloudWatch logs group (first time only)

```bash
aws logs create-log-group --log-group-name /ecs/${APP_NAME} --region ${AWS_REGION}
```

## 5) Prepare task definition

1. Copy `deploy/ecs/task-definition.template.json` to a working file, for example `deploy/ecs/task-definition.json`.
2. Replace placeholders:
   - `<ACCOUNT_ID>`
   - `<AWS_REGION>`
3. Update image tag:
   - `...amazonaws.com/todo-app:latest` -> `...amazonaws.com/todo-app:${IMAGE_TAG}`

Register task definition:

```bash
aws ecs register-task-definition \
  --cli-input-json file://deploy/ecs/task-definition.json \
  --region ${AWS_REGION}
```

## 6) Create ECS cluster (first time only)

```bash
aws ecs create-cluster --cluster-name ${CLUSTER_NAME} --region ${AWS_REGION}
```

## 7) Create ECS service (first time only)

You need your subnet IDs and security group ID.

```bash
aws ecs create-service \
  --cluster ${CLUSTER_NAME} \
  --service-name ${SERVICE_NAME} \
  --task-definition ${APP_NAME} \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-aaa,subnet-bbb],securityGroups=[sg-xxx],assignPublicIp=DISABLED}" \
  --region ${AWS_REGION}
```

If you use an ALB, attach target group and configure health check path to:

- `/actuator/health`

## 8) Update deployment (new image version)

1. Build and push a new image tag.
2. Update `deploy/ecs/task-definition.json` image tag.
3. Register a new task definition revision.
4. Force service rollout:

```bash
aws ecs update-service \
  --cluster ${CLUSTER_NAME} \
  --service ${SERVICE_NAME} \
  --force-new-deployment \
  --region ${AWS_REGION}
```

## 9) Verify deployment

```bash
aws ecs describe-services \
  --cluster ${CLUSTER_NAME} \
  --services ${SERVICE_NAME} \
  --region ${AWS_REGION}
```

Check logs:

```bash
aws logs tail /ecs/${APP_NAME} --follow --region ${AWS_REGION}
```

## Notes

- This project currently uses **H2 in-memory database**, so data resets on task restart.
- For production, move to **Amazon RDS** and inject datasource settings via environment variables or Secrets Manager.
