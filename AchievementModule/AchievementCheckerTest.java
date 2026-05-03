import rules.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AchievementCheckerTest {

    private AchievementChecker checker;

    @BeforeEach
    void setUp() {
        List<AchievementRule> rules = List.of(
                new BeginnerRule(),
                new TimeMasterRule(),
                new ProductiveDayRule(),
                new SuperProductiveDayRule(),
                new EarlyBirdRule(),
                new NightOwlRule(),
                new GoalOrientedRule(),
                new WeekendWarriorRule(),
                new PerfectWeekRule(),
                new ProjectLeaderRule(),
                new MultitaskerRule(),
                new OrganizationMasterRule(),
                new PriorityExpertRule(),
                new EarlyPlannerRule(),
                new SpeedRunnerRule()
        );
        checker = new AchievementChecker(rules);
    }

    private UserStats stats(long completed, long total, long today, long week,
                            long weekend, Set<DayOfWeek> days, long highPrio,
                            Set<String> cats, long categorized, long lastHour,
                            long futureBeyond, LocalDateTime lastAt) {
        return new UserStats(completed, total, today, week, weekend, days,
                highPrio, cats, categorized, lastHour, futureBeyond, lastAt);
    }

    @Test
    void unlocksBeginnerOnFirstCompletedTask() {
        UserStats s = stats(1, 1, 1, 1, 0, EnumSet.of(DayOfWeek.MONDAY),
                0, Set.of("work"), 1, 0, 0, LocalDateTime.now().withHour(10));

        List<String> unlocked = checker.checkUnlocked(s, Set.of());

        assertThat(unlocked).contains("beginner");
    }

    @Test
    void doesNotReturnAchievementsTheUserAlreadyHas() {
        UserStats s = stats(50, 50, 0, 0, 0, EnumSet.noneOf(DayOfWeek.class),
                10, Set.of("a", "b", "c", "d", "e"), 20, 0, 0,
                LocalDateTime.now().withHour(10));

        List<String> unlocked = checker.checkUnlocked(s,
                Set.of("beginner", "time_master", "priority_expert", "multitasker", "organization_master"));

        assertThat(unlocked)
                .doesNotContain("beginner", "time_master", "priority_expert",
                        "multitasker", "organization_master");
    }

    @Test
    void unlocksMultipleAchievementsAtOnce() {
        UserStats s = stats(10, 10, 10, 10, 0, EnumSet.of(DayOfWeek.MONDAY),
                0, Set.of("work"), 1, 0, 0, LocalDateTime.now().withHour(10));

        List<String> unlocked = checker.checkUnlocked(s, Set.of("beginner"));

        assertThat(unlocked)
                .contains("super_productive_day", "productive_day", "goal_oriented", "project_leader");
    }

    @Test
    void earlyBirdUnlocksOnlyBefore6AM() {
        UserStats early = stats(1, 1, 1, 1, 0, EnumSet.of(DayOfWeek.MONDAY),
                0, Set.of("work"), 0, 0, 0, LocalDateTime.now().withHour(5));
        UserStats late = stats(1, 1, 1, 1, 0, EnumSet.of(DayOfWeek.MONDAY),
                0, Set.of("work"), 0, 0, 0, LocalDateTime.now().withHour(7));

        assertThat(checker.checkUnlocked(early, Set.of("beginner"))).contains("early_bird");
        assertThat(checker.checkUnlocked(late, Set.of("beginner"))).doesNotContain("early_bird");
    }

    @Test
    void doesNotMutateInputArguments() {
        UserStats s = stats(1, 1, 1, 1, 0, EnumSet.of(DayOfWeek.MONDAY),
                0, Set.of("work"), 1, 0, 0, LocalDateTime.now().withHour(10));
        Set<String> alreadyUnlocked = Set.of("beginner");

        checker.checkUnlocked(s, alreadyUnlocked);

        assertThat(alreadyUnlocked).containsExactly("beginner");
    }

    @Test
    void returnsEmptyListWhenNoRulesAreMet() {
        UserStats s = stats(0, 0, 0, 0, 0, EnumSet.noneOf(DayOfWeek.class),
                0, Set.of(), 0, 0, 0, LocalDateTime.now().withHour(10));

        List<String> unlocked = checker.checkUnlocked(s, Set.of());

        assertThat(unlocked).isEmpty();
    }
}
