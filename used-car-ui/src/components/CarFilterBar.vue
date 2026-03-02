<template>
  <div class="filter-bar">
    <div class="filter-row" v-for="(group, key) in filterGroups" :key="key">
      <div class="label">{{ group.label }}</div>
      <div class="options">
        <span 
          v-for="opt in group.options" 
          :key="opt.val"
          :class="['option-item', isSelected(key, opt.val) ? 'active' : '']"
          @click="handleSelect(key, opt.val)"
        >
          {{ opt.label }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits(['change'])
const { t } = useI18n()

// 筛选参数
const params = reactive({
  energyType: '不限',
  mileageRange: null,
  carAgeRange: null,
  gearbox: '不限',
  emissionStandard: '不限',
  displacement: '不限',
  driveMode: '不限',
  manufacturerType: '不限'
})

// 筛选配置 (使用 computed 以支持动态翻译)
const filterGroups = computed(() => ({
  energyType: {
    label: t('filter.energy'),
    options: [
      { label: t('filter.unlimited'), val: '不限' },
      { label: t('options.gasoline'), val: '汽油' },
      { label: t('options.diesel'), val: '柴油' },
      { label: t('options.hybrid'), val: '油电混合' }, // 简单映射
      { label: t('options.electric'), val: '电动' }
    ]
  },
  mileageRange: {
    label: t('filter.mileage'),
    options: [
      { label: t('filter.unlimited'), val: null },
      { label: `< 1 ${t('car.mileage_unit')}`, val: 1 },
      { label: `< 3 ${t('car.mileage_unit')}`, val: 3 },
      { label: `< 5 ${t('car.mileage_unit')}`, val: 5 },
      { label: `< 10 ${t('car.mileage_unit')}`, val: 10 }
    ]
  },
  carAgeRange: {
    label: t('filter.age'),
    options: [
      { label: t('filter.unlimited'), val: null },
      { label: `< 1 ${t('car.year')}`, val: 1 },
      { label: `< 3 ${t('car.year')}`, val: 3 },
      { label: `< 5 ${t('car.year')}`, val: 5 },
      { label: `< 8 ${t('car.year')}`, val: 8 }
    ]
  },
  gearbox: {
    label: t('filter.gearbox'),
    options: [
      { label: t('filter.unlimited'), val: '不限' },
      { label: t('options.automatic'), val: '自动' },
      { label: t('options.manual'), val: '手动' }
    ]
  },
  emissionStandard: {
    label: 'Emission', // 暂时没加到 filter 语言包，直接用英文或加 key
    options: [
      { label: t('filter.unlimited'), val: '不限' },
      { label: '国VI', val: '国VI' },
      { label: '国V', val: '国V' },
      { label: '国IV', val: '国IV' }
    ]
  },
  displacement: {
    label: 'Engine',
    options: [
      { label: t('filter.unlimited'), val: '不限' },
      { label: '< 1.0L', val: '1.0L以下' },
      { label: '1.1-1.6L', val: '1.1-1.6L' },
      { label: '1.7-2.0L', val: '1.7-2.0L' },
      { label: '2.1-2.5L', val: '2.1-2.5L' },
      { label: '2.6-3.0L', val: '2.6-3.0L' },
      { label: '> 3.0L', val: '3.0L以上' }
    ]
  },
  driveMode: {
    label: 'Drive',
    options: [
      { label: t('filter.unlimited'), val: '不限' },
      { label: t('options.fwd'), val: '前驱' },
      { label: t('options.rwd'), val: '后驱' },
      { label: t('options.awd'), val: '四驱' }
    ]
  },
  manufacturerType: {
    label: 'Type',
    options: [
      { label: t('filter.unlimited'), val: '不限' },
      { label: t('options.domestic'), val: '国产' },
      { label: t('options.joint'), val: '合资' },
      { label: t('options.import'), val: '进口' }
    ]
  }
}))

const isSelected = (key, val) => {
  return params[key] === val
}

const handleSelect = (key, val) => {
  params[key] = val
  emit('change', { ...params })
}
</script>

<style scoped>
.filter-bar {
  /* 移除背景色和阴影，融入面板 */
  padding: 10px 0;
}

.filter-row {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  padding: 4px 0;
}
.filter-row:last-child { border-bottom: none; }

.label {
  width: 80px;
  font-size: 14px;
  color: #222; /* 深色标题 */
  font-weight: 600;
  padding-top: 8px; /* 对齐胶囊 */
}

.options {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12px; /* 增加间距 */
}

/* Airbnb 风格胶囊样式 */
.option-item {
  font-size: 14px;
  color: #222;
  padding: 8px 16px;
  cursor: pointer;
  border: 1px solid #dddddd; /* 默认边框 */
  border-radius: 30px; /* 圆角胶囊 */
  transition: all 0.2s;
  background: white;
}

.option-item:hover {
  border-color: #222; /* 悬停黑色边框 */
}

.option-item.active {
  background-color: #222; /* 选中黑色背景 */
  color: white;
  border-color: #222;
  font-weight: 500;
}
</style>