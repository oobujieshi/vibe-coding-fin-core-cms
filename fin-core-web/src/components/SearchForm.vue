<template>
  <el-form :model="model" inline class="search-form">
    <el-form-item v-for="item in items" :key="item.prop" :label="item.label">
      <el-input v-if="item.type==='input'" v-model="model[item.prop]" :placeholder="item.placeholder" clearable />
      <el-select v-else-if="item.type==='select'" v-model="model[item.prop]" :placeholder="item.placeholder" clearable>
        <el-option v-for="opt in item.options" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-date-picker v-else-if="item.type==='date'" v-model="model[item.prop]" type="date" :placeholder="item.placeholder" value-format="YYYY-MM-DD" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="$emit('search')">查询</el-button>
      <el-button @click="$emit('reset')">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
defineProps({
  model: { type: Object, required: true },
  items: { type: Array, required: true },
})
defineEmits(['search', 'reset'])
</script>

<style scoped>
.search-form { padding: 16px 0; }
</style>
