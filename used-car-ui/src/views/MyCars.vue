<template>
  <div class="admin-panel-container">
    <!-- 2. 核心卡片区域 -->
    <div class="panel-card">
      
      <!-- 顶部工具栏：Tab + 筛选 + 操作 -->
      <div class="toolbar-wrapper">
        <!-- 左侧 Tab -->
        <div class="tabs-section">
          <div 
            v-for="tab in tabs" 
            :key="tab.name"
            class="custom-tab"
            :class="{ active: currentTab === tab.name }"
            @click="handleTabSwitch(tab.name)"
          >
            {{ tab.label }}
            <span v-if="tab.name === 'audit' && auditCount > 0" class="badge">{{ auditCount }}</span>
          </div>
        </div>

        <!-- 右侧操作区 -->
        <div class="actions-section">
          <div class="search-group">
            <el-input 
              v-model="searchQuery" 
              placeholder="搜索车辆名称..." 
              class="compact-input"
              clearable
              @clear="loadData"
              @keyup.enter="loadData"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          
          <div class="filter-group">
            <el-select v-model="filterStatus" placeholder="状态" class="compact-select" @change="loadData" clearable>
              <el-option label="全部" value="" />
              <el-option label="已上架" :value="1" />
              <!-- 👇 区分文案：管理员看“待审核”，用户看“审核中” -->
              <el-option :label="isAdmin ? '待审核' : '审核中'" :value="0" />
              <el-option label="已售出" :value="3" />
              <el-option label="已下架" :value="4" />
              <el-option label="已驳回" :value="-1" />
            </el-select>
          </div>

          <el-button type="primary" class="action-btn primary" icon="Plus" @click="openPublishDialog">
            发布新车
          </el-button>
          
          <!-- 批量操作 (仅管理员可见) -->
          <el-dropdown v-if="isAdmin && selectedIds.length > 0" trigger="click" @command="handleBatchCommand">
            <el-button class="action-btn outline">
              批量操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="approve" v-if="currentTab === 'audit'">批量通过</el-dropdown-item>
                <el-dropdown-item command="reject" v-if="currentTab === 'audit'">批量驳回</el-dropdown-item>
                <el-dropdown-item command="off_shelf">批量下架</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 3. 数据表格区域 -->
      <div class="table-container">
        <el-table 
          :data="displayList" 
          style="width: 100%" 
          v-loading="loading"
          @selection-change="handleSelectionChange"
          class="compact-table"
          header-row-class-name="table-header"
        >
          <el-table-column type="selection" width="40" />
          
          <!-- 第1列：缩略图 -->
          <el-table-column label="缩略图" width="100">
            <template #default="{row}">
              <div class="thumb-box">
                <img :src="row.image || 'https://dummyimage.com/160x120/f0f0f0/999'" class="thumb-img" />
              </div>
            </template>
          </el-table-column>

          <!-- 第2列：车辆信息 -->
          <el-table-column label="车辆信息" min-width="280">
            <template #default="{row}">
              <div class="info-box">
                <div class="info-title">{{ row.brand }} {{ row.model }}</div>
                <div class="info-sub">{{ row.buyYear }}年 | {{ row.mileage }}万公里 | {{ row.city || '未知' }}</div>
                
                <!-- 👇 驳回原因提示 -->
                <el-alert
                  v-if="row.status === -1 && row.description && row.description.includes('【驳回原因】')"
                  :title="row.description.split('\n')[0]"
                  type="error"
                  :closable="false"
                  show-icon
                  style="margin-top: 8px; padding: 4px 8px;"
                />
              </div>
            </template>
          </el-table-column>

          <!-- 第3列：价格 -->
          <el-table-column label="价格" width="120">
            <template #default="{row}">
              <span class="price-text">{{ formatPrice(row.price) }}</span>
            </template>
          </el-table-column>

          <!-- 第4列：状态 -->
          <el-table-column label="状态" width="100">
            <template #default="{row}">
              <span class="status-pill" :class="getStatusClass(row.status)">
                {{ getStatusText(row.status) }}
              </span>
            </template>
          </el-table-column>

          <!-- 第5列：操作 -->
          <el-table-column label="操作" width="180" fixed="right" align="right">
            <template #default="{row}">
              <div class="ops-group">
                <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
                
                <el-button 
                  v-if="row.status === 1" 
                  link type="warning" size="small" 
                  @click="handleOffShelf(row)"
                >下架</el-button>
                
                <!-- 👇 仅管理员可见上架按钮 (普通用户通过编辑重新提交审核) -->
                <el-button 
                  v-if="isAdmin && (row.status === 4 || row.status === 3 || row.status === -1)" 
                  link type="success" size="small" 
                  @click="handleRelist(row)"
                >上架</el-button>

                <el-button 
                  v-if="isAdmin && row.status === 0" 
                  link type="success" size="small" 
                  @click="handleApprove(row)"
                >通过</el-button>

                <el-button 
                  v-if="isAdmin && row.status === 0" 
                  link type="danger" size="small" 
                  @click="handleReject(row)"
                >驳回</el-button>

                <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 4. 底部 -->
      <div class="pagination-bar">
        <el-pagination 
          background 
          layout="total, prev, pager, next" 
          :total="total" 
          :page-size="pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 弹窗组件 -->
    <el-dialog 
      v-model="dialogVisible" 
      :show-close="false"
      width="900px" 
      destroy-on-close 
      class="admin-dialog" 
      align-center
      top="5vh"
    >
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">{{ form.id ? '编辑车辆信息' : '发布新车' }}</span>
          <div class="close-btn" @click="dialogVisible = false"><el-icon><Close /></el-icon></div>
        </div>
      </template>
      
      <div class="dialog-body">
         <div class="form-scroll-area">
            <el-form :model="form" label-position="top" class="compact-form">
              
              <!-- Section 1: 基本信息 -->
              <div class="form-section-title">基本信息</div>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="品牌"><el-input v-model="form.brand" placeholder="如：宝马"/></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="型号"><el-input v-model="form.model" placeholder="如：3系 325Li"/></el-form-item></el-col>
                <el-col :span="8">
                  <el-form-item label="厂商属性">
                    <el-select v-model="form.manufacturerType" placeholder="请选择" style="width:100%">
                      <el-option label="国产" value="国产"/>
                      <el-option label="合资" value="合资"/>
                      <el-option label="进口" value="进口"/>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8"><el-form-item label="车身颜色"><el-input v-model="form.color" placeholder="如：白色"/></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="所在城市"><el-input v-model="form.city" placeholder="如：北京"/></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="过户次数"><el-input-number v-model="form.transferCount" :min="0" style="width:100%"/></el-form-item></el-col>
              </el-row>

              <!-- Section 2: 规格参数 -->
              <div class="form-section-title">规格参数</div>
              <el-row :gutter="20">
                <el-col :span="6"><el-form-item label="上牌年份"><el-date-picker v-model="yearStr" type="year" value-format="YYYY" @change="handleYearChange" style="width:100%" placeholder="选择年份"/></el-form-item></el-col>
                <el-col :span="6"><el-form-item label="表显里程(万公里)"><el-input-number v-model="form.mileage" :step="0.1" :min="0" style="width:100%"/></el-form-item></el-col>
                <el-col :span="6"><el-form-item label="排量"><el-input v-model="form.displacement" placeholder="如：2.0T"/></el-form-item></el-col>
                <el-col :span="6">
                  <el-form-item label="变速箱">
                    <el-select v-model="form.gearbox" placeholder="请选择" style="width:100%">
                      <el-option label="自动" value="自动"/>
                      <el-option label="手动" value="手动"/>
                      <el-option label="手自一体" value="手自一体"/>
                      <el-option label="双离合" value="双离合"/>
                      <el-option label="CVT" value="CVT"/>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="能源类型">
                    <el-select v-model="form.energyType" placeholder="请选择" style="width:100%">
                      <el-option label="汽油" value="汽油"/>
                      <el-option label="柴油" value="柴油"/>
                      <el-option label="纯电动" value="纯电动"/>
                      <el-option label="插电混动" value="插电混动"/>
                      <el-option label="油电混动" value="油电混动"/>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="排放标准">
                    <el-select v-model="form.emissionStandard" placeholder="请选择" style="width:100%">
                      <el-option label="国VI" value="国VI"/>
                      <el-option label="国V" value="国V"/>
                      <el-option label="国IV" value="国IV"/>
                      <el-option label="国III" value="国III"/>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="驱动方式">
                    <el-select v-model="form.driveMode" placeholder="请选择" style="width:100%">
                      <el-option label="前驱" value="前驱"/>
                      <el-option label="后驱" value="后驱"/>
                      <el-option label="四驱" value="四驱"/>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="保养情况">
                    <el-select v-model="form.maintenanceType" placeholder="请选择" style="width:100%">
                      <el-option label="4S店保养" value="4S店保养"/>
                      <el-option label="按时保养" value="按时保养"/>
                      <el-option label="无保养记录" value="无保养记录"/>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- Section 3: 价格与描述 -->
              <div class="form-section-title">价格与描述</div>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="期望售价(万)"><el-input-number v-model="form.price" :step="0.1" :min="0" style="width:100%"/></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="新车指导价(万)"><el-input-number v-model="form.originalPrice" :step="0.1" :min="0" style="width:100%"/></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="车辆描述"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="介绍一下车况、亮点等..."/></el-form-item></el-col>
                <el-col :span="24">
                   <el-form-item label="车辆封面图">
                     <el-upload action="#" :show-file-list="false" :http-request="uploadImage" class="avatar-uploader">
                        <img v-if="form.image" :src="form.image" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                     </el-upload>
                     <div class="upload-tip">建议上传 4:3 比例的高清图片</div>
                   </el-form-item>
                </el-col>
              </el-row>
            </el-form>
         </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePublish" :loading="submitting">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowDown, Close } from '@element-plus/icons-vue'
