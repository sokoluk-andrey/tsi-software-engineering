# Feature: Achievement Unlock Check

## Overview

When a user completes a task, the system needs to check if they have unlocked any new achievements. This is the rule that decides when to award an achievement.

## User Story

**As a** TaskMaster user,
**I want to** automatically receive achievements when I reach a milestone,
**so that** I feel rewarded for my progress and stay motivated to keep using the app.

## Acceptance Criteria

### AC1 — First task completed

**Given** a user has zero completed tasks and has not yet unlocked the "First Step" achievement,
**When** the user completes their first task,
**Then** the system returns the "First Step" achievement as newly unlocked.

### AC2 — Milestone reached, but already unlocked

**Given** a user has 50 completed tasks and has already unlocked the "Half Century" achievement,
**When** the user completes another task,
**Then** the system does not return "Half Century" again (achievements are only unlocked once).

### AC3 — Multiple achievements unlocked at the same time

**Given** a user has 9 completed tasks and the achievements "10 Tasks Done" (requires 10) and "Productive Week" (requires 5 tasks in 7 days) are both eligible,
**When** the user completes their 10th task within the same 7-day window,
**Then** the system returns both "10 Tasks Done" and "Productive Week" as newly unlocked.

## Inputs and outputs

The check function takes:
- The user's current stats (completed task count, streak days, hours tracked, etc.)
- The list of achievement rules (id, name, condition).
- The list of achievements the user already has.

The function returns:
- A list of achievement IDs that are newly unlocked in this call.

It does not write to the database, send notifications, or change any state. That is the caller's job.
