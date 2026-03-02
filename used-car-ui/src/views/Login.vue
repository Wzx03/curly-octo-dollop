<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 标题 -->
      <div class="title-group">
        <h1>{{ isLogin ? $t('login.title') : $t('login.register_title') }}</h1>
        <p>{{ $t('login.subtitle') }}</p>
      </div>

      <!-- 表单 -->
      <el-form :model="form" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" :placeholder="$t('login.username')" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" :placeholder="$t('login.password')" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">{{ $t('login.remember_me') }}</el-checkbox>
        </el-form-item>
        <el-form-item v-if="!isLogin">
          <el-input v-model="form.phone" placeholder="手机号 (可选)" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="submit-btn" 
            size="large" 
            @click="handleSubmit" 
            :loading="loading"
          >
            {{ isLogin ? $t('login.login_btn') : $t('login.register_btn') }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 切换链接 -->
      <div class="switch-link">
        <span @click="isLogin = !isLogin">
          {{ isLogin ? $t('login.register_link') : $t('login.login_link') }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { addAccount } from '../utils/auth' // 👈 引入 addAccount

const { t } = useI18n()
const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)
const rememberMe = ref(false)

const form = ref({
  username: '',
  password: '',
  phone: ''
})

onMounted(() => {
  const savedUsername = localStorage.getItem('username')
  const savedPassword = localStorage.getItem('password')
  if (savedUsername && savedPassword) {
    form.value.username = savedUsername
    form.value.password = atob(savedPassword)
    rememberMe.value = true
  }
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const url = isLogin.value ? '/api/auth/login' : '/api/auth/register'
    const res = await axios.post(url, form.value)
    if (res.data.code === 200) {
      if (isLogin.value) {
        // Handle remember me logic
        if (rememberMe.value) {
          localStorage.setItem('username', form.value.username)
          localStorage.setItem('password', btoa(form.value.password))
        } else {
          localStorage.removeItem('username')
          localStorage.removeItem('password')
        }
        
        // 👇 关键修复：调用 addAccount 解析并保存用户信息
        const token = res.data.data
        addAccount(token)
        
        ElMessage.success('登录成功')
        router.push('/cars')
      } else {
        ElMessage.success('注册成功，请登录')
        isLogin.value = true
      }
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f7fa;
}
.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 28px rgba(0,0,0,0.08);
}
.title-group {
  text-align: center;
  margin-bottom: 32px;
}
.title-group h1 {
  font-size: 28px;
  font-weight: 800;
  color: #222;
  margin: 0 0 8px 0;
}
.title-group p {
  font-size: 16px;
  color: #717171;
  margin: 0;
}
.submit-btn {
  width: 100%;
  background: #FF385C;
  border-color: #FF385C;
}
.switch-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #409EFF;
  cursor: pointer;
}
</style>