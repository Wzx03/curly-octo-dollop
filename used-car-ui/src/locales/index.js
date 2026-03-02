import { createI18n } from 'vue-i18n'
import zhCN from './lang/zh-CN.json'

// 1. 定义日期格式
const datetimeFormats = {
  'zh-CN': {
    short: {
      year: 'numeric', month: 'short', day: 'numeric'
    },
    long: {
      year: 'numeric', month: 'short', day: 'numeric',
      weekday: 'short', hour: 'numeric', minute: 'numeric'
    }
  },
  'en-US': {
    short: {
      year: 'numeric', month: 'short', day: 'numeric'
    },
    long: {
      year: 'numeric', month: 'short', day: 'numeric',
      weekday: 'short', hour: 'numeric', minute: 'numeric'
    }
  }
}

// 2. 定义数字格式 (货币)
const numberFormats = {
  'zh-CN': {
    currency: {
      style: 'currency', currency: 'CNY', minimumFractionDigits: 0
    },
    decimal: {
      style: 'decimal', minimumFractionDigits: 1, maximumFractionDigits: 1
    }
  },
  'en-US': {
    currency: {
      style: 'currency', currency: 'USD', minimumFractionDigits: 0
    },
    decimal: {
      style: 'decimal', minimumFractionDigits: 1, maximumFractionDigits: 1
    }
  }
}

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('lang') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  globalInjection: true,
  messages: {
    'zh-CN': zhCN
  },
  datetimeFormats, // 注入日期格式
  numberFormats    // 注入数字格式
})

const loadedLanguages = ['zh-CN']

export function setI18nLanguage(lang) {
  i18n.global.locale.value = lang
  document.querySelector('html').setAttribute('lang', lang)
  localStorage.setItem('lang', lang)
  return lang
}

export async function loadLanguageAsync(lang) {
  if (i18n.global.locale.value === lang) {
    return Promise.resolve(setI18nLanguage(lang))
  }
  if (loadedLanguages.includes(lang)) {
    return Promise.resolve(setI18nLanguage(lang))
  }
  const messages = await import(`./lang/${lang}.json`)
  i18n.global.setLocaleMessage(lang, messages.default)
  loadedLanguages.push(lang)
  return setI18nLanguage(lang)
}

export default i18n