# Validation rules catalog

## Coverage and limits

All five tracked COBOL programs were scanned. This catalog includes input/data-integrity checks only. CICS `RESP` branches are technical outcomes and appear in [characterization-matrix.md](characterization-matrix.md), not as validations. Screen field definitions and lengths are unavailable because BMS/symbolic-map copybooks are absent; therefore no length, pattern, or allowed-value constraint is claimed.

## Sign-on (`COSGN00C`)

### RULE-VAL-001

**Rule Description**: A sign-on request requires a user ID before credential lookup.

**COBOL Source Location**: `COSGN00C.cbl:117-122`

**Field(s) Involved**: `USERIDI OF COSGN0AI` (screen field definition unknown)

**Validation Condition**: Reject if the value is spaces or low-values; show “Please enter User ID ...” and return to sign-on.

**Trigger Conditions**: Enter key processing; this is the first branch in the ordered `EVALUATE TRUE`.

### RULE-VAL-002

**Rule Description**: A sign-on request requires a password after a nonblank user ID is supplied.

**COBOL Source Location**: `COSGN00C.cbl:123-127`

**Field(s) Involved**: `PASSWDI OF COSGN0AI` (screen field definition unknown)

**Validation Condition**: Reject if the value is spaces or low-values; show “Please enter Password ...” and return to sign-on.

**Trigger Conditions**: Enter key processing, only after RULE-VAL-001 does not match.

## Administration menu (`COADM01C`)

### RULE-VAL-003

**Rule Description**: An administration selection must be a nonzero numeric option within the configured count.

**COBOL Source Location**: `COADM01C.cbl:117-134`

**Field(s) Involved**: `OPTIONI OF COADM1AI`, `CDEMO-ADMIN-OPT-COUNT` (configuration definition unknown)

**Validation Condition**: The program right-trims input, replaces remaining blanks with zeroes, then rejects a value that is nonnumeric, zero, or greater than the configured option count.

**Trigger Conditions**: Enter key processing. The valid range cannot be stated without `COADM02Y`.

## Add user (`COUSR01C`)

The order below is observable: only the first failing branch of the `EVALUATE TRUE` is reported per submission (`COUSR01C.cbl:117-151`).

### RULE-VAL-004

**Rule Description**: First name is required when adding a user.

**COBOL Source Location**: `COUSR01C.cbl:117-123`

**Field(s) Involved**: `FNAMEI OF COUSR1AI`

**Validation Condition**: Reject spaces or low-values with “First Name can NOT be empty...”.

**Trigger Conditions**: Enter key, first in add validation order.

### RULE-VAL-005

**Rule Description**: Last name is required when adding a user.

**COBOL Source Location**: `COUSR01C.cbl:124-129`

**Field(s) Involved**: `LNAMEI OF COUSR1AI`

**Validation Condition**: Reject spaces or low-values with “Last Name can NOT be empty...”.

**Trigger Conditions**: Enter key after first name passes.

### RULE-VAL-006

**Rule Description**: User ID is required when adding a user.

**COBOL Source Location**: `COUSR01C.cbl:130-135`

**Field(s) Involved**: `USERIDI OF COUSR1AI`

**Validation Condition**: Reject spaces or low-values with “User ID can NOT be empty...”.

**Trigger Conditions**: Enter key after first and last name pass.

### RULE-VAL-007

**Rule Description**: Password is required when adding a user.

**COBOL Source Location**: `COUSR01C.cbl:136-141`

**Field(s) Involved**: `PASSWDI OF COUSR1AI`

**Validation Condition**: Reject spaces or low-values with “Password can NOT be empty...”.

**Trigger Conditions**: Enter key after names and user ID pass.

### RULE-VAL-008

**Rule Description**: User type is required when adding a user.

**COBOL Source Location**: `COUSR01C.cbl:142-147`

**Field(s) Involved**: `USRTYPEI OF COUSR1AI`

**Validation Condition**: Reject spaces or low-values with “User Type can NOT be empty...”.

**Trigger Conditions**: Enter key after all preceding add fields pass. Allowed types are unknown.

### RULE-VAL-009

**Rule Description**: A newly added user must not reuse an existing user ID.

**COBOL Source Location**: `COUSR01C.cbl:240-266`

**Field(s) Involved**: `SEC-USR-ID`

**Validation Condition**: A duplicate-key/duplicate-record response from the attempted keyed write is rejected with “User ID already exist...”.

**Trigger Conditions**: After RULE-VAL-004 through RULE-VAL-008 pass and the system writes the record.

## Update user (`COUSR02C`)

### RULE-VAL-010

**Rule Description**: A user ID is required to look up a user for update.

**COBOL Source Location**: `COUSR02C.cbl:145-164`

**Field(s) Involved**: `USRIDINI OF COUSR2AI`

**Validation Condition**: Reject spaces or low-values with “User ID can NOT be empty...”; only a nonblank ID is used for lookup.

**Trigger Conditions**: Enter key / lookup processing.

### RULE-VAL-011

**Rule Description**: Update requires values in a fixed first-failure order: user ID, first name, last name, password, then user type.

**COBOL Source Location**: `COUSR02C.cbl:179-213`

**Field(s) Involved**: `USRIDINI`, `FNAMEI`, `LNAMEI`, `PASSWDI`, `USRTYPEI` of `COUSR2AI`

**Validation Condition**: Reject the first field in that sequence that is spaces or low-values, with its field-specific required message.

**Trigger Conditions**: PF3 or PF5 invokes `UPDATE-USER-INFO` (`COUSR02C.cbl:111-124`). Allowed user types remain unknown.

### RULE-VAL-012

**Rule Description**: An update must change at least one mutable user attribute.

**COBOL Source Location**: `COUSR02C.cbl:219-243`

**Field(s) Involved**: First name, last name, password, user type

**Validation Condition**: If all four submitted values equal the retrieved record, reject the update with “Please modify to update ...”.

**Trigger Conditions**: After required checks and successful read of the existing record.

## Delete user (`COUSR03C`)

### RULE-VAL-013

**Rule Description**: A user ID is required to look up or delete a user.

**COBOL Source Location**: `COUSR03C.cbl:144-162,176-192`

**Field(s) Involved**: `USRIDINI OF COUSR3AI`

**Validation Condition**: Reject spaces or low-values with “User ID can NOT be empty...”.

**Trigger Conditions**: Enter lookup and PF5 delete processing.

## Validation order summary

| Workflow | Ordered validation sequence | Evidence |
|---|---|---|
| Sign-on | User ID → password | `COSGN00C.cbl:117-130` |
| Admin menu | trim/right-align input → numeric/range/nonzero | `COADM01C.cbl:117-134` |
| Add | First name → last name → user ID → password → user type → duplicate check | `COUSR01C.cbl:117-160,240-266` |
| Update lookup | User ID | `COUSR02C.cbl:145-164` |
| Update save | User ID → first name → last name → password → user type → changed-value requirement | `COUSR02C.cbl:179-243` |
| Delete lookup/PF5 | User ID | `COUSR03C.cbl:144-192` |
