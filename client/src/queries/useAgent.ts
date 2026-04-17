import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { computed, toValue } from 'vue'
import type { MaybeRefOrGetter } from 'vue'
import * as agentApi from '@/api/agent'
import { agentKeys } from './queryKeys'
import type { AgentChatRequest, AgentChatResponse, AgentConversationWrapper } from '@/types'

export function useAgentSuggestions() {
  return useQuery({
    queryKey: agentKeys.suggestions(),
    queryFn: () => agentApi.getAgentSuggestions(),
    staleTime: 5 * 60 * 1000,
  })
}

export function useAgentPrompt(
  profileId: MaybeRefOrGetter<string | null | undefined>,
  agent?: MaybeRefOrGetter<string | undefined>,
) {
  return useQuery({
    queryKey: computed(() => agentKeys.prompt(toValue(profileId) ?? '', toValue(agent))),
    queryFn: () => agentApi.getAgentPrompt(toValue(profileId)!, toValue(agent)),
    enabled: computed(() => !!toValue(profileId)),
  })
}

export function useAgentConversation(
  profileId: MaybeRefOrGetter<string | null | undefined>,
  agent?: MaybeRefOrGetter<string | undefined>,
) {
  return useQuery({
    queryKey: computed(() => agentKeys.conversation(toValue(profileId) ?? '', toValue(agent))),
    queryFn: () => agentApi.getAgentConversation(toValue(profileId)!, toValue(agent)),
    enabled: computed(() => !!toValue(profileId)),
  })
}

type SendChatContext = { previous: AgentConversationWrapper | undefined }

export function useSendAgentChat(agent?: MaybeRefOrGetter<string | undefined>) {
  const queryClient = useQueryClient()

  return useMutation<AgentChatResponse, Error, AgentChatRequest, SendChatContext>({
    mutationFn: (request: AgentChatRequest) => agentApi.sendAgentChat(request, toValue(agent)),
    onMutate: async (variables) => {
      const queryKey = agentKeys.conversation(variables.profileId, toValue(agent))
      await queryClient.cancelQueries({ queryKey })
      const previous = queryClient.getQueryData<AgentConversationWrapper>(queryKey)
      queryClient.setQueryData<AgentConversationWrapper>(queryKey, (old) => ({
        conversation: [
          ...(old?.conversation ?? []),
          {
            id: `optimistic-${Date.now()}`,
            role: 'USER' as const,
            content: variables.message,
            sentAt: new Date().toISOString(),
          },
        ],
      }))
      return { previous }
    },
    onError: (_, variables, context) => {
      if (context?.previous !== undefined) {
        queryClient.setQueryData(
          agentKeys.conversation(variables.profileId, toValue(agent)),
          context.previous,
        )
      }
    },
    onSuccess: async (_, variables) => {
      await queryClient.invalidateQueries({
        queryKey: agentKeys.conversation(variables.profileId, toValue(agent)),
      })
    },
  })
}

export function useClearAgentConversation(agent?: MaybeRefOrGetter<string | undefined>) {
  const queryClient = useQueryClient()

  return useMutation<void, Error, string>({
    mutationFn: (profileId: string) => agentApi.clearAgentConversation(profileId, toValue(agent)),
    onSuccess: async (_, profileId) => {
      await queryClient.invalidateQueries({
        queryKey: agentKeys.conversation(profileId, toValue(agent)),
      })
    },
  })
}
