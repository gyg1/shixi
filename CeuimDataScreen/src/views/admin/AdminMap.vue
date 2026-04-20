<template>
  <UserMapView />
</template>

<script setup lang="ts">
import UserMapView from '@/views/UserMapView.vue'
</script>

<style scoped>
/* 全屏占满父容器 */
:deep(.mars-container) {
  width: 100%;
  height: 100%;
}
</style>
