<template>
  <div class="page-container">
    <div class="page-header">
      <h2>账单管理</h2>
      <div>
        <el-button type="primary" @click="showCreateDialog">生成账单</el-button>
        <el-button @click="showMergeDialog" :disabled="selected.length<2">合并账单</el-button>
        <el-button @click="showSplitDialog" :disabled="selected.length!==1">拆分账单</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="search">
      <el-form-item label="客户ID"><el-input v-model="search.customerId" placeholder="客户ID" clearable /></el-form-item>
      <el-form-item label="状态"><el-select v-model="search.sendStatus" clearable><el-option label="未发送" :value="0" /><el-option label="已发送" :value="1" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading" @selection-change="onSelect">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="billNo" label="账单编号" width="220" />
      <el-table-column prop="customerId" label="客户ID" width="100" />
      <el-table-column label="账单周期" width="220">
        <template #default="{row}">{{ row.billPeriodStart }} ~ {{ row.billPeriodEnd }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总额" width="110"><template #default="{row}">¥{{ row.totalAmount }}</template></el-table-column>
      <el-table-column prop="paidAmount" label="已付" width="100"><template #default="{row}">¥{{ row.paidAmount }}</template></el-table-column>
      <el-table-column prop="unpaidAmount" label="未付" width="100"><template #default="{row}">¥{{ row.unpaidAmount }}</template></el-table-column>
      <el-table-column prop="sendStatusDesc" label="状态" width="100">
        <template #default="{row}"><el-tag :type="row.sendStatus===1?'success':row.sendStatus===-1?'info':''">{{ row.sendStatusDesc }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="success" @click="handleSend(row)" v-if="row.sendStatus===0">发送</el-button>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog title="生成账单" v-model="dialogVisible" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客户ID"><el-input v-model="form.customerId" placeholder="客户ID" /></el-form-item>
        <el-form-item label="周期开始"><el-date-picker v-model="form.billPeriodStart" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="周期结束"><el-date-picker v-model="form.billPeriodEnd" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="总额"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="已付金额"><el-input-number v-model="form.paidAmount" :min="0" :precision="2" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="saveBill">生成</el-button></template>
    </el-dialog>

    <el-dialog title="合并账单" v-model="mergeVisible" width="500px">
      <p>已选择 {{ selected.length }} 个账单：</p>
      <el-table :data="selected" border size="small">
        <el-table-column prop="billNo" label="账单编号" />
        <el-table-column prop="totalAmount" label="金额" width="110"><template #default="{row}">¥{{ row.totalAmount }}</template></el-table-column>
      </el-table>
      <div style="margin-top:16px;text-align:right"><strong>合并后总额：¥{{ mergeTotal }}</strong></div>
      <template #footer><el-button @click="mergeVisible=false">取消</el-button><el-button type="primary" @click="doMerge">确认合并</el-button></template>
    </el-dialog>

    <el-dialog title="拆分账单" v-model="splitVisible" width="500px">
      <p v-if="selected[0]">拆分：{{ selected[0].billNo }}（总额：¥{{ selected[0].totalAmount }}）</p>
      <div v-for="(_, idx) in splitList" :key="idx" style="margin-bottom:10px">
        <span>第{{ idx+1 }}笔：</span>
        <el-input-number v-model="splitList[idx]" :min="0" :precision="2" style="width:200px" />
        <el-button size="small" @click="removeSplit(idx)" :disabled="splitList.length<=1" style="margin-left:8px">移除</el-button>
      </div>
      <el-button @click="addSplit" size="small" style="margin-top:8px">+ 添加</el-button>
      <div style="margin-top:10px;color:#909399">剩余：¥{{ splitRemaining }}</div>
      <template #footer><el-button @click="splitVisible=false">取消</el-button><el-button type="primary" @click="doSplit">确认拆分</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const search = reactive({ customerId: '', sendStatus: null })
const page = ref(1); const size = ref(20); const total = ref(0)
const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false)
const selected = ref([])
const form = reactive({ customerId: '', billPeriodStart: null, billPeriodEnd: null, totalAmount: 0, paidAmount: 0 })
const mergeVisible = ref(false)
const mergeTotal = computed(() => selected.value.reduce((s, b) => s + (b.totalAmount || 0), 0).toFixed(2))
const splitVisible = ref(false); const splitList = ref([0])
const splitRemaining = computed(() => {
  if (!selected.value[0]) return '0.00'
  const t = selected.value[0].totalAmount || 0
  const s = splitList.value.reduce((a, v) => a + (v || 0), 0)
  return (t - s).toFixed(2)
})
function addSplit() { splitList.value.push(0) }
function removeSplit(i) { splitList.value.splice(i, 1) }

function loadData() {
  loading.value = true
  request.get('/bills', { params: { page: page.value, size: size.value, customerId: search.customerId || undefined, sendStatus: search.sendStatus !== null ? search.sendStatus : undefined } })
    .then(r => { tableData.value = r.records||[]; total.value = Number(r.total)||0 }).finally(() => loading.value = false)
}
function onSelect(rows) { selected.value = rows }
function showCreateDialog() { Object.assign(form, { customerId: '', billPeriodStart: null, billPeriodEnd: null, totalAmount: 0, paidAmount: 0 }); dialogVisible.value = true }
function fmt(d) { return d ? new Date(d).toISOString().split('T')[0] : null }
function saveBill() {
  request.post('/bills', { customerId: Number(form.customerId), billPeriodStart: fmt(form.billPeriodStart), billPeriodEnd: fmt(form.billPeriodEnd), totalAmount: form.totalAmount, paidAmount: form.paidAmount })
    .then(() => { dialogVisible.value = false; loadData(); ElMessage.success('账单生成成功') })
}
function showMergeDialog() { mergeVisible.value = true }
function doMerge() { request.post('/bills/merge', { billIds: selected.value.map(b => b.id) }).then(() => { mergeVisible.value = false; selected.value = []; loadData(); ElMessage.success('合并成功') }) }
function showSplitDialog() { splitList.value = [selected.value[0].totalAmount || 0]; splitVisible.value = true }
function doSplit() {
  if (Math.abs(Number(splitRemaining.value)) > 0.01) { ElMessage.error('金额必须等于账单总额'); return }
  request.post('/bills/split', { billId: selected.value[0].id, splitAmounts: splitList.value }).then(() => { splitVisible.value = false; selected.value = []; loadData(); ElMessage.success('拆分成功') })
}
function handleSend(row) { request.post(`/bills/${row.id}/send`).then(() => { loadData(); ElMessage.success('已发送') }) }
onMounted(loadData)
</script>
