<!-- src/views/ProfileView.vue -->
<template>
  <div class="user-profile-container">
    <n-spin :show="authStore.isLoading" size="large">
      <n-card class="profile-card" title="个人中心" :bordered="false">
        <template #header-extra>
            <n-button 
                type="primary" 
                size="small" 
                @click="isEditingProfile = !isEditingProfile"
                v-if="!isEditingPassword"
            >
                <template #icon>
                    <n-icon>
                        <svg v-if="!isEditingProfile" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83l3.75 3.75l1.83-1.83ZM3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25Z" fill="currentColor"></path></svg>
                        <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12L19 6.41Z" fill="currentColor"></path></svg>
                    </n-icon>
                </template>
                {{ isEditingProfile ? '取消编辑' : '编辑资料' }}
            </n-button>
        </template>

        <n-tabs type="line" animated @update:value="handleTabChange">
          <n-tab-pane name="profile" tab="我的资料">
            <n-space vertical :size="20">
              <!-- 用户头像和基本信息 -->
              <n-space align="center">
                <n-avatar :size="80" round :src="authStore.userProfile?.avatarUrl || 'https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg'" />
                <div>
                  <h2 class="username">{{ profileForm.username }}</h2>
                  <n-text depth="3">{{ getRoleDisplay(profileForm.role) }}</n-text>
                </div>
              </n-space>

              <n-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-placement="left"
                label-width="auto"
                :disabled="!isEditingProfile"
                @submit.prevent="handleUpdateProfile"
              >
                <n-form-item label="用户名" path="username">
                  <n-input v-model:value="profileForm.username" placeholder="请输入用户名" />
                </n-form-item>
                <n-form-item label="电子邮件" path="email">
                  <n-input v-model:value="profileForm.email" placeholder="请输入电子邮件" />
                </n-form-item>
                <!-- 如果后端支持，可以添加头像上传 -->
                <!-- <n-form-item label="头像" path="avatarUrl">
                  <n-upload
                      action="/api/upload/avatar"
                      :headers="{ Authorization: `Bearer ${authStore.token}` }"
                      :max="1"
                      @before-upload="beforeUpload"
                      @finish="handleUploadFinish"
                      @remove="handleUploadRemove"
                  >
                      <n-button :disabled="!isEditingProfile">上传头像</n-button>
                  </n-upload>
                </n-form-item> -->

                <n-form-item v-if="isEditingProfile">
                  <n-button 
                      type="primary" 
                      attr-type="submit"
                      :loading="authStore.isLoading"
                      block
                  >
                    保存资料
                  </n-button>
                </n-form-item>
              </n-form>
            </n-space>
          </n-tab-pane>

          <n-tab-pane name="password" tab="修改密码">
            <n-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-placement="left"
              label-width="auto"
              @submit.prevent="handleChangePassword"
            >
              <n-form-item label="旧密码" path="oldPassword">
                <n-input
                  v-model:value="passwordForm.oldPassword"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入旧密码"
                />
              </n-form-item>
              <n-form-item label="新密码" path="newPassword">
                <n-input
                  v-model:value="passwordForm.newPassword"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入新密码"
                />
              </n-form-item>
              <n-form-item label="确认新密码" path="confirmPassword">
                <n-input
                  v-model:value="passwordForm.confirmPassword"
                  type="password"
                  show-password-on="click"
                  placeholder="请再次输入新密码"
                />
              </n-form-item>
              <n-form-item>
                <n-button
                  type="primary"
                  attr-type="submit"
                  :loading="authStore.isLoading"
                  block
                >
                  修改密码
                </n-button>
              </n-form-item>
            </n-form>
          </n-tab-pane>
        </n-tabs>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  NSpin, NCard, NSpace, NAvatar, NText, NButton, NIcon, NTabs, NTabPane,
  NForm, NFormItem, NInput, /* NUpload, NTag, NGradientText, */ useMessage
} from 'naive-ui';
import { useAuthStore } from '@/stores/auth.js';

const router = useRouter();
const message = useMessage();
const authStore = useAuthStore();

// 个人资料表单
const profileFormRef = ref(null);
const profileForm = ref({
  username: '',
  email: '',
  role: '',
  avatarUrl: ''
});
const isEditingProfile = ref(false); // 是否处于编辑资料状态
const isEditingPassword = ref(false); // 是否处于修改密码tab

// 密码修改表单
const passwordFormRef = ref(null);
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 资料表单校验规则
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: ['blur', 'input'] },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: ['blur', 'input'] }
  ],
  email: [
    { required: true, message: '请输入电子邮件', trigger: ['blur', 'input'] },
    { type: 'email', message: '请输入正确的电子邮件格式', trigger: ['blur', 'input'] }
  ]
};

