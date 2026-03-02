<template>
  <el-dialog
    v-model="dialogVisible"
    width="560px"
    :show-close="false"
    class="airbnb-dialog-base"
    align-center
    destroy-on-close
  >
    <template #header>
      <div class="dialog-header">
        <div class="close-btn" @click="handleClose">
          <el-icon><Close /></el-icon>
        </div>
        <div class="dialog-title">发起投诉</div>
        <div class="header-placeholder"></div>
      </div>
    </template>

    <div class="dialog-body">
      <el-form :model="form" label-position="top" class="custom-form">
        <el-form-item label="投诉对象">
          <div class="target-info">
            <span class="target-tag">{{ targetTypeName }}</span>
            <span class="target-id">ID: {{ targetId }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="投诉原因" required>
          <el-select v-model="form.category" placeholder="请选择原因" class="w-full">
            <el-option label="虚假发货/车况不符" value="虚假发货" />
            <el-option label="言语辱骂/态度恶劣" value="言语辱骂" />
            <el-option label="欺诈行为" value="欺诈行为" />
            <el-option label="其他问题" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="详细描述" required>
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述您遇到的问题，以便我们快速处理..." 
            resize="none"
            class="airbnb-textarea"
          />
        </el-form-item>

        <el-form-item label="图片凭证">
          <div class="upload-container">
            <div v-for="(img, index) in imageList" :key="index" class="img-preview">
              <img :src="img" />
              <div class="del-mask" @click="removeImage(index)">
                <el-icon><Delete /></el-icon>
              </div>
            </div>
            
            <div class="upload-trigger" @click="triggerUpload" v-if="imageList.length < 3">
              <el-icon :size="24" color="#717171"><Plus /></el-icon>
              <span class="upload-text">上传图片</span>
            </div>
          </div>
          <input type="file" ref="fileInput" style="display: none" accept="image/*" @change="handleUpload" />
          <div class="upload-tip">最多上传 3 张图片</div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" class="airbnb-btn text">取消</el-button>
        <el-button type="primary" @click="submit" :loading="loading" class="airbnb-btn primary">提交投诉</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus, Close, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  visible: Boolean,
  targetType: Number, // 1-订单, 2-车辆, 3-用户
  targetId: [String, Number]
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = ref(false)
const loading = ref(false)
const fileInput = ref(null)
const imageList = ref([])

const form = ref({
  category: '',
  content: ''
})

// 监听 props 变化
watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val) {
    // 重置表单
    form.value = { category: '', content: '' }
    imageList.value = []
  }
})

const targetTypeName = computed(() => {
  const map = { 1: '订单', 2: '车辆', 3: '用户' }
  return map[props.targetType] || '未知'
})

const handleClose = () => {
  emit('update:visible', false)
}

// 上传图片
const triggerUpload = () => fileInput.value.click()

const handleUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await axios.post('/api/common/upload', formData, {
      headers: { 
        token: localStorage.getItem('token'),
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.data.code === 200) {
      imageList.value.push(res.data.data)
    }
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    event.target.value = ''
  }
}

const removeImage = (index) => {
  imageList.value.splice(index, 1)
}

// 提交
const submit = async () => {
  if (!form.value.category || !form.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  loading.value = true
  try {
    const res = await axios.post('/api/complaint/add', {
      targetType: props.targetType,
      targetId: String(props.targetId),
      category: form.value.category,
      content: form.value.content,
      images: imageList.value.join(',')
    }, {
      headers: { token: localStorage.getItem('token') }
    })
    
    if (res.data.code === 200) {
      ElMessage.success(res.data.data)
      handleClose()
      emit('success')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    loading.value = false
  }
}
</script>

<style>
/* 全局弹窗样式覆盖 (确保生效) */
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
.airbnb-dialog-base .el-dialog__footer {
  padding: 0;
}
</style>

<style scoped>
/* Header */
.dialog-header {
  height: 64px;
  border-bottom: 1px solid #ebebeb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.close-btn {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}
.close-btn:hover { background: #f7f7f7; }
.dialog-title { font-weight: 800; font-size: 16px; color: #222; }
.header-placeholder { width: 32px; }

/* Body */
.dialog-body {
  padding: 24px;
}

.target-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f7f7f7;
  border-radius: 8px;
  width: fit-content;
}
.target-tag { font-weight: 600; color: #222; }
.target-id { color: #717171; font-size: 13px; }

.w-full { width: 100%; }

/* Upload */
.upload-container {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.img-preview {
  width: 100px; height: 100px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #ebebeb;
}
.img-preview img { width: 100%; height: 100%; object-fit: cover; }
.del-mask {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s; cursor: pointer;
  color: white;
}
.img-preview:hover .del-mask { opacity: 1; }

.upload-trigger {
  width: 100px; height: 100px;
  border: 1px dashed #b0b0b0;
  border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #f9f9f9;
}
.upload-trigger:hover { background: #fff; border-color: #222; }
.upload-text { font-size: 12px; color: #717171; margin-top: 4px; }

.upload-tip { font-size: 12px; color: #717171; margin-top: 8px; }

/* Footer */
.dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid #ebebeb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Buttons */
.airbnb-btn {
  border-radius: 8px;
  font-weight: 600;
  padding: 10px 24px;
  height: 44px;
  border: 1px solid #222;
  transition: all 0.2s;
}
.airbnb-btn.primary {
  background: #222;
  border-color: #222;
  color: white;
}
.airbnb-btn.primary:hover {
  background: #000;
  border-color: #000;
}
.airbnb-btn.text {
  border: none;
  text-decoration: underline;
  color: #222;
  padding: 0;
  height: auto;
}
.airbnb-btn.text:hover { background: transparent; color: #717171; }

/* Form Overrides */
.custom-form :deep(.el-form-item__label) {
  font-weight: 600; color: #484848; padding-bottom: 6px;
}
</style>