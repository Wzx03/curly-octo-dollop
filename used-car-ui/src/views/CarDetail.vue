<template>
  <div class="detail-container" v-if="car">
    <!-- 1. 顶部面包屑 -->
    <div class="breadcrumb">
      <span>{{ $t('nav.cars') }}</span>
      <el-icon><ArrowRight /></el-icon>
      <span>{{ car.brand }}</span>
      <el-icon><ArrowRight /></el-icon>
      <span class="current">{{ car.model }}</span>
    </div>

    <!-- 2. 主体内容区 -->
    <div class="main-content">
      <!-- 左侧：图片展示 -->
      <div class="gallery-section">
        <div class="main-image-box" @click="showImageViewer = true">
          <img :src="car.image || 'https://dummyimage.com/800x600/f5f5f5/999&text=No+Image'" class="main-img" />
          <div class="img-tag" v-if="car.conditionLevel === 1">{{ $t('car.condition_excellent') }}</div>
          <div class="view-btn"><el-icon><ZoomIn /></el-icon> 查看大图</div>
        </div>
        <el-image-viewer v-if="showImageViewer" @close="showImageViewer = false" :url-list="[car.image]" />
      </div>

      <!-- 右侧：核心信息 & 估价 -->
      <div class="info-section">
        <h1 class="car-title">{{ car.brand }} {{ car.model }}</h1>
        
        <div class="tags-row">
          <el-tag effect="plain" type="info">{{ car.buyYear }}{{ $t('car.year') }}</el-tag>
          <el-tag effect="plain" type="info">{{ car.mileage }}{{ $t('car.mileage_unit') }}</el-tag>
          <el-tag effect="plain" type="info">{{ car.city || 'Local' }}</el-tag>
          <el-tag effect="plain" type="success" v-if="car.energyType">{{ car.energyType }}</el-tag>
        </div>

        <!-- 价格卡片 -->
        <div class="price-card">
          <div class="price-row">
            <span class="label">{{ $t('car_detail.price') }}</span>
            <span class="current-price">{{ formatPrice(car.price) }}</span>
            <span class="original-price">{{ $t('car_detail.basic_info') }}: {{ formatPrice(car.originalPrice) }}</span>
          </div>
          
          <!-- 智能估价核心模块 (CSS 进度条版) -->
          <div class="valuation-box">
            <div class="val-header">
              <span class="val-title">
                <el-icon color="#67C23A"><TrendCharts /></el-icon>
                AI 智能估价
              </span>
              <span class="val-num">{{ formatPrice(car.estimatedPrice) }}</span>
            </div>
            
            <!-- 价格对比条 -->
            <div class="price-bar-wrapper">
              <div class="bar-bg"></div>
              
              <!-- 估价点 (基准) -->
              <div class="bar-point estimate" style="left: 50%">
                <div class="point-dot"></div>
                <div class="point-label"></div>
              </div>
              
              <!-- 报价点 (动态) -->
              <div class="bar-point current" :style="{ left: getPricePosition(car.price, car.estimatedPrice) }">
                <div class="point-dot"></div>
                <div class="point-label"></div>
                <!-- 差价提示 -->
                <div class="diff-label" :class="getDiffClass(car.price, car.estimatedPrice)">
                  {{ getDiffText(car.price, car.estimatedPrice) }}
                </div>
              </div>
            </div>

            <div class="val-analysis">
              <span :class="getAnalysisClass(car.price, car.estimatedPrice)">
                <el-icon v-if="car.price < car.estimatedPrice"><Select /></el-icon>
                {{ getAnalysisText(car.price, car.estimatedPrice) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-btns">
          <el-button type="primary" size="large" class="buy-btn" @click="handleBuy">
            {{ $t('car_detail.buy_now') }}
          </el-button>
          <el-button size="large" class="chat-btn" @click="toChat">
            <el-icon><ChatDotRound /></el-icon> {{ $t('car_detail.contact_seller') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 3. 详细参数 & 描述 & 保值率 -->
    <div class="detail-tabs-wrapper">
      <el-tabs v-model="activeTab" class="detail-tabs">
        
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane :label="$t('car_detail.basic_info')" name="specs">
          <div class="specs-container">
            <!-- 核心参数网格 -->
            <div class="spec-grid">
              <div class="spec-item">
                <el-icon class="spec-icon"><Van /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">厂商属性</span>
                  <span class="spec-value">{{ car.manufacturerType }}</span>
                </div>
              </div>
              <div class="spec-item">
                <el-icon class="spec-icon"><Lightning /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">能源类型</span>
                  <span class="spec-value">{{ car.energyType }}</span>
                </div>
              </div>
              <div class="spec-item">
                <el-icon class="spec-icon"><Odometer /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">排量</span>
                  <span class="spec-value">{{ car.displacement }}</span>
                </div>
              </div>
              <div class="spec-item">
                <el-icon class="spec-icon"><Connection /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">变速箱</span>
                  <span class="spec-value">{{ car.gearbox }}</span>
                </div>
              </div>
              <div class="spec-item">
                <el-icon class="spec-icon"><Aim /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">驱动方式</span>
                  <span class="spec-value">{{ car.driveMode }}</span>
                </div>
              </div>
              <div class="spec-item">
                <!-- 👇 修复：替换 Leaf 为 Sunny -->
                <el-icon class="spec-icon"><Sunny /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">排放标准</span>
                  <span class="spec-value">{{ car.emissionStandard }}</span>
                </div>
              </div>
              <div class="spec-item">
                <el-icon class="spec-icon"><Brush /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">车身颜色</span>
                  <span class="spec-value">{{ car.color || '-' }}</span>
                </div>
              </div>
              <div class="spec-item">
                <el-icon class="spec-icon"><Timer /></el-icon>
                <div class="spec-text">
                  <span class="spec-label">发布时间</span>
                  <span class="spec-value">{{ $d(new Date(car.createTime), 'short') }}</span>
                </div>
              </div>
            </div>

            <el-divider />

            <!-- 车辆高亮配置 -->
            <div class="highlight-section">
              <h3 class="section-title">车辆亮点配置</h3>
              <div class="highlight-tags">
                <el-tag type="success" effect="light" round>无事故</el-tag>
                <el-tag type="warning" effect="light" round>4S店保养</el-tag>
                <el-tag v-if="car.price > 20" effect="light" round>全景天窗</el-tag>
                <el-tag v-if="car.price > 30" effect="light" round>座椅加热</el-tag>
                <el-tag v-if="car.price > 40" effect="light" round>L2辅助驾驶</el-tag>
                <el-tag v-if="car.price > 50" effect="light" round>360全景影像</el-tag>
                <el-tag effect="light" round>车况{{ car.conditionGrade || 'A' }}级</el-tag>
                <el-tag effect="light" round>过户{{ car.transferCount }}次</el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 质检报告 (新增) -->
        <el-tab-pane label="质检报告" name="report">
          <div class="report-container">
            <!-- 头部评分 -->
            <div class="report-header">
              <div class="score-circle">
                <span class="score">{{ reportData.score }}</span>
                <span class="label">综合评分</span>
              </div>
              <div class="report-summary">
                <h3>{{ reportData.summary }}</h3>
                <p>检测师：{{ reportData.inspector }} · 检测时间：{{ $d(new Date(), 'short') }}</p>
                <div class="core-check">
                  <div class="check-item pass">
                    <el-icon><CircleCheckFilled /></el-icon> 无重大事故
                  </div>
                  <div class="check-item pass">
                    <el-icon><CircleCheckFilled /></el-icon> 无火烧
                  </div>
                  <div class="check-item pass">
                    <el-icon><CircleCheckFilled /></el-icon> 无泡水
                  </div>
                </div>
              </div>
            </div>

            <el-divider />

            <!-- 详细检测项 -->
            <div class="report-details">
              <div class="detail-group" v-for="(group, index) in reportData.items" :key="index">
                <div class="group-title">{{ group.name }}</div>
                <div class="group-items">
                  <div class="item-row" v-for="(item, i) in group.list" :key="i">
                    <span class="item-name">{{ item.name }}</span>
                    <span class="item-status" :class="item.status === '正常' ? 'pass' : 'warn'">
                      {{ item.status }}
                      <el-icon v-if="item.status === '正常'"><Check /></el-icon>
                      <el-icon v-else><Warning /></el-icon>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 保值率预测 -->
        <el-tab-pane label="保值率预测" name="trend">
          <div class="trend-container">
            <div class="trend-header">
              <h3>未来价值走势</h3>
              <p>基于大数据算法与市场行情分析，仅供参考。</p>
            </div>
            <div ref="trendChartRef" class="trend-chart"></div>
          </div>
        </el-tab-pane>

        <!-- Tab 4: 卖家描述 -->
        <el-tab-pane :label="$t('car_detail.description')" name="desc">
          <div class="desc-content">
            <div class="desc-text">{{ car.description || $t('common.no_data') }}</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowRight, TrendCharts, Select, ChatDotRound, ZoomIn,
  Van, Odometer, Connection, Aim, Sunny, Brush, Timer, Lightning, // 👈 修复：引入 Sunny
  CircleCheckFilled, Check, Warning
} from '@element-plus/icons-vue'
import { getToken } from '../utils/auth'
import { useI18n } from 'vue-i18n'
import { useCurrency } from '../hooks/useCurrency'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const { formatPrice, getPriceValue, currentCurrency } = useCurrency()

const car = ref(null)
const activeTab = ref('specs')
const showImageViewer = ref(false)

// 图表引用
const trendChartRef = ref(null)
let trendChart = null

// 模拟质检报告数据
const reportData = computed(() => {
  if (!car.value) return {}
  const isExcellent = car.value.conditionLevel === 1
  return {
    score: isExcellent ? 98 : 88,
    summary: isExcellent ? '车况极佳，接近新车状态' : '车况良好，主要部件正常',
    inspector: '高级检测师-王工',
    items: [
      {
        name: '外观内饰',
        list: [
          { name: '漆面光泽度', status: '正常' },
          { name: '座椅磨损', status: isExcellent ? '正常' : '轻微磨损' },
          { name: '内饰清洁', status: '正常' }
        ]
      },
      {
        name: '发动机舱',
        list: [
          { name: '机油液位', status: '正常' },
          { name: '冷却系统', status: '正常' },
          { name: '发动机运行', status: '正常' }
        ]
      },
      {
        name: '底盘悬挂',
        list: [
          { name: '轮胎磨损', status: '正常' },
          { name: '刹车片厚度', status: '正常' },
          { name: '悬挂系统', status: '正常' }
        ]
      },
      {
        name: '电子设备',
        list: [
          { name: '灯光系统', status: '正常' },
          { name: '空调系统', status: '正常' },
          { name: '中控屏幕', status: '正常' }
        ]
      }
    ]
  }
})

const loadDetail = async () => {
  try {
    const res = await axios.get(`/api/car/${route.params.id}`, {
      headers: { token: getToken() }
    })
    car.value = res.data.data
    nextTick(() => {
      initTrendChart()
    })
  } catch (e) {}
}

// 计算报价在进度条上的位置 (50% 是估值基准)
const getPricePosition = (price, estimate) => {
  if (!estimate) return '50%'
  const ratio = price / estimate
  let percent = 50 + (ratio - 1) * 100
  percent = Math.max(10, Math.min(90, percent))
  return percent + '%'
}

const getDiffText = (price, estimate) => {
  const diff = price - estimate
  if (Math.abs(diff) < 0.01) return '持平'
  return diff > 0 ? `高 ${formatPrice(Math.abs(diff))}` : `低 ${formatPrice(Math.abs(diff))}`
}

const getDiffClass = (price, estimate) => {
  return price > estimate ? 'diff-high' : 'diff-low'
}

const getAnalysisText = (price, estimate) => {
  const ratio = price / estimate
  const diff = Math.abs(estimate - price)
  const diffStr = formatPrice(diff, false)
  
  if (ratio < 0.95) return `低于估值 ${formatPrice(diff)}，超值推荐！`
  if (ratio > 1.05) return `高于估值 ${formatPrice(diff)}，建议议价`
  return `价格合理，符合市场行情`
}

const getAnalysisClass = (price, estimate) => {
  const ratio = price / estimate
  if (ratio < 0.95) return 'good-deal'
  if (ratio > 1.05) return 'bad-deal'
  return 'normal-deal'
}

// 保值率折线图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  
  const currentVal = getPriceValue(car.value.estimatedPrice)
  const years = ['Now', '1 Year', '2 Years', '3 Years', '4 Years', '5 Years']
  const data = []
  
  // 模拟折旧算法：每年递减 12%
  for (let i = 0; i < 6; i++) {
    data.push((currentVal * Math.pow(0.88, i)).toFixed(1))
  }

  // 计算 Y 轴最小值，让曲线更饱满
  const minVal = Math.floor(data[data.length - 1] * 0.8)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: '15%', left: '1%', right: '3%', bottom: '5%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: years,
      boundaryGap: false, 
      axisLine: { lineStyle: { color: '#999' } }
    },
    yAxis: { 
      type: 'value', 
      name: t('car.price_unit'),
      min: minVal, 
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      data: data,
      type: 'line',
      smooth: true,
      symbolSize: 8, 
      lineStyle: { color: '#409EFF', width: 4 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.0)' }
        ])
      }
    }]
  })
}

