import com.example.demo.service.achievement.AchievementRule;
import com.example.demo.service.achievement.UserStats;
import org.springframework.stereotype.Component;

@Component
public class SuperProductiveDayRule implements AchievementRule {
    @Override public String achievementId() { return "super_productive_day"; }
    @Override public boolean isMet(UserStats stats) { return stats.completedToday() >= 10; }
}
