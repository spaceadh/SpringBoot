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