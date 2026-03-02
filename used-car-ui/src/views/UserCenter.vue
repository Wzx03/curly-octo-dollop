<template>
  <div class="user-center-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>👤 {{ $t('user_center.title') }}</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" class="demo-tabs" @tab-click="handleTabClick">
        
        <!-- Tab 1: 基本资料 -->
        <el-tab-pane :label="$t('user_center.basic_info')" name="info">
          <div class="info-wrapper">
            <!-- 👇 修改：头像上传 -->
            <div class="avatar-box">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :http-request="uploadAvatar"
              >
                <div class="avatar-wrapper">
                  <el-avatar :size="100" :src="form.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  <div class="avatar-mask">
                    <el-icon><Camera /></el-icon>
                    <span>{{ $t('user_center.change_avatar') }}</span>
                  </div>
                </div>
              </el-upload>
              <div style="margin-top: 10px; color: #999; font-size: 12px">{{ $t('user_center.click_to_change') }}</div>
            </div>

            <el-form :model="form" label-width="80px" style="width: 400px">
              <el-form-item :label="$t('user_center.account')">
                <el-input v-model="form.username" disabled />
              </el-form-item>
              <el-form-item :label="$t('user_center.nickname')">
                <el-input v-model="form.nickname" />
              </el-form-item>
              <el-form-item :label="$t('user_center.phone')">
                <el-input v-model="form.phone" />
              </el-form-item>
              <!-- 隐藏手动输入头像链接，改为自动填充 -->
              <el-form-item label="Avatar URL" v-show="false">
                <el-input v-model="form.avatar" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateInfo">{{ $t('common.save') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 修改密码 -->
        <el-tab-pane :label="$t('user_center.password_tab')" name="password">
          <el-form :model="pwdForm" label-width="100px" style="width: 400px; margin-top: 20px">
            <el-form-item :label="$t('user_center.old_pwd')">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="$t('user_center.new_pwd')">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="$t('user_center.confirm_pwd')">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="updatePassword">{{ $t('common.confirm') }}</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Tab 3: 我的钱包 -->
        <el-tab-pane :label="$t('user_center.balance')" name="wallet">
          <div class="wallet-box">
            <div class="balance-title">{{ $t('user_center.balance') }} ({{ $t('car.price_unit') }})</div>
            <div class="balance-num">{{ $n(form.balance || 0, 'decimal') }}</div>
            
            <div class="recharge-area">
              <p>{{ $t('user_center.recharge') }}</p>
              <div class="btn-group">
                <el-button type="success" plain @click="recharge(10)">+ 10</el-button>
                <el-button type="success" plain @click="recharge(50)">+ 50</el-button>
                <el-button type="success" plain @click="recharge(100)">+ 100</el-button>
              </div>
            </div>

            <!-- 资金流水表格 -->
            <div class="log-area" style="margin-top: 30px; text-align: left">
              <p style="font-weight: bold; margin-bottom: 10px">{{ $t('user_center.logs') }}</p>
              <el-table :data="walletLogs" border stripe style="width: 100%">
                <el-table-column prop="createTime" :label="$t('user_center.time')" width="180">
                  <template #default="scope">{{ $d(new Date(scope.row.createTime), 'short') }}</template>
                </el-table-column>
                <el-table-column prop="type" :label="$t('user_center.type')" width="100">
                  <template #default="scope">
                    <el-tag v-if="scope.row.type === 1" type="success">{{ $t('user_center.log_recharge') }}</el-tag>
                    <el-tag v-else-if="scope.row.type === 2" type="danger">{{ $t('user_center.log_expense') }}</el-tag>
                    <el-tag v-else-if="scope.row.type === 3" type="warning">{{ $t('user_center.log_income') }}</el-tag>
                    <el-tag v-else type="info">{{ $t('user_center.log_withdraw') }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" :label="$t('user_center.amount')" width="120">
                  <template #default="scope">
                    <span :style="{ color: scope.row.amount > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
                      {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="balanceAfter" :label="$t('user_center.balance')" width="120" />
                <el-table-column prop="remark" :label="$t('user_center.remark')" />
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 4: 我的维权 -->
        <el-tab-pane :label="$t('my_center.complaint')" name="complaint">
          <el-table :data="complaintList" style="width: 100%">
            <el-table-column type="expand">
              <template #default="props">
                <div style="padding: 10px 20px; background: #f9f9f9;">
                  <p><strong>{{ $t('user_center.complaint_content') }}:</strong>{{ props.row.content }}</p>
                  <p v-if="props.row.reply">
                    <strong>{{ $t('user_center.complaint_reply') }}:</strong>
                    <span style="color: #67C23A">{{ props.row.reply }}</span>
                  </p>
                  <p v-else style="color: #999">{{ $t('user_center.no_reply') }}</p>
                  <div v-if="props.row.images" style="margin-top: 10px">
                    <el-image 
                      v-for="(img, i) in props.row.images.split(',')" 
                      :key="i" 
                      :src="img" 
                      style="width: 60px; height: 60px; margin-right: 5px; border-radius: 4px"
                      :preview-src-list="props.row.images.split(',')"
                    />
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" :label="$t('user_center.time')" width="180">
              <template #default="scope">{{ $d(new Date(scope.row.createTime), 'short') }}</template>
            </el-table-column>
            <el-table-column prop="category" :label="$t('user_center.type')" width="120" />
            <el-table-column :label="$t('user_center.target')" width="150">
              <template #default="scope">
                {{ getTargetName(scope.row.targetType) }}: {{ scope.row.targetId }}
              </template>
            </el-table-column>
            <el-table-column prop="status" :label="$t('my_center.status')">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 0" type="info">{{ $t('common.status_pending') }}</el-tag>
                <el-tag v-else-if="scope.row.status === 1" type="warning">{{ $t('common.status_processing') }}</el-tag>
                <el-tag v-else type="success">{{ $t('common.status_done') }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Camera } from '@element-plus/icons-vue' // 引入图标

const { t } = useI18n()
const router = useRouter()
const activeTab = ref('info')

const form = ref({
  username: '',
  nickname: '',
  phone: '',
  avatar: '',
  balance: 0
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const complaintList = ref([])
const walletLogs = ref([])

const getTargetName = (type) => {
  const map = { 
    1: t('user_center.target_order'), 
    2: t('user_center.target_car'), 
    3: t('user_center.target_user') 
  }
  return map[type] || 'Unknown'
}

// 加载用户信息
const loadInfo = async () => {
  try {
    const res = await axios.get('/api/user/info', {
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      form.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

// 👇 头像上传逻辑
const uploadAvatar = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await axios.post('/api/common/upload', formData, {
      headers: { 
        token: localStorage.getItem('token'),
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.data.code === 200) {
      form.value.avatar = res.data.data // 更新表单中的头像 URL
      ElMessage.success(t('common.success'))
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

// 加载投诉列表
const loadComplaints = async () => {
  try {
    const res = await axios.get('/api/complaint/my-list', {
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      complaintList.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

// 加载资金流水
const loadWalletLogs = async () => {
  try {
    const res = await axios.get('/api/wallet/logs', {
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      walletLogs.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

const handleTabClick = (tab) => {
  if (tab.props.name === 'complaint') {
    loadComplaints()
  } else if (tab.props.name === 'wallet') {
    loadWalletLogs()
  }
}

// 修改资料
const updateInfo = async () => {
  try {
    const res = await axios.post('/api/user/update', form.value, {
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      ElMessage.success(t('user_center.save_success'))
      loadInfo() // 刷新
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

// 修改密码
const updatePassword = async () => {
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning(t('user_center.pwd_mismatch'))
    return
  }
  try {
    const res = await axios.post('/api/user/password', pwdForm.value, {
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      ElMessage.success(t('common.success'))
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

// 充值
const recharge = async (amount) => {
  try {
    const res = await axios.post('/api/user/recharge', { amount }, {
      headers: { token: localStorage.getItem('token') }
    })
    if (res.data.code === 200) {
      ElMessage.success(t('common.success'))
      loadInfo() // 刷新余额
      loadWalletLogs() // 刷新流水
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

onMounted(() => {
  loadInfo()
})
</script>

<style scoped>
.user-center-container { padding: 20px; max-width: 1000px; margin: 0 auto; }
.card-header { font-weight: bold; font-size: 18px; }

/* 基本资料 */
.info-wrapper { display: flex; gap: 50px; padding: 20px; }
.avatar-box { text-align: center; }

/* 👇 头像上传样式 */
.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

/* 钱包 */
.wallet-box { text-align: center; padding: 40px; }
.balance-title { font-size: 16px; color: #666; }
.balance-num { font-size: 48px; font-weight: bold; color: #f56c6c; margin: 10px 0 30px; }
.recharge-area { border-top: 1px solid #eee; padding-top: 20px; }
.btn-group { display: flex; justify-content: center; gap: 20px; margin-top: 15px; }
</style>