<template>
  <div class="page-container">
    <div class="page-header">
      <h2>银行账户</h2>
      <el-button type="primary" @click="showCreate">新建账户</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bankName" label="银行" />
      <el-table-column prop="accountName" label="账户名称" />
      <el-table-column prop="accountNo" label="账号" width="150" />
      <el-table-column prop="balance" label="余额" width="120"><template #default="{row}">¥{{ row.balance }}</template></el-table-column>
      <el-table-column prop="currency" label="币种" width="70" />
      <el-table-column prop="accountStatusDesc" label="状态" width="70" />
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button size="small" @click="showEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog :title="editId?'编辑账户':'新建账户'" v-model="dialogVisible" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="银行名称"><el-input v-model="form.bankName" /></el-form-item>
        <el-form-item label="账户名称"><el-input v-model="form.accountName" /></el-form-item>
        <el-form-item label="账号"><el-input v-model="form.accountNo" /></el-form-item>
        <el-form-item label="余额"><el-input-number v-model="form.balance" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="币种"><el-input v-model="form.currency" placeholder="CNY" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const tableData = ref([]); const loading = ref(false)
const page = ref(1); const size = ref(20); const total = ref(0)
const dialogVisible = ref(false); const editId = ref(null)
const form = reactive({ bankName:'', accountName:'', accountNo:'', balance:0, currency:'CNY' })

function loadData() {
  loading.value = true
  request.get('/accounts', {params:{page:page.value, size:size.value}}).then(r => { tableData.value = r.records||[]; total.value = Number(r.total)||0 }).finally(() => loading.value = false)
}
function showCreate() { editId.value = null; Object.assign(form, {bankName:'',accountName:'',accountNo:'',balance:0,currency:'CNY'}); dialogVisible.value = true }
function showEdit(row) { editId.value = row.id; Object.assign(form, row); dialogVisible.value = true }
function save() {
  const api = editId.value ? request.put(`/accounts/${editId.value}`, form) : request.post('/accounts', form)
  api.then(() => { dialogVisible.value = false; loadData(); ElMessage.success('保存成功') })
}
onMounted(loadData)
</script>
