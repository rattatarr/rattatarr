.PHONY: help start test test-ci refresh-packages update-packages update-packages-all clean build install package run dev logs format lint validate coverage docker-build docker-run

# Default target
.DEFAULT_GOAL := help

# Colors for output
CYAN := \033[0;36m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

help: ## Show this help message
	@echo "$(CYAN)Available targets:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

start: ## Start the application
	@echo "$(CYAN)Starting application...$(NC)"
	./mvnw spring-boot:run

test: ## Run all tests
	@echo "$(CYAN)Running tests...$(NC)"
	./mvnw clean test

test-ci: ## Test CI workflow locally using act
	@echo "$(CYAN)Running CI workflow locally with act...$(NC)"
	@echo "$(YELLOW)Note: Test reporter will be skipped locally$(NC)"
	act push -j test

refresh-packages: ## Show available Maven dependency updates
	@echo "$(CYAN)Refreshing Maven packages...$(NC)"
	./mvnw versions:display-dependency-updates
	./mvnw versions:display-plugin-updates

update-packages: ## Apply stable dependency updates (no alphas/RCs) then build
	@echo "$(CYAN)Updating Maven packages to latest stable...$(NC)"
	./mvnw versions:use-latest-releases
	./mvnw clean install -DskipTests

update-packages-all: ## Apply ALL dependency updates including alphas/RCs then build
	@echo "$(YELLOW)Updating Maven packages to latest (including pre-releases)...$(NC)"
	./mvnw versions:use-latest-versions
	./mvnw clean install -DskipTests

clean: ## Clean build artifacts
	@echo "$(CYAN)Cleaning build artifacts...$(NC)"
	./mvnw clean

build: ## Build the application (compile + test)
	@echo "$(CYAN)Building application...$(NC)"
	./mvnw clean install

install: ## Install dependencies and build without tests
	@echo "$(CYAN)Installing dependencies...$(NC)"
	./mvnw clean install -DskipTests

package: ## Package the application as JAR
	@echo "$(CYAN)Packaging application...$(NC)"
	./mvnw clean package

run: package ## Package and run the application
	@echo "$(CYAN)Running packaged application...$(NC)"
	java -jar target/*.jar

dev: ## Run in development mode with hot reload
	@echo "$(CYAN)Starting in development mode...$(NC)"
	./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev"

logs: ## Tail application logs
	@echo "$(CYAN)Tailing logs...$(NC)"
	tail -f logs/*.log 2>/dev/null || echo "$(YELLOW)No log files found$(NC)"

format: ## Format code using Spotless
	@echo "$(CYAN)Formatting code...$(NC)"
	./mvnw spotless:apply

lint: ## Check code formatting
	@echo "$(CYAN)Checking code format...$(NC)"
	./mvnw spotless:check

validate: ## Validate project structure and dependencies
	@echo "$(CYAN)Validating project...$(NC)"
	./mvnw validate

coverage: ## Generate test coverage report
	@echo "$(CYAN)Generating coverage report...$(NC)"
	./mvnw clean test jacoco:report
	@echo "$(GREEN)Coverage report available at: target/site/jacoco/index.html$(NC)"

coverage-open: coverage ## Generate and open coverage report
	@echo "$(CYAN)Opening coverage report...$(NC)"
	@if command -v xdg-open > /dev/null; then \
		xdg-open target/site/jacoco/index.html; \
	elif command -v open > /dev/null; then \
		open target/site/jacoco/index.html; \
	else \
		echo "$(YELLOW)Please open target/site/jacoco/index.html manually$(NC)"; \
	fi

docker-build: package ## Build Docker image
	@echo "$(CYAN)Building Docker image...$(NC)"
	docker build -t rattatarr:latest .

docker-run: ## Run Docker container
	@echo "$(CYAN)Running Docker container...$(NC)"
	docker run -p 8080:8080 rattatarr:latest

verify: ## Run all verification steps (format, lint, test, coverage)
	@echo "$(CYAN)Running full verification...$(NC)"
	@$(MAKE) format
	@$(MAKE) lint
	@$(MAKE) test
	@$(MAKE) coverage

quick-test: ## Run tests without cleaning
	@echo "$(CYAN)Running quick tests...$(NC)"
	./mvnw test

watch-test: ## Watch and run tests on file changes
	@echo "$(CYAN)Watching tests...$(NC)"
	./mvnw test -Dspring-boot.run.jvmArguments="-Dspring.test.watch=true"

deps: ## Show dependency tree
	@echo "$(CYAN)Showing dependency tree...$(NC)"
	./mvnw dependency:tree

deps-analyze: ## Analyze dependencies for issues
	@echo "$(CYAN)Analyzing dependencies...$(NC)"
	./mvnw dependency:analyze

security-check: ## Check for security vulnerabilities
	@echo "$(CYAN)Checking for security vulnerabilities...$(NC)"
	./mvnw org.owasp:dependency-check-maven:check

db-reset: ## Reset the database
	@echo "$(CYAN)Resetting database...$(NC)"
	rm -f rattatarr.db
	@echo "$(GREEN)Database reset complete$(NC)"

info: ## Show project information
	@echo "$(CYAN)Project Information:$(NC)"
	@echo "  Name: Rattatarr Server"
	@echo "  Description: Spring Boot application"
	@echo "  Java Version: 21"
	@echo "  Build Tool: Maven"
	@echo ""
	@echo "$(CYAN)Useful Commands:$(NC)"
	@echo "  make start        - Start the application"
	@echo "  make test         - Run tests"
	@echo "  make test-ci      - Test CI locally"
	@echo "  make coverage     - Generate coverage report"
	@echo "  make verify       - Full verification suite"

pre-commit: verify ## Run pre-commit checks (format, lint, test)
	@echo "$(GREEN)✓ All pre-commit checks passed!$(NC)"

pre-push: verify ## Run pre-push checks (full verification)
	@echo "$(GREEN)✓ Ready to push!$(NC)"

ci-local: test-ci ## Alias for test-ci

all: clean build ## Clean and build everything
	@echo "$(GREEN)✓ Build complete!$(NC)"
