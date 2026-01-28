<template>
    <div id="globalHeader">
        <a-row :wrap="false">
            <a-col flex="200px">
                <router-link to="/">
                    <div class="title-bar">
                        <img src="../assets/logo.png" alt="logo" class="logo"></img>
                        <div class="title">郄汐云图库</div>
                    </div>
                </router-link>
            </a-col>
            <a-col flex="auto">
                <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click="doMenuClick" />
            </a-col>
            <!-- 用户信息 -->
            <a-col flex="120px">
                <div class="user-login-status">
                    <div v-if="loginUserStore.loginUser.id">
                        <a-dropdown>
                            <a-space>
                                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                                {{ loginUserStore.loginUser.userName ?? '用户' }}
                            </a-space>
                            <template #overlay>
                                <a-menu>
                                    <a-menu-item key="logout" @click="doLogout"><LogoutOutlined />退出登录</a-menu-item>
                                </a-menu>
                            </template>
                        </a-dropdown>
                    </div>
                    <div v-else>
                        <a-button type="primary" @click="router.push('/user/login')">登录</a-button>
                    </div>
                </div>
            </a-col>
        </a-row>
    </div>
</template>

<script setup lang="ts">
import { h, ref } from 'vue';
import { HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue';
import { message, type MenuProps } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useLoginUserStore } from '@/stores/useLoginUserStore';
import { userLogout } from '@/api/userController';

const loginUserStore = useLoginUserStore();

const items = ref<MenuProps['items']>([
    {
        key: '/',
        icon: () => h(HomeOutlined),
        label: '主页',
    },
    {
        key: '/admin/userManage',
        label: '用户管理',
    },
    {
        key: 'others',
        label: h('a', { href: 'https://tanghc.xyz', target: '_blank' }, 'Blog'),
    },
]);

const router = useRouter();

// 当前高亮菜单
const current = ref<string[]>([]);
router.afterEach((to, from, next) => {
    current.value = [to.path];
});

// 路由跳转
const doMenuClick = ({ key }: { key: string }) => {
    router.push({
        path: key
    });
}

// 退出登录
const doLogout = async () => {
    const res = await userLogout();
    loginUserStore.setLoginUser({ userName: '未登录' });
    message.success('退出成功');
    router.push('/user/login');
}

</script>

<style scoped>
#globalHeader .title-bar {
    display: flex;
    align-items: center;
}

.title {
    color: black;
    font-size: 18px;
    margin-left: 5px;
}

.logo {
    height: 48px;
    border-radius: 50%;
}
</style>