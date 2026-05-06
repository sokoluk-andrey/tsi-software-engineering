<template>
  <div class="achievements-page">
    <div class="achievements-header">
      <div class="achievements-header-content">
        <h1 class="achievements-title">{{ $t('achievements.title') }}</h1>
        <p class="achievements-subtitle">{{ $t('achievements.subtitle') }}</p>

        <div class="achievements-stats">
          <div class="stat-row">
            <div class="stat-item">
              <div class="stat-circle">
                <svg viewBox="0 0 36 36" class="circular-chart">
                  <path class="circle-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                  <path :stroke-dasharray="`${stats.progressPercentage}, 100`" class="circle" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                </svg>
                <div class="stat-percentage">{{ stats.progressPercentage }}%</div>
              </div>
              <span class="stat-number">{{ stats.unlockedCount }}/{{ stats.totalAchievements }}</span>
              <span class="stat-label">{{ $t('achievements.stats.unlocked') }}</span>
            </div>

            <div class="stat-item">
              <div class="total-points">
                <div class="points-icon">🏆</div>
                <div class="points-value">{{ stats.totalPoints }}</div>
              </div>
              <span class="stat-label">{{ $t('achievements.stats.totalPoints') }}</span>
            </div>

            <div class="stat-item">
              <div class="streak-counter">
                <div class="streak-icon">🔥</div>
                <div class="streak-value">{{ stats.streak || 0 }}</div>
              </div>
              <span class="stat-label">{{ $t('achievements.stats.streak') }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="search-filter-container">
      <div class="search-box">
        <input
            type="text"
            :placeholder="$t('achievements.search')"
            v-model="searchQuery"
            class="search-input"
        />
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="search-icon">
          <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>

      <div class="filter-container">
        <button
            v-for="filterOption in filterOptions"
            :key="filterOption.value"
            class="filter-btn"
            :class="{ active: filter === filterOption.value }"
            @click="filter = filterOption.value"
        >
          {{ $t(`achievements.filters.${filterOption.value}`) }}
        </button>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="achievements-loading">
      <div class="loading-spinner"></div>
      <p>{{ $t('achievements.loading') }}</p>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="achievements-error">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      <h3>{{ $t('achievements.error.title') }}</h3>
      <p>{{ error }}</p>
      <button class="retry-btn" @click="loadAchievements">{{ $t('achievements.error.retry') }}</button>
    </div>

    <!-- Success state with achievements -->
    <div v-else class="achievements-grid">
      <div
          class="achievement-category"
          v-for="category in categories"
          :key="category.id"
          v-show="hasCategoryAchievements(category.id)"
      >
        <div class="category-header">
          <div class="category-icon">{{ category.icon }}</div>
          <h2 class="category-title">{{ $t(`achievements.categories.${category.id}`) }}</h2>
        </div>

        <transition-group name="achievement-list" tag="div" class="achievements-list">
          <div
              v-for="achievement in filterAchievements(achievementsByCategory[category.id] || [])"
              :key="achievement.id"
              class="achievement-card"
              :class="{
                'locked': !achievement.isUnlocked,
                'secret': achievement.secret && !achievement.isUnlocked
              }"
              v-show="!shouldHideAchievement(achievement)"
          >
            <div class="achievement-card-inner">
              <div class="achievement-icon" :class="{'locked-icon': !achievement.isUnlocked}">{{ achievement.icon }}</div>
              <div class="achievement-info">
                <h3>{{ achievement.title }}</h3>
                <p>{{ achievement.description }}</p>

                <div v-if="!achievement.isUnlocked && !achievement.secret" class="achievement-progress">
                  <div class="progress-bar">
                    <div
                        class="progress-fill"
                        :style="{ width: `${Math.min(achievement.progress / achievement.totalRequired * 100, 100)}%` }"
                    ></div>
                  </div>
                  <div class="progress-text">
                    {{ achievement.progress }} / {{ achievement.totalRequired }}
                  </div>
                </div>

                <div v-if="achievement.isUnlocked" class="achievement-points">
                  +{{ achievement.points }} pts
                </div>
              </div>
              <div class="achievement-status">
                <div v-if="achievement.isUnlocked" class="status-badge unlocked">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span>{{ $t('achievements.status.unlocked') }}</span>
                </div>
                <div v-else-if="achievement.secret" class="status-badge secret">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 11V7a5 5 0 0110 0v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span>{{ $t('achievements.status.secret') }}</span>
                </div>
                <div v-else class="status-badge locked">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 11V7a5 5 0 0110 0v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span>{{ $t('achievements.status.locked') }}</span>
                </div>
              </div>
            </div>
          </div>
        </transition-group>
      </div>
    </div>

    <div v-if="showEmptyState" class="empty-state">
      <div class="empty-state-content">
        <div class="empty-icon">🔍</div>
        <h3>{{ $t('achievements.empty.title') }}</h3>
        <p>{{ $t('achievements.empty.subtitle') }}</p>
        <button class="reset-btn" @click="resetFilters">{{ $t('achievements.empty.reset') }}</button>
      </div>
    </div>

    <!-- Debug Panel for Development Mode -->
    <div v-if="isDevMode && showDebugPanel" class="debug-panel">
      <div class="debug-header">
        <h4>{{ $t('achievements.debug.title') }}</h4>
        <button class="debug-close-btn" @click="toggleDebugPanel" :title="$t('achievements.debug.hide')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <button class="debug-btn unlock-random" @click="unlockRandomAchievement">
        {{ $t('achievements.debug.unlockRandom') }}
      </button>
      <button class="debug-btn reset-all" @click="resetAllAchievements">
        {{ $t('achievements.debug.resetAll') }}
      </button>
      <div class="debug-achievements-list">
        <h4>{{ $t('achievements.debug.status') }}:</h4>
        <div v-for="category in categories" :key="category.id">
          <strong>{{ category.id }}:</strong>
          <span v-for="ach in achievementsByCategory[category.id] || []"
                :key="ach.id"
                :class="{'unlocked-debug': ach.isUnlocked}">
            {{ ach.id }}{{ ach.isUnlocked ? '(✓)' : '(✗)' }}
          </span>
        </div>
      </div>
    </div>

    <!-- Debug Panel Toggle Button -->
    <div v-if="isDevMode && !showDebugPanel" class="debug-toggle">
      <button class="debug-toggle-btn" @click="toggleDebugPanel" :title="$t('achievements.debug.show')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1 1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        Debug
      </button>
    </div>
  </div>
