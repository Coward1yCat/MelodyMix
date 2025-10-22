import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { createDiscreteApi } from 'naive-ui'
import axios from 'axios'
import { useAuthStore } from './stores/auth'; // 导入 authStore 以进行 logout 操作

const app = createApp(App)
const pinia = createPinia()

// 配置 axios 基础 URL
axios.defaults.baseURL = '/api'

// 配置 axios 请求拦截器
axios.interceptors.request.use(
  config => {
    // 定义不需要带 token 的 API 路径（相对于 baseURL）
    const PUBLIC_PATHS = ['/auth/login', '/auth/register'];

    // 检查当前请求的 URL 是否是公共路径之一
    // config.url 可能包含查询参数，所以这里使用 startsWith 进行宽松匹配
    const isPublicPath = PUBLIC_PATHS.some(path => config.url.startsWith(path));

    // 如果不是公共路径，并且 localStorage 中有 JWT Token，就添加到 Authorization 头
    if (!isPublicPath) {
        const token = localStorage.getItem('jwt_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    } else {
        // 如果是公共路径，确保不带 Authorization 头（尽管默认可能没有）
        delete config.headers.Authorization;
    }
    
    return config;
  },
  error => {
    return Promise.reject(error)
  }
)

// 配置 axios 响应拦截器
axios.interceptors.response.use(
  response => response, // 正常响应直接返回
  async error => {
    const authStore = useAuthStore(pinia); // 在响应拦截器中获取 store 实例

    if (error.response) {
      // 捕获 401 未授权错误
      if (error.response.status === 401) {
        // 避免无限循环重试请求
        if (!error.config._retry) {
          error.config._retry = true;
          authStore.logout(); // 清除无效 Token，重定向到登录页
          window.$message.error(error.response.data?.message || '登录状态已失效，请重新登录。');
        }
      } 
      // 捕获 403 Forbidden 错误 (权限不足)
      else if (error.response.status === 403) {
        window.$message.warning(error.response.data?.message || '您没有权限执行此操作。');
        console.warn(`403 Forbidden: ${error.config.url}`, error.response.data);
      }
      // 其他 HTTP 错误 (例如 400 Bad Request, 500 Internal Server Error)
      else {
        // 可以根据需要处理其他错误，或者让组件自己捕获
        // console.error(`API Error: ${error.response.status} - ${error.response.data?.message || error.message}`);
        // if (window.$message) window.$message.error(error.response.data?.message || '请求失败，请稍后重试。');
      }
    } else {
      // 网络错误或其他非 HTTP 响应错误
      // console.error('Network Error or No Response:', error.message);
      // if (window.$message) window.$message.error('网络连接失败，请检查您的网络。');
    }
    
    return Promise.reject(error) // 继续抛出错误，让组件内的 catch 捕获
  }
)

app.use(pinia)
app.use(router)

const { message, notification, dialog, loadingBar } = createDiscreteApi(
  ['message', 'notification', 'dialog', 'loadingBar'],
  {
    // 如果需要，可以在这里配置主题
  }
)

// 将这些服务挂载到全局属性
app.config.globalProperties.$message = message
app.config.globalProperties.$notification = notification
app.config.globalProperties.$dialog = dialog
app.config.globalProperties.$loadingBar = loadingBar

// 额外将这些服务挂载到 window 对象
window.$message = message
window.$notification = notification
window.$dialog = dialog
window.$loadingBar = loadingBar

app.mount('#app')

