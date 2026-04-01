<template>
  <div class="create-order-page container-width" v-if="product.id">
    <div class="page-header">
      <h2>确认订单信息</h2>
      <p>请核对您的订单详情并填写回寄地址</p>
    </div>

    <div class="main-layout">
      <!-- 左侧：基础信息填写 -->
      <div class="left-form">
        <!-- 申请人信息动态卡片 -->
        <apple-card v-for="index in quantity" :key="'app' + index" class="mb-30">
          <h3 class="section-title">申请人 #{{ index }} 基本信息</h3>
          <el-form label-position="top">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="真实姓名" required>
                  <el-input v-model="applicantList[index - 1].name" placeholder="请输入姓名(与护照一致)"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身份类型" required>
                  <el-select v-model="applicantList[index - 1].identity" placeholder="请选择身份" style="width: 100%">
                    <el-option label="在职人员" value="EMPLOYED" />
                    <el-option label="自由职业" value="FREELANCE" />
                    <el-option label="无业人员" value="UNEMPLOYED" />
                    <el-option label="学生" value="STUDENT" />
                    <el-option label="退休人员" value="RETIRED" />
                    <el-option label="学龄前儿童" value="CHILD" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="身份证号">
                  <el-input v-model="applicantList[index - 1].idCard" placeholder="请输入18位身份证号"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="护照号码">
                  <el-input v-model="applicantList[index - 1].passportNo" placeholder="请输入护照号"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </apple-card>

        <!-- 1. 邮寄信息卡片 -->
        <apple-card class="mb-30">
          <h3 class="section-title">护照回寄地址</h3>
          <el-form :model="addressForm" label-position="top">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="收件人姓名" required>
                  <el-input v-model="addressForm.contactName" placeholder="请填写收件人真实姓名"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" required>
                  <el-input v-model="addressForm.contactPhone" placeholder="手机号码"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="详细收货地址" required>
              <el-input v-model="addressForm.address" type="textarea" :rows="3"
                placeholder="请输入详细的省市区及街道门牌号，确保出签后护照能准确送达"></el-input>
            </el-form-item>
          </el-form>
        </apple-card>

        <!-- 2. 份数展示卡片 -->
        <apple-card>
          <h3 class="section-title">办理人数</h3>
          <div class="qty-display">
            <span>当前选择办理人数：</span>
            <span class="qty-count">{{ quantity }} 位</span>
          </div>
          <p class="notice-text">
            <i class="el-icon-info"></i>
            支付成功后，您可以在“我的订单”中为这 {{ quantity }} 位申请人分别上传办签材料。
          </p>
        </apple-card>
      </div>

      <!-- 右侧：结算中心 -->
      <div class="right-summary">
        <apple-card class="sticky-card">
          <div class="product-mini">
            <img :src="$getImgUrl(product.image)" class="prod-img">
            <div class="prod-text">
              <h4>{{ product.title }}</h4>
              <el-tag size="mini" type="info">{{ product.typeName }}</el-tag>
            </div>
          </div>

          <div class="divider"></div>

          <div class="price-detail">
            <div class="price-row">
              <span>商品单价</span>
              <span>￥{{ product.price }}</span>
            </div>
            <div class="price-row">
              <span>办理人数</span>
              <span>x {{ quantity }}</span>
            </div>
          </div>

          <div class="divider"></div>

          <div class="total-row">
            <span class="label">应付总额</span>
            <span class="total-amount">￥{{ (product.price * quantity).toFixed(2) }}</span>
          </div>

          <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleOrderSubmit">
            提交订单并支付
          </el-button>

          <p class="safe-tips">安全支付保障 · 官方直办</p>
        </apple-card>
      </div>
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import AppleCard from '@/components/AppleCard'
import { getProduct } from '@/api/product'
import { listMyOrders, getStripePayUrl, submitOrder } from '@/api/order';