</template>

<script>
import achievementsService, { achievementCategories } from '@/services/achievements';
export default {
  data() {
    return {
      searchQuery: '',
      filter: 'all',
      filterOptions: [
        { value: 'all', label: 'All' },
        { value: 'unlocked', label: 'Unlocked' },
        { value: 'locked', label: 'Locked' }
      ],
      categories: achievementCategories,
      achievementsByCategory: {},
      stats: {
        unlockedCount: 0,
        totalAchievements: 0,
        progressPercentage: 0,
        totalPoints: 0,
        streak: 0
      },
      isLoading: true,
      error: null,
      isDevMode: process.env.NODE_ENV === 'development',
      showDebugPanel: false, // Initially hidden
      refreshInterval: null
    };
  },

  computed: {
    showEmptyState() {
      // If loading or has error, don't show empty state
      if (this.isLoading || this.error) {
        return false;
      }

      // Check if any achievements are visible after filtering
      for (const categoryId in this.achievementsByCategory) {
        const filteredAchievements = this.filterAchievements(this.achievementsByCategory[categoryId] || []);
        if (filteredAchievements.length > 0) {
          return false;
        }
      }
      return true;
    }
  },

  methods: {
    async loadAchievements() {
      this.isLoading = true;
      this.error = null;

      try {
        console.log("[AchievementsPage] Loading achievements...");

        // Force cache clearance to ensure fresh data
        achievementsService.clearCache();

        // Load achievements by category
        this.achievementsByCategory = await achievementsService.getAchievementsByCategory();

        // Load stats
        this.stats = await achievementsService.getStats();

        // Debug logging
        console.log("----- ACHIEVEMENT DISPLAY DIAGNOSTICS -----");
        console.log("Current filter:", this.filter);

        // Log information about categories and achievement counts
        for (const categoryId in this.achievementsByCategory) {
          const achievements = this.achievementsByCategory[categoryId] || [];
          const unlocked = achievements.filter(a => a.isUnlocked).length;
          console.log(`Category ${categoryId}: ${achievements.length} total, ${unlocked} unlocked`);

          // Log full list of unlocked achievements in this category
          if (unlocked > 0) {
            console.log("Unlocked achievements in this category:",
                achievements.filter(a => a.isUnlocked).map(a => a.id));
          }
        }

        // Calculate total unlocked achievements across all categories
        let totalUnlocked = 0;
        for (const categoryId in this.achievementsByCategory) {
          const achievements = this.achievementsByCategory[categoryId] || [];
          totalUnlocked += achievements.filter(a => a.isUnlocked).length;
        }
        console.log("Total unlocked achievements across all categories:", totalUnlocked);

        // Check stats consistency
        console.log("Stats object shows:", this.stats.unlockedCount, "unlocked");
        console.log("----- END DIAGNOSTICS -----");

        this.isLoading = false;
      } catch (error) {
        console.error('[AchievementsPage] Error loading achievements:', error);
        this.error = this.$t('achievements.error.message');
        this.isLoading = false;
      }
    },

    filterAchievements(achievements) {
      if (!Array.isArray(achievements)) {
        return [];
      }

      console.log("Filtering", achievements.length, "achievements:");

      const filtered = achievements.filter(achievement => {
        // Filter by search query
        const searchMatch = !this.searchQuery ||
            (achievement.title && achievement.title.toLowerCase().includes(this.searchQuery.toLowerCase())) ||
            (achievement.description && achievement.description.toLowerCase().includes(this.searchQuery.toLowerCase()));

        // Filter by unlock status
        const statusMatch = this.filter === 'all' ||
            (this.filter === 'unlocked' && achievement.isUnlocked) ||
            (this.filter === 'locked' && !achievement.isUnlocked);

        return searchMatch && statusMatch;
      });

      console.log("After filtering:", filtered.length, "achievements");

      // Log how many achievements are unlocked in this category
      const unlocked = filtered.filter(a => a.isUnlocked).length;
      if (unlocked > 0) {
        console.log(unlocked, "unlocked achievements in this category:",
            filtered.filter(a => a.isUnlocked).map(a => a.id));
      }

      return filtered;
    },

    shouldHideAchievement(achievement) {
      // Check achievement is valid
      if (!achievement) {
        console.warn("[AchievementsPage] Attempted to check a null/undefined achievement");
        return true;
      }

      // Check achievement fields exist (defensive)
      const title = achievement.title || '';
      const description = achievement.description || '';
      const isUnlocked = !!achievement.isUnlocked;

      // Filter by search query
      if (this.searchQuery) {
        const matchesTitle = title.toLowerCase().includes(this.searchQuery.toLowerCase());
        const matchesDescription = description.toLowerCase().includes(this.searchQuery.toLowerCase());

        if (!matchesTitle && !matchesDescription) {
          return true;
        }
      }

      // Filter by unlock status
      if (this.filter === 'unlocked' && !isUnlocked) {
        return true;
      }

      if (this.filter === 'locked' && isUnlocked) {
        return true;
      }

      return false;
    },

    hasCategoryAchievements(categoryId) {
      const achievements = this.achievementsByCategory[categoryId] || [];
      return this.filterAchievements(achievements).length > 0;
    },

    resetFilters() {
      this.searchQuery = '';
      this.filter = 'all';
    },

    toggleDebugPanel() {
      this.showDebugPanel = !this.showDebugPanel;
    },

    async unlockRandomAchievement() {
      try {
        console.log("[AchievementsPage] Unlocking random achievement...");
        const success = await achievementsService.unlockRandomAchievement();

        if (success) {
          console.log("[AchievementsPage] Successfully unlocked random achievement");
          // Reload achievements to refresh the display
          this.loadAchievements();
        } else {
          console.log("[AchievementsPage] Could not unlock random achievement");
        }
      } catch (error) {
        console.error('[AchievementsPage] Error unlocking random achievement:', error);
      }
    },

    async resetAllAchievements() {
      if (confirm(this.$t('achievements.confirmReset'))) {
        try {
          console.log("[AchievementsPage] Resetting all achievements...");
          await achievementsService.resetAllAchievements();

          // Reload achievements to reflect changes
          this.loadAchievements();
        } catch (error) {
          console.error('[AchievementsPage] Error resetting achievements:', error);
        }
      }
    },

    checkAchievementStatus() {
      console.log("[AchievementsPage] Checking achievement status...");
      this.loadAchievements();
    }
  },

  async mounted() {
    console.log("[AchievementsPage] Component mounted");
    await this.loadAchievements();

    // Check for new achievements every 30 seconds
    this.refreshInterval = setInterval(() => {
      console.log("[AchievementsPage] Refreshing achievements data");
      this.loadAchievements();
    }, 30000);

    // Listen for task completion events to check for new achievements
    document.addEventListener('task-completed', () => {
      console.log("[AchievementsPage] Task completed event detected");
      setTimeout(() => this.loadAchievements(), 1000);
    });

    // Also listen for task creation events
    document.addEventListener('task-created', () => {
      console.log("[AchievementsPage] Task created event detected");
      setTimeout(() => this.loadAchievements(), 1000);
    });
  },

  beforeUnmount() {
    console.log("[AchievementsPage] Component will unmount");
    // Clear refresh interval
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }

    // Remove event listeners
    document.removeEventListener('task-completed', this.checkAchievementStatus);
    document.removeEventListener('task-created', this.checkAchievementStatus);
  }
};
</script>

