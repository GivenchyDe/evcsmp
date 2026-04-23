<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { Refresh, SetUp, Lightning, Document, Warning } from '@element-plus/icons-vue'

const loading = ref(false)
const chargingOrders = ref([])
const stationStats = ref([])
const deviceStats = ref({})
const refreshInterval = ref(null)

// 获取正在充电的订单
const fetchChargingOrders = async () => {
  try {
    const res = await request.get('/chargingOrder/listByStatus', { 
      params: { status: 0 } 
    })
    if (res.success) {
      chargingOrders.value = res.data || []
      
      // 获取订单的关联信息
      await enrichOrderInfo()
    }
  } catch (error) {
    console.error('获取充电订单失败', error)
  }
}

// 丰富订单信息
const enrichOrderInfo = async () => {
  if (chargingOrders.value.length === 0) return
  
  // 获取用户信息
  const userIds = [...new Set(chargingOrders.value.map(item => item.userId))]
  const userRes = await request.get('/user/list', { 
    params: { page: 1, limit: 1000 } 
  })
  const userMap = {}
  userRes.data?.records?.forEach(user => {
    userMap[user.id] = user
  })
  
  // 获取充电站信息
  const stationIds = [...new Set(chargingOrders.value.map(item => item.stationId))]
  const stationRes = await request.get('/station/list', { 
    params: { page: 1, limit: 1000 } 
  })
  const stationMap = {}
  stationRes.data?.records?.forEach(station => {
    stationMap[station.id] = station
  })
  
  // 获取充电桩信息
  const chargerIds = [...new Set(chargingOrders.value.map(item => item.chargerId))]
  const chargerRes = await request.get('/charger/list', { 
    params: { page: 1, limit: 1000 } 
  })
  const chargerMap = {}
  chargerRes.data?.records?.forEach(charger => {
    chargerMap[charger.id] = charger
  })
  
  // 获取充电枪信息
  const gunIds = [...new Set(chargingOrders.value.map(item => item.gunId))]
  const gunRes = await request.get('/gun/list', { 
    params: { page: 1, limit: 1000 } 
  })
  const gunMap = {}
  gunRes.data?.records?.forEach(gun => {
    gunMap[gun.id] = gun
  })
  
  // 为每个订单添加关联信息
  chargingOrders.value = chargingOrders.value.map(order => {
    const user = userMap[order.userId]
    const station = stationMap[order.stationId]
    const charger = chargerMap[order.chargerId]
    const gun = gunMap[order.gunId]
    
    // 计算充电时长
    const startTime = new Date(order.startTime)
    const now = new Date()
    const durationMs = now - startTime
    const durationMinutes = Math.floor(durationMs / (1000 * 60))
    
    return {
      ...order,
      userName: user?.username || `用户${order.userId}`,
      userPhone: user?.phone || '',
      stationName: station?.name || `充电站${order.stationId}`,
      chargerNo: charger?.deviceNo || `充电桩${order.chargerId}`,
      gunNo: gun?.gunNo || `充电枪${order.gunId}`,
      duration: durationMinutes,
      progress: Math.min(100, Math.floor((durationMinutes / 60) * 100)) // 假设最大充电时长60分钟
    }
  })
}

// 获取充电站统计
const fetchStationStats = async () => {
  try {
    const res = await request.get('/station/list', { 
      params: { page: 1, limit: 1000 } 
    })
    if (res.success) {
      const stations = res.data?.records || []
      
      // 为每个充电站获取订单统计
      stationStats.value = await Promise.all(
        stations.map(async station => {
          const orderRes = await request.get('/chargingOrder/list', { 
            params: { page: 1, limit: 1000, stationId: station.id } 
          })
          const orders = orderRes.data?.records || []
          
          const chargingOrders = orders.filter(o => o.status === 0).length
          const completedOrders = orders.filter(o => o.status === 2).length
          const totalRevenue = orders.filter(o => o.status === 2).reduce((sum, o) => sum + (o.amount || 0), 0)
          
          return {
            ...station,
            chargingOrders,
            completedOrders,
            totalRevenue
          }
        })
      )
    }
  } catch (error) {
    console.error('获取充电站统计失败', error)
  }
}

