import { message } from "ant-design-vue";
import router from "./router";
import { useLoginUserStore } from "./stores/useLoginUserStore";

// 防止重复获取登录用户信息
let firstFetchLoginUser: boolean = true;

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
    const loginUserStore = useLoginUserStore();
    let loginUser = loginUserStore.loginUser;
    // 确保页面加载时只获取一次登录用户信息
    if (firstFetchLoginUser) {
        await loginUserStore.fetchLoginUser();
        firstFetchLoginUser = false;
        loginUser = loginUserStore.loginUser;
    }
    const toUrl = to.fullPath;
    if(toUrl.startsWith('/admin')) {
        if(!loginUser || loginUser.userRole !== 'admin'){
            message.error('无权限访问');
            next('/user/login?redirect=${to.fullPath');
            return;
        }
    }
    next();
})