import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class WeekendWarriorRule implements AchievementRule {
    @Override public String achievementId() { return "weekend_warrior"; }
    @Override public boolean isMet(UserStats stats) { return stats.completedOnWeekendThisWeek() >= 5; }
}
