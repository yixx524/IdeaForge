<!-- 统一卡片容器：header / default / footer 插槽 -->
<template>
  <section class="card" :class="{ 'card-flat': flat }">
    <header v-if="$slots.header" class="card-header">
      <slot name="header" />
    </header>
    <div v-if="$slots.default" class="card-body">
      <slot />
    </div>
    <footer v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </footer>
  </section>
</template>

<script setup>
defineProps({
  flat: { type: Boolean, default: false },
})
</script>

<style scoped>
.card-body {
  padding: 0 1.5rem 1.5rem;
}

.card-body:first-child {
  padding-top: 1.5rem;
}

.card-header + .card-body {
  padding-top: 0;
}
</style>
