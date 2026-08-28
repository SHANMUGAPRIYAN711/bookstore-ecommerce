# Bookstore E-Commerce Backend

A production-oriented Bookstore E-Commerce backend application built using
Java 21 and Spring Boot.

The project is designed with a modular architecture so that multiple
developers can work independently on different business domains.

---

## Technology Stack

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Spring Security
- JWT
- OAuth2
- Redis
- RabbitMQ
- JMS
- Spring AOP
- Spring Batch
- AWS S3
- HikariCP
- Maven
- JUnit 5
- Mockito

---

## Project Architecture

The application follows a domain-oriented package structure.

```text
com.bookstore
│
├── address/
├── audit/
├── book/
├── cart/
├── common/
├── config/
├── exception/
├── inventory/
├── logging.aspect/
├── notification/
├── order/
├── review/
├── security/
├── storage/
├── user/
└── wishlist/
```

Each business domain contains its own components such as:

```text
controller/
dto/
entity/
repository/
service/
```

This structure allows different developers to work on separate modules
without unnecessarily modifying other modules.

---

## Common Package

The `common` package contains objects shared across multiple modules.

```text
common/
├── constants/
├── dto/
├── entity/
├── enums/
└── util/
```

### Common Enums

- `AddressType`
- `AuthProvider`
- `BookStatus`
- `OrderStatus`
- `Role`
- `UserStatus`

These shared definitions should be reused instead of creating duplicate
versions inside individual modules.

---

# Business Modules

## Address

Responsible for managing user delivery and address information.

```text
address/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

---

## User

Responsible for user registration, authentication-related user information,
profile management and user operations.

```text
user/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

---

## Book

Responsible for bookstore catalog functionality.

```text
book/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

---

## Cart

Responsible for shopping-cart functionality.

```text
cart/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

---

## Inventory

Responsible for inventory and stock-related operations.

```text
inventory/
├── controller/
├── dto/
└── service/
```

---

## Order

Responsible for order creation and order lifecycle management.

```text
order/
├── controller/
├── dto/
├── entity/
├── enums/
├── repository/
└── service/
```

---

## Review

Responsible for book reviews and ratings.

```text
review/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

---

## Wishlist

Responsible for managing books added to a user's wishlist.

```text
wishlist/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

---

## Notification

Responsible for asynchronous notification processing.

```text
notification/
├── consumer/
├── dto/
└── service/
```

RabbitMQ is used for asynchronous messaging between application components.

---

## Storage

Responsible for file-upload and object-storage functionality.

```text
storage/
├── controller/
├── dto/
└── service/
```

AWS S3 is used as the object-storage integration.

---

# Security

Security-related functionality is isolated inside the `security` package.

```text
security/
├── CustomUserDetailsService
├── JwtAuthenticationFilter
├── JwtService
├── OAuth2AuthenticationFailureHandler
├── OAuth2AuthenticationSuccessHandler
└── SecurityConfig
```

The security layer is responsible for:

- Authentication
- Authorization
- JWT processing
- OAuth2 authentication
- User details loading
- Security filter configuration

Business modules should not duplicate security configuration.

---

# Configuration

Application-wide infrastructure configuration is maintained under:

```text
config/
├── OpenApiConfig
├── RabbitMqConfig
├── RedisConfig
└── S3Config
```

These configurations provide integrations used by multiple modules.

---

# Exception Handling

Global exception handling is centralized in:

```text
exception/
├── BadRequestException
├── ErrorResponse
├── ForbiddenException
├── GlobalExceptionHandler
├── ResourceNotFoundException
└── UnauthorizedException
```

Developers should reuse the existing exception classes where applicable
instead of creating duplicate global exceptions.

---

# Audit

The audit package provides auditing functionality.

```text
audit/
├── annotation/
└── aspect/
```

Audit-related cross-cutting functionality is implemented using Spring AOP.

---

# Logging

Application logging is handled through the logging aspect.

