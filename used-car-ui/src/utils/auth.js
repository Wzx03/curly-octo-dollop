const KEY_ACCOUNTS = 'uc_accounts'
const KEY_ACTIVE_ID = 'uc_active_id'
const KEY_TOKEN = 'token' // 👈 兼容旧代码

// 获取所有账号
export const getAccounts = () => {
  const str = localStorage.getItem(KEY_ACCOUNTS)
  return str ? JSON.parse(str) : []
}

// 获取当前活跃账号
export const getActiveAccount = () => {
  const accounts = getAccounts()
  const activeId = localStorage.getItem(KEY_ACTIVE_ID)
  if (!activeId) return null
  return accounts.find(acc => String(acc.id) === String(activeId)) || null
}

// 获取当前 Token
export const getToken = () => {
  // 优先从 localStorage 直接拿 (兼容旧逻辑)
  const directToken = localStorage.getItem(KEY_TOKEN)
  if (directToken) return directToken

  const active = getActiveAccount()
  return active ? active.token : null
}

// 添加/更新账号 (登录成功后调用)
export const addAccount = (token) => {
  try {
    // 解析 Token 获取用户信息
    const payload = JSON.parse(atob(token.split('.')[1]))
    const user = {
      id: payload.id,
      username: payload.username,
      role: payload.role,
      avatar: payload.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', // 默认头像
      token: token
    }

    const accounts = getAccounts()
    // 如果已存在，更新 Token；否则追加
    const index = accounts.findIndex(a => String(a.id) === String(user.id))
    if (index !== -1) {
      accounts[index] = user
    } else {
      accounts.push(user)
    }

    localStorage.setItem(KEY_ACCOUNTS, JSON.stringify(accounts))
    localStorage.setItem(KEY_ACTIVE_ID, user.id)
    
    // 👇 关键修复：同步写入 token 到 localStorage，供其他组件直接读取
    localStorage.setItem(KEY_TOKEN, token)
    
    return user
  } catch (e) {
    console.error('Token 解析失败', e)
  }
}

// 切换账号
export const switchAccount = (userId) => {
  const accounts = getAccounts()
  const target = accounts.find(a => String(a.id) === String(userId))
  
  if (target) {
    localStorage.setItem(KEY_ACTIVE_ID, userId)
    // 👇 同步更新 token
    localStorage.setItem(KEY_TOKEN, target.token)
    
    window.location.reload() // 简单粗暴：刷新页面以应用新 Token
  }
}

// 移除当前账号 (退出登录)
export const removeActiveAccount = () => {
  const activeId = localStorage.getItem(KEY_ACTIVE_ID)
  let accounts = getAccounts()
  accounts = accounts.filter(a => String(a.id) !== String(activeId))
  
  localStorage.setItem(KEY_ACCOUNTS, JSON.stringify(accounts))
  
  // 👇 移除 token
  localStorage.removeItem(KEY_TOKEN)
  
  if (accounts.length > 0) {
    // 如果还有其他账号，自动切到第一个
    const nextUser = accounts[0]
    localStorage.setItem(KEY_ACTIVE_ID, nextUser.id)
    localStorage.setItem(KEY_TOKEN, nextUser.token) // 更新 token
    window.location.reload()
  } else {
    // 没账号了，彻底退出
    localStorage.removeItem(KEY_ACTIVE_ID)
    window.location.href = '/login'
  }
}