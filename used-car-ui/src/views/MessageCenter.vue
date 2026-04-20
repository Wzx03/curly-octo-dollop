<template>
  <div class="admin-panel-container" :class="{ 'embedded-mode': embedded }">
    <!-- 1. 顶部页头 (嵌入模式下隐藏) -->
    <div class="panel-header" v-if="!embedded">
      <h1 class="panel-title">{{ $t('message.title') }}</h1>
      <p class="panel-subtitle">与卖家实时沟通，处理交易相关事宜</p>
    </div>

    <!-- 2. 核心卡片区域 -->
    <div class="panel-card im-panel-card" :class="{ 'embedded-card': embedded }">
      <!-- 3. 内容区域 -->
      <div class="content-container">
        <div class="im-layout">
          <!-- 左侧列表栏 -->
          <div class="sidebar">
            <!-- 搜索框区域 -->
            <div class="search-container">
              <el-input
                  v-model="searchText"
                  placeholder="搜索对话..."
                  class="compact-input"
                  clearable
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>

            <!-- 会话列表 -->
            <div class="session-list">
              <div
                  v-for="session in filteredSessions"
                  :key="session.userId"
                  class="session-item"
                  :class="{ active: currentSessionId === session.userId }"
                  @click="selectSession(session)"
              >
                <!-- 头像及未读红点 -->
                <div class="avatar-box">
                  <el-avatar :size="40" :src="session.avatar || defaultAvatar" />
                  <span v-if="session.unread > 0" class="unread-badge"></span>
                </div>
                <!-- 会话信息 -->
                <div class="content-box">
                  <div class="row-primary">
                    <span class="name" :class="{'text-purple-600': session.userId === AI_USER_ID}">{{ session.nickname }}</span>
                    <span class="time">{{ formatTime(session.lastTime) }}</span>
                  </div>
                  <div class="row-secondary">
                    <span class="preview">{{ getMsgPreview(session) }}</span>
                  </div>
                </div>
              </div>

              <div class="empty-state" v-if="filteredSessions.length === 0">
                <el-empty description="暂无消息" :image-size="80">
                  <template #image>
                    <el-icon :size="48" color="#ddd"><ChatDotRound /></el-icon>
                  </template>
                </el-empty>
              </div>
            </div>
          </div>

          <!-- 右侧聊天主区域 -->
          <div class="chat-area">
            <template v-if="currentSessionId !== null">
              <!-- 顶部导航 -->
              <div class="chat-header">
                <div class="header-profile">
                  <span class="profile-name" :class="{'text-purple-600': currentSessionId === AI_USER_ID}">
                    {{ currentSessionName }}
                  </span>
                  <div class="profile-status">
                    <span class="status-indicator"></span>
                    在线
                  </div>
                </div>

                <!-- 关联商品卡片 (如果不是 AI 则显示商品) -->
                <div class="product-context" v-if="currentCar && currentSessionId !== AI_USER_ID" @click="toCarDetail">
                  <div class="product-thumb-container">
                    <img :src="currentCar.image" class="product-thumb" />
                  </div>
                  <div class="product-info">
                    <div class="product-title">{{ currentCar.brand }} {{ currentCar.model }}</div>
                    <div class="product-price">¥{{ currentCar.price }}万</div>
                  </div>
                  <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                </div>
              </div>

              <!-- 消息记录区域 -->
              <div class="messages-container" ref="chatContentRef">
                <div
                    v-for="(msg, index) in chatHistory"
                    :key="index"
                    class="message-wrapper"
                    :class="{ 'mine': isMyMsg(msg) }"
                >
                  <!-- 对方头像 -->
                  <div class="avatar-wrapper" v-if="!isMyMsg(msg)">
                    <el-avatar :size="36" :src="currentAvatar" />
                  </div>

                  <!-- 消息内容 -->
                  <div class="message-content">
                    <div class="bubble" :class="getBubbleClass(msg)">
                      <!-- 🚨 兼容普通的文本消息(1) 和 后端 AI 生成的消息(10) -->
                      <div v-if="!msg.type || msg.type === 1 || msg.type === 10" class="text-message">
                        {{ msg.content }}
                      </div>

                      <!-- 图片消息 -->
                      <div v-else-if="msg.type === 2" class="media-message">
                        <el-image
                            :src="msg.content"
                            :preview-src-list="[msg.content]"
                            class="media-img"
                            fit="cover"
                        />
                      </div>

                      <!-- 视频消息 -->
                      <div v-else-if="msg.type === 3" class="media-message">
                        <video :src="msg.content" controls class="media-video"></video>
                      </div>

                      <!-- 文件消息 -->
                      <div v-else-if="msg.type === 4" class="file-message">
                        <a :href="msg.content" target="_blank" class="file-link">
                          <div class="file-icon">
                            <el-icon><Document /></el-icon>
                          </div>
                          <span class="file-name">下载文件</span>
                        </a>
                      </div>
                    </div>
                    <div class="message-time">{{ formatMsgTime(msg.createTime) }}</div>
                  </div>

                  <!-- 我的头像 -->
                  <div class="avatar-wrapper" v-if="isMyMsg(msg)">
                    <el-avatar :size="36" :src="myAvatar" />
                  </div>
                </div>

                <!-- 🤖 AI 正在思考中的骨架屏动画 -->
                <div class="message-wrapper" v-if="isAiThinking && currentSessionId === AI_USER_ID">
                  <div class="avatar-wrapper">
                    <el-avatar :size="36" :src="currentAvatar" />
                  </div>
                  <div class="message-content">
                    <div class="bubble text-bubble" style="background: #f7f7f7; display: flex; align-items: center; height: 42px;">
                      <div class="dot-typing"></div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 底部输入区 -->
              <div class="input-area">
                <!-- 工具栏 -->
                <div class="input-toolbar" v-if="currentSessionId !== AI_USER_ID">
                  <el-tooltip content="发送图片" placement="top">
                    <button class="tool-btn" @click="triggerUpload('image')">
                      <el-icon><Picture /></el-icon>
                    </button>
                  </el-tooltip>
                  <el-tooltip content="发送视频" placement="top">
                    <button class="tool-btn" @click="triggerUpload('video')">
                      <el-icon><VideoCamera /></el-icon>
                    </button>
                  </el-tooltip>
                  <el-tooltip content="发送文件" placement="top">
                    <button class="tool-btn" @click="triggerUpload('file')">
                      <el-icon><Folder /></el-icon>
                    </button>
                  </el-tooltip>
                </div>
                <!-- AI 专属提示 -->
                <div class="input-toolbar" v-else>
                  <el-tag type="primary" effect="plain" size="small" style="border:none; background: transparent; padding: 0;">
                    ✨ AI 智能顾问当前仅支持文本交互
                  </el-tag>
                </div>

                <!-- 输入框区域 -->
                <div class="input-wrapper" :class="{ 'ai-input-focus': currentSessionId === AI_USER_ID }">
                  <textarea
                      v-model="inputMsg"
                      class="message-input"
                      :placeholder="currentSessionId === AI_USER_ID ? '向 AI 提问有关购车流程、平台规则等...' : '输入消息，按 Enter 发送...'"
                      @keydown.enter.prevent="sendMsg(currentSessionId === AI_USER_ID ? 10 : 1, inputMsg)"
                      :disabled="isAiThinking && currentSessionId === AI_USER_ID"
                  ></textarea>
                  <button
                      class="send-btn"
                      :class="{ 'bg-purple-600': currentSessionId === AI_USER_ID }"
                      @click="sendMsg(currentSessionId === AI_USER_ID ? 10 : 1, inputMsg)"
                      :disabled="!inputMsg.trim() || (isAiThinking && currentSessionId === AI_USER_ID)"
                  >
                    <el-icon><Position /></el-icon>
                  </button>
                </div>
              </div>
            </template>

            <!-- 未选择会话时的空状态 -->
            <div class="empty-chat-state" v-else>
              <div class="empty-content">
                <div class="empty-icon">
                  <el-icon :size="64" color="#ddd"><ChatDotRound /></el-icon>
                </div>
                <h3 class="empty-title">开始沟通</h3>
                <p class="empty-desc">选择左侧联系人，或点击上方寻找 AI 智能客服</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 隐藏的文件上传 Input -->
    <input type="file" ref="fileInputImage" style="display: none" accept="image/*" @change="(e) => handleUpload(e, 2)" />
    <input type="file" ref="fileInputVideo" style="display: none" accept="video/*" @change="(e) => handleUpload(e, 3)" />
    <input type="file" ref="fileInputFile" style="display: none" @change="(e) => handleUpload(e, 4)" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { Search, Picture, Folder, VideoCamera, ChatDotRound, Document, Position, ArrowRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  embedded: {
    type: Boolean,
    default: false
  }
})

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

