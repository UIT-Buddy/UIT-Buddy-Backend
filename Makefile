MVN := .\mvnw.cmd
DOCKER_COMPOSE := podman compose

.PHONY: dev up down restart build logs db-shell redis-shell clean help

help: ## Show this help message
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*##' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*##"}; {printf "  %-15s %s\n", $$1, $$2}'

dev: ## Start infra (postgres, redis) and run app in dev mode
	@echo "Starting Infra..."
	$(DOCKER_COMPOSE) up -d postgres redis
	@echo "Waiting for DB..."
	@timeout /t 5 /nobreak > nul
	$(MVN) spring-boot:run "-Dspring-boot.run.profiles=dev"

up: ## Start all Docker containers
	$(DOCKER_COMPOSE) up -d

down: ## Stop all Docker containers
	$(DOCKER_COMPOSE) down

build: ## Build the JAR (skip tests)
	$(MVN) clean package -DskipTests

rebuild: ## Rebuild JAR and restart Docker containers
	@echo "Building JAR locally..."
	$(MVN) clean package -DskipTests
	@echo "Building and starting Docker containers..."
	$(DOCKER_COMPOSE) up -d --build --remove-orphans
	$(DOCKER_COMPOSE) ps
redis-shell:
	podman exec -it buddy-redis redis-cli
db-shell: ## Open a psql shell in the postgres container
	$(DOCKER_COMPOSE) exec postgres psql -U postgres -d buddy_db

logs: ## Tail Docker container logs
	$(DOCKER_COMPOSE) logs -f

clean: ## Clean Maven build and tear down containers with volumes
	$(MVN) clean
	$(DOCKER_COMPOSE) down -v --remove-orphans

test: ## Run all tests
	$(MVN) test

test-single: ## Run a single test class: make test-single t=MyTest
	$(MVN) test -Dtest=$(t)
	
test-clean: ## Clean then run all tests
	$(MVN) clean test

test-summary: ## Show test results summary
	@echo "Checking test reports..."
	@ls target/surefire-reports/*.xml >/dev/null 2>&1 || echo "No results found."


format: ## Format Java source code and organize imports
	./mvnw -q net.revelc.code.formatter:formatter-maven-plugin:2.24.1:format
	./mvnw -q net.revelc.code:impsort-maven-plugin:1.13.0:sort
