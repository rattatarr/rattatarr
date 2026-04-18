<script setup lang="ts">
  import type { AgentConversationMessage } from '@/types'

  defineProps<{
    message: AgentConversationMessage
  }>()

  function formatTime(dateStr?: string): string {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
</script>

<template>
  <div :class="['message-row', message.role === 'USER' ? 'user-row' : 'assistant-row']">
    <div :class="['message-bubble', message.role === 'USER' ? 'user-bubble' : 'assistant-bubble']">
      <p class="message-content">{{ message.content }}</p>
      <span class="message-time">{{ formatTime(message.sentAt) }}</span>
    </div>
  </div>
</template>

<style scoped>
  .message-row {
    display: flex;
  }

  .user-row {
    justify-content: flex-end;
  }

  .assistant-row {
    justify-content: flex-start;
  }

  .message-bubble {
    max-width: 75%;
    padding: 0.65rem 0.9rem;
    border-radius: 1rem;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .user-bubble {
    background: var(--p-primary-color);
    color: var(--p-primary-contrast-color);
    border-bottom-right-radius: 0.25rem;
  }

  .assistant-bubble {
    background: var(--p-surface-800);
    color: var(--p-text-color);
    border-bottom-left-radius: 0.25rem;
    border: 1px solid var(--p-surface-border);
  }

  .message-content {
    margin: 0;
    font-size: 0.9rem;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .message-time {
    font-size: 0.7rem;
    opacity: 0.65;
    align-self: flex-end;
  }
</style>
