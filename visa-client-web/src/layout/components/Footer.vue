<template>
  <div class="site-footer">
    <div class="footer-content">
      <div class="copyright">
        © 2024 Global Visa. All rights reserved.
      </div>
      <div class="links">
        <span>隐私政策</span>
        <span>服务条款</span>
        <span>联系我们</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.site-footer {
  background-color: #fff;
  border-top: 1px solid #eee;
  padding: 30px 0;
  color: #909399;
  font-size: 14px;
}

.footer-content {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.links span {
  margin-left: 20px;
  cursor: pointer;
  transition: color 0.3s;
  &:hover { color: #6AAFE6; }
}
</style>