<style scoped>
:root,
.achievements-page {
  /* Background */
  --color-bg-primary: #0F1419;
  --color-bg-card: #1A1F2E;
  --color-bg-elevated: #232A3D;
  --color-bg-input: #131722;

  /* Text */
  --color-text-primary: #F5F7FA;
  --color-text-secondary: #A8B0C0;
  --color-text-muted: #6B7280;

  /* Accent */
  --color-accent-primary: #6366F1;
  --color-accent-primary-hover: #818CF8;
  --color-accent-success: #10B981;
  --color-accent-warning: #F59E0B;
  --color-accent-danger: #EF4444;

  /* Categories */
  --color-cat-daily: #3B82F6;
  --color-cat-weekly: #8B5CF6;
  --color-cat-monthly: #EC4899;
  --color-cat-special: #F59E0B;

  /* Borders */
  --color-border-default: #2D3548;
  --color-border-subtle: #1F2536;

  /* Spacing */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-6: 24px;
  --space-8: 32px;
  --space-12: 48px;

  /* Radius */
  --radius-sm: 6px;
  --radius-md: 12px;
  --radius-lg: 20px;
  --radius-full: 999px;

  /* Type */
  --font-xs: 12px;
  --font-sm: 14px;
  --font-base: 16px;
  --font-lg: 18px;
  --font-xl: 24px;
  --font-2xl: 32px;
}

