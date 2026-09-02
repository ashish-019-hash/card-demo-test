# Business Glossary

This glossary defines business-relevant concepts evidenced in the five analyzed programs. It does not infer terms or policies that are not implemented in the supplied source.

| Term | Definition | Evidence |
| --- | --- | --- |
| Administrator condition | The `CDEMO-USRTYP-ADMIN` 88-level condition. A signed-in user for whom it is true is directed to the administration menu. The concrete encoded user-type value or values are not present in the supplied source; they are an open dependency on copybook `COCOM01Y`. | `COSGN00C.cbl`, lines 48 and 223-240; `COADM01C.cbl`, lines 2-5. |
| Non-administrator path | The application path used when `CDEMO-USRTYP-ADMIN` is false; it directs the signed-in user to the regular menu. The source does not enumerate the underlying user-type value or values. | `COSGN00C.cbl`, lines 230-240. |
| User type | The classification held in a user-security record and used to evaluate the administrator condition when selecting the signed-in user’s application path. The code does not expose the administrator condition’s encoded value or values. | `COSGN00C.cbl`, lines 48 and 223-240; `COUSR01C.cbl`, lines 153-159. |
| User-security record | The stored record containing a user ID, first name, last name, password, and user type. It is read at sign-on and maintained by the add, update, and delete programs. | `COSGN00C.cbl`, lines 211-219 and 223-227; `COUSR01C.cbl`, lines 153-159 and 240-248; `COUSR02C.cbl`, lines 219-233 and 360-366; `COUSR03C.cbl`, lines 307-311. |
| User ID | The identifier used to locate a user-security record and identify the signed-in user. | `COSGN00C.cbl`, lines 132-134 and 211-216; `COUSR01C.cbl`, lines 153-159. |
| Sign-on | The process that reads a user-security record by user ID, compares the stored and entered passwords, and then selects the user’s application path based on user type. | `COSGN00C.cbl`, lines 211-246. |
| Administration menu | The menu for administrator users, presented by program `COADM01C`. | `COADM01C.cbl`, lines 2-5; `COSGN00C.cbl`, lines 230-234. |
| User maintenance | The supported operational capability to add, update, or delete user-security records. The supplied code contains no additional business policy governing those operations. | `COUSR01C.cbl`, lines 2-5 and 153-159; `COUSR02C.cbl`, lines 2-5 and 215-243; `COUSR03C.cbl`, lines 2-5 and 188-192. |
