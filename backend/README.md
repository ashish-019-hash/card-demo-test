# CardDemo bounded backend

A Java 21 / Spring Boot modular-monolith backend for the **bounded identity and user-administration slice only**. It does not implement the regular-user menu, cards, accounts, transactions, unknown admin menu options, COBOL/BMS layouts, or record-level compatibility.

The source-backed contract is in `../01.phase-1-output/modern-api-domain-contract.md`. Intentional deviations (case-sensitive hashed passwords, role enforcement, and optimistic locking) are recorded in `../01.phase-1-output/assumptions-deviations.md`.

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 16+ for the `prod` profile

## Run locally

```bash
cd backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Use the explicit `dev` profile for local work. It uses an in-memory H2 database, enables the H2 console at `http://localhost:8080/h2-console`, uses `Secure=false` session and CSRF cookies for HTTP localhost, and enables fixtures. The database is ephemeral. The OpenAPI document is available at `http://localhost:8080/api-docs` and Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

Flyway currently has one migration, `V1__create_users.sql`; it includes the current provisional schema constraints: a 64-character user ID, 128-character names, a 16-character role, and an `ADMIN`/`REGULAR` role check. There is no `V2` migration.

For local development/testing only, `dev` startup seeds deterministic accounts:

| User ID | Password | Role |
| --- | --- | --- |
| `ADMIN001` | `AdminPassword!21` | `ADMIN` |
| `REGULAR1` | `RegularPassword!21` | `REGULAR` |

These are deliberately non-production fixture credentials, not production credentials. The base configuration and `prod` keep the session cookie `HttpOnly`, `Secure`, and `SameSite=Strict`; the browser-readable CSRF cookie is `Secure` and `SameSite=Strict`. `dev` relaxes only the `Secure` flag for local HTTP. Set `SPRING_PROFILES_ACTIVE=prod` to disable seeds and configure PostgreSQL:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL='jdbc:postgresql://localhost:5432/carddemo'
export DATABASE_USERNAME='carddemo'
export DATABASE_PASSWORD='replace-me'
mvn spring-boot:run
```

## API and security

| Endpoint | Access | Notes |
| --- | --- | --- |
| `POST /api/session` | Public | Creates an HttpOnly, SameSite=Strict server session. It is Secure outside `dev`; `dev` sets Secure=false solely for HTTP localhost. User IDs normalize to uppercase; passwords are case-sensitive, BCrypt-hashed, and capped at 72 UTF-8 bytes. |
| `GET /api/session` | Session | Inspects the authenticated user. |
| `DELETE /api/session` | Session + CSRF token | Invalidates the session. |
| `POST /api/users` | `ADMIN` + CSRF token | Create user; required validation order is first name, last name, ID, password, role. |
| `GET /api/users/{userId}` | `ADMIN` | Retrieve user. |
| `PUT /api/users/{userId}` | `ADMIN` + CSRF token | Optimistic-lock update; password is optional and replaces the old password only when supplied. |
| `DELETE /api/users/{userId}?version={version}` | `ADMIN` + CSRF token | Optimistic-lock delete. The caller/UI must provide explicit confirmation. |

State-changing authenticated endpoints require Spring Security's CSRF token, supplied by the browser-readable `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` request header. The CSRF cookie is intentionally not HttpOnly so the client can echo it; it is SameSite=Strict and Secure except in `dev`. Session creation is exempt because it is the initial credential exchange.

The API uses this stable error body:

```json
{"code":"VALIDATION_REQUIRED","message":"First name is required.","field":"firstName","traceId":"request-correlation-id"}
```

Possible codes are `VALIDATION_REQUIRED`, `INVALID_CREDENTIALS`, `DUPLICATE_USER`, `USER_NOT_FOUND`, `NO_CHANGES`, `VERSION_CONFLICT`, `UNAUTHORIZED`, `FORBIDDEN`, and `INTERNAL_ERROR`.

## Test

```bash
cd backend
mvn test
```

Tests cover normalized lookup; name, role, ID, and BCrypt-safe password validation; malformed request handling; duplicate/not-found/conflict outcomes; real JPA optimistic locking; CSRF-protected session CRUD/logout; role/deletion session invalidation; trace IDs; OpenAPI cookie security metadata; and the absence of password fields from API representations. A Testcontainers PostgreSQL/Flyway baseline migration test runs when the Docker Java client can connect to the daemon; otherwise it is skipped.
