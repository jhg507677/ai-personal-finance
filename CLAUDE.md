# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

AI Personal Finance backend — Spring Boot 4.0.2, Java 17, Gradle 9.3.

## Build & Run Commands

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.codingcat.aipersonalfinance.SomeTest"

# Run a single test method
./gradlew test --tests "com.codingcat.aipersonalfinance.SomeTest.methodName"

# Clean build
./gradlew clean build
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Architecture

This is an early-stage project with a standard Spring Boot layout:

- **Package root:** `com.codingcat.aipersonalfinance`
- **Entry point:** `AiPersonalFinanceApplication.java`
- **Dependencies:** Spring Data JPA (persistence), Spring Web MVC (REST APIs)
- **Database:** No driver or datasource configured yet — a database dependency (e.g., H2, PostgreSQL) must be added before JPA will work
