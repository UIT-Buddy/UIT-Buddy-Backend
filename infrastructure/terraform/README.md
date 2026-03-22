# UIT-Buddy — AWS Infrastructure (Terraform)

Terraform code để triển khai toàn bộ hạ tầng AWS cho backend **UIT-Buddy** trên **ECS Fargate** (Serverless Container), bao gồm mạng VPC, cơ sở dữ liệu PostgreSQL, bộ nhớ đệm Redis, Application Load Balancer và quản lý bảo mật.

---

## Kiến trúc tổng quan

```
Internet
    │
    ▼
[ALB - port 80/443]          ← Public, nhận traffic từ Internet
    │
    ▼
[ECS Task: Backend + Redis]  ← Public Subnet (cần Public IP để pull image từ GHCR)
  - Spring Boot :8080
  - Redis :6379 (sidecar)
    │ (Cloud Map DNS: postgres.uitbuddy.local)
    ▼
[ECS Task: PostgreSQL]       ← Private Subnet (cô lập, không có Internet)
  - Postgres :5432
    │
    ▼
[EFS - Persistent Storage]   ← Dữ liệu không mất khi container restart
```

**Nguyên tắc thiết kế:**
- **Không dùng NAT Gateway** — tiết kiệm chi phí. Backend ở Public Subnet dùng Internet Gateway trực tiếp để pull image.
- **Postgres ở Private Subnet** — cách ly hoàn toàn, chỉ Backend mới kết nối được.
- **Không có password trong code** — tất cả secrets lưu trong AWS Secrets Manager.
- **Không cần SSH** — dùng ECS Exec (qua AWS SSM) để debug container.

---

## Cấu trúc file

```
terraform/
├── providers.tf      # AWS Provider và Terraform version
├── variables.tf      # Tất cả biến cấu hình
├── network.tf        # VPC, Subnets (2 Public + 2 Private), Internet Gateway, Route Tables
├── security.tf       # Security Groups (ALB → Backend → Postgres → EFS)
├── efs.tf            # EFS File System (lưu dữ liệu Postgres vĩnh viễn)
├── ecs_cluster.tf    # ECS Cluster + Cloud Map (DNS nội bộ)
├── ecs_postgres.tf   # Task Definition + Service cho PostgreSQL
├── ecs_backend.tf    # Task Definition (Backend + Redis sidecar) + Service
├── alb.tf            # Application Load Balancer
├── iam.tf            # IAM Roles & Policies (Execution Role, Task Role, ECS Exec)
└── secrets.tf        # AWS Secrets Manager (DB password + Firebase JSON)
```

---

## Yêu cầu

