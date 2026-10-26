# Tasks

## 1. Unit tests — OAuth2SuccessHandler (scenarios 10.1, 10.2)

- [x] 1.1 Create `src/backend/auth-service/src/test/java/com/telegramquiz/auth/security/OAuth2SuccessHandlerTest.java` (Mockito, no Spring context; ctor `(UserRepository, JwtUtil, StringRedisTemplate)`, `ReflectionTestUtils.setField(handler, "frontendUrl", ...)`): registered Google user → redirect `<frontendUrl>/oauth2/callback?token=<valid access token carrying role>`; unknown user → hash write to `oauth2:pending:<state>` (sub/email/firstName/lastName), 10-minute expiry, redirect `<frontendUrl>/oauth2/register?state=<state>`. DisplayNames `auth / Google sign-in / ...`. Verify: `cd src/backend/auth-service && mvn test -Dtest=OAuth2SuccessHandlerTest` passes.

## 2. Unit tests — DataInitializer (scenarios 14.1, 14.2)

- [x] 2.1 Create `src/backend/auth-service/src/test/java/com/telegramquiz/auth/config/DataInitializerTest.java` (Mockito): first startup seeds `localadmin` with encoded `password88` + `ROLE_ADMIN`; existing `localadmin` with role stripped regains `ROLE_ADMIN` with the stored password hash untouched (verify `save` receives the same hash, encoder never re-invoked). DisplayNames `auth / Built-in administrator account / ...`. Verify: `cd src/backend/auth-service && mvn test -Dtest=DataInitializerTest` passes.

## 3. IT — AuthLoginIT (scenarios 1.1, 1.2, 2.2, 3.2, 3.3, 5.2)

- [x] 3.1 Create `src/backend/auth-service/src/test/java/com/telegramquiz/auth/integration/AuthLoginIT.java` (copy `AuthFlowIT` boot pattern incl. Boot-4 `AutoConfigureMockMvc` import; unique usernames): successful sign-in asserts `tokenType="Bearer"` + both tokens (1.1); wrong password → 401 (1.2); expired bearer on `GET /auth/me` → 403, expired token minted via local `JwtUtil` with `expiration=-1000` (2.2); second-device login overwrites Redis so the first refresh token then fails 401 (3.2); valid-signature but never-stored refresh token → 401 (3.3); logout without header and with garbage token → 204 both ways (5.2). Verify: `cd src/backend/auth-service && mvn verify -Dit.test=AuthLoginIT` passes.

## 4. IT — ProfileAndPasswordIT (scenarios 7.1–7.3, 8.1–8.2)

- [x] 4.1 Create `src/backend/auth-service/src/test/java/com/telegramquiz/auth/integration/ProfileAndPasswordIT.java`: change password success → 204 then login with new password works and old fails (7.1); wrong current password → 401 + old password still works (7.2); user with `googleSub` → 400 "Password change is not available for Google accounts." (7.3); `GET /auth/me` full field map incl. `provider` = `google` iff `googleSub` present, else `local` (8.1); `PATCH /auth/profile` whitespace-only firstName → 204, then `/auth/me` reports `""` (8.2). Google-user tokens minted via autowired `JwtUtil.generateAccessToken(..., "google")`. Verify: `cd src/backend/auth-service && mvn verify -Dit.test=ProfileAndPasswordIT` passes.

## 5. IT — AdminUserManagementIT (scenarios 13.1–13.3, 3.1)

- [x] 5.1 Create `src/backend/auth-service/src/test/java/com/telegramquiz/auth/integration/AdminUserManagementIT.java`: admin deletes own id → 400, user still present (13.1); password reset on Google account → 409, stored hash unchanged (13.2); deactivate → login 401 AND previously stored refresh token → 401 (13.3); promote dedicated member to `ROLE_ADMIN` then `POST /auth/refresh` → new access token carries `ROLE_ADMIN` and a subsequent admin-route call succeeds (3.1 — never mutate `localadmin`). Verify: `cd src/backend/auth-service && mvn verify -Dit.test=AdminUserManagementIT` passes.

