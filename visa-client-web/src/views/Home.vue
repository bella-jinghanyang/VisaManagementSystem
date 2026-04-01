<template>
  <div class="home-wrapper">
    <!-- 1. Global Visa 高级感 Hero 区域 -->
    <section class="hero-section">
      <div class="hero-container container-width">
        <div class="hero-left">
          <h1 class="hero-title">
            世界之门，<br/>
            <span class="text-highlight">智慧启程。</span>
          </h1>
          <p class="hero-subtitle">
            作为您的全球签证数字化管家，我们通过透明化的在线追踪与专业的人工预审服务，确保您的每一场跨境旅行都从容不迫。
          </p>
          <div class="hero-action-group">
            <el-button type="primary" class="main-cta" @click="$router.push('/login')">
              立即开启办理
            </el-button>
            <div class="ai-entry" @click="$router.push('ai-chat')">
              <div class="ai-icon"><i class="el-icon-cpu"></i></div>
              <span>咨询 AI 助手</span>
            </div>
          </div>
        </div>

        <div class="hero-right">
          <div class="hero-main-visual">
            <!-- 建议使用一张带点朦胧感的机场或城市远景图 -->
            <img :src="heroImageUrl" alt="Global Visa Service" />
            <div class="floating-card">
              <div class="user-avatar">GV</div>
              <div class="status-info">
                <p>您的签证进度已更新</p>
                <span>刚刚 · 实时同步中</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. 纵向分块流程：序号图框 + 文字描述框 -->
    <section class="workflow-section">
      <div class="container-width">
        <div class="section-header">
          <span class="sub-title">SERVICE PROCESS</span>
          <h3>全流程数字化代办</h3>
          <p>从下单到出签，每一步都清晰可见，专业人工全程护航</p>
        </div>

        <div class="vertical-steps">
          <div v-for="(step, index) in flowSteps" :key="index" class="step-item-row">
            <!-- A. 序号图框：背景是精美图片 -->
            <div class="step-num-box" :style="{ backgroundImage: `url(${step.bgImage})` }">
              <div class="overlay"></div>
              <span class="num-text">0{{ index + 1 }}</span>
            </div>

            <!-- B. 视觉连线 -->
            <div class="step-line-container">
              <div class="node-dot"></div>
              <div v-if="index < flowSteps.length - 1" class="connecting-line"></div>
            </div>

            <!-- C. 步骤描述框 -->
            <div class="step-content-card">
              <div class="card-header">
                <i :class="step.icon"></i>
                <h4>{{ step.title }}</h4>
              </div>
              <p>{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
/* eslint-disable */
export default {
  data() {
    return {
      heroImageUrl: require('@/assets/images/hero-right.jpg'),
      flowSteps: [
        { 
          title: '提交订单', desc: '在 Global Visa 平台选择目的地及签证类型，填写基础意向并一键下单。', 
          icon: 'el-icon-shopping-cart-2',
          bgImage: require('@/assets/images/step1.jpg') 
        },
        { 
          title: '递交材料', desc: '根据系统生成的动态清单，在线上传电子版扫描件，无需寄送原件至初审阶段。', 
          icon: 'el-icon-upload2',
          bgImage: require('@/assets/images/step2.jpg') 
        },
        { 
          title: '专家审核', desc: 'Global Visa 资深办签员进行人工双重核验，确保材料完全符合领事馆最新政策。', 
          icon: 'el-icon-set-up',
          bgImage: require('@/assets/images/step3.jpg') 
        },
        { 
          title: '使馆送签', desc: '我们将您的护照及相关文件安全送达大使馆，并在系统中实时更新受理节点。', 
          icon: 'el-icon-office-building',
          bgImage: require('@/assets/images/step4.jpg') 
        },
        { 
          title: '出签反馈', desc: '第一时间同步使馆签发动态，您可以在用户中心查阅签证电子联。', 
          icon: 'el-icon-circle-check',
          bgImage: require('@/assets/images/step5.jpg')
        },
        { 
          title: '极速配送', desc: '证件原件由顺丰安全寄回，全流程保险覆盖，让证件安全交还至您手中。', 
          icon: 'el-icon-truck',
          bgImage: require('@/assets/images/step6.jpg')
        }
      ]
    }
  },
  methods: {
    handleAiChat() {
      // 触发你的 AI 助手逻辑
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

/* --- Hero 区域：Global Visa 品牌感 --- */
.hero-section {
  padding: 120px 0;
  background: linear-gradient(to bottom, #ffffff, #f8fafc);
  
  .hero-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 80px;
  }

  .hero-left {
    flex: 1.2;
    .brand-tag {
      font-weight: 800;
      color: $primary-color;
      letter-spacing: 3px;
      margin-bottom: 20px;
      font-size: 14px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .hero-title {
      font-size: 72px;
      font-weight: 800;
      color: #0f172a;
      line-height: 1.1;
      margin-bottom: 30px;
      .text-highlight { color: $primary-color; }
    }
    .hero-subtitle {
      font-size: 19px;
      color: #64748b;
      line-height: 1.8;
      margin-bottom: 40px;
      max-width: 550px;
    }
    
    .hero-action-group {
      display: flex;
      align-items: center;
      gap: 30px;
      .main-cta {
        padding: 18px 40px;
        font-size: 16px;
        font-weight: 700;
        border-radius: 12px;
        box-shadow: 0 10px 25px rgba(0, 98, 255, 0.25);
      }
      .ai-entry {
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        font-weight: 600;
        color: #475569;
        &:hover { color: $primary-color; }
        .ai-icon {
          width: 40px; height: 40px;
          background: rgba($primary-color, 0.1);
          border-radius: 10px;
          display: flex; align-items: center; justify-content: center;
          color: $primary-color;
        }
      }
    }
  }

  .hero-right {
    flex: 1;
    .hero-main-visual {
      position: relative;
      img {
        width: 100%;
        border-radius: 40px;
        box-shadow: 0 30px 60px rgba(0,0,0,0.12);
      }
      .floating-card {
        position: absolute;
        bottom: 40px; left: -40px;
        background: rgba(255, 255, 255, 0.9);
        backdrop-filter: blur(10px);
        padding: 15px 25px;
        border-radius: 20px;
        display: flex; align-items: center; gap: 15px;
        box-shadow: 0 15px 35px rgba(0,0,0,0.1);
        .user-avatar {
          width: 44px; height: 44px; background: $primary-color;
          color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center;
          font-weight: 800; font-size: 12px;
        }
        .status-info {
          p { font-size: 14px; font-weight: 700; color: #1e293b; margin: 0; }
          span { font-size: 12px; color: #94a3b8; }
        }
      }
    }
  }
}

/* --- 纵向流程区域 --- */
.workflow-section {
  padding: 120px 0;
  background: #fff;

  .section-header {
    text-align: center;
    margin-bottom: 90px;
    .sub-title { font-weight: 800; color: $primary-color; letter-spacing: 2px; font-size: 13px; }
    h3 { font-size: 38px; font-weight: 800; margin: 15px 0; color: #0f172a; }
    p { color: #64748b; font-size: 16px; }
  }

  .vertical-steps {
    max-width: 1000px;
    margin: 0 auto;
    padding-left: 20px;
  }

  .step-item-row {
    display: flex;
    align-items: stretch;
    height: 180px; // 控制整体节奏
  }

  /* A. 序号图框 */
  .step-num-box {
    width: 130px; height: 130px;
    border-radius: 24px;
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: hidden;
    flex-shrink: 0;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 10px 20px rgba(0,0,0,0.1);
    
    .overlay {
      position: absolute; inset: 0;
      background: linear-gradient(135deg, rgba($primary-color, 0.8) 0%, rgba(15, 23, 42, 0.4) 100%);
    }
    .num-text {
      position: relative; z-index: 2;
      color: #fff; font-size: 36px; font-weight: 900; font-family: 'Inter', sans-serif;
    }
  }

  /* B. 连接线设计 */
  .step-line-container {
    width: 100px;
    display: flex; flex-direction: column; align-items: center;
    .node-dot {
      width: 12px; height: 12px; background: $primary-color; border-radius: 50%;
      margin-top: 59px; z-index: 5;
      box-shadow: 0 0 0 4px rgba($primary-color, 0.1);
    }
    .connecting-line {
      flex: 1; width: 2px; background: #f1f5f9; margin: 10px 0;
    }
  }

  /* C. 内容卡片 */
  .step-content-card {
    flex: 1;
    background: #f8fafc;
    border-radius: 24px;
    padding: 30px 40px;
    margin-bottom: 50px;
    display: flex; flex-direction: column; justify-content: center;
    border: 1px solid transparent;
    transition: all 0.3s ease;

    .card-header {
      display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
      i { color: $primary-color; font-size: 22px; }
      h4 { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0; }
    }
    p { color: #64748b; font-size: 15px; line-height: 1.7; margin: 0; }

    &:hover {
      background: #fff;
      border-color: rgba($primary-color, 0.2);
      box-shadow: 0 20px 40px rgba(0,0,0,0.05);
      transform: translateX(10px);
    }
  }
}
</style>