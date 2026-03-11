<template>
  <div class="file-list">
    <div class="list-header">
      <h3>已选择文件</h3>
      <span class="file-count">{{ files.length }}</span>
    </div>

    <div class="list-content">
      <div
        v-for="(file, index) in files"
        :key="index"
        class="file-item"
      >
        <div class="file-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 19V5C21 3.89543 20.1046 3 19 3H5C3.89543 3 3 3.89543 3 5V19C3 20.1046 3.89543 21 5 21H19C20.1046 21 21 20.1046 21 19Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M8.5 13.5L11 16L15.5 11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>

        <div class="file-info">
          <p class="file-name">{{ file.name }}</p>
          <p class="file-size">{{ formatFileSize(file.size) }}</p>
        </div>

        <button
          class="remove-button"
          @click="$emit('remove-file', index)"
        >
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <div v-if="files.length === 0" class="empty-state">
        <p>暂无文件</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FileList',
  props: {
    files: {
      type: Array,
      default: () => []
    }
  },
  emits: ['remove-file'],
  methods: {
    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
  }
}
</script>

<style scoped>
.file-list {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0; /* Allow shrinking below content size */
  overflow: hidden; /* Prevent content overflow */
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7eb;
}

.list-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.file-count {
  background: #3498db;
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.list-content {
  flex: 1;
  overflow-y: auto;
  min-height: 0; /* Important for flex children to shrink properly */
  /* Custom scrollbar styling */
  scrollbar-width: thin;
  scrollbar-color: #3498db #f1f1f1;
}

/* Webkit browsers (Chrome, Safari, Edge) */
.list-content::-webkit-scrollbar {
  width: 8px;
}

.list-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.list-content::-webkit-scrollbar-thumb {
  background: #3498db;
  border-radius: 4px;
}

.list-content::-webkit-scrollbar-thumb:hover {
  background: #2980b9;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #f8f9fa;
  transition: background 0.2s ease;
}

.file-item:hover {
  background: #e9ecef;
}

.file-icon {
  width: 40px;
  height: 40px;
  color: #3498db;
  flex-shrink: 0;
}

.file-icon svg {
  width: 100%;
  height: 100%;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 12px;
  color: #7f8c8d;
  margin: 0;
}

.remove-button {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #e74c3c;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
  flex-shrink: 0;
}

.remove-button:hover {
  background: #fee;
}

.remove-button svg {
  width: 16px;
  height: 16px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px; /* Use min-height instead of height */
  color: #7f8c8d;
  font-size: 14px;
}
</style>
