<script setup lang="ts">
  import { watch, nextTick } from 'vue'
  import { useTemplateRef } from 'vue'
  import Button from 'primevue/button'
  import Card from 'primevue/card'
  import Message from 'primevue/message'
  import Textarea from 'primevue/textarea'
  import ProgressSpinner from 'primevue/progressspinner'
  import ChatMessage from './ChatMessage.vue'
  import type { AgentConversationMessage } from '@/types'

  const props = defineProps<{
    conversation: AgentConversationMessage[]
    isLoading: boolean
    isError: boolean
    isSending: boolean
    isClearing: boolean
  }>()

  const emit = defineEmits<{
    send: [message: string]
    clear: []
  }>()

  const messageText = defineModel<string>('messageText', { default: '' })
  const conversationContainer = useTemplateRef<HTMLDivElement>('conversationContainer')

  function scrollToBottom() {
    if (conversationContainer.value) {
      conversationContainer.value.scrollTop = conversationContainer.value.scrollHeight
    }
  }

  watch(
    () => props.conversation.length,
    async () => {
      await nextTick()
      scrollToBottom()
    },
    { immediate: true },
  )

  function handleSend() {
    const text = messageText.value.trim()
    if (!text || props.isSending) return
    emit('send', text)
    messageText.value = ''
  }

  function handleKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault()
      handleSend()
    }
  }
</script>

<template>
  <Card class="chat-card">
    <template #title>
      <div class="chat-header">
        <span class="chat-title">
          <i class="pi pi-comments" />
          Chat
        </span>
        <Button
          label="Clear"
          icon="pi pi-trash"
          severity="danger"
          text
          size="small"
          :loading="isClearing"
          :disabled="conversation.length === 0"
          @click="emit('clear')"
        />
      </div>
    </template>

    <template #content>
      <div class="chat-body">
        <!-- Loading -->
        <div v-if="isLoading" class="chat-loading">
          <ProgressSpinner style="width: 40px; height: 40px" />
        </div>

        <!-- Error -->
        <Message v-else-if="isError" severity="error" :closable="false">
          Failed to load conversation. Please try again.
        </Message>

        <!-- Empty state -->
        <div v-else-if="conversation.length === 0" class="chat-empty">
          <i class="pi pi-comments chat-empty-icon" />
          <p>No messages yet. Start by asking for a recommendation!</p>
        </div>

        <!-- Conversation -->
        <div v-else ref="conversationContainer" class="conversation-container">
          <ChatMessage v-for="msg in conversation" :key="msg.id" :message="msg" />
        </div>

        <!-- Input area -->
        <div class="input-area">
          <Textarea
            v-model="messageText"
            placeholder="Ask for recommendations… (Ctrl+Enter to send)"
            :auto-resize="true"
            rows="2"
            class="message-input"
            @keydown="handleKeyDown"
          />
          <Button
            icon="pi pi-send"
            :loading="isSending"
            :disabled="!messageText.trim() || isSending"
            aria-label="Send message"
            class="send-button"
            @click="handleSend"
          />
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
  .chat-card {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  .chat-card :deep(.p-card-body) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    padding-bottom: 0;
  }

  .chat-card :deep(.p-card-content) {
    flex: 1;
    min-height: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
  }

  .chat-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  .chat-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .chat-body {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    padding: 0 1.25rem 1.25rem;
  }

  /* Loading / empty */
  .chat-loading {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 2rem;
  }

  .chat-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
    color: var(--p-text-secondary-color);
    padding: 2rem 1rem;
  }

  .chat-empty-icon {
    font-size: 2.5rem;
    opacity: 0.4;
  }

  /* Conversation — flex:1 fills remaining card space; padding-right keeps bubbles clear of scrollbar */
  .conversation-container {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    padding: 0.5rem 0.75rem 1rem 0;
  }

  /* Input */
  .input-area {
    display: flex;
    gap: 0.5rem;
    align-items: stretch;
    padding-top: 0.75rem;
    border-top: 1px solid var(--p-surface-border);
  }

  .message-input {
    flex: 1;
    resize: none;
    font-size: 0.9rem;
  }

  .input-area :deep(.p-button.send-button) {
    height: auto;
    align-self: stretch;
  }
</style>