| Công cụ | Phiên bản tối thiểu |
|---------|---------------------|
| [Terraform](https://developer.hashicorp.com/terraform/install) | `>= 1.5.0` |
| [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) | `>= 2.0` |
| [Session Manager Plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html) | Mới nhất (để dùng ECS Exec) |
| Tài khoản AWS | Đã cấu hình credentials (`aws configure`) |

---

## Hướng dẫn triển khai từng bước

### Bước 1: Cấu hình AWS credentials

```bash
aws configure
# AWS Access Key ID: <your-key>
# AWS Secret Access Key: <your-secret>
# Default region name: ap-southeast-2
# Default output format: json
```

Kiểm tra đã đăng nhập thành công:
```bash
aws sts get-caller-identity
```

### Bước 2: Chuẩn bị file `terraform.tfvars`

Tạo file `terraform.tfvars` trong thư mục này:

```hcl
# terraform.tfvars — KHÔNG COMMIT FILE NÀY

# AWS Region
aws_region = "ap-southeast-1"

# PostgreSQL credentials
postgres_db   = "buddy_db"
postgres_user = "postgres"
db_password   = "your-strong-password-here"

# Nội dung file Firebase service account JSON
# Copy toàn bộ nội dung file uit-buddy-firebase-adminsdk-fbsvc-*.json vào đây
firebase_json = <<EOF
{
  "type": "service_account",
  "project_id": "uit-buddy",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-fbsvc@uit-buddy.iam.gserviceaccount.com",
  ...
}
EOF
```

> **Lưu ý:** File `terraform.tfvars` đã được thêm vào `.gitignore` tự động — không cần làm thêm gì.

Các biến khác đã có giá trị mặc định hợp lý. Bạn có thể ghi đè thêm nếu cần:

```hcl
# Tùy chọn — ghi đè giá trị mặc định
aws_region   = "ap-southeast-1"
project_name = "uitbuddy"
backend_cpu  = 1024   # 1 vCPU
backend_memory = 2048 # 2 GB RAM
```

### Bước 3: Khởi tạo Terraform

```bash
cd UIT-Buddy-Backend/infrastructure/terraform

terraform init
```

Lệnh này sẽ tải AWS Provider về máy.

### Bước 4: Xem trước những gì sẽ được tạo

```bash
terraform plan
```

Terraform sẽ liệt kê toàn bộ tài nguyên AWS sẽ được tạo (~30 resources). Kiểm tra kỹ trước khi apply.

### Bước 5: Triển khai lên AWS

```bash
terraform apply
```

Nhập `yes` khi được hỏi. Quá trình hoàn tất trong khoảng 3–5 phút.

Sau khi apply xong, Terraform sẽ in ra DNS của Load Balancer:

```
Outputs:
alb_dns_name = "uitbuddy-alb-123456789.ap-southeast-1.elb.amazonaws.com"
```

### Bước 6: Kiểm tra ứng dụng

```bash
# Kiểm tra health check của Spring Boot
curl http://<alb_dns_name>/actuator/health

# Kết quả mong đợi:
# {"status":"UP"}
```

---

## Tham chiếu biến (Variables)

| Tên biến | Mặc định | Bắt buộc | Mô tả |
|----------|----------|----------|-------|
| `aws_region` | `ap-southeast-2` | Không | AWS Region triển khai |
| `project_name` | `uitbuddy` | Không | Tên dự án, dùng để đặt tên resource |
| `vpc_cidr` | `10.0.0.0/16` | Không | CIDR của VPC |
| `az_a` | `ap-southeast-2a` | Không | Availability Zone A |
| `az_b` | `ap-southeast-2b` | Không | Availability Zone B |
| `postgres_db` | `uitbuddy` | Không | Tên database PostgreSQL |
| `postgres_user` | `admin` | Không | Username PostgreSQL |
| `db_password` | — | **Có** | Mật khẩu PostgreSQL (sensitive) |
| `firebase_json` | — | **Có** | Nội dung Firebase service account JSON (sensitive) |
| `backend_image` | `ghcr.io/uit-buddy/backend:latest` | Không | Docker image backend |
| `backend_cpu` | `1024` | Không | CPU units cho backend task (1024 = 1 vCPU) |
| `backend_memory` | `2048` | Không | RAM (MB) cho backend task |
| `postgres_cpu` | `1024` | Không | CPU units cho postgres task (1024 = 1 vCPU) |
| `postgres_memory` | `2048` | Không | RAM (MB) cho postgres task |
| `internal_dns_namespace` | `uitbuddy.local` | Không | DNS namespace nội bộ (Cloud Map) |

---

## Luồng bảo mật (Security Flow)

```
Internet (0.0.0.0/0)
    │  port 80, 443
    ▼
[ALB Security Group]
    │  port 8080 only
    ▼
[Backend Security Group]
    │  port 5432 only
    ▼
[Postgres Security Group]
    │  port 2049 (NFS) only
    ▼
[EFS Security Group]
```

Người ngoài Internet **chỉ chạm được vào Load Balancer**. Toàn bộ hệ thống bên trong không thể truy cập trực tiếp từ ngoài.

---

## Debug — Chui vào Container (ECS Exec)

Vì Fargate là Serverless, không dùng SSH được. Thay vào đó dùng **ECS Exec** qua AWS SSM.

### Yêu cầu

- Cài [Session Manager Plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html) trên máy local.
- Container phải đang ở trạng thái `RUNNING`.

### Lấy Task ID đang chạy

```bash
# Lấy Task ID của Postgres
aws ecs list-tasks \
  --cluster uitbuddy-cluster \
  --service-name uitbuddy-postgres \
  --query "taskArns[0]" \
  --output text

# Lấy Task ID của Backend
aws ecs list-tasks \
  --cluster uitbuddy-cluster \
  --service-name uitbuddy-backend \
  --query "taskArns[0]" \
  --output text
```

### Mở shell vào container Postgres

```bash
aws ecs execute-command \
  --cluster "uitbuddy-cluster" \
  --task "<task-id>" \
  --container "postgres" \
  --interactive \
  --command "/bin/sh"
```

### Mở shell vào container Backend

```bash
aws ecs execute-command \
  --cluster "uitbuddy-cluster" \
  --task "<task-id>" \
  --container "backend" \
  --interactive \
  --command "/bin/sh"
```

### Reset schema database (khi cần)

Sau khi đã vào bên trong container Postgres:

```bash
# Đăng nhập psql
psql -U admin -d uitbuddy

# Xóa và tạo lại schema (XÓA TOÀN BỘ DỮ LIỆU)
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

# Thoát
\q
exit
```

---

## Quản lý Secrets

Tất cả secrets được lưu trong **AWS Secrets Manager** tại key `uitbuddy/backend/secrets` dưới dạng JSON:

```json
{
  "POSTGRES_PASSWORD": "...",
  "FIREBASE_JSON": "..."
}
```

### Xem secrets hiện tại

```bash
aws secretsmanager get-secret-value \
  --secret-id "uitbuddy/backend/secrets" \
  --query SecretString \
  --output text
```

### Cập nhật secrets (không cần terraform apply)

```bash
aws secretsmanager put-secret-value \
  --secret-id "uitbuddy/backend/secrets" \
  --secret-string '{"POSTGRES_PASSWORD":"new-password","FIREBASE_JSON":"{...}"}'
```

Sau khi cập nhật secret, **force deploy** ECS service để container load lại:

```bash
aws ecs update-service \
  --cluster uitbuddy-cluster \
  --service uitbuddy-backend \
  --force-new-deployment
```

---

## Destroy — Xóa toàn bộ hạ tầng

> **Cảnh báo:** Lệnh này xóa vĩnh viễn tất cả tài nguyên AWS bao gồm cả dữ liệu EFS.

```bash
terraform destroy
```

---

## Cấu trúc IAM (Phân quyền)

| Role | Dùng bởi | Quyền |
|------|----------|-------|
| `uitbuddy-ecs-execution-role` | Fargate engine | Pull image, ghi CloudWatch logs, đọc Secrets Manager |
| `uitbuddy-ecs-task-role` | Code bên trong container | Gọi SSM (ECS Exec); mở rộng thêm S3, v.v. khi cần |
