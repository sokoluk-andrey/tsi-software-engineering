# Project Management Approach

## Metadata

- **Owner:** Project lead (solo developer + AI assistants)
- **Applies to:** TaskMaster development

---

## 1. Framework: Hybrid Scrum + Kanban

This is a solo project with AI assistance, not a multi-person team. A pure Scrum framework would be ceremonial overkill — there are no daily standups to hold with myself. A pure Kanban board, on the other hand, would lose the rhythm and goal-setting that make a course project deliverable on time.

I therefore use a **hybrid model**:

- **Scrum-style sprints (2 weeks)** define *what* gets done. Each sprint has a goal stated in `/docs/plans/ROADMAP_MVP.md`, a small set of issues mapped to it, and a retrospective note at the end.
- **Kanban-style flow (GitHub Projects board)** governs *how* the work moves day-to-day. Columns: `Backlog → Ready → In Progress → In Review → Done`. WIP limit on `In Progress`: **2 issues**.
- **Sprint planning** at the start of each sprint moves issues from `Backlog` to `Ready`. The sprint goal is a one-line commitment.
- **Sprint review and retro** at the end produces a short note in `/docs/retros/SPRINT-NN.md`: what shipped, what didn't, what to change.

This gives me the benefits of both: explicit goals and time-boxing from Scrum, continuous-flow visibility and WIP discipline from Kanban.

## 2. Division of responsibility — human vs AI

This project is built by a **hybrid team of one human + AI agents**. The human owns judgment, architecture, and product decisions. The AI owns leverage on volume work. The split is deliberate and documented so that the human does not lose ownership of the engineering output, and so that the AI is used where it is genuinely strongest.

### What I (the human) own — non-delegable

- **Product decisions.** What the achievement system rewards. What the priority levels mean. Which language goes first in i18n fallback. These are not coding tasks; they are choices about the product. AI is not authorized to make them.
- **Architecture.** Module boundaries, API contracts, layered structure, data model. Captured in ADRs under `/docs/decisions/`. AI may *propose* architectures in a task; only I accept them.
- **Database schema design.** Initial entity relationships, indexing strategy, migration timing. AI may write SQL or JPA entities once the schema is decided.
- **Security-sensitive code.** Authentication flow, JWT handling, role checks, password hashing, input sanitization at trust boundaries. AI may help with boilerplate around it but does not get a free hand.
- **Code review.** Every PR — including those mostly written by AI — is read by me end to end before merge. The author of a line of code is whoever **understood and accepted it**, not whoever typed it.
- **ADRs and design docs.** Records of *why* are written by me, in my voice. AI may help format or proofread but does not author them.
- **UX and visual design decisions.** Component layout, color choices, accessibility decisions.

### What AI agents do — delegated, with review

- **Boilerplate generation.** DTOs, mappers, repository method stubs, basic CRUD controllers and services, OpenAPI annotations.
- **Unit tests.** Tests for existing, already-designed service methods. AI is good at covering happy paths and obvious edge cases; I add the non-obvious ones.
- **Documentation drafts.** First-pass drafts of API docs, README sections, JSDoc/JavaDoc comments.
- **i18n translation drafts.** Russian and Latvian translations for English keys. I review for nuance, especially in motivational/gamification copy where literal translation often misses tone.
- **Refactoring within scope.** Renaming, extracting methods, reorganizing imports, applying a style rule across a file.
- **Type and schema scaffolding.** Generating TypeScript types from OpenAPI, generating MapStruct mapper interfaces.
- **Investigation and explanation.** "Walk me through what this Spring Security filter chain is doing" — AI is excellent as a learning partner.

### Hard line — what AI never does

- Merge a PR.
- Edit a Flyway migration that has already been merged.
- Add a runtime dependency without an ADR.
- Modify code under `backend/src/main/java/.../security/` without explicit instruction.
- Write or modify the achievement reward formulas (this is product logic).

---

## 3. Working with AI in practice — how a typical task flows

A typical issue in this project flows like this:

1. **I write the issue** in human language: what is needed, why, acceptance criteria, links to relevant docs.
2. **I open a feature branch** and either write the design or, if the design is clear, ask AI to draft the implementation.
3. **AI generates the first pass.** I read it carefully — not skim — and either accept, modify, or rewrite.
4. **I add the parts AI cannot or should not write:** ADR if architectural, security review if relevant, the non-obvious tests.
5. **I open a PR against `main`.** Even though I am the only human reviewer, the PR exists so that:
   - The diff is visible and self-reviewable.
   - The CI pipeline runs.
   - The history reads like a real team's history (which the course report will benefit from).
6. **Self-review.** I read the entire diff one more time, ideally a few hours after writing it.
7. **Merge** with a Conventional Commits squash message.

The principle behind this flow: **AI is a force multiplier on volume, not a substitute for understanding.** If I would not be able to defend a piece of code in a viva exam, it does not get merged.

---

## 4. Definition of Done (per issue)

An issue is Done only when **all** of the following hold:

- [ ] Code is merged to `main`.
- [ ] Unit tests exist for new logic, and CI is green.
- [ ] Acceptance criteria from the issue are met.
- [ ] Relevant docs in `/docs/` are updated (architecture, API, conventions).
- [ ] If an architectural decision was made: an ADR exists in `/docs/decisions/`.
- [ ] If schema changed: a Flyway migration exists and is tested.
- [ ] If user-facing strings changed: translations exist for EN, RU, LV.
- [ ] No `TODO` comments without an associated issue link.

---

## 5. Why this approach (briefly)

A solo course project is the wrong place to demonstrate full enterprise process. It is also the wrong place to ship cowboy code with no traceability. The middle path is to run a **scaled-down version of how real teams work**: explicit sprints, a real board, real PRs, real ADRs, real reviews. The fact that the "team" is one human and several AI agents does not change the discipline — it changes the tempo.

The course report at the end of the semester benefits directly from this: every architectural choice, every sprint outcome, every retro and every ADR is already written down. The report becomes an act of synthesis, not an act of reconstruction.
