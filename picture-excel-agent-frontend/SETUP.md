# Setup Guide - Picture to Excel Agent Frontend

## Quick Start

### 1. Install Dependencies

The npm install may fail due to cache permissions. Try these solutions:

**Option A: Clear npm cache and retry**
```bash
npm cache clean --force
npm install
```

**Option B: Use a different registry**
```bash
npm install --registry=https://registry.npmjs.org/
```

**Option C: Run as Administrator (Windows)**
- Right-click on Command Prompt or PowerShell
- Select "Run as Administrator"
- Navigate to the project directory and run `npm install`

### 2. Start Development Server

```bash
npm run dev
```

The application will start at `http://localhost:3000`

### 3. Build for Production

```bash
npm run build
```

## Configuration

### Backend URL

By default, the frontend connects to `http://localhost:8123/api`. To change this:

Edit `src/api/imageProcessing.js`:
```javascript
const API_BASE_URL = 'http://your-backend-url:port/api'
```

### Development Port

The default development port is 3000. To change it:

Edit `vite.config.js`:
```javascript
server: {
  port: 8080, // Change to your preferred port
  // ... rest of config
}
```

## Project Overview

### File Structure

```
picture-excel-agent-frontend/
├── public/                 # Static assets
├── src/
│   ├── api/               # API service layer
│   │   └── imageProcessing.js
│   ├── components/        # Reusable Vue components
│   │   ├── FileUpload.vue      # Drag & drop file upload
│   │   ├── FileList.vue        # List of uploaded files
│   │   └── TaskResult.vue      # Results with download
│   ├── views/             # Page-level components
│   │   ├── Home.vue            # App navigation hub
│   │   └── ImageToExcel.vue    # Main converter page
│   ├── router/            # Vue Router configuration
│   │   └── index.js
│   ├── App.vue            # Root component
│   └── main.js            # Entry point
├── index.html             # HTML template
├── vite.config.js         # Vite configuration
├── package.json           # Dependencies
└── README.md              # Project documentation
```

### Key Components

1. **Home Page** (`views/Home.vue`)
   - Navigation hub with app cards
   - Currently shows "图片转Excel" as the only active app

2. **Image to Excel Page** (`views/ImageToExcel.vue`)
   - Upper section (60% height):
     - File upload area (drag & drop or click)
     - Custom extraction rules input
     - Convert button
     - Uploaded files list (right sidebar)
   - Lower section (40% height):
     - Processed results display
     - Download buttons for completed tasks
     - Status indicators and progress bars

3. **API Service** (`api/imageProcessing.js`)
   - Handles all backend communication
   - Supports single and batch processing
   - Implements task polling for async operations
   - File download functionality

### Features Implemented

✅ Single file upload
✅ Multiple file upload (batch processing)
✅ Drag and drop interface
✅ Custom extraction rules input
✅ Real-time task status polling
✅ Progress indicators
✅ Download processed Excel files
✅ Error handling and user feedback
✅ Responsive layout
✅ Clean, modern UI

## API Integration

### Endpoints Used

- `POST /api/process/image` - Process a single image
- `POST /api/process/batch` - Process multiple images
- `GET /api/task/{taskId}` - Get task status
- `GET /api/task/{taskId}/download` - Download result Excel file

### Request/Response Flow

1. User selects files → Files stored in component state
2. User clicks "开始转换" → Files sent to backend
3. Backend returns `taskId` → Frontend starts polling
4. Polling updates every 2 seconds until status is COMPLETED or FAILED
5. On completion, download button becomes active

## Browser Compatibility

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+

## Troubleshooting

### "Cannot connect to backend" error

1. Verify backend is running on port 8123
2. Check browser console for CORS errors
3. Ensure backend allows requests from localhost:3000

### Files not uploading

1. Check file size limits in backend
2. Verify file type is supported (image/*)
3. Check browser console for error messages

### Download not working

1. Check if taskId is valid
2. Verify backend file exists
3. Check browser's download folder

## Development Tips

1. **Hot Reload**: Vite provides instant hot module replacement
2. **Vue DevTools**: Install Vue DevTools browser extension for debugging
3. **Network Tab**: Use browser's Network tab to inspect API calls
4. **Console Logs**: Check console for detailed error messages

## Next Steps

1. Install dependencies (see above)
2. Ensure backend is running
3. Start dev server with `npm run dev`
4. Open browser to `http://localhost:3000`
5. Test image upload and conversion

## Support

For issues or questions:
- Check the backend API documentation
- Review browser console for errors
- Verify network connectivity
