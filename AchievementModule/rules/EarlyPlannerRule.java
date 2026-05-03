import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class EarlyPlannerRule implements AchievementRule {
    @Override public String achievementId() { return "early_planner"; }
    @Override public boolean isMet(UserStats stats) { return stats.futureTasksBeyondNextWeek() >= 3; }
}
