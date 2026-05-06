<template>
  <transition name="notification-slide">
    <div
      v-if="isVisible"
      class="achievement-notification"
      :class="`cat-${notification.category || 'special'}`"
      role="status"
      aria-live="polite"
    >
      <div class="achievement-notification-content">
        <button @click="close" class="notification-close" aria-label="$t('common.close')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>

        <div class="achievement-icon">{{ notification.icon }}</div>

        <div class="achievement-text">
          <div class="achievement-title">{{ $t('achievements.unlocked').toUpperCase() }}</div>
          <div class="achievement-name">{{ getTranslatedTitle() }}</div>
          <div class="achievement-description">{{ getTranslatedDescription() }}</div>
          <div class="achievement-points">+{{ notification.points }} pts</div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'AchievementNotification',
  data() {
    return {
      isVisible: false,
      notification: {
        id: '',
        titleKey: '',
        descriptionKey: '',
        icon: '🏆',
        points: 0
      },
      timeoutId: null,
      recentlyShownAchievements: new Set(),
      isMounted: false
    };
  },
  mounted() {
    console.log('🏆 AchievementNotification component mounted');

    if (!window.achievementListenerRegistered) {
      document.removeEventListener('achievement-unlocked', this.handleAchievementUnlocked);
      document.addEventListener('achievement-unlocked', this.handleAchievementUnlocked);
      window.achievementListenerRegistered = true;
      console.log('🏆 Achievement notification listener registered');
    }

    this.isMounted = true;
  },
  beforeUnmount() {
    console.log('🏆 AchievementNotification component will unmount');

    if (this.isMounted && window.achievementListenerRegistered) {
      document.removeEventListener('achievement-unlocked', this.handleAchievementUnlocked);
      window.achievementListenerRegistered = false;
      console.log('🏆 Achievement notification listener removed');
    }

    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
  },
  methods: {
    handleAchievementUnlocked(event) {
      if (!event || !event.detail) {
        console.warn('Invalid achievement unlock event:', event);
        return;
      }

      console.log('🏆 Achievement unlocked event received:', event.detail);

      const achievementId = event.detail.id || '';

      if (this.recentlyShownAchievements.has(achievementId)) {
        console.log(`Skipping duplicate notification for achievement: ${achievementId}`);
        return;
      }

      // Устанавливаем данные уведомления
      this.notification = {
        id: achievementId,
        titleKey: event.detail.titleKey || '',
        descriptionKey: event.detail.descriptionKey || '',
        icon: event.detail.icon || '🏆',
        points: event.detail.points || 0
      };

      this.recentlyShownAchievements.add(achievementId);

      setTimeout(() => {
        this.recentlyShownAchievements.delete(achievementId);
      }, 10000);

      this.isVisible = true;
      console.log('🏆 Showing achievement notification:', this.notification);

      this.playUnlockSound();

      if (this.timeoutId) {
        clearTimeout(this.timeoutId);
      }

      this.timeoutId = setTimeout(() => {
        this.isVisible = false;
      }, 5000);
    },

    getTranslatedTitle() {
      if (!this.notification.titleKey) {
        return this.$t('achievements.unlocked');
      }

      // Проверяем ключ в секции calendar.achievements
      const calendarKey = `calendar.achievements.${this.notification.titleKey}`;
      if (this.$te(calendarKey)) {
        return this.$t(calendarKey);
      }

      // Проверяем ключ в секции achievements
      const achievementKey = `achievements.${this.notification.titleKey}`;
      if (this.$te(achievementKey)) {
        return this.$t(achievementKey);
      }

      // Fallback
      return this.notification.titleKey || this.$t('achievements.unlocked');
    },

    getTranslatedDescription() {
      if (!this.notification.descriptionKey) {
        return '';
      }

      // Проверяем ключ с суффиксом Desc в calendar.achievements
      let descKey = this.notification.descriptionKey;
      if (!descKey.endsWith('Desc')) {
        descKey = descKey + 'Desc';
      }

      const calendarDescKey = `calendar.achievements.${descKey}`;
      if (this.$te(calendarDescKey)) {
        return this.$t(calendarDescKey);
      }

      // Проверяем без суффикса в calendar.achievements
      const calendarKey = `calendar.achievements.${this.notification.descriptionKey}`;
      if (this.$te(calendarKey)) {
        return this.$t(calendarKey);
      }

      // Проверяем в секции achievements
      const achievementDescKey = `achievements.${descKey}`;
      if (this.$te(achievementDescKey)) {
        return this.$t(achievementDescKey);
      }

      const achievementKey = `achievements.${this.notification.descriptionKey}`;
      if (this.$te(achievementKey)) {
        return this.$t(achievementKey);
      }

      // Fallback
      return this.notification.descriptionKey || '';
    },

    close() {
      if (this.timeoutId) {
        clearTimeout(this.timeoutId);
      }
      this.isVisible = false;
    },

    playUnlockSound() {
      try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        oscillator.type = 'sine';
        oscillator.frequency.setValueAtTime(587.33, audioContext.currentTime);

        const gainNode = audioContext.createGain();
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.start();
        oscillator.stop(audioContext.currentTime + 0.5);
      } catch (error) {
        console.error('Error playing achievement sound:', error);
      }
    }
  }
};
</script>

