import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    // Cho phép import kiểu "@/features/..." thay vì "../../features/..."
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    port: 5173,
    // Cổng 5173 bận thì báo lỗi và dừng, không tự nhảy sang cổng khác
    strictPort: true,
    // Tự mở trình duyệt khi chạy npm run dev
    open: true,
    // Chuyển tiếp request /api sang Spring Boot để khỏi vướng CORS
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
  },
});
