# CardDemo User Stories

## Scope and role evidence

This catalog covers all five tracked source files in the supplied extract: `COSGN00C.cbl`, `COADM01C.cbl`, `COUSR01C.cbl`, `COUSR02C.cbl`, and `COUSR03C.cbl`. The programs implement CICS screens for sign-on, an administration menu, and add/update/delete user administration. Copybooks and the non-administrator target program (`COMEN01C`) are referenced but are not present in this extract; no capabilities from those absent artifacts are represented as stories.

**Supported roles evidenced by the extract**

- **Unauthenticated sign-on user** — submits a user ID and password at the sign-on screen. [Source: `COSGN00C.cbl:80-95`, `PROCESS-ENTER-KEY` at `COSGN00C.cbl:108-140`]
- **Administrator** — after a successful credential check, a stored administrator user type is routed to the administration menu. The menu and all three user-maintenance programs are described as administration functions. [Source: `COSGN00C.cbl:221-240`; `COADM01C.cbl:2-5`; `COUSR01C.cbl:2-5`; `COUSR02C.cbl:2-5`; `COUSR03C.cbl:2-5`]
- **Regular authenticated user** — after successful credential validation, a non-administrator is routed to `COMEN01C`; its screen and functions are absent, so no further regular-user capabilities can be extracted. [Source: `COSGN00C.cbl:221-240`]

No customer-specific, batch, or external-integration user workflow is implemented in the supplied files. The CICS data operations are implementation mechanisms for the user-facing workflows below, not separate stories.

## Sign-On Screen — `COSGN00C`

### STORY-001: Sign in as an administrator

**User Story**: "As an administrator, I want to sign in with my user ID and password so that I can reach the administration menu."

**Story Type**: Administrative

**Source Location**: `COSGN00C.cbl`, `MAIN-PARA` lines 80-95; `PROCESS-ENTER-KEY` lines 108-140; `READ-USER-SEC-FILE` lines 209-257.

**Acceptance Criteria**:
- A user ID and password are both required; if user ID is blank, the screen displays "Please enter User ID ..." and returns focus to User ID; if password is blank, it displays "Please enter Password ..." and returns focus to Password. [Source: `COSGN00C.cbl:117-127`]
- Before verification, the submitted user ID and password are converted to uppercase. [Source: `COSGN00C.cbl:132-137`]
- When the user record is found and its password matches, the stored user type is retained in session context; an administrator type is transferred to `COADM01C`. [Source: `COSGN00C.cbl:221-234`]
- If the password does not match, the screen displays "Wrong Password. Try again ..." and returns focus to Password. [Source: `COSGN00C.cbl:241-245`]
- If no user record is found, the screen displays "User not found. Try again ..." and returns focus to User ID; any other verification failure displays "Unable to verify the User ..." and also returns focus to User ID. [Source: `COSGN00C.cbl:247-256`]
- A key other than Enter or PF3 redisplays the sign-on screen with the configured invalid-key message. [Source: `COSGN00C.cbl:85-95`]

**User Journey Context**:
- Entry Point: The initial invocation shows the sign-on screen and positions the cursor at User ID. [Source: `COSGN00C.cbl:80-83`, `SEND-SIGNON-SCREEN` at `COSGN00C.cbl:143-157`]
- User Actions: Enter a user ID and password, then press Enter.
- Expected Outcomes: Valid administrator credentials open the administration menu; incomplete or unsuccessful credentials leave the user on sign-on with a specific message and cursor placement.

**Business Value**: Gives administrators a controlled entry point to the application’s available administration functions and explains why access did not proceed when credentials are incomplete or invalid.

### STORY-002: Sign in as a regular user

**User Story**: "As a regular user, I want to sign in with my user ID and password so that I can reach the application path assigned to my user type."

**Story Type**: Customer-Facing

**Source Location**: `COSGN00C.cbl`, `PROCESS-ENTER-KEY` lines 108-140; `READ-USER-SEC-FILE` lines 209-257, especially lines 221-240.

**Acceptance Criteria**:
- The user must supply nonblank user ID and password; missing values produce the same field-specific messages and focus behavior as the administrator sign-on flow. [Source: `COSGN00C.cbl:117-127`]
- When the user record is found and the password matches but the stored user type is not administrator, the program transfers the user to `COMEN01C`. [Source: `COSGN00C.cbl:221-240`]
- An incorrect password, unknown user ID, or verification error does not transfer the user; instead, the program redisplays sign-on with its respective error message. [Source: `COSGN00C.cbl:241-256`]

