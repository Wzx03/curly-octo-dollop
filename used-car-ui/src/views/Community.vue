<template>
  <div class="admin-panel-container">
<!--    &lt;!&ndash; 1. 顶部页头 &ndash;&gt;-->
<!--    <div class="panel-header">-->
<!--      <h1 class="panel-title">{{ $t('community.title') }}</h1>-->
<!--      <p class="panel-subtitle">分享您的爱车故事，发现更多精彩</p>-->
<!--    </div>-->

    <!-- 2. 核心卡片区域 -->
    <div class="panel-card community-panel-card">
      <div class="community-layout">
        <!-- 左侧：帖子列表 -->
        <div class="feed-column">
          <div class="feed-toolbar">
            <div class="toolbar-left">
              <span class="feed-count" v-if="list.length > 0">{{ list.length }} 篇帖子</span>
            </div>
            <el-button type="primary" icon="Plus" @click="openPostDialog">
              {{ $t('community.post_btn') }}
            </el-button>
          </div>

          <div class="feed-list" v-loading="loading">
            <div v-for="item in list" :key="item.id" class="feed-card">
              <!-- 卡片头部 -->
              <div class="card-header">
                <div class="author-info">
                  <el-avatar :size="40" :src="item.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  <div class="text-info">
                    <span class="username">{{ item.author }}</span>
                    <span class="time">{{ formatTime(item.createTime) }}</span>
                  </div>
                </div>
                <!-- 下拉菜单 -->
                <el-dropdown trigger="click" @command="handleCommand" v-if="isOwner(item) || isAdmin">
                  <el-icon class="more-icon"><More /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ action: 'edit', item: item }">{{ $t('common.edit') }}</el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'delete', item: item }" style="color: #f56c6c">{{ $t('common.delete') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <!-- 内容区 -->
              <div class="card-content">
                <h3 class="post-title">{{ item.title }}</h3>
                <p class="post-desc">{{ item.content }}</p>
                
                <!-- 图片 -->
                <div class="card-image-wrapper" v-if="item.cover" @click="viewDetail(item)">
                  <el-image :src="item.cover" fit="cover" class="main-img" loading="lazy" />
                </div>
              </div>

              <!-- 底部操作栏 -->
              <div class="card-footer">
                <div class="action-btn" @click="handleLike(item)" :class="{ active: item.isLiked }">
                  <el-icon><component :is="item.isLiked ? 'StarFilled' : 'Star'" /></el-icon>
                  <span>{{ item.likes || 0 }}</span>
                </div>
                <div class="action-btn" @click="viewDetail(item)">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ item.commentCount || 0 }}</span>
                </div>
                <div class="action-btn">
                  <el-icon><Share /></el-icon>
                  <span>分享</span>
                </div>
              </div>
            </div>
            
            <el-empty v-if="list.length === 0 && !loading" :description="$t('community.no_posts')" />
          </div>
        </div>

        <!-- 右侧：热门/推荐 (预留) -->
        <div class="side-column">
          <div class="side-card">
            <h3 class="side-title">热门话题</h3>
            <div class="topic-list">
              <div class="topic-item"># 新车发布</div>
              <div class="topic-item"># 驾驶技巧</div>
              <div class="topic-item"># 保养心得</div>
              <div class="topic-item"># 自驾游记</div>
            </div>
          </div>
          
          <div class="side-card">
            <h3 class="side-title">活跃用户</h3>
            <div class="user-grid">
              <el-avatar v-for="i in 6" :key="i" :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      width="900px" 
      class="admin-dialog detail-dialog"
      align-center
      destroy-on-close
    >
      <div class="detail-layout" v-if="currentArticle">
        <!-- 左侧大图 -->
        <div class="detail-left" v-if="currentArticle.cover">
          <img :src="currentArticle.cover" class="detail-img">
        </div>
        <!-- 如果没有图，让右侧占满 -->
        <div class="detail-right" :class="{ 'full-width': !currentArticle.cover }">
          <div class="detail-header">
            <div class="header-user">
              <el-avatar :size="32" :src="currentArticle.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <div class="header-info">
                <span class="header-username">{{ currentArticle.author }}</span>
                <span class="header-time">{{ formatTime(currentArticle.createTime) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-scroll-area">
            <!-- 楼主内容 -->
            <div class="article-body">
              <h2 class="article-title">{{ currentArticle.title }}</h2>
              <p class="article-text">{{ currentArticle.content }}</p>
            </div>
            
            <el-divider content-position="left">评论 ({{ comments.length }})</el-divider>
            
            <!-- 评论列表 -->
            <div class="comment-list">
              <div v-for="c in comments" :key="c.id" class="comment-item">
                <el-avatar :size="32" :src="c.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                <div class="comment-body">
                  <div class="comment-top">
                    <span class="username-inline">{{ c.nickname }}</span>
                    <span class="comment-time">{{ formatTime(c.createTime) }}</span>
                  </div>
                  <div class="comment-text">{{ c.content }}</div>
                </div>
              </div>
              <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发" :image-size="60" />
            </div>
          </div>

          <div class="detail-footer">
            <div class="input-area">
              <el-input 
                v-model="newComment" 
                :placeholder="$t('community.add_comment')" 
                @keyup.enter="submitComment"
                class="comment-input"
              >
                <template #append>
                  <el-button @click="submitComment" :disabled="!newComment.trim()">{{ $t('common.submit') }}</el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 发布/编辑弹窗 -->
    <el-dialog 
      v-model="showPostDialog" 
      :title="isEdit ? $t('common.edit') : $t('community.post_btn')" 
      width="600px" 
      class="admin-dialog"
      align-center
    >
      <el-form :model="postForm" label-position="top" class="compact-form">
        <el-form-item :label="$t('community.form_title')" required>
          <el-input v-model="postForm.title" :placeholder="$t('community.form_title')" />
        </el-form-item>
        <el-form-item :label="$t('community.form_photo')">
          <el-upload
            class="cover-uploader"
            action="#"
            :show-file-list="false"
            :http-request="uploadCover"
          >
            <img v-if="postForm.cover" :src="postForm.cover" class="cover-preview" />
            <div v-else class="uploader-placeholder">
              <el-icon :size="28" color="#8c939d"><Plus /></el-icon>
              <span>{{ $t('community.upload_hint') }}</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item :label="$t('community.form_content')" required>
          <el-input 
            v-model="postForm.content" 
            type="textarea" 
            :rows="6" 
            :placeholder="$t('community.form_content')" 
            resize="none"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPostDialog = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitPost" :loading="submitting">{{ $t('common.submit') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { getToken, getActiveAccount } from '../utils/auth'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Star, StarFilled, ChatDotRound, Share, More } from '@element-plus/icons-vue'

const { t } = useI18n()
const list = ref([])
const loading = ref(false)
const submitting = ref(false)

// 详情相关
const dialogVisible = ref(false)
const currentArticle = ref(null)
const comments = ref([])
const newComment = ref('')

// 发布/编辑相关
const showPostDialog = ref(false)
const isEdit = ref(false)
const postForm = ref({ id: null, title: '', content: '', cover: '' })

// 权限判断
const currentUser = computed(() => getActiveAccount())
const isAdmin = computed(() => currentUser.value && currentUser.value.role === 0)
const isOwner = (item) => {
  return currentUser.value && item.createBy === currentUser.value.id
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/content/article/list', {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.records.map(item => ({
        ...item,
        likes: item.likes || 0,
        commentCount: item.commentCount || 0,
        isLiked: false
      }))
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openPostDialog = () => {
  isEdit.value = false
  postForm.value = { title: '', content: '', cover: '' }
  showPostDialog.value = true
}

