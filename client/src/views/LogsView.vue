<script setup lang="ts">
  import LogsHistoryPanel from '@/components/logs/LogsHistoryPanel.vue'
</script>

<template>
  <div class="logs-view">
    <LogsHistoryPanel />
  </div>
</template>

<style scoped>
  .logs-view {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-sizing: border-box;
  }
</style>
