<script setup>
import { ref, onMounted } from 'vue'
import { getChargerList, addCharger, updateCharger, deleteCharger } from '@/api/charger.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})
const formRef = ref(null)
const keyword = ref('')

const rules = {
  deviceNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  stationId: [{ required: true, message: '请输入网点ID', trigger: 'blur' }],
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getChargerList(page.value, limit.value, keyword.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => {
  page.value = 1
  fetchList()
}

const handleReset = () => {
  keyword.value = ''
  page.value = 1
  fetchList()
}

const handleAdd = () => {
  dialogTitle.value = '新增充电桩'
  form.value = { status: 1, type: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑充电桩'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  const api = form.value.id ? updateCharger : addCharger
  const res = await api(form.value)
  if (res.success) {
    ElMessage.success(dialogTitle.value + '成功')
    dialogVisible.value = false
    fetchList()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteCharger(row.id)
    if (res.success) { ElMessage.success('删除成功'); fetchList() }
  })
}

onMounted(fetchList)
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>充电桩管理</span>
          <div class="search-box">
            <el-input v-model="keyword" placeholder="搜索设备编号/厂家" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
            <el-button type="primary" size="small" @click="handleAdd">新增</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="deviceNo" label="设备编号" width="130" />
        <el-table-column prop="stationId" label="网点ID" width="80" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'" size="small">{{ row.type === 1 ? '快充' : '慢充' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="厂家" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="['danger','success','warning'][row.status] || 'info'" size="small">{{ ['故障','正常','维护中'][row.status] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="page" v-model:page-size="limit" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="设备编号" prop="deviceNo"><el-input v-model="form.deviceNo" /></el-form-item>
        <el-form-item label="网点ID" prop="stationId"><el-input-number v-model="form.stationId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type"><el-radio :label="0">慢充</el-radio><el-radio :label="1">快充</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="厂家"><el-input v-model="form.manufacturer" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="故障" :value="0" /><el-option label="正常" :value="1" /><el-option label="维护中" :value="2" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-box { display: flex; gap: 8px; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
