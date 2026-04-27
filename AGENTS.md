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
