# Spec Delta

## Purpose

Defines how client traffic reaches the backend services: how the gateway routes request paths to the auth and main services, how it limits request volume per client IP on each rate-limited route, and the transparency guarantees it makes about authentication, credentials, and cross-origin headers.

## ADDED Requirements

### Requirement: Path-based routing
The gateway SHALL route `/auth/**`, `/oauth2/**`, and `/login/oauth2/**` requests to the auth service and `/api/**` requests to the main service, forwarding the original path unchanged. Requests to paths outside these prefixes SHALL NOT be routed to any backend.

#### Scenario: Auth request reaches the auth service
- **WHEN** a client calls `/auth/login` on the gateway
- **THEN** the request arrives at the auth service with its path unchanged

#### Scenario: API request reaches the main service
- **WHEN** a client calls `/api/quizzes` on the gateway
- **THEN** the request arrives at the main service with its path unchanged

#### Scenario: Unmatched path
- **WHEN** a client calls a path that matches no configured route prefix
- **THEN** the gateway responds 404 and forwards nothing

#### Scenario: Downstream unreachable
- **WHEN** a routed request's target service cannot be reached
- **THEN** the gateway responds with a 5xx server error

### Requirement: Per-route request rate limiting
The gateway SHALL limit request rates per client IP independently on each rate-limited route: `/auth/**` at a sustained 20 requests per second with a burst capacity of 40, and `/api/**` at a sustained 30 requests per second with a burst capacity of 60, each request consuming one unit. Requests exceeding a route's limit SHALL be rejected with 429. The OAuth2 sign-in routes SHALL NOT be rate-limited.

#### Scenario: Burst within capacity
- **WHEN** a client sends up to 40 `/auth/**` requests as a burst against a full allowance
- **THEN** all of them are forwarded

#### Scenario: Exceeding the limit
- **WHEN** a client exceeds the `/api/**` route's rate limit
- **THEN** the gateway rejects the excess requests with 429 (Too Many Requests)

#### Scenario: OAuth2 routes exempt
- **WHEN** a client calls `/oauth2/**` or `/login/oauth2/**`
- **THEN** the request is forwarded regardless of request volume

### Requirement: Client IP resolution for rate limiting
The gateway SHALL identify the client of a request by the first entry of its `X-Forwarded-For` header when present, and otherwise by the connection's remote address. Distinct client addresses SHALL be rate-limited independently of one another.

#### Scenario: Proxied client
- **WHEN** a request arrives with `X-Forwarded-For: 1.2.3.4, 5.6.7.8`
- **THEN** its rate-limit allowance is the one belonging to `1.2.3.4`

#### Scenario: Direct client
- **WHEN** a request arrives without an `X-Forwarded-For` header
- **THEN** its rate-limit allowance is the one belonging to the connection's remote address

### Requirement: Transparent proxying
The gateway SHALL NOT authenticate or authorize requests; it SHALL forward bearer credentials and cookies unchanged and leave every access decision to the downstream services.

#### Scenario: Unauthenticated request
- **WHEN** a request without credentials reaches a protected downstream path
- **THEN** the gateway forwards it and the downstream service's rejection reaches the client unchanged

#### Scenario: Authenticated request
- **WHEN** a request carries a bearer token
- **THEN** the gateway forwards the token unchanged without interpreting it

### Requirement: Origin header removal
The gateway SHALL remove the `Origin` request header on every route before forwarding, so downstream services never observe a cross-origin marker.

#### Scenario: Cross-origin request
- **WHEN** a browser sends a request carrying an `Origin` header through the gateway
- **THEN** the forwarded request contains no `Origin` header

### Requirement: Cross-origin access policy
The gateway SHALL answer cross-origin requests for any origin pattern with credentials allowed, permitting any request headers and the methods GET, POST, PUT, PATCH, DELETE, and OPTIONS.

#### Scenario: Preflight request
- **WHEN** a browser preflights a cross-origin `POST` through the gateway
- **THEN** the gateway allows the requesting origin with credentials permitted
