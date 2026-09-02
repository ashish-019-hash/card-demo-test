# Bounded modern API and domain contract

## Status and boundary

This is a **provisional modern contract**, not a reconstructed legacy wire/schema contract. It is intentionally bounded to authentication and user administration. It excludes cards, accounts, transactions, regular-user functionality beyond an unmigrated boundary, exact admin-menu option labels/targets, BMS geometry, CICS response codes, and record-byte compatibility.

## Domain

### `User` aggregate (provisional)

```text
User {
  userId: string,             // normalized uppercase; legacy work lookup field is PIC X(08)
  firstName: string,
  lastName: string,
  passwordHash: string,       // modern security deviation; never returned
  role: string,               // provisional ADMIN/REGULAR vocabulary pending COCOM01Y/CSUSR01Y remap
  version: integer            // optimistic-lock version; modern addition
}
```

| Domain decision | Basis / status |
|---|---|
| User ID is unique and normalized to uppercase. | Legacy sign-on uppercases user ID (`COSGN00C.cbl:132-134`) and add uses ID as keyed write with duplicate failure (`COUSR01C.cbl:240-266`). **Confirmed behavior; physical length unknown.** |
| First name, last name, password, and role are required for legacy add/update. | `COUSR01C.cbl:117-160`; `COUSR02C.cbl:179-213`. **Confirmed requirement; unknown lengths/vocabulary.** |
| Password stored as hash; outbound User response omits it. | **Intentional security deviation.** Legacy compares/stores a clear value and displays it on update (`COSGN00C.cbl:223`; `COUSR02C.cbl:166-170`). |
| Version controls concurrent modification. | **Inferred modern replacement** for CICS read-with-UPDATE/rewrite (`COUSR02C.cbl:322-366`). |
| All user-management resources require ADMIN. | **Inferred authorization hardening.** Legacy routes admins to menu but CRUD programs show no direct role check (`COSGN00C.cbl:230-234`; no equivalent role check in CRUD sources). |

### Normalization and validation contract

- Normalize user ID to uppercase before identity lookup/create/update/delete. Preserve leading/trailing behavior only after BMS record semantics are known; the legacy source explicitly uppercases sign-on ID but does not explicitly uppercase CRUD IDs (`COSGN00C.cbl:132-134`; `COUSR01C.cbl:153-154`). Applying normalization consistently in modern CRUD is an **inferred contract decision**.
- Preserve first-failure field order where the legacy establishes it: sign-on ID/password; create first/last/ID/password/type; update ID/first/last/password/type (`COSGN00C.cbl:117-130`; `COUSR01C.cbl:117-151`; `COUSR02C.cbl:179-213`).
- Do not freeze max lengths or password complexity until missing copybooks/maps are recovered. `ADMIN` and `REGULAR` are approved provisional modern labels; map persisted values to recovered legacy role codes before any legacy import, schema lock, or fidelity claim.

## Browser session and authorization

| Capability | Endpoint | Request / response | Authorization | Source mapping |
|---|---|---|---|---|
| Create session | `POST /api/session` | `{userId,password}` → `200 {user:{userId,firstName,lastName,role}}` plus HttpOnly Secure SameSite session cookie | Public | Sign-on `COSGN00C.cbl:117-256` |
| Inspect session | `GET /api/session` | `200 {user:...}` or `401` | Authenticated | Modern addition supporting browser session |
| End session | `DELETE /api/session` | `204` | Authenticated | Modern counterpart to leaving terminal flow; legacy explicit browser-like logout absent |
| Create user | `POST /api/users` | `{userId,firstName,lastName,password,role}` → `201` User representation | ADMIN | Add `COUSR01C.cbl:117-160,240-274` |
| Get user | `GET /api/users/{userId}` | `200` User representation | ADMIN | Update/delete lookup `COUSR02C.cbl:145-172`; `COUSR03C.cbl:144-168` |
| Update user | `PUT /api/users/{userId}` | `{firstName,lastName,password?,role,version}` → `200` User representation | ADMIN | Update `COUSR02C.cbl:179-245,360-390` |
| Delete user | `DELETE /api/users/{userId}?version={version}` | `204` | ADMIN; UI requires explicit confirmation | Delete `COUSR03C.cbl:176-192,303-336` |

