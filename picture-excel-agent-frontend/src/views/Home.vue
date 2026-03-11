<template>
  <div class="home">
    <div class="header">
      <h1>Picture to Excel Agent</h1>
      <p class="subtitle">智能图片转Excel工具</p>
    </div>

    <div class="app-grid">
      <div
        class="app-card"
        @click="navigateToApp('/image-to-excel')"
      >
        <div class="app-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 4C4 2.89543 4.89543 2 6 2H18C19.1046 2 20 2.89543 20 4V20C20 21.1046 19.1046 22 18 22H6C4.89543 22 4 21.1046 4 20V4Z" stroke="currentColor" stroke-width="2"/>
            <path d="M9 14L9 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M12 14L12 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M15 14L15 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 18H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h2 class="app-title">图片转Excel</h2>
        <p class="app-description">上传图片，自动识别并转换为Excel表格</p>
      </div>

      <div class="app-card disabled">
        <div class="app-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          </svg>
        </div>
        <h2 class="app-title">更多应用</h2>
        <p class="app-description">即将推出...</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  methods: {
    navigateToApp(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
}

.header {
  text-align: center;
  margin-bottom: 60px;
}

.header h1 {
  font-size: 48px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
}

.subtitle {
  font-size: 20px;
  color: #7f8c8d;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  max-width: 800px;
  width: 100%;
}

.app-card {
  background: white;
  border-radius: 16px;
  padding: 40px 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border: 2px solid transparent;
  text-align: center;
}

.app-card:hover:not(.disabled) {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  border-color: #3498db;
}

.app-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.app-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  color: #3498db;
}

.app-icon svg {
  width: 100%;
  height: 100%;
}

.app-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.app-description {
  font-size: 16px;
  color: #7f8c8d;
  line-height: 1.5;
}
</style>
