<template>
  <div class="task-result">
    <div v-if="tasks.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M9 12H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M12 9V15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <p>暂无处理结果</p>
      <p class="hint">上传图片并点击"开始转换"后，结果将显示在这里</p>
    </div>

    <div v-else class="task-list">
      <div
        v-for="task in tasks"
        :key="task.taskId"
        class="task-item"
        :class="getStatusClass(task.status)"
      >
        <div class="task-main">
          <div class="task-icon">
            <svg v-if="task.status === 'COMPLETED'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <svg v-else-if="task.status === 'FAILED'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 9L9 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M9 9L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
              <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 16V16.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>

          <div class="task-content">
            <div class="task-header">
              <h4 class="task-filename">{{ task.fileName }}</h4>
              <span class="task-id">{{ task.taskId.slice(0, 8) }}</span>
            </div>

            <div class="task-status-row">
              <span class="status-badge" :class="getStatusClass(task.status)">
                {{ getStatusText(task.status) }}
              </span>
              <span class="task-time">{{ formatTime(task.createdAt) }}</span>
            </div>

            <!-- Progress Bar -->
            <div v-if="task.progress >= 0 && task.progress < 100" class="progress-bar">
              <div class="progress-fill" :style="{ width: task.progress + '%' }"></div>
            </div>

            <!-- Error Message -->
            <p v-if="task.status === 'FAILED' && task.error" class="error-message">
              {{ task.error }}
            </p>
          </div>
        </div>

        <div class="task-actions">
          <button
            v-if="task.status === 'COMPLETED'"
            @click="$emit('download', task.taskId)"
            class="action-button download"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 16V8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M8 12L12 8L16 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M4 20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V9L13 2H6C4.89543 2 4 2.89543 4 4V20Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M13 2V9H20" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            </svg>
            下载
          </button>

          <button
            v-if="task.status !== 'COMPLETED' && task.status !== 'FAILED'"
            @click="$emit('refresh', task.taskId)"
            class="action-button refresh"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M23 4V10H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M20.49 15C19.9828 16.8405 18.9211 18.4796 17.4458 19.6945C15.9706 20.9094 14.1532 21.6414 12.2512 21.7891C10.3491 21.9368 8.44283 21.4929 6.79713 20.5181C5.15144 19.5432 3.85014 18.0834 3.06355 16.3372C2.27696 14.5909 2.04391 12.6428 2.39778 10.7587C2.75165 8.87455 3.67535 7.14629 5.04544 5.82208C6.41553 4.49787 8.16548 3.63944 10.0412 3.36752C11.917 3.09561 13.8261 3.42389 15.51 4.31L20.49 9.29" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            刷新
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TaskResult',
  props: {
    tasks: {
      type: Array,
      default: () => []
    }
  },
  emits: ['download', 'refresh'],
  methods: {
    getStatusClass(status) {
      const statusMap = {
        'PENDING': 'pending',
        'PROCESSING': 'processing',
        'OCR_COMPLETED': 'processing',
        'EXTRACTION_COMPLETED': 'processing',
        'VALIDATION_COMPLETED': 'processing',
        'COMPLETED': 'completed',
        'FAILED': 'failed',
        'CANCELLED': 'failed'
      }
      return statusMap[status] || 'pending'
    },

    getStatusText(status) {
      const statusMap = {
        'PENDING': '等待中',
        'PROCESSING': '处理中',
        'OCR_COMPLETED': 'OCR识别完成',
        'EXTRACTION_COMPLETED': '数据抽取完成',
        'VALIDATION_COMPLETED': '数据校验完成',
        'COMPLETED': '已完成',
        'FAILED': '失败',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },

    formatTime(isoString) {
      if (!isoString) return ''
      const date = new Date(isoString)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`

      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.task-result {
  min-height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.empty-state p {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.hint {
  font-size: 14px;
  color: #95a5a6;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #95a5a6;
  transition: all 0.2s ease;
}

.task-item:hover {
  background: #e9ecef;
}

.task-item.completed {
  border-left-color: #27ae60;
  background: #e8f8f0;
}

.task-item.completed:hover {
  background: #d4efdf;
}

.task-item.failed {
  border-left-color: #e74c3c;
  background: #fdedec;
}

.task-item.processing,
.task-item.pending {
  border-left-color: #f39c12;
}

.task-main {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.task-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.task-icon svg {
  width: 100%;
  height: 100%;
}

.completed .task-icon {
  color: #27ae60;
}

.failed .task-icon {
  color: #e74c3c;
}

.processing .task-icon,
.pending .task-icon {
  color: #f39c12;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.task-filename {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-id {
  font-size: 12px;
  color: #95a5a6;
  font-family: monospace;
  flex-shrink: 0;
}

.task-status-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  text-transform: uppercase;
}

.status-badge.pending {
  background: #fef5e7;
  color: #f39c12;
}

.status-badge.processing {
  background: #fef5e7;
  color: #f39c12;
}

.status-badge.completed {
  background: #d4efdf;
  color: #27ae60;
}

.status-badge.failed {
  background: #fdedec;
  color: #e74c3c;
}

.task-time {
  font-size: 12px;
  color: #95a5a6;
}

.progress-bar {
  margin-top: 12px;
  height: 4px;
  background: #e4e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3498db, #2980b9);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.error-message {
  margin-top: 8px;
  font-size: 14px;
  color: #e74c3c;
}

.task-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-button svg {
  width: 16px;
  height: 16px;
}

.action-button.download {
  background: #27ae60;
  color: white;
}

.action-button.download:hover {
  background: #229954;
}

.action-button.refresh {
  background: white;
  color: #3498db;
  border: 1px solid #3498db;
}

.action-button.refresh:hover {
  background: #f0f7ff;
}
</style>
