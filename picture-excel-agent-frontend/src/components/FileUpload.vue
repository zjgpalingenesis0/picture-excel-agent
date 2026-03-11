<template>
  <div class="file-upload">
    <div
      class="upload-zone"
      :class="{ 'drag-over': isDragOver, 'disabled': isProcessing }"
      @dragover.prevent="handleDragOver"
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        multiple
        @change="handleFileSelect"
        style="display: none"
      />

      <div class="upload-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 16V8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M8 12L12 8L16 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M4 20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V9L13 2H6C4.89543 2 4 2.89543 4 4V20Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          <path d="M13 2V9H20" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
        </svg>
      </div>

      <div class="upload-text">
        <p class="main-text">拖拽图片到这里，或点击选择</p>
        <p class="sub-text">支持 JPG、PNG、GIF 等格式，可多选</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'FileUpload',
  props: {
    isProcessing: {
      type: Boolean,
      default: false
    }
  },
  emits: ['files-selected'],
  setup(props, { emit }) {
    const isDragOver = ref(false)
    const fileInput = ref(null)

    const handleDragOver = () => {
      if (!props.isProcessing) {
        isDragOver.value = true
      }
    }

    const handleDragLeave = () => {
      isDragOver.value = false
    }

    const handleDrop = (e) => {
      if (props.isProcessing) return

      isDragOver.value = false
      const files = Array.from(e.dataTransfer.files).filter(file => file.type.startsWith('image/'))

      if (files.length > 0) {
        emit('files-selected', files)
      }
    }

    const triggerFileInput = () => {
      if (!props.isProcessing) {
        fileInput.value?.click()
      }
    }

    const handleFileSelect = (e) => {
      const files = Array.from(e.target.files)
      if (files.length > 0) {
        emit('files-selected', files)
      }
      // Reset input value to allow selecting the same file again
      e.target.value = ''
    }

    return {
      isDragOver,
      fileInput,
      handleDragOver,
      handleDragLeave,
      handleDrop,
      triggerFileInput,
      handleFileSelect
    }
  }
}
</script>

<style scoped>
.file-upload {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* Allow shrinking below content size */
  overflow: hidden; /* Prevent content overflow */
}

.upload-zone {
  flex: 1;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafbfc;
  min-height: 150px;
  max-height: 300px;
}

.upload-zone:hover:not(.disabled) {
  border-color: #3498db;
  background: #f0f7ff;
}

.upload-zone.drag-over {
  border-color: #3498db;
  background: #e6f2ff;
  transform: scale(1.01);
}

.upload-zone.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-icon {
  width: 48px;
  height: 48px;
  color: #3498db;
  margin-bottom: 12px;
}

.upload-icon svg {
  width: 100%;
  height: 100%;
}

.upload-text {
  text-align: center;
}

.main-text {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 8px;
}

.sub-text {
  font-size: 14px;
  color: #7f8c8d;
}
</style>
