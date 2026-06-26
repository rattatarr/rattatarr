<script setup lang="ts">
  import Editor from 'primevue/editor'

  /**
   * Thin wrapper around PrimeVue's Quill-based Editor with a constrained
   * toolbar / format whitelist. Images are intentionally disabled — reviews are
   * text only. v-model is the HTML string.
   */

  // Whitelist of Quill formats users may apply. Excludes 'image' (and video).
  const ALLOWED_FORMATS = [
    'header',
    'bold',
    'italic',
    'underline',
    'strike',
    'blockquote',
    'code-block',
    'list',
    'link',
  ]

  withDefaults(
    defineProps<{
      placeholder?: string
      editorStyle?: string
    }>(),
    {
      editorStyle: 'height: 200px',
    },
  )

  const model = defineModel<string>({ default: '' })
</script>

<template>
  <Editor
    v-model="model"
    :formats="ALLOWED_FORMATS"
    :placeholder="placeholder"
    :editor-style="editorStyle"
    class="review-rich-text"
  >
    <template #toolbar>
      <span class="ql-formats">
        <select class="ql-header" defaultValue="0">
          <option value="1">Heading</option>
          <option value="2">Subheading</option>
          <option value="0">Normal</option>
        </select>
      </span>
      <span class="ql-formats">
        <button class="ql-bold" type="button"></button>
        <button class="ql-italic" type="button"></button>
        <button class="ql-underline" type="button"></button>
        <button class="ql-strike" type="button"></button>
      </span>
      <span class="ql-formats">
        <button class="ql-list" value="ordered" type="button"></button>
        <button class="ql-list" value="bullet" type="button"></button>
        <button class="ql-blockquote" type="button"></button>
        <button class="ql-code-block" type="button"></button>
      </span>
      <span class="ql-formats">
        <button class="ql-link" type="button"></button>
        <button class="ql-clean" type="button"></button>
      </span>
    </template>
  </Editor>
</template>

<style scoped>
  .review-rich-text :deep(.ql-container) {
    font-size: 0.95rem;
  }

  /* Wrap long unbroken tokens (URLs/words) instead of x-scrolling */
  .review-rich-text :deep(.ql-editor) {
    overflow-wrap: break-word;
    word-break: break-word;
  }

  /* Global reset forces font-weight:normal on everything; restore bold in editor */
  .review-rich-text :deep(.ql-editor strong),
  .review-rich-text :deep(.ql-editor b) {
    font-weight: 700;
  }

  /*
   * Quill 2's snow theme resets blockquote/p/headings to margin:0;padding:0 and
   * gives blockquote no visual treatment. Re-add content styling so the editor
   * matches the rendered review (ReviewDisplay).
   */
  .review-rich-text :deep(.ql-editor p) {
    margin: 0 0 0.75rem 0;
  }

  .review-rich-text :deep(.ql-editor h1),
  .review-rich-text :deep(.ql-editor h2),
  .review-rich-text :deep(.ql-editor h3) {
    font-weight: 600;
    margin: 1rem 0 0.5rem 0;
    line-height: 1.3;
  }

  .review-rich-text :deep(.ql-editor h1) {
    font-size: 1.6em;
  }

  .review-rich-text :deep(.ql-editor h2) {
    font-size: 1.35em;
  }

  .review-rich-text :deep(.ql-editor h3) {
    font-size: 1.15em;
  }

  .review-rich-text :deep(.ql-editor ol),
  .review-rich-text :deep(.ql-editor ul) {
    margin: 0 0 0.75rem 0;
  }

  .review-rich-text :deep(.ql-editor blockquote) {
    margin: 0 0 0.75rem 0;
    padding-left: 1rem;
    border-left: 3px solid var(--surface-border);
    color: var(--p-text-secondary-color);
  }

  .review-rich-text :deep(.ql-editor a) {
    color: var(--primary-color);
    text-decoration: underline;
  }

  .review-rich-text :deep(.ql-editor pre),
  .review-rich-text :deep(.ql-editor .ql-code-block-container) {
    padding: 0.75rem;
    border-radius: var(--border-radius);
    background: var(--surface-ground);
  }
</style>
