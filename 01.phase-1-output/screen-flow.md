# Application screen flow documentation

## Summary

- **Total screens analyzed:** five named map/program pairs. The term “screen” means an observed CICS map invocation; BMS sources are absent, so labels, coordinates, field length, attributes, masking, and tab order are unknown.
- **Application purpose:** bounded identity and user-administration slice: sign on, choose an administration option, add a user, look up/update a user, and look up/delete a user.
- **Main workflows:** sign-on (`COSGN00C`); admin-menu selection (`COADM01C`); user add (`COUSR01C`); update (`COUSR02C`); delete (`COUSR03C`).

## Screen inventory

| Screen ID | BMS Map | Program | Transaction | Purpose | Evidence |
|---|---|---|---|---|---|
| SCREEN-001 | `COSGN0A` / `COSGN00` | `COSGN00C` | `CC00` | Sign-on. | `COSGN00C.cbl:36-37,110-115,151-157` |
| SCREEN-002 | `COADM1A` / `COADM01` | `COADM01C` | `CA00` | Administration menu. | `COADM01C.cbl:36-37,179-197` |
| SCREEN-003 | `COUSR1A` / `COUSR01` | `COUSR01C` | `CU01` | Add user. | `COUSR01C.cbl:36-37,190-209` |
| SCREEN-004 | `COUSR2A` / `COUSR02` | `COUSR02C` | `CU02` | Look up and update user. | `COUSR02C.cbl:36-37,272-291` |
| SCREEN-005 | `COUSR3A` / `COUSR03` | `COUSR03C` | `CU03` | Look up and delete user. | `COUSR03C.cbl:36-37,219-238` |

## Detailed screen analysis

### SCREEN-001: Sign-on

**Screen Purpose**: Lets a user provide credentials and enters either administrator or regular-user flow after successful verification.

**User Interaction Flow**:
1. On a new interaction, the system presents the sign-on map with user-ID focus (`COSGN00C.cbl:80-83`).
2. The user presses Enter. A blank user ID is rejected first; then a blank password is rejected (`COSGN00C.cbl:117-130`).
3. The program uppercases both inputs, reads `USRSEC`, and compares the returned password (`COSGN00C.cbl:132-140,211-246`).
4. A matching administrator transfers to the administration menu; a matching non-administrator transfers to absent `COMEN01C` (`COSGN00C.cbl:221-240`). Wrong password, no record, or technical lookup failure redisplay this screen (`COSGN00C.cbl:241-256`).

**Screen Fields**:

| Field name used by program | Type | Data source | Description |
|---|---|---|---|
| `USERIDI` | Input | User entry | Required user identifier; visible lookup work field is eight characters, but map size is unknown. |
| `PASSWDI` | Input | User entry | Required credential; screen masking/length unknown. |
| `ERRMSGO` | Output | Program message | Error/status text. |
| Header/date/time/system fields | Output | Shared copybooks/CICS | Names are visible in code but semantics/layout come from missing copybooks/maps. |

**Navigation Conditions**:
- Sign-on → valid admin credential → administration menu (`COADM01C`).
- Sign-on → valid non-admin credential → missing regular menu (`COMEN01C`).
- Sign-on → blank/wrong/missing/technical failure → sign-on again.
- Sign-on → PF3 → plain “thank you” text/end; exact termination semantics are not established (`COSGN00C.cbl:85-94,160-172`).
- Sign-on → other key → sign-on with invalid-key message.

### SCREEN-002: Administration menu

**Screen Purpose**: Displays configured administration options and transfers a selected configured option.

**User Interaction Flow**:
1. A successful administrator handoff initializes and presents the menu (`COADM01C.cbl:86-103`).
2. The user enters an option and presses Enter. The system trims/right-aligns its value, verifies it is numeric, nonzero, and in the configured range (`COADM01C.cbl:117-134`).
3. A configured non-`DUMMY` option transfers to its configured program. A `DUMMY` option stays on this screen with a coming-soon message (`COADM01C.cbl:137-155`).

**Screen Fields**:

| Field name used by program | Type | Data source | Description |
|---|---|---|---|
| `OPTIONI` | Input | User entry | Menu option; actual length unknown. |
| `OPTIONO` | Output | Normalized entered option | Displayed selected option. |
| `OPTN001O`–`OPTN010O` | Output | `COADM02Y` configuration | Up to ten display slots are populated; actual option count/names unknown. |
| `ERRMSGO` | Output | Program message | Invalid or coming-soon message. |

