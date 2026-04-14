## ADDED Requirements

### Requirement: Retrieve message status
The system SHALL expose `GET /messages/{id}` returning the full message record including `id`, `destination_number`, `content`, `format`, `status`, and `carrier` (null if blocked).

#### Scenario: Existing message is returned
- **WHEN** a GET /messages/{id} request is made with a valid previously-sent message ID
- **THEN** the system responds HTTP 200 with the message fields including current `status`

#### Scenario: Unknown message ID returns 404
- **WHEN** a GET /messages/{id} request is made with an ID that does not exist
- **THEN** the system responds HTTP 404 with an error message

#### Scenario: Blocked message shows BLOCKED status
- **WHEN** a message was sent to an opted-out number AND a GET /messages/{id} is made for that message
- **THEN** the response includes `status: "BLOCKED"` and `carrier` is null or absent
