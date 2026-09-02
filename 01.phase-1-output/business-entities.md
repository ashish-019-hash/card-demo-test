# Business entity catalog

## Scope and evidence limits

The five programs establish one business-recognizable entity, **User**. `CSUSR01Y`, the source that would define its physical record, is absent; therefore every attribute’s exact type, size, padding, encoding, and physical record position is **unknown**. The catalog intentionally excludes COMMAREA fields, map fields, response codes, cursor positions, and work storage.

### ENTITY-001: User

**Entity Type**: Master  
**Description**: A person/application principal whose identifier and credentials are used to sign on and whose name and type are maintained through administrative workflows.  
**Source**: Record attribute use is visible in `COSGN00C.cbl:211-227`, `COUSR01C.cbl:153-160`, `COUSR02C.cbl:166-170,215-234`, and `COUSR03C.cbl:164-168`; the absent physical definition is referenced by `COPY CSUSR01Y` at `COSGN00C.cbl:55` (also copied by the other programs).

**Business Attributes**:
- **Primary Key**: `SEC-USR-ID`; CICS writes and reads use it as the RIDFLD/key (`COUSR01C.cbl:240-245`, `COUSR02C.cbl:322-328`). Exact type/length are unknown; the sign-on work lookup key is visibly `PIC X(08)` (`COSGN00C.cbl:45-46`).
- **Core Attributes**: `SEC-USR-FNAME`, `SEC-USR-LNAME`, and `SEC-USR-PWD`, populated on create and compared/changed on update (`COUSR01C.cbl:153-158`, `COUSR02C.cbl:219-230`).
- **Classification**: `SEC-USR-TYPE`, carried into the session and used for admin versus non-admin routing (`COSGN00C.cbl:223-240`; `COUSR01C.cbl:158`). Its permissible vocabulary/code values are unknown.
- **Foreign Keys / status fields**: None are confirmed by the available sources.

**Data Structure**: The actual `SEC-USER-DATA` structure is unknown because `CSUSR01Y` is absent. The following is an attribute-use catalog, **not** a reconstructed copybook.

| Field | Data type | Description | Key | Evidence / status |
|---|---|---|---|---|
| `SEC-USR-ID` | Unknown; observed sign-on work key is `PIC X(08)` | User identifier used for lookup and write key. | PK | `COUSR01C.cbl:153-154,240-245`; confirmed usage, unknown physical PIC |
| `SEC-USR-FNAME` | Unknown | User first name. | — | `COUSR01C.cbl:155`; `COUSR02C.cbl:167,219-221` |
| `SEC-USR-LNAME` | Unknown | User last name. | — | `COUSR01C.cbl:156`; `COUSR02C.cbl:168,223-225` |
| `SEC-USR-PWD` | Unknown | Credential value compared at sign-on and administered by create/update. | — | `COSGN00C.cbl:223`; `COUSR01C.cbl:157`; `COUSR02C.cbl:169,227-229` |
| `SEC-USR-TYPE` | Unknown | User classification used to determine the post-sign-on route. | — | `COSGN00C.cbl:227,230-240`; `COUSR01C.cbl:158` |

**Relationships**:
- **Parent**: None confirmed.
- **Children**: None confirmed.
- **Associates**: A User has one active session context after successful sign-on; COMMAREA representation and cardinality are unknown because `COCOM01Y` is absent (`COSGN00C.cbl:224-240`).

**Usage Context**:
- **Programs**: Sign-on (`COSGN00C`), add (`COUSR01C`), update (`COUSR02C`), delete (`COUSR03C`).
- **Business Functions**: Authenticate, classify access, create, read for maintenance, update, and remove user records.

## Relationship diagram

```text
[User] --(successful authentication creates a session context; representation unknown)--> [Active session]
```

No other entity or relationship is source-confirmed. In particular, absent map fields must not be treated as a schema, and no relationship/cardinality can be inferred from a single keyed user store.
