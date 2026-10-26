# question-bank Specification

## Purpose

Defines the reusable question library: how questions are composed from content blocks, which answer modes exist, how hints, briefings, and explanations behave, and how ownership, folder sharing, ordering, import, export, and image storage apply to them.

## Requirements

### Requirement: Question visibility
The system SHALL show a user their own questions plus questions placed in folders where the user holds an accepted membership. Questions outside that set SHALL behave as if they do not exist.

#### Scenario: Reading another user's private question
- **WHEN** a user requests a question they neither created nor can reach through an accepted folder membership
- **THEN** the system responds 404

#### Scenario: Reading a shared folder question
- **WHEN** a user with an accepted membership requests a question in that folder
- **THEN** the system returns the question

### Requirement: Content blocks
The system SHALL store question text, hints, and explanations as ordered lists of content blocks, where each block is either `text` or `image`. An image block SHALL reference a stored image by its identifier.

#### Scenario: Mixed content
- **WHEN** a question contains text blocks and image blocks
- **THEN** the question is stored and returned with the blocks in their original order

#### Scenario: Image block rendering
- **WHEN** the bot delivers a question containing an image block
- **THEN** the image is sent before the question's text blocks

### Requirement: Question kinds and precedence
The system SHALL interpret each question using this precedence: text-input question, then briefing question, then scored question. A question marked as a text-input question or a briefing SHALL NOT count toward the scored total.

#### Scenario: Text input flagged with briefing
- **WHEN** a question is marked as both text-input and briefing
- **THEN** it is handled as a text-input question and is not scored

#### Scenario: Scored total
- **WHEN** a quiz mixes scored, briefing, and text-input questions
- **THEN** its scored total counts only the scored questions

### Requirement: Scored multiple-choice questions
A scored question SHALL present its options as answer choices and SHALL compare the selected option to the stored answer ignoring case and surrounding whitespace. A question without a stored answer SHALL never be judged correct.

#### Scenario: Correct selection
- **WHEN** the player selects the option equal to the stored answer ignoring case and surrounding whitespace
- **THEN** the answer is recorded as correct and the player's score increases by one

#### Scenario: Wrong selection
- **WHEN** the player selects a different option
- **THEN** the answer is recorded as wrong and the response names the correct answer

### Requirement: Photo questions
A question marked to expect a photo SHALL accept an image message as its answer instead of answer options. A received photo SHALL be recorded as a correct answer, and sending another photo for the same question SHALL replace the stored photo without awarding another point.

#### Scenario: Submitting a photo answer
- **WHEN** the player sends a photo for a question that expects a photo
- **THEN** the answer is stored with the Telegram file reference and the score increases by one

#### Scenario: Replacing a photo answer
- **WHEN** the player sends another photo for the same question
- **THEN** the stored photo and caption are updated and the score is unchanged

### Requirement: Briefing configuration
A briefing question SHALL optionally show a primary and a secondary button with configurable text and visibility. Button text SHALL default to `READY` and `Start Timer`, and both buttons SHALL be visible by default.

#### Scenario: Default briefing
- **WHEN** a briefing is created without button settings
- **THEN** both buttons appear with the default texts

#### Scenario: Hidden button
- **WHEN** a briefing is configured to hide its secondary button
- **THEN** the delivered briefing shows only the primary button

### Requirement: After-answer configuration
A scored question SHALL optionally show a next-question button after the answer with configurable text and visibility, defaulting to `Next Question` and visible. When the button is shown and an explanation exists, the quiz SHALL wait for the player to click it.

#### Scenario: Default next button
- **WHEN** a scored question is created without after-answer settings
- **THEN** the next-question button appears after the answer

#### Scenario: Button hidden
- **WHEN** the after-answer button is hidden
- **THEN** the quiz advances automatically after showing the result

### Requirement: Hints and explanations
The system SHALL allow optional hint blocks and explanation blocks on any question. Hints SHALL be requested by the player, and explanations SHALL be shown after the answer is evaluated.

#### Scenario: Requesting a hint
- **WHEN** a scored question has hint blocks and the player presses the hint button
- **THEN** the hint images and text are delivered without ending the question

#### Scenario: Explaining an answer
- **WHEN** an answer has explanation blocks
- **THEN** those blocks are delivered after the correct or wrong result

### Requirement: Editing and deletion permissions
Any user who can access a question SHALL be able to update its content. Only the creator SHALL be able to move it to another folder. Deleting another user's question SHALL require the folder owner or co-owner role.

#### Scenario: Contributor edits a shared question
- **WHEN** a contributor updates the content of a question in a folder they belong to
- **THEN** the update is accepted

#### Scenario: Contributor deletes another user's question
- **WHEN** a contributor attempts to delete a question created by someone else in the folder
- **THEN** the system responds 403

#### Scenario: Moving a question
- **WHEN** a user other than the creator attempts to move a question to another folder
- **THEN** the question is left unchanged

### Requirement: Folder assignment
The system SHALL accept a folder when creating or updating a question only if the user has access to that folder. Moving a question into a folder SHALL be restricted to the question's creator.

#### Scenario: Assigning to an inaccessible folder
- **WHEN** a user creates a question referencing a folder they cannot access
- **THEN** the system responds 404 and creates no question

#### Scenario: Unassigning from a folder
- **WHEN** the creator moves a question out of its folder
- **THEN** the question remains in the library without a folder

### Requirement: Question ordering
The system SHALL maintain a single order index per creator and SHALL append new questions after the creator's highest index. Reordering SHALL update only the positions of questions the user can access.

#### Scenario: Creating a question
- **WHEN** a user creates a question without an explicit index
- **THEN** it receives the next index after their existing questions

#### Scenario: Reordering a mixed list
- **WHEN** a reorder request includes a question the user cannot access
- **THEN** accessible questions are repositioned and the inaccessible one is ignored

### Requirement: Import
The system SHALL import a list of questions, converting inline `data:` image URIs into stored images. Imported questions SHALL be unassigned from folders. Import SHALL be partial: valid questions are created and per-question errors are returned for the rest.

#### Scenario: Partially valid import
- **WHEN** an import contains one valid question and one malformed question
- **THEN** the valid question is created and the response reports one imported question and one error

#### Scenario: Imported images
- **WHEN** an imported question contains an inline data URI image
- **THEN** the image is stored separately and the question references it by identifier

### Requirement: Export
The system SHALL export the requested accessible questions with their configuration, optionally embedding referenced images as inline data URIs. The export SHALL carry a format version, creation time, author, and source label.

#### Scenario: Exporting with images
- **WHEN** a user exports questions with the image option enabled
- **THEN** each referenced image is embedded as a data URI in the exported payload

#### Scenario: Exporting without access
- **WHEN** the export request includes a question the user cannot access
- **THEN** that question is omitted from the export

### Requirement: Image storage and serving
The system SHALL store uploaded images in the database and serve them by identifier through a public endpoint using the stored content type. Uploads SHALL require authentication. Images larger than 2 MB SHALL be resized to at most 1920 pixels on the long edge at 85% quality.

#### Scenario: Uploading a large image
- **WHEN** an authenticated user uploads an image larger than 2 MB
- **THEN** the stored image is resized within the 1920-pixel bound

#### Scenario: Anonymous image read
- **WHEN** any client requests a stored image by identifier
- **THEN** the image is returned without authentication
