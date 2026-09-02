# Source-derived user stories

These stories apply only to the bounded identity/user-administration slice. Acceptance criteria preserve observable behavior but do not assert unknown map geometry, field lengths, role codes, option labels, or absent downstream modules.

## Sign-on

### STORY-001: Sign on and receive role-appropriate handoff

**User Story**: "As an application user, I want to sign on with my user ID and password so that I can reach the functions appropriate to my user type."

**Story Type**: Customer-Facing

**Source Location**: `COSGN00C.cbl:117-140,211-256`

**Acceptance Criteria**:
- A blank user ID is rejected before a blank password is evaluated.
- A blank password is rejected after a nonblank user ID is supplied.
- The legacy behavior uppercases both submitted credentials before lookup/comparison.
- A matching administrator reaches administration; a matching non-administrator reaches the regular-menu handoff boundary.
- Wrong password, absent user, and technical lookup outcomes remain distinguishable to the user.

**User Journey Context**:
- Entry Point: Sign-on map `COSGN0A`.
- User Actions: Enter credentials and submit.
- Expected Outcomes: Access handoff or an actionable sign-on message.

**Business Value**: Limits the available workflow to the signed-in user’s classification.

## Administration menu

### STORY-002: Choose an available administration function

**User Story**: "As an administrator, I want to choose a valid administration option so that I can continue to an available management function."

**Story Type**: Administrative

**Source Location**: `COADM01C.cbl:117-155,228-263`

**Acceptance Criteria**:
- A selection must be numeric, nonzero, and no greater than the configured option count.
- A valid option whose configured program name is not `DUMMY` transfers to that program.
- A valid `DUMMY` option stays on the menu and communicates that it is coming soon.
- Exact labels, count, and target programs remain unresolved until `COADM02Y` is supplied.

**User Journey Context**:
- Entry Point: Successful administrator sign-on.
- User Actions: Enter an option and submit.
- Expected Outcomes: Route to available configured functionality or remain on the menu with feedback.

**Business Value**: Gives administrators controlled access to configured administration capabilities.

## User maintenance

### STORY-003: Add a user

**User Story**: "As an administrator, I want to add a user with identity, credentials, and type so that the person can be recognized by the application."

**Story Type**: Administrative

**Source Location**: `COUSR01C.cbl:117-160,240-274`

**Acceptance Criteria**:
- Required fields are evaluated in order: first name, last name, user ID, password, user type.
- A fully populated record is created when its user ID is not already present.
- A duplicate user ID is rejected without claiming success.
- A technical creation failure reports an add failure and retains the workflow.
- PF4 clears values; PF3 returns to administration (`COUSR01C.cbl:90-103,277-295`).

**User Journey Context**:
- Entry Point: The configured admin-menu route is not proven; once in `COUSR01C`, the add map is shown.
- User Actions: Supply the five fields and submit.
- Expected Outcomes: A created user or the first applicable validation/persistence message.

**Business Value**: Allows administrators to provision people for application access.

### STORY-004: Find and update a user

**User Story**: "As an administrator, I want to find a user and save changed details so that their access record remains current."

**Story Type**: Administrative

**Source Location**: `COUSR02C.cbl:145-245,322-390`

**Acceptance Criteria**:
- A user ID is required for lookup; an existing user populates first name, last name, password, and type.
- Lookup not-found and technical failures remain distinguishable.
- Save validates user ID, first name, last name, password, and user type in that order.
- Saving no changed value is rejected; saving a changed record confirms update or surfaces not-found/technical failure.
- PF5 saves. PF4 clears. PF12 returns to administration.
- PF3 invokes save before navigation in the legacy source; confirmation is required before a modern UI intentionally changes that behavior (`COUSR02C.cbl:108-126`).

**User Journey Context**:
- Entry Point: The configured admin-menu route is not proven; or a preselected ID can enter through COMMAREA (`COUSR02C.cbl:95-105`).
- User Actions: Look up by ID, alter fields, confirm with PF5.
- Expected Outcomes: Updated user, no-change feedback, or an error outcome.

**Business Value**: Keeps user information and classification usable without recreating the record.

### STORY-005: Review and delete a user

**User Story**: "As an administrator, I want to review a user and explicitly confirm deletion so that obsolete access can be removed deliberately."

**Story Type**: Administrative

**Source Location**: `COUSR03C.cbl:144-192,269-336`

**Acceptance Criteria**:
- A user ID is required for lookup/delete.
- An existing lookup displays first name, last name, and type, then asks for PF5 deletion confirmation; password is not copied to the delete screen.
- PF5 deletes the located record and reports success; not-found and technical outcomes remain distinct.
- PF4 clears. PF3 returns to the prior program/administration; PF12 returns to administration.

**User Journey Context**:
- Entry Point: The configured admin-menu route is not proven; or a preselected ID can enter through COMMAREA (`COUSR03C.cbl:95-105`).
- User Actions: Look up user, review non-password details, press PF5 to delete.
- Expected Outcomes: Deletion confirmation or an error with the user retained when deletion fails.

**Business Value**: Provides deliberate removal of no-longer-needed access records.

## Excluded stories

No story is created for the regular-user menu, cards, accounts, transactions, option labels, or option targets because their source programs/configuration are unavailable. Header date/time, terminal keys, CICS response codes, and map formatting are interaction/implementation details rather than user goals.
