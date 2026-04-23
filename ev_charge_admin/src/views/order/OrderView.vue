<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
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
      const stationIds = [...new Set(list.value.map(item => item.stationId))]
      const stationRes = await request.get('/station/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const stationMap = {}
      stationRes.data?.records?.forEach(station => {
        stationMap[station.id] = station
      })
      
      // 获取充电桩信息
      const chargerIds = [...new Set(list.value.map(item => item.chargerId))]
      const chargerRes = await request.get('/charger/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const chargerMap = {}
      chargerRes.data?.records?.forEach(charger => {
        chargerMap[charger.id] = charger
      })
      
      // 获取充电枪信息
      const gunIds = [...new Set(list.value.map(item => item.gunId))]
      const gunRes = await request.get('/gun/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const gunMap = {}
      gunRes.data?.records?.forEach(gun => {
        gunMap[gun.id] = gun
      })
      
      // 为每个订单添加关联信息
      list.value = list.value.map(order => {
        const user = userMap[order.userId]
        const station = stationMap[order.stationId]
        const charger = chargerMap[order.chargerId]
        const gun = gunMap[order.gunId]
        
        return {
          ...order,
          userName: user?.username || `用户${order.userId}`,
          userPhone: user?.phone || '',
          stationName: station?.name || `充电站${order.stationId}`,
          chargerNo: charger?.deviceNo || `充电桩${order.chargerId}`,
          gunNo: gun?.gunNo || `充电枪${order.gunId}`
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
        <el-table-column prop="userName" label="用户" width="120">
          <template #default="{row}">
            <div>{{row.userName}}</div>
            <div style="font-size:12px;color:#999">{{row.userPhone}}</div>
          </template>
        </el-table-column>
        <el-table-column prop="stationName" label="充电站" width="150" />
        <el-table-column prop="chargerNo" label="充电桩" width="120" />
        <el-table-column prop="gunNo" label="充电枪" width="100" />
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
