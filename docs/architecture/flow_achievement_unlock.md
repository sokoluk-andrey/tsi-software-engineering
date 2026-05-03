# Flow: Achievement Unlock Check

This file shows how the achievement check works when a user completes a task.

The check uses the **Strategy pattern**. Each achievement is its own rule class. A separate `UserStatsBuilder` is the only class that reads from the DB. The `AchievementChecker` is a pure function: same input → same output.

## Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend (Vue)
    participant API as TaskController
    participant TS as TaskService
    participant AS as AchievementService
    participant USB as UserStatsBuilder
    participant CHK as AchievementChecker (pure)
    participant DB as Database

    User->>FE: Marks task as completed
    FE->>API: PUT /api/tasks/{id}/complete
    API->>TS: completeTask(id, user)
    TS->>DB: Save completed task
    TS->>AS: checkAndGrantAchievements(user, task)
    AS->>USB: buildFor(user, task)
    USB->>DB: Load tasks (once)
    USB-->>AS: UserStats snapshot
    AS->>DB: Load already-unlocked IDs
    AS->>CHK: checkUnlocked(stats, alreadyUnlocked)
    Note over CHK: Pure function<br/>no DB, no I/O<br/>same input = same output
    CHK-->>AS: List of newly unlocked IDs
    loop For each new ID
        AS->>DB: Save Achievement row
    end
    AS-->>TS: Done
    TS-->>API: Updated task
    API-->>FE: 200 OK
    FE-->>User: 'achievement-unlocked' event<br/>shows toast (existing flow)
```

## Decision flow inside the checker

```mermaid
flowchart TD
    A[Start: stats, alreadyUnlocked] --> B[Empty result list]
    B --> C{For each rule}
    C -->|Next rule| D{Already unlocked?}
    D -->|Yes| C
    D -->|No| E{Rule isMet stats?}
    E -->|No| C
    E -->|Yes| F[Add achievementId to result]
    F --> C
    C -->|No more rules| G[Return result list]
```

## Class structure (Strategy pattern)

```mermaid
classDiagram
    class AchievementRule {
        <<interface>>
        +achievementId() String
        +isMet(stats) boolean
    }

    class AchievementChecker {
        -List~AchievementRule~ rules
        +checkUnlocked(stats, alreadyUnlocked) List~String~
    }

    class UserStatsBuilder {
        -TaskRepository taskRepository
        +buildFor(user, task) UserStats
    }

    class UserStats {
        <<record>>
    }

    class BeginnerRule
    class TimeMasterRule
    class ProductiveDayRule
    class SuperProductiveDayRule
    class EarlyBirdRule
    class NightOwlRule
    class GoalOrientedRule
    class WeekendWarriorRule
    class PerfectWeekRule
    class ProjectLeaderRule
    class MultitaskerRule
    class OrganizationMasterRule
    class PriorityExpertRule
    class EarlyPlannerRule
    class SpeedRunnerRule

    AchievementRule <|.. BeginnerRule
    AchievementRule <|.. TimeMasterRule
    AchievementRule <|.. ProductiveDayRule
    AchievementRule <|.. SuperProductiveDayRule
    AchievementRule <|.. EarlyBirdRule
    AchievementRule <|.. NightOwlRule
    AchievementRule <|.. GoalOrientedRule
    AchievementRule <|.. WeekendWarriorRule
    AchievementRule <|.. PerfectWeekRule
    AchievementRule <|.. ProjectLeaderRule
    AchievementRule <|.. MultitaskerRule
    AchievementRule <|.. OrganizationMasterRule
    AchievementRule <|.. PriorityExpertRule
    AchievementRule <|.. EarlyPlannerRule
    AchievementRule <|.. SpeedRunnerRule

    AchievementChecker o-- AchievementRule : uses many
    AchievementChecker ..> UserStats : reads
    UserStatsBuilder ..> UserStats : creates
```

## Why this is a pure function

- No DB reads or writes inside the checker — `UserStatsBuilder` does that once, separately.
- No notifications or side effects inside the checker — only returns a list.
- Same input always gives same output.
- Easy to test without mocks or a database.

## Why Strategy

- Adding a new achievement = a new class. No existing code is touched.
- Each rule is testable on its own.
- Spring auto-discovers all `@Component` rules and injects them as a list.
- See module README at `AchievementModule/README.md` for full rationale.
