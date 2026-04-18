<script setup lang="ts">
  import { computed, ref } from 'vue'
  import Card from 'primevue/card'
  import { useProfileStore } from '@/stores'
  import {
    useAgentSuggestions,
    useAgentConversation,
    useAgentPrompt,
    useSendAgentChat,
    useClearAgentConversation,
  } from '@/queries'
  import AgentPromptPanel from '@/components/agent/AgentPromptPanel.vue'
  import AgentSuggestions from '@/components/agent/AgentSuggestions.vue'
  import AgentChat from '@/components/agent/AgentChat.vue'

  const profileStore = useProfileStore()
  const profileId = computed(() => profileStore.selectedProfileId ?? null)

  const { data: suggestionsData } = useAgentSuggestions()
  const suggestions = computed(() => suggestionsData.value?.suggestions ?? [])

  const {
    data: conversationData,
    isLoading: isConversationLoading,
    isError: isConversationError,
  } = useAgentConversation(profileId)
  const conversation = computed(() => conversationData.value?.conversation ?? [])

  const { data: promptData } = useAgentPrompt(profileId)
  const prompt = computed(() => promptData.value?.prompt ?? '')

  const messageText = ref('')

  const { mutate: sendMessage, isPending: isSending } = useSendAgentChat()
  const { mutate: clearConversation, isPending: isClearing } = useClearAgentConversation()

  function handleSend(message: string) {
    if (!profileStore.selectedProfileId) return
    sendMessage({ profileId: profileStore.selectedProfileId, message })
  }

  function handleClear() {
    if (!profileStore.selectedProfileId) return
    clearConversation(profileStore.selectedProfileId)
  }
</script>

<template>
  <div class="agent-view">
    <!-- No profile selected -->
    <Card v-if="!profileStore.hasSelectedProfile">
      <template #title>AI Recommendations</template>
      <template #content>
        <p>Please select a profile to use the AI chat feature.</p>
      </template>
    </Card>

    <template v-else>
      <!-- Prompt (collapsible, top) -->
      <AgentPromptPanel v-if="prompt" :prompt="prompt" />

      <!-- Suggestions -->
      <AgentSuggestions
        v-if="suggestions.length"
        :suggestions="suggestions"
        @select="(s) => (messageText = s)"
      />

      <!-- Chat -->
      <AgentChat
        v-model:message-text="messageText"
        :conversation="conversation"
        :is-loading="isConversationLoading"
        :is-error="isConversationError"
        :is-sending="isSending"
        :is-clearing="isClearing"
        @send="handleSend"
        @clear="handleClear"
      />
    </template>
  </div>
</template>

<style scoped>
  .agent-view {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    height: 100%;
  }
</style>
