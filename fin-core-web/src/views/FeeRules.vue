<template>
  <div class="page-container">
    <div class="page-header"><h2>费率规则</h2><el-button type="primary" @click="showDialog()">新建规则</el-button></div>
    <el-form :inline="true" :model="search"><el-form-item label="名称"><el-input v-model="search.ruleName" clearable /></el-form-item>
    <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item></el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="ruleName" label="规则名称" />
      <el-table-column prop="ruleType" label="类型" width="100" />
      <el-table-column prop="rate" label="费率" width="80"><template #default="{row}">{{ row.rate ? (row.rate*100).toFixed(1)+'%' : '-' }}</template></el-table-column>
      <el-table-column prop="status" label="状态" width="80"><template #default="{row}">{{ row.status===1?'启用':'禁用' }}</template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggle(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.ruleType"><el-option label="百分比" value="PERCENTAGE" /><el-option label="固定金额" value="FIXED" /></el-select></el-form-item>
        <el-form-item label="费率/金额"><el-input-number v-model="form.rate" :min="0" :precision="4" /></el-form-item>
        <el-form-item label="最低费用"><el-input-number v-model="form.minFee" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="最高费用"><el-input-number v-model="form.maxFee" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const search = reactive({ ruleName: '' })
const page = ref(1); const size = ref(20); const total = ref(0)
const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false); const dialogTitle = ref(''); const editId = ref(null)
const form = reactive({ ruleName:'', ruleType:'PERCENTAGE', rate:0, minFee:0, maxFee:0 })

function loadData() { loading.value=true; request.get('/fee-rules',{params:{page:page.value,size:size.value,ruleName:search.ruleName}}).then(r=>{tableData.value=r.records||[];total.value=Number(r.total)||0}).finally(()=>loading.value=false) }
function showDialog(row) { dialogTitle.value=row?'编辑规则':'新建规则'; editId.value=row?.id; Object.assign(form,row||{ruleName:'',ruleType:'PERCENTAGE',rate:0,minFee:0,maxFee:0}); dialogVisible.value=true }
function save() { const api=editId.value?request.put(`/fee-rules/${editId.value}`,form):request.post('/fee-rules',form); api().then(()=>{dialogVisible.value=false;loadData();ElMessage.success('成功')}) }
function toggle(row) { request.put(`/fee-rules/${row.id}/toggle`,{status:row.status===1?0:1}).then(()=>{loadData();ElMessage.success('已切换')}) }
onMounted(loadData)
</script>
