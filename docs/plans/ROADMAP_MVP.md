# MVP Roadmap — TaskMaster (7 weeks)

## Metadata

- **Timebox:** 7 weeks (7 sprints × 1 week)
- **Start:** Monday 12.04.2026
- **End:** Sunday 31.05.2026
- **Source of truth:** This roadmap + `docs/pm_approach.md`

---

## 1. Vision — what "MVP success" means

Deliver a deployed, stable, demonstrable version of TaskMaster that:

- Lets a registered user create, schedule, and complete tasks on a calendar.
- Tracks time on tasks via a built-in timer with notifications.
- Categorizes tasks across multiple levels and supports priorities.
- Awards achievements based on documented gamification rules.
- Supports English / Russian / Latvian end-to-end.
- Authenticates users via JWT with role-based access control.
- Has an admin panel for user and content moderation.
- Is accompanied by a documented architecture, API spec, and ADR trail sufficient for the course report.

The MVP is intentionally **vertical**: every layer (frontend, backend, DB, auth, i18n) works for a small set of features, rather than every feature being half-built.

---

## 2. Scope (in / out)

### In scope for MVP

- User registration, login, refresh, logout (JWT).
- Task CRUD with: title, description, due date, priority, category, attachments.
- Calendar view with drag-and-drop rescheduling.
- Built-in timer per task with start / pause / reset / complete and browser notifications on completion.
- Multi-level category system (parent / child).
- File attachments per task (size and type limits enforced).
- Achievement engine: at least **5 achievements** wired to user activity.
- i18n: EN / RU / LV across all user-facing screens.
- Responsive layout (desktop + mobile breakpoints).
- Admin panel: list users, ban/unban, view system stats.
- CI: lint + build + test on every PR.
- CD: deploy backend + frontend to a staging environment.

### Out of scope for MVP (deferred to future releases)

- Team / shared tasks (multi-user collaboration).
- Recurring tasks.
- External calendar sync (Google / Outlook).
- Mobile native app.
- Push notifications (only in-browser / in-app for MVP).
- AI-generated task suggestions.
- Premium tier / payments.
- Public API for third-party integrations.
- Audit log UI for admins (data is logged, but no UI).

---

## 3. Operating principles

- **Vertical slices over horizontal layers.** Ship one feature end-to-end before starting the next.
- **One ADR per architectural choice.** No silent decisions.
- **Tests written alongside code, not after.** Especially for services and the achievement engine.
- **No feature without i18n.** New strings are added to all three locales in the same PR.
- **Migrations are forward-only.** Once merged, a migration is never edited; corrections are new migrations.
- **The roadmap is a living document.** Sprint scope may shift; the vision and out-of-scope list may not, without an ADR.

---

## 4. Workstreams (epics)

1. **Foundation** — repo setup, CI, auth, base entities.
2. **Tasks & calendar** — CRUD, calendar view, drag-and-drop.
3. **Timer & notifications** — task timer, persistence, browser notifications.
4. **Categories & attachments** — category tree, file upload pipeline.
5. **Gamification** — achievement engine, rules, UI.
6. **Internationalization** — i18n infrastructure, EN/RU/LV catalogs.
7. **Admin & operations** — admin panel, monitoring, deployment.

---

## 5. Sprints

### Sprint 1 — Foundation

**Goal:** Repo, CI, and a deployable empty skeleton.

Deliverables:
- Vue 3 + Vite frontend scaffold; Spring Boot 3.4.3 backend scaffold.
- MySQL set up locally via Docker Compose.
- Flyway integrated; first migration creates `users` table.
- ESLint + Prettier (frontend), Checkstyle + Spotless (backend).
- GitHub Actions: lint + build + test on PR.
- ADR: stack choice (already drafted in PKM, formalized here).
- Deployable "Hello World" — backend `/health`, frontend landing page.

Acceptance: PR opened against an empty repo passes CI and a hello-world page is reachable on staging.

### Sprint 2 — Authentication & Users

**Goal:** A user can register, log in, refresh, and log out securely.

