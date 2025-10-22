<!-- src/views/LoginView.vue -->
<template>
  <n-card
    title="用户登录"
    style="max-width: 400px; margin: 50px auto;"
    :segmented="{ content: true, footer: 'soft' }"
  >
    <n-form
      ref="formRef"
      :model="formValue"
      :rules="rules"
      label-placement="left"
      label-width="auto"
      require-mark-placement="right-hanging"
      :size="'medium'"
    >
      <n-form-item label="用户名" path="username"> <!-- ⬅️ 修改 path 和 label -->
        <n-input v-model:value="formValue.username" placeholder="请输入用户名" /> <!-- ⬅️ 修改 v-model:value -->
      </n-form-item>
      <n-form-item label="密码" path="password">
        <n-input
          v-model:value="formValue.password"
          type="password"
          show-password-on="mousedown"
          placeholder="请输入密码"
        />
      </n-form-item>
      <n-form-item>
        <n-button
          type="primary"
          :loading="authStore.loading"
          @click="handleLogin"
          block
        >
          登录
        </n-button>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="center">
        还没有账号？
        <router-link to="/register">去注册</router-link>
      </n-space>
    </template>
  </n-card>
</template>

<script setup>
import { ref, reactive } from 'vue';
import {
  NCard, NForm, NFormItem, NInput, NButton, NSpace,
  useMessage
} from 'naive-ui';
import { useAuthStore } from '../stores/auth.js';

const message = useMessage();
const authStore = useAuthStore();

const formRef = ref(null);
const formValue = reactive({
  username: '', // ⬅️ 修改为 username
  password: '',
});

const rules = {
  username: [ // ⬅️ 修改为 username
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
};

const handleLogin = async (e) => {
  e.preventDefault();
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        await authStore.login({
          username: formValue.username, // ⬅️ 确保传递 username
          password: formValue.password,
        });
        // 登录成功消息由 store 统一处理
        // 登录成功后会通过 auth store 中的 action 自动跳转到 /dashboard
      } catch (error) {
        // 错误消息由 store 统一处理
      }
    } else {
      console.log(errors);
      message.error('请填写完整并检查输入');
    }
  });
};
</script>

<style scoped>
</style>