// --- 状态变量定义 ---
const sessionList = ref([])
const chatHistory = ref([])
const currentSessionId = ref(null)
const currentSessionName = ref('')
const currentAvatar = ref('')
const myAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const currentCar = ref(null)
const inputMsg = ref('')
const searchText = ref('')
const chatContentRef = ref(null)
const myId = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const socketConnected = ref(false)
let socket = null

// --- 🤖 AI 客服核心配置 ---
// 与后端 AiChatServiceImpl 中的 SYSTEM_AI_ID 保持一致
const AI_USER_ID = 0
const isAiThinking = ref(false)
let aiTimeoutTimer = null

const fileInputImage = ref(null)
const fileInputVideo = ref(null)
const fileInputFile = ref(null)

// --- 计算属性 ---
const filteredSessions = computed(() => {
  if (!searchText.value) return sessionList.value
  return sessionList.value.filter(s => s.nickname.includes(searchText.value))
})

// --- 工具方法 ---
const formatTime = (time) => {
  if (!time) return ''
  return time.substring(11, 16)
}

const formatMsgTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const getMsgPreview = (session) => {
  if (session.lastMsgType === 2) return '[图片]'
  if (session.lastMsgType === 3) return '[视频]'
  if (session.lastMsgType === 4) return '[文件]'
  // 🚨 兼容后端 AI 消息的 type 10
  return session.lastMsg || '点击开始对话'
}

