# URL Shortener Service

## Overview

This is a URL Shortener service built using Spring Boot. It allows users to shorten long URLs into short ones and store
them in a MongoDB database. The application uses Spring Kafka for messaging and Eureka for service discovery, making it
a scalable and robust solution.

## Project Setup

The project consists of different configuration files that define how the application behaves in different
environments (development and production). These files are written in YAML format and configure Spring Boot settings,
including database connections, messaging, and service discovery.

### Key Technologies

- **Spring Boot**: A Java-based framework used to build the application.
- **MongoDB**: NoSQL database used to store the shortened URLs.
- **Eureka**: Service registry for service discovery.
- **Kafka**: Message broker used for event streaming and communication.
- **Spring Cloud**: For managing configuration and services.

---

## Configuration Files

The project contains three main configuration files:

1. **`application.yml`**: The base configuration file used for common settings across all environments.
2. **`application-dev.yml`**: Configuration for the development environment.
3. **`application-prod.yml`**: Configuration for the production environment.

### 1. `application.yml`

This is the base configuration file used for defining common settings for the application that apply across all
environments. The updated configuration includes the following:

- **`url.shortener.domain`**:
    - This property defines the base URL where the URL shortener service is accessible. In this case, it is set to
      `http://127.0.0.1:8765/`, which means the service is available on the local machine at port `8765`. This URL will
      be used to generate shortened URLs.

- **`spring.application.name`**:
    - This sets the name of the Spring Boot application, which in this case is `urlshortener-service`. The application
      name is used internally by Spring Boot for various purposes, such as logging, monitoring, and identifying the
      service in distributed systems.

- **`spring.jackson.default-property-inclusion`**:
    - This configuration specifies how Jackson (the JSON serializer used by Spring Boot) should handle null properties
      during serialization. Setting this to `non_null` ensures that only properties with non-null values are included in
      the JSON output. This helps to avoid unnecessary `null` values in the API responses.

- **Eureka Client Configuration**:
    - **`eureka.client.service-url.defaultZone`**:
        - This defines the URL for the Eureka server, which is used for service registration and discovery. In this
          case, the service registers itself with the Eureka server at `http://localhost:8761/eureka/`.
    - **`eureka.client.enabled`**:
        - This enables the Eureka client, allowing the service to register with the Eureka server for service discovery.
          It is set to `true` to enable this feature.

- **Management Endpoints**:
    - **`management.endpoints.web.exposure.include`**:
        - This configuration specifies which management endpoints should be exposed through the web. By setting it to
          `health,prometheus`, the service exposes two endpoints:
            - `health`: This endpoint provides the health status of the application.
            - `prometheus`: This endpoint exposes metrics in a format that can be scraped by Prometheus for monitoring.
    - **`management.endpoint.prometheus.access`**:
        - This configures the access level for the `prometheus` endpoint. Setting it to `read_only` ensures that the
          metrics exposed by Prometheus can be accessed for monitoring purposes but cannot be modified.

---

These settings configure core aspects of the URL shortener service, including the URL base, service discovery, and the
management endpoints for health checks and monitoring with Prometheus.

### 2. `application-dev.yml`

This configuration is used for the development environment, where specific settings are adjusted to suit local
development needs. The key configurations in this file are:

- **`spring.autoconfigure.exclude`**:
    - **`org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration`**:
        - This property excludes the autoconfiguration for JPA repositories. Since this project uses **MongoDB** as the
          data store instead of a relational database, JPA (Java Persistence API) configuration is unnecessary. By
          excluding it, Spring Boot avoids trying to autoconfigure the JPA data source and repositories, ensuring that
          MongoDB-specific configurations are applied.

- **Server Configuration**:
    - **`server.port`**:
        - This defines the port on which the application will run. In the development environment, it is set to port
          `9090`. When the application is started, it will listen for HTTP requests on `http://127.0.0.1:9090`.
    - **`server.address`**:
        - This configuration binds the application to the IP address `127.0.0.1` (localhost). This ensures that the
          application will only be accessible from the local machine during development, providing a safe environment
          for local testing.

---

These settings ensure that the application runs smoothly in the development environment by disabling unnecessary
configurations like JPA autoconfiguration and specifying local server settings (port and address).

