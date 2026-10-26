# Design

## Context

See `proposal.md` — Why. Current state: gateway routing, rate limiting, and the logging profiles/buffer/admin endpoints exist and run in production, described today only by prose in `AGENTS.md`. `openspec/specs/` has five capabilities (auth, question-bank, folders-sharing, quiz-authoring, quiz-runtime); none covers these areas. This change adds spec deltas only — no code is written, so `config.yaml`'s design rules about new endpoints and the one-active-quiz-per-bot-token invariant are unaffected: no endpoints are added or changed, and no quiz/bot behavior is touched.

## Goals / Non-Goals

**Goals:**
- Two new capability specs whose every requirement matches the running code at archive time.
- Requirement granularity consistent with the existing five specs (one behavior per requirement, operator-observable values kept in scenarios).

**Non-Goals:**
- Changing any gateway or logging behavior, configuration defaults, or deployment files.
- Specifying health checks, metrics, or tracing (nothing exists to specify).
- Deciding whether informal `AGENTS.md` prose about these areas should shrink after archive — that sweep happens at archive per the archive guidance, not here.

## Decisions

### Code is the source of truth; specs are verified against it before archive
The baseline five specs were seeded without verification and accumulated drift. Each drafted requirement here is traced to the implementing configuration or code during apply, with evidence recorded in the change. A requirement the code cannot substantiate is removed from the delta (or corrected to the real value) rather than specified aspirationally. Alternative considered: specify intended behavior and fix code to match — rejected because this change explicitly promises no behavior changes.

### Two capabilities, not one `operations` catch-all
`api-gateway` (traffic path: routing, rate limiting, transparency) and `logging` (operator visibility: profiles, retention, buffer, admin access) evolve independently and have different audiences. A combined capability would violate the "avoid broad catch-all capabilities" guidance in the spec-driven schema.

### Observable values live in the specs
Rate-limit rates, the over-limit status code, retention window, buffer capacity, and the admin-only rule are externally observable, so they belong in requirements/scenarios (like the 6-hour/hourly values in quiz-runtime). Implementation identifiers (filter classes, appender classes, encoder libraries) stay out of the specs and are only referenced in this design and the tasks' verification evidence.

### Authorization implications
No new endpoints. The admin logs endpoints already exist and are already guarded; the `logging` spec records that guard as behavior (administrator-only, everyone else rejected) on the main-service side, complementing — not modifying — the `auth` capability's existing administrator-authorization requirement, which continues to own `/auth/admin/**`.

## Risks / Trade-offs

- [Verification finds prose/behavior mismatches (e.g. a documented value differs from configuration)] → Record the real value in the spec, note the discrepancy in `tasks.md`, and fix the stale `AGENTS.md` prose in the same archive (per archive guidance). Do not change code.
- [Specs pin exact rate-limit/retention numbers, so future tuning touches the spec] → Accepted: those numbers are operator-visible contracts, same as existing pinned values (24h token life, 2 MB image threshold). Deliberate tuning becomes a small MODIFIED delta, which is the point.
- [Gateway behavior partly depends on Redis availability] → The spec covers only what the gateway observably does; Redis provisioning remains deployment concern documented in `AGENTS.md`.