const isMyMsg = (msg) => {
  // 🚨 使用 ?? 运算符，防止 msg.senderId 为 0 时被当做 falsy 忽略
  const sender = msg.senderId ?? msg.sender_id
  return Number(sender) === Number(myId.value)
}

const getBubbleClass = (msg) => {
  if (msg.type === 2 || msg.type === 3) return 'media-bubble'
  if (msg.type === 4) return 'file-bubble'
  // type 1 和 type 10 都会返回基础气泡样式
  return 'text-bubble'
}

// --- 生命周期钩子 ---
onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      myId.value = Number(payload.id)
    } catch (e) {}

    initWebSocket(token)
    loadSessions()

    if (route.query.carId) {
      loadCarInfo(route.query.carId)
    }

    if (route.query.targetId) {
      const targetId = Number(route.query.targetId)
      setTimeout(() => {
        const exist = sessionList.value.find(s => s.userId === targetId)
        if (exist) {
          selectSession(exist)
        } else {
          selectSession({ userId: targetId, nickname: targetId === AI_USER_ID ? '🤖 AI 智能客服' : t('message.seller'), avatar: '' })
        }
      }, 500)
    }
  }
})

onUnmounted(() => {
  if (socket) socket.close()
  if (aiTimeoutTimer) clearTimeout(aiTimeoutTimer)
})

// --- 业务逻辑方法 ---
const triggerUpload = (type) => {
  if (type === 'image') fileInputImage.value.click()
  else if (type === 'video') fileInputVideo.value.click()
  else if (type === 'file') fileInputFile.value.click()
}

