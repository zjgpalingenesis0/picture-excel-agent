import axios from 'axios'

const API_BASE_URL = 'http://localhost:8123/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const imageProcessingApi = {
  // Process single image
  processImage: async (file, extractionRules = null) => {
    const formData = new FormData()
    formData.append('file', file)
    if (extractionRules) {
      formData.append('extractionRule', extractionRules)
    }
    formData.append('async', 'true')

    const response = await api.post('/image/excel/process/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    // Debug logging to see the raw axios response structure
    console.log('=== API processImage Raw Response ===')
    console.log('Response type:', typeof response)
    console.log('Response keys:', Object.keys(response))
    console.log('Response.data:', response.data)
    console.log('Response.data keys:', Object.keys(response.data || {}))
    console.log('Response.data.taskId:', response.data?.taskId)
    console.log('Response.data.task_id:', response.data?.task_id)
    console.log('Full response:', JSON.stringify(response, null, 2))

    return response.data
  },

  // Batch process multiple images
  processBatch: async (files, extractionRules = null) => {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    if (extractionRules) {
      formData.append('extractionRule', extractionRules)
    }
    formData.append('async', 'true')

    const response = await api.post('/image/excel/process/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    // Debug logging to see the raw axios response structure
    console.log('=== API processBatch Raw Response ===')
    console.log('Response type:', typeof response)
    console.log('Response keys:', Object.keys(response))
    console.log('Response.data:', response.data)
    console.log('Response.data keys:', Object.keys(response.data || {}))
    console.log('Response.data.taskId:', response.data?.taskId)
    console.log('Response.data.task_id:', response.data?.task_id)
    console.log('Full response:', JSON.stringify(response, null, 2))

    return response.data
  },

  // Get task status
  getTaskStatus: async (taskId) => {
    const response = await api.get(`/image/excel/task/${taskId}`)
    console.log('=== API getTaskStatus Raw Response ===')
    console.log('Response.data:', response.data)
    console.log('Response.data keys:', Object.keys(response.data || {}))
    console.log('Response.data.status:', response.data?.status)
    console.log('Response.data.downloadUrl:', response.data?.downloadUrl)
    console.log('Response.data.download_url:', response.data?.download_url)
    console.log('Full response:', JSON.stringify(response, null, 2))
    return response.data
  },

  // Download result file
  downloadResult: async (taskId, filename = 'result.xlsx') => {
    const response = await api.get(`/image/excel/task/${taskId}/download`, {
      responseType: 'blob'
    })

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  },

  // Get all tasks
  getAllTasks: async () => {
    const response = await api.get('/image/excel/tasks')
    return response.data
  }
}

export default imageProcessingApi
