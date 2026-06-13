# TaskMaster — Final Course Report

## Project Overview

**TaskMaster** is a gamified task management web application that combines productivity tools with motivation mechanics to help users plan, execute and track their daily work more effectively. It was developed as the course project for the Software Engineering course at TSI.

---

## The Problem

Most task management tools fall into one of two camps: they are either powerful but tedious to use or simple but lacking the features needed for serious planning. In both cases, users tend to lose motivation over time — tasks pile up, deadlines slip and there is no real feedback loop that rewards consistent execution. People need a system that not only organizes their work but also keeps them engaged in actually doing it.

---

## The User

TaskMaster targets students, knowledge workers, and self-employed professionals who manage multiple parallel tasks across personal and professional life and who benefit from visual planning, time tracking, and motivational feedback. The multilingual interface (English, Russian, Latvian) makes it accessible to users in the Baltic and Eastern European regions.

---

## Core Features

- **Calendar-based task management** with drag-and-drop support for fast rescheduling
- **Priority and multi-level category system** for flexible organization
- **Built-in timers with notifications** to track time spent on each task
- **Achievement system** that rewards productivity and consistency through gamification
- **File attachments** linked directly to tasks
- **Multilingual support** (English, Russian, Latvian)
- **Responsive interface** that works on desktop and mobile
- **Administrative panel** for user and content management
- **Secure authentication** via JWT and role-based access control

---

## Technology Stack

- **Frontend:** Vue.js 3 (Composition API, `<script setup>`), Vue Router, Vue I18n, Pinia
- **Backend:** Spring Boot 3.4.3, Spring Security, Spring Data JPA
- **Database:** MySQL 8
- **Auth:** JWT access tokens (15 min) + refresh tokens (7 days), role-based access control (`USER`, `ADMIN`)
- **Testing:** JUnit 5, AssertJ
- **CI:** GitHub Actions — lint, build, test on every PR

---

## Project Management

The project was managed using a **hybrid Scrum + Kanban approach**, documented in `docs/pm_approach.md`.

Work was organized into 2-week sprints (7 sprints total) tracked on a GitHub Projects board. Each sprint had a single stated goal. A short retrospective was written at the end of each sprint in `docs/retros/`.

The project was built as a **hybrid team of one human developer and AI assistants** (Claude, Cursor). The division of responsibility was explicitly documented and enforced:

- **Human-owned (non-delegable):** architecture, database schema, security code, product decisions (achievement rules, gamification design), code review, ADRs.
- **AI-delegated:** boilerplate generation, unit tests, documentation drafts, translation passes, within-file refactoring.

The rule throughout: if the developer could not explain a line of code in an exam, it did not get merged.

---

## Engineering Practices

### Architecture Decision Records

Every non-trivial design choice was captured as an ADR in `docs/decisions/`. ADRs are immutable once accepted. The ADR trail covers:

- **ADR-001:** Stack selection (Vue 3 + Spring Boot + MySQL)
- **ADR-002:** JWT authentication strategy and refresh token rotation
- **ADR-003:** Strategy pattern for the achievement engine

### AI Agent Contract (`AGENTS.md`)

The repository root contains an `AGENTS.md` file that defines how AI assistants interact with this codebase. Key rules include: always read `/docs` before generating non-trivial code, never introduce dependencies without an ADR, never bypass authentication or validation layers, never write or edit Flyway migrations that have already been merged.

This file was the primary tool for preventing AI hallucination and spaghetti code.

### Design Contract (`docs/DESIGN.md`)

A strict `DESIGN.md` file was written before any UI generation was attempted. It defines the color palette (specific hex codes, always referenced via CSS custom properties), typography scale, 8-point spacing grid, component rules, and explicit accessibility requirements. It also contains a dedicated "What NOT to do" section — enforcing negative rules (no Tailwind, no box-shadow on dark backgrounds, no hardcoded strings) proved more effective than positive guidelines alone.

---

## Feature Deep Dive: The Achievement Engine

### Why this feature

The achievement engine was selected as the focus of the engineering deep-dive because it is the most characteristic part of TaskMaster. It is what differentiates the product from a plain to-do list, and it is the feature most likely to grow — more achievements will be added over the product's lifetime.

It was also the riskiest feature to get wrong architecturally. A naive implementation (one long method with hardcoded `if/else` chains) would become unmanageable fast.

### BDD Requirements

