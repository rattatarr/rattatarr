<script setup lang="ts">
  import { computed } from 'vue'
  import MediaItemCard from '@/components/common/MediaItemCard.vue'
  import { SearchSource, type SearchResultItem } from '@/queries/useSearch'
  import type { MediaItem } from '@/types'

  interface Props {
    result: SearchResultItem
  }

  const props = defineProps<Props>()

  const mediaItem = computed<MediaItem>(() => {
    const { originalItem, source } = props.result

    // Internal/Jellyfin sources: originalItem is already Movie | Show
    if (source === SearchSource.INTERNAL || source === SearchSource.JELLYFIN) {
      return originalItem as MediaItem
    }

    // External sources (TMDb, etc): Transform to MediaItem format
    // This creates a minimal MediaItem-like object for prefetch and navigation
    return {
      id: props.result.id.toString(),
      title: props.result.title,
      metadata: {
        posterImageUrl: props.result.posterUrl,
      },
      // External items don't have jellyfinId or myRating
    } as MediaItem
  })
</script>

<template>
  <MediaItemCard :item="mediaItem" :media-type="result.mediaType" :source="result.source" />
</template>
