import com.example.demo.service.achievement.AchievementRule;
import com.example.demo.service.achievement.UserStats;
import org.springframework.stereotype.Component;

@Component
public class TimeMasterRule implements AchievementRule {
    @Override public String achievementId() { return "time_master"; }
    @Override public boolean isMet(UserStats stats) { return stats.totalCompletedTasks() >= 50; }
}