import { getToken, getActiveAccount } from '../utils/auth'
import { useCurrency } from '../hooks/useCurrency'

const route = useRoute()
const { formatPrice } = useCurrency()

// 状态
const isAdmin = computed(() => {
  const account = getActiveAccount()
  // 严格判断：只有 role 为 0 或 '0' 才是管理员
  return account && String(account.role) === '0'
})

const currentTab = ref('my_published')
const tabs = computed(() => {
  if (isAdmin.value) {
    return [
      { name: 'my_published', label: '我发布的' },
      { name: 'audit', label: '审核队列' },
      { name: 'all', label: '全局车辆库' }
    ]
  } else {
    // 普通用户只需要一个“我发布的”
    return [
      { name: 'my_published', label: '我发布的' }
    ]
  }
})

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const auditCount = ref(0)
const searchQuery = ref('')
const filterStatus = ref('') // 状态筛选
const selectedIds = ref([])

// 模拟数据 (当后端无数据时展示效果)
const mockData = [
  { id: 101, brand: 'Mercedes-Benz', model: 'C260L 运动版', buyYear: 2022, mileage: 1.5, city: '上海', price: 28.5, status: 1, image: 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=400&q=80' },
  { id: 102, brand: 'BMW', model: '325Li M运动套装', buyYear: 2021, mileage: 3.2, city: '北京', price: 26.5, status: 1, image: 'https://images.unsplash.com/photo-1555215695-3004980adade?w=400&q=80' },
  { id: 103, brand: 'Tesla', model: 'Model 3 Performance', buyYear: 2023, mileage: 0.8, city: '杭州', price: 22.9, status: 0, image: 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=400&q=80' },
  { id: 104, brand: 'Audi', model: 'A4L 45TFSI quattro', buyYear: 2020, mileage: 4.5, city: '广州', price: 21.8, status: 4, image: 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=400&q=80' },
  { id: 105, brand: 'Porsche', model: 'Macan 2.0T', buyYear: 2019, mileage: 5.1, city: '深圳', price: 45.0, status: 3, image: 'https://images.unsplash.com/photo-1503376763036-066120622c74?w=400&q=80' },
]

// 核心逻辑
const loadData = async () => {
  loading.value = true
  try {
    let url = ''
    let params = { page: page.value, size: pageSize.value }

    // 构造参数
    if (isAdmin.value) {
      if (currentTab.value === 'audit') url = '/api/admin/car/audit/list'
      else if (currentTab.value === 'my_published') url = '/api/car/my-cars'
      else {
        url = '/api/admin/car/list'
        if (searchQuery.value) params.keyword = searchQuery.value
      }
    } else {
      // 普通用户只有 my_published，查询所有自己的车
      url = '/api/car/my-cars'
    }

    // 状态筛选
    if (filterStatus.value !== '') params.status = filterStatus.value

    const res = await axios.get(url, { params, headers: { token: getToken() } })
    
    if (res.data.code === 200) {
      const records = res.data.data.records || res.data.data
      list.value = records.length > 0 ? records : [] 
      total.value = res.data.data.total || list.value.length
      
      if (isAdmin.value && currentTab.value === 'audit') auditCount.value = res.data.data.total
    }
  } catch (e) {
    // 仅用于演示布局，如果失败加载模拟数据
    // list.value = mockData
  } finally {
    loading.value = false
  }
}

// 计算属性：用于展示的数据 (如果后端没数据，展示模拟数据以供预览效果)
const displayList = computed(() => {
  return list.value.length > 0 ? list.value : [] 
})

const handleTabSwitch = (name) => {
  currentTab.value = name
  page.value = 1
  filterStatus.value = ''
  loadData()
}

const handlePageChange = (val) => {
  page.value = val
  loadData()
}

const handleSelectionChange = (val) => selectedIds.value = val.map(item => item.id)

// 辅助函数
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已上架', 2: '交易中', 3: '已售出', 4: '已下架', '-1': '已驳回' }
  return map[status] || '未知'
}
const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'active', 2: 'warning', 3: 'sold', 4: 'inactive', '-1': 'inactive' }
  return map[status] || ''
}

