# CardDemo Phase 1 Evidence Index

## Purpose and scope

This page is the landing point for the generated Phase 0 and Phase A analysis of the checked-in CardDemo user-security slice. It indexes the analysis rather than replacing it: follow the linked artifacts for the complete evidence, source citations, and stated exclusions.

The source set contains five COBOL programs: `COSGN00C.cbl`, `COADM01C.cbl`, `COUSR01C.cbl`, `COUSR02C.cbl`, and `COUSR03C.cbl`. The available evidence covers sign-on, administration-menu behavior, and user-security record maintenance. See the [codebase wiki scope](codebase-wiki.md#scope-and-evidence) and [screen-flow summary](screen-flow.md#summary).

## Phase status

| Phase | Status | Output / boundary |
|---|---|---|
| Phase 0 — codebase orientation | Complete | [Codebase wiki](codebase-wiki.md) documents the supplied programs, CICS interaction pattern, dependencies, data overview, workflows, and modernization handoff priorities. |
| Phase A — business-analysis artifacts | Complete for the five supplied COBOL programs | Entity, glossary, business-rule, screen-flow, user-story, validation-rule, and validation-dependency outputs are indexed below. Each artifact states its own evidence boundary. |
| Phase B | **Not started** | No Phase B implementation, design, API, database, frontend, migration, or test artifact is represented by this index. Phase A findings must be reviewed and the absent dependencies obtained before any later-phase work begins. |

## Artifact library

### Phase 0

| Artifact | What it provides |
|---|---|
| [Codebase wiki](codebase-wiki.md) | Orientation to the five programs, verified architecture and workflows, shared dependencies, `USRSEC` data use, and modernization priorities. Start with its [executive overview](codebase-wiki.md#executive-overview), then review [shared dependencies and limitations](codebase-wiki.md#shared-dependencies-and-limitations). |

### Phase A

| Artifact | What it provides |
|---|---|
| [Business entities](business-entities.md) | One evidenced persisted entity: **Application User**; visible attributes, use context, and exclusions. See [ENTITY-001](business-entities.md#entity-001-application-user). |
| [Business glossary](business-glossary.md) | Definitions for the user-security concepts evidenced in the source, including the administrator condition, non-administrator path, user type, and sign-on. |
| [Business rules catalog](business-rules-catalog.md) | One extracted business decision: route an authenticated user according to assigned user type. See [RULE-DECISION-001](business-rules-catalog.md#rule-decision-001-direct-a-signed-in-user-according-to-assigned-user-type). |
| [Screen-flow guide](screen-flow.md) | Five map-backed screen analyses, fields referenced in code, navigation conditions, and the documented end-to-end flow. Start at [Screen Inventory](screen-flow.md#screen-inventory) and [Complete Application Flow](screen-flow.md#complete-application-flow). |
| [User stories](user-stories.md) | Seven source-based stories covering sign-on, exit, administration-menu selection, and add/update/delete user administration. |
| [Validation rules catalog](validation-rules.md) | Twenty input, credential, uniqueness, existence, and update-integrity checks, grouped by workflow. |
| [Validation dependency diagram](validation-dependencies.svg) | Visual companion to the validation catalog; solid links show ordered validation, while dashed links show shared fields or prerequisites. |

## Concise findings

- The supplied slice manages an `USRSEC` user-security record: sign-on reads it, and the three user-maintenance programs create, update, and delete it. The detailed evidence is in the [wiki data overview](codebase-wiki.md#data-overview) and [entity catalog](business-entities.md#entity-001-application-user).
- Successful credentials are routed by stored user type: when the `CDEMO-USRTYP-ADMIN` condition is true, control transfers to `COADM01C`; otherwise it transfers to the external `COMEN01C` program, whose purpose and source are not supplied. The encoded administrator value remains an open dependency on absent copybook `COCOM01Y`. See [RULE-DECISION-001](business-rules-catalog.md#rule-decision-001-direct-a-signed-in-user-according-to-assigned-user-type) and [SCREEN-001](screen-flow.md#screen-001-sign-on-cosgn0a).
- The verified screen scope is five map-backed interactions: sign-on, administration menu, add user, update user, and delete user. Refer to the [screen inventory](screen-flow.md#screen-inventory).
- The administration menu dispatches to targets provided by configuration, not by the available menu program text. Add, update, and delete programs exist in the extract, but their menu-option mapping is intentionally unverified. See [administration-menu flow interpretation](screen-flow.md#flow-interpretation).
- The business-rules analysis found no amount, fee, balance, rate, threshold, or other calculation rule in this source set; it identified one role-based routing decision. See [business-rules result](business-rules-catalog.md#evidence-based-result).
- The validation catalog records required-field, credential, user-existence, unique-ID, valid-menu-option, and changed-update checks. Exact rule definitions and trigger conditions are in the [validation catalog](validation-rules.md).

## Source-to-artifact traceability

The matrix below maps each source program to the capabilities analyzed and the detailed generated artifacts. Links lead to the relevant program or artifact section; they do not imply capabilities beyond the cited source.

| Source program | Evidenced capability | Detailed analysis artifacts |
|---|---|---|
| `COSGN00C.cbl` | Sign-on; required credentials; user lookup and password comparison; route administrators to the admin menu and non-administrators to the absent `COMEN01C`; PF3 exit. | [Wiki: sign-on](codebase-wiki.md#cosgn00c--sign-on-cc00) · [Screen: SCREEN-001](screen-flow.md#screen-001-sign-on-cosgn0a) · [Stories: 001–003](user-stories.md#sign-on-screen--cosgn00c) · [Business decision](business-rules-catalog.md#rule-decision-001-direct-a-signed-in-user-according-to-assigned-user-type) · [Validation: sign-on](validation-rules.md#sign-on-and-credentials) |
| `COADM01C.cbl` | Present an administrator menu; validate a configured option; route to a configured non-`DUMMY` target or show a coming-soon result; return to sign-on. | [Wiki: administrator menu](codebase-wiki.md#coadm01c--administrator-menu-ca00) · [Screen: SCREEN-002](screen-flow.md#screen-002-administration-menu-coadm1a) · [Story 004](user-stories.md#story-004-select-an-available-administration-menu-option) · [Validation: menu](validation-rules.md#administrative-menu) |
| `COUSR01C.cbl` | Add a user-security record; require first name, last name, user ID, password, and user type; report duplicate user ID or write result. | [Wiki: add user](codebase-wiki.md#cousr01c--add-user-cu01) · [Screen: SCREEN-003](screen-flow.md#screen-003-add-user-cousr1a) · [Story 005](user-stories.md#story-005-add-a-regular-or-administrator-user) · [Entity usage](business-entities.md#entity-001-application-user) · [Validation: user creation](validation-rules.md#user-creation) |
| `COUSR02C.cbl` | Look up a user; display and save changed user details; reject incomplete or unchanged submissions; return according to context or the administration menu. | [Wiki: update user](codebase-wiki.md#cousr02c--look-up-and-update-user-cu02) · [Screen: SCREEN-004](screen-flow.md#screen-004-update-user-cousr2a) · [Story 006](user-stories.md#story-006-look-up-and-update-an-existing-user) · [Entity usage](business-entities.md#entity-001-application-user) · [Validation: lookup and update](validation-rules.md#user-lookup-and-update) |
| `COUSR03C.cbl` | Look up a user for review; delete after PF5; report missing/deletion outcomes; return according to context or the administration menu. | [Wiki: delete user](codebase-wiki.md#cousr03c--look-up-and-delete-user-cu03) · [Screen: SCREEN-005](screen-flow.md#screen-005-delete-user-cousr3a) · [Story 007](user-stories.md#story-007-look-up-and-delete-an-existing-user) · [Entity usage](business-entities.md#entity-001-application-user) · [Validation: user deletion](validation-rules.md#user-deletion) |

## Evidence limitations and required follow-up

The analysis is intentionally limited to what is present in the checkout. The [full dependency table](codebase-wiki.md#shared-dependencies-and-limitations) and [screen-flow evidence boundary](screen-flow.md#evidence-boundary-and-confidence) contain the detailed citations.

| Missing or unavailable item | Consequence for this analysis | Review follow-up |
|---|---|---|
| `CSUSR01Y` user-record copybook | Actual `USRSEC` record layout, field sizes, key format, optionality, and password storage policy cannot be confirmed. The entity catalog lists only fields visibly used by the COBOL. | Obtain the copybook before defining a target schema, data contract, or credential-handling approach. See [entity evidence boundary](business-entities.md#scope-and-evidence-boundary). |
| BMS map sources/generated map copybooks (`COSGN00`, `COADM01`, `COUSR01`, `COUSR02`, `COUSR03`) | Visual layout, labels, field attributes, lengths, editability, mandatory BMS attributes, PF-key legends, and complete field inventories cannot be reconstructed. | Obtain the BMS sources before treating screen-field descriptions as an exact UI specification. See [screen-flow evidence boundary](screen-flow.md#evidence-boundary-and-confidence). |
| `COCOM01Y` common-area copybook | Shared navigation/session layout and exact field semantics are unavailable. | Obtain it before defining session, navigation, or integration contracts. |
| `COADM02Y` menu configuration | Exact menu labels, option count, configured targets, and the mapping from menu options to add/update/delete are not verified. | Obtain the configuration before asserting reachable admin navigation. |
| `COMEN01C` | The regular-user destination and its capabilities are out of scope. | Obtain its source and dependencies to extend the user journey beyond sign-on. |
| Title/date/message copybooks and CICS-supplied definitions | Exact shared text/constants and environment symbols are not represented as business facts. | Treat these as implementation/environment dependencies, not missing business requirements. |

## Recommended review order

1. Read the [codebase wiki executive overview](codebase-wiki.md#executive-overview) and [dependency limitations](codebase-wiki.md#shared-dependencies-and-limitations) to establish the evidence boundary.
2. Review the [screen-flow inventory and complete flow](screen-flow.md#screen-inventory) to validate the user journey and its explicit unknowns.
3. Review the [Application User entity](business-entities.md#entity-001-application-user) and [glossary](business-glossary.md) to align terminology and visible data scope.
4. Review the [user stories](user-stories.md) and [validation catalog](validation-rules.md), using the [validation dependency diagram](validation-dependencies.svg) as a navigation aid.
5. Confirm the [role-routing decision](business-rules-catalog.md#rule-decision-001-direct-a-signed-in-user-according-to-assigned-user-type) and the catalog’s stated exclusions.
6. Obtain and analyze the missing copybooks, BMS resources, menu configuration, and `COMEN01C` before beginning any Phase B work.

## Explicit Phase B boundary

**Phase B was not started.** This directory currently provides Phase 0 orientation and Phase A evidence extraction only. It does not authorize or contain a target architecture, database model, API contract, frontend design, code conversion, implementation plan, test suite, security remediation, or migration outcome. Any subsequent phase must use the cited artifacts together with the currently missing source dependencies, rather than treating this index as a complete application specification.
