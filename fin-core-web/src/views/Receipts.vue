<template>
  <div class="page-container">
    <div class="page-header">
      <h2>收款管理</h2>
      <el-button type="primary" @click="showCreateDialog">新建收款记录</el-button>
    </div>

    <el-form :inline="true" :model="search">
      <el-form-item label="付款方"><el-input v-model="search.payerName" placeholder="付款方名称" clearable /></el-form-item>
      <el-form-item label="状态"><el-select v-model="search.status" clearable><el-option label="待确认" :value="1" /><el-option label="已到账" :value="2" /><el-option label="已核销" :value="3" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="receiptNo" label="收款编号" width="200" />
      <el-table-column prop="payerName" label="付款方" />
      <el-table-column prop="amount" label="金额" width="120"><template #default="{row}">¥{{ row.amount }}</template></el-table-column>
      <el-table-column prop="receiptMethodDesc" label="收款方式" width="100" />
      <el-table-column prop="receiptStatusDesc" label="状态" width="90">
        <template #default="{row}"><el-tag :type="row.receiptStatus===2?'success':row.receiptStatus===3?'info':''">{{ row.receiptStatusDesc }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="success" @click="handleConfirm(row)" v-if="row.receiptStatus===1">确认到账</el-button>
          <el-button size="small" type="warning" @click="showVerifyCode(row)" v-if="row.receiptStatus===1">核销码</el-button>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog title="新建收款记录" v-model="dialogVisible" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="付款方"><el-input v-model="form.payerName" placeholder="付款方名称" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="收款方式"><el-select v-model="form.receiptMethod" style="width:100%"><el-option label="银行转账" :value="1" /><el-option label="扫码支付" :value="2" /><el-option label="现金" :value="3" /></el-select></el-form-item>
        <el-form-item label="关联订单ID"><el-input v-model="form.orderId" placeholder="订单ID" /></el-form-item>
        <el-form-item label="关联账单ID"><el-input v-model="form.billId" placeholder="账单ID" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="saveReceipt">保存</el-button></template>
    </el-dialog>

    <el-dialog title="核销码" v-model="verifyVisible" width="350px" center>
      <div style="text-align:center;padding:20px">
        <p style="font-size:28px;letter-spacing:6px;font-weight:bold;font-family:monospace;margin:10px 0">{{ code }}</p>
        <p style="color:#909399;font-size:13px">请向付款方展示此核销码</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const search = reactive({ payerName: '', status: null })
const page = ref(1); const size = ref(20); const total = ref(0)
const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false)
const verifyVisible = ref(false); const code = ref('')
const form = reactive({ payerName: '', amount: 0, receiptMethod: 1, orderId: '', billId: '' })

function loadData() {
  loading.value = true
  request.get('/receipts', { params: { page: page.value, size: size.value, payerName: search.payerName || undefined, status: search.status || undefined } })
    .then(r => { tableData.value = r.records||[]; total.value = Number(r.total)||0 }).finally(() => loading.value = false)
}
function showCreateDialog() { Object.assign(form, { payerName: '', amount: 0, receiptMethod: 1, orderId: '', billId: '' }); dialogVisible.value = true }
function saveReceipt() {
  request.post('/receipts', { ...form, orderId: Number(form.orderId) || null, billId: Number(form.billId) || null })
    .then(() => { dialogVisible.value = false; loadData(); ElMessage.success('创建成功') })
}
function handleConfirm(row) { request.put(`/receipts/${row.id}/confirm`).then(() => { loadData(); ElMessage.success('已确认到账') }) }
function showVerifyCode(row) { request.get(`/receipts/${row.id}/verify-code`).then(r => { code.value = r.verifyCode; verifyVisible.value = true }) }
onMounted(loadData)
</script>
