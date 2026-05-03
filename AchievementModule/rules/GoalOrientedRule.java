import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class GoalOrientedRule implements AchievementRule {
    @Override public String achievementId() { return "goal_oriented"; }
    @Override public boolean isMet(UserStats stats) { return stats.completedThisWeek() >= 10; }
}
