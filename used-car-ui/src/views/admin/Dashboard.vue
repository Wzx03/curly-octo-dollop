<template>
  <div class="dashboard-container">
    <!-- 1. 顶部核心指标 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="(item, index) in cards" :key="index">
        <el-card shadow="hover" class="data-card" :class="'card-' + index">
          <div class="card-header">
            <span class="card-title">{{ item.title }}</span>
            <!-- 样式软化：淡色背景 -->
            <span class="soft-tag" :class="item.tagType">{{ item.tagText }}</span>
          </div>
          <div class="card-num">
            <span v-if="item.prefix" class="prefix">{{ item.prefix }}</span>
            {{ item.value }}
            <span v-if="item.suffix" class="suffix">{{ item.suffix }}</span>
          </div>
          <div class="card-footer">
            <span>{{ $t('dashboard.vs_yesterday') }}</span>
            <span :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
              {{ item.trend >= 0 ? '+' : '' }}{{ item.trend }}%
              <el-icon><component :is="item.trend >= 0 ? 'Top' : 'Bottom'" /></el-icon>
            </span>
          </div>
          <!-- 装饰性图标 -->
          <el-icon class="decor-icon"><component :is="item.icon" /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 核心图表区 -->
    <el-row :gutter="20" class="mb-20">
      <!-- 左侧：品牌分布 (环形图) -->
      <el-col :span="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>🚗 {{ $t('dashboard.brand_dist') }}</span>
            </div>
          </template>
          <div ref="roseChartRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <!-- 右侧：交易趋势 (平滑面积图) -->
      <el-col :span="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>📈 {{ $t('dashboard.sales_trend') }}</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button label="volume">{{ $t('dashboard.volume') }}</el-radio-button>
                <el-radio-button label="amount">{{ $t('dashboard.amount') }}</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 3. 详细数据区 -->
    <el-row :gutter="20">
      <!-- 左侧：最新交易记录 -->
      <el-col :span="14">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <div class="chart-header">
              <span>💰 {{ $t('dashboard.latest_orders') }}</span>
              <el-button link type="primary">{{ $t('dashboard.view_all') }}</el-button>
            </div>
          </template>
          
          <!-- 空状态处理 -->
          <el-empty v-if="latestOrders.length === 0" description="暂无交易记录" :image-size="100" />
          
          <el-table v-else :data="latestOrders" style="width: 100%" :show-header="true">
            <el-table-column :label="$t('dashboard.buyer')" width="180">
              <template #default="{row}">
                <div class="user-info">
                  <el-avatar :size="30" :src="row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  <span class="nickname">{{ row.nickname }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="carModel" :label="$t('dashboard.car_model')" show-overflow-tooltip />
            <el-table-column prop="price" :label="$t('dashboard.deal_price')" width="120">
              <template #default="{row}">
                <span style="color: #f56c6c; font-weight: bold">{{ formatPrice(row.price) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="time" :label="$t('dashboard.deal_time')" width="100" align="right" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：价格区间 (水平条形图) -->
      <el-col :span="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>📊 {{ $t('dashboard.price_dist') }}</span>
            </div>
          </template>
          <div ref="radarChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import { Top, Bottom, Money, User, Van, Star } from '@element-plus/icons-vue'
import { getToken } from '../../utils/auth'
import { useI18n } from 'vue-i18n'
import { useCurrency } from '../../hooks/useCurrency'

const { t, locale } = useI18n()
const { formatPrice, currentCurrency } = useCurrency()

// --- 1. 顶部卡片数据 ---
const cards = ref([
  { title: t('dashboard.total_sales'), value: '0', prefix: '', suffix: '', trend: 12.5, tagText: 'Monthly', tagType: 'primary', icon: 'Money' },
  { title: t('dashboard.active_users'), value: '0', suffix: '', trend: 5.2, tagText: 'Active', tagType: 'success', icon: 'User' },
  { title: t('dashboard.cars_on_sale'), value: '0', suffix: '', trend: -2.1, tagText: 'Stock', tagType: 'warning', icon: 'Van' },
  { title: t('dashboard.new_cars_today'), value: '0', suffix: '', trend: 24.0, tagText: 'New', tagType: 'danger', icon: 'Star' }
])

// 监听语言/货币变化
watch([locale, currentCurrency], () => {
  updateCards()
  if (lastData) initCharts(lastData)
})

const updateCards = () => {
  if (!lastData) return
  
  cards.value[0].title = t('dashboard.total_sales')
  // 👇 修复：显示单位 (true)
  cards.value[0].value = formatPrice(lastData.totalAmount, true)
  
  cards.value[1].title = t('dashboard.active_users')
  cards.value[1].value = lastData.totalUsers
  
  cards.value[2].title = t('dashboard.cars_on_sale')
  cards.value[2].value = lastData.totalCars
  
  cards.value[3].title = t('dashboard.new_cars_today')
  cards.value[3].value = lastData.todayNewCars
}

// --- 2. 图表引用 ---
const roseChartRef = ref(null)
const lineChartRef = ref(null)
const radarChartRef = ref(null) // 实际是 Bar Chart
let roseChart = null
let lineChart = null
let radarChart = null
let lastData = null

const trendType = ref('volume')
const latestOrders = ref([])
let timer = null

// --- 4. 初始化图表 ---
const initCharts = (data) => {
  lastData = data
  
  // A. 环形图 (品牌分布)
  if (roseChartRef.value) {
    if (!roseChart) roseChart = echarts.init(roseChartRef.value)
    roseChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { 
        orient: 'vertical', 
        right: 10, 
        top: 'center',
        itemWidth: 10,
        itemHeight: 10
      },
      color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'],
      series: [
        {
          name: t('dashboard.brand_dist'),
          type: 'pie',
          radius: ['40%', '70%'], // 环形
          center: ['40%', '50%'], // 左移一点给 legend 腾空间
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: { show: false },
          data: data.brandChart || []
        }
      ]
    })
  }

  // B. 平滑面积图 (交易趋势)
  if (lineChartRef.value) {
    if (!lineChart) lineChart = echarts.init(lineChartRef.value)
    
    let seriesData = data.trendVolumes || []
    
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.trendDates || [],
        axisLine: { lineStyle: { color: '#ccc' } },
        axisLabel: { color: '#666' }
      },
      yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
      },
      series: [
        {
          name: trendType.value === 'volume' ? t('dashboard.volume') : t('dashboard.amount'),
          type: 'line',
          smooth: true, // 平滑曲线
          symbol: 'none',
          lineStyle: { width: 3, color: '#409EFF' },
          areaStyle: {
            opacity: 0.8,
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.4)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ])
          },
          data: seriesData
        }
      ]
    })
  }

  // C. 水平条形图 (价格区间) - 替代雷达图
  if (radarChartRef.value) {
    if (!radarChart) radarChart = echarts.init(radarChartRef.value)
    
    // 构造数据
    const pUnit = t('car.price_unit')
    const categories = ['< 5' + pUnit, '5-10' + pUnit, '10-20' + pUnit, '20-50' + pUnit, '50+' + pUnit]
    // 假设 data.priceChart 是 [{name: '<5', value: 10}, ...] 格式，或者直接是 values
    // 这里为了演示，假设 data.priceChart 是雷达图格式，我们需要提取 value
    // 实际后端返回可能需要适配。这里假设 data.priceChart 是一个数组，对应上面的 categories
    // 如果后端返回的是雷达图格式 [{value: [...], name: ...}]，我们取 value 数组
    let barData = []
    if (data.priceChart && data.priceChart[0] && Array.isArray(data.priceChart[0].value)) {
        barData = data.priceChart[0].value
    } else {
        barData = [0, 0, 0, 0, 0] // Fallback
    }
    
    radarChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value', splitLine: { show: false } },
      yAxis: { 
        type: 'category', 
        data: categories,
        axisTick: { show: false },
        axisLine: { show: false }
      },
      series: [
        {
          name: t('dashboard.price_dist'),
          type: 'bar',
          data: barData,
          itemStyle: {
            borderRadius: [0, 20, 20, 0], // 右侧圆角
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          barWidth: 20
        }
      ]
    })
  }
}

