# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Spring Boot microservices architecture demonstrating e-commerce order processing with event-driven communication via Kafka. The system implements the Saga pattern for distributed transactions across Product, Order, Payment, and User services.

**Tech Stack:**
- Kotlin 1.9.25 + Spring Boot 3.5.6 (Java 21)
- Gradle multi-module build with Kotlin DSL
- MySQL 8.0 (separate databases per service)
- Apache Kafka for async messaging
- QueryDSL for type-safe queries
- Kotest + MockK for testing
- TestContainers for integration tests

## Build and Development Commands

### Infrastructure Setup

Start required infrastructure (run before starting any service):

```bash
# Start Kafka + Zookeeper + Kafka UI
cd docker/kafka && docker-compose up -d

# Start MySQL instances (ports 8010, 8011, 8012)
cd docker/mysql && docker-compose up -d

# Kafka UI available at http://localhost:8030
```

### Building

```bash
# Build all modules
./gradlew build

# Build specific service
./gradlew :product-service:build
./gradlew :order-service:build
./gradlew :shared:build

# Clean and rebuild
./gradlew clean build

# Build without tests
./gradlew build -x test

# Generate QueryDSL Q-classes (run after entity changes)
./gradlew kaptKotlin
```

### Running Services

```bash
# Run Product Service (port 8081)
./gradlew :product-service:bootRun

# Run Order Service (port 8082)
./gradlew :order-service:bootRun

# Run in background
./gradlew :product-service:bootRun &
```

**Service Ports:**
- Product Service: 8081
- Order Service: 8082
- Kafka UI: 8030
- Product DB: 8010
- Order DB: 8011
- User DB: 8012

### Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific service
./gradlew :product-service:test
./gradlew :order-service:test

# Run specific test class
./gradlew test --tests "*ProductServiceTest"
./gradlew test --tests "*ProductRestControllerTest"

# Run with TestContainers (automatically uses MySQL container)
./gradlew :product-service:test --info

# Run tests continuously (useful during development)
./gradlew test --continuous
```

### Code Quality

```bash
# Compile Kotlin code
./gradlew compileKotlin

# Check for compilation errors without running tests
./gradlew check -x test

# View dependencies
./gradlew dependencies

# View project structure
./gradlew projects
```

## Architecture

### Module Structure

```
commerce-msa-demo/
├── shared/                    # Shared library for all services
│   └── com.raynor.demo.shared
│       ├── typed/            # Value objects (ProductId, Money, etc.)
│       ├── enums/            # Shared enums (OrderStatus)
│       └── events/           # Domain events (OrderEvent)
├── services/
│   ├── product-service/      # Port 8081, DB 8010
│   └── order-service/        # Port 8082, DB 8011
└── docker/                   # Infrastructure
    ├── kafka/
    └── mysql/
```

### Service Architecture Pattern

All services follow Clean Architecture with DDD:

```
REST Controller (API Layer)
    ↓ [Commands/DTOs]
Service (Business Logic)
    ↓ [Domain Models with Value Objects]
Repository (Data Access)
    ↓ [JPA Entities]
Database
```

**Key Layers:**
- **API:** `*RestController` - REST endpoints, validation, error handling
- **Service:** `*Service` - Business logic, orchestration
- **Repository:** `*Repository` (Spring Data) + `*QueryDslRepository` (custom queries)
- **Domain:** Value objects (`ProductId`, `Money`), Commands, Events
- **Entity:** `*Entity` - JPA entities with auditing

### Shared Module Usage

The `shared` module provides type-safe value objects used across all services:

```kotlin
// Type-safe inline value classes with validation
ProductId(123L)              // Validated product ID
Money(BigDecimal("100.00"))  // Money with arithmetic operators
ProductStockQuantity(50)     // Stock quantity

// Extension functions for conversion
val id = 123L.toProductId()
val price = BigDecimal("99.99").toMoney()
val stock = 10.toProductStockQuantity()
```

All services depend on `shared` module via `implementation(project(":shared"))`.

### Database Configuration

Each service has its own MySQL database with identical credentials:

```yaml
# Pattern for all services
datasource:
  jdbc-url: jdbc:mysql://localhost:{PORT}/msa_{service}
  username: general_user
  password: passw0rd
```

**Important JPA Settings:**
- `ddl-auto: create` - Schema recreated on startup (dev mode)
- `open-in-view: false` - No lazy loading outside transactions
- `default_batch_fetch_size: 100` - Prevents N+1 queries

**Hikari Optimizations:**
- Prepared statement caching enabled
- Batch statement rewriting enabled
- Connection pool sized for 10 connections

### Kafka Configuration

**Producer Settings (all services):**
- `acks: all` - Wait for all replicas
- `enable.idempotence: true` - Exactly-once semantics
- `retries: 3` with exponential backoff
- `compression-type: gzip`

**Consumer Settings:**
- `ErrorHandlingDeserializer` wraps all deserializers for fault tolerance
- Retry with exponential backoff (1s → 2s → 4s)
- Dead Letter Topic (DLT) for failed messages after 3 attempts
- Manual offset commit after successful processing

**Retry Configuration Pattern:**
```kotlin
// In KafkaConfig
- Max attempts: 3
- Initial interval: 1000ms
- Multiplier: 2.0
- Max interval: 10000ms
- DLT handler method for permanently failed messages
```

### Event-Driven Patterns

**Saga Choreography:** Services coordinate via events without central orchestrator.

Example flow (Order → Product → Payment):
```
OrderService: OrderCreated event
    ↓ Kafka
ProductService: Consumes event, reserves inventory
    → InventoryReserved event OR InventoryReservationFailed
    ↓ Kafka
PaymentService: Processes payment
    → PaymentCompleted OR PaymentFailed
    ↓ Kafka (if failed, triggers compensation)
