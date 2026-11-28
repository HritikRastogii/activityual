import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

const proxy = (target: string) => ({
  target,
  changeOrigin: true,
  rewrite: (path: string) => path.substring(path.indexOf('/', 1))
});

export default defineConfig({
  plugins: [sveltekit()],
  server: {
    proxy: {
      '/auth-api': proxy('http://localhost:8081'),
      '/activity-api': proxy('http://localhost:8082'),
      '/tracking-api': proxy('http://localhost:8083'),
      '/analytics-api': proxy('http://localhost:8084'),
      '/recommendation-api': proxy('http://localhost:8085')
    }
  }
});
