import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'
import { fileURLToPath, URL } from 'url'

export default defineConfig({
  plugins: [
    vue(),
    vuetify({ autoImport: true }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 8000,
    proxy: {
      '^/client($|/.*)': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '^/order($|/.*)': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws-notifications': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },
})
