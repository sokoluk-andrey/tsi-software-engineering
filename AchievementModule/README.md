# Achievement Module

This module decides which achievements a user has unlocked when they complete a task.

It replaces the rule logic that previously lived inside `AchievementService` as 7 private `check*` methods. The pattern decision and rationale are recorded in ADR-003: https://github.com/sokoluk-andrey/tsi-software-engineering-pkm/blob/main/decisions/ADR-003-strategy-pattern-for-achievements.md

## Pattern used: Strategy

The module is built around the **Strategy** pattern (Gang of Four, behavioral group).

### Why Strategy fits

Every achievement is a different rule, but every rule answers the same question: *given the user's stats, is this achievement unlocked?* Same shape, different logic. That is the textbook case for Strategy.

### Why not other patterns

- **Big `if/else` chain (the current code).** Works for 5 rules. Becomes unmanageable past that. Every new achievement edits a central method — breaks the Open/Closed Principle.
- **Chain of Responsibility.** Rules would pass the request along. Overkill — rules are independent and don't need ordering.
- **Observer.** Right pattern for *notifying* about unlocks. The frontend already uses it via `achievement-unlocked` events. Wrong pattern for *deciding* unlocks.
- **Command.** Encapsulates an action. Achievements are conditions, not actions. Wrong fit.
- **Rule engine library (Drools).** Massive overkill for 15 rules.

## Structure
service/achievement/
├── AchievementRule.java      # Strategy interface
├── AchievementChecker.java   # Context — runs all strategies (pure function)
├── UserStats.java            # Immutable input record
├── UserStatsBuilder.java     # The ONE class allowed to read repos
└── rules/                    # Concrete strategies (one per achievement)
├── BeginnerRule.java
├── TimeMasterRule.java
├── ProductiveDayRule.java
├── SuperProductiveDayRule.java
├── EarlyBirdRule.java
├── NightOwlRule.java
├── GoalOrientedRule.java
├── WeekendWarriorRule.java
├── PerfectWeekRule.java
├── ProjectLeaderRule.java
├── MultitaskerRule.java
├── OrganizationMasterRule.java
├── PriorityExpertRule.java
├── EarlyPlannerRule.java
└── SpeedRunnerRule.java

## How it works

1. `TaskService.completeTask()` marks a task complete and calls `AchievementService.checkAndGrantAchievements(user, task)`.
2. `AchievementService` asks `UserStatsBuilder` to build a `UserStats` snapshot. This is the only place the database is read.
3. `AchievementService` loads the IDs the user has already unlocked.
4. `AchievementChecker` runs every `AchievementRule` against the snapshot and returns the IDs that should be unlocked now.
5. `AchievementService` saves a new `Achievement` row for each one (using its existing `unlockAchievement` method).

The frontend already listens for `achievement-unlocked` events in `AchievementsNotification.vue` and `services/achievements.js`, so no frontend changes are needed.

## How this connects to the rest of the system
TaskController -> TaskService.completeTask()
│
├─> updates task status in DB
└─> AchievementService.checkAndGrantAchievements(user, task)
│
├─> UserStatsBuilder.buildFor(user, task)         // reads DB once
├─> AchievementChecker.checkUnlocked(stats, ...)  // PURE FUNCTION
└─> for each new ID: unlockAchievement()          // saves Achievement row
│
└─> Frontend polls and dispatches
'achievement-unlocked' event
(existing behavior)
The checker is a **pure function**: same input always returns the same output. No DB, no logging, no I/O. This makes it trivial to test and reusable for a future "preview my next achievement" feature.

## Adding a new achievement

1. Add the achievement definition (title, icon, points) to `AchievementService.initializeAchievementDefinitions()`.
2. Create a new `@Component` class in `rules/` that implements `AchievementRule`.
3. Add the same definition to `frontend/src/services/achievements.js` so the UI knows about it.
4. If the rule needs a stat that isn't in `UserStats`, add the field to the record and update `UserStatsBuilder`.
5. Add a unit test in `AchievementCheckerTest`.

## The AI prompt used to generate this module

The exact prompt is logged in the PKM repo: `experiments/EXP-2026-05-09-pattern-implementation.md`.

## Tests

`AchievementModule/AchievementCheckerTest.java` covers:
- Single rule unlocks.
- Already-unlocked achievements are filtered out.
- Multiple unlocks at once.
- Time-of-day rules respect their windows.
- No mutation of input arguments.
- Empty result when no rules match.
- 
