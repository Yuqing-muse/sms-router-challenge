## Why

Sinch requires a take-home exercise demonstrating clean API design and routing logic: build an SMS routing service that selects the correct carrier by phone number prefix and enforces opt-out rules, all within a Spring Boot application.

## What Changes

- Introduce a REST API for sending SMS messages (`POST /messages`)
- Introduce a message status query endpoint (`GET /messages/{id}`)
- Introduce an opt-out management endpoint (`POST /optout/{phoneNumber}`)
- Implement carrier routing logic based on destination number prefix (AU → Telstra/Optus alternating, NZ → Spark, other → Global)
- Implement message lifecycle tracking: PENDING → SENT → DELIVERED / BLOCKED
- Block delivery to opted-out numbers

## Capabilities

### New Capabilities

- `message-send`: Accepts a message payload, validates the destination number, checks opt-out status, selects a carrier, persists the message in memory, and returns an ID + final status (DELIVERED or BLOCKED — delivery is synchronous within the same request).
- `message-status`: Retrieves the current status and metadata for a previously submitted message by ID.
- `opt-out-management`: Records a phone number as opted out, preventing future message delivery to that number.
- `carrier-routing`: Determines the appropriate carrier for a destination number based on country prefix rules (AU/NZ/Global) with round-robin alternation for AU carriers.

### Modified Capabilities

## Impact

- New Spring Boot project (Java, Gradle) with no external database dependency
- In-memory storage only (maps/lists); no persistence layer required
- Affects: REST controllers, service layer, routing engine, phone number validator, unit tests, README
