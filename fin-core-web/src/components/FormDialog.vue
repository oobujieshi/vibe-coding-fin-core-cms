<template>
  <el-dialog :model-value="visible" :title="title" width="600px" @close="$emit('update:visible',false)">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item v-for="item in items" :key="item.prop" :label="item.label" :prop="item.prop">
        <el-input v-if="item.type==='input'" v-model="form[item.prop]" :placeholder="item.placeholder" />
        <el-input-number v-else-if="item.type==='number'" v-model="form[item.prop]" :precision="2" style="width:100%" />
        <el-select v-else-if="item.type==='select'" v-model="form[item.prop]" :placeholder="item.placeholder">
          <el-option v-for="opt in item.options" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-date-picker v-else-if="item.type==='date'" v-model="form[item.prop]" type="date" value-format="YYYY-MM-DD" style="width:100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible',false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({
  visible: Boolean, title: String, form: Object, items: Array, rules: Object, loading: Boolean,
})
const emit = defineEmits(['update:visible', 'submit'])
const formRef = ref(null)

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  emit('submit')
}
</script>
