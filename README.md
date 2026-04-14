# SMS Router

A Spring Boot REST service that simply routes outbound SMS messages to carriers based on destination number prefix and enforces opt-out rules. 
Built with Java 17, Spring Boot 3.5, and Gradle, using in-memory storage with no external dependencies.

## Assumptions

- **Phone number validation**: AU (`+61[2-9]\d{8}`) and NZ (`+64[2-9]\d{7,9}`) are strictly validated; all other numbers are accepted if they match `+\d+`. Short codes and non-E.164 formats are rejected.
- **AU carrier round-robin**: a single in-memory counter alternates Telstra (even) / Optus (odd) per AU send. Blocked messages do not increment the counter; counter resets on restart.
- **No idempotency**: each `POST /messages` creates a new message regardless of duplicate content or destination. Opt-out is the only delivery guard.
- **Synchronous delivery (mock)**: in real world, delivery would be async — `SENT` on carrier acknowledgement, `DELIVERED` on handset confirmation. In this mock, I assume it's simulated synchronously (`PENDING -> SENT -> DELIVERED`).
- **In-memory storage**: all state is held in memory and does not persist across restarts.

## Features

- **Send Message** - validates, routes, and delivers an SMS to the appropriate carrier
- **Get Message Status** - retrieves the full message record including status and assigned carrier
- **Opt-out Management** - registers a number as opted out; subsequent sends are blocked
- **Carrier Routing** - routes AU numbers round-robin between Telstra and Optus, NZ to Spark, all others to Global
- **Phone Number Validation** - strict format validation for AU and NZ numbers; international fallback for others

## Project Structure

```
src/main/java/com/sinch/smsrouter/
├── controller/    REST endpoints (MessageController, OptOutController)
├── service/       Business logic (send, routing, validation, opt-out)
├── repository/    In-memory data stores backed by ConcurrentHashMap
├── model/         Domain entities: Message, MessageStatus, Carrier
├── dto/           Request/response shapes
└── exception/     Custom exceptions and global error handler
```

## Prerequisites

- Java 17+
- No other dependencies required (in-memory storage, no database)

## Build & Run

```bash
./gradlew bootRun
```

The service starts on `http://localhost:8080`. Verify with:

```bash
curl -s -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{"destination_number": "+61491570156", "content": "Hello", "format": "SMS"}'
```

Run tests:

```bash
./gradlew test
```

## API

### Send a message

```bash
curl -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{"destination_number": "+61491570156", "content": "Hello world", "format": "SMS"}'
```

Response `201 Created`:
```json
{
  "id": "a1b2c3d4-...",
  "status": "DELIVERED"
}
```

### Get message status

```bash
curl http://localhost:8080/messages/a1b2c3d4-...
```

Response `200 OK` with full message record:
```json
{
  "id": "a1b2c3d4-...",
  "destination_number": "+61491570156",
  "content": "Hello world",
  "format": "SMS",
  "status": "DELIVERED",
  "carrier": "Telstra"
}
```

### Opt out a number

```bash
curl -X POST http://localhost:8080/optout/+61491570156
```

Response `200 OK` (no body). Subsequent sends to this number will be stored with `status: "BLOCKED"`.

## Routing Rules

| Prefix | Carrier |
|--------|---------|
| `+61` (AU) | Telstra / Optus (alternating, round-robin) |
| `+64` (NZ) | Spark |
| Other | Global |

## Error Handling

All errors return a JSON body with an `error` field, e.g. `{"error": "Invalid phone number: invalid_number"}`.

| Status | Trigger |
|--------|---------|
| `400` | `POST /messages` — invalid phone number, unsupported format (non-`SMS`), or missing required field |
| `400` | `POST /optout/{phoneNumber}` — invalid phone number format |
| `404` | `GET /messages/{id}` — message ID does not exist |

## Edge Cases

- **Opted-out number**: message is stored with `BLOCKED` status, no carrier is assigned, and the AU round-robin counter is not incremented
- **Blocked messages are still retrievable**: a `GET /messages/{id}` on a blocked message returns the full record with `status: "BLOCKED"` and no `carrier` field
- **Duplicate opt-out**: opting out an already opted-out number is a no-op — no error is returned
- **Opt-out validation**: `POST /optout/{phoneNumber}` applies the same phone number validation as `POST /messages` — invalid numbers are rejected with `400`
