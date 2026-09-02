# Codebase orientation / wiki

## Repository snapshot

| Item | Finding | Evidence | Status |
|---|---|---|---|
| Baseline | Five COBOL programs are tracked at `598d70c0564d51dfd546abb40beb2f63629e72c1`. | repository inventory | Confirmed |
| Runtime style | Interactive CICS programs retain/re-enter with a COMMAREA. | `COSGN00C.cbl:64-102`, `COADM01C.cbl:66-110`, `COUSR01C.cbl:62-110`, `COUSR02C.cbl:73-138`, `COUSR03C.cbl:73-137` | Confirmed |
| Persistent store referenced | `USRSEC` is read, written, rewritten, and deleted. | `COSGN00C.cbl:211-219`, `COUSR01C.cbl:240-248`, `COUSR02C.cbl:322-331,360-366`, `COUSR03C.cbl:269-278,307-311` | Confirmed |
| Record layout and storage details | `CSUSR01Y` is copied but absent. Its fields are used, but field PICs, record length, encoding, cluster/file organization and key definition are absent. | copies: `COSGN00C.cbl:48-58`; use: `COSGN00C.cbl:211-227` | Unknown |
| Screens | Source names five mapsets/maps, but BMS source and symbolic map copybooks are absent. | `COSGN00C.cbl:110-115`; `COADM01C.cbl:179-197`; `COUSR01C.cbl:190-209`; `COUSR02C.cbl:272-291`; `COUSR03C.cbl:219-238` | Confirmed names / unknown layouts |

## Program inventory

| Program / transaction ID in source | Observed responsibility | Map | Direct routes | Evidence |
|---|---|---|---|---|
| `COSGN00C` / `CC00` | Sign-on: required credentials, uppercase lookup/comparison, role-based handoff. | `COSGN0A` / `COSGN00` | Admin → `COADM01C`; otherwise → `COMEN01C`. | `COSGN00C.cbl:36-58,110-140,211-256` |
| `COADM01C` / `CA00` | Administrator menu: parses an option and routes using an absent configuration table. | `COADM1A` / `COADM01` | Configured non-`DUMMY` target; PF3 → sign-on. | `COADM01C.cbl:36-61,82-103,117-155` |
| `COUSR01C` / `CU01` | Add user; validates five fields and writes a `USRSEC` record. | `COUSR1A` / `COUSR01` | PF3 → `COADM01C`. | `COUSR01C.cbl:36-57,78-103,117-160,240-274` |
| `COUSR02C` / `CU02` | Look up and update a user; detects no changes; rewrite after a read-with-update. | `COUSR2A` / `COUSR02` | PF3 → originating program/admin; PF12 → admin. | `COUSR02C.cbl:36-68,90-131,145-245,322-390` |
| `COUSR03C` / `CU03` | Look up and, on PF5, delete a user. | `COUSR3A` / `COUSR03` | PF3 → originating program/admin; PF12 → admin. | `COUSR03C.cbl:36-68,90-130,144-192,269-336` |

## Observed component flow

```text
COSGN00C sign-on
  ├─ valid credential and CDEMO-USRTYP-ADMIN → COADM01C
  └─ valid credential and not-admin          → COMEN01C (missing; boundary)

COADM01C admin menu
  ├─ valid, configured, non-DUMMY option → configured target (COADM02Y missing)
  ├─ valid DUMMY option                  → "coming soon" on the menu
  └─ PF3                                 → COSGN00C

COUSR01C add ─PF3→ COADM01C
COUSR02C update ─PF3/PF12→ prior screen or COADM01C
COUSR03C delete ─PF3/PF12→ prior screen or COADM01C
```

The specific menu option-to-program mappings, including whether the three CRUD programs are configured options, cannot be established because `COADM02Y` is unavailable (`COADM01C.cbl:50-51,127-155,228-263`).

## Data flow (only what source proves)

1. Sign-on receives `USERIDI` and `PASSWDI`, converts both to upper case, reads `USRSEC` by an eight-character work key, compares the returned password, and places the returned user type in COMMAREA before routing (`COSGN00C.cbl:45-46,117-140,211-240`).
2. Add moves five screen inputs into the missing `SEC-USER-*` record and writes the record keyed by `SEC-USR-ID` (`COUSR01C.cbl:153-160,240-248`).
3. Update looks up by entered ID, displays first/last name, password, and type; on a changed value it rewrites the returned record (`COUSR02C.cbl:157-172,215-243,322-390`).
4. Delete looks up by entered ID, displays first/last name and type (not password), then PF5 rereads and deletes the record (`COUSR03C.cbl:156-192,269-336`).

## Unresolved dependencies / recovery checklist

| Missing artifact | Why it is required | Source evidence |
|---|---|---|
| `COCOM01Y` | COMMAREA fields/role condition names and `CDEMO-PGM-REENTER` semantics. | copied by all programs, e.g. `COSGN00C.cbl:48`; used `COSGN00C.cbl:224-240` |
| `COADM02Y` | Admin option count, labels, numeric table, and routes. | `COADM01C.cbl:51,127-155,228-263` |
| `COSGN00`, `COADM01`, `COUSR01`, `COUSR02`, `COUSR03` symbolic maps and their BMS sources | Field definitions, field lengths, labels, geometry, attributes/masking/tab order. | map copy/use citations in program inventory |
| `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y` | Header/title/date/message definitions. | copied by all, e.g. `COUSR01C.cbl:50-53` |
| `CSUSR01Y` and USRSEC data definition/catalog | Actual record PICs, record length, key/encoding, persistence compatibility. | all persistence calls use `SEC-USER-DATA`; e.g. `COUSR02C.cbl:322-327` |
| `COMEN01C` | Regular-user destination and behavior. | `COSGN00C.cbl:236-239` |
| Configured admin target programs | Dynamic navigation behavior outside these five programs. | `COADM01C.cbl:138-145` |
| CICS resource definitions, build inputs, executable and resettable test data | Resolve dependencies and validate source-derived behavior against an oracle. | source requires CICS maps/files/transactions throughout |

`DFHAID` and `DFHBMSCA` are standard CICS includes; all other missing COPY targets above are project artifacts. No copybook or BMS source is present in this repository snapshot.
