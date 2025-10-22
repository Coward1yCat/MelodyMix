<!-- src/views/RegisterView.vue -->
<template>
  <n-card
    title="注册新用户"
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
      <n-form-item label="用户名" path="username">
        <n-input v-model:value="formValue.username" placeholder="请输入用户名" />
      </n-form-item>
      <n-form-item label="邮箱" path="email">
        <n-input v-model:value="formValue.email" placeholder="请输入邮箱" />
      </n-form-item>
      <n-form-item label="密码" path="password">
        <n-input
          v-model:value="formValue.password"
          type="password"
          show-password-on="mousedown"
          placeholder="请输入密码"
        />
      </n-form-item>
      <n-form-item label="确认密码" path="confirmPassword">
        <n-input
          v-model:value="formValue.confirmPassword"
          type="password"
          show-password-on="mousedown"
          placeholder="请再次输入密码"
        />
      </n-form-item>
      <n-form-item label="角色" path="role">
        <n-select
          v-model:value="formValue.role"
          :options="roleOptions"
          placeholder="请选择角色"
        />
      </n-form-item>
      <n-form-item label="公司名称" path="companyName" v-if="formValue.role === 'COMPANY'">
        <n-input v-model:value="formValue.companyName" placeholder="请输入公司名称" />
      </n-form-item>
      <n-form-item label="公司地址" path="companyAddress" v-if="formValue.role === 'COMPANY'"> <!-- 新增公司地址字段 -->
        <n-input v-model:value="formValue.companyAddress" placeholder="请输入公司地址" />
      </n-form-item>
      <n-form-item>
        <n-button
          type="primary"
          :loading="authStore.loading"
          @click="handleRegister"
          block
        >
          注册
        </n-button>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="center">
        已经有账号了？
        <router-link to="/login">去登录</router-link>
      </n-space>
    </template>
  </n-card>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import {
  NCard, NForm, NFormItem, NInput, NButton, NSelect, NSpace,
  useMessage
} from 'naive-ui';
import { useAuthStore } from '../stores/auth.js';

const message = useMessage();
const authStore = useAuthStore();

const formRef = ref(null);
const formValue = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: null, // admin, user, company
  companyName: '', // 仅当角色为 company 时需要
  companyAddress: '', // ⬅️ 新增公司地址字段
});

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },    // ⬅️ 值改为大写
  { label: '普通用户', value: 'USER' },      // ⬅️ 值改为大写
  { label: '公司', value: 'COMPANY' }     // ⬅️ 值改为大写
];


const validatePasswordSame = (rule, value) => {
  return value === formValue.password;
};

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入合法的邮箱地址', trigger: ['blur', 'input'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '长度在 6 到 30 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: validatePasswordSame, // Use a function for custom validation
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ],
  role: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
  companyName: [
    {
      required: (rule) => formValue.role === 'COMPANY', // ⬅️ 只有当角色是 COMPANY 时才必填
      message: '请输入公司名称',
      trigger: 'blur'
    }
  ],
  companyAddress: [ // ⬅️ 新增公司地址规则
    {
      required: (rule) => formValue.role === 'COMPANY',
      message: '请输入公司地址',
      trigger: 'blur'
    }
  ]
};

// 监听角色变化，如果不是 company，则清空 companyName 和 companyAddress
watch(() => formValue.role, (newRole) => {
  if (newRole !== 'COMPANY') { // ⬅️ 检查大写 'COMPANY'
    formValue.companyName = '';
    formValue.companyAddress = ''; // ⬅️ 清空公司地址
  }
});


const handleRegister = async (e) => {
  e.preventDefault();
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        const userData = {
          username: formValue.username,
          email: formValue.email,
          password: formValue.password,
          role: formValue.role,
          companyName: formValue.role === 'COMPANY' ? formValue.companyName : null, // ⬅️ 检查大写 'COMPANY'
          companyAddress: formValue.role === 'COMPANY' ? formValue.companyAddress : null // ⬅️ 传递公司地址
        };
        await authStore.register(userData);
        // 注册成功消息由 store 统一处理
        // router.push('/login'); // 注册成功后 store 会自动跳转
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
