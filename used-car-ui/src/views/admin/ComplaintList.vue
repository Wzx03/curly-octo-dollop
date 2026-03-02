<template>
  <div class="admin-container">
    <el-card>
      <div slot="header" class="card-header">
        <span style="font-weight: bold; font-size: 18px">⚖️ 投诉管理控制台</span>
        <el-radio-group v-model="filterStatus" size="small" @change="loadData">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="0">待处理</el-radio-button>
          <el-radio-button :label="2">已完结</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="category" label="类型" width="120">
          <template #default="{row}">
            <el-tag :type="getTagType(row.category)">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
        <el-table-column label="凭证" width="120">
          <template #default="{row}">
            <el-image 
              v-if="row.images"
              :src="row.images.split(',')[0]" 
              :preview-src-list="row.images.split(',')"
              style="width: 40px; height: 40px; border-radius: 4px"
              fit="cover"
            />
            <span v-else style="color: #ccc">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.status === 0" type="danger" effect="dark">待处理</el-tag>
            <el-tag v-else type="success" effect="plain">已完结</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{row}">
            <el-button 
              v-if="row.status === 0" 
              type="primary" 
              size="small" 
              @click="openHandle(row)"
            >处理</el-button>
            <el-button v-else size="small" disabled>已处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 处理弹窗 -->
    <el-dialog v-model="dialogVisible" title="处理投诉" width="500px">
      <el-form :model="handleForm">
        <el-form-item label="投诉内容">
          <div style="background: #f5f7fa; padding: 10px; border-radius: 4px; width: 100%; color: #666; line-height: 1.5">
            {{ currentRow?.content }}
          </div>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input 
            v-model="handleForm.reply" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入处理意见（如：已核实，对卖家进行警告处理...）" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHandle">确认处理</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const filterStatus = ref(0) // 默认看待处理

const dialogVisible = ref(false)
const currentRow = ref(null)
const handleForm = ref({ id: null, reply: '' })

const formatTime = (time) => time ? time.replace('T', ' ').substring(0, 16) : ''

const getTagType = (cat) => {
  if (cat && cat.includes('欺诈')) return 'danger'
  if (cat && cat.includes('辱骂')) return 'warning'
  return ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/complaint/list', {
      params: { page: page.value, size: 10, status: filterStatus.value },
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

const openHandle = (row) => {
  currentRow.value = row
  handleForm.value = { id: row.id, reply: '' }
  dialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.value.reply) return ElMessage.warning('请输入回复内容')
  
  try {
    const res = await axios.post('/api/admin/complaint/handle', handleForm.value, {
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      ElMessage.success('处理成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.admin-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>