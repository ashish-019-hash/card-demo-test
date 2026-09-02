# CardDemo — bounded user-administration migration

A Spring Boot and React modernization of the source-backed **identity and user-administration slice** of CardDemo. It provides sign-in, administrator user creation, lookup/update, and lookup/delete with browser sessions, CSRF protection, password hashing, and optimistic-lock conflict handling.

This repository is **not a full CardDemo replacement**. The original baseline contains only five COBOL/CICS programs and does not include the BMS maps, project copybooks, USRSEC data definition, runnable legacy environment, regular-user menu, or the unknown administrator-menu configuration. See [`01.phase-1-output/`](01.phase-1-output/) for the evidence, characterization, contract, and deviation records.

## Repository layout

| Path | Purpose |
| --- | --- |
| `COSGN00C.cbl`, `COADM01C.cbl`, `COUSR01C.cbl`, `COUSR02C.cbl`, `COUSR03C.cbl` | Five-program COBOL source baseline. |
| `01.phase-1-output/` | Source-derived analysis, assumptions, characterization, contract, and coverage matrix. |
| `backend/` | Java 21 / Spring Boot API with Flyway migrations and H2/PostgreSQL profiles. |
| `frontend/` | React, TypeScript, and Vite browser client. |

## Prerequisites

Install the following before running locally:

- Java **21**
- Maven **3.9+**
- Node.js **20+** and npm
- PostgreSQL **16+** only when using the `prod` backend profile

## Quick start (development)

Use the explicit `dev` backend profile for local development. It uses an in-memory H2 database, enables the H2 console, configures localhost-compatible session/CSRF cookies, and enables deterministic development fixtures. Start the backend in one terminal:

```bash
cd backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Then install and start the frontend in a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The Vite server proxies relative `/api` requests to `http://localhost:8080`, so both processes must be running.

Backend API documentation is available while the backend is running:

- OpenAPI: `http://localhost:8080/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 console (`dev` profile only): `http://localhost:8080/h2-console`

## Development seed accounts

The `dev` in-memory database is recreated whenever the backend restarts. It supplies these **development-only fixture accounts**:

| User ID | Password | Role | Expected result |
| --- | --- | --- | --- |
| `ADMIN001` | `AdminPassword!21` | `ADMIN` | Opens the bounded administrator menu and may manage users. |
| `REGULAR1` | `RegularPassword!21` | `REGULAR` | Authenticates, then stops at the explicit not-migrated boundary. |

User IDs normalize to uppercase. Passwords are case-sensitive and are BCrypt-hashed; the original COBOL behavior uppercased and compared cleartext passwords, which is an intentional security deviation.

## Configuration

### `dev` profile

Activate `dev` explicitly with `SPRING_PROFILES_ACTIVE=dev`. It uses H2, enables `/h2-console`, sets the session and readable CSRF cookies `Secure=false` for HTTP localhost, and enables the fixture accounts above. This relaxed cookie setting is local-development-only; the base and `prod` configuration retain `HttpOnly`, `Secure`, and `SameSite=Strict` session-cookie settings, and a `Secure`, `SameSite=Strict` CSRF cookie.

Flyway currently has one migration: `backend/src/main/resources/db/migration/V1__create_users.sql`. It creates the current provisional constraints in the initial schema: `user_id VARCHAR(64)`, name fields `VARCHAR(128)`, `role VARCHAR(16)`, and an `ADMIN`/`REGULAR` role check. There is no `V2` migration.

To point the frontend development proxy at a different backend, start it with:

```bash
cd frontend
VITE_API_PROXY_TARGET=http://localhost:8080 npm run dev
```

### PostgreSQL production profile

The `prod` profile disables seeds. Supply PostgreSQL connection details through environment variables:

```bash
cd backend
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL='jdbc:postgresql://localhost:5432/carddemo'
export DATABASE_USERNAME='carddemo'
export DATABASE_PASSWORD='replace-me'
mvn spring-boot:run
```

Do not use the fixture credentials or an in-memory H2 database in production.

## API and security behavior

- `POST /api/session` authenticates and creates a browser session.
- `GET /api/session` inspects the current session; `DELETE /api/session` signs out.
- `/api/users` operations require an authenticated `ADMIN` user.
- Authenticated state-changing requests require the readable `XSRF-TOKEN` cookie value in the `X-XSRF-TOKEN` header. Session creation is intentionally exempt. The CSRF cookie is intentionally readable by the browser client; the separate session cookie is `HttpOnly`.
- User API representations never include a password or password hash.
- Updates and deletes require the current `version`, returning a conflict if stale. Delete requires an explicit UI confirmation.

