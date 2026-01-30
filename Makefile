# ===========================================
# UIT-Buddy Backend - Makefile
# ===========================================
# Unified management for development and production
#
# Quick Start:
#   make run    - Start all services in Docker (production-like)
#   make local  - Start DB in Docker + Backend locally (development)
#   make help   - Show all available commands
# ===========================================

.DEFAULT_GOAL := help

SHELL := powershell.exe
.SHELLFLAGS := -NoProfile -Command

# ===========================================
# HELP
# ===========================================

help: ## Show this help message
	@Write-Host ""
	@Write-Host "============================================================" -ForegroundColor Cyan
	@Write-Host "       UIT-Buddy Backend - Makefile Commands" -ForegroundColor Cyan
	@Write-Host "============================================================" -ForegroundColor Cyan
	@Write-Host ""
	@Write-Host "[QUICK START]" -ForegroundColor Green
	@Write-Host "  make run                - Start ALL services in Docker (production-like)"
	@Write-Host "  make local              - Start DB in Docker + Backend locally (development)"
	@Write-Host "  make stop               - Stop all services"
	@Write-Host ""
	@Write-Host "[DOCKER - ALL SERVICES]" -ForegroundColor Green
	@Write-Host "  make up                 - Start all Docker services"
	@Write-Host "  make down               - Stop all Docker services"
	@Write-Host "  make restart            - Restart all Docker services"
	@Write-Host "  make logs               - Show all Docker logs"
	@Write-Host "  make status             - Show status of all services"
	@Write-Host "  make clean              - Stop and remove containers"
	@Write-Host "  make clean-all          - Remove everything including volumes"
	@Write-Host ""
	@Write-Host "[DOCKER - INDIVIDUAL SERVICES]" -ForegroundColor Green
	@Write-Host "  make backend-logs       - Show backend logs"
	@Write-Host "  make backend-restart    - Restart backend container"
	@Write-Host "  make postgres-logs      - Show PostgreSQL logs"
	@Write-Host "  make redis-logs         - Show Redis logs"
	@Write-Host ""
	@Write-Host "[INFRASTRUCTURE ONLY]" -ForegroundColor Green
	@Write-Host "  make infra-up           - Start only PostgreSQL and Redis"
	@Write-Host "  make infra-down         - Stop PostgreSQL and Redis"
	@Write-Host ""
	@Write-Host "[LOCAL DEVELOPMENT]" -ForegroundColor Green
	@Write-Host "  make local              - Start infra + run backend locally"
	@Write-Host "  make dev                - Start only DB containers (for DevTools)"
	@Write-Host "  make run-local          - Run backend locally with dev profile"
	@Write-Host "  make debug              - Run backend with debug port 5005"
	@Write-Host ""
	@Write-Host "[BUILD & TEST]" -ForegroundColor Green
	@Write-Host "  make build              - Build project (mvn clean package)"
	@Write-Host "  make build-skip-test    - Build without running tests"
	@Write-Host "  make test               - Run all tests"
	@Write-Host "  make compile            - Compile project"
	@Write-Host "  make clean-maven        - Clean Maven build files"
	@Write-Host "  make deps               - Show dependency tree"
	@Write-Host ""
	@Write-Host "[UTILITIES]" -ForegroundColor Green
	@Write-Host "  make rebuild            - Rebuild and restart all Docker services"
	@Write-Host "  make shell-postgres     - Open PostgreSQL shell"
	@Write-Host "  make shell-redis        - Open Redis CLI"
	@Write-Host ""
	@Write-Host "============================================================" -ForegroundColor Cyan
	@Write-Host ""

# ===========================================
# QUICK START COMMANDS
# ===========================================

run: up status ## Start all services in Docker (production-like)
	@Write-Host ""
	@Write-Host "============================================================" -ForegroundColor Green
	@Write-Host "All services started in Docker!" -ForegroundColor Green
	@Write-Host "============================================================" -ForegroundColor Green
	@Write-Host ""
	@Write-Host "Backend API:  http://localhost:8080" -ForegroundColor Cyan
	@Write-Host "Swagger UI:   http://localhost:8080/swagger-ui/index.html" -ForegroundColor Cyan
	@Write-Host ""
	@Write-Host "View logs: make logs" -ForegroundColor Yellow
	@Write-Host ""

local: infra-up _wait-db _run-local ## Start DB in Docker + Backend locally (development)

stop: down ## Stop all services
	@Write-Host "All services stopped!" -ForegroundColor Yellow

# ===========================================
# DOCKER - ALL SERVICES
# ===========================================

up: ## Start all Docker services
	@Write-Host "Starting all services..." -ForegroundColor Cyan
	docker compose up -d
	@Write-Host "All services started!" -ForegroundColor Green

down: ## Stop all Docker services
	@Write-Host "Stopping all services..." -ForegroundColor Yellow
	docker compose down
	@Write-Host "All services stopped!" -ForegroundColor Green

restart: ## Restart all Docker services
	@Write-Host "Restarting all services..." -ForegroundColor Yellow
	docker compose restart
	@Write-Host "All services restarted!" -ForegroundColor Green

logs: ## Show all Docker logs
	docker compose logs -f

status: ## Show status of all services
	@Write-Host ""
	@Write-Host "Service Status:" -ForegroundColor Cyan
	docker compose ps
	@Write-Host ""

