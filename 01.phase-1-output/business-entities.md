# CardDemo Business Entity Catalog

## Scope and evidence boundary

This catalog covers every COBOL source file in the supplied extract: `COADM01C.cbl`, `COSGN00C.cbl`, `COUSR01C.cbl`, `COUSR02C.cbl`, and `COUSR03C.cbl`. The only data store operated on by these programs is the CICS `USRSEC` dataset (declared by value in each user program; for example, `COUSR01C.cbl`, lines 35-44). Its record layout is referenced through `COPY CSUSR01Y` but that copybook is not present in this extract (`COUSR01C.cbl`, lines 50-53; `COSGN00C.cbl`, lines 52-55; `COUSR02C.cbl`, lines 62-65; `COUSR03C.cbl`, lines 62-65).

Consequently, **Application User** is inferable as a persisted entity, but its actual 01/05-level copybook layout, field picture clauses, optional fields, and any records not exercised by these programs are not visible. The catalog lists only fields whose use is demonstrated in the supplied COBOL; it does not reconstruct or invent the missing `CSUSR01Y` layout. CICS map fields, `WS-` work fields, the `CARDDEMO-COMMAREA`, and administrator-menu options are excluded because they are presentation, processing, or navigation data rather than persisted business entities.

### ENTITY-001: Application User

**Entity Type**: Master  
**Description**: A person authorized to use CardDemo. The user-security record stores an identifier, name, credential, and user type. It is created, retrieved, amended, and deleted in the `USRSEC` dataset. The user type controls whether successful sign-on goes to the administrator or regular-user menu.  
**Source**: Referenced copybook `CSUSR01Y` (not supplied): `COUSR01C.cbl`, lines 50-53. Visible field population and persisted write: `COUSR01C.cbl`, lines 153-159 and 240-248. Visible retrieval and use during authentication: `COSGN00C.cbl`, lines 211-227. Visible amendment: `COUSR02C.cbl`, lines 215-237 and 360-366. Visible deletion: `COUSR03C.cbl`, lines 188-191 and 307-311.

**Business Attributes**:
- Primary Key: `SEC-USR-ID` (inferred). It is the CICS record identifier for writes (`COUSR01C.cbl`, lines 240-245) and reads (`COSGN00C.cbl`, lines 211-216); duplicate-key/duplicate-record handling identifies it as the unique user ID (`COUSR01C.cbl`, lines 260-265).
- Core Attributes: `SEC-USR-FNAME`, `SEC-USR-LNAME`, and `SEC-USR-PWD`. The add program maps first name, last name, and password into these fields (`COUSR01C.cbl`, lines 154-158); the update program retrieves and changes them (`COUSR02C.cbl`, lines 166-170 and 219-229).
- Foreign Keys: None visible. No field in the observable user record is used to identify another persisted entity.
- Status Fields: No lifecycle status field is visible. `SEC-USR-TYPE` is a role/access classification, not a user-account status: it is populated on add (`COUSR01C.cbl`, lines 154-159), can be changed (`COUSR02C.cbl`, lines 231-233), and selects the administrator versus regular menu after authentication (`COSGN00C.cbl`, lines 223-240).

**Data Structure**:

The actual record definition is unavailable because `CSUSR01Y` is referenced but absent from the repository. The table therefore records the only business fields demonstrably addressed by the COBOL. `PIC` clauses and subordinate/group layout cannot be determined from this extract.

| Field | Data Type | Description | Key |
|---|---|---|---|
| `SEC-USR-ID` | Not visible — declared in missing `CSUSR01Y`; CICS key length is taken from this field (`COUSR01C.cbl`, lines 240-245) | Unique application-user identifier. | PK |
| `SEC-USR-FNAME` | Not visible — declared in missing `CSUSR01Y`; populated from the first-name screen value (`COUSR01C.cbl`, lines 154-156) | User's first name. | — |
| `SEC-USR-LNAME` | Not visible — declared in missing `CSUSR01Y`; populated from the last-name screen value (`COUSR01C.cbl`, lines 155-157) | User's last name. | — |
| `SEC-USR-PWD` | Not visible — declared in missing `CSUSR01Y`; compared at sign-on (`COSGN00C.cbl`, lines 221-246) | Credential used to authenticate the user. Storage format is not visible. | — |
| `SEC-USR-TYPE` | Not visible — declared in missing `CSUSR01Y`; populated from user type (`COUSR01C.cbl`, lines 157-159) | User role/access classification used to route the signed-on user. Allowed values are not visible. | — |

**Relationships**:
- Parent: None visible in this extract. The user record is read and written with `SEC-USR-ID` as its own CICS record identifier, and no observable field is passed as a key to another business data store (`COUSR01C.cbl`, lines 240-248; `COSGN00C.cbl`, lines 211-219).
- Children: None visible. No child record or dependent business data store is accessed by these five programs.
- Associates: No persisted associated entity is evidenced. `SEC-USR-TYPE` associates a user with an access path, routing to `COADM01C` for an administrator and `COMEN01C` otherwise, but the code does not show a Role entity or role-data store (`COSGN00C.cbl`, lines 223-240). Cardinality must therefore not be inferred.

**Usage Context**:
- Programs: `COSGN00C` reads the record to authenticate a user (`COSGN00C.cbl`, lines 207-257). `COUSR01C` creates it (`COUSR01C.cbl`, lines 153-159 and 236-274). `COUSR02C` reads and rewrites it (`COUSR02C.cbl`, lines 157-172, 215-245, and 318-366). `COUSR03C` reads and deletes it (`COUSR03C.cbl`, lines 156-169, 188-192, and 265-311). `COADM01C` is an administrator-menu program; it neither reads nor writes a user record (`COADM01C.cbl`, lines 115-155 and 170-263).
- Business Functions: User provisioning, sign-on authentication, role-based menu routing, user maintenance, and user removal. The supplied code validates the add fields before creating a record (`COUSR01C.cbl`, lines 117-159), compares the stored password at sign-on (`COSGN00C.cbl`, lines 221-246), and provides update/delete operations (`COUSR02C.cbl`, lines 175-245; `COUSR03C.cbl`, lines 172-192).

## Excluded candidate data

- **Card, customer, account, transaction, and balance data**: No such record layout, file definition, CICS dataset operation, or SQL declaration appears in the supplied COBOL. The application title alone is insufficient evidence to catalog these entities.
- **Administrator menu options**: `COADM01C` references `COADM02Y` (`COADM01C.cbl`, lines 50-53) and uses option number, name, and target program to build/navigate a menu (`COADM01C.cbl`, lines 117-155 and 224-263). This is navigation configuration, not a business entity, and the referenced copybook is absent.
- **User-selection fields in the COMMAREA**: `CDEMO-CU02-*` and `CDEMO-CU03-*` fields are screen/workflow context attached after `COPY COCOM01Y` (`COUSR02C.cbl`, lines 49-58; `COUSR03C.cbl`, lines 49-58). They are not a persisted user layout and are excluded.
