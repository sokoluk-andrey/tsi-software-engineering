# AGENTS.md — TaskMaster Engineering Harness for AI Agents

This file is the entry point for **AI coding agents** (Claude, Cursor, GitHub Copilot, and others) working in this repository. Humans should start with `README.md`. Agents should start here.

TaskMaster is a gamified task management web application built as the course project for Software Engineering at TSI. It is a real product, not a toy — code committed here is expected to meet professional standards.

---

## 1. Project concept (in 30 seconds)

- **Product:** TaskMaster — a calendar-based task manager with built-in timers, multi-level categories, file attachments, and a gamified achievement system.
- **Users:** Students, knowledge workers, self-employed professionals managing multiple parallel tasks.
- **Languages:** Interface in English, Russian, and Latvian.
- **Frontend:** Vue.js 3 (Composition API, `<script setup>`), Vue Router, Vue I18n, Pinia for state.
- **Backend:** Spring Boot 3.4.3, Spring Security (JWT), Spring Data JPA.
- **Database:** MySQL 8.
- **Auth model:** JWT access tokens (15 min) + refresh tokens (7 days). Role-based access control (`USER`, `ADMIN`).

---

## 2. Non-negotiable rules

These rules are not suggestions. Violating them is a defect.

1. **Always read `/docs` before generating non-trivial code.** Specifically `/docs/pm_approach.md`, `/docs/plans/ROADMAP_MVP.md`, and any file under `/docs/architecture/` relevant to your task. If a doc you need does not exist, ask before guessing.
2. **Do not introduce new dependencies without an ADR.** Adding a library — frontend or backend — requires an entry under `/docs/decisions/`. If the ADR does not exist, stop and ask.
3. **Do not invent business rules.** If the requirement is not in `/docs` or in the issue you are working on, ask. Do not "make a sensible default" without flagging it.
4. **Do not modify database schema silently.** Schema changes go through Flyway migrations under `backend/src/main/resources/db/migration/`. Never edit an existing migration that has been merged.
5. **Do not bypass authentication or validation layers.** Every protected endpoint must go through Spring Security; every input DTO must have Bean Validation annotations.
6. **No secrets in code.** Configuration goes through `application.yml` profiles; secrets go through environment variables. Never commit `.env` files or hardcoded credentials.

---

## 3. Coding style

### Frontend (Vue 3)

- Use the **Composition API with `<script setup>`** exclusively. No Options API in new code.
- Component file naming: `PascalCase.vue` (e.g., `TaskTimer.vue`).
- Composables live in `src/composables/` and are named `useXxx.js` (e.g., `useTimer.js`).
- State management: **Pinia stores**, not `provide/inject` for cross-component state.
- Translations: every user-facing string goes through `vue-i18n` `t()`. No hardcoded strings in templates.
- Linting: ESLint + Prettier configurations in the repo are authoritative. Do not disable rules inline without justification in a comment.
- Tests: unit tests with **Vitest**, component tests with **Vue Test Utils**. Test files live next to source as `*.spec.js`.

### Backend (Spring Boot)

- Java 21. Use `record` for DTOs where possible.
- Package layout: `controller / service / repository / domain / dto / mapper / config / security`.
- Controllers are thin: validation, mapping, delegation. **No business logic in controllers.**
- Services contain business logic and are transactional (`@Transactional` at service level, not repository).
- Repositories extend `JpaRepository`. Custom queries use `@Query` with JPQL or named methods. **Avoid native SQL** unless there is a documented reason.
- Mappers: **MapStruct**, not manual mapping.
- Validation: `jakarta.validation` annotations on DTOs.
- Tests: JUnit 5 + Mockito for unit; Testcontainers + MySQL for integration.
- Logging: SLF4J. No `System.out.println`. Log at appropriate levels (`debug`, `info`, `warn`, `error`).

### Git & commits

- Branch naming: `feature/<short-slug>`, `fix/<short-slug>`, `chore/<short-slug>`, `docs/<short-slug>`.
- Commit format: **Conventional Commits** — `feat: ...`, `fix: ...`, `refactor: ...`, `docs: ...`, `test: ...`, `chore: ...`.
- One logical change per commit. Squash WIP commits before opening a PR.

---

## 4. What agents do well here, and what they don't

To use AI effectively on this project, the division of labor is documented in `/docs/pm_approach.md`. Short version:

**Agents are encouraged to do:**
- Generate boilerplate: DTOs, mappers, controllers, repository methods, basic CRUD scaffolding.
- Write unit tests for existing service methods.
- Write JSDoc / JavaDoc for documented public APIs.
- Translate i18n keys into Russian and Latvian (the human reviews for nuance).
- Suggest refactorings within a single file or composable.
- Draft documentation pages from existing code.

**Agents should NOT do without explicit human direction:**
- Make architectural choices (new modules, new layers, new patterns).
- Design the database schema or modify entity relationships.
- Write authentication / authorization logic.
- Decide what the achievement / gamification rules are (this is product logic, not code).
- Tune performance or add caching.
- Write or edit ADRs (`/docs/decisions/`) — these are human-authored records.

---

## 5. Where to find context (read these first)

- Project README — `README.md`
- Project management approach — `docs/pm_approach.md`
- MVP roadmap — `docs/plans/ROADMAP_MVP.md`
- Architecture (when written) — `docs/architecture/`
- Architectural Decision Records — `docs/decisions/`
- API specification — `docs/api/`
- Database schema — `docs/architecture/database.md`
- Coding conventions detail — `docs/conventions/`

If a task touches an area where the doc does not exist yet, **say so explicitly in your response** before writing code.

---

## 6. Default agent workflow

When given a task in this repository, an agent should:

1. **Locate the task** in `/docs/plans/ROADMAP_MVP.md` if possible. Identify which sprint/epic it belongs to.
2. **Read related docs** under `/docs/` — at minimum the architecture or convention doc covering the affected layer.
3. **Search the codebase** for existing patterns. Match them. Do not introduce new patterns unilaterally.
4. **Propose a plan** before generating code if the change touches more than one file or layer.
5. **Generate code** following the style rules in section 3.
6. **Generate tests** for new logic. Coverage target: 70%+ on services, 50%+ overall.
7. **Update docs** if the change affects an API, schema, or convention.

---

## 7. Escalate when uncertain

Stop and ask before proceeding if:

- The required behavior is not specified in the issue or in `/docs/`.
- A change would touch authentication, authorization, or financial / sensitive data flow.
- A change would require modifying a merged Flyway migration or a public API contract.
- A new external library is needed.
- The task seems to require a product decision (e.g., "what should the achievement threshold be?").

A good question is always cheaper than a wrong implementation.

---

## 8. Repository conventions (locked)

- **Internal links in docs:** Markdown relative links (GitHub-native).
- **Code line endings:** LF, enforced via `.gitattributes`.
- **Date format:** ISO 8601 (`YYYY-MM-DD`).
- **File naming for docs:** lowercase, kebab-case, no spaces.
- **Language of all docs and code comments:** English.

---
