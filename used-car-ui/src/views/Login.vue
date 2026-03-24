<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center relative overflow-hidden">
    <div class="absolute top-[-10%] left-[-10%] w-96 h-96 bg-blue-400 rounded-full mix-blend-multiply filter blur-[100px] opacity-40 animate-pulse"></div>
    <div class="absolute bottom-[-10%] right-[-10%] w-96 h-96 bg-indigo-400 rounded-full mix-blend-multiply filter blur-[100px] opacity-40 animate-pulse" style="animation-delay: 2s;"></div>

    <div class="relative w-full max-w-4xl bg-white/80 backdrop-blur-xl shadow-2xl rounded-2xl overflow-hidden flex z-10 mx-4 transition-all duration-500 hover:shadow-blue-500/20">

      <div class="hidden md:flex flex-col justify-center items-center w-1/2 bg-gradient-to-br from-blue-300 to-indigo-800 text-white p-10 relative overflow-hidden">
        <img
            src="https://images.unsplash.com/photo-1583121274602-3e2820c69888?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
            alt="Car Background"
            class="absolute inset-0 w-full h-full object-cover opacity-30 mix-blend-overlay"
        />
        <div class="relative z-10 text-center">
          <div class="bg-white/20 p-4 rounded-full inline-block mb-6 shadow-lg backdrop-blur-sm">
            <el-icon :size="40"><Van /></el-icon>
          </div>
          <h1 class="text-4xl font-extrabold mb-4 tracking-wider">星选二手车</h1>
          <p class="text-blue-100 text-lg font-light tracking-wide mt-2">安全 · 透明 · 便捷的交易体验</p>
        </div>
      </div>

      <div class="w-full md:w-1/2 p-8 sm:p-12 bg-white">
        <div class="flex justify-between items-end mb-8">
          <div>
            <h2 class="text-3xl font-bold text-gray-800 tracking-tight">{{ isLogin ? '欢迎回来' : '创建新账号' }}</h2>
            <p class="text-gray-400 text-sm mt-2">{{ isLogin ? '请输入您的账号密码登录' : '只需几步即可加入我们' }}</p>
          </div>
          <el-button link type="primary" @click="toggleMode" class="text-sm font-medium">
            {{ isLogin ? '立即注册 →' : '返回登录 →' }}
          </el-button>
        </div>

        <el-form
            v-if="isLogin"
            :model="loginForm"
            :rules="loginRules"
            ref="loginFormRef"
            @keyup.enter="handleLogin"
            size="large"
        >
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>

          <div class="flex justify-between items-center mb-6">
            <el-checkbox v-model="loginForm.rememberMe" class="text-gray-500">记住我</el-checkbox>
            <el-link type="primary" :underline="false" class="text-sm text-gray-500 hover:text-blue-600">忘记密码？</el-link>
          </div>

          <el-button type="primary" class="w-full shadow-lg shadow-blue-500/30" round :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form>

        <el-form
            v-else
            :model="registerForm"
            :rules="registerRules"
            ref="registerFormRef"
            size="large"
        >
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="设置用户名 (4-20位字符)" :prefix-icon="User" clearable />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="设置密码 (至少6位，包含字母和数字)" :prefix-icon="Lock" show-password />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" placeholder="手机号码 (选填)" :prefix-icon="Phone" clearable />
          </el-form-item>

          <el-button type="primary" class="w-full shadow-lg shadow-blue-500/30 mt-4" round :loading="loading" @click="handleRegister">
            立 即 注 册
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Van } from '@element-plus/icons-vue'
import axios from 'axios'
import { addAccount } from '../utils/auth' // 使用您封装的 auth.js 工具

const router = useRouter()

// 状态控制
const isLogin = ref(true)
const loading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 切换登录/注册模式
const toggleMode = () => {
  isLogin.value = !isLogin.value
  // 切换时重置表单状态
  if (isLogin.value) {
    registerFormRef.value?.resetFields()
  } else {
    loginFormRef.value?.resetFields()
  }
}

// ================= 登录逻辑 =================
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('/api/auth/login', loginForm)
        if (res.data.code === 200) {
          ElMessage.success('登录成功，欢迎回来！')
          const token = res.data.data
          addAccount(token) // 解析并保存用户信息和 Token
          router.push('/cars') // 跳转到主页列表
        } else {
          ElMessage.error(res.data.msg || '登录失败')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '网络或服务器错误')
      } finally {
        loading.value = false
      }
    }
  })
}

// ================= 注册逻辑 =================
const registerForm = reactive({
  username: '',
  password: '',
  phone: ''
})

const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{4,20}$/, message: '需4-20位字母或数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).{6,20}$/, message: '需6-20位，且包含字母和数字', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
})

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('/api/auth/register', registerForm)
        if (res.data.code === 200) {
          ElMessage.success('注册成功，请登录！')
          // 自动填充刚注册的账号并切换到登录页
          loginForm.username = registerForm.username
          loginForm.password = registerForm.password
          isLogin.value = true
        } else {
          ElMessage.error(res.data.msg || '注册失败')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '网络或服务器错误')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 定义背景光晕的呼吸动画 */
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
.animate-blob {
  animation: blob 7s infinite;
}
</style>