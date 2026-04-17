<script setup lang="ts">
  import { useClipboard } from '@vueuse/core'
  import Button from 'primevue/button'
  import Accordion from 'primevue/accordion'
  import AccordionPanel from 'primevue/accordionpanel'
  import AccordionHeader from 'primevue/accordionheader'
  import AccordionContent from 'primevue/accordioncontent'

  const props = defineProps<{
    prompt: string
  }>()

  const { copy, copied } = useClipboard()

  function copyPrompt() {
    copy(props.prompt)
  }
</script>

<template>
  <Accordion>
    <AccordionPanel value="prompt">
      <AccordionHeader>
        <span class="prompt-header-content">
          <i class="pi pi-sparkles" />
          <span>AI System Prompt</span>
          <span class="prompt-header-subtitle">
            Copy the system prompt to use in other AI tools
          </span>
        </span>
      </AccordionHeader>
      <AccordionContent>
        <div class="prompt-content">
          <div class="prompt-actions">
            <Button
              :icon="copied ? 'pi pi-check' : 'pi pi-copy'"
              :label="copied ? 'Copied!' : 'Copy to clipboard'"
              :severity="copied ? 'success' : 'secondary'"
              size="small"
              @click="copyPrompt"
            />
          </div>
          <pre class="prompt-text">{{ prompt }}</pre>
        </div>
      </AccordionContent>
    </AccordionPanel>
  </Accordion>
</template>

<style scoped>
  .prompt-header-content {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex: 1;
  }

  .prompt-header-subtitle {
    font-size: 0.8rem;
    color: var(--p-text-secondary-color);
    font-weight: 400;
    margin-left: 0.5rem;
  }

  .prompt-content {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .prompt-actions {
    display: flex;
    justify-content: flex-end;
  }

  .prompt-text {
    margin: 0;
    font-family: var(--p-font-family);
    font-size: 0.85rem;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    background: var(--p-surface-800);
    border: 1px solid var(--p-surface-border);
    border-radius: 6px;
    padding: 1rem;
    color: var(--p-text-color);
    max-height: 400px;
    overflow-y: auto;
  }
</style>