const viewDetail = async (item) => {
  currentArticle.value = item
  dialogVisible.value = true
  loadComments(item.id)
}

const loadComments = async (articleId) => {
  try {
    const res = await axios.get(`/api/content/comment/list?articleId=${articleId}`, {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      comments.value = res.data.data
    }
  } catch (e) {}
}

const handleLike = (item) => {
  item.isLiked = !item.isLiked
  item.likes += item.isLiked ? 1 : -1
  ElMessage.success(item.isLiked ? 'Liked' : 'Unliked')
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  try {
    const res = await axios.post('/api/content/comment/publish', {
      articleId: currentArticle.value.id,
      content: newComment.value
    }, { headers: { token: getToken() } })
    
    if (res.data.code === 200) {
      ElMessage.success(t('common.success'))
      newComment.value = ''
      loadComments(currentArticle.value.id)
      const idx = list.value.findIndex(i => i.id === currentArticle.value.id)
      if (idx !== -1) list.value[idx].commentCount++
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

const uploadCover = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await axios.post('/api/common/upload', formData, {
      headers: { token: getToken(), 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200) {
      postForm.value.cover = res.data.data
      ElMessage.success(t('common.success'))
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  }
}

const submitPost = async () => {
  if (!postForm.value.title || !postForm.value.content) return ElMessage.warning(t('community.form_title') + ' & ' + t('community.form_content'))
  
  submitting.value = true
  try {
    const url = isEdit.value ? '/api/content/article/update' : '/api/content/article/publish'
    const res = await axios.post(url, postForm.value, {
      headers: { token: getToken() }
    })
    if (res.data.code === 200) {
      ElMessage.success(t('common.success'))
      showPostDialog.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error(t('common.fail'))
  } finally {
    submitting.value = false
  }
}

const handleCommand = (payload) => {
  const { action, item } = payload
  if (action === 'edit') {
    isEdit.value = true
    postForm.value = { ...item }
    showPostDialog.value = true
  } else if (action === 'delete') {
    ElMessageBox.confirm(t('common.confirm'), t('common.tips'), { type: 'warning' })
      .then(async () => {
        try {
          const res = await axios.post(`/api/content/article/delete/${item.id}`, {}, { headers: { token: getToken() } })
          if (res.data.code === 200) {
            ElMessage.success(t('common.success'))
            loadData()
            if (dialogVisible.value && currentArticle.value && currentArticle.value.id === item.id) {
              dialogVisible.value = false
            }
          }
        } catch (e) {
          ElMessage.error(t('common.fail'))
        }
      })
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 容器 */
.admin-panel-container {
  max-width: 1440px; /* 宽屏适配 */
  margin: 0 auto;
  padding: 24px 40px;
  background-color: #f7f9fc; /* 浅灰背景 */
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  display: flex;
  flex-direction: column;
}

/* 页头 */
.panel-header {
  margin-bottom: 24px;
}
.panel-title {
  font-size: 24px;
  font-weight: 700;
  color: #111;
  margin: 0 0 8px 0;
}
.panel-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 核心卡片 */
.panel-card {
  background: transparent; /* 社区页不需要白色大背景，因为有两列布局 */
  box-shadow: none;
  border: none;
  overflow: visible;
  flex: 1;
}

.community-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 左侧：帖子列表 */
.feed-column {
  flex: 1;
  min-width: 0;
}

.feed-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: #fff;
  padding: 16px 24px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}
.feed-count {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feed-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.feed-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.card-header {
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f5f5f5;
}
.author-info { display: flex; align-items: center; gap: 12px; }
.text-info { display: flex; flex-direction: column; }
.username { font-weight: 600; font-size: 14px; color: #222; }
.time { font-size: 12px; color: #999; }
.more-icon { cursor: pointer; transform: rotate(90deg); color: #666; }

.card-content {
  padding: 16px 24px;
}
.post-title {
  font-size: 18px;
  font-weight: 700;
  color: #222;
  margin: 0 0 8px 0;
}
.post-desc {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-image-wrapper {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #f5f5f5;
}
.main-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.card-image-wrapper:hover .main-img { transform: scale(1.02); }

.card-footer {
  padding: 12px 24px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  gap: 24px;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}
.action-btn:hover { color: #0052cc; }
.action-btn.active { color: #FF385C; }

/* 右侧：侧边栏 */
.side-column {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  padding: 20px;
}
.side-title {
  font-size: 16px;
  font-weight: 700;
  color: #222;
  margin: 0 0 16px 0;
}

.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic-item {
  background: #f0f7ff;
  color: #0052cc;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}
.topic-item:hover { background: #e0efff; }

.user-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

/* 详情弹窗 */
.admin-dialog { border-radius: 8px; }
.detail-layout { display: flex; height: 600px; }
.detail-left { width: 60%; background: #000; display: flex; align-items: center; justify-content: center; }
.detail-img { max-width: 100%; max-height: 100%; object-fit: contain; }
.detail-right { width: 40%; display: flex; flex-direction: column; background: #fff; border-left: 1px solid #ebebeb; }
.detail-right.full-width { width: 100%; border-left: none; }

.detail-header {
  height: 64px; border-bottom: 1px solid #ebebeb; display: flex; align-items: center; padding: 0 24px; flex-shrink: 0;
}
.header-user { display: flex; align-items: center; gap: 12px; }
.header-info { display: flex; flex-direction: column; }
.header-username { font-weight: 600; font-size: 14px; color: #222; }
.header-time { font-size: 12px; color: #999; }

.detail-scroll-area { flex: 1; overflow-y: auto; padding: 24px; }
.article-body { margin-bottom: 24px; }
.article-title { font-size: 20px; font-weight: 700; margin: 0 0 12px 0; color: #222; }
.article-text { font-size: 15px; line-height: 1.6; color: #333; white-space: pre-wrap; }

.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item { display: flex; gap: 12px; }
.comment-body { flex: 1; }
.comment-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.username-inline { font-weight: 600; font-size: 13px; color: #222; }
.comment-time { font-size: 12px; color: #999; }
.comment-text { font-size: 14px; color: #444; line-height: 1.4; }

.detail-footer { padding: 16px 24px; border-top: 1px solid #ebebeb; }
.comment-input :deep(.el-input__wrapper) { box-shadow: 0 0 0 1px #ddd inset; }
.comment-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #0052cc inset !important; }

/* 上传 */
.cover-uploader {
  width: 100%; height: 200px; border: 1px dashed #dcdfe6; border-radius: 8px;
  display: flex; justify-content: center; align-items: center; background: #f5f7fa; cursor: pointer; overflow: hidden;
  transition: border-color 0.2s;
}
.cover-uploader:hover { border-color: #0052cc; }
.cover-preview { width: 100%; height: 100%; object-fit: cover; }
.uploader-placeholder { display: flex; flex-direction: column; align-items: center; color: #909399; }

/* 弹窗底部 */
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
</style>