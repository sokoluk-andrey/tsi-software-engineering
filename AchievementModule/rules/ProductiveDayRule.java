import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class ProductiveDayRule implements AchievementRule {
    @Override public String achievementId() { return "productive_day"; }
    @Override public boolean isMet(UserStats stats) { return stats.completedToday() >= 5; }
}
