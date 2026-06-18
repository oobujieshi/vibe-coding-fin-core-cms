<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#304156">
      <div style="color:#fff;text-align:center;padding:20px 0;font-size:18px;font-weight:bold">FinCoreCms</div>
      <el-menu :default-active="route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router>
        <el-menu-item index="/"><el-icon><HomeFilled /></el-icon> 首页</el-menu-item>
        <el-sub-menu index="orders-group" v-if="auth.hasRole('FINANCE') || auth.hasRole('BIZ') || auth.hasRole('ADMIN')">
          <template #title><el-icon><Document /></el-icon> 订单结算</template>
          <el-menu-item index="/orders">订单管理</el-menu-item>
          <el-menu-item index="/fee-rules">费率规则</el-menu-item>
          <el-menu-item index="/settlements">结算管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="payment-group" v-if="auth.hasRole('FINANCE') || auth.hasRole('ADMIN')">
          <template #title><el-icon><Money /></el-icon> 收付款</template>
          <el-menu-item index="/receipts">收款管理</el-menu-item>
          <el-menu-item index="/payments">付款管理</el-menu-item>
          <el-menu-item index="/bills">账单管理</el-menu-item>
          <el-menu-item index="/approvals">审批管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="fund-group" v-if="auth.hasRole('FINANCE') || auth.hasRole('ADMIN')">
          <template #title><el-icon><Coin /></el-icon> 资金流水</template>
          <el-menu-item index="/accounts">银行账户</el-menu-item>
          <el-menu-item index="/transactions">流水记录</el-menu-item>
          <el-menu-item index="/reconciliations">对账管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/reports" v-if="auth.hasRole('FINANCE') || auth.hasRole('ADMIN')">
          <el-icon><DataAnalysis /></el-icon> 报表
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="border-bottom:1px solid #e4e7ed;display:flex;align-items:center;justify-content:flex-end">
        <span>{{ auth.userInfo?.username || '' }}</span>
        <el-button type="danger" text style="margin-left:16px" @click="handleLogout">退出</el-button>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
function handleLogout() { auth.logout(); router.push('/login') }
</script>