Before writing any code, the feature was specified using Behavior-Driven Development format. The specification lives in `docs/requirements/feature_achievement_unlock.md` and defines:

- **User story:** "As a TaskMaster user, I want to automatically receive achievements when I reach a milestone, so that I feel rewarded for my progress and stay motivated to keep using the app."
- **AC1:** First task completion unlocks the "First Step" achievement.
- **AC2:** Already-unlocked achievements are never returned again.
- **AC3:** Multiple achievements can unlock simultaneously in a single check.

The BDD spec also formally defines the function contract: inputs (user stats snapshot, list of rules, set of already-unlocked IDs), output (list of newly unlocked IDs), and the hard constraint that the function must produce no side effects.

### Architecture (Strategy Pattern)

The achievement engine is built around the **Strategy pattern** (Gang of Four, behavioral group), documented in `docs/decisions/ADR-003-strategy-pattern-for-achievements.md`.

The structure lives in `backend/src/main/java/com/example/demo/service/achievement/`:

**Why Strategy and not alternatives:**

| Pattern considered | Verdict |
|---|---|
| One `if/else` chain | Rejected — breaks Open/Closed Principle, unmanageable past ~5 rules |
| Chain of Responsibility | Rejected — rules are independent, no ordering needed, overkill |
| Observer | Wrong layer — right for notifying about unlocks, wrong for deciding them |
| Rule engine (Drools) | Rejected — heavy dependency, new DSL, overkill for 15 rules |
| **Strategy** | **Selected** — one class per rule, Spring auto-discovers, extending = adding a file |

**The key design decision:** `AchievementChecker` is a **pure function**. It accepts a `UserStats` snapshot and the set of already-unlocked IDs, and returns a list of newly unlocked IDs. It makes no database calls, sends no notifications, and mutates no state. The same input always returns the same output.

`UserStatsBuilder` is the only class allowed to read from repositories. It builds the `UserStats` record once per check. This means the entire rule evaluation phase costs exactly one database read pass regardless of how many achievements exist.

Side effects (persisting new `Achievement` rows) happen in `AchievementService` after the checker returns, completely outside the pure function.


**Adding a new achievement in this architecture:**
1. Add the definition to `AchievementService.initializeAchievementDefinitions()`.
2. Create a new `@Component` class in `rules/` (typically 10 lines).
3. Add a unit test.

That is it. `AchievementService`, `TaskService`, and the frontend are not touched.

### Bug found during implementation

While implementing the Strategy module, a real defect was discovered: the `consistency` achievement ("Complete tasks for 30 days in a row") was defined in `initializeAchievementDefinitions()` and listed in `frontend/src/services/achievements.js`, making it visible to users as a permanently-locked achievement. However, no logic ever called `unlockAchievement(user, "consistency")` — the rule was simply never implemented. The achievement had been silently broken since it was added.

The Strategy refactor made this impossible to miss: every defined achievement should have a corresponding rule class, and the absence of one is immediately visible. The bug was not a product of the refactor — it pre-dated it. The forced symmetry of the pattern made it detectable.

The `consistency` achievement was left out of the new module on purpose. It is deferred to a future sprint when proper streak tracking is implemented. The decision is documented.

### Unit tests

`AchievementCheckerTest` covers:
- Single rule unlocks correctly.
- Already-unlocked achievements are not returned again (AC2 from BDD).
- Multiple achievements unlock simultaneously in one call (AC3 from BDD).
- Time-of-day rules respect their hour windows (e.g., `EarlyBirdRule` only unlocks before 6 AM).
- Input arguments are not mutated.
- Empty result when no rules are met.

All tests run without a database or Spring context. The pure-function design makes mocking unnecessary.

---

## UI: Spec-Driven Development

The achievement feature UI was redesigned using **Spec-Driven Development (SDD)**. A `docs/DESIGN.md` file was written first as a formal design contract, then used as the AI prompt context for all UI generation.

The design contract defines:
- **Color palette:** 17 named CSS custom properties covering backgrounds, text, accents, category colors, and borders. All referenced via `var(--name)` — no raw hex codes in component CSS.
- **Typography:** A 6-step type scale from 12px (captions) to 32px (page titles), with weight rules.
- **Spacing:** An 8-point grid with 7 allowed values. No values outside the scale.
- **Component rules:** Buttons, cards, achievement cards, notification toasts, forms, empty states — each with explicit rules.
- **Accessibility:** WCAG AA contrast requirements, `:focus-visible` rules, `aria-live` for notifications, no color as the only carrier of meaning.
- **What NOT to do:** No Tailwind, no box-shadow on dark backgrounds, no hardcoded strings, no two primary buttons next to each other, no light mode.

