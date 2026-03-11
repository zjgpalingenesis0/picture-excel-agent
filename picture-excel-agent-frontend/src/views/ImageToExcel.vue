<template>
  <div class="image-to-excel">
    <header class="page-header">
      <button class="back-button" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回首页
      </button>
      <h1>图片转Excel</h1>
    </header>

    <div class="main-content">
      <!-- Upper Section (larger) -->
      <div class="upper-section">
        <div class="upload-area">
          <FileUpload
            @files-selected="handleFilesSelected"
            :is-processing="isProcessing"
          />

          <div class="action-panel">
            <input
              v-model="extractionRules"
              type="text"
              placeholder="自定义提取规则（可选）"
              class="rules-input"
            />
            <button
              @click="handleConvert"
              :disabled="!hasFiles || isProcessing"
              class="convert-button"
            >
              {{ isProcessing ? '处理中...' : '开始转换' }}
            </button>
          </div>
        </div>

        <FileList
          :files="uploadedFiles"
          @remove-file="handleRemoveFile"
        />
      </div>

      <!-- Lower Section -->
      <div class="lower-section">
        <div class="section-header">
          <h2>处理结果</h2>
          <span class="result-count">{{ taskResults.length }} 个结果</span>
        </div>

        <TaskResult
          :tasks="taskResults"
          @download="handleDownload"
          @refresh="handleRefreshStatus"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import FileUpload from '../components/FileUpload.vue'
import FileList from '../components/FileList.vue'
import TaskResult from '../components/TaskResult.vue'
import { imageProcessingApi } from '../api/imageProcessing.js'

