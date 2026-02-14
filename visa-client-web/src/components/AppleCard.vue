<template>
  <div class="apple-card" :class="{ 'is-hover': hover }">
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'AppleCard',
  props: {
    hover: {
      type: Boolean,
      default: false // 是否开启悬浮上浮效果
    }
  }
}
</script>

<style scoped lang="scss">
.apple-card {
  background: #ffffff;
  border-radius: 20px; /* 大圆角 */
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;

  /* 如果开启了 hover 属性 */
  &.is-hover:hover {
    transform: translateY(-8px); /* 上浮 */
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08); /* 阴影加深 */
  }
}
</style>
