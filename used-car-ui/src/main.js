import { createApp } from 'vue'
import App from './App.vue'

// 1. 引入路由
import router from './router'

// 2. 引入 Element Plus 及其样式
import ElementPlus, { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'

// 3. 引入全局样式
import './style.css'

// 4. 引入 i18n
import i18n from './locales'

// 5. 引入 Axios 并配置拦截器
import axios from 'axios'

// 全局响应拦截器：处理 401 Token 过期
axios.interceptors.response.use(
    response => {
        return response
    },
    error => {
        if (error.response && error.response.status === 401) {
            // 避免重复弹窗 (可选优化，这里简单处理)
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            router.push('/login')
        }
        return Promise.reject(error)
    }
)

const app = createApp(App)

app.use(router)      // 使用路由
app.use(ElementPlus) // 使用 UI 库
app.use(i18n)        // 使用 i18n
app.mount('#app')