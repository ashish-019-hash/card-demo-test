# CardDemo Validation Rules Catalog

## Scope and method

All COBOL source programs in this repository were scanned: `COADM01C.cbl`, `COSGN00C.cbl`, `COUSR01C.cbl`, `COUSR02C.cbl`, and `COUSR03C.cbl`.

This catalog contains business-facing input, credential, uniqueness, existence, and workflow-integrity checks only. It deliberately excludes CICS communication failures, generic file/I/O failures, navigation/key handling, screen presentation, and calculation/formatting logic. COBOL `SPACES` and `LOW-VALUES` are both treated by these programs as an empty input value.

## Administrative menu

### RULE-VAL-001

**Rule Description**: An administrator must select a valid, enabled menu option before the requested administration function can be opened.

**COBOL Source Location**: `COADM01C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-134 (validation condition at lines 127-133).

**Field(s) Involved**: `OPTIONI OF COADM1AI` (entered option), `WS-OPTION`, `CDEMO-ADMIN-OPT-COUNT`.

**Validation Condition**: The normalized option must be numeric, greater than zero, and no greater than the configured number of administrative options.

**Trigger Conditions**: On Enter from the administration menu, after trailing spaces in `OPTIONI` are removed and remaining spaces are replaced with zeroes. The option is rejected when `WS-OPTION IS NOT NUMERIC OR WS-OPTION > CDEMO-ADMIN-OPT-COUNT OR WS-OPTION = ZEROS`.

## Sign-on and credentials

### RULE-VAL-002

**Rule Description**: A sign-on request requires a user ID.

**COBOL Source Location**: `COSGN00C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-122 (condition at line 118).

**Field(s) Involved**: `USERIDI OF COSGN0AI` (User ID).

**Validation Condition**: User ID must not be empty.

**Trigger Conditions**: On Enter at sign-on, reject the request when `USERIDI OF COSGN0AI = SPACES OR LOW-VALUES`.

### RULE-VAL-003

**Rule Description**: A sign-on request requires a password.

**COBOL Source Location**: `COSGN00C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-127 (condition at line 123).

**Field(s) Involved**: `PASSWDI OF COSGN0AI` (Password).

**Validation Condition**: Password must not be empty.

**Trigger Conditions**: On Enter at sign-on, after the User ID required check has passed, reject the request when `PASSWDI OF COSGN0AI = SPACES OR LOW-VALUES`.

### RULE-VAL-004

**Rule Description**: Sign-on is permitted only when the normalized entered password matches the registered password for the normalized entered user ID.

**COBOL Source Location**: `COSGN00C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 132-137 (uppercase normalization); `READ-USER-SEC-FILE` paragraph, lines 221-246 (credential comparison at lines 223-246). The Add User flow stores the entered credentials without this normalization in `COUSR01C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 153-159.

**Field(s) Involved**: `USERIDI OF COSGN0AI`, `PASSWDI OF COSGN0AI`, `WS-USER-ID`, `WS-USER-PWD`, `SEC-USR-ID`, `SEC-USR-PWD`.

**Validation Condition**: Before lookup and comparison, the sign-on program applies `FUNCTION UPPER-CASE` to the entered User ID and Password. The uppercased password must equal the stored password (`SEC-USR-PWD = WS-USER-PWD`) for the user found using the uppercased User ID.

**Trigger Conditions**: After required sign-on fields pass, lines 132-137 uppercase both inputs. After a record is successfully found (`WS-RESP-CD = 0`), the program rejects the sign-on when the password comparison fails. Because Add User persists the submitted User ID and Password unchanged, a mixed-case or lowercase stored User ID may not be found by the uppercased lookup, and a mixed-case or lowercase stored password will not match the uppercased entered password unless its stored value is already uppercase.

### RULE-VAL-005

**Rule Description**: A sign-on user ID must identify a registered user.

**COBOL Source Location**: `COSGN00C.cbl`, `READ-USER-SEC-FILE` paragraph, lines 211-251 (not-found branch at lines 247-251).

**Field(s) Involved**: `WS-USER-ID` / `USERIDI OF COSGN0AI` (User ID).

**Validation Condition**: The submitted user ID must exist in the user-security records.

**Trigger Conditions**: After required sign-on fields pass and the user-security lookup is attempted, reject the sign-on when `WS-RESP-CD` is `13` (the program’s User-not-found outcome).

## User creation

### RULE-VAL-006

**Rule Description**: A new user must have a first name.

**COBOL Source Location**: `COUSR01C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-123 (condition at line 118).

**Field(s) Involved**: `FNAMEI OF COUSR1AI` (First Name).

**Validation Condition**: First Name must not be empty.

**Trigger Conditions**: On Enter from the Add User screen, reject the request when `FNAMEI OF COUSR1AI = SPACES OR LOW-VALUES`.

### RULE-VAL-007

**Rule Description**: A new user must have a last name.

