Microservices Architecture Project
📋 Overview
A comprehensive microservices-based system built with Spring Boot 3.5.4 and Spring Cloud 2025.0.0.
The project demonstrates modern distributed system patterns including service discovery, 
API gateway, configuration management, and inter-service communication.


┌─────────────────┐    ┌──────────────────┐    ┌────────────────────┐                                              
│                   API Gateway   │    │  Config Server   │    │  Discovery Server  │                                              
│                  (Gateway)     │◄──►│   (Config)       │◄──►│   (Eureka)         │                                    
└─────────────────┘    └──────────────────┘    └────────────────────┘                                     
           ▲                       ▲                       ▲                                                          
           │                       │                       │                                                                                                                           
┌─────────┴─────────┐   ┌─────────┴─────────┐   ┌─────────┴─────────┐                                       
│               User Service    │   │ Notification Srv  │   │   Other Services  │                                           
│               (Business)      │   │   (Messaging)     │   │                   │                                              
└───────────────────┘   └───────────────────┘   └───────────────────┘                                        


🚀 Services
1. Discovery Service (Eureka Server)
   Port: 8761

Role: Service registry and discovery

Dashboard: http://localhost:8761

2. Config Service (Spring Cloud Config Server)
   Port: 8881

Role: Centralized configuration management

Profile: Native (file-based)

Config Files: Located in /config/ directory

3. Gateway Service (Spring Cloud Gateway)
   Port: 8080

Role: API gateway, routing, load balancing

Features:

Dynamic routing to services

Load balancing with Eureka

Reactive architecture

4. User Service (Business Domain)
   Port: 8082

Technologies: Spring Web, Data JPA, PostgreSQL, Kafka

Features: User management, HATEOAS, Validation, Security

5. Notification Service (Messaging)
   Port: 8081

Technologies: Spring Mail, Kafka, Web

Features: Email notifications, event processing

🛠️ Technology Stack
Java 21 with preview features

Spring Boot 3.5.4

Spring Cloud 2025.0.0

Spring Cloud Gateway - Reactive API gateway

Eureka Server - Service discovery

Spring Cloud Config - Configuration management

PostgreSQL - Relational database

Apache Kafka - Message broker

Docker - Containerization

Lombok - Code generation

MapStruct - DTO mapping

Testcontainers - Integration testing

📁 Project Structure
task4/
├── discovery-service/          # Eureka server
├── config-service/             # Config server
├── gateway-service/            # API Gateway
├── user-service/               # User management
├── notification-service/       # Notifications
└── pom.xml                     # Parent POM

⚙️ Configuration
Centralized Config Structure

config-service/src/main/resources/config/
├── application.yml              # Common configuration
├── gateway-service.yml          # Gateway specific config
├── user-service.yml            # User service config
└── notification-service.yml    # Notification service config

Key Configuration Features
Service Discovery: Automatic registration with Eureka

Config Server: Externalized configuration management

API Gateway: Dynamic routing with service discovery

Health Checks: Spring Boot Actuator endpoints

🚀 Getting Started
Prerequisites
Java 21

Maven 3.9+

Docker (optional)

PostgreSQL (for user service)

Kafka (for messaging)

Local Development

1.Start Infrastructure Services:
# Start Eureka Server
cd discovery-service && ./mvnw spring-boot:run

# Start Config Server
cd config-service && ./mvnw spring-boot:run

2.Start Business Services:
# Start Gateway
cd gateway-service && ./mvnw spring-boot:run

# Start User Service
cd user-service && ./mvnw spring-boot:run

# Start Notification Service
cd notification-service && ./mvnw spring-boot:run

Docker Deployment

# Build all services
./mvnw clean package -DskipTests

# Build and run with Docker Compose
docker-compose up --build

📊 Monitoring & Debugging
Health Endpoints
Eureka Dashboard: http://localhost:8761

Gateway Health: http://localhost:8080/actuator/health

Config Server: http://localhost:8881/actuator/health

Service URLs
API Gateway: http://localhost:8080

User Service API: http://localhost:8080/api/users/**

Notification API: http://localhost:8080/api/notifications/**

🔧 Development Guidelines
Code Style
Use Lombok for boilerplate code reduction

Implement proper DTO mapping with MapStruct

Follow reactive patterns in Gateway service

Use constructor injection for dependencies

Testing
Unit tests with JUnit 5 and Mockito

Integration tests with Testcontainers

Kafka testing with embedded brokers

Database testing with PostgreSQL containers

Best Practices
Configuration: Use Config Server for externalized config

Service Discovery: Leverage Eureka for dynamic service lookup

API Gateway: Implement rate limiting and circuit breakers

Messaging: Use Kafka for reliable async communication

Monitoring: Implement comprehensive health checks

🐛 Troubleshooting
Common Issues
Spring MVC Conflict with Gateway:

Ensure spring-boot-starter-web is excluded from gateway dependencies

Use spring.main.web-application-type=reactive

Service Registration Issues:

Verify Eureka server is running

Check service configuration in respective YAML files

Config Server Problems:

Verify config files are in correct location

Check profile settings

Debug Tips
Enable debug logging: logging.level.org.springframework.cloud=DEBUG

Check Eureka dashboard for service registration status

Verify config server endpoints: http://localhost:8881/{service}/{profile}

📈 Future Enhancements
Distributed tracing with Zipkin

Metrics collection with Prometheus

Advanced security with OAuth2

Kubernetes deployment manifests

CI/CD pipeline configuration

Performance monitoring dashboard

Circuit breaker implementation

API documentation aggregation

📝 License
This project is for educational purposes as part of the Aston Microservices Course.

👥 Team
Developed as part of Module 2 Homework - Microservices Architecture implementation.

Note: This is a demonstration project showcasing microservices patterns and Spring Cloud technologies.