const handleUpload = async (event, type) => {
  const file = event.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await axios.post('/api/common/upload', formData, {
      headers: { token: localStorage.getItem('token'), 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200) sendMsg(type, res.data.data)
    else ElMessage.error(res.data.message)
  } catch (e) { ElMessage.error(t('common.fail')) }
  finally { event.target.value = '' }
}

const loadCarInfo = async (carId) => {
  try {
    const res = await axios.get(`/api/car/${carId}`, { headers: { token: localStorage.getItem('token') } })
    if (res.data.code === 200) currentCar.value = res.data.data
  } catch (e) {}
}

const toCarDetail = () => {
  if (currentCar.value) router.push(`/car/${currentCar.value.id}`)
}

// 加载会话列表并置顶注入 AI
const loadSessions = async () => {
  try {
    const res = await axios.get('/api/chat/sessions', { headers: { token: localStorage.getItem('token') } })
    if (res.data.code === 200) {
      let list = res.data.data || []

      // 寻找列表中的 AI 会话
      const aiSession = list.find(s => s.userId === AI_USER_ID)
      const aiAvatar = 'https://api.dicebear.com/7.x/bottts/svg?seed=AutoBot&backgroundColor=e6e6fa'

      if (!aiSession) {
        list.unshift({
          userId: AI_USER_ID,
          nickname: '🤖 AI 智能客服',
          avatar: aiAvatar,
          lastMsg: '24小时为您解答平台与购车问题',
          lastTime: new Date().toISOString(),
          unread: 0
        })
      } else {
        aiSession.nickname = '🤖 AI 智能客服'
        aiSession.avatar = aiAvatar
        list = list.filter(s => s.userId !== AI_USER_ID)
        list.unshift(aiSession) // 强制置顶
      }

      sessionList.value = list
    }
  } catch (e) {}
}

const selectSession = async (session) => {
  currentSessionId.value = session.userId
  currentSessionName.value = session.nickname
  currentAvatar.value = session.avatar || defaultAvatar
  session.unread = 0

  try {
    await axios.post(`/api/chat/read?targetId=${session.userId}`, {}, { headers: { token: localStorage.getItem('token') } })
    const res = await axios.get(`/api/chat/history?targetId=${session.userId}`, { headers: { token: localStorage.getItem('token') } })
    if (res.data.code === 200) {
      chatHistory.value = res.data.data
      scrollToBottom()
    }
  } catch (e) {}
}

const sendMsg = (type = 1, content = '') => {
  if (!content && !inputMsg.value.trim()) return
  const finalContent = content || inputMsg.value
  const msg = {
    receiverId: currentSessionId.value,
    content: finalContent,
    carId: currentCar.value ? currentCar.value.id : null,
    type: type
  }

  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(msg))
    // 前端本地直接推入记录（以便立刻显示）
    chatHistory.value.push({
      senderId: myId.value,
      content: finalContent,
      type: type,
      createTime: new Date().toISOString()
    })

    if (type === 1 || type === 10) inputMsg.value = ''
    scrollToBottom()

    // 如果发送目标是 AI，立刻进入思考转圈动画
    if (currentSessionId.value === AI_USER_ID) {
      isAiThinking.value = true
      scrollToBottom()

      clearTimeout(aiTimeoutTimer)
      aiTimeoutTimer = setTimeout(() => {
        if (isAiThinking.value) {
          isAiThinking.value = false
          ElMessage.error('AI 响应超时，请检查后端网络状态')
        }
      }, 45000) // 延长兜底超时时间为 45 秒
    }
  } else {
    ElMessage.error(t('message.connection_lost'))
  }
}

const initWebSocket = (token) => {
  socket = new WebSocket(`ws://localhost:8080/ws/chat/${token}`)
  socket.onopen = () => { socketConnected.value = true }

  socket.onmessage = (event) => {
    const data = JSON.parse(event.data)

    // 捕获异常消息打断转圈
    if (data.type === 'error') {
      ElMessage.error(data.message || '后端处理消息失败')
      isAiThinking.value = false
      if (aiTimeoutTimer) clearTimeout(aiTimeoutTimer)
      return
    }

    if (data.type === 'chat') {
      const msg = data.data

      // 🚨 核心防御点：0 是 falsy 值，坚决不能用 ||，必须用 ?? 严格判断 null/undefined
      const msgSenderId = Number(msg.senderId ?? msg.sender_id)

      // 如果消息属于当前打开的聊天框
      if (currentSessionId.value === msgSenderId) {
        chatHistory.value.push(msg)

        // 收到 AI 消息后立刻结束转圈
        if (msgSenderId === AI_USER_ID) {
          isAiThinking.value = false
          if (aiTimeoutTimer) clearTimeout(aiTimeoutTimer)
        }

        scrollToBottom()
        axios.post(`/api/chat/read?targetId=${msgSenderId}`, {}, { headers: { token: localStorage.getItem('token') } })
      } else {
        ElMessage.success(t('message.new_msg'))
        loadSessions()
      }
    }
  }
  socket.onclose = () => { socketConnected.value = false }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContentRef.value) chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
  })
}
</script>

