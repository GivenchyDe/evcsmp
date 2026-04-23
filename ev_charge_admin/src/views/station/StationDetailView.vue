<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const stationId = route.params.id

const loading = ref(false)
const stationInfo = ref({})
const chargerList = ref([])
const gunList = ref([])

// 获取充电站信息
const fetchStationInfo = async () => {
  try {
    const res = await request.get(`/station/${stationId}`)
    if (res.success) {
      stationInfo.value = res.data
    } else {
      ElMessage.error('获取充电站信息失败')
    }
  } catch (error) {
    ElMessage.error('获取充电站信息失败')
  }
}

// 获取该站点的充电桩
const fetchChargers = async () => {
  try {
    const res = await request.get('/charger/list', { 
      params: { page: 1, limit: 1000 } 
    })
    if (res.success) {
      // 过滤出该站点的充电桩
      chargerList.value = res.data?.records?.filter(charger => charger.stationId == stationId) || []
      
      // 获取每个充电桩的充电枪
      await fetchGunsForChargers()
    }
  } catch (error) {
    console.error('获取充电桩失败', error)
  }
}

// 获取充电桩的充电枪
const fetchGunsForChargers = async () => {
  try {
    const res = await request.get('/gun/list', { 
      params: { page: 1, limit: 1000 } 
    })
    if (res.success) {
      const allGuns = res.data?.records || []
      
      // 为每个充电桩添加充电枪列表
      chargerList.value = chargerList.value.map(charger => {
        const guns = allGuns.filter(gun => gun.chargerId == charger.id)
        return {
          ...charger,
          guns: guns,
          gunCount: guns.length
        }
      })
      
      // 整理所有充电枪列表
      gunList.value = allGuns.filter(gun => {
        return chargerList.value.some(charger => charger.id == gun.chargerId)
      })
    }
  } catch (error) {
    console.error('获取充电枪失败', error)
  }
}

// 获取该站点的订单统计
const fetchOrderStats = async () => {
  try {
    const res = await request.get('/chargingOrder/list', { 
      params: { page: 1, limit: 1000, stationId: stationId } 
    })
    if (res.success) {
      const orders = res.data?.records || []
      
      // 计算统计信息
      const stats = {
        totalOrders: orders.length,
        chargingOrders: orders.filter(o => o.status === 0).length,
        completedOrders: orders.filter(o => o.status === 2).length,
        totalRevenue: orders.filter(o => o.status === 2).reduce((sum, o) => sum + (o.amount || 0), 0),
        totalElectricity: orders.reduce((sum, o) => sum + (o.electricity || 0), 0)
      }
      
      stationInfo.value.stats = stats
    }
  } catch (error) {
    console.error('获取订单统计失败', error)
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchStationInfo(),
      fetchChargers(),
      fetchOrderStats()
    ])
  } finally {
    loading.value = false
  }
})

// 状态映射
const stationStatusMap = ['维护中', '正常运营', '已停用']
const chargerStatusMap = ['故障', '正常', '维护中']
const chargerTypeMap = ['慢充', '快充']
const gunStatusMap = ['空闲', '繁忙', '故障']
const gunStatusColor = ['success', 'warning', 'danger']
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 充电站基本信息 -->
    <el-card class="mb-16">
      <template #header>
        <div class="card-header">
          <span>充电站基本信息</span>
          <el-button type="primary" @click="$router.push('/station')">返回列表</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="充电站名称">{{ stationInfo.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="stationInfo.status === 1 ? 'success' : stationInfo.status === 0 ? 'warning' : 'info'">
            {{ stationStatusMap[stationInfo.status] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址">{{ stationInfo.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ stationInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经纬度">
          {{ stationInfo.longitude && stationInfo.latitude ? `${stationInfo.longitude}, ${stationInfo.latitude}` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="营业时间">
          {{ stationInfo.openTime && stationInfo.closeTime ? `${stationInfo.openTime} - ${stationInfo.closeTime}` : '24小时' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="16" class="mb-16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stationInfo.stats?.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">¥{{ stationInfo.stats?.totalRevenue?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">总营收</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stationInfo.stats?.totalElectricity?.toFixed(1) || '0.0' }}度</div>
            <div class="stat-label">总充电量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ chargerList.length }}</div>
            <div class="stat-label">充电桩数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 充电桩列表 -->
    <el-card class="mb-16">
      <template #header>
        <div class="card-header">
          <span>充电桩列表 ({{ chargerList.length }})</span>
        </div>
      </template>
      
      <el-table :data="chargerList" border stripe>
        <el-table-column prop="deviceNo" label="设备编号" width="130" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'" size="small">
              {{ chargerTypeMap[row.type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="生产厂家" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="['danger','success','warning'][row.status] || 'info'" size="small">
              {{ chargerStatusMap[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="installDate" label="安装时间" width="120">
          <template #default="{ row }">
            {{ row.installDate ? row.installDate.slice(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="gunCount" label="充电枪数量" width="100" />
        <el-table-column label="充电枪状态" width="200">
          <template #default="{ row }">
            <div v-if="row.guns && row.guns.length > 0">
              <el-tag 
                v-for="gun in row.guns" 
                :key="gun.id"
                :type="gunStatusColor[gun.status] || 'info'"
                size="small"
                class="mr-4 mb-2"
              >
                {{ gun.gunNo }}: {{ gunStatusMap[gun.status] || '未知' }}
              </el-tag>
            </div>
            <span v-else style="color: #999;">暂无充电枪</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 充电枪状态总览 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>充电枪状态总览</span>
        </div>
      </template>
      
      <el-table :data="gunList" border stripe>
        <el-table-column prop="gunNo" label="枪号" width="100" />
        <el-table-column prop="chargerId" label="所属充电桩" width="120">
          <template #default="{ row }">
            {{ chargerList.find(c => c.id == row.chargerId)?.deviceNo || `充电桩${row.chargerId}` }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="gunStatusColor[row.status] || 'info'" size="small">
              {{ gunStatusMap[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 2" 
              type="warning" 
              size="small"
              @click="handleRepairGun(row)"
            >
              维修完成
            </el-button>
            <el-button 
              v-else-if="row.status === 0" 
              type="danger" 
              size="small"
              @click="handleMarkFault(row)"
            >
              标记故障
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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

.mr-4 {
  margin-right: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item {
  text-align: center;
  padding: 12px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #888;
  margin-top: 4px;
}
</style>