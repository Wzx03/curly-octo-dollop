<template>
  <div class="admin-container">
    <el-card>
      <div slot="header" class="card-header">
        <span style="font-weight: bold; font-size: 18px">🚙 车辆发布审核</span>
        
        <div class="header-actions">
          <!-- 👇 批量下架按钮 -->
          <el-button 
            type="danger" 
            plain 
            :disabled="selectedIds.length === 0"
            @click="handleBatchOffShelf"
            style="margin-right: 10px"
          >
            批量下架
          </el-button>

          <el-upload
            action=""
            :http-request="handleImport"
            :show-file-list="false"
            accept=".xlsx, .xls"
            :auto-upload="true"
            style="display: inline-block; margin-right: 10px"
          >
            <el-button type="warning" icon="Upload">批量导入</el-button>
          </el-upload>
          
          <el-button icon="Refresh" circle @click="loadData"></el-button>
        </div>
      </div>

      <el-table 
        :data="tableData" 
        border 
        stripe 
        v-loading="loading" 
        style="margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <!-- 👇 多选框 -->
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="60" />
        
        <el-table-column label="车辆图片" width="120">
          <template #default="{row}">
            <el-image 
              :src="row.image || 'https://dummyimage.com/100x100/eee/999'" 
              :preview-src-list="[row.image]"
              style="width: 80px; height: 60px; border-radius: 4px"
              fit="cover"
            />
          </template>
        </el-table-column>

        <el-table-column label="车辆信息" min-width="200">
          <template #default="{row}">
            <div style="font-weight: bold">{{ row.brand }} {{ row.model }}</div>
            <div style="font-size: 12px; color: #666">
              {{ row.buyYear }}年 | {{ row.mileage }}万公里 | {{ row.city || '未知城市' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="价格" width="150">
          <template #default="{row}">
            <div style="color: #f56c6c; font-weight: bold">报价: {{ row.price }}万</div>
            <div style="font-size: 12px; color: #999">估价: {{ row.estimatedPrice }}万</div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="发布时间" width="170">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.status === 0">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已上架</el-tag>
            <el-tag v-else-if="row.status === 4" type="info">已下架</el-tag>
            <el-tag v-else type="danger">已驳回</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" size="small" @click="handleReject(row)">驳回</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" @click="handleOffShelf(row)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right">
        <el-pagination 
          background 
          layout="prev, pager, next" 
          :total="total" 
          :page-size="10"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Upload, Refresh } from '@element-plus/icons-vue'

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const selectedIds = ref([])

const formatTime = (time) => time ? time.replace('T', ' ').substring(0, 16) : ''

const loadData = async () => {
  loading.value = true
  try {
    // 注意：这里可能需要调整后端接口以支持查询所有状态的车，或者增加筛选
    // 目前 /audit/list 只查 status=0。为了管理方便，管理员应该能看到所有车。
    // 建议修改后端 AdminCarController.auditList 支持 status 参数
    // 暂时先用现有的，假设管理员主要处理待审核的
    const res = await axios.get('/api/admin/car/audit/list', {
      params: { page: page.value, size: 10 },
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val) => {
  page.value = val
  loadData()
}

const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

const handleImport = async (options) => {
  console.log('开始导入文件:', options.file.name)
  const formData = new FormData()
  formData.append('file', options.file)
  const loadingInstance = ElLoading.service({
    lock: true, text: '正在导入中...', background: 'rgba(0, 0, 0, 0.7)',
  })
  try {
    const res = await axios.post('/api/admin/car/import', formData, {
      headers: { token: localStorage.getItem('token'), 'Content-Type': 'multipart/form-data' }
    })
    loadingInstance.close()
    if (res.data.code === 200) {
      ElMessage.success(res.data.data || '导入成功')
      loadData() 
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    loadingInstance.close()
    ElMessage.error('导入失败')
  }
}

const handleApprove = (row) => {
  ElMessageBox.confirm(`确认通过 [${row.brand}] 的发布申请吗？`, '审核通过', {
    type: 'success', confirmButtonText: '通过'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/admin/car/approve/${row.id}`, {}, {
        headers: { token: localStorage.getItem('token') }
      })
      if (res.data.code === 200) {
        ElMessage.success('已上架')
        loadData()
      }
    } catch (e) {}
  })
}

const handleReject = (row) => {
  ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
    confirmButtonText: '驳回', cancelButtonText: '取消', inputPattern: /\S/, inputErrorMessage: '原因不能为空'
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/admin/car/reject/${row.id}`, {}, {
        headers: { token: localStorage.getItem('token') }
      })
      if (res.data.code === 200) {
        ElMessage.warning('已驳回')
        loadData()
      }
    } catch (e) {}
  })
}

// 👇 单条下架
const handleOffShelf = (row) => {
  ElMessageBox.confirm(`确定要强制下架 [${row.brand}] 吗？`, '下架确认', {
    type: 'warning'
  }).then(async () => {
    // 管理员调用批量接口下架单条（复用逻辑）
    try {
      const res = await axios.post('/api/admin/car/batch-off-shelf', [row.id], {
        headers: { token: localStorage.getItem('token') }
      })
      if (res.data.code === 200) {
        ElMessage.success('下架成功')
        loadData()
      }
    } catch (e) {}
  })
}

// 👇 批量下架
const handleBatchOffShelf = () => {
  ElMessageBox.confirm(`确定要下架选中的 ${selectedIds.value.length} 辆车吗？`, '批量下架', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post('/api/admin/car/batch-off-shelf', selectedIds.value, {
        headers: { token: localStorage.getItem('token') }
      })
      if (res.data.code === 200) {
        ElMessage.success('批量下架成功')
        loadData()
      }
    } catch (e) {}
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.admin-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
</style>