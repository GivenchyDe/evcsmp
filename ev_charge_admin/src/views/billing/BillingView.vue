<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})
const keyword = ref('')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/billingRule/list', { params: { page: page.value, limit: limit.value, keyword: keyword.value } })
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

const handleAdd = () => { dialogTitle.value = '新增计费规则'; form.value = { freeMinutes: 0 }; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑计费规则'; form.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
  const api = form.value.id ? '/billingRule/update' : '/billingRule/add'
  const res = await request.post(api, form.value)
  if (res.success) { ElMessage.success(dialogTitle.value + '成功'); dialogVisible.value = false; fetchList() }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await request.get('/billingRule/delete', { params: { id: row.id } })
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
          <span>计费规则</span>
          <div class="search-box">
            <el-input v-model="keyword" placeholder="搜索备注内容" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
            <el-button type="primary" size="small" @click="handleAdd">新增</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="chargerId" label="充电桩ID" width="90" />
        <el-table-column prop="pricePerKwh" label="电费(元/度)" width="110"><template #default="{row}"><span style="color:#f56c6c">¥{{row.pricePerKwh}}</span></template></el-table-column>
        <el-table-column prop="serviceFee" label="服务费(元/度)" width="120"><template #default="{row}">¥{{row.serviceFee}}</template></el-table-column>
        <el-table-column prop="freeMinutes" label="免费时长(分)" width="110" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="page" v-model:page-size="limit" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="充电桩ID"><el-input-number v-model="form.chargerId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="电费(元/度)"><el-input-number v-model="form.pricePerKwh" :precision="2" :step="0.1" style="width:100%" /></el-form-item>
        <el-form-item label="服务费(元/度)"><el-input-number v-model="form.serviceFee" :precision="2" :step="0.1" style="width:100%" /></el-form-item>
        <el-form-item label="免费时长(分)"><el-input-number v-model="form.freeMinutes" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" rows="2" /></el-form-item>
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