export default {
  name: 'ImageToExcel',
  components: {
    FileUpload,
    FileList,
    TaskResult
  },
  setup() {
    const router = useRouter()
    const uploadedFiles = ref([])
    const taskResults = ref([])
    const isProcessing = ref(false)
    const extractionRules = ref('')
    const pollIntervals = ref(new Map())

    const hasFiles = computed(() => uploadedFiles.value.length > 0)

    const goBack = () => {
      router.push('/')
    }

    const handleFilesSelected = (files) => {
      uploadedFiles.value = [...uploadedFiles.value, ...files]
    }

    const handleRemoveFile = (index) => {
      uploadedFiles.value.splice(index, 1)
    }

    const handleConvert = async () => {
      if (uploadedFiles.value.length === 0 || isProcessing.value) return

      isProcessing.value = true

      try {
        let response

        if (uploadedFiles.value.length === 1) {
          // Single file processing
          response = await imageProcessingApi.processImage(
            uploadedFiles.value[0],
            extractionRules.value || null
          )
        } else {
          // Batch processing
          response = await imageProcessingApi.processBatch(
            uploadedFiles.value,
            extractionRules.value || null
          )
        }

        // Handle response - backend always returns a single taskId for both single and batch
        console.log('=== API Response Debug ===')
        console.log('Raw response type:', typeof response)
        console.log('Raw response:', response)
        console.log('Response keys:', Object.keys(response || {}))
        console.log('response.taskId:', response?.taskId)
        console.log('response.task_id:', response?.task_id)
        console.log('Full response JSON:', JSON.stringify(response, null, 2))

        // Normalize response - handle both camelCase and snake_case field names
        // Backend might return task_id (snake_case) or taskId (camelCase)
        const taskId = response?.taskId || response?.task_id
        console.log('Extracted taskId:', taskId)

        if (taskId) {
          // Single task ID for both single and batch processing
          const filename = uploadedFiles.value.length === 1
            ? uploadedFiles.value[0].name
            : `${uploadedFiles.value.length}个文件`
          await addAndPollTask(taskId, filename)

          // Clear uploaded files after successful submission
          uploadedFiles.value = []
          extractionRules.value = ''
        } else {
          console.error('No task ID found in response. Available fields:', Object.keys(response || {}))
          throw new Error('No task ID returned from backend')
        }
      } catch (error) {
        console.error('Error processing images:', error)
        alert('处理失败：' + (error.response?.data?.message || error.message))
      } finally {
        isProcessing.value = false
      }
    }

    const addAndPollTask = async (taskId, filename) => {
      // Add task to results
      const task = {
        taskId,
        fileName: filename,
        status: 'PENDING',
        progress: 0,
        createdAt: new Date().toISOString()
      }
      taskResults.value.unshift(task)

      // Start polling for status updates
      pollTaskStatus(taskId)
    }

    // Calculate progress from status since backend doesn't return it directly
    // statusResponse is the API response object with a status property (string enum)
    const calculateProgress = (statusResponse) => {
      switch (statusResponse.status) {
        case 'PENDING': return 0
        case 'PROCESSING': return 50
        case 'OCR_COMPLETED': return 75
        case 'EXTRACTION_COMPLETED': return 90
        case 'VALIDATION_COMPLETED': return 95
        case 'COMPLETED': return 100
        case 'FAILED': return 0
        case 'CANCELLED': return 0
        default: return 0
      }
    }

    const pollTaskStatus = async (taskId) => {
      // Clear existing interval if any
      if (pollIntervals.value.has(taskId)) {
        clearInterval(pollIntervals.value.get(taskId))
      }

      const interval = setInterval(async () => {
        try {
          console.log(`Polling task ${taskId}...`)
          const statusResponse = await imageProcessingApi.getTaskStatus(taskId)
          console.log(`Task ${taskId} status response:`, statusResponse)
          console.log(`Task ${taskId} status value:`, statusResponse?.status)
          console.log(`Task ${taskId} status type:`, typeof statusResponse?.status)

          // Update task in results
          const taskIndex = taskResults.value.findIndex(t => t.taskId === taskId)
          if (taskIndex !== -1) {
            taskResults.value[taskIndex] = {
              ...taskResults.value[taskIndex],
              status: statusResponse.status,
              progress: calculateProgress(statusResponse),
              completedAt: statusResponse.completedAt || statusResponse.completed_at,
              resultFile: statusResponse.downloadUrl || statusResponse.download_url,
              error: statusResponse.status === 'FAILED' ? (statusResponse.message || statusResponse.error_message) : null
            }
            console.log(`Task ${taskId} updated in results:`, taskResults.value[taskIndex])
          }

          // Stop polling if task is completed or failed
          if (statusResponse.status === 'COMPLETED' || statusResponse.status === 'FAILED') {
            console.log(`Task ${taskId} finished with status: ${statusResponse.status}. Stopping polling.`)
            clearInterval(interval)
            pollIntervals.value.delete(taskId)
          }
        } catch (error) {
          console.error(`Error polling task ${taskId}:`, error)
          // Don't stop polling on error, just log it
        }
      }, 2000) // Poll every 2 seconds

      pollIntervals.value.set(taskId, interval)
    }

    const handleDownload = async (taskId) => {
      try {
        const task = taskResults.value.find(t => t.taskId === taskId)
        // Use the original filename to generate the download name
        const filename = task?.fileName
          ? task.fileName.replace(/\.[^/.]+$/, '') + '.xlsx'
          : `result_${taskId}.xlsx`
        console.log(`Downloading task ${taskId} as ${filename}...`)
        await imageProcessingApi.downloadResult(taskId, filename)
      } catch (error) {
        console.error('Error downloading file:', error)
        alert('下载失败：' + (error.response?.data?.message || error.message))
      }
    }

    const handleRefreshStatus = async (taskId) => {
      try {
        console.log(`Refreshing status for task ${taskId}...`)
        const statusResponse = await imageProcessingApi.getTaskStatus(taskId)
        console.log(`Task ${taskId} refreshed status:`, statusResponse)

        const taskIndex = taskResults.value.findIndex(t => t.taskId === taskId)
        if (taskIndex !== -1) {
          taskResults.value[taskIndex] = {
            ...taskResults.value[taskIndex],
            status: statusResponse.status,
            progress: calculateProgress(statusResponse),
            completedAt: statusResponse.completedAt || statusResponse.completed_at,
            resultFile: statusResponse.downloadUrl || statusResponse.download_url,
            error: statusResponse.status === 'FAILED' ? (statusResponse.message || statusResponse.error_message) : null
          }
        }

        // Restart polling if task is not complete
        if (statusResponse.status !== 'COMPLETED' && statusResponse.status !== 'FAILED') {
          pollTaskStatus(taskId)
        }
      } catch (error) {
        console.error('Error refreshing status:', error)
        alert('刷新状态失败：' + (error.response?.data?.message || error.message))
      }
    }

    return {
      uploadedFiles,
      taskResults,
      isProcessing,
      extractionRules,
      hasFiles,
      goBack,
      handleFilesSelected,
      handleRemoveFile,
      handleConvert,
      handleDownload,
      handleRefreshStatus
    }
  }
}
</script>

<style scoped>
.image-to-excel {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: white;
  padding: 20px 40px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #2c3e50;
  transition: all 0.2s ease;
}

.back-button:hover {
  background: #e4e7eb;
}

.back-button svg {
  width: 20px;
  height: 20px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 32px 40px;
  gap: 32px;
}

.upper-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  height: 500px;
  overflow: hidden; /* Prevent content from spilling outside */
}

.upload-area {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  min-height: 0; /* Allow shrinking below content size */
  overflow: hidden; /* Prevent content overflow */
}

.action-panel {
  display: flex;
  gap: 12px;
}

.rules-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e4e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.rules-input:focus {
  outline: none;
  border-color: #3498db;
}

.convert-button {
  padding: 12px 32px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.convert-button:hover:not(:disabled) {
  background: #2980b9;
}

.convert-button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.lower-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.result-count {
  font-size: 14px;
  color: #7f8c8d;
  background: #f5f7fa;
  padding: 4px 12px;
  border-radius: 12px;
}
</style>
