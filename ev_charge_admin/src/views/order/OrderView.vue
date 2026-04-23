<script setup>
import { ref, onMounted } from 'vue'
import { getOrderList, deleteOrder } from '@/api/order.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)

const statusMap = ['充电中','待结算','已完成','已取消','退款中','已退款']
const statusType = ['warning','primary','success','info','danger','danger']
const keyword = ref('')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getOrderList(page.value, limit.value, keyword.value)
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

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该订单吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteOrder(row.id)
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
          <span>充电订单</span>
          <div class="search-box">
            <el-input v-model="keyword" placeholder="搜索订单号" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="stationId" label="网点ID" width="80" />
        <el-table-column prop="electricity" label="充电量" width="90"><template #default="{row}">{{row.electricity}}度</template></el-table-column>
        <el-table-column prop="amount" label="金额" width="90"><template #default="{row}"><span style="color:#f56c6c">¥{{row.amount}}</span></template></el-table-column>
        <el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag :type="statusType[row.status]||'info'" size="small">{{statusMap[row.status]||'未知'}}</el-tag></template></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ row.startTime ? row.startTime.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template #default="{ row }">
            {{ row.endTime ? row.endTime.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="paidAt" label="结算时间" width="160">
          <template #default="{ row }">
            {{ row.paidAt ? row.paidAt.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="page" v-model:page-size="limit" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>
  </div>
</template>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-box { display: flex; gap: 8px; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
