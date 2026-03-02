<template>
  <div class="airbnb-list-container">
    
    <!-- 1. 可收纳的筛选面板 (悬浮在顶部) -->
    <div class="filter-panel-wrapper" v-show="globalState.showFilter">
      <div class="filter-panel">
        <CarFilterBar @change="handleFilterChange" />
        <div class="panel-footer">
          <span class="clear-btn" @click="resetFilter">{{ $t('car.clear_filter') }}</span>
          <el-button type="primary" color="#222" @click="globalState.showFilter = false">{{ $t('car.show_cars') }}</el-button>
        </div>
      </div>
      <div class="filter-mask" @click="globalState.showFilter = false"></div>
    </div>

    <!-- 2. 推荐车源 (横向滚动 + 交互优化) -->
    <div class="section-wrapper" v-if="recommendList.length > 0">
      <!-- 头部：标题 + 导航按钮 -->
      <div class="section-header-flex">
        <div class="clickable-header" @click="handleRecommendClick">
          <h2>{{ $t('car.recommend_title') }}</h2>
          <el-icon class="header-arrow"><ArrowRight /></el-icon>
        </div>
        
        <!-- 导航按钮组 -->
        <div class="nav-controls">
          <button 
            class="nav-btn" 
            :disabled="!showLeftBtn"
            @click="scroll('left')"
          >
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <button 
            class="nav-btn" 
            :disabled="!showRightBtn"
            @click="scroll('right')"
          >
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </div>
      
      <div class="carousel-wrapper">
        <!-- 滚动容器 -->
        <div 
          class="scroll-container" 
          ref="scrollContainerRef"
          @scroll="checkScroll"
        >
          <div 
            v-for="car in recommendList" 
            :key="'rec-'+car.id" 
            class="carousel-item"
          >
            <CarCard 
              :data="car" 
              :is-favorited="isFavorited(car.id)"
              @click="toDetail(car.id)" 
              @toggle-favorite="toggleFavorite(car.id)"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 全部车源网格 (恢复原样) -->
    <div class="section-wrapper">
      <div class="section-header">
        <h2>{{ $t('car.all_cars_title') }}</h2>
      </div>
      
      <!-- 👇 强制内联样式：7列网格 (加 !important) -->
      <div style="display: grid !important; grid-template-columns: repeat(7, 1fr) !important; gap: 16px !important; width: 100% !important;">
        <div v-for="car in carList" :key="car.id" class="car-card">
          <div class="img-container" @click="toDetail(car.id)">
            <img :src="car.image || 'https://dummyimage.com/600x400/eee/999'" class="car-img" />
            
            <div class="heart-btn" @click.stop="toggleFavorite(car.id)">
              <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" role="presentation" focusable="false" 
                   :class="['heart-svg', { 'is-favorited': isFavorited(car.id) }]">
                <path d="m16 28c7-4.733 14-10 14-17 0-1.792-.683-3.583-2.05-4.95-1.367-1.366-3.158-2.05-4.95-2.05-1.791 0-3.583.684-4.949 2.05l-2.051 2.051-2.05-2.051c-1.367-1.366-3.158-2.05-4.95-2.05-1.791 0-3.583.684-4.949 2.05-1.367 1.367-2.051 3.158-2.051 4.95 0 7 7 12.267 14 17z"></path>
              </svg>
            </div>

            <div class="super-host" v-if="car.conditionLevel === 1">{{ $t('car.condition_excellent') }}</div>
          </div>

          <div class="info-container" @click="toDetail(car.id)">
            <div class="row-1">
              <span class="title">{{ car.brand }} {{ car.model }}</span>
              <div class="rating">
                <el-icon><StarFilled /></el-icon>
                <span>4.9</span>
              </div>
            </div>
            <div class="row-2">
              <span>{{ car.buyYear }}{{ $t('car.year') }} · {{ car.mileage }}{{ $t('car.mileage_unit') }}</span>
              <span class="price-tag">{{ formatPrice(car.price) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-if="carList.length === 0" :description="$t('car.no_cars_found')" />

      <!-- 👇 新增分页组件 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, Star, StarFilled } from '@element-plus/icons-vue'
import CarFilterBar from '../components/CarFilterBar.vue'
import CarCard from '../components/CarCard.vue'
import { globalState } from '../store'
import { getToken } from '../utils/auth'
import { useI18n } from 'vue-i18n'
import { useCurrency } from '../hooks/useCurrency'

const router = useRouter()
const { t } = useI18n()
const { formatPrice } = useCurrency()
const carList = ref([])
const recommendList = ref([])
const filterParams = ref({})
const favoriteIds = ref(new Set())

// 分页状态
const currentPage = ref(1)
const pageSize = ref(14) // 7列 x 2行 = 14
const total = ref(0)

// 滚动相关状态
const scrollContainerRef = ref(null)
const showLeftBtn = ref(false)
const showRightBtn = ref(true)

watch(() => globalState.searchKeyword, () => {
  currentPage.value = 1 // 搜索时重置页码
  loadCars()
})

const loadCars = async () => {
  try {
    const params = { 
      ...filterParams.value, 
      brand: globalState.searchKeyword,
      page: currentPage.value,
      size: pageSize.value
    }
    const res = await axios.get('/api/car/list', {
      params: params,
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      if (res.data.data.records) {
        carList.value = res.data.data.records
        total.value = res.data.data.total
      } else {
        carList.value = res.data.data
        total.value = res.data.data.length
      }
    }
  } catch (err) {}
}

const loadRecommend = async () => {
  const token = getToken()
  if (!token) return 

  try {
    const res = await axios.get('/api/car/recommend', {
      headers: { token: token }
    })
    if (res.data.code === 200) {
      recommendList.value = res.data.data.slice(0, 14)
      nextTick(() => checkScroll())
    }
  } catch (e) {}
}

const loadFavorites = async () => {
  const token = getToken()
  if (!token) return
  try {
    const res = await axios.get('/api/favorite/my-ids', { headers: { token } })
    if (res.data.code === 200) {
      favoriteIds.value = new Set(res.data.data)
    }
  } catch (e) {}
}

const toggleFavorite = async (carId) => {
  const token = getToken()
  if (!token) {
    router.push('/login')
    return
  }
  try {
    await axios.post(`/api/favorite/toggle/${carId}`, {}, { headers: { token } })
    if (favoriteIds.value.has(carId)) {
      favoriteIds.value.delete(carId)
    } else {
      favoriteIds.value.add(carId)
    }
  } catch (e) {}
}

const isFavorited = (carId) => {
  return favoriteIds.value.has(carId)
}

const handleFilterChange = (filters) => {
  filterParams.value = filters
  currentPage.value = 1
  loadCars()
}

const resetFilter = () => {
  filterParams.value = {}
  globalState.searchKeyword = ''
  currentPage.value = 1
  loadCars()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadCars()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const toDetail = (id) => {
  router.push(`/car/${id}`)
}

const handleRecommendClick = () => {
  router.push({ path: '/cars', query: { type: 'recommend' } })
}

const scroll = (direction) => {
  const container = scrollContainerRef.value
  if (!container) return
  const scrollAmount = container.clientWidth
  const targetScrollLeft = direction === 'left' 
    ? container.scrollLeft - scrollAmount 
    : container.scrollLeft + scrollAmount
  container.scrollTo({ left: targetScrollLeft, behavior: 'smooth' })
}

const checkScroll = () => {
  const container = scrollContainerRef.value
  if (!container) return
  showLeftBtn.value = container.scrollLeft > 1
  showRightBtn.value = container.scrollLeft + container.clientWidth < container.scrollWidth - 1
}

onMounted(() => {
  loadCars()
  loadRecommend()
  loadFavorites()
  window.addEventListener('resize', checkScroll)
})
</script>

<style scoped>
.airbnb-list-container {
  max-width: 1440px;
  margin: 0 auto;
  padding-top: 20px;
  position: relative;
}

.filter-panel-wrapper {
  position: fixed;
  top: 160px;
  left: 0;
  width: 100%;
  z-index: 998;
}

.filter-panel {
  background: white;
  padding: 20px 40px;
  border-bottom: 1px solid #ebebeb;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  position: relative;
  z-index: 1000;
  max-height: 80vh;
  overflow-y: auto;
  max-width: 1440px;
  margin: 0 auto;
}

.filter-mask {
  position: fixed;
  top: 160px;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.25);
  z-index: 999;
}

.panel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebebeb;
}
.clear-btn {
  font-size: 14px;
  text-decoration: underline;
  cursor: pointer;
  color: #717171;
}

.section-wrapper {
  width: 100%;
  padding: 0 40px;
  box-sizing: border-box;
  margin-bottom: 40px;
}

.section-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
.section-header h2, .clickable-header h2 { font-size: 26px; font-weight: 800; margin: 0; color: #222; }
.section-header p { color: #717171; font-size: 16px; margin: 0; }

.clickable-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s;
}
.clickable-header:hover {
  opacity: 0.7;
}
.header-arrow {
  margin-left: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #222;
}

.nav-controls {
  display: flex;
  gap: 8px;
}
.nav-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(0,0,0,0.08);
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #222;
}
.nav-btn:hover:not(:disabled) {
  border-color: #222;
  background: #f7f7f7;
  transform: scale(1.04);
}
.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  border-color: rgba(0,0,0,0.05);
}

