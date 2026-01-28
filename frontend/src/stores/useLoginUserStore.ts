import { ref } from 'vue';
import { defineStore } from 'pinia';
import { getLoginUser } from '@/api/userController';

//登录用户信息
export const useLoginUserStore = defineStore('loginUser', () => {
    const loginUser = ref<API.LoginUserVO>(
        { userName: '未登录' }
    );

    //获取登录用户信息
    async function fetchLoginUser() {
        try {
            const res = await getLoginUser();
            
            if (res.data.code === 200 && res.data.data) {
                loginUser.value = res.data.data;
            } else {
                loginUser.value = { userName: '未登录' };
            }
        } catch (error) {
            loginUser.value = { userName: '未登录' };
        }
    }

    function setLoginUser(newLoginUser: API.LoginUserVO) {
        loginUser.value = newLoginUser;
    }
    return { loginUser, fetchLoginUser, setLoginUser };
})