// 操作逻辑 (简化版)
const dialogVisible = ref(false)
const submitting = ref(false)
const yearStr = ref('')
const form = ref({})

const openPublishDialog = () => {
  form.value = { 
    brand: '', 
    model: '',
    price: undefined,
    originalPrice: undefined,
    mileage: undefined,
    transferCount: 0,
    city: '',
    color: '',
    displacement: '',
    gearbox: '',
    energyType: '',
    emissionStandard: '',
    driveMode: '',
    manufacturerType: '',
    maintenanceType: '',
    description: '',
    image: ''
  }
  yearStr.value = ''
  dialogVisible.value = true
}
const openEdit = (row) => {
  form.value = { ...row }
  yearStr.value = row.buyYear ? row.buyYear + '' : ''
  dialogVisible.value = true
}
const handlePublish = async () => {
  // ... 复用之前的发布逻辑 ...
  submitting.value = true
  try {
    const url = form.value.id ? '/api/car/update' : '/api/car/publish'
    await axios.post(url, form.value, { headers: { token: getToken() } })
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } catch(e) { ElMessage.error('失败') }
  finally { submitting.value = false }
}
const handleOffShelf = (row) => actionApi(`/api/car/off-shelf/${row.id}`)
const handleRelist = (row) => actionApi('/api/car/update', { ...row, status: 0 })
const handleDelete = (row) => actionApi(`/api/car/delete/${row.id}`)
const handleApprove = (row) => actionApi(`/api/admin/car/approve/${row.id}`)

