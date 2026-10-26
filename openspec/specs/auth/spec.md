# auth Specification

## Purpose

Defines how people sign in to Telegram Quiz: password and Google authentication, JWT session tokens, roles and activation, invitation-gated registration, the built-in administrator account, and the public branding values shown on the login screen.

## Requirements

### Requirement: Password sign-in
The system SHALL authenticate a username and password and, on success, return a bearer access token and a refresh token. Unknown usernames, wrong passwords, and deactivated accounts SHALL be rejected with 401 and no tokens.

#### Scenario: Successful sign-in
- **WHEN** an active local account submits the correct username and password
- **THEN** the response contains an access token, a refresh token, and token type `Bearer`

#### Scenario: Wrong password
- **WHEN** the submitted password does not match the stored hash
- **THEN** the system responds 401 and issues no tokens

#### Scenario: Deactivated account
- **WHEN** an account with `active = false` attempts to sign in
- **THEN** the system responds 401 even when the password is correct

### Requirement: Access token contents and lifetime
The system SHALL sign access tokens with the shared JWT secret and include the username as subject, `type=access`, the user id, the account role, and the provider (`local` or `google`). Access tokens SHALL expire 24 hours after issue.

#### Scenario: Access token authenticates a request
- **WHEN** a request carries a valid access token as a bearer credential
- **THEN** the request is authenticated as the token's subject with the token's role

#### Scenario: Expired access token
- **WHEN** an access token older than 24 hours is presented
- **THEN** the request is treated as unauthenticated

### Requirement: Refresh token rotation and storage
The system SHALL issue a refresh token with `type=refresh` and a 7-day lifetime and SHALL store it in Redis per user, replacing any previously stored refresh token. Refresh SHALL succeed only while the presented token equals the stored token.

#### Scenario: Refreshing an access token
- **WHEN** the client submits the currently stored refresh token
- **THEN** the system returns a new access token reflecting the account's current role and provider

#### Scenario: Signing in on a second device
- **WHEN** the same account signs in again
- **THEN** the newly stored refresh token replaces the previous one and the previous refresh token stops working

#### Scenario: Revoked refresh token
- **WHEN** the submitted refresh token does not match the stored token or no token is stored
- **THEN** the system responds 401

### Requirement: Refresh tokens are not API credentials
The system SHALL authenticate protected endpoints only with `type=access` tokens. A refresh token presented as a bearer credential SHALL be treated as unauthenticated.

#### Scenario: Refresh token used as bearer token
- **WHEN** a refresh token is sent in the `Authorization` header to a protected endpoint
- **THEN** the endpoint responds 401 or 403 and does not act on behalf of the user

### Requirement: Logout revokes the session
The system SHALL delete the stored refresh token on logout. Logout SHALL return 204 even when the bearer token is missing or invalid.

#### Scenario: Logout invalidates refresh
- **WHEN** a signed-in client calls logout and then attempts to refresh
- **THEN** the refresh attempt is rejected with 401

#### Scenario: Logout without a valid token
- **WHEN** logout is called without an `Authorization` header or with an invalid token
- **THEN** the system still responds 204

### Requirement: Administrator authorization
The system SHALL treat an account as an administrator when its roles include `ROLE_ADMIN` and SHALL require that role for every `/auth/admin/**` endpoint. New local and Google accounts SHALL receive `ROLE_MEMBER`.

#### Scenario: Member calls an admin endpoint
- **WHEN** a user whose role is `ROLE_MEMBER` calls an admin endpoint
- **THEN** the system responds 403

#### Scenario: Administrator calls an admin endpoint
- **WHEN** a user with `ROLE_ADMIN` calls an admin endpoint
- **THEN** the request is processed

### Requirement: Password change
The system SHALL let an authenticated local account change its own password after confirming the current password. The new password SHALL be at least 8 characters. Google accounts SHALL NOT be able to set a password this way.

#### Scenario: Changing a password
- **WHEN** a local user submits the correct current password and a new password of at least 8 characters
- **THEN** the password is replaced and the request returns 204

#### Scenario: Incorrect current password
- **WHEN** the submitted current password is wrong
- **THEN** the system responds 401 and keeps the old password

#### Scenario: Google account changes password
- **WHEN** a Google-linked account calls the password change endpoint
- **THEN** the system responds 400

### Requirement: Profile retrieval and update
The system SHALL expose the authenticated user's username, first name, last name, email, and provider, and SHALL allow the user to update the names. Blank names SHALL be stored as empty.

#### Scenario: Reading the profile
- **WHEN** an authenticated user requests their profile
- **THEN** the response reports provider `google` when a Google subject exists, otherwise `local`

