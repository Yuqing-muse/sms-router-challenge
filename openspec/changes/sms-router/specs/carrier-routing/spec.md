## ADDED Requirements

### Requirement: Route AU numbers with alternating carriers
The system SHALL route messages to Australian numbers (`+61` prefix) alternately to "Telstra" and "Optus", starting with "Telstra" for the first AU message.

#### Scenario: First AU message routes to Telstra
- **WHEN** the first POST /messages is sent to an AU number
- **THEN** the assigned carrier is "Telstra"

#### Scenario: Second AU message routes to Optus
- **WHEN** a second POST /messages is sent to an AU number (after the first AU message)
- **THEN** the assigned carrier is "Optus"

#### Scenario: Third AU message routes to Telstra
- **WHEN** a third POST /messages is sent to an AU number
- **THEN** the assigned carrier is "Telstra" (round-robin restarts)

### Requirement: Route NZ numbers to Spark
The system SHALL route all messages to New Zealand numbers (`+64` prefix) to the carrier "Spark".

#### Scenario: NZ number routes to Spark
- **WHEN** a POST /messages is made with `destination_number` starting with `+64`
- **THEN** the assigned carrier is "Spark"

### Requirement: Route all other numbers to Global
The system SHALL route messages with any prefix other than `+61` or `+64` to the carrier "Global".

#### Scenario: US number routes to Global
- **WHEN** a POST /messages is made with `destination_number` starting with `+1`
- **THEN** the assigned carrier is "Global"

#### Scenario: Unknown prefix routes to Global
- **WHEN** a POST /messages is made with a `destination_number` that has no country-specific rule
- **THEN** the assigned carrier is "Global"

### Requirement: Opted-out messages skip carrier routing
The system SHALL NOT assign a carrier to messages that are blocked due to opt-out.

#### Scenario: Blocked message has no carrier
- **WHEN** a POST /messages is made to an opted-out number
- **THEN** no carrier is assigned and the AU round-robin counter is NOT incremented
