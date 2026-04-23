<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, User, Reading, Clock } from '@element-plus/icons-vue'

const loading = ref(false)
const pushLoading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keyword = ref('')

// 推送表单
const pushForm = ref({
  type: 0,
  title: '',
  content: '',
  userIds: [],
  userType: 'all', // all, specific, condition
  condition: {
    minBalance: 0,
    minOrders: 0,
    lastLoginDays: 30
  }
})

// 推送类型选项
const typeOptions = [
  { label: '订单通知', value: 0 },
  { label: '充值通知', value: 1 },
  { label: '反馈通知', value: 2 },
  { label: '系统公告', value: 3 },
  { label: '故障通知', value: 4 }
]

// 用户类型选项
const userTypeOptions = [
  { label: '所有用户', value: 'all' },
  { label: '指定用户', value: 'specific' },
  { label: '条件筛选', value: 'condition' }
]

// 获取推送历史
const fetchPushHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/message/push/history', { 
      params: { page: page.value, limit: limit.value, keyword: keyword.value } 
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { 
    loading.value = false 
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchPushHistory()
}

// 重置
const handleReset = () => {
  keyword.value = ''
  page.value = 1
  fetchPushHistory()
}

// 发送推送
const handlePush = async () => {
  if (!pushForm.value.title.trim()) {
    ElMessage.warning('请输入消息标题')
    return
  }
  if (!pushForm.value.content.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  pushLoading.value = true
  try {
    const res = await request.post('/message/push/send', pushForm.value)
    if (res.success) {
      ElMessage.success('消息推送成功')
      // 重置表单
      pushForm.value = {
        type: 0,
        title: '',
        content: '',
        userIds: [],
        userType: 'all',
        condition: {
          minBalance: 0,
          minOrders: 0,
          lastLoginDays: 30
        }
      }
      // 刷新历史记录
      fetchPushHistory()
    } else {
      ElMessage.error(res.message || '推送失败')
    }
  } catch (error) {
    console.error('推送失败', error)
    ElMessage.error('推送失败')
  } finally {
    pushLoading.value = false
  }
}

// 删除推送记录
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除这条推送记录吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await request.get('/message/push/delete', { params: { id: row.id } })
    if (res.success) {
      ElMessage.success('删除成功')
      fetchPushHistory()
    }
  })
}

// 获取用户列表（用于选择指定用户）
const userList = ref([])
const userLoading = ref(false)
const fetchUserList = async () => {
  userLoading.value = true
  try {
    const res = await request.get('/user/list', { params: { page: 1, limit: 1000 } })
    userList.value = res.data?.records || []
  } finally {
    userLoading.value = false
  }
}

// 获取推送统计
const pushStats = ref({})
const fetchPushStats = async () => {
  try {
    const res = await request.get('/message/push/stats')
    pushStats.value = res.data || {}
  } catch (error) {
    console.error('获取推送统计失败', error)
  }
}

onMounted(() => {
  fetchPushHistory()
  fetchPushStats()
  fetchUserList()
})
</script>

<template>
  <div class="page-container">
    <!-- 推送统计 -->
    <el-row :gutter="16" class="mb-16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff20; color: #409eff;">
              <el-icon size="28"><Message /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ pushStats.totalPushes || 0 }}</div>
              <div class="stat-label">总推送次数</div>
              <div class="stat-detail">今日: {{ pushStats.todayPushes || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a20; color: #67c23a;">
              <el-icon size="28"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ pushStats.totalUsers || 0 }}</div>
              <div class="stat-label">覆盖用户数</div>
              <div class="stat-detail">活跃: {{ pushStats.activeUsers || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c20; color: #e6a23c;">
              <el-icon size="28"><Reading /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ pushStats.readRate || 0 }}%</div>
              <div class="stat-label">平均阅读率</div>
              <div class="stat-detail">7日平均</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c20; color: #f56c6c;">
              <el-icon size="28"><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ pushStats.avgResponseTime || 0 }}s</div>
              <div class="stat-label">平均响应时间</div>
              <div class="stat-detail">用户点击响应</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 消息推送表单 -->
    <el-card class="mb-16">
      <template #header>
        <div class="card-header">
          <span>发送消息推送</span>
        </div>
      </template>
      
      <el-form :model="pushForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="消息类型" required>
              <el-select v-model="pushForm.type" placeholder="请选择消息类型" style="width:100%">
                <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="推送对象" required>
              <el-select v-model="pushForm.userType" placeholder="请选择推送对象" style="width:100%">
                <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 指定用户选择 -->
        <el-form-item v-if="pushForm.userType === 'specific'" label="选择用户">
          <el-select
            v-model="pushForm.userIds"
            multiple
            filterable
            placeholder="请选择用户"
            style="width:100%"
            :loading="userLoading"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.username} (${user.phone})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <!-- 条件筛选 -->
        <div v-if="pushForm.userType === 'condition'" class="condition-panel">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="最低余额">
                <el-input-number v-model="pushForm.condition.minBalance" :min="0" :step="10" placeholder="元" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="最少订单数">
                <el-input-number v-model="pushForm.condition.minOrders" :min="0" :step="1" placeholder="个" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="最近登录">
                <el-input-number v-model="pushForm.condition.lastLoginDays" :min="1" :step="1" placeholder="天内" style="width:100%" />
                <span class="condition-hint">天内登录过</span>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-form-item label="消息标题" required>
          <el-input v-model="pushForm.title" placeholder="请输入消息标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="消息内容" required>
          <el-input
            v-model="pushForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handlePush" :loading="pushLoading">发送推送</el-button>
          <el-button @click="pushForm = {
            type: 0,
            title: '',
            content: '',
            userIds: [],
            userType: 'all',
            condition: {
              minBalance: 0,
              minOrders: 0,
              lastLoginDays: 30
            }
          }">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 推送历史记录 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>推送历史记录</span>
          <div class="search-box">
            <el-input v-model="keyword" placeholder="搜索标题/内容" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{row}">
            <el-tag :type="row.type === 0 ? 'warning' : row.type === 1 ? 'success' : row.type === 2 ? 'info' : row.type === 3 ? 'primary' : 'danger'" size="small">
              {{ typeOptions.find(t => t.value === row.type)?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="userCount" label="推送用户数" width="110" />
        <el-table-column prop="readCount" label="已读用户数" width="110" />
        <el-table-column prop="readRate" label="阅读率" width="100">
          <template #default="{row}">
            <span :class="row.readRate >= 50 ? 'high' : row.readRate >= 30 ? 'medium' : 'low'">
              {{ row.readRate || 0 }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="推送时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt ? row.createdAt.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination 
        class="pagination" 
        v-model:current-page="page" 
        v-model:page-size="limit" 
        :total="total" 
        layout="total, prev, pager, next" 
        @current-change="fetchPushHistory" 
      />
    </el-card>
  </div>
</template>

<style scoped>
.page-container {
  padding: 16px;
}

.mb-16 {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box {
  display: flex;
  gap: 8px;
  align-items: center;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 4px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #888;
  margin-top: 2px;
}

.stat-detail {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.condition-panel {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.condition-hint {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.high {
  color: #67c23a;
  font-weight: 500;
}

.medium {
  color: #e6a23c;
  font-weight: 500;
}

.low {
  color: #f56c6c;
  font-weight: 500;
}
</style>