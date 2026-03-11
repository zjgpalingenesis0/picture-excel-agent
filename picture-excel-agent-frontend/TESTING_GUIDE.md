# Quick Testing Guide

## How to Test the Fix

### 1. Start the Backend
```bash
# Make sure the backend is running on port 8123
cd ../
mvn spring-boot:run
```

### 2. Start the Frontend
```bash
# In the frontend directory
npm run dev
```

### 3. Open Browser DevTools
- Press F12 or right-click → Inspect
- Go to the Console tab
- Keep this visible while testing

### 4. Test the Fix

#### Step 1: Upload Image
1. Navigate to `http://localhost:5173` (or your frontend port)
2. Click "图片转Excel" button
3. Upload a test image (certificate, award, etc.)
4. (Optional) Add custom extraction rules

#### Step 2: Start Conversion
1. Click "开始转换" button
2. **Expected Result:** Task immediately appears in "处理结果" section
3. **Console Output:**
   ```
   Polling task {taskId}...
   Task {taskId} status: {status: "PENDING", ...}
   ```

#### Step 3: Monitor Progress
1. Watch the status badge change: "等待中" → "处理中" → "已完成"
2. **Console Output** (every 2 seconds):
   ```
   Polling task {taskId}...
   Task {taskId} status: {status: "PROCESSING", ...}
   Polling task {taskId}...
   Task {taskId} status: {status: "COMPLETED", outputFilePath: "...", ...}
   ```

#### Step 4: Download Result
1. When status shows "已完成", a green "下载" button should appear
2. Click the download button
3. **Console Output:**
   ```
   Downloading task {taskId} as {filename}.xlsx...
   ```
4. **Expected Result:** Excel file downloads with correct name

### 5. Verify Backend Output
Check that the Excel file was generated in `../data/output/` directory

### 6. Test Error Handling
To test error handling:
1. Upload a corrupted or non-image file
2. Expected: Status shows "失败" with error message
3. Red X icon should appear

### 7. Test Batch Processing
1. Upload multiple images (2-3 files)
2. Click "开始转换"
3. Expected: Each file gets its own task card in results
4. All tasks poll independently

## What to Look For

✅ **Success Indicators:**
- Task appears immediately after clicking "开始转换"
- Status updates in real-time
- Progress bar shows during processing
- Download button appears when completed
- Download works with correct filename

❌ **Failure Indicators:**
- "处理结果" section stays empty
- No console logs appearing
- Status stuck on "等待中"
- Download button doesn't appear
- Download fails or wrong filename

## Common Issues

### Issue: Task doesn't appear in results
**Check:** Console for errors, network tab for failed requests

### Issue: Status stuck on "等待中"
**Check:** Backend logs, verify task is actually processing

### Issue: Download button doesn't appear
**Check:** Console for status response, verify `status === 'COMPLETED'`

### Issue: Download fails
**Check:** Network tab for download endpoint response

## Debug Console Logs

The fix adds these console logs for debugging:
- `Polling task {taskId}...` - Shows polling is active
- `Task {taskId} status: {...}` - Shows backend response
- `Refreshing status for task {taskId}...` - Manual refresh triggered
- `Task {taskId} refreshed status: {...}` - Manual refresh response
- `Downloading task {taskId} as {filename}...` - Download initiated

## Field Mapping Reference

| Backend Field | Frontend Field | Notes |
|--------------|----------------|-------|
| `status` | `status` | Direct mapping |
| (none) | `progress` | Calculated from status |
| `outputFilePath` | `resultFile` | Field name changed |
| `errorMessage` | `error` | Field name changed |
| `completedAt` | `completedAt` | Direct mapping |
| (not in status) | `fileName` | Set at task creation |