const handleBuy = () => {
  ElMessageBox.confirm(t('common.confirm'), t('common.tips'), { type: 'success' }).then(async () => {
    try {
      const res = await axios.post('/api/order/create', { carId: car.value.id }, {
        headers: { token: getToken() }
      })
      if (res.data.code === 200) {
        ElMessage.success(t('car_detail.order_created'))
        router.push('/my-orders')
      } else {
        ElMessage.error(res.data.message || t('car_detail.create_order_fail'))
      }
    } catch (e) {}
  })
}

const toChat = () => {
  router.push(`/service?targetId=${car.value.userId}&carId=${car.value.id}`)
}

// 监听货币/语言变化，重绘图表
watch([locale, currentCurrency], () => {
  if (car.value) {
    nextTick(() => {
      initTrendChart()
    })
  }
})

// 监听 Tab 切换，如果是 trend，需要 resize
watch(activeTab, (val) => {
  if (val === 'trend') {
    nextTick(() => {
      trendChart?.resize()
    })
  }
})

onMounted(() => {
  loadDetail()
  window.addEventListener('resize', () => {
    trendChart?.resize()
  })
})

onUnmounted(() => {
  trendChart?.dispose()
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  /* height: 100%; /* 移除高度限制，让内容撑开 */
  /* overflow-y: auto; /* 移除内部滚动 */
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin-bottom: 24px;
}
.breadcrumb .current { color: #222; font-weight: 600; }

/* 主体布局 */
.main-content {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

/* 左侧图片 */
.gallery-section {
  flex: 1.2;
}
.main-image-box {
  width: 100%;
  aspect-ratio: 4/3;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
  cursor: zoom-in;
}
.main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.main-image-box:hover .main-img { transform: scale(1.02); }

.img-tag {
  position: absolute;
  top: 16px;
  left: 16px;
  background: rgba(255, 255, 255, 0.95);
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  color: #222;
  font-size: 14px;
}
.view-btn {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 右侧信息 */
.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.car-title {
  font-size: 28px;
  font-weight: 800;
  color: #222;
  margin: 0 0 16px 0;
  line-height: 1.3;
}

.tags-row {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
}

/* 价格卡片 */
.price-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 20px;
}
.price-row .label { font-size: 14px; color: #666; margin-right: 8px; }
.current-price { font-size: 36px; font-weight: 800; color: #FF385C; line-height: 1; margin-right: 16px; }
.original-price { font-size: 14px; color: #999; text-decoration: line-through; }

/* 智能估价模块 */
.valuation-box {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #ebebeb;
}
.val-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.val-title { display: flex; align-items: center; gap: 6px; font-weight: 600; color: #222; }
.val-num { font-weight: 700; color: #67C23A; }

/* 价格对比条 */
.price-bar-wrapper {
  position: relative;
  height: 40px;
  margin-bottom: 12px;
}
.bar-bg {
  position: absolute;
  top: 18px;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #67C23A 0%, #E6A23C 50%, #F56C6C 100%);
  border-radius: 2px;
}
.bar-point {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.point-label { font-size: 12px; color: #666; margin-top: 4px; }
.point-dot { width: 12px; height: 12px; border-radius: 50%; border: 2px solid #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.2); }

.estimate .point-dot { background: #67C23A; }
.current .point-dot { background: #FF385C; width: 16px; height: 16px; top: -2px; }

/* 👇 差价标签样式 */
.diff-label {
  position: absolute;
  top: 32px; /* 在点下方 */
  font-size: 12px;
  font-weight: bold;
  white-space: nowrap;
  padding: 2px 6px;
  border-radius: 4px;
}
.diff-low { color: #67C23A; background: #f0f9eb; }
.diff-high { color: #F56C6C; background: #fef0f0; }

.val-analysis { font-size: 13px; color: #666; margin-top: 20px; }
.good-deal { color: #67C23A; font-weight: 600; display: flex; align-items: center; gap: 4px; }
.bad-deal { color: #F56C6C; font-weight: 600; }
.normal-deal { color: #409EFF; font-weight: 600; }

/* 按钮 */
.action-btns {
  display: flex;
  gap: 16px;
}
.buy-btn { flex: 2; font-weight: 600; background: #FF385C; border-color: #FF385C; }
.buy-btn:hover { background: #E00B41; border-color: #E00B41; }
.chat-btn { flex: 1; font-weight: 600; }

/* 详细参数 Tabs */
.detail-tabs-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #ebebeb;
}
.detail-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 600; }

/* 基本信息网格 */
.specs-container { padding: 10px 0; }
.spec-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4列布局 */
  gap: 24px;
  margin-bottom: 32px;
}
.spec-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.spec-icon {
  font-size: 24px;
  color: #717171;
  background: #f7f7f7;
  padding: 8px;
  border-radius: 8px;
  box-sizing: content-box;
}
.spec-text {
  display: flex;
  flex-direction: column;
}
.spec-label {
  font-size: 12px;
  color: #717171;
  margin-bottom: 2px;
}
.spec-value {
  font-size: 15px;
  color: #222;
  font-weight: 600;
}

/* 亮点配置 */
.highlight-section { margin-top: 20px; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #222; }
.highlight-tags { display: flex; gap: 12px; flex-wrap: wrap; }

/* 质检报告 */
.report-container { padding: 10px 0; }
.report-header { display: flex; gap: 32px; align-items: center; margin-bottom: 32px; }
.score-circle {
  width: 100px; height: 100px;
  border-radius: 50%;
  border: 4px solid #67C23A;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #67C23A;
}
.score-circle .score { font-size: 32px; font-weight: 800; line-height: 1; }
.score-circle .label { font-size: 12px; margin-top: 4px; }

.report-summary h3 { margin: 0 0 8px 0; font-size: 18px; }
.report-summary p { color: #717171; font-size: 14px; margin: 0 0 16px 0; }
.core-check { display: flex; gap: 24px; }
.check-item { display: flex; align-items: center; gap: 6px; font-weight: 600; }
.check-item.pass { color: #67C23A; }

.report-details { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
.group-title { font-weight: 600; margin-bottom: 16px; border-left: 4px solid #FF385C; padding-left: 10px; }
.item-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #eee; font-size: 14px; }
.item-status { display: flex; align-items: center; gap: 4px; }
.item-status.pass { color: #67C23A; }
.item-status.warn { color: #E6A23C; }

/* 保值率 */
.trend-container { padding: 10px 0; }
.trend-header h3 { font-size: 18px; margin: 0 0 8px 0; }
.trend-header p { color: #717171; font-size: 14px; margin: 0; }
.trend-chart { width: 100%; height: 320px; margin-top: 20px; }

/* 描述 */
.desc-content { padding: 10px 0; }
.desc-text { line-height: 1.8; color: #484848; font-size: 16px; white-space: pre-wrap; }
</style>