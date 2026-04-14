## 1. Project Bootstrap

- [x] 1.1 Generate Spring Boot project via start.spring.io (Java 17+, Gradle, Spring Web, Lombok optional)
- [x] 1.2 Confirm project builds and runs with `./gradlew bootRun`
- [x] 1.3 Add any extra dependencies (e.g., spring-boot-starter-validation) to build.gradle

## 2. Domain Model

- [x] 2.1 Create `Message` model with fields: `id` (UUID), `destinationNumber`, `content`, `format`, `status` (enum), `carrier` (nullable)
- [x] 2.2 Create `MessageStatus` enum: PENDING, SENT, DELIVERED, BLOCKED
- [x] 2.3 Create request/response DTOs: `SendMessageRequest`, `SendMessageResponse`, `MessageResponse`

## 3. Exception Classes

- [x] 3.1 Create `InvalidPhoneNumberException extends RuntimeException` (used for bad number format)
- [x] 3.2 Create `UnsupportedFormatException extends RuntimeException` (used for format ≠ "SMS")
- [x] 3.3 Create `MessageNotFoundException extends RuntimeException` (used for unknown message ID)
- [x] 3.4 Create `GlobalExceptionHandler` (`@ControllerAdvice`) mapping each exception to a structured HTTP error response (400/404)

## 4. In-Memory Repositories

- [x] 4.1 Create `MessageRepository` backed by `ConcurrentHashMap<UUID, Message>` with save/findById methods
- [x] 4.2 Create `OptOutRepository` backed by `ConcurrentHashSet` (or `ConcurrentHashMap` keyed by phone number) with add/contains methods

## 5. Carrier Routing

- [x] 5.1 Create `CarrierRoutingService` with an `AtomicInteger` counter for AU round-robin
- [x] 5.2 Implement `selectCarrier(String destinationNumber)` returning "Telstra"/"Optus"/"Spark"/"Global"
- [x] 5.3 Ensure AU counter is NOT incremented for blocked (opted-out) messages

## 6. Phone Number Validation

- [x] 6.1 Create `PhoneNumberValidator` utility/service with regex patterns for AU and NZ
- [x] 6.2 Throw `InvalidPhoneNumberException` for malformed numbers
- [x] 6.3 Accept any `+<digits>` format for non-AU/NZ numbers (route to Global)

## 7. Message Service

- [x] 7.1 Create `MessageService` with `sendMessage(SendMessageRequest)` method
- [x] 7.2 Validate phone number; throw `InvalidPhoneNumberException` if invalid
- [x] 7.3 Validate format; throw `UnsupportedFormatException` if format ≠ "SMS"
- [x] 7.4 Check opt-out status; if opted out, store message as BLOCKED and return
- [x] 7.5 Select carrier, set status PENDING → SENT → DELIVERED, persist and return response
- [x] 7.6 Create `getMessageStatus(UUID id)` method; throw `MessageNotFoundException` if not found

## 8. REST Controllers

- [x] 8.1 Create `MessageController` with `POST /messages` mapped to `MessageService.sendMessage`
- [x] 8.2 Add `GET /messages/{id}` mapped to `MessageService.getMessageStatus`
- [x] 8.3 Create `OptOutController` with `POST /optout/{phoneNumber}` that saves to `OptOutRepository`

## 9. Unit Tests

- [x] 9.1 Test `CarrierRoutingService`: AU alternation (Telstra→Optus→Telstra), NZ→Spark, other→Global
- [x] 9.2 Test `MessageService.sendMessage`: valid AU, valid NZ, opted-out (blocked), invalid number, unsupported format
- [x] 9.3 Test `MessageService.getMessageStatus`: found, not found (throws `MessageNotFoundException`)
- [x] 9.4 Test `PhoneNumberValidator`: valid AU, valid NZ, invalid format (throws `InvalidPhoneNumberException`)

## 10. README and Cleanup

- [x] 10.1 Write README with: prerequisites, build & run instructions, API examples (curl), assumptions made
- [x] 10.2 Document assumption: `POST /messages` is not idempotent — each call creates a new independent message with a new UUID, even if the destination number and content are identical. There is no deduplication or cooldown between sends to the same number. The only mechanism that prevents delivery is opt-out. Callers are responsible for avoiding unintended duplicate sends.
- [x] 10.2a Document assumption: Message delivery is synchronous (mock) — a message transitions PENDING → SENT → DELIVERED within the same request. In a real system, `POST /messages` would return PENDING immediately and delivery would happen asynchronously as the carrier accepts (SENT) and the handset confirms (DELIVERED). The state machine is intentionally modelled to support this — adding `@Async` on the delivery step and having callers poll `GET /messages/{id}` would be a natural next step.
- [x] 10.3 Add example curl commands for all three endpoints
- [x] 10.4 Review code for clarity, remove any dead code, ensure consistent naming
