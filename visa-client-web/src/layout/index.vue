<template>
  <div class="app-wrapper">
    <!-- 1. 顶部导航 (磨砂玻璃效果) -->
    <navbar />

    <!-- 2. 主体内容区 (核心出口) -->
    <!-- 加上 fade-transform 动画，让切换页面时有淡入淡出的高级感 -->
    <div class="main-container">
      <transition name="fade-transform" mode="out-in">
        <router-view />
      </transition>
    </div>

    <!-- 3. 底部版权区 -->
    <app-footer />
  </div>
</template>

<script>
/* eslint-disable */
import AppFooter from './components/Footer';
import Navbar from './components/Navbar';

export default {
  name: 'Layout',
  components: {
    Navbar,
    AppFooter
  }
}
</script>

<style scoped lang="scss">

@import "@/assets/styles/variables.scss";

.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: $bg-color; // 使用我们定义的牛奶白背景
}

.main-container {
  flex: 1;
  width: 100%;
  padding-top: 0; /* ★ 核心修改：去掉 padding-top，让首屏直接顶到最上面 */
  padding-bottom: 0;
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.fade-transform-enter {
  opacity: 0;
  transform: translateY(10px); /* 稍微从下往上浮现 */
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