// 获取设备统计和报警信息
const fetchDeviceStats = async () => {
  try {
    // 获取充电桩统计
    const chargerRes = await request.get('/charger/list', { 
      params: { page: 1, limit: 1000 } 
    })
    const chargers = chargerRes.data?.records || []
    
    // 获取充电枪统计
    const gunRes = await request.get('/gun/list', { 
      params: { page: 1, limit: 1000 } 
    })
    const guns = gunRes.data?.records || []
    
    // 获取充电站信息用于关联显示
    const stationRes = await request.get('/station/list', { 
      params: { page: 1, limit: 1000 } 
    })
    const stationMap = {}
    stationRes.data?.records?.forEach(station => {
      stationMap[station.id] = station
    })
    
    // 获取充电桩的充电站信息
    const chargerStationMap = {}
    chargers.forEach(charger => {
      if (charger.stationId && stationMap[charger.stationId]) {
        chargerStationMap[charger.id] = stationMap[charger.stationId].name
      }
    })
    
    // 获取故障设备列表
    const faultChargers = chargers.filter(c => c.status === 0)
    const faultGuns = guns.filter(g => g.status === 2)
    
    // 计算设备使用率
    const totalGuns = guns.length
    const busyGuns = guns.filter(g => g.status === 1).length
    const gunUtilization = totalGuns > 0 ? (busyGuns / totalGuns * 100).toFixed(1) : 0
    
    // 计算故障率
    const chargerFaultRate = chargers.length > 0 ? (faultChargers.length / chargers.length * 100).toFixed(1) : 0
    const gunFaultRate = totalGuns > 0 ? (faultGuns.length / totalGuns * 100).toFixed(1) : 0
    
    deviceStats.value = {
      totalChargers: chargers.length,
      normalChargers: chargers.filter(c => c.status === 1).length,
      faultChargers: faultChargers.length,
      maintainingChargers: chargers.filter(c => c.status === 2).length,
      totalGuns: totalGuns,
      idleGuns: guns.filter(g => g.status === 0).length,
      busyGuns: busyGuns,
      faultGuns: faultGuns.length,
      gunUtilization,
      chargerFaultRate,
      gunFaultRate,
      faultDeviceList: {
        chargers: faultChargers.map(charger => ({
          ...charger,
          stationName: chargerStationMap[charger.id] || '未知充电站'
        })),
        guns: faultGuns.map(gun => {
          const charger = chargers.find(c => c.id === gun.chargerId)
          return {
            ...gun,
            chargerNo: charger?.deviceNo || `充电桩${gun.chargerId}`,
            stationName: charger ? chargerStationMap[charger.id] : '未知充电站'
          }
        })
      }
    }
    
    // 发送故障报警
    await sendFaultAlert(deviceStats.value)
  } catch (error) {
    console.error('获取设备统计失败', error)
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }
  refreshInterval.value = setInterval(() => {
    fetchChargingOrders()
    fetchStationStats()
    fetchDeviceStats()
  }, 10000) // 每10秒刷新一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
    refreshInterval.value = null
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchChargingOrders(),
      fetchStationStats(),
      fetchDeviceStats()
    ])
    startAutoRefresh()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  stopAutoRefresh()
})

// 手动刷新
const handleRefresh = () => {
  loading.value = true
  Promise.all([
    fetchChargingOrders(),
    fetchStationStats(),
    fetchDeviceStats()
  ]).finally(() => {
    loading.value = false
  })
}

// 处理设备维修
const handleRepairDevice = async (device, type) => {
  try {
    let api = ''
    let data = {}
    let deviceName = ''
    
    if (type === 'charger') {
      api = '/charger/update'
      data = {
        id: device.id,
        status: 2 // 维护中
      }
      deviceName = `充电桩 ${device.deviceNo}`
    } else if (type === 'gun') {
      api = '/gun/update'
      data = {
        id: device.id,
        status: 0 // 空闲（维修完成）
      }
      deviceName = `充电枪 ${device.gunNo}`
    }
    
    const res = await request.post(api, data)
    if (res.success) {
      ElMessage.success('设备状态已更新')
      
      // 发送维修通知
      await sendRepairNotification(device, type, deviceName)
      
      fetchDeviceStats()
    }
  } catch (error) {
    console.error('更新设备状态失败', error)
    ElMessage.error('更新失败')
  }
}