**COBOL Source Location**: `COUSR01C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-129 (condition at line 124).

**Field(s) Involved**: `LNAMEI OF COUSR1AI` (Last Name).

**Validation Condition**: Last Name must not be empty.

**Trigger Conditions**: On Enter from the Add User screen, after the First Name required check has passed, reject the request when `LNAMEI OF COUSR1AI = SPACES OR LOW-VALUES`.

### RULE-VAL-008

**Rule Description**: A new user must have a user ID.

**COBOL Source Location**: `COUSR01C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-135 (condition at line 130).

**Field(s) Involved**: `USERIDI OF COUSR1AI` (User ID).

**Validation Condition**: User ID must not be empty.

**Trigger Conditions**: On Enter from the Add User screen, after the preceding name checks pass, reject the request when `USERIDI OF COUSR1AI = SPACES OR LOW-VALUES`.

### RULE-VAL-009

**Rule Description**: A new user must have a password.

**COBOL Source Location**: `COUSR01C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-141 (condition at line 136).

**Field(s) Involved**: `PASSWDI OF COUSR1AI` (Password).

**Validation Condition**: Password must not be empty.

**Trigger Conditions**: On Enter from the Add User screen, after the preceding required-field checks pass, reject the request when `PASSWDI OF COUSR1AI = SPACES OR LOW-VALUES`.

### RULE-VAL-010

**Rule Description**: A new user must have a user type.

**COBOL Source Location**: `COUSR01C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 117-147 (condition at line 142).

**Field(s) Involved**: `USRTYPEI OF COUSR1AI` (User Type).

**Validation Condition**: User Type must not be empty.

**Trigger Conditions**: On Enter from the Add User screen, after the preceding required-field checks pass, reject the request when `USRTYPEI OF COUSR1AI = SPACES OR LOW-VALUES`.

### RULE-VAL-011

**Rule Description**: A new user ID must be unique.

**COBOL Source Location**: `COUSR01C.cbl`, `WRITE-USER-SEC-FILE` paragraph, lines 240-266 (duplicate-key outcomes at lines 260-266).

**Field(s) Involved**: `SEC-USR-ID` / `USERIDI OF COUSR1AI` (User ID).

**Validation Condition**: No existing user-security record may have the submitted user ID.

**Trigger Conditions**: After all Add User required-field checks pass and a write is attempted, reject the creation when the user record reports `DFHRESP(DUPKEY)` or `DFHRESP(DUPREC)`.

## User lookup and update

### RULE-VAL-012

**Rule Description**: Looking up or updating a user requires a user ID.

