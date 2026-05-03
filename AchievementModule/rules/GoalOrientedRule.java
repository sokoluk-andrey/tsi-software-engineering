import com.example.demo.service.achievement.AchievementRule;
import com.example.demo.service.achievement.UserStats;
import org.springframework.stereotype.Component;

@Component
public class GoalOrientedRule implements AchievementRule {
    @Override public String achievementId() { return "goal_oriented"; }
    @Override public boolean isMet(UserStats stats) { return stats.completedThisWeek() >= 10; }
}
