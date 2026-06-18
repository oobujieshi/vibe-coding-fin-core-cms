<template>
  <div class="page-container">
    <div class="page-header">
      <h2>付款管理</h2>
      <el-button type="primary" @click="showApplyDialog">发起付款申请</el-button>
    </div>

    <el-form :inline="true" :model="search">
      <el-form-item label="收款方"><el-input v-model="search.payeeName" placeholder="收款方名称" clearable /></el-form-item>
      <el-form-item label="状态"><el-select v-model="search.status" clearable><el-option label="待审批" :value="1" /><el-option label="审批中" :value="2" /><el-option label="已付款" :value="3" /><el-option label="已驳回" :value="4" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="paymentNo" label="付款编号" width="200" />
      <el-table-column prop="payeeName" label="收款方" />
      <el-table-column prop="amount" label="金额" width="120"><template #default="{row}">¥{{ row.amount }}</template></el-table-column>
      <el-table-column prop="paymentStatusDesc" label="状态" width="90">
        <template #default="{row}"><el-tag :type="row.paymentStatus===3?'success':row.paymentStatus===4?'danger':row.paymentStatus===2?'warning':''">{{ row.paymentStatusDesc }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="appliedTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="success" @click="handleApprove(row)" v-if="row.paymentStatus===1||row.paymentStatus===2">审批通过</el-button>
          <el-button size="small" type="danger" @click="handleReject(row)" v-if="row.paymentStatus===1||row.paymentStatus===2">驳回</el-button>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog title="发起付款申请" v-model="dialogVisible" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="收款方"><el-input v-model="form.payeeName" placeholder="收款方名称" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="供应商ID"><el-input v-model="form.supplierId" placeholder="供应商ID" /></el-form-item>
        <el-form-item label="关联订单ID"><el-input v-model="form.orderId" placeholder="订单ID" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="savePayment">提交申请</el-button></template>
    </el-dialog>

    <el-dialog title="审批付款" v-model="approveVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="付款编号"><strong>{{ target?.paymentNo }}</strong></el-form-item>
        <el-form-item label="收款方">{{ target?.payeeName }}</el-form-item>
        <el-form-item label="金额">¥{{ target?.amount }}</el-form-item>
        <el-form-item label="意见"><el-input v-model="comment" type="textarea" placeholder="审批意见" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="approveVisible=false">取消</el-button><el-button type="primary" @click="submitApprove">确认通过</el-button></template>
    </el-dialog>

    <el-dialog title="驳回付款" v-model="rejectVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="付款编号"><strong>{{ target?.paymentNo }}</strong></el-form-item>
        <el-form-item label="驳回原因"><el-input v-model="comment" type="textarea" placeholder="请填写驳回原因" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="rejectVisible=false">取消</el-button><el-button type="danger" @click="submitReject">确认驳回</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const search = reactive({ payeeName: '', status: null })
const page = ref(1); const size = ref(20); const total = ref(0)
const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ payeeName: '', amount: 0, supplierId: '', orderId: '', remark: '' })
const approveVisible = ref(false); const rejectVisible = ref(false)
const target = ref(null); const comment = ref('')

function loadData() {
  loading.value = true
  request.get('/payments', { params: { page: page.value, size: size.value, payeeName: search.payeeName || undefined, status: search.status || undefined } })
    .then(r => { tableData.value = r.records||[]; total.value = Number(r.total)||0 }).finally(() => loading.value = false)
}
function showApplyDialog() { Object.assign(form, { payeeName: '', amount: 0, supplierId: '', orderId: '', remark: '' }); dialogVisible.value = true }
function savePayment() {
  request.post('/payments', { ...form, supplierId: Number(form.supplierId) || null, orderId: Number(form.orderId) || null })
    .then(() => { dialogVisible.value = false; loadData(); ElMessage.success('付款申请已提交') })
}
function handleApprove(row) { target.value = row; comment.value = ''; approveVisible.value = true }
function submitApprove() { request.put(`/payments/${target.value.id}/approve`, { comment: comment.value }).then(() => { approveVisible.value = false; loadData(); ElMessage.success('已通过') }) }
function handleReject(row) { target.value = row; comment.value = ''; rejectVisible.value = true }
function submitReject() { request.put(`/payments/${target.value.id}/reject`, { comment: comment.value }).then(() => { rejectVisible.value = false; loadData(); ElMessage.success('已驳回') }) }
onMounted(loadData)
</script>