// 密码表单校验规则
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        return value === passwordForm.value.newPassword;
      },
      message: '两次输入的新密码不一致',
      trigger: ['blur', 'input']
    }
  ]
};

// 获取用户资料
const fetchProfileData = async () => {
  if (!authStore.isLoggedIn) {
    message.warning('请先登录。');
    router.push('/login');
    return;
  }
  try {
    await authStore.fetchUserProfile();
    // 监听器会处理 profileForm 的更新
  } catch (error) {
    // 错误在 store 中处理
  }
};

// 处理更新个人资料
const handleUpdateProfile = async () => {
  profileFormRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        await authStore.updateUserProfile({
          username: profileForm.value.username,
          email: profileForm.value.email,
          // avatarUrl: profileForm.value.avatarUrl, // 如果有上传功能
        });
        isEditingProfile.value = false; // 更新成功后退出编辑状态
      } catch (error) {
        // 错误在 store 中处理
      }
    } else {
      message.error('请检查表单填写是否正确。');
    }
  });
};

// 处理修改密码
const handleChangePassword = async () => {
  passwordFormRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        await authStore.changePassword(
          passwordForm.value.oldPassword,
          passwordForm.value.newPassword
        );
        // 密码修改成功后清空表单
        passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
      } catch (error) {
        // 错误在 store 中处理
      }
    } else {
      message.error('请检查表单填写是否正确。');
    }
  });
};

// 角色显示转换
const getRoleDisplay = (role) => {
    switch (role) {
        case 'ADMIN': return '管理员';
        case 'USER': return '普通用户';
        case 'COMPANY': return '公司用户';
        default: return '未知角色';
    }
};

const handleTabChange = (value) => {
    isEditingPassword.value = value === 'password';
    if (value === 'profile') {
        // 当切换回资料tab时，如果之前在编辑，重置编辑状态并重新填充表单
        if (isEditingProfile.value) {
            isEditingProfile.value = false;
            if (authStore.userProfile) { // 从 store 重新加载，以防用户取消编辑
                profileForm.value = { ...authStore.userProfile };
            }
        }
    }
};

// 组件挂载时获取用户资料
onMounted(() => {
  fetchProfileData();
});

// 监听 authStore.userProfile 变化以同步表单
watch(() => authStore.userProfile, (newProfile) => {
    if (newProfile) {
        // 只有当不在编辑状态时才自动同步，防止用户正在输入时被覆盖
        if (!isEditingProfile.value) {
            profileForm.value = { ...newProfile };
        }
    }
}, { deep: true });

// 监听 isEditingProfile 变化，如果取消编辑，则重置表单
watch(isEditingProfile, (newValue) => {
    if (!newValue && authStore.userProfile) {
        profileForm.value = { ...authStore.userProfile }; // 退出编辑时，重置表单为 store 中的数据
        profileFormRef.value?.restoreValidation(); // 并且清除校验错误
    }
});

// 上传相关 (如果后端支持)
// const beforeUpload = (file) => {
//   // 可以在这里进行文件类型和大小校验
//   if (file.file.type !== 'image/jpeg' && file.file.type !== 'image/png') {
//     message.error('只能上传 JPG/PNG 格式的图片文件');
//     return false;
//   }
//   if (file.file.size > 2 * 1024 * 1024) {
//     message.error('图片文件大小不能超过 2MB');
//     return false;
//   }
//   return true;
// };
// const handleUploadFinish = ({ file, event }) => {
//   const res = JSON.parse(event.target.response);
//   if (res.code === 200 && res.data && res.data.url) {
//     profileForm.value.avatarUrl = res.data.url;
//     // 可能还需要调用 authStore.updateUserProfile 来保存头像 URL 到后端
//     message.success('头像上传成功');
//   } else {
//     message.error('头像上传失败');
//   }
// };
// const handleUploadRemove = () => {
//   profileForm.value.avatarUrl = ''; // 清空头像URL
//   // 可能还需要调用 authStore.updateUserProfile 来移除头像
//   message.info('头像已移除');
// };
</script>

<style scoped>
.user-profile-container {
  max-width: 800px;
  margin: 30px auto;
  padding: 20px;
}

.profile-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 增加一点阴影效果 */
  border-radius: 12px;
}

.username {
  font-size: 1.8em;
  font-weight: bold;
  margin: 0;
}

/* 调整表单项的间距，如果需要 */
.n-form-item {
  margin-bottom: 20px;
}
</style>
