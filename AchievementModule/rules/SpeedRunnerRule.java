import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class SpeedRunnerRule implements AchievementRule {
    @Override public String achievementId() { return "speed_runner"; }
    @Override public boolean isMet(UserStats stats) { return stats.completedInLastHour() >= 3; }
}
