# Business Rules Catalog

## Scope and method

This catalog analyzes all five supplied COBOL programs: `COADM01C.cbl`, `COSGN00C.cbl`, `COUSR01C.cbl`, `COUSR02C.cbl`, and `COUSR03C.cbl`.

Only logic that represents a business-relevant decision or calculation is included. Screen navigation, CICS file operations, date/time display, key handling, field-presence checks, input normalization, duplicate-record handling, and change-detection flags were reviewed but excluded as technical handling, validation, conversion, or housekeeping.

## Evidence-based result

**One true business decision was identified.** The extract contains no business amount, rate, fee, balance, threshold, aggregation, or formula calculations. The four user-maintenance programs implement create, read/update, and delete workflows, but do not encode decision criteria beyond validation and technical persistence outcomes.

### RULE-DECISION-001: Direct a signed-in user according to assigned user type

**Description**: After credentials match the stored user-security record, the application grants the signed-in user the path associated with the `CDEMO-USRTYP-ADMIN` condition: users meeting that condition enter the administration menu, while users not meeting it enter the regular menu.

**Source**: `COSGN00C.cbl`, lines 221-240.

**Logic**: When the user-security record is found and its password equals the entered password, the program retains the record’s user type. If the `CDEMO-USRTYP-ADMIN` 88-level condition is true, it transfers control to `COADM01C` (the Admin Menu); otherwise, it transfers control to `COMEN01C` (the regular menu).

**Variables**:
- Input: Stored user type (`SEC-USR-TYPE`) and authenticated user-security record.
- Output: Authorized application entry path: administration menu or regular menu.

**Impact**: The result of the administrator user-type condition determines whether a signed-in user receives the administration menu or the regular application experience.

**Open dependency**: The concrete encoded value or values that make `CDEMO-USRTYP-ADMIN` true are defined in copybook `COCOM01Y`, which is referenced by the program at line 48 but is absent from the supplied extract. The literal must be obtained from that copybook; this source does not prove `ADMIN` or any other concrete value.

## Reviewed code and exclusions

| Program | Lines reviewed | Result |
| --- | --- | --- |
| `COADM01C.cbl` | 1-269 | No business rule extracted. The option range check at lines 127-134 is input validation; option-to-program dispatch at lines 137-154 and menu construction at lines 228-263 are UI/navigation handling. |
| `COSGN00C.cbl` | 1-260 | One role-based access decision extracted at lines 221-240. Required-field checks at lines 117-130, uppercase conversion at lines 132-136, password comparison, and file-status handling are excluded as validation, conversion, authentication implementation, or technical handling. |
| `COUSR01C.cbl` | 1-299 | No business rule extracted. Required-field checks at lines 117-151 and duplicate-key processing at lines 250-274 are validation/data-persistence outcomes; record creation at lines 153-159 and 240-248 is technical CRUD handling. |
| `COUSR02C.cbl` | 1-414 | No business rule extracted. Required-field checks at lines 179-213, changed-field detection at lines 219-243, and CICS read/rewrite status handling at lines 322-390 are validation, housekeeping, and technical persistence. |
| `COUSR03C.cbl` | 1-359 | No business rule extracted. Required-user-ID checks at lines 144-154 and 176-186 and CICS read/delete status handling at lines 269-336 are validation and technical persistence. |