const handleReject = (row) => {
  ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
    confirmButtonText: '驳回',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '原因不能为空'
  }).then(async ({ value }) => {
    try {
      await axios.post(`/api/admin/car/reject/${row.id}`, { reason: value }, { headers: { token: getToken() } })
      ElMessage.warning('已驳回')
      loadData()
    } catch (e) {}
  })
}

const actionApi = async (url, data = {}) => {
  try {
    await axios.post(url, data, { headers: { token: getToken() } })
    ElMessage.success('操作成功')
    loadData()
  } catch(e) {}
}

const handleBatchCommand = (cmd) => {
  if (cmd === 'approve') actionApi('/api/admin/car/batch-approve', selectedIds.value)
  if (cmd === 'reject') actionApi('/api/admin/car/batch-reject', selectedIds.value)
  if (cmd === 'off_shelf') actionApi('/api/admin/car/batch-off-shelf', selectedIds.value)
}

const uploadImage = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  const res = await axios.post('/api/common/upload', formData, { headers: { token: getToken() } })
  if (res.data.code === 200) form.value.image = res.data.data
}
const handleYearChange = (val) => form.value.buyYear = parseInt(val)

watch(isAdmin, () => loadData())
watch(() => route.query.action, (val) => { if (val === 'publish') openPublishDialog() }, { immediate: true })
onMounted(() => loadData())
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
  padding: 0; /* 紧凑布局，无内边距 */
  overflow: hidden;
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
.badge {
  background: #ff4d4f;
  color: white;
  font-size: 10px;
  padding: 0 6px;
  border-radius: 10px;
  margin-left: 4px;
  vertical-align: text-top;
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

/* 按钮 */
.action-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}
.action-btn.primary {
  background: #0052cc; /* 品牌蓝 */
  border-color: #0052cc;
}
.action-btn.primary:hover { background: #0043a8; border-color: #0043a8; }
.action-btn.outline {
  background: #fff;
  border-color: #ddd;
  color: #333;
}
.action-btn.outline:hover { background: #f5f5f5; }

/* 表格区域 */
.table-container {
  padding: 0;
}
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
  padding: 12px 0; /* 紧凑行高 */
  border-bottom: 1px solid #eee;
}

/* 缩略图 */
.thumb-box {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #eee;
  background: #f5f5f5;
}
.thumb-img { width: 100%; height: 100%; object-fit: cover; }

/* 车辆信息 */
.info-box { display: flex; flex-direction: column; gap: 4px; }
.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #111;
  line-height: 1.4;
}
.info-sub {
  font-size: 12px;
  color: #888;
}