<style scoped>
/* =========== 核心样式 =========== */
.admin-panel-container { max-width: 1440px; margin: 0 auto; padding: 24px 40px; background-color: #f7f9fc; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; display: flex; flex-direction: column; }
.admin-panel-container.embedded-mode { padding: 0; min-height: auto; height: 100%; background: transparent; }
.panel-header { margin-bottom: 24px; }
.panel-title { font-size: 24px; font-weight: 700; color: #111; margin: 0 0 8px 0; }
.panel-subtitle { font-size: 14px; color: #666; margin: 0; }
.panel-card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); border: 1px solid #e0e0e0; padding: 0; overflow: hidden; min-height: 600px; display: flex; flex-direction: column; }
.im-panel-card { height: calc(100vh - 160px); }
.im-panel-card.embedded-card { height: 100%; min-height: auto; border: none; box-shadow: none; border-radius: 0; }
.content-container { padding: 0; flex: 1; display: flex; flex-direction: column; min-height: 0; }
.im-layout { display: flex; height: 100%; width: 100%; background: #fff; }

/* 侧边栏 */
.sidebar { width: 300px; border-right: 1px solid #ebebeb; display: flex; flex-direction: column; background: #fff; flex-shrink: 0; }
.search-container { padding: 16px; border-bottom: 1px solid #f0f0f0; }
.compact-input :deep(.el-input__wrapper) { box-shadow: 0 0 0 1px #ddd inset; border-radius: 4px; height: 32px; font-size: 13px; background: #fff; }
.compact-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #0052cc inset !important; }
.session-list { flex: 1; overflow-y: auto; padding: 8px; }
.session-item { display: flex; padding: 12px 16px; border-radius: 8px; cursor: pointer; transition: all 0.2s; margin-bottom: 4px; align-items: flex-start; }
.session-item:hover { background: #f7f7f7; transform: translateX(2px); }
.session-item.active { background: #f0f7ff; border-left: 3px solid #0052cc; }
.avatar-box { position: relative; margin-right: 12px; flex-shrink: 0; }
.unread-badge { position: absolute; top: -2px; right: -2px; width: 12px; height: 12px; background: #ff385c; border-radius: 50%; border: 2px solid #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.content-box { flex: 1; display: flex; flex-direction: column; justify-content: center; min-width: 0; }
.row-primary { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.name { font-weight: 600; font-size: 14px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 160px; }
.time { font-size: 11px; color: #999; flex-shrink: 0; }
.row-secondary { display: flex; }
.preview { font-size: 12px; color: #717171; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 200px; }
.empty-state { display: flex; align-items: center; justify-content: center; height: 100%; padding: 20px; }

/* 聊天区域 */
.chat-area { flex: 1; display: flex; flex-direction: column; position: relative; min-width: 0; background: #fafafa; }
.chat-header { height: 72px; border-bottom: 1px solid #ebebeb; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; flex-shrink: 0; background: #fff; }
.header-profile { display: flex; flex-direction: column; }
.profile-name { font-size: 16px; font-weight: 700; color: #222; margin-bottom: 4px; }
.profile-status { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #16a34a; }
.status-indicator { width: 8px; height: 8px; background: #16a34a; border-radius: 50%; animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }

/* 商品卡片 */
.product-context { display: flex; align-items: center; gap: 12px; padding: 8px 16px; border: 1px solid #ebebeb; border-radius: 8px; cursor: pointer; transition: all 0.2s; background: #f9fafb; max-width: 280px; }
.product-context:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); border-color: #0052cc; transform: translateY(-1px); }
.product-thumb-container { width: 40px; height: 40px; border-radius: 4px; overflow: hidden; background: #f5f5f5; flex-shrink: 0; }
.product-thumb { width: 100%; height: 100%; object-fit: cover; }
.product-info { display: flex; flex-direction: column; flex: 1; }
.product-title { font-size: 13px; font-weight: 600; color: #222; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-price { font-size: 13px; font-weight: 700; color: #d93025; }
.arrow-icon { color: #999; font-size: 16px; flex-shrink: 0; }

/* 聊天气泡区 */
.messages-container { flex: 1; overflow-y: auto; padding: 24px; display: flex; flex-direction: column; gap: 20px; background: #fafafa; }
.message-wrapper { display: flex; align-items: flex-start; gap: 12px; max-width: 80%; }
.message-wrapper.mine { align-self: flex-end; }
.avatar-wrapper { flex-shrink: 0; }
.message-content { display: flex; flex-direction: column; gap: 4px; max-width: calc(100% - 48px); }
.message-wrapper.mine .message-content { align-items: flex-end; }
.bubble { padding: 12px 16px; border-radius: 18px; font-size: 14px; line-height: 1.5; position: relative; word-wrap: break-word; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.message-wrapper:not(.mine) .bubble { background: #fff; color: #222; border-bottom-left-radius: 4px; border: 1px solid #ebebeb; }
.message-wrapper.mine .bubble { background: #0052cc; color: #fff; border-bottom-right-radius: 4px; }
.text-bubble { min-width: 60px; }
.media-bubble { padding: 0; background: transparent !important; border: none !important; box-shadow: none; }
.file-bubble { padding: 0; background: transparent !important; border: none !important; box-shadow: none; }
.text-message { white-space: pre-wrap; }
.media-message { border-radius: 12px; overflow: hidden; }
.media-img { max-width: 300px; max-height: 300px; display: block; border-radius: 12px; }
.media-video { max-width: 300px; max-height: 300px; border-radius: 12px; }
.file-message { background: #fff; border-radius: 12px; border: 1px solid #ebebeb; }
.file-link { display: flex; align-items: center; gap: 12px; padding: 12px 16px; text-decoration: none; color: inherit; min-width: 120px; }
.file-icon { width: 36px; height: 36px; background: #f0f7ff; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #0052cc; }
.message-wrapper.mine .file-icon { background: rgba(255,255,255,0.2); color: #fff; }
.file-name { font-size: 14px; font-weight: 500; }
.message-time { font-size: 11px; color: #999; margin: 0 8px; }
.message-wrapper.mine .message-time { text-align: right; }

/* 输入区 */
.input-area { padding: 16px 24px 100px; border-top: 1px solid #ebebeb; background: #fff; }
.input-toolbar { display: flex; gap: 16px; margin-bottom: 12px; }
.tool-btn { width: 36px; height: 36px; border-radius: 50%; background: #f7f7f7; border: 1px solid #ebebeb; cursor: pointer; color: #717171; font-size: 18px; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.tool-btn:hover { background: #0052cc; color: #fff; border-color: #0052cc; transform: translateY(-1px); }
.input-wrapper { display: flex; align-items: flex-end; background: #f7f7f7; border-radius: 24px; padding: 4px 4px 4px 20px; border: 1px solid transparent; transition: all 0.2s; }
.input-wrapper:focus-within { border-color: #0052cc; background: #fff; box-shadow: 0 0 0 2px rgba(0,82,204,0.1); }
.input-wrapper.ai-input-focus:focus-within { border-color: #9333ea; box-shadow: 0 0 0 2px rgba(147,51,234,0.1); }
.message-input { flex: 1; background: transparent; border: none; resize: none; height: 24px; max-height: 120px; padding: 8px 0; font-size: 14px; outline: none; font-family: inherit; color: #222; }
.send-btn { width: 36px; height: 36px; border-radius: 50%; background: #0052cc; color: #fff; border: none; display: flex; align-items: center; justify-content: center; cursor: pointer; margin-left: 8px; transition: all 0.2s; }
.send-btn:hover { transform: scale(1.05); }
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

/* 缺省页 */
.empty-chat-state { flex: 1; display: flex; align-items: center; justify-content: center; background: #fafafa; }
.empty-content { text-align: center; color: #717171; max-width: 320px; }
.empty-icon { margin-bottom: 24px; }
.empty-title { font-size: 20px; font-weight: 600; color: #222; margin: 0 0 12px 0; }
.empty-desc { font-size: 14px; color: #666; margin: 0; line-height: 1.5; }

/* 🤖 AI 动画 */
.dot-typing { position: relative; width: 6px; height: 6px; border-radius: 5px; background-color: #999; animation: dotTyping 1.5s infinite linear; margin: 0 12px; }
@keyframes dotTyping {
  0% { box-shadow: -10px 0 0 0 #999, 0 0 0 0 #999, 10px 0 0 0 #999; }
  33% { box-shadow: -10px 0 0 0 rgba(153,153,153,0.3), 0 0 0 0 #999, 10px 0 0 0 #999; }
  66% { box-shadow: -10px 0 0 0 #999, 0 0 0 0 rgba(153,153,153,0.3), 10px 0 0 0 #999; }
  100% { box-shadow: -10px 0 0 0 #999, 0 0 0 0 #999, 10px 0 0 0 rgba(153,153,153,0.3); }
}
.text-purple-600 { color: #9333ea !important; }
.bg-purple-600 { background-color: #9333ea !important; }
</style>