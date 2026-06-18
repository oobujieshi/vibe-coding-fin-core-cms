<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="actions">
        <el-button type="primary" @click="showCreateDialog">新建订单</el-button>
        <el-button type="success" @click="showCalcDialog" :disabled="!selected.length">批量计算费用</el-button>
        <el-upload :action="''" :auto-upload="false" :show-file-list="false" @change="handleImport" accept=".xlsx">
          <el-button>导入 Excel</el-button>
        </el-upload>
        <el-button @click="handleExport">导出 Excel</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="search" class="search-form">
      <el-form-item label="客户"><el-input v-model="search.customerName" placeholder="客户名称" clearable /></el-form-item>
      <el-form-item label="状态"><el-select v-model="search.status" placeholder="全部" clearable>
        <el-option label="待结算" :value="1" /><el-option label="已结算" :value="2" /><el-option label="已关闭" :value="3" />
      </el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe @selection-change="handleSelect" v-loading="loading">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="customerName" label="客户" />
      <el-table-column prop="contractNo" label="合同号" />
      <el-table-column prop="totalAmount" label="订单金额" width="120"><template #default="{row}">¥{{ row.totalAmount }}</template></el-table-column>
      <el-table-column prop="calculatedAmount" label="结算金额" width="120"><template #default="{row}">¥{{ row.calculatedAmount || '-' }}</template></el-table-column>
      <el-table-column prop="feeAmount" label="费用" width="100"><template #default="{row}">¥{{ row.feeAmount || '-' }}</template></el-table-column>
      <el-table-column prop="status" label="状态" width="80"><template #default="{row}">{{ ['','待结算','已结算','已关闭'][row.status] }}</template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleClose(row)" v-if="row.status===1">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <!-- 新建/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="合同号"><el-input v-model="form.contractNo" /></el-form-item>
        <el-form-item label="订单金额"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
        <el-form-item label="订单明细">
          <el-button size="small" @click="addItem" style="margin-bottom:5px">+ 添加</el-button>
          <div v-for="(item, idx) in form.items" :key="idx" style="display:flex;gap:4px;margin-bottom:4px">
            <el-input v-model="item.productName" placeholder="产品" size="small" style="width:200px" />
            <el-input-number v-model="item.quantity" :min="1" size="small" style="width:80px" />
            <el-input-number v-model="item.unitPrice" :min="0" :precision="2" size="small" style="width:120px" />
            <span style="line-height:32px">= ¥{{ (item.quantity||0)*(item.unitPrice||0) }}</span>
            <el-button size="small" type="danger" @click="form.items.splice(idx,1)">×</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="saveOrder">保存</el-button></template>
    </el-dialog>

    <!-- 批量计算弹窗 -->
    <el-dialog title="批量费用计算" v-model="calcVisible" width="500px">
      <p>已选择 {{ selected.length }} 个订单，确认计算？</p>
      <template #footer><el-button @click="calcVisible=false">取消</el-button><el-button type="primary" @click="doBatchCalc">开始计算</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const search = reactive({ customerName: '', status: null })
const page = ref(1); const size = ref(20); const total = ref(0)
const tableData = ref([]); const loading = ref(false); const selected = ref([])

const dialogVisible = ref(false); const dialogTitle = ref(''); const editId = ref(null)
const form = reactive({ customerName: '', contractNo: '', totalAmount: 0, remark: '', items: [] })

const calcVisible = ref(false)

function loadData() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (search.customerName) params.customerName = search.customerName
  if (search.status) params.status = search.status
  request.get('/orders', { params }).then(res => {
    tableData.value = res.records||[]; total.value = Number(res.total)||0
  }).finally(() => loading.value = false)
}

function showCreateDialog() { dialogTitle.value = '新建订单'; editId.value = null; Object.assign(form, { customerName:'',contractNo:'',totalAmount:0,remark:'',items:[] }); dialogVisible.value = true }
function showEditDialog(row) { dialogTitle.value = '编辑订单'; editId.value = row.id; Object.assign(form, {...row, items: row.items||[]}); dialogVisible.value = true }

function addItem() { form.items.push({ productName:'', quantity:1, unitPrice:0 }) }

function saveOrder() {
  form.totalAmount = form.items.reduce((s,i)=>s+(i.quantity||0)*(i.unitPrice||0),0)
  const api = editId.value ? () => request.put(`/orders/${editId.value}`, form) : () => request.post('/orders', form)
  api().then(() => { dialogVisible.value = false; loadData(); ElMessage.success('成功') })
}

function handleClose(row) { request.delete(`/orders/${row.id}`).then(() => { loadData(); ElMessage.success('已关闭') }) }

function handleSelect(rows) { selected.value = rows.map(r => r.id) }
function showCalcDialog() { calcVisible.value = true }
function doBatchCalc() { request.post('/orders/batch-calc', { orderIds: selected.value }).then(res => { calcVisible.value = false; loadData(); ElMessage.success(`成功${res.successCount}，失败${res.failCount}`) }) }

function handleExport() {
  axios.get('/api/v1/orders/export', {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
  }).then(res => {
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a'); a.href = url; a.download = '订单导出.csv'; a.click()
    URL.revokeObjectURL(url); ElMessage.success('导出成功')
  }).catch(() => ElMessage.error('导出失败'))
}
function handleImport(file) {
  const fd = new FormData(); fd.append('file', file.raw)
  request.post('/orders/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } }).then(res => ElMessage.success(`导入${res.successRows}/${res.totalRows}`))
}

onMounted(loadData)
</script>
