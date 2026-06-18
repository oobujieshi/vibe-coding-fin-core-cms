<template>
  <div class="page-container">
    <div class="page-header">
      <h2>报表中心</h2>
      <el-radio-group v-model="tab" size="default">
        <el-radio-button label="income">收支明细</el-radio-button>
        <el-radio-button label="balance">资金余额</el-radio-button>
        <el-radio-button label="settle">结算统计</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" style="min-height:300px">
      <!-- Income/Expense -->
      <template v-if="tab==='income'">
        <h3>收支明细</h3>
        <p>总收入: ¥{{ data.totalIncome }}  总支出: ¥{{ data.totalExpense }}  净流量: ¥{{ data.netFlow }}</p>
        <el-table :data="data.items || []" border stripe size="small" max-height="400">
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column label="收入" width="120"><template #default="{row}">¥{{ row.income }}</template></el-table-column>
          <el-table-column label="支出" width="120"><template #default="{row}">¥{{ row.expense }}</template></el-table-column>
        </el-table>
      </template>

      <!-- Balance -->
      <template v-if="tab==='balance'">
        <h3>资金余额</h3>
        <el-descriptions border :column="2">
          <el-descriptions-item label="当前余额">¥{{ data.currentBalance }}</el-descriptions-item>
          <el-descriptions-item label="交易笔数">{{ data.transactionCount }}</el-descriptions-item>
          <el-descriptions-item label="总流入">¥{{ data.totalInflow }}</el-descriptions-item>
          <el-descriptions-item label="总流出">¥{{ data.totalOutflow }}</el-descriptions-item>
        </el-descriptions>
      </template>

      <!-- Settlement Stats -->
      <template v-if="tab==='settle'">
        <h3>结算统计</h3>
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单总数">{{ data.totalOrders }}</el-descriptions-item>
          <el-descriptions-item label="已结算金额">¥{{ data.settledAmount }}</el-descriptions-item>
          <el-descriptions-item label="已结算笔数">{{ data.settledCount }}</el-descriptions-item>
          <el-descriptions-item label="已付款金额">¥{{ data.paidAmount }}</el-descriptions-item>
          <el-descriptions-item label="待结算金额">¥{{ data.pendingAmount }}</el-descriptions-item>
          <el-descriptions-item label="已收款金额">¥{{ data.receiptAmount }}</el-descriptions-item>
          <el-descriptions-item label="待结算笔数">{{ data.pendingCount }}</el-descriptions-item>
          <el-descriptions-item label="已关闭笔数">{{ data.closedCount }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import request from '@/utils/request'

const tab = ref('income'); const loading = ref(false)
const data = reactive({})

function loadReport() {
  loading.value = true
  const api = tab.value === 'income' ? '/reports/income-expense' : tab.value === 'balance' ? '/reports/balance' : '/reports/settlement-stats'
  request.get(api).then(r => { Object.assign(data, r) }).finally(() => loading.value = false)
}
watch(tab, loadReport)
onMounted(loadReport)
</script>
