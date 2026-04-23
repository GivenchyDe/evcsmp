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

const statusMap = ['未发布','已发布','已过期']
const statusType = ['info','success','warning']

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/announcement/list', { params: { page: page.value, limit: limit.value, keyword: keyword.value } })
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

const handleAdd = () => { dialogTitle.value = '新增公告'; form.value = { status: 0, viewCount: 0 }; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑公告'; form.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
  const api = form.value.id ? '/announcement/update' : '/announcement/add'
  const res = await request.post(api, form.value)
  if (res.success) { ElMessage.success(dialogTitle.value + '成功'); dialogVisible.value = false; fetchList() }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await request.get('/announcement/delete', { params: { id: row.id } })
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
          <span>公告管理</span>
          <div class="search-box">
            <el-input v-model="keyword" placeholder="搜索标题" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
            <el-button type="primary" size="small" @click="handleAdd">新增公告</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column prop="adminId" label="发布者ID" width="90" />
        <el-table-column prop="viewCount" label="阅读量" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="statusType[row.status]||'info'" size="small">{{statusMap[row.status]||'未知'}}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publishAt" label="发布时间" width="160">
          <template #default="{ row }">
            {{ row.publishAt ? row.publishAt.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="page" v-model:page-size="limit" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" rows="5" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="未发布" :value="0" /><el-option label="已发布" :value="1" /><el-option label="已过期" :value="2" /></el-select>
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
