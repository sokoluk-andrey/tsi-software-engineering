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
