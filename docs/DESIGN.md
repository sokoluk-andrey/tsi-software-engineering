# DESIGN.md — TaskMaster UI Design Contract

This file is the design contract for TaskMaster's frontend. AI assistants generating UI code (Claude, Cursor, Copilot) MUST follow these rules. Rules are not suggestions.

If a rule is unclear or seems to block the task, stop and ask. Do not guess.

---

## 1. Framework

- **Vue 3** with the **Composition API** (`<script setup>`).
- **Vue I18n** for all user-facing strings — no hardcoded text in templates.
- **Plain CSS** scoped to each component (`<style scoped>`). No Tailwind, no Bootstrap, no UI library.
- Icons: Unicode emoji (already used across the codebase). No icon font dependencies.
- Animations: native CSS only. No animation libraries.

---

## 2. Color palette

These are the only colors allowed in new UI work. They must be declared as CSS custom properties in `:root` (already present in the project's global stylesheet). Refer to them via `var(--name)`.

### Background
- `--color-bg-primary: #0F1419` — main page background
- `--color-bg-card: #1A1F2E` — card and panel background
- `--color-bg-elevated: #232A3D` — modals, popovers, hover states
- `--color-bg-input: #131722` — form inputs

### Text
- `--color-text-primary: #F5F7FA` — headings, body text
- `--color-text-secondary: #A8B0C0` — captions, meta info
- `--color-text-muted: #6B7280` — disabled, placeholder

### Accent
- `--color-accent-primary: #6366F1` — primary buttons, active states (indigo)
- `--color-accent-primary-hover: #818CF8`
- `--color-accent-success: #10B981` — unlocked achievements, completed states
- `--color-accent-warning: #F59E0B` — high-priority, points
- `--color-accent-danger: #EF4444` — errors, locked-and-disabled states

### Achievement category colors (existing semantic in the codebase)
- `--color-cat-daily: #3B82F6` — blue
- `--color-cat-weekly: #8B5CF6` — purple
- `--color-cat-monthly: #EC4899` — pink
- `--color-cat-special: #F59E0B` — amber

### Borders
- `--color-border-default: #2D3548`
- `--color-border-subtle: #1F2536`

**Hard rules:**
- No raw hex codes in component CSS. Always `var(--name)`.
- No `rgba(0, 0, 0, ...)` overlays. Use elevated backgrounds.
- No gradients except on achievement cards (defined in §6 below).

---

## 3. Typography

- **Font family:** system stack — `-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`.
- **Type scale:**
  - `--font-xs: 12px / 16px line-height` — captions, badges
  - `--font-sm: 14px / 20px` — body, labels
  - `--font-base: 16px / 24px` — primary body
  - `--font-lg: 18px / 28px` — card titles
  - `--font-xl: 24px / 32px` — section headings
  - `--font-2xl: 32px / 40px` — page titles
- **Weights:** 400 (normal), 500 (medium), 600 (semibold), 700 (bold). No 300 (light) — too thin on dark backgrounds.

---

## 4. Spacing

8-point grid. Pick from this scale only:

- `--space-1: 4px`
- `--space-2: 8px`
- `--space-3: 12px`
- `--space-4: 16px`
- `--space-6: 24px`
- `--space-8: 32px`
- `--space-12: 48px`

**Rules:**
- Card internal padding: `--space-6` (24px) on all sides.
- Card gap (in grids): `--space-4` (16px).
- Section vertical rhythm: `--space-8` (32px) between major sections.
- Never use values outside this scale.

---

## 5. Border radius

- `--radius-sm: 6px` — buttons, inputs, badges
- `--radius-md: 12px` — cards, panels
- `--radius-lg: 20px` — modals, large containers
- `--radius-full: 999px` — pills, circular avatars

No square corners except on full-width section dividers.

---

## 6. Component rules

### Buttons
- Always `--radius-sm`.
- Min height 40px; padding `--space-2` vertical, `--space-4` horizontal.
- **Primary:** background `--color-accent-primary`, text `--color-text-primary`, hover `--color-accent-primary-hover`.
- **Secondary:** background transparent, border 1px `--color-border-default`, text `--color-text-primary`.
- **Ghost:** transparent, text `--color-text-secondary`, hover background `--color-bg-elevated`.
- **NEVER** stack two primary buttons next to each other. The right action is primary; the left is secondary or ghost.
- Disabled buttons: `opacity: 0.4`, `cursor: not-allowed`. No removed border or text.

### Cards
- Background `--color-bg-card`, padding `--space-6`, radius `--radius-md`.
- Border 1px `--color-border-subtle`. No drop shadows on dark backgrounds — they look muddy. Use border + background contrast instead.

### Achievement cards (specific to this feature)
- Locked: background `--color-bg-card`, opacity 0.5 on icon, grayscale on title.
- Unlocked: background `--color-bg-card` with a 2px gradient border in the achievement's category color. Icon at full opacity.
- Secret + locked: icon shows `❓`, title shows `???`, description shows `Secret achievement`.
- Each card displays: icon (left, 48px), title, description, point value badge (top-right), unlock date (bottom, only if unlocked).

### Notification toast
- Position: top-right, `--space-6` from edges.
- Width: 360px max.
- Background `--color-bg-elevated`, border 2px gradient in category color.
- Slide in from right with `cubic-bezier(0.4, 0, 0.2, 1)` over 300ms.
- Auto-dismiss after 5 seconds. Manual close button (×) in top-right corner.
- Stack at most 3 visible at once. Older notifications fade out first.

### Forms
- Input height 40px. Border 1px `--color-border-default`. Focus border `--color-accent-primary`.
- Labels above inputs, `--font-sm`, `--color-text-secondary`.
- Error state: border `--color-accent-danger`, message below input in same color, `--font-xs`.

### Empty states
- Centered icon (48px) + heading + one-sentence description.
- No call-to-action button unless there's a meaningful next step.

---

## 7. Layout

- Page max-width: 1280px. Centered with auto margins.
- Page side padding: `--space-6` on desktop, `--space-4` on mobile (< 768px).
- Achievement grid: CSS Grid, `grid-template-columns: repeat(auto-fill, minmax(280px, 1fr))`, gap `--space-4`.
- Single-column layout below 640px.

---

## 8. Accessibility (non-negotiable)

- Color contrast: text vs background must hit WCAG AA — 4.5:1 for normal text, 3:1 for large.
- Every interactive element must be reachable by keyboard. `:focus-visible` must show a 2px outline in `--color-accent-primary`.
- Every icon-only button must have an `aria-label`.
- Notifications must use `role="status"` and `aria-live="polite"` so screen readers announce them.
- Locked achievements must not be focusable (they're not interactive). Use `tabindex="-1"` or simply no button wrapper.
- Color must never be the only carrier of meaning. Locked vs unlocked also differentiated by opacity and a lock icon.

---

## 9. Responsive breakpoints

- Mobile: `< 640px` — single column, stacked filters, full-width buttons.
- Tablet: `640px – 1024px` — 2-column achievement grid, side-by-side filters.
- Desktop: `> 1024px` — 3+ column achievement grid, full layout.

Use `@media (min-width: ...)`, mobile-first.

---

## 10. What NOT to do

- ❌ No Tailwind classes anywhere. This is a Vue + scoped CSS codebase.
- ❌ No external icon libraries. Emojis only.
- ❌ No `box-shadow` for elevation — use background contrast.
- ❌ No light mode. The app is dark-mode only by design.
- ❌ No raw color values in component styles — only `var(--name)`.
- ❌ No hardcoded user-facing strings — everything goes through `$t('key')`.
- ❌ No `position: fixed` except for the notification toast container.
- ❌ No two primary buttons next to each other.
