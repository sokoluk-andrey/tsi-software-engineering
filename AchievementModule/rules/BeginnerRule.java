import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class BeginnerRule implements AchievementRule {
    @Override public String achievementId() { return "beginner"; }
    @Override public boolean isMet(UserStats stats) { return stats.totalCompletedTasks() >= 1; }
}
