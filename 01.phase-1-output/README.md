# Phase 1 discovery — bounded CardDemo identity and user administration

**Baseline:** `598d70c0564d51dfd546abb40beb2f63629e72c1` (the checked-out `vorflux/migrate-user-admin` baseline).  
**Analysis date:** 2026-09-02.  
**Scope:** only the five tracked COBOL programs: `COSGN00C`, `COADM01C`, `COUSR01C`, `COUSR02C`, and `COUSR03C` (1,601 physical source lines total). This is not a full CardDemo specification.

## Deliverables

| Artifact | Purpose |
|---|---|
| [codebase-wiki.md](codebase-wiki.md) | Source inventory, dependencies, program and data-flow orientation |
| [business-entities.md](business-entities.md) | Source-backed provisional User entity catalog |
| [business-rules.md](business-rules.md), [business-glossary.md](business-glossary.md) | Business decisions separated from input validation |
| [validation-rules.md](validation-rules.md), [validation-dependencies.svg](validation-dependencies.svg) | Ordered input validations and dependency diagram |
| [screen-flow.md](screen-flow.md) | Five observed terminal-screen workflows and navigation |
| [user-stories.md](user-stories.md) | Source-derived, testable bounded-slice capabilities |
| [evidence-matrix.md](evidence-matrix.md) | Confirmed/inferred/unknown evidence ledger and recovery blockers |
| [characterization-matrix.md](characterization-matrix.md) | Observable branches, setup, actions, outcomes, and routes |
| [modern-api-domain-contract.md](modern-api-domain-contract.md) | Bounded API/domain contract and legacy-to-HTTP mappings |
| [assumptions-deviations.md](assumptions-deviations.md) | Explicit modern assumptions and intentional deviations |
| [migration-coverage-matrix.md](migration-coverage-matrix.md) | Source-to-modern coverage, intentional deviations, blockers, and test references |

## Evidence vocabulary

- **Confirmed** — directly expressed in one of the five sources; a citation identifies the exact lines.
- **Inferred** — a reasonable migration interpretation, never a frozen legacy requirement.
- **Unknown** — not recoverable from the repository. It must be resolved before claiming record, screen, or full-application fidelity.

Source citations use `file:line-line` and resolve against the baseline above. Legacy execution and data were unavailable, so all behavioral expectations are **source-derived and unverified by a runnable oracle**.

## Scope boundary

This documentation preserves the observable sign-on and user-administration behavior only. It excludes card/account/transaction functions, the regular-user experience beyond its handoff to absent `COMEN01C`, labels and destinations supplied by absent `COADM02Y`, exact screen geometry, exact record layout, and any missing application program.
