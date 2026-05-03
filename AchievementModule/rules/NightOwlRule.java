import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class NightOwlRule implements AchievementRule {
    @Override public String achievementId() { return "night_owl"; }
    @Override public boolean isMet(UserStats stats) {
        if (stats.lastCompletionAt() == null) return false;
        int hour = stats.lastCompletionAt().getHour();
        return hour >= 0 && hour < 4;
    }
}
