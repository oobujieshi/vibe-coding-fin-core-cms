<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审批管理</h2>
      <el-radio-group v-model="tab" size="default">
        <el-radio-button label="pending">待审批</el-radio-button>
        <el-radio-button label="all">全部审批</el-radio-button>
      </el-radio-group>
    </div>

    <el-form :inline="true" :model="search">
      <el-form-item label="业务类型"><el-select v-model="search.bizType" clearable><el-option label="付款" value="PAYMENT" /><el-option label="收款" value="RECEIPT" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bizType" label="业务类型" width="90" />
      <el-table-column prop="bizId" label="业务ID" width="80" />
      <el-table-column label="审批进度" width="120">
        <template #default="{row}">第{{ row.currentNode }}步 / 共{{ row.totalNodes }}步</template>
      </el-table-column>
      <el-table-column prop="approvalStatusDesc" label="状态" width="90">
        <template #default="{row}">
          <el-tag :type="row.approvalStatus===2?'success':row.approvalStatus===3?'danger':'warning'">{{ row.approvalStatusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="审批意见" min-width="150" show-overflow-tooltip />
      <el-table-column prop="approvedTime" label="审批时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <template v-if="row.approvalStatus===1">
            <el-button size="small" type="success" @click="process(row,'APPROVE')">通过</el-button>
            <el-button size="small" type="danger" @click="process(row,'REJECT')">驳回</el-button>
          </template>
          <span v-else style="color:#909399">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog title="审批操作" v-model="dialogVisible" width="420px">
      <el-form label-width="80px">
        <el-form-item label="操作"><el-tag :type="currentAction==='APPROVE'?'success':'danger'">{{ currentAction==='APPROVE'?'通过':'驳回' }}</el-tag></el-form-item>
        <el-form-item label="意见"><el-input v-model="comment" type="textarea" placeholder="审批意见（驳回时必填）" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button :type="currentAction==='APPROVE'?'primary':'danger'" @click="submitProcess">确认{{ currentAction==='APPROVE'?'通过':'驳回' }}</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const tab = ref('pending')
const search = reactive({ bizType: '' })
const page = ref(1); const size = ref(20); const total = ref(0)
const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false); const currentRow = ref(null); const currentAction = ref(''); const comment = ref('')

function loadData() {
  loading.value = true
  const params = { page: page.value, size: size.value, bizType: search.bizType || undefined }
  request.get('/approvals/pending', { params }).then(r => { tableData.value = r.records||[]; total.value = Number(r.total)||0 }).finally(() => loading.value = false)
}
function process(row, action) { currentRow.value = row; currentAction.value = action; comment.value = ''; dialogVisible.value = true }
function submitProcess() {
  if (currentAction.value === 'REJECT' && !comment.value.trim()) { ElMessage.error('驳回时必须填写意见'); return }
  request.put(`/approvals/${currentRow.value.id}/process`, { action: currentAction.value, comment: comment.value })
    .then(() => { dialogVisible.value = false; loadData(); ElMessage.success(currentAction.value === 'APPROVE' ? '已通过' : '已驳回') })
}
onMounted(loadData)
</script>
