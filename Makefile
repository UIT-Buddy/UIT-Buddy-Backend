.PHONY: run build clean test install help docker-up docker-down docker-logs dev prod local

SHELL := powershell.exe
.SHELLFLAGS := -NoProfile -Command

# Chạy ứng dụng (dev mode)
run:
	.\mvnw.cmd spring-boot:run

# Chạy ứng dụng với profile dev
dev:
	.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Chạy ứng dụng với profile prod
prod:
	.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod

# Build dự án
build:
	.\mvnw.cmd clean package

# Xóa các file build
clean:
	.\mvnw.cmd clean

# Chạy tests
test:
	.\mvnw.cmd test

# Cài đặt dependencies
install:
	.\mvnw.cmd clean install

# Build không chạy test
build-skip-test:
	.\mvnw.cmd clean package -DskipTests

# Chạy ở chế độ debug
debug:
	.\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Hiển thị thông tin dependencies
deps:
	.\mvnw.cmd dependency:tree

# Docker: Khởi động PostgreSQL và Redis
docker-up:
	podman compose up -d

# Docker: Dừng containers
docker-down:
	podman compose down

# Docker: Xem logs
docker-logs:
	podman compose logs -f

# Docker: Khởi động lại containers
docker-restart:
	podman compose restart

# Docker: Xóa containers và volumes
docker-clean:
	podman compose down -v

# Setup local: Khởi động Docker và chạy app
local:
	podman compose up -d
	@Write-Host "Waiting for database to be ready..." -ForegroundColor Yellow
	@Start-Sleep -Seconds 10
	Get-Content .env | ForEach-Object { if ($$_ -match '^([^#][^=]*)=(.*)$$') { [System.Environment]::SetEnvironmentVariable($$matches[1], $$matches[2]) } }; .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Hiển thị hướng dẫn
help:
	@Write-Host "UIT-Buddy Backend - Makefile Commands" -ForegroundColor Cyan
	@Write-Host "======================================" -ForegroundColor Cyan
	@Write-Host ""
	@Write-Host "Application:" -ForegroundColor Green
	@Write-Host "  make run              - Chạy ứng dụng (dev mode)"
	@Write-Host "  make dev              - Chạy với profile dev"
	@Write-Host "  make prod             - Chạy với profile prod"
	@Write-Host "  make local            - Setup local (Docker + App)"
	@Write-Host "  make build            - Build dự án"
	@Write-Host "  make clean            - Xóa các file build"
	@Write-Host "  make test             - Chạy tests"
	@Write-Host "  make install          - Cài đặt dependencies"
	@Write-Host "  make build-skip-test  - Build không chạy test"
	@Write-Host "  make debug            - Chạy ở chế độ debug (port 5005)"
	@Write-Host "  make deps             - Hiển thị dependency tree"
	@Write-Host ""
	@Write-Host "Docker:" -ForegroundColor Green
	@Write-Host "  make docker-up        - Khởi động PostgreSQL và Redis"
	@Write-Host "  make docker-down      - Dừng containers"
	@Write-Host "  make docker-logs      - Xem logs"
	@Write-Host "  make docker-restart   - Khởi động lại containers"
	@Write-Host "  make docker-clean     - Xóa containers và volumes"
	@Write-Host ""
	@Write-Host "  make help             - Hiển thị hướng dẫn này"
