<template>
  <div>
    <el-table :data="data" v-loading="loading" border stripe style="width:100%">
      <template v-for="col in columns" :key="col.prop">
        <el-table-column :prop="col.prop" :label="col.label" :width="col.width" :min-width="col.minWidth" :align="col.align || 'left'" />
      </template>
      <el-table-column v-if="$slots.actions" label="操作" width="200" align="center" fixed="right">
        <template #default="scope"><slot name="actions" :row="scope.row" /></template>
      </el-table-column>
    </el-table>
    <div style="margin-top:16px;text-align:right" v-if="showPagination">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" @change="emit('pageChange')" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  data: { type: Array, default: () => [] },
  columns: { type: Array, required: true },
  loading: { type: Boolean, default: false },
  showPagination: { type: Boolean, default: true },
})
const currentPage = defineModel('currentPage', { default: 1 })
const pageSize = defineModel('pageSize', { default: 20 })
const total = defineModel('total', { default: 0 })
const emit = defineEmits(['pageChange'])
</script>
