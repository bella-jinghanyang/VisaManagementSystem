<template>
  <div class="cart-container container-width">
    <div class="page-header">
      <h2>预选清单</h2>
      <p>选择您计划办理的签证，开启旅程</p>
    </div>

    <!-- 列表不为空 -->
    <div v-if="cartList.length > 0">
      <div class="cart-list">
        <apple-card v-for="item in cartList" :key="item.id" class="cart-item mb-20">
          <div class="item-inner">
            <!-- 勾选框 -->
            <el-checkbox v-model="item.checked" class="custom-checkbox"></el-checkbox>

            <!-- 产品图 -->
            <img :src="$getImgUrl(item.productImage)" class="item-img">

            <!-- 信息区 -->
            <div class="item-info">
              <h4 @click="$router.push('/product/'+item.productId)">{{ item.productTitle }}</h4>
              <p class="price">￥{{ item.productPrice }}</p>
            </div>

            <!-- 产品数量区 -->
            <div class="item-stepper">
              <el-input-number v-model="item.quantity" @change="(val) => handleQuantityChange(val, item)" :min="1"
                size="small" label="描述文字">
              </el-input-number>
            </div>

            <!-- 操作区 -->
            <div class="item-action">
              <el-button type="text" icon="el-icon-delete" class="delete-btn" @click="handleDelete(item.id)"></el-button>
            </div>
          </div>
        </apple-card>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox v-model="allChecked" @change="handleSelectAll">全选</el-checkbox>
        </div>
        <div class="footer-right">
          <div class="total-info">
            <span class="label">已选 {{ selectedCount }} 项，合计：</span>
            <span class="total-amount">￥{{ totalAmount }}</span>
          </div>
          <el-button type="primary" class="checkout-btn" @click="handleCheckout">去办理</el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <apple-card class="empty-card">
        <i class="el-icon-shopping-cart-2"></i>
        <h3>清单还是空的</h3>
        <p>去签证大厅看看有哪些感兴趣的国家吧</p>
        <el-button type="primary" round @click="$router.push('/products')">前往签证大厅</el-button>
      </apple-card>
    </div>
  </div>
</template>

<script>
import AppleCard from '@/components/AppleCard'
import { listCart, delCart, updateCartQuantity } from '@/api/cart'

export default {
  name: 'Cart',
  components: { AppleCard },
  data () {
    return {
      cartList: [],
      allChecked: false
    }
  },
  computed: {
    // 选中的数量
    selectedCount () {
      return this.cartList.filter(item => item.checked).length
    },

    // 修改计算总价的逻辑 (价格 * 数量)
    totalAmount () {
      return this.cartList
        .filter(item => item.checked)
        .reduce((sum, item) => sum + (parseFloat(item.productPrice) * item.quantity), 0)
        .toFixed(2)
    }
  },
  created () {
    this.getList()
  },
  methods: {
    getList () {
      const user = JSON.parse(localStorage.getItem('Client-User'))
      listCart(user.id).then(res => {
        // 给每个数据手动加上 checked 属性，方便勾选
        this.cartList = res.data.map(item => ({ ...item, checked: false }))
      })
    },
    handleDelete (id) {
      this.$confirm('确定从清单中移除吗？', '提示').then(() => {
        delCart(id).then(() => {
          this.$message.success('已移除')
          this.getList()
          this.$bus.$emit('cart-updated')
        })
      })
    },
    handleSelectAll (val) {
      this.cartList.forEach(item => { item.checked = val })
    },
    handleCheckout () {
      const selectedItems = this.cartList.filter(item => item.checked)
      if (selectedItems.length === 0) {
        this.$message.warning('请至少勾选一个签证')
        return
      }

      // 简便起见，我们取第一个勾选的产品和它的数量
      const firstItem = selectedItems[0]

      this.$router.push({
        path: '/order/create',
        query: {
          productId: firstItem.productId,
          quantity: firstItem.quantity // 这里传过去！
        }
      })
    },
    // 处理数量变化
    handleQuantityChange (currentValue, item) {
    // 调用后端接口更新数据库里的数量
      updateCartQuantity(item.id, currentValue).then(res => {
      // 更新成功后，通知 Navbar 刷新总数
        this.$bus.$emit('cart-updated')
      })
    }

  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.cart-container { padding: 40px 0 120px; }
.page-header { margin-bottom: 30px; h2 { font-size: 28px; font-weight: 700; } p { color: $text-secondary; } }

.cart-item {
  padding: 15px 25px !important;
  .item-inner { display: flex; align-items: center; }
  .item-img { width: 100px; height: 70px; border-radius: 12px; object-fit: cover; margin: 0 25px; }
  .item-info { flex: 1; h4 { font-size: 18px; margin-bottom: 8px; cursor: pointer; &:hover { color: $primary-color; } } .price { color: #FF6B6B; font-weight: 700; font-size: 18px; } }
  .delete-btn { color: #C0C4CC; font-size: 20px; &:hover { color: #FF6B6B; } }
}

.cart-footer {
  position: fixed; bottom: 40px; width: 1200px; height: 80px;
  background: rgba(255, 255, 255, 0.8); backdrop-filter: blur(20px);
  border-radius: 24px; box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  display: flex; align-items: center; justify-content: space-between; padding: 0 40px;
  z-index: 100;
}

.footer-right {
  display: flex; align-items: center; gap: 30px;
  .total-amount { font-size: 28px; font-weight: 800; color: $primary-color; }
  .checkout-btn { height: 50px; padding: 0 40px; font-size: 18px; }
}

.empty-state { text-align: center; padding-top: 100px; .empty-card { padding: 60px !important; i { font-size: 80px; color: #E4E7ED; margin-bottom: 20px; } } }
</style>
