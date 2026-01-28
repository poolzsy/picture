import axios from 'axios';
import { message } from 'ant-design-vue';
import router from './router';

const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 60000,
    withCredentials: true,
});

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const { data } = response;
        if (data.code !== 200) {
            message.error(data.msg || '请求出错');
            return Promise.reject(data);
        }
        return response;
    },
    (error) => {
        const { response } = error;
        if (response && response.data) {
            const data = response.data;
            if (data.code === 400001) {
                const currentPath = router.currentRoute.value.path;
                if (!currentPath.includes('/user/login') && currentPath !== '/') {
                    router.push('/user/login');
                }
                // 获取当前用户接口静默失败
                if (error.config.url?.includes('/get/login')) {
                    return Promise.reject(data);
                }
            }
            message.error(data.msg || '系统异常');
        }
        return Promise.reject(error);
    }
);

export default request;