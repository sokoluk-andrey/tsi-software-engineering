/**
 * Strategy interface. Each concrete rule decides whether one specific
 * achievement is unlocked, based only on UserStats.
 *
 * Rules MUST be:
 *  - Stateless: no fields holding state across calls.
 *  - Side-effect free: no DB, no logging, no I/O, no input mutation.
 *  - Independent: one rule does not know about other rules.
 */
public interface AchievementRule {

    /** The achievement ID this rule controls (matches Achievement.achievementId). */
    String achievementId();

    /** True if the user's stats satisfy this rule. */
    boolean isMet(UserStats stats);
}
