# Tasks

## 1. Extend Question Visibility Tests

- [ ] 1.1 Extend QuestionAccessIT with test for "Reading another user's private question" scenario: verify GET /api/questions/{id} returns 404 when user has no access, verify with `mvn test -Dtest=QuestionAccessIT#privateQuestionReturns404`
- [ ] 1.2 Extend QuestionAccessIT with test for "Reading a shared folder question" scenario: verify GET /api/questions/{id} returns 200 with question data when user has accepted folder membership, verify with `mvn test -Dtest=QuestionAccessIT#sharedFolderQuestionAccessible`

## 2. Question Content Tests

- [ ] 2.1 Create QuestionContentIT class with test for "Mixed content" scenario: verify question with text and image blocks stores and returns blocks in original order, verify with `mvn test -Dtest=QuestionContentIT#mixedContentPreservesOrder`
- [ ] 2.2 Add test to QuestionContentIT for "Image block rendering" scenario: verify image blocks reference stored image identifiers, verify with `mvn test -Dtest=QuestionContentIT#imageBlockReferencesStoredImage`

## 3. Question Kinds and Precedence Tests

- [ ] 3.1 Create QuestionKindsIT class with test for "Text input flagged with briefing" scenario: verify question marked as both text-input and briefing is handled as text-input and not scored, verify with `mvn test -Dtest=QuestionKindsIT#textInputWithBriefingHandledAsTextInput`
- [ ] 3.2 Add test to QuestionKindsIT for "Scored total" scenario: verify quiz with scored, briefing, and text-input questions counts only scored questions in total, verify with `mvn test -Dtest=QuestionKindsIT#scoredTotalExcludesBriefingAndTextInput`

## 4. Scored Multiple-Choice Tests

- [ ] 4.1 Create QuestionScoringIT class with test for "Correct selection" scenario: verify selecting option equal to stored answer (ignoring case and whitespace) records correct answer and increases score, verify with `mvn test -Dtest=QuestionScoringIT#correctSelectionIncreasesScore`
- [ ] 4.2 Add test to QuestionScoringIT for "Wrong selection" scenario: verify selecting different option records wrong answer and response names correct answer, verify with `mvn test -Dtest=QuestionScoringIT#wrongSelectionNamesCorrectAnswer`
- [ ] 4.3 Add test to QuestionScoringIT for question without stored answer: verify question with no stored answer is never judged correct, verify with `mvn test -Dtest=QuestionScoringIT#noStoredAnswerNeverCorrect`

## 5. Photo Questions Tests

- [ ] 5.1 Create QuestionPhotoIT class with test for "Submitting a photo answer" scenario: verify photo submission for expectPhoto question stores Telegram file reference and increases score by one, verify with `mvn test -Dtest=QuestionPhotoIT#photoAnswerStoredAndScored`
- [ ] 5.2 Add test to QuestionPhotoIT for "Replacing a photo answer" scenario: verify sending another photo updates stored photo and caption without awarding another point, verify with `mvn test -Dtest=QuestionPhotoIT#photoReplacementUpdatesWithoutRescoring`

## 6. Briefing Configuration Tests

- [ ] 6.1 Create QuestionBriefingIT class with test for "Default briefing" scenario: verify briefing created without button settings shows both buttons with default texts "READY" and "Start Timer", verify with `mvn test -Dtest=QuestionBriefingIT#defaultBriefingShowsBothButtons`
- [ ] 6.2 Add test to QuestionBriefingIT for "Hidden button" scenario: verify briefing configured to hide secondary button shows only primary button, verify with `mvn test -Dtest=QuestionBriefingIT#hiddenSecondaryButtonNotShown`

## 7. After-Answer Configuration Tests

- [ ] 7.1 Create QuestionAfterAnswerIT class with test for "Default next button" scenario: verify scored question created without after-answer settings shows next button with text "Next Question", verify with `mvn test -Dtest=QuestionAfterAnswerIT#defaultNextButtonShown`
- [ ] 7.2 Add test to QuestionAfterAnswerIT for "Button hidden" scenario: verify after-answer button hidden causes quiz to advance automatically after showing result, verify with `mvn test -Dtest=QuestionAfterAnswerIT#hiddenButtonAdvancesAutomatically`

## 8. Hints and Explanations Tests