```text
logging.aspect/
└── LoggingAspect
```

The logging aspect provides centralized logging without requiring
duplicate logging logic in every business method.

---

# Database

PostgreSQL is used as the primary relational database.

Database migration files are maintained under:

```text
src/main/resources/db.migration/
```

JPA and Hibernate are used for object-relational mapping.

---

# Redis

Redis is used for caching and other fast-access data requirements.

Configuration:

```text
config/RedisConfig
```

Redis-specific implementation should remain isolated from business logic
where possible.

---

# RabbitMQ

RabbitMQ is used for asynchronous communication.

Typical flow:

```text
Business Service
       |
       v
RabbitMQ Producer
       |
       v
     Queue
       |
       v
RabbitMQ Consumer
       |
       v
Notification / Other Processing
```

For example, an order service can publish an event and a notification
consumer can process that event asynchronously.

---

# AOP

Spring AOP is used for cross-cutting concerns such as:

- Logging
- Auditing
- Performance monitoring
- Other reusable cross-cutting functionality

Business services should focus primarily on business logic.

---

# Spring Batch

Spring Batch is intended for batch-processing requirements such as
large-scale data processing and scheduled jobs.

---

# DTO and Entity Convention

Entities represent persistent database objects.

DTOs represent data exchanged through APIs.

Typical application flow:

```text
Client
  |
  v
Controller
  |
  v
Request DTO
  |
  v
Service
  |
  v
Entity
  |
  v
Repository
  |
  v
Database
```

Responses should normally be returned through response DTOs rather than
exposing JPA entities directly.

---

# Development Responsibilities

The project is structured so that developers can work independently on
business modules.

Examples:

```text
Developer A -> Book
Developer B -> Cart
Developer C -> Order
Developer D -> Review
Developer E -> User
Developer F -> Inventory
```

Developers should primarily modify the module assigned to them.

Before modifying shared classes under `common`, `config`, `security`,
`exception`, or other cross-cutting packages, coordinate with the team.

---

# Coding Guidelines

- Use Java 21.
- Follow Spring Boot conventions.
- Use constructor-based dependency injection.
- Use Lombok where already established by the project.
- Do not hardcode passwords, API keys or secrets.
- Do not commit `.env` files containing real credentials.
- Reuse existing common enums and DTOs where applicable.
- Avoid duplicate utility classes.
- Keep controllers thin.
- Keep business logic inside services.
- Keep database access inside repositories.
- Do not expose JPA entities directly from APIs unless explicitly required.
- Use meaningful class and method names.
- Maintain the existing package structure.

---

# Running the Application

## Build

### Windows

```powershell
.\mvnw.cmd clean test
```

### Linux/macOS

```bash
./mvnw clean test
```

---

## Run

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

---

# Project Verification

Before pushing changes to Git, run:

```powershell
.\mvnw.cmd clean test
```

The build should finish with:

```text
BUILD SUCCESS
```

and:

```text
Tests run: ...
Failures: 0
Errors: 0
```

---

# Git Workflow

Developers should create a separate branch for their assigned work.

Example:

```bash
git checkout -b feature/book-module
```

After completing the work:

```bash
git status
git add .
git commit -m "Implement book module"
git push -u origin feature/book-module
```

Do not directly push unfinished work to the main branch.

---

# Important

The project contains the common domain contracts, entities, DTOs and shared
structures required for the team to start implementing business logic.

Developers should build their assigned:

```text
Controller
Service
Repository
Integration
Tests
```

around the existing domain model and shared contracts.

Changes to shared entities, DTOs, enums or common classes should be
discussed with the team before modification because those classes may be
used by multiple modules.

---

# Project Status

Initial project structure completed.

Completed foundation includes:

- Domain package structure
- Entities
- DTOs
- Common package
- Shared enums
- Exception foundation
- Configuration foundation
- Security foundation
- Infrastructure package structure
- Maven build configuration
- Basic Spring Boot test

The project currently passes the Maven test/build verification.