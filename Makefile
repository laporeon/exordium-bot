.DEFAULT_GOAL := help

JAR     := target/javoid-bot.jar
MVN ?= ./mvnw

.PHONY: help env build compile verify clean run

help: ## Show available prefixCommands
	@awk 'BEGIN {FS = ":.*## "} /^[a-zA-Z_-]+:.*## / {printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

env: ## Create .env from .env.example if missing
	@test -f .env || cp .env.example .env

build: ## Clean and build the executable JAR
	$(MVN) clean package

compile: ## Compile the project
	$(MVN) compile

verify: ## Run Maven verification
	$(MVN) verify

clean: ## Clean Maven build artifacts
	$(MVN) clean

run: build ## Build and run the bot locally
	java -jar $(JAR)