.achievements-page {
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  min-height: 100vh;
  padding: var(--space-6);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.page-header {
  max-width: 1280px;
  margin: 0 auto var(--space-8);
}

.page-title {
  font-size: var(--font-2xl);
  font-weight: 700;
  line-height: 40px;
  margin: 0 0 var(--space-2);
}

.page-subtitle {
  font-size: var(--font-base);
  color: var(--color-text-secondary);
  margin: 0;
}

/* Stats summary bar */
.stats-bar {
  max-width: 1280px;
  margin: 0 auto var(--space-8);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
}

.stat-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: var(--space-6);
}

.stat-label {
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  margin: 0 0 var(--space-2);
}

.stat-value {
  font-size: var(--font-xl);
  font-weight: 600;
  margin: 0;
}

/* Filters */
.filters {
  max-width: 1280px;
  margin: 0 auto var(--space-6);
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.filter-button {
  background: transparent;
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  color: var(--color-text-primary);
  padding: var(--space-2) var(--space-4);
  font-size: var(--font-sm);
  font-weight: 500;
  cursor: pointer;
  min-height: 40px;
  transition: background 150ms, border-color 150ms;
}

.filter-button:hover {
  background: var(--color-bg-elevated);
}

.filter-button.is-active {
  background: var(--color-accent-primary);
  border-color: var(--color-accent-primary);
}

.filter-button:focus-visible {
  outline: 2px solid var(--color-accent-primary);
  outline-offset: 2px;
}

/* Achievement grid */
.achievement-grid {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

/* Achievement card */
.achievement-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: var(--space-6);
  display: flex;
  gap: var(--space-4);
  align-items: flex-start;
  position: relative;
  transition: border-color 150ms, transform 150ms;
}

.achievement-card.is-unlocked {
  border-width: 2px;
  border-color: var(--cat-color, var(--color-accent-success));
}

.achievement-card.is-locked {
  opacity: 0.5;
}

.achievement-card.is-unlocked:hover {
  transform: translateY(-2px);
}

.achievement-icon {
  font-size: 48px;
  line-height: 1;
  flex-shrink: 0;
}

.achievement-card.is-locked .achievement-icon {
  filter: grayscale(1);
}

.achievement-body {
  flex: 1;
  min-width: 0;
}

.achievement-title {
  font-size: var(--font-lg);
  font-weight: 600;
  margin: 0 0 var(--space-2);
}

.achievement-description {
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  margin: 0 0 var(--space-3);
}

.achievement-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-xs);
  color: var(--color-text-muted);
}

.achievement-points {
  background: var(--color-accent-warning);
  color: var(--color-bg-primary);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  font-weight: 600;
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
}

/* Category colors via inline style: --cat-color */
.cat-daily   { --cat-color: var(--color-cat-daily); }
.cat-weekly  { --cat-color: var(--color-cat-weekly); }
.cat-monthly { --cat-color: var(--color-cat-monthly); }
.cat-special { --cat-color: var(--color-cat-special); }

/* Empty state */
.empty-state {
  max-width: 480px;
  margin: var(--space-12) auto;
  text-align: center;
}

.empty-state-icon {
  font-size: 48px;
  margin-bottom: var(--space-4);
}

.empty-state-title {
  font-size: var(--font-xl);
  margin: 0 0 var(--space-2);
}

.empty-state-text {
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  margin: 0;
}

/* Loading state */
.loading {
  text-align: center;
  padding: var(--space-12);
  color: var(--color-text-secondary);
}

/* Mobile */
@media (max-width: 640px) {
  .achievements-page { padding: var(--space-4); }
  .page-title { font-size: var(--font-xl); }
  .achievement-grid { grid-template-columns: 1fr; }
  .achievement-card { padding: var(--space-4); }
}
</style>
