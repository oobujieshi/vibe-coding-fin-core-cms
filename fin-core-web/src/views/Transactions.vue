<template>
  <div class="page-container">
    <div class="page-header">
      <h2>流水记录</h2>
      <div>
        <el-button type="primary" @click="showCreate">手动录入</el-button>
        <el-button @click="showSync">银行同步(Mock)</el-button>
      </div>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="transNo" label="流水号" width="200" />
      <el-table-column prop="accountId" label="账户ID" width="80" />
      <el-table-column prop="transTypeDesc" label="类型" width="70" />
      <el-table-column prop="amount" label="金额" width="110"><template #default="{row}">¥{{ row.amount }}</template></el-table-column>
      <el-table-column prop="balanceAfter" label="余额" width="110"><template #default="{row}">¥{{ row.balanceAfter }}</template></el-table-column>
      <el-table-column prop="counterparty" label="对方" />
      <el-table-column prop="sourceDesc" label="来源" width="90" />
      <el-table-column prop="transTime" label="时间" width="170" />
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog title="录入流水" v-model="dialogVisible" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账户ID"><el-input v-model="form.accountId" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.transType" style="width:100%"><el-option label="收入" :value="1" /><el-option label="支出" :value="2" /></el-select></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="对方"><el-input v-model="form.counterparty" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">录入</el-button></template>
    </el-dialog>

    <el-dialog title="银行同步" v-model="syncVisible" width="350px">
      <el-form label-width="80px">
        <el-form-item label="账户ID"><el-input v-model="syncAccountId" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="syncVisible=false">取消</el-button><el-button type="primary" @click="doSync">同步</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const tableData = ref([]); const loading = ref(false)
const page = ref(1); const size = ref(20); const total = ref(0)
const dialogVisible = ref(false)
const form = reactive({ accountId:'', transType:1, amount:0, counterparty:'', summary:'' })
const syncVisible = ref(false); const syncAccountId = ref('')

function loadData() {
  loading.value = true
  request.get('/transactions', {params:{page:page.value, size:size.value}}).then(r => { tableData.value = r.records||[]; total.value = Number(r.total)||0 }).finally(() => loading.value = false)
}
function showCreate() { Object.assign(form, {accountId:'',transType:1,amount:0,counterparty:'',summary:''}); dialogVisible.value = true }
function save() {
  request.post('/transactions', { ...form, accountId: Number(form.accountId) }).then(() => { dialogVisible.value = false; loadData(); ElMessage.success('录入成功') })
}
function showSync() { syncAccountId.value = ''; syncVisible.value = true }
function doSync() {
  request.post('/transactions/batch-sync', { accountId: Number(syncAccountId.value) }).then(r => { syncVisible.value = false; loadData(); ElMessage.success(`同步${r.importedCount}条`) })
}
onMounted(loadData)
</script>
