import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class ProjectLeaderRule implements AchievementRule {
    @Override public String achievementId() { return "project_leader"; }
    @Override public boolean isMet(UserStats stats) {
        return stats.totalTasks() > 0 && stats.totalCompletedTasks() == stats.totalTasks();
    }
}