User representations exclude `passwordHash` and password. An optional password on update means “replace password”; omitted password means no password change. This deliberately replaces the legacy copy-out/copy-back of stored password.

## Error model

```json
{
  "code": "VALIDATION_REQUIRED | INVALID_CREDENTIALS | DUPLICATE_USER | USER_NOT_FOUND | NO_CHANGES | VERSION_CONFLICT | UNAUTHORIZED | FORBIDDEN | INTERNAL_ERROR",
  "message": "human-readable message meaning",
  "field": "optional field name",
  "traceId": "optional support correlation"
}
```

| Condition | HTTP response | Legacy source / meaning |
|---|---|---|
| Required field / invalid menu selection | `400` `VALIDATION_REQUIRED` | `COSGN00C.cbl:117-127`; `COADM01C.cbl:127-134`; `COUSR01C.cbl:117-147`; `COUSR02C.cbl:179-209`; `COUSR03C.cbl:144-150` |
| Wrong password | `401` `INVALID_CREDENTIALS` | `COSGN00C.cbl:241-246` |
| Sign-on user not found | `401` `INVALID_CREDENTIALS` | Legacy distinguishes it (`COSGN00C.cbl:247-251`); **modern security deviation** may deliberately use a generic authentication message. |
| Create duplicate | `409` `DUPLICATE_USER` | `COUSR01C.cbl:260-266` |
| Lookup/update/delete unknown user | `404` `USER_NOT_FOUND` | `COUSR02C.cbl:340-345,377-382`; `COUSR03C.cbl:287-292,323-328` |
| Update has no changes | `409` `NO_CHANGES` | `COUSR02C.cbl:236-243` |
| Version stale | `409` `VERSION_CONFLICT` | Modern inferred replacement for read-with-update lock. |
| Missing/invalid session | `401` `UNAUTHORIZED` | Modern HTTP mapping. |
| Authenticated non-admin CRUD request | `403` `FORBIDDEN` | Modern inferred access-control hardening. |
| Technical persistence failure | `500` `INTERNAL_ERROR` | `COSGN00C.cbl:252-256`; `COUSR01C.cbl:267-273`; `COUSR02C.cbl:383-389`; `COUSR03C.cbl:329-335` |

## Workflow translation

| Legacy interaction | Modern intent | Preservation / change |
|---|---|---|
| Enter on sign-on | Submit sign-in form | Preserve ordered required checks and authentication outcomes. |
| PF3 on add/delete | Back/cancel navigation | Preserve route intent; no implicit mutation. |
| PF3 on update | Back navigation | **Approved intentional deviation:** legacy attempts update before leaving (`COUSR02C.cbl:111-119`); modern workflow uses separate explicit Save changes and Back controls, and Back never persists edits. |
| PF4 | Clear form | Preserve as named Clear control. |
| PF5 update/delete | Explicit Save / Delete confirmation | Preserve explicit mutation; delete must be confirmed. |
| PF12 | Return to administration | Preserve as named navigation. |
| CICS read `UPDATE` then `REWRITE`/`DELETE` | Transactional service mutation with version | Avoid holding a database lock over browser think time; return conflict when version changes. |

## Contract examples

**Create user**

```http
POST /api/users
Content-Type: application/json

{"userId":"newuser","firstName":"New","lastName":"User","password":"one-time-secret","role":"<confirmed-role-code-required>"}
```

`201 Created` returns user ID/name/role/version only. Exact role code must not be invented.

**No-change update**

```http
PUT /api/users/NEWUSER
Content-Type: application/json

{"firstName":"New","lastName":"User","role":"<confirmed-role-code-required>","version":3}
```

`409` with `NO_CHANGES` maps to legacy “Please modify to update ...” (`COUSR02C.cbl:236-243`).

**Authorization test expectations**

- An unauthenticated CRUD request receives `401`.
- A regular authenticated user receives `403` for all CRUD endpoints.
- An administrator can create, retrieve, change, and delete users subject to the listed validation/conflict rules.
- Every characterized legacy branch has an API outcome mapping above, except terminal-only routing/formatting and unknown configuration branches.
