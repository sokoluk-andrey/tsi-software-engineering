import AchievementModule.AchievementRule;
import AchievementModule.UserStats;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMasterRule implements AchievementRule {
    @Override public String achievementId() { return "organization_master"; }
    @Override public boolean isMet(UserStats stats) { return stats.categorizedTasksEver() >= 20; }
}