- [ ] 8.1 Create QuestionHintsIT class with test for "Requesting a hint" scenario: verify hint button press delivers hint blocks without ending question, verify with `mvn test -Dtest=QuestionHintsIT#hintRequestDeliversBlocks`
- [ ] 8.2 Add test to QuestionHintsIT for "Explaining an answer" scenario: verify answer with explanation blocks delivers them after correct or wrong result, verify with `mvn test -Dtest=QuestionHintsIT#explanationDeliveredAfterResult`

## 9. Editing and Deletion Permissions Tests

- [ ] 9.1 Create QuestionPermissionsIT class with test for "Contributor edits a shared question" scenario: verify contributor can update content of question in folder they belong to, verify with `mvn test -Dtest=QuestionPermissionsIT#contributorCanEditSharedQuestion`
- [ ] 9.2 Add test to QuestionPermissionsIT for "Contributor deletes another user's question" scenario: verify contributor attempting to delete another user's question receives 403, verify with `mvn test -Dtest=QuestionPermissionsIT#contributorCannotDeleteOthersQuestion`
- [ ] 9.3 Add test to QuestionPermissionsIT for "Moving a question" scenario: verify non-creator attempting to move question to another folder leaves question unchanged, verify with `mvn test -Dtest=QuestionPermissionsIT#nonCreatorCannotMoveQuestion`

## 10. Folder Assignment Tests

- [ ] 10.1 Create QuestionFolderIT class with test for "Assigning to an inaccessible folder" scenario: verify creating question with folder user cannot access returns 404 and creates no question, verify with `mvn test -Dtest=QuestionFolderIT#inaccessibleFolderReturns404`
- [ ] 10.2 Add test to QuestionFolderIT for "Unassigning from a folder" scenario: verify creator moving question out of folder leaves question in library without folder, verify with `mvn test -Dtest=QuestionFolderIT#unassignFromFolderKeepsInLibrary`

## 11. Question Ordering Tests

- [ ] 11.1 Create QuestionOrderingIT class with test for "Creating a question" scenario: verify creating question without explicit index receives next index after existing questions, verify with `mvn test -Dtest=QuestionOrderingIT#newQuestionGetsNextIndex`
- [ ] 11.2 Add test to QuestionOrderingIT for "Reordering a mixed list" scenario: verify reorder request with inaccessible question repositions accessible questions and ignores inaccessible one, verify with `mvn test -Dtest=QuestionOrderingIT#reorderIgnoresInaccessible`

## 12. Import Tests

- [ ] 12.1 Create QuestionImportIT class with test for "Partially valid import" scenario: verify import with one valid and one malformed question creates valid question and reports one imported and one error, verify with `mvn test -Dtest=QuestionImportIT#partialImportCreatesValidAndReportsErrors`
- [ ] 12.2 Add test to QuestionImportIT for "Imported images" scenario: verify imported question with inline data URI image stores image separately and references by identifier, verify with `mvn test -Dtest=QuestionImportIT#importedImageStoredAndReferenced`

## 13. Export Tests

- [ ] 13.1 Create QuestionExportIT class with test for "Exporting with images" scenario: verify export with image option enabled embeds referenced images as data URIs, verify with `mvn test -Dtest=QuestionExportIT#exportWithImagesEmbedsDataUris`
- [ ] 13.2 Add test to QuestionExportIT for "Exporting without access" scenario: verify export request with inaccessible question omits that question from export, verify with `mvn test -Dtest=QuestionExportIT#exportOmitsInaccessibleQuestion`

## 14. Image Storage Tests

- [ ] 14.1 Create QuestionImageIT class with test for "Uploading a large image" scenario: verify uploading image larger than 2 MB resizes to at most 1920 pixels on long edge at 85% quality, verify with `mvn test -Dtest=QuestionImageIT#largeImageResized`
- [ ] 14.2 Add test to QuestionImageIT for "Anonymous image read" scenario: verify requesting stored image by identifier returns image without authentication, verify with `mvn test -Dtest=QuestionImageIT#anonymousImageRead`

## 15. Full Test Suite Verification

- [ ] 15.1 Run all question-bank tests with `cd src/backend/main-service && mvn test -Dtest="Question*IT"` and verify all tests pass
- [ ] 15.2 Run full main-service test suite with `cd src/backend/main-service && mvn verify` and verify all tests pass (including existing folders-sharing tests)
- [ ] 15.3 Verify scenario coverage: confirm all 29 question-bank scenarios have at least one named test, record the scenario→test map in tasks.md, and ensure no deferrals are needed (or record deferrals per testing-conventions)

## Workflow follow-up

- Archive the change after all tests pass
- Update testing-conventions skill with question-bank coverage state after archive