.carousel-wrapper {
  position: relative;
  width: 100%;
}

.scroll-container {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding-bottom: 4px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.scroll-container::-webkit-scrollbar {
  display: none;
}

.carousel-item {
  flex: 0 0 calc((100% - 96px) / 7);
  max-width: calc((100% - 96px) / 7);
}

.car-card { 
  cursor: default; /* Remove cursor from the whole card */
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  width: auto; 
  min-width: 0; 
}
.car-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.img-container {
  position: relative;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
  margin-bottom: 12px;
  cursor: pointer; /* Add cursor only to image and info */
}
.car-img { width: 100%; height: 100%; object-fit: cover; }

.heart-btn { 
  position: absolute; 
  top: 12px; 
  right: 12px; 
  z-index: 2; 
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}
.heart-btn:hover { transform: scale(1.1); }

.heart-svg {
  display: block;
  height: 20px;
  width: 20px;
  stroke: white;
  stroke-width: 2;
  overflow: visible;
  fill: rgba(0, 0, 0, 0.5);
  transition: all 0.2s;
}
.heart-svg.is-favorited {
  fill: #FF385C;
  stroke: #FF385C;
}

.super-host { 
  position: absolute; 
  top: 12px; 
  left: 12px; 
  background: rgba(255, 255, 255, 0.95); 
  padding: 4px 8px; 
  border-radius: 4px; 
  font-size: 12px; 
  font-weight: bold; 
  color: #222;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.info-container { color: #222; padding: 0 4px; cursor: pointer; }

.row-1 { 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
  margin-bottom: 4px; 
}
.title { 
  font-weight: 700;
  font-size: 16px; 
  color: #222;
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis; 
}
.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #222;
}

.row-2 { 
  color: #717171;
  font-size: 14px; 
  margin-bottom: 6px; 
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-tag {
  font-weight: 700;
  color: #222;
  font-size: 15px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 20px;
}
</style>