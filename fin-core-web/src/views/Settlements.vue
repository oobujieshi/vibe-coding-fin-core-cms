<template>
  <div class="page-container">
    <div class="page-header">
      <h2>结算管理</h2>
      <el-button type="primary" @click="showCreateDialog">生成结算单</el-button>
    </div>
    <el-form :inline="true" :model="search"><el-form-item label="状态"><el-select v-model="search.status" clearable><el-option label="待确认" :value="1" /><el-option label="已确认" :value="2" /><el-option label="已作废" :value="3" /></el-select></el-form-item>
    <el-form-item><el-button type="primary" @click="loadData">搜索</el-button></el-form-item></el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="settlementNo" label="结算单号" width="200" />
      <el-table-column prop="orderNo" label="关联订单" width="180" />
      <el-table-column prop="customerName" label="客户" />
      <el-table-column prop="originalAmount" label="原金额" width="110"><template #default="{row}">¥{{ row.originalAmount }}</template></el-table-column>
      <el-table-column prop="realAmount" label="结算金额" width="110"><template #default="{row}">¥{{ row.realAmount }}</template></el-table-column>
      <el-table-column prop="status" label="状态" width="80"><template #default="{row}">{{ ['','待确认','已确认','已作废'][row.status] }}</template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" type="success" @click="confirm(row)" v-if="row.status===1">确认</el-button>
          <el-button size="small" type="danger" @click="cancel(row)" v-if="row.status===1">作废</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next" @current-change="loadData" />

    <el-dialog title="生成结算单" v-model="dialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="选择订单">
          <el-select v-model="selectedOrderIds" multiple filterable placeholder="选择待结算订单" style="width:100%">
            <el-option v-for="o in pendingOrders" :key="o.id" :label="`${o.orderNo} - ${o.customerName} (¥${o.totalAmount})`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="settlementRemark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="createSettlements">生成</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const search = reactive({ status: null })
const page = ref(1); const size = ref(20); const total = ref(0)
const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false); const selectedOrderIds = ref([]); const settlementRemark = ref(''); const pendingOrders = ref([])

function loadData() { loading.value=true; request.get('/settlements',{params:{page:page.value,size:size.value,status:search.status}}).then(r=>{tableData.value=r.records||[];total.value=Number(r.total)||0}).finally(()=>loading.value=false) }
function showCreateDialog() { request.get('/orders',{params:{status:1,size:999}}).then(r=>{pendingOrders.value=r.records;selectedOrderIds.value=[];settlementRemark.value='';dialogVisible.value=true}) }
function createSettlements() { request.post('/settlements',{orderIds:selectedOrderIds.value,remark:settlementRemark.value}).then(()=>{dialogVisible.value=false;loadData();ElMessage.success('结算单生成成功')}) }
function confirm(row) { request.put(`/settlements/${row.id}/confirm`).then(()=>{loadData();ElMessage.success('已确认')}) }
function cancel(row) { request.put(`/settlements/${row.id}/cancel`).then(()=>{loadData();ElMessage.success('已作废')}) }
onMounted(loadData)
</script>
