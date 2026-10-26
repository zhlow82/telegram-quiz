# Tasks

## 1. Test Infrastructure Setup

- [x] 1.1 Create `FolderServiceTest.java` unit test class with Mockito setup, and verify it compiles and runs with `mvn test -Dtest=FolderServiceTest`
- [x] 1.2 Create `FolderIntegrationTestBase.java` base class for integration tests with Testcontainers and MockMvc setup, and verify it works by creating a minimal test that passes

## 2. Unit Tests for FolderService

- [x] 2.1 Write unit test for `declineInvitation()` bug: test that declining an ACCEPTED invitation should fail (not silently succeed), verify with `mvn test -Dtest=FolderServiceTest#declineAcceptedInvitationFails`
- [x] 2.2 Write unit test for `declineInvitation()`: test that declining a PENDING invitation succeeds and marks it as DECLINED, verify with `mvn test -Dtest=FolderServiceTest#declinePendingInvitationSucceeds`
- [x] 2.3 Write unit test for `declineInvitation()`: test that declining an already DECLINED invitation fails with appropriate error, verify with `mvn test -Dtest=FolderServiceTest#declineAlreadyDeclinedInvitationFails`

## 3. Integration Tests - Folder Operations (Requirements 1-2)

- [x] 3.1 Write integration test for requirement 1 scenario 1 (folder listing): verify user sees only their own folders with OWNER role, verify with `mvn verify -Dit.test=FolderListingIT#ownerSeesOwnFolders`
- [x] 3.2 Write integration test for requirement 1 scenario 2 (shared folder listing): verify accepted co-owner sees shared folder with CO_OWNER role, verify with `mvn verify -Dit.test=FolderListingIT#acceptedCoOwnerSeesSharedFolder`
- [x] 3.3 Write integration test for requirement 1 scenario 3 (pending folder hidden): verify pending invitation does not appear in folder listing, verify with `mvn verify -Dit.test=FolderListingIT#pendingInvitationHiddenFromListing`
- [x] 3.4 Write integration test for requirement 2 scenario 1 (folder creation): verify new folder is appended to end of user's folder order, verify with `mvn verify -Dit.test=FolderOperationsIT#createFolderAppendedToEnd`
- [x] 3.5 Write integration test for requirement 2 scenario 2 (folder reordering): verify owner can reorder folders and order is persisted, verify with `mvn verify -Dit.test=FolderOperationsIT#reorderFoldersPersisted`

## 4. Integration Tests - Roles and Access (Requirements 3, 8)

- [x] 4.1 Write integration test for requirement 3 scenario 1 (contributor permissions): verify contributor cannot rename folder or manage members (403), verify with `mvn verify -Dit.test=FolderAccessIT#contributorCannotRenameFolder` and `#contributorCannotManageMembers`
- [x] 4.2 Write integration test for requirement 3 scenario 2 (co-owner permissions): verify co-owner can invite and remove members, verify with `mvn verify -Dit.test=FolderAccessIT#coOwnerCanInviteMembers` and `#coOwnerCanRemoveMembers`
- [x] 4.3 Write integration test for requirement 3 scenario 3 (pending invitation holder): verify pending invitation holder cannot read folder content (404), verify with `mvn verify -Dit.test=FolderAccessIT#pendingInvitationHolderCannotReadContent`
- [x] 4.4 Write integration test for requirement 8 scenario 1 (no access): verify user with no relationship gets 404, verify with `mvn verify -Dit.test=FolderAccessIT#noRelationshipGets404`
- [x] 4.5 Write integration test for requirement 8 scenario 2 (insufficient role): verify contributor attempting owner action gets 403, verify with `mvn verify -Dit.test=FolderAccessIT#contributorOwnerActionGets403`

## 5. Integration Tests - Folder Rename and Deletion (Requirement 4)