**Key finding from the SDD experiment:** the "What NOT to do" section was more effective than the positive rules. The AI followed specific bans reliably ("never use box-shadow") but followed positive guidelines inconsistently ("use the 8-point scale"). Negative constraints are a better AI control mechanism than positive ones.

The SDD experiment and its full results (two-attempt comparison, accessibility findings, backend connection notes) are documented in the PKM repository at `experiments/EXP-2026-05-16-spec-driven-ui.md`.

---

## PKM Repository

Alongside the product repository, a **Cognitive PKM (Personal Knowledge Management)** repository was maintained throughout the course. It served as the substrate for all engineering decisions, experiments, and learning documented during development.

### What the PKM contains

**`decisions/`** — Architectural Decision Records written before making choices, not after. Format: Context → Options → Decision → Consequences.

**`notes/`** — Durable, atomic notes on technical concepts encountered during the project (Composition API vs Options API, JWT vs session auth, what makes a good ADR, the Strategy pattern rationale).

**`experiments/`** — Three formal experiments conducted during the course:

| Experiment | Date | Finding |
|---|---|---|
| `EXP-2026-04-28-pure-function.md` | 2026-04-28 | Vague prompts produce code with DB side effects by default. Explicit pure-function constraints changed the output fundamentally. |
| `EXP-2026-05-09-pattern-implementation.md` | 2026-05-09 | AI applies design patterns shallowly without explicit constraints. "Use Strategy" alone produced DB calls inside rule classes. Adding "no DB in rules" solved it. |
| `EXP-2026-05-16-spec-driven-ui.md` | 2026-05-16 | DESIGN.md with a "What NOT to do" section cut manual corrections from many to three. Backend connection was a non-issue because the Strategy refactor preserved the public API contract. |

**`literature/`** — Notes from the course lectures and re-read technical books, captured in a fixed format that separates "what the source says" from "what I think about it."

**`journal/`** — Dated entries written the same day, capturing friction, surprises, and open questions while still fresh.

**`questions/`** — Open questions tracked across the course, promoted to `notes/` when resolved. Includes "What does Senior actually mean beyond the job title?" and "Why do experienced engineers distrust ORMs?"

### The PKM as a development tool

The most useful thing the PKM did was slow down decisions. Writing an ADR before implementing a feature forces articulation of the alternatives, which regularly revealed options that weren't initially obvious. The `consistency` bug was found not through testing but through the structural pressure of the Strategy pattern — but it was the PKM's ADR process that made "what achievements does this cover?" an explicit question before the first line of code was written.

The PKM is also the source material for this report. Instead of reconstructing decisions from memory, every section above has a corresponding document that was written at the time the decision was made.

---

## What Was Learned

**On AI-Native engineering:**

The course's central claim — that unstructured AI prompts produce unmanageable code, and that strict contracts (BDD, Mermaid, AGENTS.md, DESIGN.md) produce usable output — held up in practice across all three experiments. The pattern was consistent: vague prompts produced plausible-looking code with hidden defects (DB calls in pure functions, hardcoded strings in i18n apps, box-shadows on dark UIs). Specific constraints produced correct code with minimal correction.

The most effective type of constraint was always a *ban*, not a *guideline*. "Never put DB calls inside rule classes" worked better than "keep rules stateless." This is a finding worth carrying beyond this course.

**On design patterns:**

The Strategy pattern did not add complexity to the achievement engine. It removed it. The original service was 392 lines of procedural code that could not be tested without a database. The refactored version has 15 files of ~10 lines each, a pure-function checker, and a test suite that runs in under a second with no mocks. The pattern reduced cognitive load, not increased it.

This would not have been true at 3 rules. With 15 active rules and a documented need for more, Strategy was the right tool.

**On being a Team Lead during a course project:**

The most surprising thing about this semester was how much the PKM habit and the ADR discipline transferred to work. Writing decisions down before implementing them is the same muscle whether the team is a solo developer + AI or three developers at ClarityLabs. The difference is that in a team, the ADR trail protects the other developers from your past self. Writing it for a solo project is practice.
