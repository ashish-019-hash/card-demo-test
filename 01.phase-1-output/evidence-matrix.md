# Evidence matrix and recovery ledger

**Baseline:** `598d70c0564d51dfd546abb40beb2f63629e72c1`.  
**Confidence meanings:** Confirmed = direct source; Inferred = migration design interpretation; Unknown = absent evidence. No row labeled Confirmed depends on an absent copybook/BMS file.

| ID | Topic / claim | Confidence | Source / evidence | Recovery needed or impact |
|---|---|---|---|---|
| E-01 | This repository has exactly five tracked COBOL programs. | Confirmed | repository inventory at baseline | None for bounded static analysis. |
| E-02 | Sign-on map/mapset are `COSGN0A`/`COSGN00`; admin is `COADM1A`/`COADM01`; add/update/delete use `COUSR1A`/`COUSR01`, `COUSR2A`/`COUSR02`, `COUSR3A`/`COUSR03`. | Confirmed | `COSGN00C.cbl:110-115`; `COADM01C.cbl:179-197`; `COUSR01C.cbl:190-209`; `COUSR02C.cbl:272-291`; `COUSR03C.cbl:219-238` | BMS sources for visual/field fidelity. |
| E-03 | Map labels, positions, field lengths, attributes, masking, and tab sequence. | Unknown | BMS/symbolic map COPY targets are absent; e.g. `COPY COSGN00` at `COSGN00C.cbl:50`. | Recover five BMS sources and generated map copybooks. |
| E-04 | `USRSEC` stores user records accessed by keyed CICS operations. | Confirmed | read `COSGN00C.cbl:211-219`; write `COUSR01C.cbl:240-248`; rewrite `COUSR02C.cbl:360-366`; delete `COUSR03C.cbl:307-311` | None for statement; layout still unknown. |
| E-05 | User attributes used are ID, first name, last name, password, and type. | Confirmed | `COUSR01C.cbl:153-160`; `COUSR02C.cbl:166-170` | `CSUSR01Y` to establish PICs/lengths/order. |
| E-06 | Physical record length/layout/key encoding/cluster organization. | Unknown | all use `LENGTH OF SEC-USER-DATA`; `CSUSR01Y` absent, e.g. `COUSR02C.cbl:322-328`. | Recover `CSUSR01Y`, USRSEC catalog/definition, representative scrubbed data. |
| E-07 | Sign-on uses an eight-character alphanumeric work user ID and password. | Confirmed | `COSGN00C.cbl:45-46` | This does not prove map/record field sizes. |
| E-08 | Sign-on uppercases user ID and password. | Confirmed | `COSGN00C.cbl:132-136` | Modern case-sensitive password policy is a deviation. |
| E-09 | Admin condition routes to `COADM01C`; other successful login routes to `COMEN01C`. | Confirmed | `COSGN00C.cbl:221-240` | Recover `COCOM01Y` role values and `COMEN01C` behavior. |
| E-10 | The literal administrator role value/code. | Unknown | condition name only, defined in missing `COCOM01Y` (`COSGN00C.cbl:230`). | Recover `COCOM01Y`. |
| E-11 | Admin option number/count/name/target configuration. | Unknown | supplied by missing `COADM02Y`; referenced `COADM01C.cbl:50-51,127-155,228-263`. | Recover `COADM02Y` and target programs. |
| E-12 | Valid configured non-`DUMMY` option routes; `DUMMY` shows coming soon. | Confirmed | `COADM01C.cbl:137-155` | Exact configured instances unknown. |
| E-13 | Add validates first name, last name, ID, password, type in order and rejects duplicates. | Confirmed | `COUSR01C.cbl:117-160,240-266` | Field size/type vocabulary unknown. |
| E-14 | Update lookups/populates user fields, requires a changed value, and rewrites. | Confirmed | `COUSR02C.cbl:145-245,322-390` | Legacy UI exposes stored password; security deviation tracked. |
| E-15 | Delete displays non-password attributes, prompts PF5, rereads then deletes. | Confirmed | `COUSR03C.cbl:156-192,269-336` | Exact user-facing layout unknown. |
| E-16 | CRUD screens themselves enforce administrator authorization. | Unknown / not evidenced | No role check appears in `COUSR01C`, `COUSR02C`, or `COUSR03C`; admin menu is an upstream route. | Modern endpoint authorization is an inferred hardening decision. |
| E-17 | Legacy source can be run/reset as an oracle. | Unknown | no resource definitions, executable, data, or tests are in repository. | Recover CICS definitions/build/runbook and resettable data. |
| E-18 | Web REST/session-cookie model and optimistic version. | Inferred | modernization design; legacy uses CICS COMMAREA/read-with-update (`COUSR02C.cbl:322-366`). | Explicit contract/deviation, not legacy fidelity claim. |

## Static recovery check

| Check | Result |
|---|---|
| Project COPY targets resolve | **Blocked:** `COCOM01Y`, `COADM02Y`, five symbolic maps, `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSUSR01Y` absent. |
| Standard `DFHAID`/`DFHBMSCA` distinction | These are standard CICS includes; no local copies are expected from this repository alone. |
| XCTL target inventory | **Incomplete:** direct targets `COADM01C`, `COMEN01C`, `COSGN00C`; dynamic admin targets depend on `COADM02Y`. |
| USRSEC record layout/key/encoding | **Blocked:** `CSUSR01Y` and dataset definitions absent. |
| Resettable runnable legacy scenario | **Blocked:** runtime resource definitions, executable, data, and runbook absent. |

Until these artifacts are supplied, fidelity is limited to static, workflow-level, source-derived behavior for the five-program identity/admin slice.
