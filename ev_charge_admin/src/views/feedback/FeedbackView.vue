<script setup>
import { ref, onMounted } from 'vue'
import { getFeedbackList, handleFeedback } from '@/api/feedback.js'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const dialogVisible = ref(false)
const form = ref({})
const keyword = ref('')

const typeMap = ['网点问题','设备故障','软件异常','使用体验','其他']
const statusMap = ['待处理','处理中','已处理','已驳回']
const statusType = ['danger','warning','success','info']

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getFeedbackList(page.value, limit.value, keyword.value)
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

const openHandle = (row) => {
  form.value = { id: row.id, status: 2, reply: '' }
  dialogVisible.value = true
}

const submitHandle = async () => {
  const res = await handleFeedback(form.value)
  if (res.success) { ElMessage.success('处理成功'); dialogVisible.value = false; fetchList() }
}

onMounted(fetchList)
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
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="feedbackNo" label="编号" width="140" />
        <el-table-column prop="type" label="类型" width="90"><template #default="{row}">{{typeMap[row.type]||'未知'}}</template></el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" width="120" />
        <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="statusType[row.status]||'info'" size="small">{{statusMap[row.status]||'未知'}}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt ? row.createdAt.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button v-if="row.status===0" type="primary" size="small" @click="openHandle(row)">处理</el-button></template>
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
  </div>
</template>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-box { display: flex; gap: 8px; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
