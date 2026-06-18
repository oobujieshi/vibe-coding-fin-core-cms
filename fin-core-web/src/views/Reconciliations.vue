<template>
  <div class="page-container">
    <div class="page-header">
      <h2>对账管理</h2>
      <div>
        <el-button type="primary" @click="showRun">执行对账</el-button>
        <el-button @click="loadData" :type="showDiffs?'':'primary'">{{ showDiffs?'全部':'仅差异' }}</el-button>
      </div>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="diffAmount" label="差额" width="100"><template #default="{row}">¥{{ row.diffAmount }}</template></el-table-column>
      <el-table-column prop="matchStatusDesc" label="状态" width="80">
        <template #default="{row}"><el-tag :type="row.matchStatus===1?'success':row.matchStatus===2?'info':'warning'">{{ row.matchStatusDesc }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button size="small" type="success" @click="handle(row,'APPROVE')" v-if="row.matchStatus===0">确认</el-button>
          <el-button size="small" type="info" @click="handle(row,'SKIP')" v-if="row.matchStatus===0">忽略</el-button>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog title="执行对账" v-model="runVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="账户ID"><el-input v-model="runForm.accountId" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="runForm.startDate" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="runForm.endDate" type="date" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="runVisible=false">取消</el-button><el-button type="primary" @click="doRun">执行</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const tableData = ref([]); const loading = ref(false)
const page = ref(1); const size = ref(20); const total = ref(0)
const showDiffs = ref(false)
const runVisible = ref(false)
const runForm = reactive({ accountId:'', startDate:null, endDate:null })

function fmt(d) { return d ? new Date(d).toISOString().split('T')[0] : null }
function loadData() {
  loading.value = true
  const url = showDiffs.value ? '/reconciliations/diffs' : '/reconciliations'
  request.get(url, {params:{page:page.value, size:size.value}}).then(r => { tableData.value = r.records||[]; total.value = Number(r.total)||0 }).finally(() => loading.value = false)
}
function toggleDiffs() { showDiffs.value = !showDiffs.value; loadData() }
function showRun() { runVisible.value = true }
function doRun() {
  request.post('/reconciliations/run', { accountId: Number(runForm.accountId), startDate: fmt(runForm.startDate), endDate: fmt(runForm.endDate) })
    .then(r => { runVisible.value = false; loadData(); ElMessage.success(`${r.matchedCount}匹配, ${r.diffCount}差异`) })
}
function handle(row, action) {
  request.put(`/reconciliations/${row.id}/handle`, { action }).then(() => { loadData(); ElMessage.success(action==='APPROVE'?'已确认':'已忽略') })
}
onMounted(loadData)
</script>
