import { defineStore } from 'pinia';
import axios from 'axios';
import { ref, computed } from 'vue';
import router from '../router';

export const useAuthStore = defineStore('auth', () => {
    // === 状态 (State) ===
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'));
    const token = ref(localStorage.getItem('jwt_token') || null);
    const isLoading = ref(false);
    const authError = ref(null);
    const userProfile = ref(null); // 用于存储当前用户的详细个人资料

    // === 计算属性 (Getters) ===
    const isAuthenticated = computed(() => !!token.value && !!user.value);
    const isLoggedIn = computed(() => !!token.value);
    const isAdmin = computed(() => user.value && user.value.role === 'ADMIN');
    const isCompany = computed(() => user.value && user.value.role === 'COMPANY');
    const isNormalUser = computed(() => user.value && user.value.role === 'USER');
    const getUserRole = computed(() => user.value ? user.value.role : null);

    // === 动作 (Actions) ===

    // 辅助函数：保存认证信息
    function _setAuth(userData, jwtToken) {
        user.value = userData;
        token.value = jwtToken;
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('jwt_token', jwtToken);
        // axios 拦截器已处理 Authorization 头，这里无需直接手动设置
    }

    // 辅助函数：清除认证信息
    function _clearAuth() {
        user.value = null;
        token.value = null;
        userProfile.value = null;
        localStorage.removeItem('user');
        localStorage.removeItem('jwt_token');
        // delete axios.defaults.headers.common['Authorization']; // 拦截器处理，或在 logout 中处理
    }

    // 初始化认证：检查是否存在 token 并尝试获取用户信息
    async function initializeAuth() {
        if (token.value && !user.value) {
            isLoading.value = true;
            try {
                // ✅ 路径修正：使用后端 /api/user/me 接口获取当前用户信息
                const response = await axios.get('/user/me'); 
                user.value = response.data;
                localStorage.setItem('user', JSON.stringify(response.data));
            } catch (error) {
                console.error('初始化认证失败或获取用户信息失败:', error);
                // 401/403 错误会被 main.js 的响应拦截器处理，并调用 logout
                // 这里只需确保清除状态并重定向，避免重复弹窗
                if (!router.currentRoute.value.path.includes('/login')) { // 避免在登录页时再次跳转/提示
                    _clearAuth();
                    if (window.$message) window.$message.error('会话已过期或无效，请重新登录。');
                    router.push('/login');
                }
            } finally {
                isLoading.value = false;
            }
        } else if (!token.value) {
            _clearAuth(); // 确保没有 token 时状态也是清理的
        }
    }

    // 用户注册
    async function register(userData) {
        isLoading.value = true;
        authError.value = null;
        try {
            const response = await axios.post('/auth/register', userData);
            if (window.$message) window.$message.success('注册成功！请登录。');
            router.push('/login');
            return response.data;
        } catch (error) {
            console.error('注册失败:', error.response?.data || error.message);
            authError.value = error.response?.data?.message || '注册失败，请重试。';
            if (window.$message) window.$message.error(authError.value);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // 用户登录
    async function login(credentials) {
        isLoading.value = true;
        authError.value = null;
        try {
            // 1. 调用登录接口，获取 JWT Token
            const response = await axios.post('/auth/login', {
                username: credentials.username,
                password: credentials.password
            });
            const { token: jwtToken } = response.data;

            // 2. 将 token 临时保存到 localStorage，以便后续请求拦截器能够获取到
            localStorage.setItem('jwt_token', jwtToken); 

            // 3. 登录成功后，立即调用 /api/user/me 获取用户详细信息
            // 这里的 headers 显式设置Authorization，是为了确保即使在axios拦截器执行前也能保证这个请求带上新的token
            const userProfileResponse = await axios.get('/user/me', { 
                headers: { 
                    Authorization: `Bearer ${jwtToken}`
                }
            });
            
            // 4. 使用获取到的用户资料和 token 设置认证状态
            _setAuth(userProfileResponse.data, jwtToken); 

            if (window.$message) window.$message.success('登录成功！');
            router.push('/dashboard');
            return response.data;
        } catch (error) {
            console.error('登录失败:', error.response?.data?.message || error.message);
            authError.value = error.response?.data?.message || '用户名或密码不正确。';
            if (window.$message) window.$message.error(authError.value);
            _clearAuth(); // 登录失败也清除一下，防止脏数据
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // 用户登出
    async function logout() {
        _clearAuth();
        if (window.$message) window.$message.info('您已成功登出！');
        router.push('/login');
    }

    // 假设后端 /api/user/me 是用于 GET (获取) 而不是 PUT (更新) 个人资料
    // 如果需要更新，后端需要提供独立的 PUT /api/user 或 PUT /api/user/{userId} 接口
    async function fetchUserProfile() {
        isLoading.value = true;
        try {
            const response = await axios.get('/user/me'); // ✅ 假设后端 API 路径为 /api/user/me
            userProfile.value = response.data;
            if (user.value) { // 将个人资料同步到简略的 user 状态
                user.value.username = response.data.username || user.value.username;
                user.value.email = response.data.email || user.value.email;
                user.value.role = response.data.role || user.value.role;
                localStorage.setItem('user', JSON.stringify(user.value));
            }
            return response.data;
        } catch (error) {
            console.error('获取用户资料失败:', error);
            const errorMsg = error.response?.data?.message || '获取用户资料失败';
            if (window.$message) window.$message.error(errorMsg);
            userProfile.value = null;
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // ⚠️ 占位符：后端目前没有对应的 PUT /api/user/:id 或 /api/user/me 接口用于更新
    // 如果实现此功能，需要在后端 UserController 中添加相应接口，并修正此处的路径
    async function updateUserProfile(userData) {
        isLoading.value = true;
        try {
            // ✅ 占位符：假设后端 API 路径为 /user/me 并且支持 PUT 方法
            // 并且后端接收的 userData 结构是匹配的
            const response = await axios.put('/user/me', userData); 
            userProfile.value = response.data; 
            // 同时更新 localStorage 中的简略 user 信息和 store 中的 user 状态
            if (user.value) {
                user.value.username = response.data.username || user.value.username;
                user.value.email = response.data.email || user.value.email;
                // 根据后端返回的数据更新 user 状态
                localStorage.setItem('user', JSON.stringify(user.value));
            }
            if (window.$message) window.$message.success('个人资料更新成功！');
            return response.data;
        } catch (error) {
            console.error('更新用户资料失败:', error);
            const errorMsg = error.response?.data?.message || '更新用户资料失败';
            if (window.$message) window.$message.error(errorMsg);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // ⚠️ 占位符：后端目前没有对应的 PUT /api/user/change-password 接口用于修改密码
    // 如果实现此功能，需要在后端 UserController 中添加相应接口，并修正此处的路径
    async function changePassword(oldPassword, newPassword) {
        isLoading.value = true;
        try {
            // ✅ 占位符：假设后端 API 路径为 /user/change-password 并且支持 PUT 方法
            await axios.put('/user/change-password', { oldPassword, newPassword }); 
            if (window.$message) window.$message.success('密码修改成功！');
            return true;
        } catch (error) {
            console.error('修改密码失败:', error);
            const errorMsg = error.response?.data?.message || '修改密码失败';
            if (window.$message) window.$message.error(errorMsg);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        user,
        token,
        isLoading,
        authError,
        userProfile,
        isAuthenticated,
        isLoggedIn,
        isAdmin,
        isCompany,
        isNormalUser,
        getUserRole,
        initializeAuth,
        register,
        login,
        logout,
        fetchUserProfile,
        updateUserProfile,
        changePassword,
    };
});
