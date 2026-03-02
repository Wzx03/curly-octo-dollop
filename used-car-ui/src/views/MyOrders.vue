<template>
  <div class="admin-panel-container">
    <!-- 2. 核心卡片区域 -->
    <div class="panel-card">
      
      <!-- 顶部工具栏：Tab + 筛选 -->
      <div class="toolbar-wrapper">
        <!-- 左侧 Tab -->
        <div class="tabs-section">
          <div 
            class="custom-tab" 
            :class="{ active: activeTab === 'orders' }"
            @click="activeTab = 'orders'"
          >
            {{ $t('my_center.tab_orders') }}
          </div>
          <div 
            class="custom-tab" 
            :class="{ active: activeTab === 'favorites' }"
            @click="activeTab = 'favorites'"
          >
            {{ $t('my_center.tab_favorites') }}
          </div>
        </div>

        <!-- 右侧操作区 -->
        <div class="actions-section" v-if="activeTab === 'orders'">
          <div class="search-group">
            <el-input 
              v-model="searchQuery" 
              :placeholder="$t('my_center.search_placeholder')" 
              class="compact-input"
              clearable
              @clear="loadOrders"
              @keyup.enter="loadOrders"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          
          <div class="filter-group">
            <el-select v-model="filterStatus" :placeholder="$t('my_center.status')" class="compact-select" @change="loadOrders">
              <el-option :label="$t('my_center.filter_all')" value="all" />
              <el-option :label="$t('my_center.filter_unpaid')" value="0" />
              <el-option :label="$t('my_center.filter_completed')" value="1" />
              <el-option :label="$t('my_center.filter_cancelled')" value="2" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 3. 内容区域 -->
      <div class="content-container">
        <!-- Tab 1: 我的订单 -->
        <div v-if="activeTab === 'orders'">
          <el-table 
            :data="filteredOrderList" 
            style="width: 100%" 
            class="compact-table"
            header-row-class-name="table-header"
          >
            <!-- 1. 车辆信息列 -->
            <el-table-column :label="$t('my_center.car_info')" min-width="320">
              <template #default="{row}">
                <div class="info-box-row">
                  <div class="thumb-box">
                    <img :src="row.carImage || 'https://dummyimage.com/160x120/f0f0f0/999'" class="thumb-img" />
                  </div>
                  <div class="text-info">
                    <div class="info-title">{{ row.carBrand }} {{ row.carModel }}</div>
                    <div class="info-sub">{{ $t('my_center.order_no') }}: {{ row.orderNo }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <!-- 2. 价格 -->
            <el-table-column prop="totalPrice" :label="$t('my_center.deal_price')" width="150">
              <template #default="{row}">
                <span class="price-text">{{ formatPrice(row.totalPrice) }}</span>
              </template>
            </el-table-column>

            <!-- 3. 剩余时间 (仅待支付显示) -->
            <el-table-column :label="$t('my_center.remaining_time')" width="160">
              <template #default="{row}">
                <span v-if="row.status === 0" class="countdown">
                  {{ getRemainingTime(row.createTime) }}
                </span>
                <span v-else class="text-gray">-</span>
              </template>
            </el-table-column>

            <!-- 4. 状态 -->
            <el-table-column prop="status" :label="$t('my_center.status')" width="120">
              <template #default="{row}">
                <el-tag :type="getStatusType(row.status)" effect="light" round>
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <!-- 5. 操作 -->
            <el-table-column :label="$t('common.operation')" width="200" fixed="right" align="right">
              <template #default="{row}">
                <div class="ops-group">
                  <!-- 待支付 -->
                  <template v-if="row.status === 0">
                    <el-button type="danger" size="small" @click="openPayDialog(row.orderNo)">
                      {{ $t('my_center.pay_now') }}
                    </el-button>
                    <el-button link type="info" size="small" @click="handleCancel(row.id)">
                      {{ $t('common.cancel') }}
                    </el-button>
                  </template>
                  
                  <!-- 已完成 -->
                  <template v-if="row.status === 1">
                    <el-button link type="primary" size="small" @click="toDetail(row.carId)">
                      {{ $t('my_center.view_detail') }}
                    </el-button>
                    <el-button link type="info" size="small" @click="openComplaint(row.orderNo)">
                      {{ $t('my_center.complaint') }}
                    </el-button>
                  </template>
                  
                  <!-- 已取消 -->
                  <template v-if="row.status === 2">
                    <el-button link type="info" size="small" disabled>
                      {{ $t('my_center.filter_cancelled') }}
                    </el-button>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="filteredOrderList.length === 0" :description="$t('my_center.no_orders')" />
        </div>

        <!-- Tab 2: 我的收藏 -->
        <div v-else-if="activeTab === 'favorites'" class="favorites-container">
          <div class="favorites-grid" v-if="favoriteList.length > 0">
            <div v-for="car in favoriteList" :key="car.id" class="fav-card-wrapper">
              <CarCard :data="car" @click="toDetail(car.id)" />
              <div class="remove-fav-btn" @click.stop="removeFavorite(car.id)">
                <el-icon><Delete /></el-icon>
              </div>
            </div>
          </div>
          <el-empty v-else :description="$t('my_center.no_favorites')" />
        </div>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" :title="$t('my_center.cashier_title')" width="400px" center class="admin-dialog" align-center>
      <div class="pay-content">
        <p class="pay-tip">{{ $t('my_center.pay_tip') }}</p>
        <div class="qr-box">
          <img src="https://dummyimage.com/200x200/000/fff&text=QR+Code" />
        </div>
        <p class="order-info">{{ $t('my_center.order_no') }}: {{ currentOrderNo }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="payDialogVisible = false">{{ $t('my_center.pay_later') }}</el-button>
          <el-button type="primary" @click="confirmPay" :loading="paying">
            {{ $t('my_center.pay_finished') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 投诉弹窗 -->
    <ComplaintDialog 
      v-model:visible="complaintVisible" 
      :target-type="1" 
      :target-id="complaintTargetId"
    />

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import ComplaintDialog from '../components/ComplaintDialog.vue'
import CarCard from '../components/CarCard.vue'
import { getToken } from '../utils/auth'
import { useI18n } from 'vue-i18n'
import { useCurrency } from '../hooks/useCurrency'

const { t } = useI18n()
const { formatPrice } = useCurrency()
const router = useRouter()
const activeTab = ref('orders')
const filterStatus = ref('all')
const searchQuery = ref('')

// 订单相关
const orderList = ref([])
const payDialogVisible = ref(false)
const currentOrderNo = ref('')
const paying = ref(false)
let timer = null
const complaintVisible = ref(false)
const complaintTargetId = ref('')

// 收藏相关
const favoriteList = ref([])

const filteredOrderList = computed(() => {
  let list = orderList.value
  
  // 状态筛选
  if (filterStatus.value !== 'all') {
    list = list.filter(item => item.status === Number(filterStatus.value))
  }
  
  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(item => 
      item.orderNo.toLowerCase().includes(query) || 
      (item.carBrand + ' ' + item.carModel).toLowerCase().includes(query)
    )
  }
  
  return list
})

const getRemainingTime = (createTimeStr) => {
  if (!createTimeStr) return ''
  const createTime = new Date(createTimeStr).getTime()
  const now = new Date().getTime()
  const diff = now - createTime
  const thirtyMinutes = 30 * 60 * 1000
  if (diff >= thirtyMinutes) return t('my_center.timeout_closed')
  const remaining = thirtyMinutes - diff
  const minutes = Math.floor(remaining / 1000 / 60)
  const seconds = Math.floor((remaining / 1000) % 60)
  return `${minutes}m ${seconds}s`
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  if (status === undefined || status === null) return '-'
  return t('dict.order_status.' + status)
}

const loadOrders = async () => {
  try {
    const res = await axios.get('/api/order/my-orders', {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      orderList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error(t('my_center.fetch_order_fail'))
  }
}

const loadFavorites = async () => {
  try {
    const res = await axios.get('/api/favorite/my-list', {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      favoriteList.value = res.data.data
    }
  } catch (error) {
    // ElMessage.error('获取收藏失败')
  }
}

const removeFavorite = async (carId) => {
  ElMessageBox.confirm(t('my_center.cancel_fav_confirm'), t('common.tips'), { 
    confirmButtonText: t('common.confirm'), 
    cancelButtonText: t('common.cancel') 
  })
    .then(async () => {
      try {
        await axios.post(`/api/favorite/toggle/${carId}`, {}, { headers: { token: getToken() } })
        ElMessage.success(t('my_center.cancel_fav_success'))
        loadFavorites() // 刷新列表
      } catch (e) {}
    })
}

watch(activeTab, (val) => {
  if (val === 'orders') loadOrders()
  else if (val === 'favorites') loadFavorites()
})

const openPayDialog = (orderNo) => {
  currentOrderNo.value = orderNo
  payDialogVisible.value = true
}

const confirmPay = async () => {
  paying.value = true
  try {
    const res = await axios.post(`/api/order/pay?orderNo=${currentOrderNo.value}`, {}, {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      ElMessage.success(t('my_center.pay_success'))
      payDialogVisible.value = false
      loadOrders()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error(t('my_center.pay_fail'))
  } finally {
    paying.value = false
  }
}

const handleCancel = (orderId) => {
  ElMessageBox.confirm(t('my_center.cancel_order_confirm'), t('common.tips'), { 
    confirmButtonText: t('common.confirm'), 
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await axios.post(`/api/order/cancel/${orderId}`, {}, { headers: { token: getToken() } })
        if (res.data.code === 200) {
          ElMessage.success(t('my_center.order_cancelled'))
          loadOrders()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error(t('common.fail'))
      }
    })
}

const openComplaint = (orderNo) => {
  complaintTargetId.value = orderNo
  complaintVisible.value = true
}

const toDetail = (id) => {
  router.push(`/car/${id}`)
}

onMounted(() => {
  loadOrders()
  timer = setInterval(() => {
    if (activeTab.value === 'orders') {
      // 触发响应式更新以刷新倒计时
      orderList.value = [...orderList.value]
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
/* 容器 */
.admin-panel-container {
  max-width: 1440px; /* 宽屏适配 */
  margin: 0 auto;
  padding: 24px 40px;
  background-color: #f7f9fc; /* 浅灰背景 */
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 页头 */
.panel-header {
  margin-bottom: 24px;
}
.panel-title {
  font-size: 24px;
  font-weight: 700;
  color: #111;
  margin: 0 0 8px 0;
}
.panel-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 核心卡片 */
.panel-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  border: 1px solid #e0e0e0;
  padding: 0;
  overflow: hidden;
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

/* 工具栏 */
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
  background: #fff;
}

/* 左侧 Tabs */
.tabs-section {
  display: flex;
  gap: 32px;
}
.custom-tab {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  position: relative;
  padding-bottom: 16px;
  margin-bottom: -17px; /* 覆盖 border-bottom */
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.custom-tab:hover { color: #0052cc; } /* 品牌蓝 */
.custom-tab.active {
  color: #0052cc;
  border-bottom-color: #0052cc;
  font-weight: 600;
}

/* 右侧操作区 */
.actions-section {
  display: flex;
  gap: 12px;
  align-items: center;
}
.search-group { width: 240px; }
.filter-group { width: 120px; }

/* 紧凑型输入框 */
.compact-input :deep(.el-input__wrapper),
.compact-select :deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px #ddd inset;
  border-radius: 4px;
  height: 32px;
  font-size: 13px;
}
.compact-input :deep(.el-input__wrapper.is-focus),
.compact-select :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px #0052cc inset !important; /* 品牌蓝 */
}

/* 内容区域 */
.content-container {
  padding: 0;
  flex: 1;
}

/* 表格区域 */
.compact-table {
  --el-table-header-bg-color: #f9fafb;
  --el-table-header-text-color: #555;
  --el-table-row-hover-bg-color: #f0f7ff;
}
.compact-table :deep(th) {
  font-weight: 600;
  font-size: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}
.compact-table :deep(td) {
  padding: 16px 0; /* 增加内边距，更透气 */
  border-bottom: 1px solid #eee;
}

/* 车辆信息 */
.info-box-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.thumb-box {
  width: 100px;
  height: 75px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #eee;
  background: #f5f5f5;
  flex-shrink: 0;
}
.thumb-img { width: 100%; height: 100%; object-fit: cover; }

.text-info { display: flex; flex-direction: column; gap: 6px; }
.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #111;
  line-height: 1.4;
}
.info-sub {
  font-size: 12px;
  color: #999;
}

/* 价格 */
.price-text {
  font-size: 15px;
  font-weight: 700;
  color: #F56C6C; /* 红色 */
}

/* 倒计时 */
.countdown { color: #E6A23C; font-weight: 600; font-family: monospace; }
.text-gray { color: #999; }

/* 操作按钮 */
.ops-group {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 收藏列表网格 */
.favorites-container {
  padding: 24px;
}
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4列 */
  gap: 24px;
}
.fav-card-wrapper {
  position: relative;
}
.remove-fav-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s;
}
.remove-fav-btn:hover {
  background: #FF385C;
  transform: scale(1.1);
}

/* 弹窗微调 */
.admin-dialog { border-radius: 8px; }
.pay-content { text-align: center; padding: 20px 0; }
.pay-tip { font-size: 16px; margin-bottom: 20px; font-weight: 600; }
.qr-box img { border-radius: 12px; border: 1px solid #ebebeb; padding: 10px; }
.order-info { color: #717171; font-size: 12px; margin-top: 16px; font-family: monospace; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 10px; }
</style>