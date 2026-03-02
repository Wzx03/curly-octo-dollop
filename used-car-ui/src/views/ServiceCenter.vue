<template>
  <div class="admin-panel-container" :class="{ 'full-screen-mode': activeTab === 'message' }">
    <!-- 1. 顶部页头 (已移除) -->

    <!-- 2. 核心卡片区域 -->
    <div class="panel-card" :class="{ 'im-mode-card': activeTab === 'message' }">

      <!-- 顶部工具栏：Tab -->
      <div class="toolbar-wrapper">
        <div class="tabs-section">
          <div
            class="custom-tab"
            :class="{ active: activeTab === 'message' }"
            @click="activeTab = 'message'"
          >
            {{ $t('message.notification') }}
          </div>

          <div
            class="custom-tab"
            :class="{ active: activeTab === 'complaint' }"
            @click="activeTab = 'complaint'"
          >
            {{ $t('my_center.complaint') }}
          </div>

          <div
            v-if="isAdmin"
            class="custom-tab"
            :class="{ active: activeTab === 'admin-complaint' }"
            @click="activeTab = 'admin-complaint'"
          >
            {{ $t('message.admin_complaint') }}
          </div>

          <div
            class="custom-tab"
            :class="{ active: activeTab === 'help' }"
            @click="activeTab = 'help'"
          >
            {{ $t('message.help_center') }}
          </div>
        </div>
      </div>

      <!-- 3. 内容区域 -->
      <div class="content-container" :class="{ 'flex-1': activeTab === 'message' }">

        <!-- Tab 1: 消息中心 -->
        <div v-if="activeTab === 'message'" class="tab-content full-height im-wrapper">
          <!-- 悬浮返回按钮 (已移除，因为 Tab 栏已恢复) -->
          
          <MessageCenter :embedded="true" />
        </div>

        <!-- Tab 2: 我的投诉 -->
        <div v-if="activeTab === 'complaint'" class="tab-content scrollable">
          <div class="content-header">
            <h2 class="section-title">{{ $t('message.my_complaints') }}</h2>
            <div class="header-actions">
              <el-button
                icon="Refresh"
                circle
                @click="loadComplaints"
                class="refresh-btn"
              ></el-button>
            </div>
          </div>

          <el-table
            :data="complaintList"
            style="width: 100%"
            class="compact-table"
            header-row-class-name="table-header"
          >
            <el-table-column type="expand" width="60">
              <template #default="props">
                <div class="expand-content">
                  <div class="detail-section">
                    <h4 class="detail-title">{{ $t('message.complaint_content') }}</h4>
                    <p class="detail-text">{{ props.row.content }}</p>
                  </div>

                  <div v-if="props.row.reply" class="detail-section">
                    <h4 class="detail-title">{{ $t('message.handle_result') }}</h4>
                    <p class="reply-text">{{ props.row.reply }}</p>
                  </div>

                  <div v-else class="detail-section">
                    <h4 class="detail-title">{{ $t('message.handle_result') }}</h4>
                    <p class="no-data-text">{{ $t('common.no_data') }}</p>
                  </div>

                  <div v-if="props.row.images" class="detail-section">
                    <h4 class="detail-title">{{ $t('message.evidence_images') }}</h4>
                    <div class="image-gallery">
                      <el-image
                        v-for="(img, i) in props.row.images.split(',')"
                        :key="i"
                        :src="img"
                        class="evidence-img"
                        :preview-src-list="props.row.images.split(',')"
                        fit="cover"
                      />
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="id" label="ID" width="80" />

            <el-table-column :label="$t('my_center.order_time')" min-width="180">
              <template #default="scope">
                <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
              </template>
            </el-table-column>

            <el-table-column :label="$t('my_cars.brand_model')" min-width="160">
              <template #default="scope">
                <div class="target-info">
                  <el-tag
                    :type="getTargetType(scope.row.targetType)"
                    effect="light"
                    size="small"
                  >
                    {{ getTargetName(scope.row.targetType) }}
                  </el-tag>
                  <span class="target-id">#{{ scope.row.targetId }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column :label="$t('my_center.status')" width="120">
              <template #default="scope">
                <el-tag
                  :type="getStatusType(scope.row.status)"
                  effect="light"
                  round
                >
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column :label="$t('common.operation')" width="120" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 0"
                  type="primary"
                  size="small"
                  link
                  @click="viewComplaintDetail(scope.row)"
                >
                  {{ $t('common.view_detail') }}
                </el-button>
                <span v-else class="completed-text">{{ $t('common.status_done') }}</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="empty-state" v-if="complaintList.length === 0">
            <el-empty :description="$t('message.no_complaints')" />
          </div>
        </div>

        <!-- Tab 3: 投诉管理 (管理员) -->
        <div v-if="activeTab === 'admin-complaint'" class="tab-content scrollable">
          <div class="content-header">
            <h2 class="section-title">{{ $t('message.complaint_console') }}</h2>
            <div class="header-actions">
              <el-radio-group
                v-model="adminFilterStatus"
                size="small"
                @change="loadAdminComplaints"
              >
                <el-radio-button :label="null">{{ $t('common.all') }}</el-radio-button>
                <el-radio-button :label="0">{{ $t('common.status_pending') }}</el-radio-button>
                <el-radio-button :label="2">{{ $t('common.status_done') }}</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <el-table
            :data="adminComplaintList"
            class="compact-table"
            v-loading="adminLoading"
            header-row-class-name="table-header"
          >
            <el-table-column prop="id" label="ID" width="80" />

            <el-table-column :label="$t('my_cars.brand_model')" width="140">
              <template #default="{row}">
                <el-tag type="info" effect="light" size="small">
                  {{ row.category }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column
              :label="$t('message.complaint_content')"
              show-overflow-tooltip
              min-width="200"
            >
              <template #default="{row}">
                <span class="content-preview">{{ row.content }}</span>
              </template>
            </el-table-column>

            <el-table-column :label="$t('message.evidence')" width="100">
              <template #default="{row}">
                <el-button
                  v-if="row.images"
                  type="primary"
                  size="small"
                  link
                  @click="previewImages(row.images)"
                >
                  {{ $t('common.view') }}
                </el-button>
                <span v-else class="no-data-text">{{ $t('common.no_data') }}</span>
              </template>
            </el-table-column>

            <el-table-column :label="$t('my_center.order_time')" width="180">
              <template #default="{row}">
                <span class="time-text">{{ formatDate(row.createTime) }}</span>
              </template>
            </el-table-column>

            <el-table-column :label="$t('my_center.status')" width="120">
              <template #default="{row}">
                <el-tag
                  :type="getStatusType(row.status)"
                  effect="light"
                  round
                >
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column :label="$t('common.operation')" width="140" fixed="right">
              <template #default="{row}">
                <el-button
                  v-if="row.status === 0"
                  type="primary"
                  size="small"
                  @click="openAdminHandle(row)"
                >
                  {{ $t('message.handle_complaint') }}
                </el-button>
                <span v-else class="completed-text">{{ $t('common.status_done') }}</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :total="adminTotal"
              :page-size="10"
              @current-change="handleAdminPageChange"
            />
          </div>

          <div class="empty-state" v-if="adminComplaintList.length === 0 && !adminLoading">
            <el-empty :description="$t('message.no_complaints')" />
          </div>
        </div>

        <!-- Tab 4: 帮助中心 -->
        <div v-if="activeTab === 'help'" class="tab-content scrollable">
          <div class="help-center">
            <div class="help-header">
              <h2 class="section-title">{{ $t('message.need_help') }}</h2>
              <p class="help-description">{{ $t('message.help_desc') }}</p>
            </div>

            <div class="faq-container">
              <el-collapse accordion class="faq-collapse">
                <el-collapse-item
                  v-for="(faq, index) in faqList"
                  :key="index"
                  :title="faq.question"
                  :name="index"
                >
                  <div class="faq-answer">{{ faq.answer }}</div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 管理员处理投诉弹窗 -->
    <el-dialog
      v-model="adminDialogVisible"
      :title="$t('message.handle_complaint')"
      width="500px"
      class="admin-dialog"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <el-icon class="dialog-icon"><EditPen /></el-icon>
          <span class="dialog-title">{{ $t('message.handle_complaint') }}</span>
        </div>
      </template>

      <el-form :model="adminHandleForm" label-position="top" class="handle-form">
        <el-form-item :label="$t('message.complaint_content')">
          <div class="complaint-preview">
            {{ adminCurrentRow?.content }}
          </div>
        </el-form-item>

        <el-form-item :label="$t('message.handle_result')">
          <el-input
            v-model="adminHandleForm.reply"
            type="textarea"
            :rows="4"
            :placeholder="$t('message.reply_placeholder')"
            class="reply-input"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="adminDialogVisible = false">
            {{ $t('common.cancel') }}
          </el-button>
          <el-button
            type="primary"
            @click="submitAdminHandle"
            :loading="submitting"
          >
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import axios from 'axios'
import { Refresh, ChatDotRound, Warning, Management, QuestionFilled, EditPen, Back } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MessageCenter from './MessageCenter.vue'
import { getToken, getActiveAccount } from '../utils/auth'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const { t } = useI18n()
const route = useRoute()
const activeTab = ref('message')
const complaintList = ref([])

// FAQ 数据
const faqList = ref([
  {
    question: '如何发布车辆？',
    answer: '在"车辆管理"页面点击"发布新车"，填写车辆基本信息、上传车辆图片、设置价格等详细信息后提交审核。'
  },
  {
    question: '交易流程是怎样的？',
    answer: '买家下单 → 支付定金 → 线下看车 → 确认收货 → 支付尾款 → 交易完成。整个过程平台提供安全保障。'
  },
  {
    question: '如何申请退款？',
    answer: '在订单详情页点击"申请退款"，填写退款理由并上传相关证据，等待卖家处理。如协商不成可申请平台介入。'
  },
  {
    question: '车辆信息有误怎么办？',
    answer: '可以联系卖家沟通修改，或在订单页面发起投诉，提供相关证据，平台客服会协助处理。'
  }
])

// 初始化逻辑
onMounted(() => {
  if (route.query.targetId) {
    activeTab.value = 'message'
  }
})

const isAdmin = computed(() => {
  const account = getActiveAccount()
  return account && account.role === 0
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const getTargetName = (type) => {
  const map = { 1: t('my_center.tab_orders'), 2: t('nav.cars'), 3: t('nav.profile') }
  return map[type] || t('my_center.status_unknown')
}

const getTargetType = (type) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: t('common.status_pending'), 1: t('common.status_processing'), 2: t('common.status_done') }
  return map[status] || t('my_center.status_unknown')
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success' }
  return map[status] || 'info'
}

const loadComplaints = async () => {
  try {
    const res = await axios.get('/api/complaint/my-list', {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      complaintList.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(t('common.load_fail'))
  }
}

// ==================== 管理员逻辑 ====================
const adminComplaintList = ref([])
const adminLoading = ref(false)
const adminTotal = ref(0)
const adminPage = ref(1)
const adminFilterStatus = ref(null)
const adminDialogVisible = ref(false)
const adminCurrentRow = ref(null)
const adminHandleForm = ref({ id: null, reply: '' })
const submitting = ref(false)

const loadAdminComplaints = async () => {
  if (!isAdmin.value) return
  adminLoading.value = true
  try {
    const res = await axios.get('/api/admin/complaint/list', {
      params: { page: adminPage.value, size: 10, status: adminFilterStatus.value },
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      adminComplaintList.value = res.data.data.records
      adminTotal.value = res.data.data.total
    }
  } catch (e) {
    ElMessage.error(t('common.load_fail'))
  } finally {
    adminLoading.value = false
  }
}

const handleAdminPageChange = (val) => {
  adminPage.value = val
  loadAdminComplaints()
}

const openAdminHandle = (row) => {
  adminCurrentRow.value = row
  adminHandleForm.value = { id: row.id, reply: '' }
  adminDialogVisible.value = true
}

const submitAdminHandle = async () => {
  if (!adminHandleForm.value.reply?.trim()) {
    return ElMessage.warning(t('message.reply_required'))
  }

  submitting.value = true
  try {
    const res = await axios.post('/api/admin/complaint/handle', adminHandleForm.value, {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      ElMessage.success(t('common.success'))
      adminDialogVisible.value = false
      loadAdminComplaints()
    } else {
      ElMessage.error(res.data.message || t('common.fail'))
    }
  } catch (e) {
    ElMessage.error(t('common.network_error'))
  } finally {
    submitting.value = false
  }
}

const previewImages = (images) => {
  const imageList = images.split(',')
  ElMessageBox({
    title: t('message.evidence_images'),
    message: `
      <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px;">
        ${imageList.map(img => `<img src="${img}" style="width: 100%; border-radius: 4px;" />`).join('')}
      </div>
    `,
    dangerouslyUseHTMLString: true,
    showCancelButton: false,
    confirmButtonText: t('common.confirm')
  })
}

const viewComplaintDetail = (row) => {
  ElMessageBox({
    title: t('message.complaint_detail'),
    message: `
      <div style="line-height: 1.6;">
        <p><strong>${t('message.complaint_content')}:</strong></p>
        <p>${row.content}</p>
        ${row.reply ? `
          <p style="margin-top: 16px;"><strong>${t('message.handle_result')}:</strong></p>
          <p style="color: #16a34a;">${row.reply}</p>
        ` : ''}
        ${row.images ? `
          <p style="margin-top: 16px;"><strong>${t('message.evidence_images')}:</strong></p>
          <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 8px;">
            ${row.images.split(',').map(img => `<img src="${img}" style="width: 100%; border-radius: 4px; border: 1px solid #eee;" />`).join('')}
          </div>
        ` : ''}
      </div>
    `,
    dangerouslyUseHTMLString: true,
    showCancelButton: false,
    confirmButtonText: t('common.confirm')
  })
}

watch(activeTab, (val) => {
  if (val === 'complaint') {
    loadComplaints()
  } else if (val === 'admin-complaint') {
    loadAdminComplaints()
  }
})
</script>

<style scoped>
/* 容器 */
.admin-panel-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px 40px;
  background-color: #f7f9fc;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  display: flex;
  flex-direction: column;
}

/* 全屏模式：移除边距，占满屏幕 */
.admin-panel-container.full-screen-mode {
  padding: 0;
  margin: 0;
  max-width: none;
  height: calc(100vh - 80px); /* 减去顶部导航栏高度，视具体布局调整 */
  overflow: hidden;
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

/* IM 模式卡片：去边框，全高 */
.panel-card.im-mode-card {
  border: none;
  box-shadow: none;
  background: transparent;
  height: 100%;
  border-radius: 0;
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

/* Tabs */
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
  margin-bottom: -17px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.tab-icon {
  font-size: 16px;
}
.custom-tab:hover { color: #0052cc; }
.custom-tab.active {
  color: #0052cc;
  border-bottom-color: #0052cc;
  font-weight: 600;
}

/* 内容区域 */
.content-container {
  padding: 0;
  flex: 1;
  overflow-y: auto;
}

.content-container.flex-1 {
  display: flex;
  flex-direction: column;
}

.tab-content {
  height: 100%;
  padding: 24px;
}
.tab-content.full-height {
  padding: 0;
  height: calc(100vh - 200px);
}
.tab-content.scrollable {
  overflow-y: auto;
}

/* IM 容器 wrapper */
.im-wrapper {
  position: relative;
  height: 100% !important;
}

/* 悬浮返回按钮 */
.floating-back-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #222;
  margin: 0;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 表格样式 */
.compact-table {
  --el-table-header-bg-color: #f9fafb;
  --el-table-header-text-color: #555;
  --el-table-row-hover-bg-color: #f0f7ff;
}
.compact-table :deep(th) {
  font-weight: 600;
  font-size: 14px; /* 增大表头字号 */
  padding: 16px 0; /* 增加表头上下间距 */
}
.compact-table :deep(td) {
  font-size: 14px; /* 增大内容字号 */
  padding: 20px 0; /* 大幅增加表格行的上下间距，让行变高 */
}
.compact-table :deep(.el-table__expanded-cell) {
  padding: 20px 48px;
  background: #f9fafb;
}

/* 展开内容 */
.expand-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.detail-section {
  margin-bottom: 16px;
}
.detail-section:last-child {
  margin-bottom: 0;
}
.detail-title {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  margin: 0 0 8px 0;
}
.detail-text {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin: 0;
}
.reply-text {
  color: #16a34a;
  font-weight: 500;
  margin: 0;
}
.no-data-text {
  color: #999;
  font-style: italic;
  margin: 0;
}
.image-gallery {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.evidence-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: all 0.2s;
}
.evidence-img:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 目标信息 */
.target-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.target-id {
  font-size: 12px;
  color: #717171;
}

/* 时间文本 */
.time-text {
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  color: #666;
}

/* 内容预览 */
.content-preview {
  color: #555;
}

/* 完成状态文本 */
.completed-text {
  color: #999;
  font-size: 12px;
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
.status-pill.success { background: #e6f4ea; color: #137333; } /* 绿 */

/* 空状态 */
.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 帮助中心 */
.help-center {
  max-width: 800px;
  margin: 0 auto;
}
.help-header {
  text-align: center;
  margin-bottom: 32px;
}
.help-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 12px 0 0 0;
}
.faq-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  border: 1px solid #ebebeb;
}
.faq-collapse :deep(.el-collapse-item__header) {
  font-size: 15px;
  font-weight: 500;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
}
.faq-collapse :deep(.el-collapse-item__wrap) {
  border-bottom: 1px solid #eee;
}
.faq-collapse :deep(.el-collapse-item__content) {
  padding: 20px 24px;
}
.faq-answer {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

/* 分页 */
.pagination-wrapper {
  padding: 20px 0;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #eee;
  margin-top: auto;
}

/* 弹窗样式 */
.admin-dialog {
  border-radius: 8px;
}
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
}
.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}
.handle-form {
  padding: 20px 24px;
}
.complaint-preview {
  background: #f7f7f7;
  padding: 16px;
  border-radius: 4px;
  color: #333;
  line-height: 1.6;
  font-size: 14px;
  border-left: 3px solid #0052cc;
}
.reply-input :deep(.el-textarea__inner) {
  font-size: 14px;
}
.dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>