- [x] 5.1 Write integration test for requirement 4 scenario 1 (delete folder with questions): verify folder deleted and questions remain without folder, verify with `mvn verify -Dit.test=FolderDeletionIT#deleteFolderWithQuestionsKeepsQuestions`
- [x] 5.2 Write integration test for requirement 4 scenario 2 (delete folder removes shares): verify accepted members lose access after folder deletion, verify with `mvn verify -Dit.test=FolderDeletionIT#deleteFolderRemovesShares`

## 6. Integration Tests - Invitations (Requirements 5-6)

- [x] 6.1 Write integration test for requirement 5 scenario 1 (successful invitation): verify co-owner can invite user with valid role, verify with `mvn verify -Dit.test=FolderInvitationIT#coOwnerCanInviteValidRole`
- [x] 6.2 Write integration test for requirement 5 scenario 2 (invite owner): verify inviting folder owner returns 400, verify with `mvn verify -Dit.test=FolderInvitationIT#inviteOwnerReturns400`
- [x] 6.3 Write integration test for requirement 5 scenario 3 (duplicate invitation): verify inviting user with existing membership returns 409, verify with `mvn verify -Dit.test=FolderInvitationIT#duplicateInvitationReturns409`
- [x] 6.4 Write integration test for requirement 5 scenario 4 (invalid role): verify invitation with invalid role returns 400, verify with `mvn verify -Dit.test=FolderInvitationIT#invalidRoleReturns400`
- [x] 6.5 Write integration test for requirement 6 scenario 1 (accept invitation): verify accepted invitation makes folder visible to invitee, verify with `mvn verify -Dit.test=FolderInvitationIT#acceptInvitationMakesFolderVisible`
- [x] 6.6 Write integration test for requirement 6 scenario 2 (decline invitation): verify declined invitation keeps folder hidden, verify with `mvn verify -Dit.test=FolderInvitationIT#declineInvitationKeepsFolderHidden`
- [x] 6.7 Write integration test for requirement 6 scenario 3 (process someone else's invitation): verify accepting another user's invitation returns 403, verify with `mvn verify -Dit.test=FolderInvitationIT#acceptOthersInvitationReturns403`
- [x] 6.8 Write integration test for requirement 6 scenario 4 (process resolved invitation): verify accepting already-accepted invitation returns 400, verify with `mvn verify -Dit.test=FolderInvitationIT#acceptAlreadyAcceptedReturns400`
- [x] 6.9 Write integration test for requirement 6 scenario 5 (re-invite declined user): verify re-inviting declined user returns 409, verify with `mvn verify -Dit.test=FolderInvitationIT#reInviteDeclinedUserReturns409`

## 7. Integration Tests - Member Management (Requirement 7)

- [x] 7.1 Write integration test for requirement 7 scenario 1 (list members): verify accepted contributor can list folder members including pending/declined, verify with `mvn verify -Dit.test=FolderMemberIT#listMembersIncludesPendingAndDeclined`
- [x] 7.2 Write integration test for requirement 7 scenario 2 (change role): verify co-owner can promote contributor to co-owner, verify with `mvn verify -Dit.test=FolderMemberIT#promoteContributorToCoOwner`
- [x] 7.3 Write integration test for requirement 7 scenario 3 (remove member): verify co-owner can remove accepted member and they lose access, verify with `mvn verify -Dit.test=FolderMemberIT#removeMemberLosesAccess`

## 8. Integration Tests - Question Access (Requirement 9)

- [x] 8.1 Write integration test for requirement 9 scenario 1 (co-owner sees questions): verify accepted co-owner sees folder questions in library, verify with `mvn verify -Dit.test=FolderQuestionAccessIT#coOwnerSeesFolderQuestions`
- [x] 8.2 Write integration test for requirement 9 scenario 2 (membership revoked): verify removed member no longer sees another user's question, verify with `mvn verify -Dit.test=FolderQuestionAccessIT#removedMemberCannotSeeQuestions`

## 9. Final Verification

- [x] 9.1 Run all folders-sharing tests with `cd src/backend/main-service && mvn test` and verify all unit tests pass
- [x] 9.2 Run all folders-sharing integration tests with `cd src/backend/main-service && mvn verify` and verify all IT tests pass
- [x] 9.3 Verify scenario coverage: confirm all 26 folders-sharing scenarios have at least one named test, record the scenario→test map in tasks.md, and ensure no deferrals are needed (or record deferrals per testing-conventions)

**Scenario Coverage Map:**

**Requirement 1: Folder listing**
- Scenario 1 (owner listing): `FolderListingIT#ownerSeesOwnFolders`
- Scenario 2 (accepted share appears): `FolderListingIT#acceptedCoOwnerSeesSharedFolder`
- Scenario 3 (pending share hidden): `FolderListingIT#pendingInvitationHiddenFromListing`

**Requirement 2: Flat folders and ordering**
- Scenario 4 (creating a folder): `FolderOperationsIT#createFolderAppendedToEnd`
- Scenario 5 (reordering folders): `FolderOperationsIT#reorderFoldersPersisted`

**Requirement 3: Folder roles and access levels**
- Scenario 6 (contributor permissions): `FolderAccessIT#contributorCannotRenameFolder`, `FolderAccessIT#contributorCannotManageMembers`
- Scenario 7 (co-owner permissions): `FolderAccessIT#coOwnerCanInviteMembers`, `FolderAccessIT#coOwnerCanRemoveMembers`
- Scenario 8 (pending invitation reads content): `FolderAccessIT#pendingInvitationHolderCannotReadContent`

**Requirement 4: Folder rename and deletion**
- Scenario 9 (deleting folder with questions): `FolderDeletionIT#deleteFolderWithQuestionsKeepsQuestions`
- Scenario 10 (deleting folder removes shares): `FolderDeletionIT#deleteFolderRemovesShares`

**Requirement 5: Invitations**
- Scenario 11 (successful invitation): `FolderInvitationIT#coOwnerCanInviteValidRole`
- Scenario 12 (inviting the owner): `FolderInvitationIT#inviteOwnerReturns400`
- Scenario 13 (duplicate invitation): `FolderInvitationIT#duplicateInvitationReturns409`
- Scenario 14 (invalid role): `FolderInvitationIT#invalidRoleReturns400`

**Requirement 6: Accepting and declining invitations**
- Scenario 15 (accepting invitation): `FolderInvitationIT#acceptInvitationMakesFolderVisible`
- Scenario 16 (declining invitation): `FolderInvitationIT#declineInvitationKeepsFolderHidden`
- Scenario 17 (processing someone else's invitation): `FolderInvitationIT#acceptOthersInvitationReturns403`
- Scenario 18 (processing resolved invitation): `FolderInvitationIT#acceptAlreadyAcceptedReturns400`
- Scenario 19 (declined user cannot be re-invited): `FolderInvitationIT#reInviteDeclinedUserReturns409`

**Requirement 7: Member management**
- Scenario 20 (listing members): `FolderMemberIT#listMembersIncludesPendingAndDeclined`
- Scenario 21 (changing role): `FolderMemberIT#promoteContributorToCoOwner`
- Scenario 22 (removing member): `FolderMemberIT#removeMemberLosesAccess`

**Requirement 8: Access error semantics**
- Scenario 23 (no access): `FolderAccessIT#noRelationshipGets404`
- Scenario 24 (insufficient role): `FolderAccessIT#contributorOwnerActionGets403`

**Requirement 9: Folder membership grants question access**
- Scenario 25 (co-owner sees folder questions): `FolderQuestionAccessIT#coOwnerSeesFolderQuestions`
- Scenario 26 (membership revoked): `FolderQuestionAccessIT#removedMemberCannotSeeQuestions`

**Coverage:** 26/26 scenarios covered (100%)

## Workflow follow-up

- ✅ If the declineInvitation bug test fails (as expected), report the bug and pause - do not adjust the test to hide the bug
- ✅ Archive the change after all tests pass and the bug decision is made
- Update testing-conventions skill with folders-sharing coverage state after archive
