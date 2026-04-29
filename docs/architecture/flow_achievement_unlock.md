# Flow: Achievement Unlock Check

This diagram shows how the achievement check works when a user completes a task.

The actual unlock logic lives in a pure function. It only decides *which* achievements should be unlocked. Saving them to the database and notifying the user happens outside the function.

## Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend (Vue)
    participant API as TaskController
    participant SVC as TaskService
    participant CHK as AchievementChecker (pure)
    participant DB as Database
    participant NOTIF as Notification Service

    User->>FE: Marks task as completed
    FE->>API: POST /api/tasks/{id}/complete
    API->>SVC: completeTask(taskId, userId)
    SVC->>DB: Update task status
    SVC->>DB: Load user stats
    SVC->>DB: Load already unlocked achievements
    SVC->>CHK: checkUnlocked(stats, rules, alreadyUnlocked)
    Note over CHK: Pure function<br/>no DB, no I/O<br/>same input = same output
    CHK-->>SVC: List of newly unlocked IDs
    alt Newly unlocked is not empty
        SVC->>DB: Save new UserAchievement rows
        SVC->>NOTIF: Send unlock notifications
    end
    SVC-->>API: TaskCompleteResponse
    API-->>FE: 200 OK + unlocked list
    FE-->>User: Show toast for each new achievement
```

## Decision flow inside the pure function

```mermaid
flowchart TD
    A[Start: get stats, rules, alreadyUnlocked] --> B[Create empty result list]
    B --> C{For each rule in rules}
    C -->|Next rule| D{Is rule.id in alreadyUnlocked?}
    D -->|Yes| C
    D -->|No| E{Does stats meet rule condition?}
    E -->|No| C
    E -->|Yes| F[Add rule.id to result list]
    F --> C
    C -->|No more rules| G[Return result list]
```

## Why this is a pure function

- **No database reads or writes.** The caller passes in everything the function needs.
- **No notifications or side effects.** It only returns a list.
- **Same input always gives same output.** Calling it twice with the same arguments returns the same result.
- **Easy to test.** Any combination of stats and rules can be tested without setting up a database or mocks.
