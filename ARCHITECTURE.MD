```
src/main/java/com/poeticjustice/deeppoemsinc/
│
├── domain/                        # Pure business logic and entities
│   ├── model/
│   │   ├── mongo/
│   │   └── mysql/
│   ├── repository/                # Repository interfaces only (ports)
│   ├── service/                   # Domain services (business rules)
│   └── events/                   # Domain events (event classes)
│
├── application/                   # Application layer (use cases)
│   ├── dto/                      # DTOs for input/output
│   ├── service/                  # Application services implementing domain use cases
│   └── events/                   # Application event handlers, producers, consumers
│
├── infrastructure/               # External and framework-specific implementations
│   ├── config/                   # Spring configs (RabbitMQ, Redis, Minio, filters)
│   ├── repository/               # Repository implementations (DB, Mongo, Redis)
│   ├── messaging/                # RabbitMQ consumers and producers
│   ├── utils/                    # Framework-dependent utilities (email, pdf, jwt utils)
│   └── scheduler/                # Scheduled jobs
│
├── presentation/                 # REST API, Web, controllers
│   └── controller/               # Spring MVC controllers
│
├── common/                      # Cross-cutting concerns (exceptions, helpers, validators)
│
└── UploadBoxApplication.java     # Main SpringBoot app entrypoint
```
```
| Layer              | Key Contents                                  | Examples                                                           |
| ------------------ | --------------------------------------------- | ------------------------------------------------------------------ |
| **Domain**         | Entities, interfaces, domain services, events | `User`, `UserRepository` interface, `NewIPDetectedEvent`           |
| **Application**    | Use cases, DTOs, event handlers, producers    | `UploadFileUseCase`, `UploadFileRequestDto`, `UploadEventConsumer` |
| **Infrastructure** | DB repos, messaging, storage, configs         | `FileMetaRepositoryImpl`, `RabbitMQConfig`, `MinioClient` setup    |
| **Presentation**   | Controllers, validation, mappers              | `FileUploadController`, `GlobalExceptionHandler`                   |
| **Common**         | Exceptions, helpers, validators, constants    | `InvalidEmailException`, `DateUtils`, `ValidationUtils`            |
```
```
 Domain Layer
Purpose: Core business logic, pure Java (framework-agnostic).

Things to add:

Entities / Models: Core domain objects (User, FileMeta, UserQuota).

Repository Interfaces (Ports): Abstractions for persistence (e.g., UserRepository, FileMetaRepository interfaces).

Domain Services: Business logic that doesn’t fit into a single entity (e.g., quota calculation, IP validation).

Domain Events: Event classes that represent important domain occurrences (e.g., NewIPDetectedEvent).

Value Objects: Immutable objects representing domain concepts if applicable.

Exceptions (domain specific): Exceptions related strictly to domain rules.

2. Application Layer
Purpose: Application business rules, orchestrate domain, define use cases.

Things to add:

Use Case Services: Application services coordinating domain services, repositories, and external APIs (e.g., FileUploadUseCaseService).

DTOs: Input/output data transfer objects to decouple internal models from API/external contracts (e.g., UploadFileRequestDto).

Event Handlers: Classes that listen for domain events and trigger application logic (e.g., UploadEventConsumer).

Event Producers: Publish events after use cases complete (e.g., UploadEventProducer).

Application Exceptions: Exceptions related to use cases and application flow.

Interfaces for external services: Abstractions for messaging, storage, notification services used by the application.

3. Infrastructure Layer
Purpose: Technical implementation details, frameworks, external systems.

Things to add:

Repository Implementations: Concrete implementations of domain repository interfaces (e.g., UserRepositoryImpl with JPA, FileMetaRepositoryImpl with MySQL).

Messaging: RabbitMQ consumers, producers, configurations (RabbitMQConfig), message serialization.

Storage: MinIO client configs and implementation for file storage.

Database Configurations: Datasource, transaction management.

Cache: Redis clients (Jedis/Lettuce) configurations and utils.

Scheduler Implementations: Scheduled jobs (e.g., quota monitoring).

Framework-specific utils: Email service, PDF generation, JWT utilities.

Security: Authentication filters, CORS configuration, rate limiting.

Properties / Config files: Spring properties, externalized config loading.

4. Presentation Layer
Purpose: API/Web layer, handling HTTP requests and responses.

Things to add:

Controllers: REST controllers for APIs (e.g., FileUploadController, HealthController).

Request Mappers: Map API requests to application DTOs.

Response Mappers: Map application DTOs to API responses.

Exception Handlers: Controller advice for translating exceptions to HTTP responses.

Validation: API-level input validation using annotations or validators.

5. Common Layer (Cross-cutting concerns)
Purpose: Code used across layers.

Things to add:

Custom Exceptions: Base exceptions, validation exceptions.

Helpers / Utilities: General-purpose helpers (date formatting, string utils).

Validators: Reusable validation logic (e.g., email format, file size).

Constants: Common constants, error codes.

Logging: Centralized logging utilities or aspects.

Security Utils: Token extraction, role checks used across layers.
```