<style scoped>
.achievement-notification {
  position: fixed;
  top: var(--space-6, 24px);
  right: var(--space-6, 24px);
  width: 100%;
  max-width: 360px;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.achievement-notification-content {
  background: var(--color-bg-elevated, #232A3D);
  border: 2px solid var(--cat-color, var(--color-accent-success, #10B981));
  border-radius: var(--radius-md, 12px);
  padding: var(--space-4, 16px);
  display: flex;
  gap: var(--space-3, 12px);
  align-items: flex-start;
  position: relative;
  color: var(--color-text-primary, #F5F7FA);
}

.notification-close {
  position: absolute;
  top: var(--space-2, 8px);
  right: var(--space-2, 8px);
  background: transparent;
  border: none;
  color: var(--color-text-secondary, #A8B0C0);
  font-size: 18px;
  cursor: pointer;
  padding: var(--space-1, 4px);
  border-radius: var(--radius-sm, 6px);
  line-height: 1;
}

.notification-close:hover {
  background: var(--color-bg-card, #1A1F2E);
  color: var(--color-text-primary, #F5F7FA);
}

.notification-close:focus-visible {
  outline: 2px solid var(--color-accent-primary, #6366F1);
  outline-offset: 2px;
}

.achievement-icon {
  font-size: 32px;
  line-height: 1;
  flex-shrink: 0;
}

.achievement-text {
  flex: 1;
  min-width: 0;
  padding-right: var(--space-4, 16px);
}

.achievement-title {
  font-size: var(--font-base, 16px);
  font-weight: 600;
  margin: 0 0 var(--space-1, 4px);
}

.achievement-description {
  font-size: var(--font-sm, 14px);
  color: var(--color-text-secondary, #A8B0C0);
  margin: 0 0 var(--space-2, 8px);
}

.achievement-points {
  font-size: var(--font-xs, 12px);
  font-weight: 600;
  color: var(--color-accent-warning, #F59E0B);
}

/* Slide-in animation */
.notification-slide-enter-active,
.notification-slide-leave-active {
  transition: transform 300ms cubic-bezier(0.4, 0, 0.2, 1),
              opacity 300ms cubic-bezier(0.4, 0, 0.2, 1);
}

.notification-slide-enter-from,
.notification-slide-leave-to {
  transform: translateX(120%);
  opacity: 0;
}

/* Category colors */
.cat-daily   { --cat-color: var(--color-cat-daily, #3B82F6); }
.cat-weekly  { --cat-color: var(--color-cat-weekly, #8B5CF6); }
.cat-monthly { --cat-color: var(--color-cat-monthly, #EC4899); }
.cat-special { --cat-color: var(--color-cat-special, #F59E0B); }

/* Mobile */
@media (max-width: 640px) {
  .achievement-notification {
    top: var(--space-4, 16px);
    right: var(--space-4, 16px);
    left: var(--space-4, 16px);
    max-width: none;
  }
}
</style>