/* 价格 */
.price-text {
  font-size: 15px;
  font-weight: 700;
  color: #d93025; /* 红色 */
}

/* 状态胶囊 */
.status-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}
.status-pill.active { background: #e6f4ea; color: #137333; } /* 绿 */
.status-pill.pending { background: #fef7e0; color: #b06000; } /* 橙 */
.status-pill.inactive, .status-pill.sold { background: #f1f3f4; color: #5f6368; } /* 灰 */
.status-pill.warning { background: #e8f0fe; color: #1967d2; } /* 蓝 */

/* 分页 */
.pagination-bar {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #eee;
}

/* 弹窗微调 */
.admin-dialog { border-radius: 8px; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid #eee; }
.dialog-title { font-size: 16px; font-weight: 600; }
.dialog-body { padding: 0; }
.form-scroll-area { max-height: 65vh; overflow-y: auto; padding: 24px; }
.dialog-footer { padding: 16px 24px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 12px; }
.compact-form :deep(.el-form-item) { margin-bottom: 16px; }

/* 表单分节标题 */
.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 4px solid #0052cc;
  line-height: 1.2;
}
.form-section-title:not(:first-child) {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #eee;
  border-left: none;
  padding-left: 0;
}

/* 图片上传 */
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 160px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fafafa;
  transition: border-color 0.2s;
}
.avatar-uploader:hover { border-color: #0052cc; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; }
.avatar { width: 100%; height: 100%; object-fit: cover; display: block; }
.upload-tip { font-size: 12px; color: #999; margin-top: 8px; }
</style>