export default {
  name: 'OrderCreate',
  components: { AppleCard },
  data() {
    return {
      submitting: false,
      product: {},
      quantity: 1,
      // 收货地址表单
      addressForm: {
        contactName: '',
        contactPhone: '',
        address: ''
      },
      applicantList: [], // 申请人数组
      
    }
  },
  created() {
    // 1. 获取地址栏传过来的 productId 和 quantity
    const pid = this.$route.query.productId
    this.quantity = parseInt(this.$route.query.quantity) || 1

    if (!pid) {
      this.$message.error('订单参数错误')
      this.$router.push('/products')
      return
    }

    // 2. 加载产品详情
    getProduct(pid).then(res => {
      this.product = res.data
    })

    // 3. 自动从缓存里拿当前用户信息回填，省去用户输入
    const userStr = localStorage.getItem('Client-User')
    if (userStr) {
      const user = JSON.parse(userStr)
      this.addressForm.contactName = user.realName || ''
      this.addressForm.contactPhone = user.phone || ''
    }

    
    // 根据购买人数初始化申请人列表
    this.applicantList = [];
    for (let i = 0; i < this.quantity; i++) {
      this.applicantList.push({
        name: '',
        identity: 'EMPLOYED', // 默认选“在职”，因为这是最主流的人群
        idCard: '',
        passportNo: ''
      });
    }
  },
  methods: {
    handleOrderSubmit() {
     // 1. 校验申请人信息是否填完整
      for (let i = 0; i < this.applicantList.length; i++) {
        if (!this.applicantList[i].name) {
          this.$message.warning(`请填写申请人 #${i+1} 的姓名`);
          return;
        }
        if (!this.applicantList[i].identity) {
          this.$message.warning(`请选择申请人 #${i+1} 的身份类型`);
          return;
        }
      }

      if (!this.addressForm.contactName || !this.addressForm.contactPhone || !this.addressForm.address) {
        this.$message.warning('请填写完整的收货信息')
        return
      }

      this.submitting = true
      const user = JSON.parse(localStorage.getItem('Client-User'))

      // 1. 构造数据，这里不需要传 orderNo，让后端生成
      const postData = {
        productId: this.product.id,
        customerId: user.id,
        quantity: this.quantity,
        mailingAddress: JSON.stringify(this.addressForm),
        applicantList: this.applicantList
      }

      console.log("步骤1: 提交订单到后端...");

      submitOrder(postData).then(res => {
        const realOrderNo = res.data;

        if (!realOrderNo) {
          console.error("后端未返回正确的单号，res内容：", res);
          this.$message.error("系统单号获取异常");
          return;
        }

        console.log("步骤2: 拿到数据库真实单号:", realOrderNo);
        this.$message.success('订单已生成，准备跳转支付...');

        // 3. 延时一下，拿着【真实单号】去请求支付
        setTimeout(() => {
          getStripePayUrl(realOrderNo).then(payRes => {
            if (payRes.code === 200) {
              window.location.href = payRes.data;
            } else {
              this.$message.error(payRes.msg);
            }
          });
        }, 500);

      }).catch(err => {
        this.$message.error('提交失败');
      }).finally(() => {
        this.submitting = false;
      });
    }
  }
}
</script>

<style scoped lang="scss">
/* eslint-disable */
@import "@/assets/styles/variables.scss";

.create-order-page {
  padding: 40px 0 100px;
}

.page-header {
  margin-bottom: 30px;

  h2 {
    font-size: 28px;
    font-weight: 700;
  }

  p {
    color: $text-secondary;
  }
}

.main-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.left-form {
  flex: 1;
}

.right-summary {
  width: 380px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  border-left: 4px solid $primary-color;
  padding-left: 12px;
}

.qty-display {
  padding: 20px;
  background: #F7F9FC;
  border-radius: 12px;
  margin-bottom: 15px;

  .qty-count {
    font-size: 24px;
    font-weight: 800;
    color: $primary-color;
  }
}

.notice-text {
  font-size: 13px;
  color: #909399;

  i {
    color: $primary-color;
    margin-right: 5px;
  }
}

.sticky-card {
  position: sticky;
  top: 110px;
}

.product-mini {
  display: flex;
  gap: 15px;
  align-items: center;

  .prod-img {
    width: 80px;
    height: 60px;
    border-radius: 10px;
    object-fit: cover;
  }

  h4 {
    margin-bottom: 5px;
    font-size: 16px;
  }
}

.divider {
  height: 1px;
  background: #eee;
  margin: 20px 0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: $text-secondary;
  font-size: 14px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;

  .label {
    font-weight: 600;
  }

  .total-amount {
    font-size: 32px;
    font-weight: 800;
    color: #ff6b6b;
  }
}

.submit-btn {
  width: 100%;
  height: 56px;
  font-size: 18px;
  margin-top: 30px;
  border-radius: 16px !important;
}

.safe-tips {
  text-align: center;
  color: #C0C4CC;
  font-size: 12px;
  margin-top: 15px;
}
</style>
