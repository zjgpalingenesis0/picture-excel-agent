# 百度OCR集成指南

## 概述

本系统集成了百度OCR API，提供高准确率的中文文字识别能力。百度OCR相比本地OCR方案具有以下优势：

- 识别准确率更高，尤其是中文字符
- 支持多种OCR场景（通用、高精度、表格、身份证等）
- 无需本地安装OCR软件
- API调用简单，易于集成

## 前置准备

### 1. 注册百度智能云账号

1. 访问 [百度智能云控制台](https://console.bce.baidu.com/ai/#/ai/ocr/overview/index)
2. 注册并登录账号
3. 完成实名认证（个人或企业）

### 2. 创建OCR应用

1. 在控制台选择"人工智能" → "文字识别"
2. 点击"创建应用"
3. 填写应用信息：
   - 应用名称：如"图片转Excel系统"
   - 应用归属：个人或企业
   - 应用描述：简单描述应用用途
4. 提交后，系统会生成以下信息：
   - **AppID**
   - **API Key**
   - **Secret Key**

### 3. 查看免费配额

百度OCR提供免费配额：
- 通用文字识别：1000次/天
- 通用文字识别（高精度版）：500次/天
- 其他接口各有不同的免费配额

## 配置步骤

### 方式1: 环境变量（推荐）

在系统环境变量中设置：

```bash
export BAIDU_OCR_APP_ID=your-app-id
export BAIDU_OCR_API_KEY=your-api-key
export BAIDU_OCR_SECRET_KEY=your-secret-key
```

### 方式2: 配置文件

编辑 `src/main/resources/application.yml`:

```yaml
# 设置OCR引擎为百度
ocr:
  default-engine: baidu

# 百度OCR配置
baidu:
  ocr:
    app-id: your-actual-app-id
    api-key: your-actual-api-key
    secret-key: your-actual-secret-key
    use-accurate: false  # 是否使用高精度版
```

### 方式3: 启动参数

```bash
java -jar picture-excel-agent.jar \
  --baidu.ocr.app-id=your-app-id \
  --baidu.ocr.api-key=your-api-key \
  --baidu.ocr.secret-key=your-secret-key
```

## 功能说明

### 普通模式 vs 高精度模式

| 模式 | 准确率 | 速度 | 免费配额 |
|------|--------|------|----------|
| 普通模式 | ⭐⭐⭐⭐ | 快 | 1000次/天 |
| 高精度模式 | ⭐⭐⭐⭐⭐ | 较慢 | 500次/天 |

配置高精度模式：

```yaml
baidu:
  ocr:
    use-accurate: true
```

**建议**：
- 一般场景使用普通模式（速度快，准确率已足够）
- 重要文档、复杂表格使用高精度模式

### 支持的识别类型

当前实现的功能：
- ✅ 通用文字识别
- ✅ 通用文字识别（高精度版）
- ✅ 中英文混合识别
- ✅ 图像方向检测
- ✅ 语言自动检测

可扩展功能：
- 表格识别
- 身份证识别
- 银行卡识别
- 驾驶证识别
- 营业执照识别

## API使用示例

### 单图处理

```bash
curl -X POST http://localhost:8080/api/v1/process/image \
  -F "file=@certificate.jpg" \
  -F "extractionRule=提取证书信息"
```

### 批量处理

```bash
curl -X POST http://localhost:8080/api/v1/process/batch \
  -F "files=@cert1.jpg" \
  -F "files=@cert2.jpg"
```

## 错误处理

### 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 1 | API Key不存在 | 检查API Key配置 |
| 2 | 请求参数错误 | 检查请求参数格式 |
| 3 | API调用次数超限 | 检查配额或购买套餐 |
| 17 | 每天请求量超限额 | 次日重置或购买套餐 |
| 18 | QPS超限额 | 降低请求频率 |
| 19 | 请求总量超限额 | 购买套餐或等待重置 |
| 100 | 无效参数 | 检查参数配置 |
| 110 | Access Token无效 | 重新获取Token |
| 111 | Access Token过期 | 重新获取Token |
| 282000 | 内部错误 | 稍后重试 |
| 282003 | 请求参数缺失 | 检查请求参数 |
| 282005 | 处理超时 | 降低图片大小或分辨率 |
| 282006 | 找不到结果 | 检查图片内容 |
| 282114 | URL参数不存在 | 检查URL参数 |
| 282808 | request id检查失败 | 检查请求ID |

### 故障排查

1. **API调用失败**
   - 检查网络连接
   - 验证API密钥是否正确
   - 确认配额是否充足

2. **识别结果不准确**
   - 启用高精度模式
   - 提高图片质量和分辨率
   - 确保图片清晰度

3. **处理速度慢**
   - 使用普通模式而非高精度
   - 降低图片分辨率
   - 使用异步处理

## 费用说明

### 免费配额

- 通用文字识别：1000次/天
- 通用文字识别（高精度版）：500次/天

### 付费标准

超出免费配额后按量计费（具体价格参考百度官方文档）：

| 接口 | 单价（次/元） |
|------|--------------|
| 通用文字识别 | 0.002 |
| 通用文字识别（高精度版） | 0.006 |

### 成本优化建议

1. 优先使用普通模式（更便宜）
2. 重要文档才使用高精度模式
3. 批量处理时注意控制并发量
4. 定期监控API调用量

## 性能优化

### 1. 并发控制

系统默认支持并发处理，但要注意：
- 免费版QPS限制：2
- 付费版QPS限制：10

### 2. 图片优化

建议：
- 图片大小：不超过4MB
- 图片分辨率：1024x768 或更高
- 图片格式：JPG、PNG
- 图片质量：清晰，避免模糊

### 3. 缓存策略

可以添加缓存避免重复识别：
```java
@Cacheable(value = "ocrResults", key = "#imageFile.name")
public OcrResult recognize(File imageFile) {
    // OCR识别
}
```

## 最佳实践

### 1. 错误重试

实现自动重试机制（已内置）：
```yaml
dashscope:
  max-retries: 3
```

### 2. 日志记录

开启详细日志用于调试：
```yaml
logging:
  level:
    com.zjg.pictureexcelagent.ocr: DEBUG
```

### 3. 监控告警

监控以下指标：
- API调用量
- 成功率
- 响应时间
- 错误率

### 4. 配额管理

- 定期检查剩余配额
- 设置配额预警
- 合理分配使用量

## 安全建议

1. **保护API密钥**
   - 使用环境变量存储
   - 不要提交到代码仓库
   - 定期轮换密钥

2. **访问控制**
   - 限制API调用来源IP
   - 设置访问频率限制
   - 记录所有调用日志

3. **数据安全**
   - 不上传敏感信息
   - 及时删除临时文件
   - 遵守隐私法规

## 技术支持

- 百度OCR文档: https://cloud.baidu.com/doc/OCR/index.html
- 技术社区: https://cloud.baidu.com/forum/
- 工单支持: 控制台提交工单

## 版本更新

当前集成版本：百度AI Java SDK 4.16.18

更新日志：
- 2024-01: 初始集成
- 支持通用文字识别
- 支持高精度模式
- 自动错误重试

## 常见问题

### Q: 百度OCR和Tesseract如何选择？

A:
- **百度OCR**：准确率高，使用简单，适合生产环境
- **Tesseract**：完全免费，本地运行，适合开发测试

### Q: 如何提高识别准确率？

A:
1. 使用高精度模式
2. 提高图片质量和分辨率
3. 确保图片清晰，避免模糊
4. 正确的图片方向

### Q: 配额用完了怎么办？

A:
1. 等待次日配额重置
2. 购买付费套餐
3. 或切换到Tesseract OCR

### Q: 可以同时使用多个OCR引擎吗？

A: 系统设计上只同时使用一个OCR引擎，但可以随时切换配置。

---

**最后更新**: 2024年
**文档版本**: 1.0