ProductService: Releases inventory (compensation)
OrderService: Cancels order (compensation)
```

**Topic Naming:** `{DOMAIN}_{ACTION}_{VERSION}` (e.g., `PRODUCT_REDUCE_STOCK_QUANTITY_V1`)

### QueryDSL Usage

QueryDSL provides type-safe query construction. Q-classes are generated via `kapt`:

```kotlin
// Custom queries in *QueryDslRepository
fun findPageByQuery(lastId: Long?, limit: Int): List<ProductEntity> {
    val query = jpaQueryFactory.selectFrom(product)
    if (lastId != null) {
        query.where(product.id.gt(lastId))
    }
    return query.orderBy(product.id.asc())
        .limit(limit.toLong())
        .fetch()
}

// Pessimistic locking for concurrency
fun findByIdWithLock(id: ProductId): ProductEntity? {
    return jpaQueryFactory.selectFrom(product)
        .where(product.id.eq(id.value))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne()
}
```

After modifying entities, regenerate Q-classes: `./gradlew kaptKotlin`

### Testing Patterns

**Kotest FunSpec Style:**
```kotlin
class ProductServiceTest : FunSpec({
    lateinit var service: ProductService
    lateinit var repository: ProductRepository

    beforeEach {
        repository = mockk()
        service = ProductService(repository)
    }

    test("should create product with valid input") {
        // Given
        val command = CreateProductCommand(...)
        every { repository.save(any()) } returns productEntity

        // When
        val result = service.createProduct(command)

        // Then
        result.id shouldBe productId
        verify(exactly = 1) { repository.save(any()) }
    }
})
```

**Integration Tests with TestContainers:**
```kotlin
@SpringBootTest
@Testcontainers
class ProductRepositoryIntegrationTest : FunSpec() {
    companion object {
        @Container
        val mysqlContainer = MySQLContainer("mysql:8.0").apply {
            withDatabaseName("test")
            withReuse(true)  // Reuse container across tests
        }
    }
}
```

TestContainers automatically starts MySQL for integration tests - no manual setup needed.

### Error Handling

**Global Exception Handler:**
```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse>

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse>
}
```

**Validation Pattern:**
- Use Jakarta validation annotations on Commands/DTOs
- Field-level validation with custom messages
- Return structured error responses with field names

**Kafka Error Handling:**
- Malformed messages handled by `ErrorHandlingDeserializer`
- Transient errors retried automatically
- Permanent failures sent to DLT for manual investigation

## Development Patterns

### Creating a New Entity

1. Define JPA entity in `rds/entity/*Entity.kt` with auditing:
   ```kotlin
   @Entity
   @Table(name = "product")
   @EntityListeners(AuditingEntityListener::class)
   class ProductEntity(
       @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
       val id: Long? = null,

       @CreatedDate
       var createdAt: LocalDateTime? = null,

       @LastModifiedDate
       var updatedAt: LocalDateTime? = null
   )
   ```

2. Regenerate QueryDSL Q-classes: `./gradlew kaptKotlin`

3. Create repository interfaces:
   ```kotlin
   interface ProductRdsRepository : JpaRepository<ProductEntity, Long>

   interface ProductQueryDslRepository {
       fun findByIdWithLock(id: ProductId): ProductEntity?
   }
   ```

### Adding a Kafka Consumer

1. Define event in `shared/events/`:
   ```kotlin
   sealed interface OrderEvent {
       data class OrderCreated(val orderId: Long, val productId: Long) : OrderEvent
   }
   ```

2. Create consumer with retry/DLT:
   ```kotlin
   @Component
   class ProductConsumer(private val service: ProductService) {
       @KafkaListener(
           topics = ["PRODUCT_REDUCE_STOCK_QUANTITY_V1"],
           containerFactory = "kafkaListenerContainerFactory"
       )
       fun consumeStockReduction(message: String) {
           // Process message
       }

       @DltHandler
       fun handleDlt(message: String) {
           // Log to DLT for manual intervention
       }
   }
   ```

3. Configure in `KafkaConfig` with retry policy

### Creating Value Objects

Add to `shared/typed/` for cross-service use:

```kotlin
@JvmInline
value class ProductId(val value: Long) {
    init {
        require(value > 0) { "ProductId must be positive" }
    }
}

fun Long.toProductId() = ProductId(this)
```

Value objects provide compile-time type safety: `ProductId` cannot be accidentally used as `OrderId`.

### Adding a New Service

1. Update `settings.gradle.kts`:
   ```kotlin
   include("services:payment-service")
   ```

2. Create `services/payment-service/build.gradle.kts` with shared dependency

3. Add `application.yaml` with unique port and database

4. Add MySQL config to `docker/mysql/docker-compose.yaml`

5. Follow the standard layering: Controller → Service → Repository → Entity

## Important Notes

- **Schema Management:** Currently using `ddl-auto: create` (dev mode). For production, switch to Flyway/Liquibase migrations.
- **Transaction Boundaries:** Service methods are `@Transactional`. Keep transactions short to avoid holding locks.
- **Pessimistic Locking:** Used in `findByIdWithLock()` for stock management to prevent overselling. This locks the row until transaction completes.
- **Kafka Idempotence:** Producer idempotence prevents duplicate messages. Consumers should implement idempotent message processing (deduplication).
- **Value Object Validation:** Validation happens at construction time in value objects, ensuring invalid domain objects cannot exist.
- **QueryDSL Generation:** Run `./gradlew kaptKotlin` after entity changes, before running tests or application.

## Event Flow Scenarios

Refer to README.md for detailed Saga scenarios:
1. Successful order completion (happy path)
2. Inventory reservation failure (immediate compensation)
3. Payment failure (multi-step compensation with inventory release)