**Navigation Conditions**:
- Administration menu → valid non-`DUMMY` configuration → configured target (unknown until `COADM02Y` is recovered).
- Administration menu → valid `DUMMY` configuration → administration menu with coming-soon message.
- Administration menu → invalid selection/other key → administration menu.
- Administration menu → PF3 → sign-on (`COSGN00C`) (`COADM01C.cbl:93-103,137-167`).

### SCREEN-003: Add user

**Screen Purpose**: Lets an administrator create a User record.

**User Interaction Flow**:
1. The system starts with first-name focus (`COUSR01C.cbl:83-87`).
2. The user supplies first name, last name, user ID, password, and user type and presses Enter.
3. The system reports the first missing field in that order; once complete, it attempts creation (`COUSR01C.cbl:117-160`).
4. A success clears fields and confirms the add; a duplicate ID or technical failure stays on the screen (`COUSR01C.cbl:240-274`).

**Screen Fields**: `FNAMEI`, `LNAMEI`, `USERIDI`, `PASSWDI`, and `USRTYPEI` are inputs; `ERRMSGO` is output. Their exact BMS definitions are unknown. Inputs are moved to `SEC-USR-FNAME`, `SEC-USR-LNAME`, `SEC-USR-ID`, `SEC-USR-PWD`, and `SEC-USR-TYPE` (`COUSR01C.cbl:153-158`).

**Navigation Conditions**:
- Add user → successful create / duplicate / technical failure / validation failure → add user.
- Add user → PF4 → cleared add user.
- Add user → PF3 → administration menu.
- Add user → other key → add user with invalid-key message (`COUSR01C.cbl:90-103,240-295`).

### SCREEN-004: Update user

**Screen Purpose**: Lets an administrator find a user, see its stored details, and save a changed record.

**User Interaction Flow**:
1. The user enters a user ID and presses Enter; a nonblank ID is looked up (`COUSR02C.cbl:145-164,322-353`).
2. On success the system displays first name, last name, password, and type, asking for PF5 to save (`COUSR02C.cbl:166-171,333-339`).
3. The user modifies one or more values and presses PF5. Required fields are checked in source order; no changed values are rejected; otherwise the record is rewritten (`COUSR02C.cbl:179-245,360-390`).

**Screen Fields**: Input/then-display fields are `USRIDINI`, `FNAMEI`, `LNAMEI`, `PASSWDI`, `USRTYPEI`; `ERRMSGO` is output. The source explicitly copies the stored password into `PASSWDI` (`COUSR02C.cbl:166-170`), but no layout/masking is available.

**Navigation Conditions**:
- Update → successful/missing/technical lookup → update screen.
- Update → PF5 changed valid record → update screen with success.
- Update → PF5 no change or validation failure → update screen.
- Update → PF4 → clear update screen.
- Update → PF12 → administration menu.
- Update → PF3 → **attempt update first**, then return to source program or administration menu (`COUSR02C.cbl:108-126`). This is an observable behavior requiring stakeholder confirmation before normalizing it.

### SCREEN-005: Delete user

**Screen Purpose**: Lets an administrator look up a user, review non-password details, and confirm deletion with PF5.

**User Interaction Flow**:
1. The user enters a user ID and presses Enter; the system requires it and looks it up (`COUSR03C.cbl:144-162,269-300`).
2. On success it displays first name, last name, and type and asks for PF5 to delete (`COUSR03C.cbl:164-168,280-286`).
3. The user presses PF5. The system requires/re-reads the ID, then deletes the record and confirms success; errors keep the screen displayed (`COUSR03C.cbl:176-192,303-336`).

**Screen Fields**: `USRIDINI` is input; `FNAMEI`, `LNAMEI`, and `USRTYPEI` are populated after lookup; password is not copied to this screen (`COUSR03C.cbl:156-168`). Exact BMS properties are unknown.

**Navigation Conditions**:
- Delete → successful/missing/technical lookup → delete screen.
- Delete → PF5 successful/not-found/technical delete → delete screen.
- Delete → PF4 → cleared delete screen.
- Delete → PF3 → originating program or administration menu; PF12 → administration menu (`COUSR03C.cbl:108-130,339-356`).

## Complete flow diagram

```text
[Sign-on]
  ├── admin credentials ───────────> [Administration menu]
  │                                      ├── configured target ──> [Unknown program]
  │                                      ├── DUMMY ─────────────> [Administration menu]
  │                                      └── PF3 ───────────────> [Sign-on]
  └── non-admin credentials ───────> [COMEN01C — missing/out of scope]

[Add user] PF3 ────────────────────> [Administration menu]
[Update user] PF3/PF12 ────────────> [Prior program or Administration menu]
[Delete user] PF3/PF12 ────────────> [Prior program or Administration menu]
```