#### Scenario: Clearing a name
- **WHEN** the user submits a name containing only whitespace
- **THEN** the stored first or last name becomes empty

### Requirement: Google sign-in configuration
The system SHALL offer Google sign-in only when both a client id and client secret are stored. The public configuration endpoint SHALL report availability, and the admin settings endpoint SHALL report only whether a secret is stored, never the secret itself.

#### Scenario: Google not configured
- **WHEN** either the client id or the client secret is missing
- **THEN** the public endpoint reports Google as not configured and no Google sign-in is offered

#### Scenario: Reading Google settings as admin
- **WHEN** an administrator reads the Google settings
- **THEN** the response includes the client id and a boolean indicating whether a secret is stored

#### Scenario: Saving settings without a new secret
- **WHEN** an administrator saves a client id with a blank client secret
- **THEN** the existing secret is preserved

### Requirement: Google sign-in for registered accounts
The system SHALL match returning Google accounts by Google subject id and redirect the browser to the frontend callback with a newly issued access token that carries the account's role. Google sign-in SHALL NOT create a refresh token.

#### Scenario: Returning Google user
- **WHEN** the Google subject matches an existing account
- **THEN** the browser is redirected to the frontend callback with a valid access token

#### Scenario: Session lifetime for Google users
- **WHEN** 24 hours pass after a Google sign-in
- **THEN** the access token expires and the user must sign in with Google again

### Requirement: Invitation-gated Google registration
The system SHALL hold unknown Google profiles in Redis for 10 minutes and SHALL create an account only when the user supplies an active invitation code. The new account SHALL receive `ROLE_MEMBER` and a unique username derived from the Google profile.

#### Scenario: Completing registration
- **WHEN** a pending Google profile supplies an active invitation code
- **THEN** the system creates the account and returns an access token

#### Scenario: Expired registration state
- **WHEN** the pending Google state is missing or older than 10 minutes
- **THEN** the system responds 400 and asks the user to sign in with Google again

#### Scenario: Invalid invitation code
- **WHEN** the supplied code is unknown or inactive
- **THEN** the system responds 403 and creates no account

#### Scenario: Already registered Google account
- **WHEN** the Google subject already has an account
- **THEN** the system responds 400 and creates no duplicate

### Requirement: Invitation code lifecycle
The system SHALL generate 12-character uppercase invitation codes that start active. Administrators SHALL be able to deactivate, reactivate, and permanently delete codes. Registration SHALL NOT consume a code; it remains usable until deactivated.

#### Scenario: Reusing a code
- **WHEN** two different Google users register with the same still-active code
- **THEN** both registrations succeed

#### Scenario: Deactivated code
- **WHEN** a registration uses a deactivated code
- **THEN** the system rejects it with 403

### Requirement: Administrator user management
The system SHALL let administrators list users, create local users, change roles, edit names, reset local passwords, activate or deactivate accounts, and delete accounts. An administrator SHALL NOT delete their own account, and SHALL NOT set a password on a Google account.

#### Scenario: Deleting own account
- **WHEN** an administrator attempts to delete the account they are signed in with
- **THEN** the system responds 400 and keeps the account

#### Scenario: Resetting a Google account password
- **WHEN** an administrator resets the password of a Google-linked account
- **THEN** the system responds 409 and keeps the account unchanged

#### Scenario: Deactivating an account
- **WHEN** an administrator deactivates an account
- **THEN** that account can no longer sign in or refresh its session

### Requirement: Built-in administrator account
The system SHALL ensure a `localadmin` account with password `password88` and `ROLE_ADMIN` exists on every startup, restoring the role if it was removed but leaving an existing password unchanged.

#### Scenario: First startup
- **WHEN** the auth service starts and `localadmin` does not exist
- **THEN** it creates the account with the documented password and `ROLE_ADMIN`

#### Scenario: Role removed manually
- **WHEN** the auth service starts and `localadmin` exists without `ROLE_ADMIN`
- **THEN** it restores `ROLE_ADMIN` without changing the password

### Requirement: Public branding settings
The system SHALL serve the application name, login welcome text, and logo URL without authentication. The application name SHALL default to `Telegram Quiz`, and a configured logo SHALL be exposed as a public file URL.

#### Scenario: No branding configured
- **WHEN** no branding settings are stored
- **THEN** the public endpoint returns the default name and an empty welcome text and logo

#### Scenario: Custom logo
- **WHEN** an administrator sets a logo image
- **THEN** the public endpoint returns a URL that serves that image