## 6. IT — GoogleRegistrationIT (scenarios 9.1–9.3, 11.1–11.4, 12.1–12.2)

- [x] 6.1 Create `src/backend/auth-service/src/test/java/com/telegramquiz/auth/integration/GoogleRegistrationIT.java` (`@TestMethodOrder(OrderAnnotation)`; delete `google_client_id`/`google_client_secret` from `app_settings` first so 9.1 observes the unconfigured state): `GET /auth/oauth2/configured` → `{"configured":false}` before settings exist (9.1); admin `GET /auth/admin/settings/google` → `{clientId, secretConfigured}` and the response never contains the secret value (9.2); `PUT` with blank `clientSecret` preserves the stored secret (9.3, verify via repository + `secretConfigured:true`); completion happy path with Redis-seeded `oauth2:pending:<state>` hash + active code → 200 accessToken, user created ROLE_MEMBER/derived username/provider google, Redis key deleted (11.1); unknown state → 400 (11.2); unknown or deactivated code → 403 and no user created (11.3); already-registered `googleSub` → 400 (11.4); same active code reused for two subs → both 200, code still active (12.1); `DELETE /auth/admin/invitation-codes/{id}` then complete → 403 (12.2); codes created via admin endpoint assert 12-char uppercase-hex format. Verify: `cd src/backend/auth-service && mvn verify -Dit.test=GoogleRegistrationIT` passes.

## 7. IT — BrandingSettingsIT (scenarios 15.1, 15.2)

- [x] 7.1 Create `src/backend/auth-service/src/test/java/com/telegramquiz/auth/integration/BrandingSettingsIT.java` (`@TestMethodOrder(OrderAnnotation)`; clear `app_name`/`login_welcome_text`/`app_logo_blob_id` from `app_settings` in the first ordered test): public unauthenticated `GET /auth/settings/branding` → `appName="Telegram Quiz"`, `loginWelcomeText=""`, `appLogoUrl` JSON null (15.1); admin `PUT /auth/admin/settings/branding` with `appLogoBlobId` → public GET returns `appLogoUrl="/api/files/<blobId>"` (15.2). Verify: `cd src/backend/auth-service && mvn verify -Dit.test=BrandingSettingsIT` passes.

## 8. Full-suite verification and coverage closeout

- [x] 8.1 Run the complete authorized scope — `cd src/backend/auth-service && mvn test` then `cd src/backend/auth-service && mvn verify` — all unit tests and all IT classes (existing + new) green in one run; record the commands and totals here.
  > Evidence: `mvn test` → 9/9 unit tests green (JwtUtilTest 4, OAuth2SuccessHandlerTest 3, DataInitializerTest 2). `mvn verify` → 31/31 IT tests green in one JVM (AuthFlowIT 4, AuthLoginIT 6, ProfileAndPasswordIT 5, AdminUserManagementIT 4, GoogleRegistrationIT 10, BrandingSettingsIT 2) + the 9 unit tests re-run green. Zero cross-class interference observed.
