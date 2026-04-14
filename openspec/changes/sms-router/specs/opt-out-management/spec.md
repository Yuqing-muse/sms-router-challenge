## ADDED Requirements

### Requirement: Register opt-out
The system SHALL expose `POST /optout/{phoneNumber}` that marks the given phone number as opted out. Subsequent message sends to that number SHALL be blocked.

#### Scenario: New opt-out is registered
- **WHEN** a POST /optout/+61491570156 request is made
- **THEN** the system responds HTTP 200 and the number is recorded as opted out

#### Scenario: Duplicate opt-out is idempotent
- **WHEN** a POST /optout/{phoneNumber} request is made for a number already opted out
- **THEN** the system responds HTTP 200 without error

#### Scenario: Opted-out number blocks future sends
- **WHEN** a number has been opted out AND a POST /messages is made to that number
- **THEN** the message is stored with `status: "BLOCKED"` and no carrier routing occurs
