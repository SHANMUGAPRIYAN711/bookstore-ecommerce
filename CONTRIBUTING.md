# Contributing Guide

## 1. Purpose

This document defines the development workflow and coding guidelines for the Bookstore E-Commerce Backend.

The project is designed so that multiple developers can work independently on different business domains while maintaining a consistent architecture.

---

## 2. Project Architecture

The project follows a domain-oriented package structure.

```text
com.bookstore
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

Each business domain may contain:

```text
controller/
dto/
entity/
repository/
service/
```

Developers should work primarily inside their assigned domain.

---

## 3. Developer Responsibilities

Each developer will normally be assigned one or more business modules.

Example:

```text
Developer A -> Book
Developer B -> Cart
Developer C -> Order
Developer D -> Review
Developer E -> User
Developer F -> Inventory
```

A developer is responsible for implementing:

- Controller
- Service
- Repository
- Business logic
- Integration logic
- Unit tests
- Module-specific exception handling where required

Developers should avoid modifying unrelated modules.

---

## 4. Shared Packages

The following packages contain functionality that may be used by multiple developers:

```text
common/
config/
exception/
security/
audit/
logging.aspect/
```

Before modifying shared classes, coordinate with the team.

This is especially important for:

- Common DTOs
- Common entities
- Enums
- Constants
- Security configuration
- Global exception handling
- Shared configuration
- Audit functionality
- Logging functionality

Do not create duplicate versions of existing shared classes.

---

## 5. Entity and DTO Contracts

The existing entity and DTO structures form the common contract between developers.

### Entities

Entities represent persistent database objects.

Developers should not change shared entity fields, relationships, or mappings without discussing the change with the team.

### DTOs

DTOs are used for communication between the API and clients.

Existing DTOs should be reused wherever applicable.

If a new DTO is genuinely required, follow the existing naming and package conventions.

---

## 6. Layer Responsibilities

Follow the standard layered flow:

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
Repository
   |
   v
Database
```

### Controller

Controllers should:

- Receive HTTP requests
- Validate request input
- Call the appropriate service
- Return appropriate responses
- Avoid business logic

### Service

Services should contain:

- Business rules
- Business validation
- Transaction boundaries
- Coordination between repositories and integrations

### Repository

Repositories should handle:

- Database access
- Queries
- Persistence operations

Database logic should not be placed directly inside controllers.

---

## 7. Coding Standards

Use:

- Java 21
- Spring Boot conventions
- Constructor-based dependency injection
- Lombok where already established
- Meaningful class and method names
- Proper exception handling
- Appropriate logging

Avoid:

- Hardcoded credentials
- Duplicate utility classes
- Duplicate enums
- Duplicate DTOs
- Business logic inside controllers
- Direct database access from controllers
- Unnecessary changes to shared packages

---

## 8. Dependency Injection

Prefer constructor injection.

Example:

```java
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
}
```

Do not introduce field injection unless there is a specific project requirement.

---

## 9. Exception Handling

The project contains centralized exception handling.

Existing exceptions include:

```text
BadRequestException
ForbiddenException
ResourceNotFoundException
UnauthorizedException
ErrorResponse
GlobalExceptionHandler
```

Reuse existing exceptions when applicable.

Do not create duplicate global exception handlers.

If a module requires a domain-specific exception, coordinate with the team before adding it.

---

## 10. Security

Security functionality is centralized under:

```text
security/
```

Security-related functionality includes:

- Spring Security
- JWT
- OAuth2
- Authentication
- Authorization
- User details
- Security filters

Business developers should not create their own security configuration.

If a business endpoint requires authorization, coordinate with the security implementation and use the established security mechanism.

---

## 11. Common Enums

The project contains shared enums under:

```text
common/enums/
```

Examples include:

```text
AddressType
AuthProvider
BookStatus
OrderStatus
Role
UserStatus
```

Reuse these enums instead of creating duplicate enums inside individual modules.

---

## 12. Database Changes

PostgreSQL is the primary relational database.

Database migration files are maintained under:

```text
src/main/resources/db.migration/
```

Before modifying the database structure:

1. Discuss the required change with the team.
2. Check whether another developer is modifying the same table.
3. Add the appropriate migration.
4. Verify the application starts successfully.
5. Run the test suite.

Avoid manually changing shared database structures without coordination.

---

## 13. Git Branching Strategy

Do not develop directly on the main branch.

Create a branch for your assigned feature.

Example:

```bash
git checkout -b feature/book-module
```

Other examples:

```text
feature/cart-module
feature/order-module
feature/review-module
feature/user-module
feature/inventory-module
bugfix/cart-total
bugfix/order-validation
```

Use clear branch names that describe the work.

---

## 14. Development Workflow

Follow this workflow:

```text
Pull latest changes
       |
       v
Create feature branch
       |
       v
Implement assigned functionality
       |
       v
Run tests
       |
       v
Review changes
       |
       v
Commit changes
       |
       v
Push feature branch
       |
       v
Create Pull Request
```

---

## 15. Before Starting Development

Update your local branch before starting new work.

```bash
git checkout main
git pull origin main
git checkout -b feature/your-feature
```

If the team uses a different integration branch, follow the team's agreed branch strategy.

---

## 16. Before Committing

