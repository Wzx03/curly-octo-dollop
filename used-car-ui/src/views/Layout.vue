<template>
  <div class="app-wrapper">
    <!-- 移除动态 class header-hidden -->
    <header class="airbnb-header">
      <div class="header-container">
        
        <!-- 第一层：Logo - 导航 - 用户 -->
        <div class="top-row">
          <!-- Logo -->
          <div class="logo-section" @click="router.push('/cars')">
            <el-icon :size="32" color="#FF385C"><Van /></el-icon>
            <span class="brand-text">UsedCar</span>
          </div>

          <!-- 核心导航 (居中) -->
          <div class="nav-links">
            <span 
              v-if="role === 0"
              class="nav-item" 
              :class="{ active: route.path === '/admin/dashboard' }"
              @click="router.push('/admin/dashboard')"
            >{{ $t('nav.dashboard') }}</span>

            <span 
              class="nav-item" 
              :class="{ active: route.path === '/cars' }"
              @click="router.push('/cars')"
            >{{ $t('nav.cars') }}</span>
            
            <span 
              class="nav-item" 
              :class="{ active: route.path === '/my-cars' }"
              @click="router.push('/my-cars')"
            >{{ $t('nav.manage') }}</span>

            <span 
              class="nav-item" 
              :class="{ active: route.path === '/my-orders' }"
              @click="router.push('/my-orders')"
            >{{ $t('nav.orders') }}</span>

            <span 
              class="nav-item" 
              :class="{ active: route.path === '/service' }" 
              @click="router.push('/service')"
            >
              {{ $t('nav.messages') }}
              <span v-if="hasNewMsg" class="nav-dot"></span>
            </span>

            <span 
              class="nav-item" 
              :class="{ active: route.path === '/community' }"
              @click="router.push('/community')"
            >{{ $t('nav.community') }}</span>
          </div>

          <!-- 右侧菜单 -->
          <div class="user-menu">
            <!-- 👇 修改：带参数跳转 -->
            <div class="host-link" @click="router.push('/my-cars?action=publish')">{{ $t('nav.sell') }}</div>
            
            <div class="globe-icon" @click="showLangModal = true">
              <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" role="presentation" focusable="false" style="display: block; height: 16px; width: 16px; fill: currentcolor;"><path d="m8.002.25a7.77 7.77 0 0 1 7.748 7.776 7.75 7.75 0 0 1 -7.521 7.72l-.246.004a7.75 7.75 0 0 1 -7.73-7.513l-.003-.245a7.75 7.75 0 0 1 7.752-7.742zm1.949 8.5h-3.903c.155 2.897 1.176 5.343 1.886 5.493l.068.007c.68-.002 1.72-2.365 1.932-5.234zm4.255 0h-2.752c-.091 1.96-.53 3.783-1.188 5.076a6.257 6.257 0 0 0 3.905-4.829zm-9.661 0h-2.75a6.257 6.257 0 0 0 3.934 5.075c-.615-1.208-1.036-2.875-1.162-4.686l-.022-.39zm1.188-6.576-.115.046a6.257 6.257 0 0 0 -3.823 5.03h2.75c.085-1.83.471-3.54 1.059-4.81zm2.262-.424c-.702.002-1.784 2.512-1.947 5.5h3.904c-.156-2.903-1.178-5.343-1.892-5.494l-.065-.007zm2.28.432.023.05c.643 1.288 1.069 3.084 1.157 5.018h2.748a6.275 6.275 0 0 0 -3.929-5.068z"></path></svg>
            </div>
            
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-capsule">
                <el-icon :size="16" style="margin-right: 8px"><Menu /></el-icon>
                <el-avatar :size="30" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              </div>
              <template #dropdown>
                <el-dropdown-menu class="airbnb-dropdown">
                  <el-dropdown-item command="/user">{{ $t('nav.profile') }}</el-dropdown-item>
                  <el-divider style="margin: 5px 0" />
                  <el-dropdown-item command="logout">{{ $t('nav.logout') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 第二层：搜索框 (居中) -->
        <!-- 保持 v-if="showSearchBar" -->
        <div class="bottom-row" v-if="showSearchBar">
          <div class="search-bar-real">
            <!-- 👇 筛选按钮 (替代地点) -->
            <div 
              class="search-input-group filter-trigger" 
              @click="globalState.showFilter = !globalState.showFilter"
              :class="{ active: globalState.showFilter }"
            >
              <label>{{ $t('nav.filter') }}</label>
              <div class="placeholder-text">按条件查找</div>
            </div>
            
            <div class="divider-v"></div>
            
            <div class="search-input-group flex-1">
              <label>车型</label>
              <input 
                v-model="globalState.searchKeyword" 
                :placeholder="$t('nav.search_placeholder')" 
                class="mini-input"
                @keyup.enter="handleSearch"
              />
            </div>
            <div class="search-btn-circle big" @click="handleSearch">
              <el-icon color="white" :size="18"><Search /></el-icon>
            </div>
          </div>
        </div>

      </div>
    </header>

    <!-- 移除 ref="mainContentRef" -->
    <!-- 👇 修复：动态 padding-top -->
    <main class="main-content" :style="{ paddingTop: showSearchBar ? '160px' : '80px' }">
      <router-view />
    </main>

    <!-- 语言/货币设置弹窗 (Airbnb 风格) -->
    <el-dialog 
      v-model="showLangModal" 
      width="640px" 
      align-center
      class="airbnb-dialog-base"
      :show-close="false"
    >
      <template #header>
        <div class="dialog-header-simple">
          <div class="close-btn-simple" @click="showLangModal = false">
            <el-icon><Close /></el-icon>
          </div>
        </div>
      </template>

      <div class="dialog-content-padded">
        <el-tabs v-model="activeLangTab" class="airbnb-tabs-simple">
          <el-tab-pane label="语言和地区" name="lang">
            <div class="lang-grid">
              <div 
                v-for="lang in languages" 
                :key="lang.code" 
                class="lang-item" 
                :class="{ active: currentLang === lang.code }"
                @click="changeLanguage(lang.code)"
              >
                <div class="lang-name">{{ lang.name }}</div>
                <div class="lang-region">{{ lang.region }}</div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="货币" name="currency">
            <div class="lang-grid">
              <div 
                v-for="curr in currencies" 
                :key="curr.code" 
                class="lang-item"
                :class="{ active: currentCurrency === curr.code }"
                @click="changeCurrency(curr.code)"
              >
                <div class="lang-name">{{ curr.name }}</div>
                <div class="lang-region">{{ curr.symbol }} - {{ curr.code }}</div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Van, Search, Menu, Filter, Close } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { globalState } from '../store'
import { loadLanguageAsync } from '../locales'
import { useI18n } from 'vue-i18n'
import { useCurrency } from '../hooks/useCurrency'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const { currentCurrency, setCurrency } = useCurrency()

const role = ref(2)
const hasNewMsg = ref(true)

// 👇 计算属性：是否显示搜索栏
const showSearchBar = computed(() => {
  return route.path === '/cars' || route.path === '/'
})

// 语言设置相关
const showLangModal = ref(false)
const activeLangTab = ref('lang')
const currentLang = ref(localStorage.getItem('lang') || 'zh-CN')

const languages = [
  { name: '简体中文', region: '中国', code: 'zh-CN' },
  { name: 'English', region: 'United States', code: 'en-US' }
]

const currencies = [
  { name: '人民币', symbol: '¥', code: 'CNY' },
  { name: '美元', symbol: '$', code: 'USD' },
  { name: '欧元', symbol: '€', code: 'EUR' },
  { name: '日元', symbol: '¥', code: 'JPY' },
  { name: '英镑', symbol: '£', code: 'GBP' },
  { name: '韩元', symbol: '₩', code: 'KRW' }
]

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      role.value = payload.role
    } catch (e) {}
  }
})

