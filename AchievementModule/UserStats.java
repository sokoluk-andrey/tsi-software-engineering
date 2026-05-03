import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Immutable snapshot of a user's stats at the moment a task was completed.
 *
 * This is the input every AchievementRule sees. Built once per check by
 * UserStatsBuilder. Rules never touch the database — they read this record.
 */
public record UserStats(
        long totalCompletedTasks,
        long totalTasks,
        long completedToday,
        long completedThisWeek,
        long completedOnWeekendThisWeek,
        Set<DayOfWeek> daysWithTasksThisWeek,
        long completedHighPriority,
        Set<String> distinctCategoriesEver,
        long categorizedTasksEver,
        long completedInLastHour,
        long futureTasksBeyondNextWeek,
        LocalDateTime lastCompletionAt
) {}