Deliverables:
- `User`, `Role`, `RefreshToken` entities + migrations.
- Spring Security configured with JWT (15-min access, 7-day refresh).
- Endpoints: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /me`.
- Frontend: register / login / logout flows with Pinia auth store.
- Password hashing: BCrypt.
- ADR: JWT strategy and refresh-token rotation.
- Integration tests with Testcontainers.

Acceptance: a new user can register, log in on the frontend, refresh their token, and reach a protected endpoint. Passwords never appear in logs or responses.

### Sprint 3 — Tasks & Calendar

**Goal:** A logged-in user can create, view, edit, and reschedule tasks on a calendar.

Deliverables:
- `Task` entity with title, description, due date, priority, status.
- CRUD endpoints with validation.
- Calendar view (week + month) using a Vue calendar library (decision via ADR).
- Drag-and-drop rescheduling that persists to backend.
- Optimistic UI on drag-and-drop with rollback on failure.
- Frontend tests for task store and calendar interactions.

Acceptance: tasks appear on the calendar, can be moved by drag-and-drop, and survive a page reload in the new position.

### Sprint 4 — Timer & Categories

**Goal:** Timer works end-to-end. Tasks are organized by multi-level categories.

Deliverables:
- `useTimer` composable (already prototyped — see PKM experiment 2026-04-26).
- `TimeEntry` entity recording start/end timestamps per task.
- Browser Notifications API integration (with permission flow).
- `Category` entity with self-referential parent/child relationship.
- Category CRUD endpoints + tree-view UI.
- Tasks linked to a category.
- ADR: storing time entries vs. computing on the fly.

Acceptance: a user can start a timer on a task, get a notification on stop, and see total time logged. A user can create nested categories and assign tasks to them.

### Sprint 5 — Attachments & Gamification

**Goal:** Files can be attached to tasks. Achievements work.

Deliverables:
- File upload pipeline (multipart) with type/size whitelist.
- Storage: local filesystem in dev, ready for S3-compatible swap (interface-based).
- `Achievement`, `UserAchievement` entities.
- Achievement engine — event-driven: services emit domain events, an `AchievementListener` evaluates rules.
- 5 achievements wired up (e.g., *first task created*, *first task completed*, *5-day streak*, *10 hours tracked*, *50 tasks completed*).
- Achievement notification UI (toast on unlock).
- ADR: achievement engine architecture (event-driven vs. polling).

Acceptance: uploading a 5MB PDF works, downloading it works, the file is virus-scan-ready (interface in place). Completing the first task unlocks an achievement and shows a toast.

### Sprint 6 — i18n & Admin Panel

**Goal:** Three languages everywhere. Admin can manage users.

Deliverables:
- Vue I18n integrated with full EN locale.
- Translation pass: RU and LV catalogs.
- Language switcher persists per-user in DB.
- Admin role enforced via `@PreAuthorize`.
- Admin panel: paginated user list, ban/unban, basic stats (users count, tasks count, attachments size).
- Audit log table populated on admin actions.

Acceptance: switching language updates the entire UI without reload. An admin can ban a user, who is then unable to log in. A non-admin gets `403` on any admin endpoint.

### Sprint 7 — Polish, Docs, Release

**Goal:** Cleanup, full documentation, deployable release tag.

Deliverables:
- Bug bash: at least 1 dedicated day on stability and edge cases.
- Mobile responsive review and fixes.
- Performance pass: N+1 query audit on hot paths (use `p6spy` in dev).
- Full API docs via Springdoc OpenAPI, exposed at `/swagger-ui`.
- `/docs/architecture/` populated: system overview, data model, API summary, deployment.
- README updated with setup, dev, deploy instructions.
- `v0.1.0-mvp` Git tag and GitHub release.

Acceptance: a fresh clone can be brought up locally in under 15 minutes following the README. The staging environment is running the tagged version. The course report has all the source material it needs.

---

## 6. Success metrics (MVP-level)

**M1: Functional completeness**
- All "in scope" items above are deployed and reachable on staging.
- A 30-minute end-to-end demo of the product can be performed without the developer touching the IDE.

**M2: Engineering quality**
- CI is green on `main` for at least the final two sprints.
- Backend unit + integration test coverage ≥ 70% on services.
- No `P0` or `P1` bugs open at release.

**M3: Documentation completeness**
- Every architectural choice has an ADR.
- API is documented via OpenAPI.
- README walks a new contributor from zero to running in under 15 minutes.
- Database schema is documented with an ER diagram.

**M4: Process traceability**
- Every sprint has a goal and a retro note.
- Every merged PR references an issue.
- The course report can be assembled by stitching together existing docs, not by writing from scratch.

---

## 7. Definition of Done (per sprint)

A sprint is Done when:

- All "in" items shipped or explicitly carried over (not silently dropped).
- All issues closed or moved to next sprint with reason.
- CI is green on `main`.
- Demo recording or screenshot exists for the new feature(s).

---

## 8. Risks & mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Day-job (Team Lead) absorbs more time than expected | High | High | Reserve 4–6 hours/week as a hard floor; cut scope before deadline |
| Unfamiliar territory in Spring Security at scale | Medium | Medium | Spike experiment in PKM repo before Sprint 2 |
| Achievement rules creep into product design rabbit hole | Medium | Medium | Lock 5 achievements via ADR by end of Sprint 4 |
| i18n quality (RU/LV) becomes time sink | Medium | Low | Use AI-assisted draft + native-speaker check (self for RU/LV) |
| Performance issues surface late | Low | High | Add `p6spy` in dev from Sprint 3; audit hot paths in Sprint 7 |
| Solo project burnout | Medium | High | Hard cap on hours; weekly retro is honest about exhaustion |

---

## 9. Future releases (post-MVP, out of MVP scope)

Roughly ordered by likely value:

- **v0.2 — Recurring tasks.** RRULE-based recurrence, modeled after iCalendar.
- **v0.3 — External calendar sync.** Two-way sync with Google Calendar.
- **v0.4 — Team workspaces.** Shared task lists, assignment.
- **v0.5 — Mobile PWA hardening.** Offline support, install prompt, service worker.
- **v0.6 — AI suggestions.** Natural-language task creation; smart scheduling proposals.
- **v1.0 — Public API + integrations.** Stable REST + webhook surface.

These are intentionally not detailed here — they exist only to make MVP scope decisions defensible (i.e., "feature X is *deferred*, not *forgotten*").

---

## 10. Appendix — key documents

- Project README — `../../README.md`
- AI agent rules — `../../AGENTS.md`
- Project management — `../pm_approach.md`
- ADRs — `../decisions/`
- Architecture — `../architecture/`