**COBOL Source Location**: `COUSR02C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 143-155 (condition at line 146); `UPDATE-USER-INFO` paragraph, lines 177-213 (condition at line 180).

**Field(s) Involved**: `USRIDINI OF COUSR2AI` (User ID).

**Validation Condition**: User ID must not be empty.

**Trigger Conditions**: On Enter to retrieve a user or when PF3/PF5 invokes user update processing, reject the request when `USRIDINI OF COUSR2AI = SPACES OR LOW-VALUES`.

### RULE-VAL-013

**Rule Description**: An update requires a first name.

**COBOL Source Location**: `COUSR02C.cbl`, `UPDATE-USER-INFO` paragraph, lines 179-191 (condition at line 186).

**Field(s) Involved**: `FNAMEI OF COUSR2AI` (First Name).

**Validation Condition**: First Name must not be empty.

**Trigger Conditions**: During update processing, after the User ID required check has passed, reject the update when `FNAMEI OF COUSR2AI = SPACES OR LOW-VALUES`.

### RULE-VAL-014

**Rule Description**: An update requires a last name.

**COBOL Source Location**: `COUSR02C.cbl`, `UPDATE-USER-INFO` paragraph, lines 179-197 (condition at line 192).

**Field(s) Involved**: `LNAMEI OF COUSR2AI` (Last Name).

**Validation Condition**: Last Name must not be empty.

**Trigger Conditions**: During update processing, after the prior required-field checks have passed, reject the update when `LNAMEI OF COUSR2AI = SPACES OR LOW-VALUES`.

### RULE-VAL-015

**Rule Description**: An update requires a password.

**COBOL Source Location**: `COUSR02C.cbl`, `UPDATE-USER-INFO` paragraph, lines 179-203 (condition at line 198).

**Field(s) Involved**: `PASSWDI OF COUSR2AI` (Password).

**Validation Condition**: Password must not be empty.

**Trigger Conditions**: During update processing, after the prior required-field checks have passed, reject the update when `PASSWDI OF COUSR2AI = SPACES OR LOW-VALUES`.

### RULE-VAL-016

**Rule Description**: An update requires a user type.

**COBOL Source Location**: `COUSR02C.cbl`, `UPDATE-USER-INFO` paragraph, lines 179-209 (condition at line 204).

**Field(s) Involved**: `USRTYPEI OF COUSR2AI` (User Type).

**Validation Condition**: User Type must not be empty.

**Trigger Conditions**: During update processing, after the prior required-field checks have passed, reject the update when `USRTYPEI OF COUSR2AI = SPACES OR LOW-VALUES`.

### RULE-VAL-017

**Rule Description**: A user ID used for lookup or update is intended to identify an existing user; the legacy flow does not reliably enforce that intended prerequisite before attempting an update.

**COBOL Source Location**: `COUSR02C.cbl`, `UPDATE-USER-INFO` paragraph, lines 215-245 (the error-flag guard is evaluated before the read at line 215; read at line 217; subsequent comparisons and possible rewrite at lines 219-237); `READ-USER-SEC-FILE` paragraph, lines 320-353 (not-found outcome at lines 340-345); `UPDATE-USER-SEC-FILE` paragraph, lines 358-390 (not-found outcome at lines 377-382).

**Field(s) Involved**: `SEC-USR-ID` / `USRIDINI OF COUSR2AI` (User ID), `WS-ERR-FLG`.

**Validation Condition**: Intended rule: the submitted user ID must exist in the user-security records before the record is saved.

**Trigger Conditions**: After required update-field checks pass, `UPDATE-USER-INFO` checks `NOT ERR-FLG-ON` once, then performs `READ-USER-SEC-FILE`. If that read returns `DFHRESP(NOTFND)`, its paragraph sets `WS-ERR-FLG`, but control returns to the already-entered outer IF and still runs field comparisons and may call `UPDATE-USER-SEC-FILE`; the flag is not rechecked between READ and REWRITE. Therefore NOTFND does not cleanly gate the rewrite. The final visible failure can be `Unable to Update User...` from the rewrite’s `WHEN OTHER` branch rather than the earlier `User ID NOT found...` message. This documents observed legacy behavior, not a reliable enforcement pattern.

### RULE-VAL-018

**Rule Description**: An update submission must change at least one editable user attribute.

**COBOL Source Location**: `COUSR02C.cbl`, `UPDATE-USER-INFO` paragraph, lines 215-245 (field comparisons at lines 219-234; no-change rejection at lines 236-243).

**Field(s) Involved**: `FNAMEI`, `LNAMEI`, `PASSWDI`, `USRTYPEI` of `COUSR2AI`; `SEC-USR-FNAME`, `SEC-USR-LNAME`, `SEC-USR-PWD`, `SEC-USR-TYPE`.

**Validation Condition**: At least one of First Name, Last Name, Password, or User Type must differ from the corresponding stored value.

**Trigger Conditions**: After all update required-field checks pass and the existing user record has been read, reject the update when `USR-MODIFIED-YES` is false (all four entered values match the stored values).

## User deletion

### RULE-VAL-019

**Rule Description**: Deleting a user requires a user ID.

**COBOL Source Location**: `COUSR03C.cbl`, `PROCESS-ENTER-KEY` paragraph, lines 142-154 (condition at line 145); `DELETE-USER-INFO` paragraph, lines 174-186 (condition at line 177); key dispatch at lines 108-123.

**Field(s) Involved**: `USRIDINI OF COUSR3AI` (User ID).

**Validation Condition**: User ID must not be empty.

**Trigger Conditions**: On Enter to retrieve a user for deletion, or when **PF5** invokes `DELETE-USER-INFO`, reject the request when `USRIDINI OF COUSR3AI = SPACES OR LOW-VALUES`. PF3 returns to the previous screen and does not invoke deletion processing (lines 111-118).

### RULE-VAL-020

**Rule Description**: A user ID selected for deletion is intended to identify an existing user; the legacy flow does not reliably enforce that intended prerequisite before attempting the delete.

**COBOL Source Location**: `COUSR03C.cbl`, `DELETE-USER-INFO` paragraph, lines 174-192 (the error-flag guard is evaluated before the read at line 188; read and delete execute at lines 190-191); `READ-USER-SEC-FILE` paragraph, lines 267-300 (not-found outcome at lines 287-292); `DELETE-USER-SEC-FILE` paragraph, lines 305-336 (not-found outcome at lines 323-328).

**Field(s) Involved**: `SEC-USR-ID` / `USRIDINI OF COUSR3AI` (User ID), `WS-ERR-FLG`.

**Validation Condition**: Intended rule: the submitted user ID must exist in the user-security records before it can be deleted.

**Trigger Conditions**: After the PF5 non-empty User ID check passes, `DELETE-USER-INFO` checks `NOT ERR-FLG-ON` once, then performs `READ-USER-SEC-FILE` and immediately performs `DELETE-USER-SEC-FILE`. If the read returns `DFHRESP(NOTFND)`, it sets `WS-ERR-FLG`, but the flag is not rechecked before DELETE. Therefore NOTFND does not cleanly gate deletion. The final visible failure can be `Unable to Update User...` (the delete paragraph’s legacy message) from DELETE’s `WHEN OTHER` branch, rather than the earlier `User ID NOT found...` message. This documents observed legacy behavior, not a reliable enforcement pattern.
