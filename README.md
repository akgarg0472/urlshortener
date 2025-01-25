# URL Shortener Service

![Java Version](https://img.shields.io/badge/Java-21-orange)
![version](https://img.shields.io/badge/version-1.2.3-blue)

## Table of Contents

- [Introduction](#introduction)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
    - [application.yml](#applicationyml)
    - [application-dev.yml](#application-devyml)
    - [application-prod.yml](#application-prodyml)
- [Logging Configuration](#logging-configuration)
- [Environment Configuration](#environment-configuration)
- [Running the Application](#running-the-application)
- [Docker Setup](#docker-setup)
- [API Documentation](#api-documentation)
- [Additional Notes](#additional-notes)

## Introduction

The **URL Shortener Service** is a microservice designed to create short URLs for long links, with a focus on high
availability and performance. It utilizes **Spring Boot**, **MongoDB** for storing shortened URLs, and **Kafka** for
event-driven communication. This service is registered with **Eureka** for service discovery and is production-ready.

## Prerequisites

Before running the service, ensure the following are installed and running:

- **Java 21+** (JDK)
- **Maven** (for building the project)
- **MongoDB** (for storing URL mappings)
- **Kafka** (for publishing and consuming statistics events)
- **Eureka Server** (for service discovery)

## Installation

### Clone the Repository

```bash
git clone https://github.com/akgarg0472/urlshortener
cd urlshortener
```

### Build the Application

Run the following Maven command to build the application:

```bash
./mvnw clean package -DskipTests
```

## Configuration

The application has two primary configuration files:

### application.yml

This configuration file contains general settings for the application.

```yml
url:
  shortener:
    domain: http://127.0.0.1:8765/

spring:
  application:
    name: urlshortener-service
  jackson:
    default-property-inclusion: non_null

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    prometheus:
      access: read_only
```

#### Key Features:

- **Eureka Client**: Registers the service with the Eureka server.
- **Prometheus Metrics**: Exposes health and Prometheus endpoints for monitoring.
- **Base Domain**: Configures the base domain for short URLs.

### application-dev.yml

This configuration file is used for the development environment and excludes JPA autoconfiguration.

```yml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration

server:
  port: 9090
  address: 127.0.0.1
```

#### Key Features:

- **JPA Exclusion**: Disables JPA auto-configuration for development purposes.
- **Localhost Binding**: Configures the service to run on 127.0.0.1 for development.

### application-prod.yml

This configuration file is tailored for production environments.

```yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
  data:
    mongodb:
      uri: mongodb://admin:admin@127.0.0.1:27017/urlshortener?authSource=admin

kafka:
  statistics:
    topic:
      name: urlshortener.statistics.events
      partitions: 1
      replication-factor: 1

server:
  port: 9090

url:
  shortener:
    domain: localhost:8765/

process:
  node:
    id: ${PROCESS_NODE_ID:1}
```

#### Key Features:

- **MongoDB**: Configures the MongoDB connection with authentication.
- **Kafka**: Configures Kafka settings for publishing statistics events.
- **Port Configuration**: Runs the service on port 9090.

## Logging Configuration

The URL Shortener Service uses environment variables for logging configuration. Here are the available environment
variables that you can customize for logging:

- **LOG_LEVEL**: Specifies the log level for the application (e.g., `INFO`, `DEBUG`, `WARN`).
    - Default value: `INFO`

- **LOG_PATH**: Specifies the base path for log files.
    - Default value: `/tmp/logs/urlshortener`

- **LOGGER_REF**: Specifies the appender reference to use for logging.
    - Default value: `consoleLogger`
    - Allowed values: `consoleLogger`, `fileLogger`

## Environment Configuration

- **PROCESS_NODE_ID**:
    - **Description**: The unique identifier for the node instance. This property ensures that the short URLs generated
      by the service are unique across different instances. It should be an integer to represent the node's ID.
    - **Default**: `1` (Example: for the first instance, use `1`; for the second instance, use `2`, and so on.)

- **URL_SHORTENER_DOMAIN**:
    - **Description**: The base URL for the URL shortener. It defines the domain where the shortened URLs will be
      generated. Example: `bit.ly/`.
    - **Default**: `localhost:8765/` (for local development)

## Running the Application

### Local Execution

Run the application locally using Maven:

```bash
./mvnw spring-boot:run
```

By default, the application starts on http://localhost:9090 with the dev/prod profile.

### Production Profile

To run the application in production mode:

```bash
java -Dspring.profiles.active=prod -jar target/UrlShortenerService.jar
```

## Docker Setup

The application is Dockerized for simplified deployment. The `Dockerfile` is already configured to build and run the
Spring Boot application.

The `Dockerfile` defines the build and runtime configuration for the container.

### Building the Docker Image

To build the Docker image, run the following command:

```bash
docker build -t akgarg0472/urlshortener-service:tag .
```

### Run the Docker Container

You can run the application with custom environment variables using the docker run command. For example:

```bash
docker run -p 9090:9090 \
           -e SPRING_PROFILES_ACTIVE=prod \
           -e PROCESS_NODE_ID=1 \
           akgarg0472/urlshortener-service:tag
```

This will start the container with the necessary environment variables.

## API Documentation

The **API Documentation** for the URL Shortener Profile Service is automatically generated using **Springdoc OpenAPI**
and can be accessed at the following endpoints:

1. **OpenAPI Specification**: Available at:

    ```text
    http://<host>:<port>/api-docs
    ```

   This provides the raw OpenAPI specification in JSON format, which can be used for integrations or importing into API
   tools.

2. **Swagger UI**: The user-friendly API documentation is accessible at:

    ```text
    http://<host>:<port>/docs
    ```

   Replace `<host>` and `<port>` with your application's host and port. For example, if running locally:

- OpenAPI Specification: [http://localhost:9090/api-docs](http://localhost:9090/api-docs)
- Swagger UI: [http://localhost:9090/docs](http://localhost:9090/docs)

The Swagger UI provides detailed information about the available endpoints, including request and response formats,
sample payloads, and error codes, making it easy for developers to integrate with the service.

## Additional Notes

- **Database Connection**: Ensure MongoDB is running and accessible at the URI specified in the configuration file.
- **Kafka Topics**: Pre-create the Kafka topic for statistics events if auto-creation is disabled in your Kafka setup.
- **Eureka Registration**: Verify that the Eureka server is running at the URL configured in application.yml.