clean: ## Stop and remove containers
	@Write-Host "Cleaning up containers..." -ForegroundColor Yellow
	docker compose down --remove-orphans
	@Write-Host "Cleanup complete!" -ForegroundColor Green

clean-all: ## Remove everything including volumes (WARNING: deletes all data)
	@Write-Host "WARNING: This will remove ALL data including databases!" -ForegroundColor Red
	docker compose down -v --remove-orphans
	@Write-Host "Everything cleaned!" -ForegroundColor Green

rebuild: down ## Rebuild and restart all Docker services
	@Write-Host "Rebuilding all services..." -ForegroundColor Yellow
	docker compose build --no-cache
	docker compose up -d
	@Write-Host "Rebuild complete!" -ForegroundColor Green

# ===========================================
# DOCKER - INDIVIDUAL SERVICES
# ===========================================

backend-logs: ## Show backend logs
	docker compose logs -f backend

backend-restart: ## Restart backend container
	docker compose restart backend

postgres-logs: ## Show PostgreSQL logs
	docker compose logs -f postgres

redis-logs: ## Show Redis logs
	docker compose logs -f redis

# ===========================================
# INFRASTRUCTURE ONLY
# ===========================================

infra-up: ## Start only PostgreSQL and Redis (for local development)
	@Write-Host "Starting infrastructure services..." -ForegroundColor Cyan
	-docker compose stop backend
	docker compose up -d postgres redis
	@Write-Host ""
	@Write-Host "Infrastructure started:" -ForegroundColor Green
	@Write-Host "  PostgreSQL: localhost:5433" -ForegroundColor Cyan
	@Write-Host "  Redis:      localhost:6379" -ForegroundColor Cyan
	@Write-Host ""

infra-down: ## Stop PostgreSQL and Redis
	@Write-Host "Stopping infrastructure services..." -ForegroundColor Yellow
	docker compose stop postgres redis

# ===========================================
# LOCAL DEVELOPMENT
# ===========================================

_wait-db:
	@Write-Host "Waiting for database to be ready..." -ForegroundColor Yellow
	@Start-Sleep -Seconds 5

_run-local:
	@Write-Host ""
	@Write-Host "============================================================" -ForegroundColor Green
	@Write-Host "Starting backend locally..." -ForegroundColor Green
	@Write-Host "============================================================" -ForegroundColor Green
	@Write-Host ""
	@powershell -ExecutionPolicy Bypass -File .\scripts\run-local.ps1 -Profile dev

dev: infra-up ## Start only DB containers (for Spring DevTools development)
	@Write-Host ""
	@Write-Host "============================================================" -ForegroundColor Green
	@Write-Host "Development environment ready!" -ForegroundColor Green
	@Write-Host "============================================================" -ForegroundColor Green
	@Write-Host ""
	@Write-Host "PostgreSQL: localhost:5433" -ForegroundColor Cyan
	@Write-Host "Redis:      localhost:6379" -ForegroundColor Cyan
	@Write-Host ""
	@Write-Host "Now run your Spring Boot app from IDE or:" -ForegroundColor Yellow
	@Write-Host "  make run-local" -ForegroundColor Yellow
	@Write-Host ""
	@Write-Host "Spring DevTools will auto-reload on code changes!" -ForegroundColor Green
	@Write-Host ""

run-local: ## Run backend locally with dev profile (requires infra running)
	@Write-Host "Running backend with dev profile..." -ForegroundColor Cyan
	.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

debug: ## Run backend with debug port 5005
	@Write-Host "Running backend in debug mode (port 5005)..." -ForegroundColor Cyan
	.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.jvmArguments=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005"

# ===========================================
# BUILD & TEST
# ===========================================

build: ## Build project (mvn clean package)
	@Write-Host "Building project..." -ForegroundColor Cyan
	.\mvnw.cmd clean package
	@Write-Host "Build complete!" -ForegroundColor Green

build-skip-test: ## Build without running tests
	@Write-Host "Building project (skipping tests)..." -ForegroundColor Cyan
	.\mvnw.cmd clean package -DskipTests
	@Write-Host "Build complete!" -ForegroundColor Green

test: ## Run all tests
	@Write-Host "Running tests..." -ForegroundColor Cyan
	.\mvnw.cmd test

compile: ## Compile project
	@Write-Host "Compiling project..." -ForegroundColor Cyan
	.\mvnw.cmd compile

clean-maven: ## Clean Maven build files
	@Write-Host "Cleaning Maven build files..." -ForegroundColor Yellow
	.\mvnw.cmd clean
	@Write-Host "Clean complete!" -ForegroundColor Green

deps: ## Show dependency tree
	.\mvnw.cmd dependency:tree

# ===========================================
# UTILITIES
# ===========================================

shell-postgres: ## Open PostgreSQL shell
	@Write-Host "Connecting to PostgreSQL..." -ForegroundColor Cyan
	docker compose exec postgres psql -U postgres -d buddy_db

shell-redis: ## Open Redis CLI
	@Write-Host "Connecting to Redis..." -ForegroundColor Cyan
	docker compose exec redis redis-cli

# ===========================================
# PHONY TARGETS
# ===========================================

.PHONY: help run local stop \
	up down restart logs status clean clean-all rebuild \
	backend-logs backend-restart postgres-logs redis-logs \
	infra-up infra-down \
	dev run-local debug \
	build build-skip-test test compile clean-maven deps \
	shell-postgres shell-redis \
	_wait-db _run-local