The API error schema and endpoint contract are documented in [`01.phase-1-output/modern-api-domain-contract.md`](01.phase-1-output/modern-api-domain-contract.md).

## Test and build

Run backend tests:

```bash
cd backend
mvn test
```

Run frontend tests and production build:

```bash
cd frontend
npm test
npm run build
```

The backend tests cover session/CSRF behavior, administrator authorization, fixture login, normalized lookup, ordered validation, duplicate/conflict paths, and password omission. The frontend tests cover ordered validation, uppercase sign-in IDs, clearing/focus, no-change feedback, and delete cancellation/confirmation.

## Reset local state

### `dev` H2 development profile

The `dev` database is in memory. Stop and restart `SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run` to recreate it, rerun Flyway, and reseed `ADMIN001` and `REGULAR1`.

### PostgreSQL profile

There is no repository reset script for PostgreSQL. Reset only a disposable local database, then restart the backend so Flyway recreates the schema:

```bash
PGPASSWORD="$DATABASE_PASSWORD" psql -h localhost -p 5432 -U "$DATABASE_USERNAME" -d carddemo \
  -c 'DROP SCHEMA public CASCADE; CREATE SCHEMA public;'
cd backend
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

This command permanently deletes the selected database schema. Confirm `DATABASE_URL` points to a disposable local database before using it. Production data reset/import awaits the missing legacy USRSEC record definition and approved migration strategy.

## Troubleshooting

| Symptom | Check / resolution |
| --- | --- |
| Frontend cannot load or requests fail | Start the backend with `SPRING_PROFILES_ACTIVE=dev` on port 8080, then run the frontend on port 5173. To use another API target, set `VITE_API_PROXY_TARGET` when starting Vite. |
| Sign-in fails with a seed account | Use the exact case-sensitive fixture password and restart the `dev` backend to reset the in-memory database. IDs may be entered in any case. |
| Browser does not retain the local session or CSRF cookie | Use the `dev` profile when serving the local app over `http://localhost`; it sets `Secure=false` for local session and CSRF cookies. Do not carry this setting into production. |
| A create/update/delete request returns `403` | Sign in as `ADMIN001`, obtain a fresh session, and let the client send its `XSRF-TOKEN` cookie/header. `REGULAR1` is intentionally forbidden from user administration. |
| Update reports no changes or conflict | Modify at least one field for `NO_CHANGES`; reload the user before retrying a stale `VERSION_CONFLICT`. |
| `mvn` uses the wrong JDK | Verify `java -version` reports Java 21 and configure `JAVA_HOME` accordingly. |
| `npm install` or build fails | Verify a supported Node.js release, remove `frontend/node_modules`, rerun `npm install`, then rerun the command. |
| PostgreSQL startup fails | Confirm `SPRING_PROFILES_ACTIVE=prod`, `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` are present, the database exists, and the account can create/use the Flyway schema history table. |

## Scope and fidelity limitations

The current migration covers authentication and administrator user maintenance only. It intentionally excludes cards, accounts, transactions, the regular-user journey beyond its boundary, unknown administrator menu options, exact BMS screen layout, CICS response-code rendering, and record-level or byte-for-byte compatibility.

The modern implementation also deliberately uses case-sensitive hashed passwords, server-side browser sessions/CSRF, admin authorization on CRUD, hidden stored passwords, optimistic locking, and accessible named controls. Most significantly, the update screen uses explicit **Save changes** and **Back** controls rather than saving automatically when leaving the screen. The provisional `ADMIN`/`REGULAR` role labels must be remapped when the missing legacy role vocabulary is recovered.

Do not claim full CardDemo, pixel fidelity, record compatibility, or behavioral equivalence until the missing copybooks, BMS maps, USRSEC layout/data, routed programs, and runnable legacy oracle are recovered and compared. The authoritative evidence and exit conditions are in [`01.phase-1-output/assumptions-deviations.md`](01.phase-1-output/assumptions-deviations.md) and [`01.phase-1-output/migration-coverage-matrix.md`](01.phase-1-output/migration-coverage-matrix.md).