const handleResize = () => {
  roseChart?.resize()
  lineChart?.resize()
  radarChart?.resize()
}

const loadStats = async () => {
  try {
    const res = await axios.get('/api/admin/dashboard/stats', {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      const d = res.data.data
      lastData = d
      updateCards()
      latestOrders.value = d.latestOrders || []
      initCharts(d)
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  nextTick(() => {
    loadStats()
    window.addEventListener('resize', handleResize)
    timer = setInterval(loadStats, 5000)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (timer) clearInterval(timer)
  roseChart?.dispose()
  lineChart?.dispose()
  radarChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { 
  padding: 24px; 
  background-color: #f7f9fc; 
  height: 100%; 
  overflow-y: auto; 
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}
.mb-20 { margin-bottom: 24px; }

/* 顶部卡片 */
.data-card {
  border: none;
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;
  background: white;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}
.data-card:hover { transform: translateY(-4px); box-shadow: 0 8px 25px rgba(0,0,0,0.1); }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.card-title { font-size: 14px; color: #606266; font-weight: 500; }

/* 软化标签样式 */
.soft-tag {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.soft-tag.primary { background: #ecf5ff; color: #409eff; }
.soft-tag.success { background: #f0f9eb; color: #67c23a; }
.soft-tag.warning { background: #fdf6ec; color: #e6a23c; }
.soft-tag.danger { background: #fef0f0; color: #f56c6c; }

.card-num { font-size: 36px; font-weight: 800; color: #1a1a1a; margin-bottom: 16px; letter-spacing: -1px; }
.prefix, .suffix { font-size: 16px; font-weight: 600; margin: 0 4px; color: #909399; }

.card-footer { font-size: 13px; color: #909399; display: flex; align-items: center; gap: 6px; }
.trend-up { color: #f56c6c; font-weight: 700; display: flex; align-items: center; }
.trend-down { color: #67c23a; font-weight: 700; display: flex; align-items: center; }

/* 装饰图标 */
.decor-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 80px;
  opacity: 0.1;
  color: #000;
  transform: rotate(-15deg);
}

/* 图表卡片 */
.chart-card { border-radius: 12px; border: none; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }
.chart-header { display: flex; justify-content: space-between; align-items: center; font-weight: 700; font-size: 16px; color: #1a1a1a; }
.chart-box { height: 320px; width: 100%; }

/* 表格卡片 */
.table-card { border-radius: 12px; border: none; height: 100%; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }
.user-info { display: flex; align-items: center; gap: 12px; }
.nickname { font-size: 14px; font-weight: 600; color: #333; }
</style>