**User Journey Context**:
- Entry Point: The CardDemo sign-on screen (`COSGN0A`). [Source: `COSGN00C.cbl:151-157`]
- User Actions: Enter credentials and press Enter.
- Expected Outcomes: A verified non-administrator is sent to the distinct regular-user program. The behavior of that destination is not available in this extract.

**Business Value**: Routes verified regular users to their assigned application experience while preventing failed credential checks from progressing.

### STORY-003: End a sign-on session

**User Story**: "As a sign-on user, I want to exit from the sign-on screen so that I can end my current attempt without continuing into the application."

**Story Type**: Operational

**Source Location**: `COSGN00C.cbl`, `MAIN-PARA` lines 85-95; `SEND-PLAIN-TEXT` lines 160-172.

**Acceptance Criteria**:
- Pressing PF3 at sign-on displays the configured thank-you message as plain text, erases the screen, frees the keyboard, and returns from the CICS interaction. [Source: `COSGN00C.cbl:85-90`, `COSGN00C.cbl:160-172`]
- Pressing a key other than Enter or PF3 does not exit; it redisplays the sign-on screen with an invalid-key message. [Source: `COSGN00C.cbl:91-95`]

**User Journey Context**:
- Entry Point: The sign-on screen before a successful transfer to another program.
- User Actions: Press PF3.
- Expected Outcomes: The user receives the thank-you message and the interaction ends rather than attempting authentication.

**Business Value**: Lets a user explicitly leave the sign-on interaction without submitting credentials.

## Administration Menu — `COADM01C`

### STORY-004: Select an available administration menu option

**User Story**: "As an administrator, I want to select a numbered administration option so that I can navigate to an available administrative function."

**Story Type**: Administrative

**Source Location**: `COADM01C.cbl`, `MAIN-PARA` lines 82-103; `PROCESS-ENTER-KEY` lines 115-155; `BUILD-MENU-OPTIONS` lines 226-263.

**Acceptance Criteria**:
- On first entry, the program builds and displays numbered administration options from the configured option list. [Source: `COADM01C.cbl:87-90`, `COADM01C.cbl:172-184`, `COADM01C.cbl:226-263`]
- The entered option is trimmed of trailing spaces and treated as a number. A nonnumeric, zero, or number greater than the configured option count displays "Please enter a valid option number..." and redisplays the menu. [Source: `COADM01C.cbl:117-134`]
- A valid option whose configured target program is not marked `DUMMY` transfers control to that configured program while preserving the prior transaction/program context. [Source: `COADM01C.cbl:137-145`]
- A valid option marked `DUMMY` does not transfer; the menu displays "This option is coming soon ...". The supplied source does not reveal which option numbers or names are configured this way. [Source: `COADM01C.cbl:137-155`]
- PF3 returns to sign-on. Any other unsupported key displays the configured invalid-key message and redisplays the menu. [Source: `COADM01C.cbl:93-103`, `RETURN-TO-SIGNON-SCREEN` at `COADM01C.cbl:160-167`]

**User Journey Context**:
- Entry Point: Successful sign-on by a user with administrator type transfers here. [Source: `COSGN00C.cbl:223-234`]
- User Actions: Review the numbered options, enter a valid option number, and press Enter; alternatively, press PF3 to return to sign-on.
- Expected Outcomes: The administrator reaches an implemented configured function, receives clear feedback for invalid or not-yet-implemented choices, or returns to sign-on.

**Business Value**: Provides an organized navigation point for available administrative work and prevents an out-of-range or malformed selection from reaching an unintended function.

## User Add Screen — `COUSR01C`

### STORY-005: Add a regular or administrator user

**User Story**: "As an administrator, I want to add a user with identity, credentials, and user type so that a regular or administrator user can be set up for application access."

**Story Type**: Administrative

**Source Location**: `COUSR01C.cbl`, program function comment lines 2-5; `PROCESS-ENTER-KEY` lines 115-160; `WRITE-USER-SEC-FILE` lines 238-274.

