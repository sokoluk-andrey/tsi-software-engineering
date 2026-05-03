import com.example.demo.service.achievement.AchievementRule;
import com.example.demo.service.achievement.UserStats;
import org.springframework.stereotype.Component;

@Component
public class EarlyBirdRule implements AchievementRule {
    @Override public String achievementId() { return "early_bird"; }
    @Override public boolean isMet(UserStats stats) {
        return stats.lastCompletionAt() != null && stats.lastCompletionAt().getHour() < 6;
    }
}