Run:

```bash
.\mvnw.cmd clean test
```

The build should finish successfully.

Expected result:

```text
BUILD SUCCESS
```

Also verify:

```text
Failures: 0
Errors: 0
```

Check the files being committed:

```bash
git status
```

Review the changes:

```bash
git diff
```

---

## 17. Commit Guidelines

Use meaningful commit messages.

Good examples:

```text
Implement book repository
Implement book service
Add book controller
Add book search functionality
Add cart service
Implement order creation
Add review validation
```

Avoid vague messages such as:

```text
changes
update
done
final
test
new code
```

---

## 18. Commit and Push

After verifying the changes:

```bash
git add .
```

Then:

```bash
git commit -m "Implement book module"
```

Push the feature branch:

```bash
git push -u origin feature/book-module
```

Do not directly push unfinished work to the main branch.

---

## 19. Pull Requests

After pushing your feature branch, create a Pull Request.

The Pull Request should contain:

- What was implemented
- What was changed
- Tests performed
- Any database changes
- Any configuration changes
- Any known limitations

Example:

```text
Implemented Book module.

Changes:
- Added BookRepository
- Added BookService
- Added BookController
- Added book search
- Added validation

Testing:
- mvnw clean test
- All tests passing
```

---

## 20. Avoid Unnecessary Changes

When working on a feature, avoid modifying files unrelated to your task.

For example, a developer implementing the Book module should normally not modify:

```text
cart/
order/
review/
wishlist/
security/
```

unless the feature genuinely requires a coordinated change.

This reduces merge conflicts and makes Pull Requests easier to review.

---

## 21. Shared Contract Changes

Changes to the following should be discussed with the team before implementation:

```text
Entity fields
Entity relationships
DTO fields
Common enums
Database schema
API request/response contracts
Security configuration
Common utilities
Global exception handling
```

A change to a shared contract can affect multiple developers.

---

## 22. Secrets and Credentials

Never commit real credentials.

Do not commit:

```text
Database passwords
JWT secrets
OAuth client secrets
AWS access keys
RabbitMQ passwords
Redis passwords
API keys
Private certificates
```

Use environment variables or an appropriate local configuration mechanism.

Before committing, inspect:

```bash
git status
```

and verify that no sensitive files are included.

---

## 23. Configuration

Application configuration is maintained primarily through:

```text
src/main/resources/application.yaml
```

Developers should avoid unnecessarily changing shared configuration.

If a feature requires a new property:

1. Discuss the property with the team.
2. Use a clear property name.
3. Avoid hardcoding secrets.
4. Document the required configuration.

---

## 24. Testing

Developers should add tests for the functionality they implement.

At minimum, test important:

- Service logic
- Validation
- Repository queries
- Controller behavior
- Error scenarios

Run the complete test suite before creating a Pull Request:

```bash
.\mvnw.cmd clean test
```

---

## 25. Module Independence

Each business module should remain as independent as reasonably possible.

Example:

```text
Book
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

The same pattern applies to:

```text
Cart
Order
Review
Wishlist
User
Address
Inventory
```

Avoid unnecessary dependencies between modules.

When communication between modules is required, use an appropriate service or integration mechanism.

---

## 26. Asynchronous Processing

RabbitMQ is used for asynchronous communication.

Typical flow:

```text
Business Service
       |
       v
Producer
       |
       v
RabbitMQ
       |
       v
Queue
       |
       v
Consumer
       |
       v
Notification / Processing
```

Developers implementing asynchronous functionality should follow the existing RabbitMQ configuration and messaging conventions.

---

## 27. Redis

Redis may be used for:

- Caching
- Fast-access data
- Temporary data
- Other appropriate high-performance use cases

Redis configuration is maintained centrally.

Business modules should avoid creating independent Redis configurations.

---

## 28. AOP and Cross-Cutting Concerns

Spring AOP is used for cross-cutting functionality such as:

- Logging
- Auditing
- Performance monitoring
- Other reusable concerns

Do not duplicate cross-cutting logic inside every service when an existing aspect already provides the functionality.

---

## 29. Code Review Checklist

Before creating a Pull Request, verify:

```text
[ ] Code compiles
[ ] Tests pass
[ ] No unnecessary files changed
[ ] No secrets committed
[ ] No duplicate DTOs created
[ ] No duplicate enums created
[ ] Existing entities were not changed unnecessarily
[ ] Existing common classes were reused
[ ] Controllers contain minimal logic
[ ] Business logic is inside services
[ ] Database access is inside repositories
[ ] Meaningful commit message used
[ ] Feature branch used
```

---

## 30. Current Project Foundation

The initial project foundation contains:

```text
Domain package structure
Entities
DTOs
Common package
Shared enums
Exception foundation
Configuration foundation
Security foundation
Infrastructure package structure
Maven configuration
Basic Spring Boot test
```

The existing entity and DTO contracts should be treated as the baseline shared structure for module development.

Developers can now focus primarily on implementing their assigned business logic.

---

## 31. Final Rule

Keep your changes focused on your assigned module.

Before changing shared contracts or infrastructure, communicate with the team.

The objective is:

```text
Independent Development
        +
Shared Contracts
        +
Code Review
        +
Automated Testing
        =
Stable Integration
```