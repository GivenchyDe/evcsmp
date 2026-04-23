<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { getFeedbackList, handleFeedback, updateFeedback, deleteFeedback } from '@/api/feedback.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const dialogVisible = ref(false)
const editDialogVisible = ref(false)
const form = ref({})
const editForm = ref({})
const formRef = ref(null)
const keyword = ref('')
const dialogTitle = ref('')
const stationOptions = ref([])
const chargerOptions = ref([])

const typeMap = ['网点问题','设备故障','软件异常','使用体验','其他']
const statusMap = ['待处理','处理中','已处理','已驳回']
const statusType = ['danger','warning','success','info']

const rules = {
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getFeedbackList(page.value, limit.value, keyword.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
    
    // 获取关联信息
    if (list.value.length > 0) {
      // 获取用户信息
      const userIds = [...new Set(list.value.map(item => item.userId))]
      const userRes = await request.get('/user/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const userMap = {}
      userRes.data?.records?.forEach(user => {
        userMap[user.id] = user
      })
      
      // 获取充电站信息
      const stationIds = list.value.filter(item => item.stationId).map(item => item.stationId)
      const stationRes = await request.get('/station/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const stationMap = {}
      stationRes.data?.records?.forEach(station => {
        stationMap[station.id] = station
      })
      
      // 获取充电桩信息
      const chargerIds = list.value.filter(item => item.chargerId).map(item => item.chargerId)
      const chargerRes = await request.get('/charger/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const chargerMap = {}
      chargerRes.data?.records?.forEach(charger => {
        chargerMap[charger.id] = charger
      })
      
      // 为每个反馈添加关联信息
      list.value = list.value.map(feedback => {
        const user = userMap[feedback.userId]
        const station = stationMap[feedback.stationId]
        const charger = chargerMap[feedback.chargerId]
        
        return {
          ...feedback,
          userName: user?.username || `用户${feedback.userId}`,
          stationName: station?.name || (feedback.stationId ? `充电站${feedback.stationId}` : '未关联'),
          chargerNo: charger?.deviceNo || (feedback.chargerId ? `充电桩${feedback.chargerId}` : '未关联')
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

const openHandle = (row) => {
  form.value = { id: row.id, status: 2, reply: '' }
  dialogVisible.value = true
}

const submitHandle = async () => {
  const res = await handleFeedback(form.value)
  if (res.success) { ElMessage.success('处理成功'); dialogVisible.value = false; fetchList() }
}

const handleAdd = () => {
  dialogTitle.value = '新增反馈'
  editForm.value = { 
    type: 0, 
    status: 0,
    contact: '',
    stationId: null,
    chargerId: null,
    reply: ''
  }
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑反馈'
  editForm.value = { ...row }
  editDialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该反馈吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteFeedback(row.id)
    if (res.success) { ElMessage.success('删除成功'); fetchList() }
  })
}

const submitEdit = async () => {
  await formRef.value.validate()
  
  // 如果是新增，需要生成反馈编号
  if (!editForm.value.id) {
    editForm.value.feedbackNo = 'FB-' + Date.now()
    editForm.value.userId = 1 // 默认管理员用户ID，实际应该从登录用户获取
  }
  
  const api = editForm.value.id ? updateFeedback : (data) => request.post('/feedback/add', data)
  const res = await api(editForm.value)
  if (res.success) { 
    ElMessage.success(dialogTitle.value + '成功')
    editDialogVisible.value = false
    fetchList() 
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 获取充电站和充电桩选项
const fetchOptions = async () => {
  try {
    // 获取充电站选项
    const stationRes = await request.get('/station/list', { 
      params: { page: 1, limit: 1000 } 
    })
    stationOptions.value = stationRes.data?.records || []
    
    // 获取充电桩选项
    const chargerRes = await request.get('/charger/list', { 
      params: { page: 1, limit: 1000 } 
    })
    chargerOptions.value = chargerRes.data?.records || []
  } catch (error) {
    console.error('获取选项失败', error)
  }
}

onMounted(async () => {
  await Promise.all([
    fetchList(),
    fetchOptions()
  ])
})
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>故障反馈</span>
          <div class="search-box">
            <el-input v-model="keyword" placeholder="搜索编号/内容" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
            <el-button type="primary" size="small" @click="handleAdd">新增反馈</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="feedbackNo" label="编号" width="140" />
        <el-table-column prop="userName" label="提交用户" width="120" />
        <el-table-column prop="type" label="类型" width="90"><template #default="{row}">{{typeMap[row.type]||'未知'}}</template></el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="stationName" label="关联充电站" width="150" />
        <el-table-column prop="chargerNo" label="关联充电桩" width="150" />
        <el-table-column prop="contact" label="联系方式" width="120" />
        <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="statusType[row.status]||'info'" size="small">{{statusMap[row.status]||'未知'}}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt ? row.createdAt.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status===0" type="warning" size="small" @click="openHandle(row)">处理</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="page" v-model:page-size="limit" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="处理反馈" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="处理意见"><el-input v-model="form.reply" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="处理中" :value="1" /><el-option label="已处理" :value="2" /><el-option label="已驳回" :value="3" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="反馈类型" prop="type">
          <el-select v-model="editForm.type">
            <el-option v-for="(type, index) in typeMap" :key="index" :label="type" :value="index" />
          </el-select>
        </el-form-item>
        <el-form-item label="反馈内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="editForm.contact" />
        </el-form-item>
        <el-form-item label="关联充电站">
          <el-select v-model="editForm.stationId" clearable placeholder="请选择关联充电站">
            <el-option v-for="station in stationOptions" :key="station.id" :label="station.name" :value="station.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联充电桩">
          <el-select v-model="editForm.chargerId" clearable placeholder="请选择关联充电桩">
            <el-option v-for="charger in chargerOptions" :key="charger.id" :label="charger.deviceNo" :value="charger.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="editForm.status">
            <el-option v-for="(status, index) in statusMap" :key="index" :label="status" :value="index" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input v-model="editForm.reply" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
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