const changeLanguage = async (lang) => {
  await loadLanguageAsync(lang)
  currentLang.value = lang
}

const changeCurrency = (code) => {
  setCurrency(code)
}

const handleSearch = () => {
  if (route.path !== '/cars') {
    router.push('/cars')
  }
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    ElMessageBox.confirm(t('common.logout_confirm'), t('common.tips'), { 
      confirmButtonText: t('common.confirm'), 
      cancelButtonText: t('common.cancel') 
    })
      .then(() => {
        localStorage.removeItem('token')
        router.push('/login')
        ElMessage.success(t('common.logout_success'))
      })
  } else {
    router.push(cmd)
  }
}
</script>

<style>
/* 1. 恢复 html/body 的高度控制，禁止全局滚动 */
html, body {
  margin: 0; 
  padding: 0; 
  height: 100%; 
  /* 移除 overflow: hidden; */
}

/* 全局弹窗样式覆盖 */
.airbnb-dialog-base {
  border-radius: 12px;
  overflow: hidden;
}
.airbnb-dialog-base .el-dialog__header {
  padding: 0;
  margin: 0;
}
.airbnb-dialog-base .el-dialog__body {
  padding: 0;
}

/* 👇 新增：全局 el-main 样式覆盖 */
.el-main {
  padding: 0 !important; /* 去掉默认的 20px 内边距 */
  background-color: #f0f2f5; /* 建议把灰色背景色移到这里，这样整个底色都是灰的 */
}
</style>

<style scoped>
.app-wrapper {
  font-family: 'Circular', -apple-system, BlinkMacSystemFont, Roboto, 'Helvetica Neue', sans-serif;
  background-color: #fff;
  height: 100vh; /* 占满全屏 */
  display: flex;
  flex-direction: column; /* 垂直布局 */
}

.airbnb-header {
  /* 👇 保持 fixed 定位 */
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  /* height: 160px;  <-- 移除固定高度，让内容撑开 */
  background: #fff;
  border-bottom: 1px solid #ebebeb;
  z-index: 1000;
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
  /* 移除 transition */
}

.header-container {
  max-width: 1760px; margin: 0 auto; padding: 0 40px;
  display: flex; flex-direction: column;
}