- [x] 8.2 Coverage closeout: confirm every one of the 36 `auth` spec scenarios now maps to a named test (existing or new); record the scenario→test map (or any deferral per `testing-conventions`) in a note under this task, and run `openspec validate --all --strict` → 7/7 pass.
  > Scenario→test map (36/36, no deferrals):
  > - Password sign-in: Successful sign-in → `AuthLoginIT.successfulSignInReturnsBearerTokens`; Wrong password → `AuthLoginIT.wrongPasswordIsRejectedWithoutTokens`; Deactivated account → `AuthFlowIT.deactivatedAccountCannotSignIn`
  > - Access token contents and lifetime: Authenticates a request → `JwtUtilTest.accessTokenCarriesExpectedClaims` + `AuthFlowIT.loginRefreshLogout`; Expired access token → `JwtUtilTest.expiredAccessTokenIsRejected` + `AuthLoginIT.expiredAccessTokenIsTreatedAsUnauthenticated`
  > - Refresh token rotation and storage: Refreshing an access token → `AdminUserManagementIT.refreshReflectsCurrentRoleAfterPromotion`; Second device → `AuthLoginIT.secondSignInReplacesFirstRefreshToken`; Revoked refresh token → `AuthLoginIT.neverStoredRefreshTokenIsRejected` (+ post-logout in `AuthFlowIT.loginRefreshLogout`)
  > - Refresh tokens are not API credentials: bearer rejection → `AuthFlowIT.refreshTokenIsNotAnApiCredential`
  > - Logout revokes the session: Invalidates refresh → `AuthFlowIT.loginRefreshLogout`; Without a valid token → `AuthLoginIT.logoutWithoutValidTokenStillReturns204`
  > - Administrator authorization: Member 403 / Admin allowed → `AuthFlowIT.adminRouteRequiresAdminRole`
  > - Password change: Changing → `ProfileAndPasswordIT.changingPasswordReplacesTheOldOne`; Incorrect current → `ProfileAndPasswordIT.incorrectCurrentPasswordKeepsTheOldOne`; Google account → `ProfileAndPasswordIT.googleAccountCannotChangePassword`
  > - Profile retrieval and update: Reading the profile → `ProfileAndPasswordIT.profileReportsProviderAndFields`; Clearing a name → `ProfileAndPasswordIT.whitespaceNameIsStoredAsEmpty`
  > - Google sign-in configuration: Not configured → `GoogleRegistrationIT.googleIsNotConfiguredWhenSettingsAreMissing`; Reading as admin → `GoogleRegistrationIT.adminReadsSettingsWithoutSeeingTheSecret`; Saving without new secret → `GoogleRegistrationIT.blankSecretPreservesTheStoredOne`
  > - Google sign-in for registered accounts: Returning user → `OAuth2SuccessHandlerTest.returningGoogleUserIsRedirectedWithAccessToken`; Session lifetime → `OAuth2SuccessHandlerTest.googleSessionEndsWhenAccessTokenExpires`
  > - Invitation-gated Google registration: Completing → `GoogleRegistrationIT.pendingProfileWithActiveCodeCreatesTheAccount`; Expired state → `GoogleRegistrationIT.missingPendingStateIsRejected`; Invalid code → `GoogleRegistrationIT.unknownCodeCreatesNoAccount`; Already registered → `GoogleRegistrationIT.registeredSubjectIsNotDuplicated`; (pending-hold mechanism → `OAuth2SuccessHandlerTest.unknownGoogleUserIsHeldInRedisForRegistration`)
  > - Invitation code lifecycle: Reusing a code → `GoogleRegistrationIT.sameActiveCodeServesTwoRegistrations`; Deactivated code → `GoogleRegistrationIT.deactivatedCodeIsRejected`; (format/start-active → `GoogleRegistrationIT.generatedCodeHasTheDocumentedShape`)
  > - Administrator user management: Deleting own account → `AdminUserManagementIT.adminCannotDeleteOwnAccount`; Resetting Google password → `AdminUserManagementIT.googleAccountPasswordResetIsRejected`; Deactivating → `AdminUserManagementIT.deactivatedAccountCannotSignInOrRefresh`
  > - Built-in administrator account: First startup → `DataInitializerTest.firstStartupCreatesLocalAdmin`; Role removed manually → `DataInitializerTest.removedRoleIsRestoredWithoutTouchingPassword`
  > - Public branding settings: No branding configured → `BrandingSettingsIT.defaultsAreServedWithoutAuthentication`; Custom logo → `BrandingSettingsIT.configuredLogoIsExposedAsPublicFileUrl`

## Workflow follow-up

- Present the change for user review; archive only on explicit user instruction (`skip_specs: true` → no delta merge; move to `openspec/changes/archive/<date>-backfill-auth-scenario-tests`).
- No git commit/push unless the user explicitly asks.