**Acceptance Criteria**:
- The add screen requires first name, last name, user ID, password, and user type. It detects the first blank field in that order, displays its specific "can NOT be empty" message, and positions the cursor at that field. [Source: `COUSR01C.cbl:117-151`]
- When all required fields are present, the program records the submitted user ID, first name, last name, password, and user type as the new user information. [Source: `COUSR01C.cbl:153-160`, `COUSR01C.cbl:240-248`]
- A successful add clears the form and confirms "User [ID] has been added ...". [Source: `COUSR01C.cbl:250-259`, `INITIALIZE-ALL-FIELDS` at `COUSR01C.cbl:285-295`]
- If the user ID already exists, the screen displays "User ID already exist..." and returns focus to User ID. [Source: `COUSR01C.cbl:260-266`]
- For another add failure, the screen displays "Unable to Add User..." and returns focus to First Name. [Source: `COUSR01C.cbl:267-273`]
- PF4 clears all entered values; PF3 returns to the administration menu; unsupported keys show the configured invalid-key message. [Source: `COUSR01C.cbl:90-103`, `CLEAR-CURRENT-SCREEN` at `COUSR01C.cbl:276-295`, `RETURN-TO-PREV-SCREEN` at `COUSR01C.cbl:165-178`]
- The extract does not validate the permitted values of the nonblank user type, even though the program’s stated function describes adding Regular/Admin users. [Source: `COUSR01C.cbl:2-5`, `COUSR01C.cbl:142-158`]

**User Journey Context**:
- Entry Point: An administration option reaches the add-user screen; the first-name field is initially selected. [Source: `COUSR01C.cbl:83-87`]
- User Actions: Enter first name, last name, user ID, password, and user type; press Enter to add. Use PF4 to reset or PF3 to return.
- Expected Outcomes: A unique, complete user is added and confirmed; missing, duplicate, or failed submissions remain on the screen with a targeted message.

**Business Value**: Enables administrators to provision the user information needed for regular or administrative application access without allowing an existing user ID to be silently overwritten.

## User Update Screen — `COUSR02C`

### STORY-006: Look up and update an existing user

**User Story**: "As an administrator, I want to look up a user and save changed user details so that user information and access credentials remain current."

**Story Type**: Administrative

**Source Location**: `COUSR02C.cbl`, program function comment lines 2-5; `PROCESS-ENTER-KEY` lines 143-172; `UPDATE-USER-INFO` lines 177-245; `READ-USER-SEC-FILE` lines 320-353; `UPDATE-USER-SEC-FILE` lines 358-390.

**Acceptance Criteria**:
- Entering a nonblank User ID and pressing Enter retrieves that user’s first name, last name, password, and user type, then displays a prompt to press PF5 to save updates. [Source: `COUSR02C.cbl:145-172`, `COUSR02C.cbl:322-339`]
- A blank lookup User ID displays "User ID can NOT be empty..." and returns focus to User ID. A user that is not found displays "User ID NOT found..." and also returns focus to User ID. [Source: `COUSR02C.cbl:145-151`, `COUSR02C.cbl:340-345`]
- To save, the User ID, first name, last name, password, and user type must all be nonblank. The program displays a field-specific message and focuses the first missing field in that sequence. [Source: `COUSR02C.cbl:179-213`]
- Saving changes updates only fields that differ from the retrieved first name, last name, password, or user type. If none of those fields differs, the screen displays "Please modify to update ..." rather than reporting an update. [Source: `COUSR02C.cbl:215-243`]
- **Observed legacy behavior — unknown ID on save:** PF5 validates the nonblank fields and checks the error flag *before* it reads the user record. The subsequent `READ` can set the not-found error and display "User ID NOT found...", but execution then continues through the field comparisons and invokes `REWRITE` without rechecking that flag. Consequently, a save attempt for an unknown ID can end with the rewrite failure message "Unable to Update User..." rather than leaving the lookup not-found message as the final outcome. [Source: `COUSR02C.cbl:215-245`, `COUSR02C.cbl:322-353`, `COUSR02C.cbl:360-390`]
- A successful rewrite confirms "User [ID] has been updated ...". A rewrite that reports not found displays "User ID NOT found..."; another rewrite failure displays "Unable to Update User...". [Source: `COUSR02C.cbl:368-390`]
- **Recommended modern behavior:** After any lookup that reports not found or another lookup error, stop the save workflow before comparing fields or attempting an update; retain the lookup error on screen. This is a modernization recommendation, not observed legacy behavior.
- PF4 clears all fields. PF5 initiates the update. PF12 returns to the administration menu. **Observed legacy behavior — PF3:** PF3 first attempts `UPDATE-USER-INFO` and then unconditionally transfers to the prior program; because the transfer follows the attempted update regardless of its result, any update error screen can be discarded. [Source: `COUSR02C.cbl:108-119`, `UPDATE-USER-INFO` at `COUSR02C.cbl:177-245`, `RETURN-TO-PREV-SCREEN` at `COUSR02C.cbl:250-261`]
- Unsupported keys redisplay the update screen with the configured invalid-key message. [Source: `COUSR02C.cbl:127-130`]

