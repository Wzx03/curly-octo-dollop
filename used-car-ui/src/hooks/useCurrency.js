import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

// 汇率表 (基准: CNY)
const RATES = {
  CNY: 1,
  USD: 0.14, // 1 CNY ≈ 0.14 USD
  EUR: 0.13,
  JPY: 20.5,
  GBP: 0.11,
  KRW: 185.0
}

// 货币符号
const SYMBOLS = {
  CNY: '¥',
  USD: '$',
  EUR: '€',
  JPY: '¥',
  GBP: '£',
  KRW: '₩'
}

// 全局状态 (简单起见，这里用 ref，也可以用 Pinia)
const currentCurrency = ref(localStorage.getItem('currency') || 'CNY')

export function useCurrency() {
  const { n, t } = useI18n()

  const setCurrency = (code) => {
    if (RATES[code]) {
      currentCurrency.value = code
      localStorage.setItem('currency', code)
    }
  }

  const currencySymbol = computed(() => SYMBOLS[currentCurrency.value])

  // 格式化价格
  // price: 人民币金额 (单位: 万)
  // unit: 是否显示单位 (默认 true)
  const formatPrice = (priceCNY, showUnit = true) => {
    if (priceCNY === null || priceCNY === undefined) return '-'
    
    const rate = RATES[currentCurrency.value]
    const symbol = SYMBOLS[currentCurrency.value]
    
    // 1. 先转换为目标货币的 "万" 单位数值
    let val = Number(priceCNY) * rate
    
    // 2. 获取当前语言环境下的单位 (万 vs k)
    // zh-CN: "万"
    // en-US: "k"
    const unitLabel = t('car.price_unit')
    
    // 3. 如果单位是 k，需要将 "万" 转换为 "k" (x10)
    // 例如: 1 万 = 10 k
    if (unitLabel === 'k') {
      val = val * 10
    }

    const finalUnit = showUnit ? unitLabel : ''

    // 使用 vue-i18n 的 n 函数格式化数字 (保留1位小数)
    return `${symbol} ${n(val, 'decimal')} ${finalUnit}`
  }

  // 仅获取数值 (用于图表等)
  const getPriceValue = (priceCNY) => {
    if (priceCNY === null || priceCNY === undefined) return 0
    const rate = RATES[currentCurrency.value]
    const unitLabel = t('car.price_unit')
    
    let val = Number(priceCNY) * rate
    if (unitLabel === 'k') {
      val = val * 10
    }
    return val
  }

  return {
    currentCurrency,
    currencySymbol, // 👈 导出符号
    setCurrency,
    formatPrice,
    getPriceValue
  }
}