## Context

This is a greenfield Spring Boot application with no existing codebase. The only constraints are: Java language, Maven or Gradle build, in-memory storage (no DB), and the three REST endpoints defined in the challenge. The app will be evaluated for code quality, error handling, and testability.

## Goals / Non-Goals

**Goals:**
- Single deployable Spring Boot JAR with three REST endpoints
- Stateless carrier routing via phone prefix rules with AU round-robin alternation
- Opt-out enforcement at send time with appropriate status codes
- Message lifecycle state machine (PENDING → SENT → DELIVERED / BLOCKED)
- Unit test coverage for routing, opt-out, and send scenarios
- Clean README with setup instructions and API examples

**Non-Goals:**
- Persistent storage or database
- Real carrier integration or actual SMS delivery
- Authentication / authorization
- Rate limiting
- Multi-node / distributed state

## Decisions

### 1. Package structure: layered by feature or by layer? - layer first

**Decision**: Layer-first (`controller`, `service`, `model`, `repository`) — this is the most familiar Spring Boot convention and keeps the small codebase navigable without ceremony.

**Alternatives considered**: Feature packages (`message/`, `optout/`) — better for large codebases but adds indirection for a 3-endpoint service.

---

### 2. In-memory storage: `ConcurrentHashMap` in a `@Repository` bean vs a plain `@Service` field - repository

**Decision**: Dedicate a `MessageRepository` and `OptOutRepository` class (annotated `@Repository`) backed by `ConcurrentHashMap`. This separates storage from business logic and makes unit testing straightforward with constructor injection.

**Alternatives considered**: Store directly in service — simpler, but harder to test in isolation.

---

### 3. Carrier round-robin for AU - use simple router way

**Decision**: Use an `AtomicInteger` counter in the `CarrierRoutingService` bean (singleton scope) to alternate between Telstra and Optus. Counter increments on every AU message regardless of opt-out, ensuring fair alternation across non-blocked messages only.

**Alternatives considered**: Random selection — non-deterministic and harder to test. Per-number last-used tracking — unnecessary complexity.

---

### 4. Phone number validation - assume based on real world examples

**Decision**: Validate using regex patterns:
- AU: `\+61[2-9]\d{8}` (10 digits after country code, excluding leading zero)
- NZ: `\+64[2-9]\d{7,9}` (mobile and landline ranges)
- Others accepted as-is for routing to Global carrier

Return HTTP 400 for malformed numbers that don't match any accepted pattern. The challenge notes "make reasonable assumptions" so we accept any `+<digits>` for non-AU/NZ prefixes.

---

### 5. Message status transitions - assume it's sync in this mock system

**Decision**:
- On send: PENDING immediately, then synchronously "deliver" (set to SENT → DELIVERED) within the same request since there's no real async carrier. If opted-out, set to BLOCKED and never reach SENT.
- Status returned in `POST /messages` response reflects final synchronous state.

**Alternatives considered**: True async (PENDING response, background delivery) — adds complexity without value in a mock system.

---

### 6. Build tool

**Decision**: Gradle (via Spring Initializr) — more concise build scripts, faster incremental builds.

---

### 7. Error handling: custom exceptions + `@ControllerAdvice` vs returning errors directly - define custom exceptions

**Decision**: Service layer throws typed domain exceptions; a single `@ControllerAdvice` maps them to HTTP responses. Controllers stay thin and HTTP-unaware.

Exceptions used:
- `InvalidPhoneNumberException` → 400 (bad number format in POST /messages)
- `UnsupportedFormatException` → 400 (format)
- `MessageNotFoundException` → 404 (unknown ID)

Everything else is normal flow - no exception thrown for duplicate opt-out (idempotent 200) or unknown carrier prefix (silently routes to Global).

**Alternatives considered**: Return `ResponseEntity` with error bodies directly from controllers — simpler for trivial cases but leaks HTTP concerns into the service layer and complicates unit testing.