// 发送维修通知
const sendRepairNotification = async (device, type, deviceName) => {
  try {
    // 获取所有管理员
    const adminRes = await request.get('/admin/list', { params: { page: 1, limit: 100 } })
    const admins = adminRes.data?.records || []
    
    // 为每个管理员发送通知
    for (const admin of admins) {
      const messageData = {
        userId: admin.id,
        type: 4, // 故障通知
        title: '设备维修通知',
        content: `${deviceName}（${device.stationName}）已被标记为维修状态，请及时处理。`,
        isRead: 0
      }
      
      await request.post('/message/add', messageData)
    }
    
    console.log('维修通知已发送给所有管理员')
  } catch (error) {
    console.error('发送维修通知失败', error)
  }
}

// 发送故障报警
const sendFaultAlert = async (deviceStats) => {
  try {
    // 检查是否有故障设备
    if ((deviceStats.faultChargers > 0 || deviceStats.faultGuns > 0) && deviceStats.faultAlertSent !== true) {
      // 获取所有管理员
      const adminRes = await request.get('/admin/list', { params: { page: 1, limit: 100 } })
      const admins = adminRes.data?.records || []
      
      // 构建报警消息
      let alertContent = '系统检测到以下故障设备：\n'
      
      if (deviceStats.faultChargers > 0) {
        alertContent += `- 故障充电桩: ${deviceStats.faultChargers}个\n`
      }
      
      if (deviceStats.faultGuns > 0) {
        alertContent += `- 故障充电枪: ${deviceStats.faultGuns}个\n`
      }
      
      alertContent += '\n请及时登录系统查看并处理。'
      
      // 为每个管理员发送报警
      for (const admin of admins) {
        const messageData = {
          userId: admin.id,
          type: 4, // 故障通知
          title: '设备故障报警',
          content: alertContent,
          isRead: 0
        }
        
        await request.post('/message/add', messageData)
      }
      
      console.log('故障报警已发送给所有管理员')
      deviceStats.faultAlertSent = true
    }
  } catch (error) {
    console.error('发送故障报警失败', error)
  }
}

// 获取利用率样式类
const getUtilizationClass = (utilization) => {
  if (utilization >= 80) return 'high'
  if (utilization >= 50) return 'medium'
  return 'low'
}

// 获取故障率样式类
const getFaultRateClass = (faultRate) => {
  if (faultRate >= 20) return 'high'
  if (faultRate >= 10) return 'medium'
  return 'low'
}

// 获取健康度样式类
const getHealthClass = (stats) => {
  const score = calculateHealthScore(stats)
  if (score >= 80) return 'high'
  if (score >= 60) return 'medium'
  return 'low'
}