### 3. `application-prod.yml`

This configuration is used for the production environment. It includes settings that are tailored for a live or
production deployment, such as Kafka integration, MongoDB database configuration, and server settings. The key
configurations in this file are:

- **Spring Kafka Configuration**:
    - **`spring.kafka.bootstrap-servers`**:
        - This property defines the address of the Kafka broker(s) the application will connect to. In this case, it is
          set to `localhost:9092`, indicating that the Kafka server is running locally on port `9092`. Kafka is used for
          event-driven messaging, so this setting ensures that the application can send and receive messages for event
          streaming.

- **MongoDB Configuration**:
    - **`spring.data.mongodb.uri`**:
        - This property specifies the connection URI for MongoDB, where the URL data will be stored. The URI
          `mongodb://admin:admin@127.0.0.1:27017/urlshortener?authSource=admin` includes:
            - **`admin:admin`**: MongoDB credentials (username and password) for accessing the database.
            - **`127.0.0.1:27017`**: The host and port where MongoDB is running (in this case, locally on port `27017`).
            - **`urlshortener`**: The name of the MongoDB database used to store the shortened URLs.
            - **`authSource=admin`**: This specifies the authentication source (`admin` database) for MongoDB.

- **Kafka Topic Configuration**:
    - **`kafka.statistics.topic.name`**:
        - Defines the name of the Kafka topic for collecting event statistics related to the URL shortener service. In
          this case, the topic is named `urlshortener.statistics.events`.
    - **`kafka.statistics.topic.partitions`**:
        - This setting specifies the number of partitions for the Kafka topic. Here it is set to `1`, meaning there is a
          single partition for the topic. More partitions can be added for scaling purposes.
    - **`kafka.statistics.topic.replication-factor`**:
        - Defines the replication factor for the Kafka topic. It is set to `1`, meaning there is one replica of the
          data. In production environments, higher replication factors are typically used for fault tolerance and
          redundancy.

- **Server Configuration**:
    - **`server.port`**:
        - This property defines the port on which the application will run in the production environment. It is set to
          port `9090`. This ensures the application is accessible at `http://localhost:9090/`.

- **URL Shortener Domain**:
    - **`url.shortener.domain`**:
        - This property specifies the base domain for the URL shortener service in the production environment. It is set
          to `localhost:8765/`, meaning that the shortener service will be available on the local machine at port
          `8765`.

---

These settings ensure that the application is properly connected to both Kafka and MongoDB, with the appropriate topic
configuration for event streaming and database connection in the production environment. The server is set to run on
port `9090`, with the URL shortener service accessible at `localhost:8765/`.

## Environment Configuration

### Development (`application-dev.yml`)

In the development environment, the application runs locally on port `9090`, excluding JPA repositories since MongoDB is
used for data storage. The server listens on `127.0.0.1` (localhost).

### Production (`application-prod.yml`)

In the production environment, the application connects to a Kafka broker and MongoDB running locally. Kafka is used for
event streaming, while MongoDB stores the shortened URLs. The server configuration also ensures the application runs on
port `9090` in production.

---

## Running the Application

1. **Set up MongoDB**: Ensure that MongoDB is installed and running locally. Create the `urlshortener` database if
   needed.

2. **Set up Kafka**: Make sure Kafka is running on `localhost:9092` to handle event streaming.

3. **Build project**: Build the project by running the following command to compile the application:

    ```shell
    ./mvnw clean package
    ```

4. **Run the Application**: Use Maven to start the Spring Boot application:

      ```shell
       java -DLOG_PATH=/tmp -jar target/UrlShortenerService.jar --spring.profiles.active=dev
      ```

5. **Access the Service**: The URL shortener service will be available at `http://127.0.0.1:9090/` in the development
   environment and `localhost:9090/` in production.

---

## Additional Notes

- **Eureka**: The service registers itself with Eureka for service discovery, allowing other services in the system to
  locate it.
- **MongoDB**: MongoDB is used for storing the URLs. Ensure the MongoDB instance is set up and accessible.
- **Kafka**: Kafka is configured for event-driven messaging. If scaling is needed, partitioning and replication
  configurations can be adjusted.
