import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Strategy "context".
 *
 * Holds all AchievementRule strategies and runs them against the user's stats.
 * Spring auto-injects every @Component implementing AchievementRule.
 *
 * This is a PURE FUNCTION:
 *   - no DB calls
 *   - no logging, no notifications
 *   - same input -> same output, every time
 *
 * Adding a new achievement = adding a new @Component class. No edits here.
 */
@Component
public class AchievementChecker {

    private final List<AchievementRule> rules;

    public AchievementChecker(List<AchievementRule> rules) {
        this.rules = List.copyOf(rules);
    }

    /**
     * Returns the IDs of achievements that should be unlocked now,
     * but are not in alreadyUnlocked.
     */
    public List<String> checkUnlocked(UserStats stats, Set<String> alreadyUnlocked) {
        return rules.stream()
                .filter(rule -> !alreadyUnlocked.contains(rule.achievementId()))
                .filter(rule -> rule.isMet(stats))
                .map(AchievementRule::achievementId)
                .toList();
    }
}
