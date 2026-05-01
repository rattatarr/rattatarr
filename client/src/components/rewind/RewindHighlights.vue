<script setup lang="ts">
  import type { YearRewindHighlights } from '@/types'

  interface Props {
    highlights: YearRewindHighlights
  }

  defineProps<Props>()

  function formatWatchTime(minutes: number): string {
    return `${Math.round(minutes / 60)}h`
  }

  function formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  }

  function heroUrl(url: string | undefined): string | undefined {
    if (!url) return undefined
    return url.replace(/\/w\d+\//, '/w780/')
  }
</script>

<template>
  <div class="rewind-highlights">
    <!-- ── WATCH HIGHLIGHTS ──────────────────────────────── -->
    <div v-if="highlights.firstWatched || highlights.lastWatched" class="section">
      <div class="section-divider">
        <span class="section-label">Watch Highlights</span>
      </div>

      <div class="hero-grid hero-grid--watch">
        <div v-if="highlights.firstWatched" class="hero-card glow-blue">
          <div
            class="hero-inner"
            :style="{
              backgroundImage: highlights.firstWatched.posterImageUrl
                ? `url(${heroUrl(highlights.firstWatched.posterImageUrl)})`
                : 'none',
            }"
          >
            <div class="hero-overlay">
              <span class="hero-badge badge-blue">First Watched</span>
              <span class="hero-title">{{ highlights.firstWatched.title }}</span>
              <span v-if="highlights.firstWatched.eventAt" class="hero-date">
                {{ formatDate(highlights.firstWatched.eventAt) }}
              </span>
              <span v-if="highlights.firstWatched.mediaType" class="hero-type">
                {{ highlights.firstWatched.mediaType }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="highlights.lastWatched" class="hero-card glow-purple">
          <div
            class="hero-inner"
            :style="{
              backgroundImage: highlights.lastWatched.posterImageUrl
                ? `url(${heroUrl(highlights.lastWatched.posterImageUrl)})`
                : 'none',
            }"
          >
            <div class="hero-overlay">
              <span class="hero-badge badge-purple">Last Watched</span>
              <span class="hero-title">{{ highlights.lastWatched.title }}</span>
              <span v-if="highlights.lastWatched.eventAt" class="hero-date">
                {{ formatDate(highlights.lastWatched.eventAt) }}
              </span>
              <span v-if="highlights.lastWatched.mediaType" class="hero-type">
                {{ highlights.lastWatched.mediaType }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── RATING HIGHLIGHTS ─────────────────────────────── -->
    <div v-if="highlights.highestRated || highlights.lowestRated" class="section">
      <div class="section-divider">
        <span class="section-label">Rating Highlights</span>
      </div>

      <div class="hero-grid hero-grid--watch">
        <div v-if="highlights.highestRated" class="hero-card glow-gold">
          <div
            class="hero-inner"
            :style="{
              backgroundImage: highlights.highestRated.posterImageUrl
                ? `url(${heroUrl(highlights.highestRated.posterImageUrl)})`
                : 'none',
            }"
          >
            <div class="hero-overlay">
              <span class="hero-badge badge-gold">Highest Rated</span>
              <span class="hero-title">{{ highlights.highestRated.title }}</span>
              <span class="hero-rating"
                ><i class="pi pi-star-fill star" /> {{ highlights.highestRated.rating }}</span
              >
              <span v-if="highlights.highestRated.mediaType" class="hero-type">
                {{ highlights.highestRated.mediaType }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="highlights.lowestRated" class="hero-card glow-red">
          <div
            class="hero-inner"
            :style="{
              backgroundImage: highlights.lowestRated.posterImageUrl
                ? `url(${heroUrl(highlights.lowestRated.posterImageUrl)})`
                : 'none',
            }"
          >
            <div class="hero-overlay">
              <span class="hero-badge badge-red">Lowest Rated</span>
              <span class="hero-title">{{ highlights.lowestRated.title }}</span>
              <span class="hero-rating"
                ><i class="pi pi-star-fill star" /> {{ highlights.lowestRated.rating }}</span
              >
              <span v-if="highlights.lowestRated.mediaType" class="hero-type">
                {{ highlights.lowestRated.mediaType }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── WATCH STATS ────────────────────────────────────── -->
    <div
      v-if="
        highlights.busiestDay ||
        highlights.longestWatchStreak ||
        highlights.totalWatchTimeMinutes != null ||
        highlights.uniqueItemsWatched != null ||
        highlights.totalMoviesWatched != null ||
        highlights.totalSeriesWatched != null
      "
      class="section"
    >
      <div class="section-divider">
        <span class="section-label">Watch Stats</span>
      </div>

      <div class="stat-grid">
        <div v-if="highlights.totalMoviesWatched != null" class="stat-card stat-card--movies">
          <span class="stat-icon">🎬</span>
          <span class="stat-value">{{ highlights.totalMoviesWatched!.toLocaleString() }}</span>
          <span class="stat-label">Movies Watched</span>
        </div>

        <div v-if="highlights.totalSeriesWatched != null" class="stat-card stat-card--series">
          <span class="stat-icon">📺</span>
          <span class="stat-value">{{ highlights.totalSeriesWatched!.toLocaleString() }}</span>
          <span class="stat-label">Series Watched</span>
        </div>

        <div v-if="highlights.uniqueItemsWatched != null" class="stat-card">
          <span class="stat-icon">✨</span>
          <span class="stat-value">{{ highlights.uniqueItemsWatched!.toLocaleString() }}</span>
          <span class="stat-label">Unique Items</span>
        </div>

        <div v-if="highlights.totalWatchTimeMinutes != null" class="stat-card stat-card--time">
          <span class="stat-icon">⏱</span>
          <span class="stat-value">{{ formatWatchTime(highlights.totalWatchTimeMinutes!) }}</span>
          <span class="stat-label">Watch Time</span>
        </div>

        <div v-if="highlights.busiestDay" class="stat-card stat-card--wide">
          <span class="stat-icon">📅</span>
          <span class="stat-value">{{ highlights.busiestDay }}</span>
          <span class="stat-label">
            Busiest Day
            <span v-if="highlights.busiestDayCount" class="stat-sub">
              · {{ highlights.busiestDayCount }} completed
            </span>
          </span>
        </div>

        <div v-if="highlights.longestWatchStreak" class="stat-card stat-card--wide">
          <span class="stat-icon">🔥</span>
          <span class="stat-value">{{ highlights.longestWatchStreak.days }} days</span>
          <span class="stat-label">
            Longest Streak
            <span class="stat-sub">
              · {{ highlights.longestWatchStreak.startDate }} →
              {{ highlights.longestWatchStreak.endDate }}
            </span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .rewind-highlights {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  /* ── Section ─────────────────────────────────── */
  .section {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    padding: 1.25rem 0;
  }

  /* ── Section divider ─────────────────────────── */
  .section-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
  }

  .section-divider::before,
  .section-divider::after {
    content: '';
    flex: 1;
    height: 1px;
    background: linear-gradient(to var(--dir, right), transparent, var(--p-primary-color));
    opacity: 0.4;
  }

  .section-divider::before {
    --dir: right;
  }

  .section-divider::after {
    --dir: left;
  }

  .section-label {
    font-size: 1.05rem;
    font-weight: 700;
    white-space: nowrap;
    letter-spacing: 0.02em;
    color: var(--p-primary-color);
    text-shadow:
      0 0 12px color-mix(in srgb, var(--p-primary-color) 60%, transparent),
      0 0 24px color-mix(in srgb, var(--p-primary-color) 30%, transparent);
  }

  /* ── Hero grids ──────────────────────────────── */
  .hero-grid {
    display: grid;
    gap: 0.875rem;
  }

  .hero-grid--4 {
    grid-template-columns: repeat(4, 1fr);
  }

  /* Watch highlights: 2 cards centered, same column width as 4-col */
  .hero-grid--watch {
    display: flex;
    justify-content: center;
    gap: 0.875rem;
  }

  .hero-grid--watch > * {
    flex: 0 0 calc(30% - 0.44rem);
    min-width: 0;
  }

  /* ── Hero card ───────────────────────────────── */
  .hero-card {
    border-radius: 0.75rem;
    overflow: hidden;
    transition:
      transform 0.2s ease,
      box-shadow 0.2s ease;
    cursor: default;
  }

  .hero-card:hover {
    transform: translateY(-3px);
  }

  /* Glow variants */
  .glow-gold {
    box-shadow:
      0 0 16px rgba(251, 191, 36, 0.25),
      0 2px 8px rgba(0, 0, 0, 0.3);
  }
  .glow-gold:hover {
    box-shadow:
      0 6px 28px rgba(251, 191, 36, 0.45),
      0 4px 12px rgba(0, 0, 0, 0.4);
  }

  .glow-red {
    box-shadow:
      0 0 16px rgba(248, 113, 113, 0.22),
      0 2px 8px rgba(0, 0, 0, 0.3);
  }
  .glow-red:hover {
    box-shadow:
      0 6px 28px rgba(248, 113, 113, 0.4),
      0 4px 12px rgba(0, 0, 0, 0.4);
  }

  .glow-blue {
    box-shadow:
      0 0 16px rgba(96, 165, 250, 0.22),
      0 2px 8px rgba(0, 0, 0, 0.3);
  }
  .glow-blue:hover {
    box-shadow:
      0 6px 28px rgba(96, 165, 250, 0.4),
      0 4px 12px rgba(0, 0, 0, 0.4);
  }

  .glow-purple {
    box-shadow:
      0 0 16px rgba(167, 139, 250, 0.22),
      0 2px 8px rgba(0, 0, 0, 0.3);
  }
  .glow-purple:hover {
    box-shadow:
      0 6px 28px rgba(167, 139, 250, 0.4),
      0 4px 12px rgba(0, 0, 0, 0.4);
  }

  /* ── Hero inner ──────────────────────────────── */
  .hero-inner {
    position: relative;
    aspect-ratio: 2 / 3;
    width: 100%;
    background-size: cover;
    background-position: center top;
    background-color: var(--p-surface-200);
    display: flex;
    align-items: flex-end;
  }

  /* ── Hero overlay ────────────────────────────── */
  .hero-overlay {
    width: 100%;
    padding: 3.5rem 1rem 1.25rem;
    background: linear-gradient(
      to top,
      rgba(0, 0, 0, 0.92) 0%,
      rgba(0, 0, 0, 0.55) 55%,
      transparent 100%
    );
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
  }

  /* ── Badge labels ────────────────────────────── */
  .hero-badge {
    display: inline-flex;
    align-items: center;
    padding: 0.2em 0.65em;
    border-radius: 999px;
    font-size: 0.6rem;
    text-transform: uppercase;
    letter-spacing: 0.12em;
    font-weight: 800;
    width: fit-content;
    backdrop-filter: blur(6px);
    border: 1px solid transparent;
  }

  .badge-gold {
    background: rgba(251, 191, 36, 0.2);
    color: #fbbf24;
    border-color: rgba(251, 191, 36, 0.35);
    text-shadow: 0 0 8px rgba(251, 191, 36, 0.6);
  }

  .badge-red {
    background: rgba(248, 113, 113, 0.2);
    color: #f87171;
    border-color: rgba(248, 113, 113, 0.35);
    text-shadow: 0 0 8px rgba(248, 113, 113, 0.5);
  }

  .badge-blue {
    background: rgba(96, 165, 250, 0.2);
    color: #60a5fa;
    border-color: rgba(96, 165, 250, 0.35);
    text-shadow: 0 0 8px rgba(96, 165, 250, 0.5);
  }

  .badge-purple {
    background: rgba(167, 139, 250, 0.2);
    color: #a78bfa;
    border-color: rgba(167, 139, 250, 0.35);
    text-shadow: 0 0 8px rgba(167, 139, 250, 0.5);
  }

  /* ── Hero text ───────────────────────────────── */
  .hero-title {
    font-size: 1rem;
    font-weight: 800;
    color: #fff;
    line-height: 1.25;
    text-shadow: 0 1px 6px rgba(0, 0, 0, 0.8);
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .hero-date {
    font-size: 0.78rem;
    color: rgba(255, 255, 255, 0.68);
  }

  .hero-rating {
    font-size: 1rem;
    font-weight: 800;
    color: #fff;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
  }

  .hero-rating .star {
    color: #fbbf24;
    text-shadow: 0 0 8px rgba(251, 191, 36, 0.7);
    font-size: 0.9em;
  }

  .hero-type {
    font-size: 0.6rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: rgba(255, 255, 255, 0.4);
  }

  /* ── Stat grid ───────────────────────────────── */
  .stat-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 0.875rem;
    max-width: 700px;
    margin: 0 auto;
    width: 100%;
  }

  .stat-card {
    background: var(--p-surface-800);
    border: 1px solid var(--p-surface-border);
    border-radius: 0.75rem;
    padding: 1.25rem 1rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.3rem;
    text-align: center;
  }

  .stat-card--wide {
    grid-column: span 2;
  }

  .stat-card--movies {
    border-color: rgba(96, 165, 250, 0.3);
    box-shadow: 0 0 12px rgba(96, 165, 250, 0.1);
  }

  .stat-card--series {
    border-color: rgba(167, 139, 250, 0.3);
    box-shadow: 0 0 12px rgba(167, 139, 250, 0.1);
  }

  .stat-card--time {
    border-color: rgba(52, 211, 153, 0.3);
    box-shadow: 0 0 12px rgba(52, 211, 153, 0.1);
  }

  .stat-icon {
    font-size: 1.4rem;
    line-height: 1;
  }

  .stat-value {
    font-size: 1.8rem;
    font-weight: 800;
    color: var(--p-primary-color);
    line-height: 1.1;
  }

  .stat-label {
    font-size: 0.7rem;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: var(--p-text-muted-color);
    font-weight: 700;
  }

  .stat-sub {
    font-size: 0.72rem;
    text-transform: none;
    letter-spacing: 0;
    font-weight: 400;
    color: var(--p-text-muted-color);
  }

  /* ── Responsive ──────────────────────────────── */
  @media (max-width: 800px) {
    .hero-grid--4 {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 500px) {
    .hero-grid--4 {
      grid-template-columns: 1fr;
    }

    .hero-grid--watch {
      flex-direction: column;
      align-items: center;
    }

    .hero-grid--watch > * {
      flex: 0 0 100%;
      width: 100%;
    }

    .stat-grid {
      grid-template-columns: 1fr;
    }

    .stat-card--wide {
      grid-column: span 1;
    }
  }
</style>