**User Journey Context**:
- Entry Point: The update-user screen starts with User ID selected; it can also receive a preselected user ID in its passed context. [Source: `COUSR02C.cbl:95-105`]
- User Actions: Enter User ID and press Enter; review the loaded details; alter one or more fields; press PF5 to save. Use PF4 to reset or PF12 to return to the administration menu.
- Expected Outcomes: The administrator sees the existing user before saving, receives confirmation for a genuine update, and receives precise feedback for blank fields, unknown users, no-change submissions, or failures.

**Business Value**: Allows administrators to maintain user identity, credentials, and user type while avoiding a misleading success result when nothing changed.

## User Delete Screen — `COUSR03C`

### STORY-007: Look up and delete an existing user

**User Story**: "As an administrator, I want to look up a user and delete that user so that accounts that should no longer have application access can be removed."

**Story Type**: Administrative

**Source Location**: `COUSR03C.cbl`, program function comment lines 2-5; `PROCESS-ENTER-KEY` lines 142-169; `DELETE-USER-INFO` lines 174-192; `READ-USER-SEC-FILE` lines 267-300; `DELETE-USER-SEC-FILE` lines 305-336.

**Acceptance Criteria**:
- Entering a nonblank User ID and pressing Enter retrieves and displays that user’s first name, last name, and user type, then displays a prompt to press PF5 to delete the user. [Source: `COUSR03C.cbl:144-169`, `COUSR03C.cbl:269-286`]
- A blank User ID displays "User ID can NOT be empty..." and returns focus to User ID. A user that cannot be found displays "User ID NOT found..." and also returns focus to User ID. [Source: `COUSR03C.cbl:144-150`, `COUSR03C.cbl:287-292`]
- **Observed legacy behavior — unknown ID on delete:** PF5 validates the nonblank User ID and checks the error flag *before* it reads the user record. The subsequent `READ` can set the not-found error and display "User ID NOT found...", but the code then invokes `DELETE-USER-SEC-FILE` without rechecking that flag. Consequently, a delete attempt for an unknown ID can end with the delete failure message "Unable to Update User..." (the exact message coded by the delete program), rather than leaving the lookup not-found message as the final outcome. [Source: `COUSR03C.cbl:176-192`, `COUSR03C.cbl:269-300`, `COUSR03C.cbl:305-336`]
- A successful deletion clears the form and confirms "User [ID] has been deleted ...". A delete that reports not found displays "User ID NOT found..."; another delete failure displays "Unable to Update User..." (the exact message coded for this delete failure). [Source: `COUSR03C.cbl:313-336`, `INITIALIZE-ALL-FIELDS` at `COUSR03C.cbl:347-356`]
- **Recommended modern behavior:** After any lookup that reports not found or another lookup error, stop the deletion workflow before attempting deletion; retain the lookup error on screen. This is a modernization recommendation, not observed legacy behavior.
- PF4 clears all fields; PF3 returns to the prior program; PF12 returns to the administration menu; unsupported keys show the configured invalid-key message. [Source: `COUSR03C.cbl:108-130`, `CLEAR-CURRENT-SCREEN` at `COUSR03C.cbl:339-356`]

**User Journey Context**:
- Entry Point: The delete-user screen starts with User ID selected; it can also receive a preselected user ID in its passed context. [Source: `COUSR03C.cbl:95-105`]
- User Actions: Enter User ID and press Enter to view the user; press PF5 to delete. Use PF4 to clear, PF3 to return, or PF12 for the administration menu.
- Expected Outcomes: The administrator can review the available identity details before deletion, receives confirmation when deletion succeeds, and remains informed if the user is absent or the operation fails.

**Business Value**: Enables administrators to remove user accounts that should no longer be available while providing a review step and explicit outcome feedback.
