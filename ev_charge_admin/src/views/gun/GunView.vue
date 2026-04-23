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
const formRef = ref(null)
const keyword = ref('')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/gun/list', { params: { page: page.value, limit: limit.value, keyword: keyword.value } })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
    
    // 获取充电桩和充电站信息
    if (list.value.length > 0) {
      const chargerIds = [...new Set(list.value.map(item => item.chargerId))]
      
      // 获取充电桩信息
      const chargerRes = await request.get('/charger/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const chargerMap = {}
      chargerRes.data?.records?.forEach(charger => {
        chargerMap[charger.id] = charger
      })
      
      // 获取充电站信息
      const stationIds = [...new Set(Object.values(chargerMap).map(c => c.stationId))]
      const stationRes = await request.get('/station/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const stationMap = {}
      stationRes.data?.records?.forEach(station => {
        stationMap[station.id] = station.name
      })
      
      // 为每个充电枪添加关联信息
      list.value = list.value.map(gun => {
        const charger = chargerMap[gun.chargerId]
        return {
          ...gun,
          chargerNo: charger?.deviceNo || `充电桩${gun.chargerId}`,
          stationName: stationMap[charger?.stationId] || `充电站${charger?.stationId}`
        }
      })
    }
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

const handleAdd = () => { dialogTitle.value = '新增充电枪'; form.value = { status: 0 }; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑充电枪'; form.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
  const api = form.value.id ? '/gun/update' : '/gun/add'
  const res = await request.post(api, form.value)
  if (res.success) { ElMessage.success(dialogTitle.value + '成功'); dialogVisible.value = false; fetchList() }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await request.get('/gun/delete', { params: { id: row.id } })
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
          <span>充电枪管理</span>
          <div class="search-box">
            <el-input v-model="keyword" placeholder="搜索枪号" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
            <el-button type="primary" size="small" @click="handleAdd">新增</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="stationName" label="所属充电站" width="150" />
        <el-table-column prop="chargerNo" label="充电桩编号" width="120" />
        <el-table-column prop="gunNo" label="枪号" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="['success','danger','info'][row.status]||'info'" size="small">{{['空闲','繁忙','故障'][row.status]||'未知'}}</el-tag></template>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="充电桩ID"><el-input-number v-model="form.chargerId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="枪号"><el-input v-model="form.gunNo" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="空闲" :value="0" /><el-option label="繁忙" :value="1" /><el-option label="故障" :value="2" /></el-select>
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
