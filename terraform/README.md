# Terraform Infrastructure for Banana Date

이 디렉토리는 Banana Date 프로젝트의 AWS 인프라를 관리하는 Terraform 코드를 포함합니다.

## 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                      Internet                            │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│           Application Load Balancer (ALB)                │
│              (Public Subnets)                            │
└──────────┬───────────────────────┬──────────────────────┘
           │                       │
┌──────────▼──────────┐  ┌────────▼──────────┐
│   ECS Fargate       │  │   ECS Fargate     │
│   Frontend          │  │   Backend         │
│   (Private Subnet)  │  │   (Private Subnet)│
└─────────────────────┘  └────────┬──────────┘
                                  │
                         ┌────────▼──────────┐
                         │  RDS PostgreSQL   │
                         │  (Private Subnet) │
                         └───────────────────┘
```

## 구성 요소

### VPC 모듈 (`modules/vpc/`)
- VPC 생성
- Public/Private Subnets (Multi-AZ)
- Internet Gateway
- NAT Gateways
- Route Tables

### ECR 모듈 (`modules/ecr/`)
- Frontend 컨테이너 레지스트리
- Backend 컨테이너 레지스트리
- 이미지 라이프사이클 정책 (최근 10개 이미지 유지)

### ALB 모듈 (`modules/alb/`)
- Application Load Balancer
- Frontend Target Group (Port 3000)
- Backend Target Group (Port 8080)
- HTTP Listener
- Path-based routing (/api/* → Backend)

### RDS 모듈 (`modules/rds/`)
- PostgreSQL 16.4
- Multi-AZ 지원
- 자동 백업 (7일 retention)
- Enhanced Monitoring
- Encryption at rest

### ECS 모듈 (`modules/ecs/`)
- ECS Cluster (Fargate)
- Frontend Service
- Backend Service
- CloudWatch Logs
- Auto Scaling 지원

## 사전 요구사항

1. **AWS CLI 설치 및 구성**
```bash
aws configure
```

2. **Terraform 설치**
```bash
# macOS
brew install terraform

# 또는 공식 사이트에서 다운로드
# https://www.terraform.io/downloads
```

3. **S3 백엔드 설정**

Terraform 상태 파일을 저장할 S3 버킷과 DynamoDB 테이블을 생성합니다:

```bash
# S3 버킷 생성
aws s3 mb s3://banana-date-terraform-state --region ap-northeast-2

# S3 버킷 버저닝 활성화
aws s3api put-bucket-versioning \
  --bucket banana-date-terraform-state \
  --versioning-configuration Status=Enabled

# DynamoDB 테이블 생성 (상태 잠금용)
aws dynamodb create-table \
  --table-name banana-date-terraform-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-2
```

## 사용 방법

### 1. 초기화

```bash
cd terraform
terraform init
```

### 2. 변수 설정

`terraform.tfvars` 파일 생성:

```hcl
aws_region = "ap-northeast-2"
environment = "prod"
project_name = "banana-date"

# VPC 설정
vpc_cidr = "10.0.0.0/16"
availability_zones = ["ap-northeast-2a", "ap-northeast-2c"]

# 데이터베이스 인증 정보 (민감 정보)
db_username = "bananadate"
db_password = "CHANGE_ME_SECURE_PASSWORD"

# Docker 이미지 태그
frontend_image_tag = "latest"
backend_image_tag = "latest"
```

**주의:** `terraform.tfvars` 파일은 절대 Git에 커밋하지 마세요!

### 3. 계획 확인

```bash
terraform plan
```

### 4. 인프라 배포

```bash
terraform apply
```

### 5. 출력 확인

```bash
terraform output

# 특정 출력만 확인
terraform output alb_dns_name
terraform output ecr_frontend_repository_url
```

### 6. 인프라 삭제

```bash
terraform destroy
```

## 프로덕션 환경 관리

이 프로젝트는 프로덕션 환경만 사용합니다.

```bash
# 프로덕션 환경 변수 파일 사용
terraform apply -var-file="environments/prod/terraform.tfvars"

# 또는 루트 디렉토리의 terraform.tfvars 사용
terraform apply
```

## 인프라 배포 방식

### 로컬에서 수동 배포

Terraform 인프라는 **로컬에서 직접 관리**합니다. GitHub Actions 자동화를 사용하지 않습니다.

```bash
# 인프라 배포
cd terraform
terraform init
terraform plan
terraform apply

# 인프라 변경사항 확인
terraform plan

# 인프라 업데이트
terraform apply

# 인프라 삭제 (주의!)
terraform destroy
```

### 애플리케이션 배포 (GitHub Actions)

Frontend와 Backend는 GitHub Actions를 통해 자동 배포됩니다.

- **Frontend**: `.github/workflows/deploy-frontend.yml`
- **Backend**: `.github/workflows/deploy-backend.yml`

### 필요한 GitHub Secrets (애플리케이션 배포용)

```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
NEXT_PUBLIC_API_URL
NEXT_PUBLIC_KAKAO_REST_API_KEY
NEXT_PUBLIC_GOOGLE_CLIENT_ID
NEXT_PUBLIC_KAKAO_MAP_API_KEY
```

## 모니터링 및 로그

### CloudWatch Logs

- Frontend 로그: `/ecs/banana-date-frontend-prod`
- Backend 로그: `/ecs/banana-date-backend-prod`

### CloudWatch 접속

```bash
aws logs tail /ecs/banana-date-backend-prod --follow
```

## 비용 최적화

### 프로덕션 환경 설정
- RDS: `db.t4g.micro` (시작), 필요 시 `db.t4g.small` 이상으로 확장
- ECS: Fargate 사용 (안정성 우선)
- NAT Gateway: Multi-AZ 구성
- Auto Scaling: 트래픽에 따른 자동 확장 설정 권장

## 보안 고려사항

1. **RDS 접근 제한**: ECS 보안 그룹에서만 접근 가능
2. **Private Subnets**: ECS 태스크와 RDS는 Private Subnet에서 실행
3. **Secrets 관리**: 민감한 정보는 AWS Secrets Manager 사용 권장
4. **IAM 최소 권한**: Task Role과 Execution Role 분리

## 트러블슈팅

### Terraform 상태 잠금 문제

```bash
# 강제로 잠금 해제 (주의!)
terraform force-unlock <LOCK_ID>
```

### RDS 접속 테스트

```bash
# Bastion 호스트를 통한 접속 (필요시)
aws ssm start-session --target <bastion-instance-id>
psql -h <rds-endpoint> -U bananadate -d bananadate
```

### ECS 태스크 로그 확인

```bash
aws ecs describe-tasks \
  --cluster banana-date-cluster-dev \
  --tasks <task-id>
```

## 참고 자료

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/intro.html)
- [Terraform Backend Configuration](https://www.terraform.io/docs/language/settings/backends/s3.html)
