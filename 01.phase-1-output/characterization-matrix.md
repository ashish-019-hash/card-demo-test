# Characterization matrix — source-derived, unverified

## Test basis

No runnable legacy oracle, USRSEC data, or map copybooks are available. Each expected result below is derived directly from source and must be executed against a resettable legacy baseline when it is recovered. “Existing admin” and “existing regular” require fixtures whose actual role code is currently unknown. State changes are logical expectations; record byte compatibility is not testable until `CSUSR01Y`, encoding, and data organization are known.

| ID | Flow / initial data | Action | Expected observable result / resulting data | Route | Source |
|---|---|---|---|---|---|
| C-01 | Sign-on, any data | Enter with blank ID | “Please enter User ID ...”; no lookup. | Sign-on stays. | `COSGN00C.cbl:117-122` |
| C-02 | Sign-on, nonblank ID | Enter with blank password | “Please enter Password ...”; no lookup. | Sign-on stays. | `COSGN00C.cbl:123-127` |
| C-03 | Existing user; submitted password differs after uppercasing | Enter | “Wrong Password. Try again ...”. | Sign-on stays. | `COSGN00C.cbl:132-140,221-246` |
| C-04 | No record for uppercased ID | Enter | “User not found. Try again ...”. | Sign-on stays. | `COSGN00C.cbl:211-219,247-251` |
| C-05 | Existing user whose type satisfies admin condition and stored password equals uppercased submitted password | Enter | Session is populated with ID/type; successful authentication. | `COADM01C`. | `COSGN00C.cbl:132-136,221-234` |
| C-06 | Existing user whose type does not satisfy admin condition and password matches | Enter | Session is populated with ID/type. | `COMEN01C` (absent; journey stops). | `COSGN00C.cbl:221-240` |
| C-07 | Sign-on read returns response other than 0/13 | Enter | “Unable to verify the User ...”. | Sign-on stays. | `COSGN00C.cbl:252-256` |
| C-08 | Admin menu | Enter blank/non-numeric/0/out-of-range option | “Please enter a valid option number...”. | Menu stays. | `COADM01C.cbl:117-134` |
| C-09 | Admin menu configuration has valid non-`DUMMY` option | Enter that option | Transfer to configured target. | Dynamic/unknown target. | `COADM01C.cbl:137-145` |
| C-10 | Admin menu configuration has valid `DUMMY` option | Enter that option | “This option is coming soon ...”. | Menu stays. | `COADM01C.cbl:137-155` |
| C-11 | Add user | Enter; first name missing (or all missing) | First-name-required message; no write. | Add stays. | `COUSR01C.cbl:117-123` |
| C-12 | Add user; first name supplied | Enter; last name missing | Last-name-required message; no write. | Add stays. | `COUSR01C.cbl:124-129` |
| C-13 | Add user; names supplied | Enter; ID missing | User-ID-required message; no write. | Add stays. | `COUSR01C.cbl:130-135` |
| C-14 | Add user; names/ID supplied | Enter; password missing | Password-required message; no write. | Add stays. | `COUSR01C.cbl:136-141` |
| C-15 | Add user; preceding fields supplied | Enter; type missing | User-type-required message; no write. | Add stays. | `COUSR01C.cbl:142-147` |
| C-16 | Add user; valid populated data, new key | Enter | User record is written; fields clear; “User [ID] has been added ...”. | Add stays. | `COUSR01C.cbl:153-160,240-259` |
| C-17 | Add user; populated data, existing key | Enter | “User ID already exist...”; existing record unchanged. | Add stays. | `COUSR01C.cbl:260-266` |
| C-18 | Add user; write technical response | Enter | “Unable to Add User...”. | Add stays. | `COUSR01C.cbl:267-273` |
| C-19 | Update screen | Enter blank ID | User-ID-required message. | Update stays. | `COUSR02C.cbl:145-151` |
| C-20 | Update screen; existing user | Enter ID | First/last name, password, and type populate; “Press PF5 key to save your updates ...”. | Update stays. | `COUSR02C.cbl:157-172,322-339` |
| C-21 | Update lookup; unknown user | Enter ID | “User ID NOT found...”. | Update stays. | `COUSR02C.cbl:340-345` |
| C-22 | Update lookup technical error | Enter ID | “Unable to lookup User...”. | Update stays. | `COUSR02C.cbl:346-352` |
| C-23 | Loaded update; ID/first/last/password/type each present but unchanged | Press PF5 | “Please modify to update ...”; no rewrite. | Update stays. | `COUSR02C.cbl:219-243` |
| C-24 | Loaded update; at least one mutable field differs | Press PF5 | Changed values move to record; rewrite; “User [ID] has been updated ...”. | Update stays. | `COUSR02C.cbl:219-237,360-376` |
| C-25 | Update rewrite returns NOTFND/other | Press PF5 with changed field | Not-found or unable-to-update message. | Update stays. | `COUSR02C.cbl:377-389` |
| C-26 | Loaded update | Press PF3 | Source invokes update, then returns to source program/admin; distinguish this from conventional back/cancel. | Prior program or admin. | `COUSR02C.cbl:111-119` |
| C-27 | Delete screen | Enter blank ID | User-ID-required message. | Delete stays. | `COUSR03C.cbl:144-150` |
| C-28 | Delete screen; existing user | Enter ID | First/last name/type populate; “Press PF5 key to delete this user ...”; password not populated. | Delete stays. | `COUSR03C.cbl:156-168,269-286` |
| C-29 | Delete lookup; unknown user/technical error | Enter ID | “User ID NOT found...” or “Unable to lookup User...”. | Delete stays. | `COUSR03C.cbl:287-299` |
| C-30 | Existing user located | Press PF5 | Re-read then delete; fields clear; “User [ID] has been deleted ...”. | Delete stays. | `COUSR03C.cbl:176-192,303-322` |
| C-31 | Delete returns NOTFND/other | Press PF5 | Not-found or “Unable to Update User...” (legacy text) message. | Delete stays. | `COUSR03C.cbl:323-335` |
| C-32 | Add/update/delete screens | Press PF4 | Clears fields and redisplays respective screen. | Same screen. | `COUSR01C.cbl:96-97,277-295`; `COUSR02C.cbl:120-121,393-411`; `COUSR03C.cbl:119-120,339-356` |

## Key/route behavior matrix

| Flow | Enter | PF3 | PF4 | PF5 | PF12 | Other key |
|---|---|---|---|---|---|---|
| Sign-on | validate/authenticate | thank-you/end text | not coded | not coded | not coded | invalid-key message | 
| Admin menu | validate option/route or coming soon | sign-on | not coded | not coded | not coded | invalid-key message |
| Add | validate/create | admin | clear | not coded | not coded | invalid-key message |
| Update | lookup | **update then return** | clear | update | admin | invalid-key message |
| Delete | lookup | prior/admin | clear | delete | admin | invalid-key message |

Sources: `COSGN00C.cbl:85-94`; `COADM01C.cbl:93-103`; `COUSR01C.cbl:90-103`; `COUSR02C.cbl:108-131`; `COUSR03C.cbl:108-130`.
