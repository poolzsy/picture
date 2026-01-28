<template>
    <div class="userManage">
        <!-- 登录表单 -->
        <a-form :model="searchParams" layout="inline" @finish="doSearch">
            <a-form-item label="账号">
                <a-input v-model:value="searchParams.userAccount" placeholder="账户" allow-clear />
            </a-form-item>
            <a-form-item label="用户名">
                <a-input v-model:value="searchParams.userName" placeholder="用户名" allow-clear />
            </a-form-item>
            <a-form-item>
                <a-button type="primary" html-type="submit">查询</a-button>
            </a-form-item>
        </a-form>
        <div style="margin-top: 16px;"></div>
        <!-- 表格列定义 -->
        <a-table :columns="columns" :data-source="dataList" :pagination="pagination" @change="doTableChange">
            <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'userAvatar'">
                    <a-avatar :src="record.userAvatar" />
                </template>
                <template v-else-if="column.dataIndex === 'userRole'">
                    <div v-if="record.userRole === 'admin'">
                        <a-tag color="green">管理员</a-tag>
                    </div>
                    <div v-else>
                        <a-tag color="blue">普通用户</a-tag>
                    </div>
                </template>
                <template v-else-if="column.dataIndex === 'createTime'">
                    {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                </template>
                <template v-else-if="column.key === 'action'">
                    <a-button>编辑</a-button>
                    <a-button danger @click="doDelete(record.id)">删除</a-button>
                </template>
            </template>
        </a-table>
    </div>
</template>
<script lang="ts" setup>
import { deleteUser, listUserVOByPage } from '@/api/userController';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { computed, onMounted, reactive, ref } from 'vue';

// 表格列定义
const columns = [
    {
        title: 'Id',
        dataIndex: 'id',
    },
    {
        title: '账户',
        dataIndex: 'userAccount',
    },
    {
        title: '用户名',
        dataIndex: 'userName',
    },
    {
        title: '头像',
        dataIndex: 'userAvatar',
    },
    {
        title: '简介',
        dataIndex: 'userProfile',
    },
    {
        title: '用户角色',
        dataIndex: 'userRole',
    },
    {
        title: '创建时间',
        dataIndex: 'createTime',
    },
    {
        title: '操作',
        key: 'action',
    },
];

// 数据
const dataList = ref<API.UserVO[]>([]);
const total = ref(0);

// 查询参数
const searchParams = reactive<API.UserQueryRequest>({
    current: 1,
    pageSize: 10,
    sortField: 'createTime',
    sortOrder: 'ascend',
});

// 分页
const pagination = computed(() => {
    return {
        current: searchParams.current,
        pageSize: searchParams.pageSize,
        total: total.value,
        showSizeChanger: true,
        showTotal: (total: number) => `总共 ${total} 条`,
    }
});

// 表格变化事件
const doTableChange = (page: any) => {
    searchParams.current = page.current;
    searchParams.pageSize = page.pageSize;
    fetchData();
}

// 获取数据
const fetchData = async () => {
    try {
        const res: any = await listUserVOByPage({
            ...searchParams,
        });
        dataList.value = res.data.data.records || [];
        total.value = Number(res.data.data.total) || 0;
    }
    catch (error) {
        dataList.value = [];
        total.value = 0;
    }
}

// 搜索
const doSearch = () => {
    searchParams.current = 1;
    fetchData();
}

// 删除
const doDelete = async (id: number) => {
    if (!id) {
        return;
    }
    const res = await deleteUser({ id });
    message.success('删除成功');
    fetchData();
}

// 页面加载时获取数据
onMounted(() => {
    fetchData();
});
</script>

<style></style>