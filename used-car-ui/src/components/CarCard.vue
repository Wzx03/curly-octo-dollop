<template>
  <div class="group cursor-pointer" @click="$emit('click')">
    <!-- 图片区域 -->
    <div class="relative w-full aspect-[1/1] overflow-hidden rounded-xl bg-gray-100 mb-3">
      <img 
        :src="data.image || 'https://dummyimage.com/600x400/eee/999'" 
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      
      <!-- 收藏按钮 -->
      <div 
        class="absolute top-3 right-3 z-10" 
        @click.stop="$emit('toggle-favorite')"
      >
        <div class="w-8 h-8 rounded-full bg-black/50 flex items-center justify-center hover:scale-110 transition-transform">
          <svg 
            viewBox="0 0 32 32" 
            xmlns="http://www.w3.org/2000/svg" 
            aria-hidden="true" 
            role="presentation" 
            focusable="false" 
            class="block h-4 w-4 stroke-white stroke-[2px] overflow-visible transition-colors duration-200"
            :class="isFavorited ? 'fill-[#FF385C] stroke-[#FF385C]' : 'fill-transparent'"
          >
            <path d="m16 28c7-4.733 14-10 14-17 0-1.792-.683-3.583-2.05-4.95-1.367-1.366-3.158-2.05-4.95-2.05-1.791 0-3.583.684-4.949 2.05l-2.051 2.051-2.05-2.051c-1.367-1.366-3.158-2.05-4.95-2.05-1.791 0-3.583.684-4.949 2.05-1.367 1.367-2.051 3.158-2.051 4.95 0 7 7 12.267 14 17z"></path>
          </svg>
        </div>
      </div>

      <!-- 推荐标签 -->
      <div v-if="data.conditionLevel === 1" class="absolute top-3 left-3 bg-white/95 px-2 py-1 rounded-md text-xs font-bold text-[#222] shadow-sm">
        {{ $t('car.condition_excellent') }}
      </div>
    </div>

    <!-- 信息区域 (仿照全部车源的布局) -->
    <div class="px-1">
      <!-- 第一行：标题 + 评分 -->
      <div class="flex justify-between items-center mb-1">
        <span class="font-bold text-[16px] text-[#222] truncate pr-2">{{ data.brand }} {{ data.model }}</span>
        <div class="flex items-center gap-1 shrink-0">
          <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" role="presentation" focusable="false" class="block h-3 w-3 fill-[#222222]"><path d="M15.094 1.579l-4.124 8.885-9.86 1.27a1 1 0 0 0-.54 1.736l7.293 6.815-1.991 9.692a1 1 0 0 0 1.488 1.081L16 26.223l8.641 4.834a1 1 0 0 0 1.488-1.08l-1.992-9.693 7.294-6.815a1 1 0 0 0-.541-1.735l-9.86-1.271-4.127-8.885a1 1 0 0 0-1.809 0z"></path></svg>
          <span class="text-[14px] text-[#222]">5.0</span>
        </div>
      </div>

      <!-- 第二行：年份里程 + 价格 (两端对齐) -->
      <div class="flex justify-between items-center text-[14px] text-[#717171]">
        <span>{{ data.buyYear }}{{ $t('car.year') }} · {{ data.mileage }}{{ $t('car.mileage_unit') }}</span>
        <span class="font-bold text-[15px] text-[#222]">{{ formatPrice(data.price) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCurrency } from '../hooks/useCurrency'

const { formatPrice } = useCurrency()

defineProps({
  data: {
    type: Object,
    required: true
  },
  isFavorited: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click', 'toggle-favorite'])
</script>