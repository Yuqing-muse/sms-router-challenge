## ADDED Requirements

### Requirement: Send SMS message
The system SHALL expose `POST /messages` accepting a JSON body with `destination_number`, `content`, and `format`. On success it SHALL return HTTP 201 with the generated message `id` and current (the latest) `status`.m
- 
#### Scenario: Valid AU number is accepted - expction class?
- **WHEN** a POST /messages request is made with `destination_number: "+61491570156"`, `content: "Hello"`, `format: "SMS"`
- **THEN** the system responds HTTP 201 with a UUID `id` and `status: "DELIVERED"`

#### Scenario: Invalid phone number is rejected
- **WHEN** a POST /messages request is made with `destination_number: "not-a-number"`
- **THEN** the system responds HTTP 400 with an error message indicating invalid phone number

#### Scenario: Missing required field is rejected
- **WHEN** a POST /messages request is made without the `content` field
- **THEN** the system responds HTTP 400

#### Scenario: Opted-out number is blocked
- **WHEN** the destination number has been previously opted out AND a POST /messages is made to that number
- **THEN** the system responds HTTP 201 with `status: "BLOCKED"` and no carrier is assigned

### Requirement: Message payload validation
The system SHALL validate that `format` is `"SMS"`. The system SHALL validate that `destination_number` is a non-empty string beginning with `+`.

#### Scenario: Unsupported format is rejected
- **WHEN** a POST /messages request is made with `format: "MMS"`
- **THEN** the system responds HTTP 400 with an error indicating unsupported format
