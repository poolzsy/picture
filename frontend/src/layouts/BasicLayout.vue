<template>
    <div id="basicLayout">
        <a-layout style="min-height: 100vh">
            <a-layout-header class="header">
                <GlobalHeader />
            </a-layout-header>
            <a-layout-content class="content">
                <router-view />
            </a-layout-content>
            <a-layout-footer class="footer">
                郄汐云图库 ©2026 Created by 郄汐
            </a-layout-footer>
        </a-layout>
    </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<style scoped>
#basicLayout .header{
    background: white;
    padding-inline: 20px;
}

#basicLayout .content{
    padding: 20px;
    background: linear-gradient(to right, #efefef, #fff);
    margin-bottom: 28px;
}

#basicLayout .footer{
    background: #efefef;
    padding: 10px;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    text-align: center;
}
</style>