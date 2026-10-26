# folders-sharing Specification

## Purpose

Defines how users organize questions into folders and share them: folder ownership, the co-owner and contributor roles, invitation and acceptance flow, member management, ordering, and what happens to shared content when a folder is deleted.

## Requirements

### Requirement: Folder listing
The system SHALL list, for the current user, folders they own followed by folders shared with them through an accepted membership. The response SHALL report the user's role in each folder.

#### Scenario: Owner listing
- **WHEN** a user lists folders
- **THEN** their own folders appear with role `OWNER`

#### Scenario: Accepted share appears
- **WHEN** a user has accepted a co-owner invitation
- **THEN** the folder appears in their listing with role `CO_OWNER`

#### Scenario: Pending share is hidden
- **WHEN** a user has a pending invitation that they have not accepted
- **THEN** the folder does not appear in their listing

### Requirement: Flat folders and ordering
Folders SHALL be a flat list without nesting. Each folder SHALL carry a sort position within its owner's list, and only the owner SHALL be able to reorder their folders.

#### Scenario: Creating a folder
- **WHEN** a user creates a folder
- **THEN** it is appended to the end of that user's folder order

#### Scenario: Reordering folders
- **WHEN** the owner submits an ordered list of their folder identifiers
- **THEN** the stored positions follow the submitted order

### Requirement: Folder roles and access levels
The system SHALL support two membership roles: `CO_OWNER` and `CONTRIBUTOR`. Owners have full access; accepted co-owners may manage the folder and its members; accepted contributors may view and work with its questions; pending and declined invitations grant no access.

#### Scenario: Contributor permissions
- **WHEN** an accepted contributor tries to rename the folder or manage members
- **THEN** the system responds 403

#### Scenario: Co-owner permissions
- **WHEN** an accepted co-owner invites or removes a member
- **THEN** the action is allowed

#### Scenario: Pending invitation holder reads content
- **WHEN** a user with only a pending invitation accesses a question in the folder
- **THEN** the question behaves as not found

### Requirement: Folder rename and deletion
The system SHALL allow renaming and deleting a folder for the owner and co-owners. Deleting a folder SHALL detach its questions instead of deleting them, and SHALL remove all membership records for that folder.

#### Scenario: Deleting a folder with questions
- **WHEN** an owner deletes a folder containing questions
- **THEN** the folder disappears and its questions remain in the library without a folder

#### Scenario: Deleting a folder removes shares
- **WHEN** a folder with accepted members is deleted
- **THEN** those members lose access and the folder no longer appears in their listing

### Requirement: Invitations
The system SHALL let the owner and co-owners invite an existing user by username with role `CO_OWNER` or `CONTRIBUTOR`. The system SHALL reject inviting oneself, inviting the folder owner, inviting a user who already has a record for that folder, and unknown roles.

#### Scenario: Successful invitation
- **WHEN** a co-owner invites a user with a valid role
- **THEN** a pending membership is created and the inviter is recorded

#### Scenario: Inviting the owner
- **WHEN** a member invites the folder owner
- **THEN** the system responds 400

#### Scenario: Duplicate invitation
- **WHEN** a user who already has a membership record for the folder is invited again
- **THEN** the system responds 409

#### Scenario: Invalid role
- **WHEN** an invitation names a role other than `CO_OWNER` or `CONTRIBUTOR`
- **THEN** the system responds 400

### Requirement: Accepting and declining invitations
The system SHALL show pending invitations to the invited user together with the folder name. Only the invited user SHALL be able to accept or decline, and only while the invitation is pending.

#### Scenario: Accepting an invitation
- **WHEN** the invited user accepts a pending invitation
- **THEN** the membership becomes accepted and the folder appears in their listing

#### Scenario: Declining an invitation
- **WHEN** the invited user declines a pending invitation
- **THEN** the membership becomes declined and the folder remains hidden

#### Scenario: Processing someone else's invitation
- **WHEN** a user tries to accept an invitation addressed to another user
- **THEN** the system responds 403

#### Scenario: Processing a resolved invitation
- **WHEN** an invitation is accepted or declined a second time
- **THEN** the system responds 400

#### Scenario: Declined user cannot be re-invited
- **WHEN** an owner invites a user whose previous invitation was declined and whose membership was not removed
- **THEN** the system responds 409

### Requirement: Member management
The system SHALL let any accepted member list a folder's members. Changing a member's role or removing a member SHALL require the owner or a co-owner. A removed member SHALL lose access immediately.

#### Scenario: Listing members
- **WHEN** an accepted contributor lists the folder's members
- **THEN** the member records are returned including pending and declined invitations

#### Scenario: Changing a role
- **WHEN** a co-owner changes a contributor to co-owner
- **THEN** the member gains co-owner permissions

#### Scenario: Removing a member
- **WHEN** a co-owner removes an accepted member
- **THEN** that user can no longer access the folder's questions

### Requirement: Access error semantics
The system SHALL report a folder as not found when the user has no access level at all, and SHALL report insufficient permission with 403 when the user has access but lacks the required role.

#### Scenario: No access
- **WHEN** a user with no relationship to a folder requests its members
- **THEN** the system responds 404

#### Scenario: Insufficient role
- **WHEN** a contributor attempts an owner-only action
- **THEN** the system responds 403

### Requirement: Folder membership grants question access
Questions placed in a folder SHALL be visible to accepted members of that folder, and membership SHALL be the basis for editing and deleting shared questions.

#### Scenario: Co-owner sees folder questions
- **WHEN** an accepted co-owner lists the question library
- **THEN** questions in the shared folder are included

#### Scenario: Membership revoked
- **WHEN** a member is removed from a folder containing another user's question
- **THEN** that question is no longer visible to them
