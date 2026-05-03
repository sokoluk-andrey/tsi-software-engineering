import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class PriorityExpertRule implements AchievementRule {
    @Override public String achievementId() { return "priority_expert"; }
    @Override public boolean isMet(UserStats stats) { return stats.completedHighPriority() >= 10; }
}
