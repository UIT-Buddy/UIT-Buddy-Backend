MVN := .\mvnw.cmd
DOCKER_COMPOSE := podman compose

.PHONY: dev up down restart build logs db-shell redis-shell clean

dev:
	@echo "Starting Infra..."
	$(DOCKER_COMPOSE) up -d postgres redis
	@echo "Waiting for DB..."
	@timeout /t 5 /nobreak > nul
	$(MVN) spring-boot:run "-Dspring-boot.run.profiles=dev"

up:
	$(DOCKER_COMPOSE) up -d

down:
	$(DOCKER_COMPOSE) down

build:
	$(MVN) clean package -DskipTests

rebuild:
	@echo "Building JAR locally..."
	$(MVN) clean package -DskipTests
	@echo "Building and starting Docker containers..."
	$(DOCKER_COMPOSE) up -d --build --remove-orphans
	$(DOCKER_COMPOSE) ps

db-shell:
	$(DOCKER_COMPOSE) exec postgres psql -U postgres -d buddy_db

logs:
	$(DOCKER_COMPOSE) logs -f

clean:
	$(MVN) clean
	$(DOCKER_COMPOSE) down -v --remove-orphans
	