import com.example.demo.service.achievement.AchievementRule;
import com.example.demo.service.achievement.UserStats;
import org.springframework.stereotype.Component;

@Component
public class PerfectWeekRule implements AchievementRule {
    @Override public String achievementId() { return "perfect_week"; }
    @Override public boolean isMet(UserStats stats) { return stats.daysWithTasksThisWeek().size() == 7; }
}
