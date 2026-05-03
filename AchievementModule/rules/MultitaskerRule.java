import com.example.demo.service.achievement.AchievementRule;
import com.example.demo.service.achievement.UserStats;
import org.springframework.stereotype.Component;

@Component
public class MultitaskerRule implements AchievementRule {
    @Override public String achievementId() { return "multitasker"; }
    @Override public boolean isMet(UserStats stats) { return stats.distinctCategoriesEver().size() >= 5; }
}
