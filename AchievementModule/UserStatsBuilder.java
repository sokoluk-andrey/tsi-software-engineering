import com.example.demo.model.Task;
import com.example.demo.model.User;
import com.example.demo.repository.TaskRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The ONE place that reads from repositories. Builds an immutable
 * UserStats snapshot. This keeps every rule a pure function.
 */
@Component
public class UserStatsBuilder {

    private final TaskRepository taskRepository;

    public UserStatsBuilder(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public UserStats buildFor(User user, Task lastCompletedTask) {
        LocalDateTime lastCompletionAt = lastCompletedTask.getCompletedAt();

        // Fetch everything we need
        List<Task> allTasks = taskRepository.findByUser(user);
        long totalTasks = allTasks.size();
        long totalCompleted = taskRepository.countByUserAndCompletedTrue(user);

        long completedToday = 0;
        long completedThisWeek = 0;
        long completedOnWeekend = 0;
        Set<DayOfWeek> daysWithTasks = EnumSet.noneOf(DayOfWeek.class);
        long completedInLastHour = 0;

        if (lastCompletionAt != null) {
            LocalDate day = lastCompletionAt.toLocalDate();
            LocalDateTime startOfDay = day.atStartOfDay();
            LocalDateTime endOfDay = day.atTime(23, 59, 59);

            completedToday = taskRepository
                    .findByUserAndCompletedTrueAndCompletedAtBetween(user, startOfDay, endOfDay)
                    .size();

            LocalDate startOfWeek = day.minusDays(day.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            List<Task> weekTasks = taskRepository
                    .findByUserAndCompletedTrueAndCompletedAtBetween(
                            user, startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59));

            completedThisWeek = weekTasks.size();

            for (Task t : weekTasks) {
                DayOfWeek dow = t.getCompletedAt().getDayOfWeek();
                daysWithTasks.add(dow);
                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                    completedOnWeekend++;
                }
            }

            completedInLastHour = taskRepository
                    .findByUserAndCompletedTrueAndCompletedAtBetween(
                            user, lastCompletionAt.minusHours(1), lastCompletionAt)
                    .size();
        }

        long completedHighPriority = taskRepository
                .findByUserAndPriorityAndCompletedTrue(user, "high")
                .size();

        Set<String> distinctCategories = new HashSet<>();
        long categorized = 0;
        long futureBeyondNextWeek = 0;
        LocalDate cutoff = LocalDate.now().plusDays(7);

        for (Task t : allTasks) {
            String cat = t.getCategory();
            if (cat != null && !cat.trim().isEmpty()) {
                distinctCategories.add(cat);
                categorized++;
            }
            if (t.getYear() != null && t.getMonth() != null && t.getDay() != null) {
                // Note: Task.month is stored 0-indexed (matches the JS frontend convention)
                LocalDate taskDate = LocalDate.of(t.getYear(), t.getMonth() + 1, t.getDay());
                if (taskDate.isAfter(cutoff)) {
                    futureBeyondNextWeek++;
                }
            }
        }

        return new UserStats(
                totalCompleted,
                totalTasks,
                completedToday,
                completedThisWeek,
                completedOnWeekend,
                Set.copyOf(daysWithTasks),
                completedHighPriority,
                Set.copyOf(distinctCategories),
                categorized,
                completedInLastHour,
                futureBeyondNextWeek,
                lastCompletionAt
        );
    }
}
