# Business rules catalog

## Method and boundary

This catalog deliberately excludes required-field checks, option numeric/range checks, file response handling, uppercasing/data conversion, and CICS mechanics; those are documented in [validation-rules.md](validation-rules.md) or characterization artifacts. The available slice contains no rates, thresholds, amounts, or formulas. The two source-backed consequential decisions below are the only candidates that survive the business-rule filter.

### RULE-DECISION-001: Authenticated user route follows user classification

**Description**: A successfully authenticated user classified as an administrator is sent to administration; a successfully authenticated user not classified as an administrator is sent to the regular-user menu.

**Source**: `COSGN00C.cbl:221-240`

**Logic**: After a successful read and password equality, place the user type in session context. If `CDEMO-USRTYP-ADMIN` is true, transfer to `COADM01C`; otherwise transfer to `COMEN01C`.

**Variables**:
- Input: verified credential and returned `SEC-USR-TYPE`.
- Output: administration versus regular-user destination.

**Impact**: Determines the initial functional area available after sign-on. The actual administrator role code and regular menu behavior are unknown because `COCOM01Y` and `COMEN01C` are absent.

### RULE-DECISION-002: Configured administration options may be available or deferred

**Description**: A valid administration-menu option with a configured target other than the `DUMMY` marker transfers to that target; a `DUMMY` target remains on the menu with a coming-soon message.

**Source**: `COADM01C.cbl:137-155`

**Logic**: Read the selected entry from the configured program-name table. Non-`DUMMY` targets are invoked; `DUMMY` targets yield a deferred/coming-soon outcome.

**Variables**:
- Input: valid selected option and its configured target.
- Output: target program transfer or deferred message.

**Impact**: Allows the menu configuration to control which administration functions are operational. Option count, labels, option-to-program table, and all targets are unknown because `COADM02Y` is absent (`COADM01C.cbl:50-51,127-155,228-263`).

## Explicit non-rules

- `USERIDI`/password uppercasing is an implementation-visible normalization behavior, documented in characterization and the modern contract (`COSGN00C.cbl:132-136`).
- Required fields, option parsing, and duplicate identifiers are data-integrity/interaction behavior, documented in the validation catalog.
- CICS response handling is technical error handling, not a business rule.
