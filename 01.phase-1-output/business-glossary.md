# Business glossary

| Term | Meaning in this bounded slice | Evidence | Status |
|---|---|---|---|
| User | Principal with identifier, name, password and type managed in `USRSEC`. | `COUSR01C.cbl:153-160,240-248` | Confirmed attribute use; layout unknown |
| User ID | Identifier used as the `USRSEC` record key for add, lookup, update and delete. | `COUSR01C.cbl:130-135,153-154,240-245`; `COUSR02C.cbl:162-163` | Confirmed |
| Password | Credential collected at sign-on, compared with stored password, and maintained in add/update. | `COSGN00C.cbl:123-136,221-246`; `COUSR02C.cbl:198-203,227-230` | Confirmed; physical storage unknown |
| User type | Classification stored with a user and used to choose administrator versus regular-user handoff. | `COSGN00C.cbl:227-240` | Confirmed use; values unknown |
| Administrator | A user whose session satisfies `CDEMO-USRTYP-ADMIN`, sent to `COADM01C`. | `COSGN00C.cbl:230-234` | Confirmed condition name; role code unknown |
| Regular user | Any successfully authenticated user that does not satisfy the admin condition; sent to missing `COMEN01C`. | `COSGN00C.cbl:235-239` | Confirmed route; downstream function unknown |
| User administration | The observed add, lookup/update, lookup/delete, and admin-menu functions. | `COADM01C.cbl:117-155`; `COUSR01C.cbl:117-274`; `COUSR02C.cbl:145-390`; `COUSR03C.cbl:144-336` | Confirmed bounded slice |
| USRSEC | Named persistent dataset used for User records. | `COSGN00C.cbl:39,211-219` | Confirmed name; organization/encoding unknown |
| DUMMY option | A configured admin-menu program marker that yields a coming-soon result instead of transfer. | `COADM01C.cbl:137-155` | Confirmed marker; labels/options unknown |