/* 第一层 */
.top-row {
  height: 80px; display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0; /* 保持高度 */
}

.logo-section {
  display: flex; align-items: center; cursor: pointer; color: #FF385C; flex: 1;
}
.brand-text {
  font-size: 24px; font-weight: 800; margin-left: 8px; letter-spacing: -0.5px;
}

.nav-links {
  display: flex; gap: 24px; font-size: 16px; color: #717171; justify-content: center; flex: 2;
}
.nav-item {
  cursor: pointer; position: relative; padding: 10px 0; transition: color 0.2s; white-space: nowrap;
}
.nav-item:hover { color: #222; opacity: 0.7; }
.nav-item.active { color: #222; font-weight: 600; }
.nav-item.active::after {
  content: ""; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%);
  width: 20px; height: 2px; background: #222;
}

.nav-dot {
  display: inline-block; width: 8px; height: 8px; background: #ef4444; border-radius: 50%;
  position: absolute; top: 8px; right: -8px;
}

.user-menu { display: flex; align-items: center; gap: 15px; flex: 1; justify-content: flex-end; }

/* 第二层 */
.bottom-row {
  height: 80px; display: flex; justify-content: center; align-items: flex-start; position: relative;
}

.search-bar-real {
  display: flex; align-items: center;
  background: #fff;
  border: 1px solid #dddddd;
  border-radius: 32px;
  padding: 0;
  box-shadow: 0 3px 12px rgba(0,0,0,0.1);
  transition: box-shadow 0.2s;
  width: 600px; /* 更宽 */
  height: 64px;
  position: relative;
}
.search-bar-real:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.15); background: #f7f7f7; }

.search-input-group {
  padding: 14px 24px; display: flex; flex-direction: column; justify-content: center; cursor: pointer;
  border-radius: 32px;
}
.search-input-group:hover { background: #ebebeb; }
.search-input-group label { font-size: 12px; font-weight: 800; color: #222; margin-bottom: 2px; display: block; }
.mini-input { border: none; outline: none; background: transparent; font-size: 14px; color: #717171; width: 100%; }

/* 筛选触发器样式 */
.filter-trigger {
  min-width: 120px;
}
.filter-trigger.active {
  background: #f0f0f0;
}
.placeholder-text {
  font-size: 14px;
  color: #717171;
}

.divider-v { width: 1px; height: 32px; background: #ddd; }
.flex-1 { flex: 1; }

.search-btn-circle.big {
  width: 48px; height: 48px; background: #FF385C; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  margin-right: 8px; transition: transform 0.2s;
}
.search-btn-circle.big:hover { transform: scale(1.05); }

.host-link { font-size: 14px; font-weight: 600; color: #222; cursor: pointer; padding: 10px; border-radius: 22px; }
.host-link:hover { background: #f7f7f7; }

.user-capsule {
  display: flex; align-items: center; padding: 5px 5px 5px 12px;
  border: 1px solid #dddddd; border-radius: 21px; cursor: pointer; transition: box-shadow 0.2s;
}
.user-capsule:hover { box-shadow: 0 2px 4px rgba(0,0,0,0.18); }

.globe-icon {
  width: 40px; height: 40px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; cursor: pointer; transition: background 0.2s; color: #222;
}
.globe-icon:hover { background: #f7f7f7; }

.main-content {
  /* 👇 关键修改：给内容区顶部增加 padding，防止被 fixed header 遮挡 */
  /* 如果有搜索栏，padding 160px；如果没有，padding 80px */
  /* 这里我们用 CSS 变量或者简单点，直接给个较大的值，或者动态绑定 style */
  /* padding-top: 160px;  <-- 移除固定值，改为动态绑定 */
  flex: 1; /* 占满剩余空间 */
  /* 移除 overflow: hidden; */
  overflow-y: auto; /* <-- 允许 main-content 内部滚动 */
  position: relative;
}

/* 弹窗样式 */
.dialog-header-simple {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.close-btn-simple {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}
.close-btn-simple:hover { background: #f7f7f7; }

.dialog-content-padded {
  padding: 0 24px 24px;
}

/* Tabs 样式 */
.airbnb-tabs-simple :deep(.el-tabs__nav-wrap::after) {
  height: 1px; background-color: #ebebeb;
}
.airbnb-tabs-simple :deep(.el-tabs__item) {
  font-size: 16px; font-weight: 600; color: #717171; padding: 0 20px;
}
.airbnb-tabs-simple :deep(.el-tabs__item.is-active) {
  color: #222;
}
.airbnb-tabs-simple :deep(.el-tabs__active-bar) {
  background-color: #222; height: 2px;
}

.lang-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; padding-top: 20px;
}
.lang-item {
  padding: 12px; border-radius: 8px; cursor: pointer; transition: background 0.2s;
}
.lang-item:hover { background: #f7f7f7; }
.lang-item.active { background: #f0f0f0; border: 1px solid #222; }
.lang-name { font-size: 14px; color: #222; margin-bottom: 4px; font-weight: 500; }
.lang-region { font-size: 12px; color: #717171; }
</style>