// 计算设备健康度评分
const calculateHealthScore = (stats) => {
  if (!stats) return 0
  
  const weights = {
    gunUtilization: 0.3,  // 利用率权重
    chargerFaultRate: 0.3, // 充电桩故障率权重
    gunFaultRate: 0.4      // 充电枪故障率权重
  }
  
  // 利用率越高越好（0-100%）
  const utilizationScore = (stats.gunUtilization || 0) * weights.gunUtilization
  
  // 故障率越低越好（0-100%）
  const chargerFaultScore = (100 - (stats.chargerFaultRate || 0)) * weights.chargerFaultRate
  const gunFaultScore = (100 - (stats.gunFaultRate || 0)) * weights.gunFaultRate
  
  const totalScore = utilizationScore + chargerFaultScore + gunFaultScore
  return Math.min(100, Math.max(0, totalScore)).toFixed(1)
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 标题和刷新按钮 -->
    <div class="header">
      <h2>实时监控面板</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleRefresh" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-tag type="success" size="large">自动刷新中 (10秒)</el-tag>
      </div>
    </div>

    <!-- 设备状态统计 -->
    <el-row :gutter="16" class="mb-16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff20; color: #409eff;">
              <el-icon size="28"><SetUp /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ deviceStats.totalChargers || 0 }}</div>
              <div class="stat-label">充电桩总数</div>
              <div class="stat-detail">
                <span class="normal">正常: {{ deviceStats.normalChargers || 0 }}</span>
                <span class="fault ml-8">故障: {{ deviceStats.faultChargers || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a20; color: #67c23a;">
              <el-icon size="28"><Lightning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ deviceStats.totalGuns || 0 }}</div>
              <div class="stat-label">充电枪总数</div>
              <div class="stat-detail">
                <span class="idle">空闲: {{ deviceStats.idleGuns || 0 }}</span>
                <span class="busy ml-8">繁忙: {{ deviceStats.busyGuns || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c20; color: #e6a23c;">
              <el-icon size="28"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ chargingOrders.length }}</div>
              <div class="stat-label">正在充电订单</div>
              <div class="stat-detail">
                实时监控中
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c20; color: #f56c6c;">
              <el-icon size="28"><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ deviceStats.faultGuns || 0 }}</div>
              <div class="stat-label">故障设备</div>
              <div class="stat-detail">
                需要及时处理
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 正在充电订单 -->
    <el-card class="mb-16">
      <template #header>
        <div class="card-header">
          <span>正在充电订单 ({{ chargingOrders.length }})</span>
          <el-tag type="warning">实时更新</el-tag>
        </div>
      </template>
      
      <el-table :data="chargingOrders" border stripe>
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
        <el-table-column prop="electricity" label="充电量" width="90">
          <template #default="{row}">{{row.electricity || '0.0'}}度</template>
        </el-table-column>
        <el-table-column prop="amount" label="预估费用" width="100">
          <template #default="{row}">
            <span style="color:#f56c6c;font-weight:500">¥{{row.amount || '0.00'}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="充电时长" width="100">
          <template #default="{row}">{{row.duration || 0}}分钟</template>
        </el-table-column>
        <el-table-column label="充电进度" width="150">
          <template #default="{row}">
            <el-progress 
              :percentage="row.progress || 0" 
              :stroke-width="12"
              :color="row.progress > 80 ? '#67c23a' : '#409eff'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ row.startTime ? row.startTime.slice(0, 19).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 故障设备报警 -->
    <el-card class="mb-16" v-if="deviceStats.faultChargers > 0 || deviceStats.faultGuns > 0">
      <template #header>
        <div class="card-header">
          <span class="alarm-title">
            <el-icon color="#f56c6c"><Warning /></el-icon>
            故障设备报警
          </span>
          <el-tag type="danger">需要及时处理</el-tag>
        </div>
      </template>
      
      <el-alert
        v-if="deviceStats.faultChargers > 0"
        :title="`${deviceStats.faultChargers}个充电桩故障`"
        type="error"
        :closable="false"
        class="mb-8"
      />
      
      <el-alert
        v-if="deviceStats.faultGuns > 0"
        :title="`${deviceStats.faultGuns}个充电枪故障`"
        type="error"
        :closable="false"
        class="mb-8"
      />
      
      <!-- 故障充电桩列表 -->
      <div v-if="deviceStats.faultDeviceList?.chargers?.length > 0" class="mb-16">
        <h4>故障充电桩列表</h4>
        <el-table :data="deviceStats.faultDeviceList.chargers" border stripe size="small">
          <el-table-column prop="deviceNo" label="设备编号" width="130" />
          <el-table-column prop="stationName" label="所属充电站" width="150" />
          <el-table-column prop="manufacturer" label="生产厂家" width="120" />
          <el-table-column prop="type" label="类型" width="80">
            <template #default="{ row }">
              <el-tag :type="row.type === 1 ? 'success' : 'warning'" size="small">
                {{ row.type === 1 ? '快充' : '慢充' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{row}">
              <el-button type="warning" size="small" @click="handleRepairDevice(row, 'charger')">
                标记维修
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 故障充电枪列表 -->
      <div v-if="deviceStats.faultDeviceList?.guns?.length > 0">
        <h4>故障充电枪列表</h4>
        <el-table :data="deviceStats.faultDeviceList.guns" border stripe size="small">
          <el-table-column prop="gunNo" label="枪号" width="100" />
          <el-table-column prop="chargerNo" label="所属充电桩" width="130" />
          <el-table-column prop="stationName" label="所属充电站" width="150" />
          <el-table-column label="操作" width="100">
            <template #default="{row}">
              <el-button type="warning" size="small" @click="handleRepairDevice(row, 'gun')">
                标记维修
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 设备性能监控 -->
    <el-card class="mb-16">
      <template #header>
        <div class="card-header">
          <span>设备性能监控</span>
        </div>
      </template>
      
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="performance-card">
            <div class="performance-title">充电枪利用率</div>
            <div class="performance-value" :class="getUtilizationClass(deviceStats.gunUtilization)">
              {{ deviceStats.gunUtilization || 0 }}%
            </div>
            <div class="performance-detail">
              繁忙: {{ deviceStats.busyGuns || 0 }} / 总数: {{ deviceStats.totalGuns || 0 }}
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="performance-card">
            <div class="performance-title">充电桩故障率</div>
            <div class="performance-value" :class="getFaultRateClass(deviceStats.chargerFaultRate)">
              {{ deviceStats.chargerFaultRate || 0 }}%
            </div>
            <div class="performance-detail">
              故障: {{ deviceStats.faultChargers || 0 }} / 总数: {{ deviceStats.totalChargers || 0 }}
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="performance-card">
            <div class="performance-title">充电枪故障率</div>
            <div class="performance-value" :class="getFaultRateClass(deviceStats.gunFaultRate)">
              {{ deviceStats.gunFaultRate || 0 }}%
            </div>
            <div class="performance-detail">
              故障: {{ deviceStats.faultGuns || 0 }} / 总数: {{ deviceStats.totalGuns || 0 }}
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="performance-card">
            <div class="performance-title">设备健康度</div>
            <div class="performance-value" :class="getHealthClass(deviceStats)">
              {{ calculateHealthScore(deviceStats) }}%
            </div>
            <div class="performance-detail">
              综合评估设备运行状态
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 充电站状态 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>充电站状态监控</span>
        </div>
      </template>
      
      <el-table :data="stationStats" border stripe>
        <el-table-column prop="name" label="充电站名称" width="200" />
        <el-table-column prop="status" label="运营状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'" size="small">
              {{ ['维护中','正常运营','已停用'][row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chargingOrders" label="正在充电" width="100">
          <template #default="{row}">
            <span :class="row.chargingOrders > 0 ? 'busy' : 'idle'">
              {{ row.chargingOrders }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="completedOrders" label="已完成订单" width="110" />
        <el-table-column prop="totalRevenue" label="总营收" width="120">
          <template #default="{row}">
            <span style="color:#f56c6c;font-weight:500">¥{{row.totalRevenue?.toFixed(2) || '0.00'}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="$router.push(`/station-detail/${row.id}`)">
              详情
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

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.mb-16 {
  margin-bottom: 16px;
}

.ml-8 {
  margin-left: 8px;
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
  font-size: 28px;
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

.stat-detail .normal {
  color: #67c23a;
}

.stat-detail .fault {
  color: #f56c6c;
}

.stat-detail .idle {
  color: #409eff;
}

.stat-detail .busy {
  color: #e6a23c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.busy {
  color: #e6a23c;
  font-weight: 500;
}

.idle {
  color: #67c23a;
  font-weight: 500;
}

.alarm-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #f56c6c;
}

.performance-card {
  padding: 16px;
  text-align: center;
  border-radius: 8px;
  background: #f8f9fa;
}

.performance-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.performance-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.performance-value.high {
  color: #67c23a;
}

.performance-value.medium {
  color: #e6a23c;
}

.performance-value.low {
  color: #f56c6c;
}

.performance-detail {
  font-size: 12px;
  color: #999;
}
</style>