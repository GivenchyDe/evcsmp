<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Download, Document, Money, User, SetUp } from '@element-plus/icons-vue'

const loading = ref(false)
const dateRange = ref([new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()]) // 默认最近30天
const reportType = ref('daily') // daily, weekly, monthly
const revenueStats = ref({})
const orderStats = ref({})
const userStats = ref({})
const deviceStats = ref({})
const peakHours = ref([])
const userBehavior = ref({})
const exportLoading = ref(false)

let revenueChart = null
let orderChart = null
let userChart = null
let deviceChart = null

// 获取营收统计
const fetchRevenueStats = async () => {
  try {
    const [startDate, endDate] = dateRange.value
    const startStr = startDate.toISOString().split('T')[0]
    const endStr = endDate.toISOString().split('T')[0]
    
    // 获取订单数据
    const orderRes = await request.get('/chargingOrder/list', { 
      params: { page: 1, limit: 10000 } 
    })
    const orders = orderRes.data?.records || []
    
    // 过滤日期范围内的已完成订单
    const filteredOrders = orders.filter(order => {
      if (order.status !== 2 || !order.paidAt) return false
      const paidDate = new Date(order.paidAt)
      return paidDate >= startDate && paidDate <= endDate
    })
    
    // 计算统计
    const totalRevenue = filteredOrders.reduce((sum, order) => sum + (order.amount || 0), 0)
    const avgOrderValue = filteredOrders.length > 0 ? totalRevenue / filteredOrders.length : 0
    
    // 按日期分组
    const revenueByDate = {}
    filteredOrders.forEach(order => {
      if (order.paidAt) {
        const date = order.paidAt.split('T')[0]
        revenueByDate[date] = (revenueByDate[date] || 0) + (order.amount || 0)
      }
    })
    
    // 按充电站分组
    const revenueByStation = {}
    const stationIds = [...new Set(filteredOrders.map(order => order.stationId))]
    
    if (stationIds.length > 0) {
      const stationRes = await request.get('/station/list', { 
        params: { page: 1, limit: 1000 } 
      })
      const stationMap = {}
      stationRes.data?.records?.forEach(station => {
        stationMap[station.id] = station
      })
      
      filteredOrders.forEach(order => {
        const station = stationMap[order.stationId]
        const stationName = station?.name || `充电站${order.stationId}`
        revenueByStation[stationName] = (revenueByStation[stationName] || 0) + (order.amount || 0)
      })
    }
    
    revenueStats.value = {
      totalRevenue,
      avgOrderValue,
      orderCount: filteredOrders.length,
      revenueByDate,
      revenueByStation
    }
    
    // 渲染营收图表
    renderRevenueChart()
  } catch (error) {
    console.error('获取营收统计失败', error)
  }
}

// 获取订单统计
const fetchOrderStats = async () => {
  try {
    const orderRes = await request.get('/chargingOrder/list', { 
      params: { page: 1, limit: 10000 } 
    })
    const orders = orderRes.data?.records || []
    
    // 订单状态统计
    const statusCount = {
      0: 0, // 充电中
      1: 0, // 待结算
      2: 0, // 已完成
      3: 0, // 已取消
      4: 0, // 退款中
      5: 0  // 已退款
    }
    
    orders.forEach(order => {
      if (order.status !== undefined && order.status !== null) {
        statusCount[order.status] = (statusCount[order.status] || 0) + 1
      }
    })
    
    // 订单趋势（按创建日期）
    const ordersByDate = {}
    orders.forEach(order => {
      if (order.createdAt) {
        const date = order.createdAt.split('T')[0]
        ordersByDate[date] = (ordersByDate[date] || 0) + 1
      }
    })
    
    orderStats.value = {
      totalOrders: orders.length,
      statusCount,
      ordersByDate
    }
    
    // 渲染订单图表
    renderOrderChart()
  } catch (error) {
    console.error('获取订单统计失败', error)
  }
}

// 获取用户统计
const fetchUserStats = async () => {
  try {
    const userRes = await request.get('/user/list', { 
      params: { page: 1, limit: 10000 } 
    })
    const users = userRes.data?.records || []
    
    // 用户状态统计
    const activeUsers = users.filter(user => user.status === 1).length
    const inactiveUsers = users.filter(user => user.status === 0).length
    
    // 用户余额统计
    const totalBalance = users.reduce((sum, user) => sum + (user.balance || 0), 0)
    const avgBalance = users.length > 0 ? totalBalance / users.length : 0
    
    // 用户增长趋势
    const usersByDate = {}
    users.forEach(user => {
      if (user.createdAt) {
        const date = user.createdAt.split('T')[0]
        usersByDate[date] = (usersByDate[date] || 0) + 1
      }
    })
    
    userStats.value = {
      totalUsers: users.length,
      activeUsers,
      inactiveUsers,
      totalBalance,
      avgBalance,
      usersByDate
    }
    
    // 渲染用户图表
    renderUserChart()
  } catch (error) {
    console.error('获取用户统计失败', error)
  }
}

// 获取设备统计
const fetchDeviceStats = async () => {
  try {
    // 充电桩统计
    const chargerRes = await request.get('/charger/list', { 
      params: { page: 1, limit: 1000 } 
    })
    const chargers = chargerRes.data?.records || []
    
    // 充电枪统计
    const gunRes = await request.get('/gun/list', { 
      params: { page: 1, limit: 1000 } 
    })
    const guns = gunRes.data?.records || []
    
    // 充电站统计
    const stationRes = await request.get('/station/list', { 
      params: { page: 1, limit: 1000 } 
    })
    const stations = stationRes.data?.records || []
    
    // 设备利用率（繁忙充电枪比例）
    const busyGuns = guns.filter(gun => gun.status === 1).length
    const gunUtilization = guns.length > 0 ? (busyGuns / guns.length * 100).toFixed(1) : 0
    
    // 设备故障率
    const faultChargers = chargers.filter(charger => charger.status === 0).length
    const chargerFaultRate = chargers.length > 0 ? (faultChargers / chargers.length * 100).toFixed(1) : 0
    
    deviceStats.value = {
      totalStations: stations.length,
      totalChargers: chargers.length,
      totalGuns: guns.length,
      gunUtilization,
      chargerFaultRate,
      stationStatus: {
        normal: stations.filter(s => s.status === 1).length,
        maintaining: stations.filter(s => s.status === 0).length,
        stopped: stations.filter(s => s.status === 2).length
      },
      chargerStatus: {
        normal: chargers.filter(c => c.status === 1).length,
        fault: chargers.filter(c => c.status === 0).length,
        maintaining: chargers.filter(c => c.status === 2).length
      },
      gunStatus: {
        idle: guns.filter(g => g.status === 0).length,
        busy: guns.filter(g => g.status === 1).length,
        fault: guns.filter(g => g.status === 2).length
      }
    }
    
    // 渲染设备图表
    renderDeviceChart()
  } catch (error) {
    console.error('获取设备统计失败', error)
  }
}

// 获取高峰期分析
const fetchPeakHoursAnalysis = async () => {
  try {
    const orderRes = await request.get('/chargingOrder/list', { 
      params: { page: 1, limit: 10000 } 
    })
    const orders = orderRes.data?.records || []
    
    // 按小时统计订单数量
    const hourCount = Array(24).fill(0)
    orders.forEach(order => {
      if (order.startTime) {
        const startTime = new Date(order.startTime)
        const hour = startTime.getHours()
        hourCount[hour]++
      }
    })
    
    // 找出高峰期
    const peakHoursData = hourCount.map((count, hour) => ({
      hour: `${hour}:00`,
      count,
      percentage: orders.length > 0 ? ((count / orders.length) * 100).toFixed(1) : 0
    }))
    
    // 按数量排序，取前5个高峰期
    peakHours.value = peakHoursData
      .sort((a, b) => b.count - a.count)
      .slice(0, 5)
    
    // 渲染高峰期图表
    renderPeakHoursChart()
  } catch (error) {
    console.error('获取高峰期分析失败', error)
  }
}

// 获取用户行为分析
const fetchUserBehaviorAnalysis = async () => {
  try {
    const userRes = await request.get('/user/list', { 
      params: { page: 1, limit: 10000 } 
    })
    const users = userRes.data?.records || []
    
    const orderRes = await request.get('/chargingOrder/list', { 
      params: { page: 1, limit: 10000 } 
    })
    const orders = orderRes.data?.records || []
    
    // 用户充电频率分析
    const userOrderCount = {}
    orders.forEach(order => {
      userOrderCount[order.userId] = (userOrderCount[order.userId] || 0) + 1
    })
    
    const frequencyDistribution = {
      low: 0,    // 1-2次
      medium: 0, // 3-5次
      high: 0    // 6次以上
    }
    
    Object.values(userOrderCount).forEach(count => {
      if (count <= 2) frequencyDistribution.low++
      else if (count <= 5) frequencyDistribution.medium++
      else frequencyDistribution.high++
    })
    
    // 用户充值金额分析
    const rechargeRes = await request.get('/rechargeOrder/list', { 
      params: { page: 1, limit: 10000 } 
    })
    const recharges = rechargeRes.data?.records || []
    
    const userRechargeTotal = {}
    recharges.forEach(recharge => {
      if (recharge.status === 1) { // 已完成
        userRechargeTotal[recharge.userId] = (userRechargeTotal[recharge.userId] || 0) + (recharge.amount || 0)
      }
    })
    
    const rechargeDistribution = {
      low: 0,    // 0-100元
      medium: 0, // 101-500元
      high: 0    // 501元以上
    }
    
    Object.values(userRechargeTotal).forEach(amount => {
      if (amount <= 100) rechargeDistribution.low++
      else if (amount <= 500) rechargeDistribution.medium++
      else rechargeDistribution.high++
    })
    
    userBehavior.value = {
      totalUsers: users.length,
      activeUsers: users.filter(u => u.status === 1).length,
      totalOrders: orders.length,
      avgOrdersPerUser: users.length > 0 ? (orders.length / users.length).toFixed(1) : 0,
      frequencyDistribution,
      rechargeDistribution,
      avgRechargeAmount: Object.values(userRechargeTotal).length > 0 
        ? (Object.values(userRechargeTotal).reduce((sum, amount) => sum + amount, 0) / Object.values(userRechargeTotal).length).toFixed(2)
        : 0
    }
    
    // 渲染用户行为图表
    renderUserBehaviorChart()
  } catch (error) {
    console.error('获取用户行为分析失败', error)
  }
}

// 渲染营收图表
const renderRevenueChart = () => {
  const chartDom = document.getElementById('revenue-chart')
  if (!chartDom) return
  
  if (revenueChart) {
    revenueChart.dispose()
  }
  
  revenueChart = echarts.init(chartDom)
  
  const dates = Object.keys(revenueStats.value.revenueByDate || {}).sort()
  const revenueData = dates.map(date => revenueStats.value.revenueByDate[date])
  
  const option = {
    title: {
      text: '营收趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>营收: ¥{c}'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '营收(元)'
    },
    series: [{
      type: 'line',
      data: revenueData,
      smooth: true,
      lineStyle: {
        color: '#409eff',
        width: 3
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])
      },
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      }
    }]
  }
  
  revenueChart.setOption(option)
}

// 渲染订单图表
const renderOrderChart = () => {
  const chartDom = document.getElementById('order-chart')
  if (!chartDom) return
  
  if (orderChart) {
    orderChart.dispose()
  }
  
  orderChart = echarts.init(chartDom)
  
  const statusLabels = ['充电中', '待结算', '已完成', '已取消', '退款中', '已退款']
  const statusColors = ['#e6a23c', '#409eff', '#67c23a', '#909399', '#f56c6c', '#ff5252']
  const statusData = statusLabels.map((label, index) => ({
    name: label,
    value: orderStats.value.statusCount[index] || 0,
    itemStyle: { color: statusColors[index] }
  }))
  
  const option = {
    title: {
      text: '订单状态分布',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      data: statusData,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      label: {
        show: true,
        formatter: '{b}\n{c}',
        fontSize: 11
      }
    }]
  }
  
  orderChart.setOption(option)
}

// 渲染用户图表
const renderUserChart = () => {
  const chartDom = document.getElementById('user-chart')
  if (!chartDom) return
  
  if (userChart) {
    userChart.dispose()
  }
  
  userChart = echarts.init(chartDom)
  
  const dates = Object.keys(userStats.value.usersByDate || {}).sort()
  const userData = dates.map(date => userStats.value.usersByDate[date])
  
  const option = {
    title: {
      text: '用户增长趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>新增用户: {c}'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '新增用户数'
    },
    series: [{
      type: 'bar',
      data: userData,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2378f7' },
            { offset: 0.7, color: '#2378f7' },
            { offset: 1, color: '#83bff6' }
          ])
        }
      }
    }]
  }
  
  userChart.setOption(option)
}

// 渲染设备图表
const renderDeviceChart = () => {
  const chartDom = document.getElementById('device-chart')
  if (!chartDom) return
  
  if (deviceChart) {
    deviceChart.dispose()
  }
  
  deviceChart = echarts.init(chartDom)
  
  const gunStatusData = [
    { value: deviceStats.value.gunStatus?.idle || 0, name: '空闲', itemStyle: { color: '#67c23a' } },
    { value: deviceStats.value.gunStatus?.busy || 0, name: '繁忙', itemStyle: { color: '#e6a23c' } },
    { value: deviceStats.value.gunStatus?.fault || 0, name: '故障', itemStyle: { color: '#f56c6c' } }
  ]
  
  const option = {
    title: {
      text: '充电枪状态分布',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      data: gunStatusData,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      label: {
        show: true,
        formatter: '{b}\n{c}',
        fontSize: 11
      }
    }]
  }
  
  deviceChart.setOption(option)
}

// 渲染高峰期图表
const renderPeakHoursChart = () => {
  const chartDom = document.getElementById('peak-hours-chart')
  if (!chartDom) return
  
  let peakHoursChart = echarts.getInstanceByDom(chartDom)
  if (peakHoursChart) {
    peakHoursChart.dispose()
  }
  
  peakHoursChart = echarts.init(chartDom)
  
  const hours = peakHours.value.map(item => item.hour)
  const counts = peakHours.value.map(item => item.count)
  
  const option = {
    title: {
      text: '充电高峰期分析',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>订单数量: {c}'
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '订单数量'
    },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#f56c6c' },
          { offset: 0.5, color: '#e6a23c' },
          { offset: 1, color: '#e6a23c' }
        ])
      },
      label: {
        show: true,
        position: 'top'
      }
    }]
  }
  
  peakHoursChart.setOption(option)
}

// 渲染用户行为图表
const renderUserBehaviorChart = () => {
  const chartDom = document.getElementById('user-behavior-chart')
  if (!chartDom) return
  
  let userBehaviorChart = echarts.getInstanceByDom(chartDom)
  if (userBehaviorChart) {
    userBehaviorChart.dispose()
  }
  
  userBehaviorChart = echarts.init(chartDom)
  
  const frequencyData = [
    { value: userBehavior.value.frequencyDistribution?.low || 0, name: '低频用户(1-2次)', itemStyle: { color: '#909399' } },
    { value: userBehavior.value.frequencyDistribution?.medium || 0, name: '中频用户(3-5次)', itemStyle: { color: '#409eff' } },
    { value: userBehavior.value.frequencyDistribution?.high || 0, name: '高频用户(6次以上)', itemStyle: { color: '#67c23a' } }
  ]
  
  const option = {
    title: {
      text: '用户充电频率分布',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      data: frequencyData,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      label: {
        show: true,
        formatter: '{b}\n{c}',
        fontSize: 11
      }
    }]
  }
  
  userBehaviorChart.setOption(option)
}

// 处理日期范围变化
const handleDateChange = () => {
  loading.value = true
  Promise.all([
    fetchRevenueStats(),
    fetchOrderStats(),
    fetchUserStats(),
    fetchDeviceStats()
  ]).finally(() => {
    loading.value = false
  })
}

// 处理报表命令
const handleReportCommand = async (command) => {
  if (command === 'daily') {
    await generateDailyReport()
  } else if (command === 'weekly') {
    await generateWeeklyReport()
  } else if (command === 'monthly') {
    await generateMonthlyReport()
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchRevenueStats(),
      fetchOrderStats(),
      fetchUserStats(),
      fetchDeviceStats(),
      fetchPeakHoursAnalysis(),
      fetchUserBehaviorAnalysis()
    ])
  } finally {
    loading.value = false
  }
  
  // 监听窗口大小变化，重绘图表
  window.addEventListener('resize', () => {
    revenueChart?.resize()
    orderChart?.resize()
    userChart?.resize()
    deviceChart?.resize()
    const peakHoursChart = echarts.getInstanceByDom(document.getElementById('peak-hours-chart'))
    const userBehaviorChart = echarts.getInstanceByDom(document.getElementById('user-behavior-chart'))
    peakHoursChart?.resize()
    userBehaviorChart?.resize()
  })
})

// 格式化金额
const formatCurrency = (value) => {
  return '¥' + (value || 0).toFixed(2)
}

// 格式化百分比
const formatPercent = (value) => {
  return (value || 0) + '%'
}

// 导出报表
const exportReport = async () => {
  exportLoading.value = true
  try {
    // 收集所有数据
    const reportData = {
      dateRange: dateRange.value.map(d => d.toISOString().split('T')[0]),
      reportType: reportType.value,
      revenueStats: revenueStats.value,
      orderStats: orderStats.value,
      userStats: userStats.value,
      deviceStats: deviceStats.value,
      peakHours: peakHours.value,
      userBehavior: userBehavior.value,
      generatedAt: new Date().toISOString()
    }
    
    // 转换为CSV格式
    const csvContent = generateCSV(reportData)
    
    // 创建下载链接
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `充电站统计报表_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('报表导出成功')
  } catch (error) {
    console.error('导出报表失败', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 生成日报
const generateDailyReport = async () => {
  try {
    // 设置日期为昨天
    const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000)
    dateRange.value = [yesterday, yesterday]
    reportType.value = 'daily'
    
    loading.value = true
    await Promise.all([
      fetchRevenueStats(),
      fetchOrderStats(),
      fetchUserStats(),
      fetchDeviceStats(),
      fetchPeakHoursAnalysis(),
      fetchUserBehaviorAnalysis()
    ])
    
    // 导出日报
    await exportReport()
    
    ElMessage.success('日报生成成功')
  } catch (error) {
    console.error('生成日报失败', error)
    ElMessage.error('生成日报失败')
  } finally {
    loading.value = false
  }
}

// 生成周报
const generateWeeklyReport = async () => {
  try {
    // 设置日期为上周
    const today = new Date()
    const lastWeekStart = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
    const lastWeekEnd = new Date(today.getTime() - 24 * 60 * 60 * 1000)
    dateRange.value = [lastWeekStart, lastWeekEnd]
    reportType.value = 'weekly'
    
    loading.value = true
    await Promise.all([
      fetchRevenueStats(),
      fetchOrderStats(),
      fetchUserStats(),
      fetchDeviceStats(),
      fetchPeakHoursAnalysis(),
      fetchUserBehaviorAnalysis()
    ])
    
    // 导出周报
    await exportReport()
    
    ElMessage.success('周报生成成功')
  } catch (error) {
    console.error('生成周报失败', error)
    ElMessage.error('生成周报失败')
  } finally {
    loading.value = false
  }
}

// 生成月报
const generateMonthlyReport = async () => {
  try {
    // 设置日期为上个月
    const today = new Date()
    const lastMonthStart = new Date(today.getFullYear(), today.getMonth() - 1, 1)
    const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0)
    dateRange.value = [lastMonthStart, lastMonthEnd]
    reportType.value = 'monthly'
    
    loading.value = true
    await Promise.all([
      fetchRevenueStats(),
      fetchOrderStats(),
      fetchUserStats(),
      fetchDeviceStats(),
      fetchPeakHoursAnalysis(),
      fetchUserBehaviorAnalysis()
    ])
    
    // 导出月报
    await exportReport()
    
    ElMessage.success('月报生成成功')
  } catch (error) {
    console.error('生成月报失败', error)
    ElMessage.error('生成月报失败')
  } finally {
    loading.value = false
  }
}

// 自动生成报表（定时任务调用）
const autoGenerateReports = async () => {
  try {
    const now = new Date()
    const hour = now.getHours()
    
    // 每天凌晨1点生成日报
    if (hour === 1) {
      await generateDailyReport()
    }
    
    // 每周一凌晨2点生成周报
    if (now.getDay() === 1 && hour === 2) {
      await generateWeeklyReport()
    }
    
    // 每月1号凌晨3点生成月报
    if (now.getDate() === 1 && hour === 3) {
      await generateMonthlyReport()
    }
  } catch (error) {
    console.error('自动生成报表失败', error)
  }
}

// 生成CSV内容
const generateCSV = (data) => {
  const lines = []
  
  // 标题
  lines.push('新能源汽车充电站统计报表')
  lines.push(`生成时间: ${new Date().toLocaleString()}`)
  lines.push(`统计周期: ${data.dateRange[0]} 至 ${data.dateRange[1]}`)
  lines.push('')
  
  // 营收统计
  lines.push('=== 营收统计 ===')
  lines.push(`总营收,${formatCurrency(data.revenueStats.totalRevenue)}`)
  lines.push(`订单数量,${data.revenueStats.orderCount || 0}`)
  lines.push(`平均订单金额,${formatCurrency(data.revenueStats.avgOrderValue)}`)
  lines.push('')
  
  // 订单统计
  lines.push('=== 订单统计 ===')
  lines.push(`总订单数,${data.orderStats.totalOrders || 0}`)
  lines.push(`充电中订单,${data.orderStats.statusCount?.[0] || 0}`)
  lines.push(`待结算订单,${data.orderStats.statusCount?.[1] || 0}`)
  lines.push(`已完成订单,${data.orderStats.statusCount?.[2] || 0}`)
  lines.push(`已取消订单,${data.orderStats.statusCount?.[3] || 0}`)
  lines.push('')
  
  // 用户统计
  lines.push('=== 用户统计 ===')
  lines.push(`总用户数,${data.userStats.totalUsers || 0}`)
  lines.push(`活跃用户,${data.userStats.activeUsers || 0}`)
  lines.push(`非活跃用户,${data.userStats.inactiveUsers || 0}`)
  lines.push(`总余额,${formatCurrency(data.userStats.totalBalance)}`)
  lines.push(`人均余额,${formatCurrency(data.userStats.avgBalance)}`)
  lines.push('')
  
  // 设备统计
  lines.push('=== 设备统计 ===')
  lines.push(`充电站总数,${data.deviceStats.totalStations || 0}`)
  lines.push(`充电桩总数,${data.deviceStats.totalChargers || 0}`)
  lines.push(`充电枪总数,${data.deviceStats.totalGuns || 0}`)
  lines.push(`设备利用率,${data.deviceStats.gunUtilization || 0}%`)
  lines.push(`充电桩故障率,${data.deviceStats.chargerFaultRate || 0}%`)
  lines.push(`充电枪故障率,${data.deviceStats.gunFaultRate || 0}%`)
  lines.push('')
  
  // 高峰期分析
  lines.push('=== 充电高峰期分析 ===')
  lines.push('时间段,订单数量,占比')
  data.peakHours.forEach(item => {
    lines.push(`${item.hour},${item.count},${item.percentage}%`)
  })
  lines.push('')
  
  // 用户行为分析
  lines.push('=== 用户行为分析 ===')
  lines.push(`人均订单数,${data.userBehavior.avgOrdersPerUser || 0}`)
  lines.push(`人均充值金额,${formatCurrency(data.userBehavior.avgRechargeAmount)}`)
  lines.push(`低频用户(1-2次),${data.userBehavior.frequencyDistribution?.low || 0}`)
  lines.push(`中频用户(3-5次),${data.userBehavior.frequencyDistribution?.medium || 0}`)
  lines.push(`高频用户(6次以上),${data.userBehavior.frequencyDistribution?.high || 0}`)
  
  return lines.join('\n')
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 日期筛选 -->
    <el-card class="mb-16">
      <template #header>
        <div class="card-header">
          <span>统计时间段</span>
        </div>
      </template>
      <div class="filter-container">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateChange"
        />
        <el-select v-model="reportType" class="ml-8" style="width:120px">
          <el-option label="日报" value="daily" />
          <el-option label="周报" value="weekly" />
          <el-option label="月报" value="monthly" />
        </el-select>
        <el-button type="primary" @click="handleDateChange" :loading="loading" class="ml-8">
          更新统计
        </el-button>
        <el-button type="success" @click="exportReport" :loading="exportLoading" class="ml-8">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
        <el-dropdown @command="handleReportCommand" class="ml-8">
          <el-button type="warning">
            <el-icon><Document /></el-icon>
            生成报表
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="daily">生成日报</el-dropdown-item>
              <el-dropdown-item command="weekly">生成周报</el-dropdown-item>
              <el-dropdown-item command="monthly">生成月报</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-card>

    <!-- 关键指标 -->
    <el-row :gutter="16" class="mb-16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c20; color: #f56c6c;">
              <el-icon size="28"><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatCurrency(revenueStats.totalRevenue) }}</div>
              <div class="stat-label">总营收</div>
              <div class="stat-detail">
                订单数: {{ revenueStats.orderCount || 0 }}
              </div>
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
              <div class="stat-value">{{ userStats.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
              <div class="stat-detail">
                活跃: {{ userStats.activeUsers || 0 }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff20; color: #409eff;">
              <el-icon size="28"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ orderStats.totalOrders || 0 }}</div>
              <div class="stat-label">总订单数</div>
              <div class="stat-detail">
                已完成: {{ orderStats.statusCount?.[2] || 0 }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c20; color: #e6a23c;">
              <el-icon size="28"><SetUp /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ deviceStats.totalGuns || 0 }}</div>
              <div class="stat-label">充电枪总数</div>
              <div class="stat-detail">
                利用率: {{ deviceStats.gunUtilization || 0 }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="mb-16">
      <el-col :xs="24" :lg="12">
        <el-card>
          <div id="revenue-chart" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <div id="order-chart" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb-16">
      <el-col :xs="24" :lg="12">
        <el-card>
          <div id="user-chart" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <div id="device-chart" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 深度分析图表 -->
    <el-row :gutter="16" class="mb-16">
      <el-col :xs="24" :lg="12">
        <el-card>
          <div id="peak-hours-chart" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <div id="user-behavior-chart" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>详细统计</span>
        </div>
      </template>
      
      <el-tabs type="border-card">
        <el-tab-pane label="营收详情">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总营收">{{ formatCurrency(revenueStats.totalRevenue) }}</el-descriptions-item>
            <el-descriptions-item label="订单数量">{{ revenueStats.orderCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="平均订单金额">{{ formatCurrency(revenueStats.avgOrderValue) }}</el-descriptions-item>
            <el-descriptions-item label="统计天数">{{ Object.keys(revenueStats.revenueByDate || {}).length }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="mt-16">
            <h4>各充电站营收排名</h4>
            <el-table :data="Object.entries(revenueStats.revenueByStation || {}).map(([name, revenue]) => ({ name, revenue }))" border>
              <el-table-column prop="name" label="充电站" />
              <el-table-column prop="revenue" label="营收金额">
                <template #default="{row}">{{ formatCurrency(row.revenue) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="设备详情">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="充电站总数">{{ deviceStats.totalStations || 0 }}</el-descriptions-item>
            <el-descriptions-item label="充电桩总数">{{ deviceStats.totalChargers || 0 }}</el-descriptions-item>
            <el-descriptions-item label="充电枪总数">{{ deviceStats.totalGuns || 0 }}</el-descriptions-item>
            <el-descriptions-item label="设备利用率">{{ deviceStats.gunUtilization || 0 }}%</el-descriptions-item>
            <el-descriptions-item label="设备故障率">{{ deviceStats.chargerFaultRate || 0 }}%</el-descriptions-item>
          </el-descriptions>
          
          <div class="mt-16">
            <h4>设备状态统计</h4>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-card>
                  <div class="device-stat">
                    <div class="device-title">充电站状态</div>
                    <div class="device-item">正常运营: {{ deviceStats.stationStatus?.normal || 0 }}</div>
                    <div class="device-item">维护中: {{ deviceStats.stationStatus?.maintaining || 0 }}</div>
                    <div class="device-item">已停用: {{ deviceStats.stationStatus?.stopped || 0 }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <div class="device-stat">
                    <div class="device-title">充电桩状态</div>
                    <div class="device-item">正常: {{ deviceStats.chargerStatus?.normal || 0 }}</div>
                    <div class="device-item">故障: {{ deviceStats.chargerStatus?.fault || 0 }}</div>
                    <div class="device-item">维护中: {{ deviceStats.chargerStatus?.maintaining || 0 }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <div class="device-stat">
                    <div class="device-title">充电枪状态</div>
                    <div class="device-item">空闲: {{ deviceStats.gunStatus?.idle || 0 }}</div>
                    <div class="device-item">繁忙: {{ deviceStats.gunStatus?.busy || 0 }}</div>
                    <div class="device-item">故障: {{ deviceStats.gunStatus?.fault || 0 }}</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="用户行为分析">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总用户数">{{ userBehavior.totalUsers || 0 }}</el-descriptions-item>
            <el-descriptions-item label="活跃用户数">{{ userBehavior.activeUsers || 0 }}</el-descriptions-item>
            <el-descriptions-item label="总订单数">{{ userBehavior.totalOrders || 0 }}</el-descriptions-item>
            <el-descriptions-item label="人均订单数">{{ userBehavior.avgOrdersPerUser || 0 }}</el-descriptions-item>
            <el-descriptions-item label="人均充值金额">{{ formatCurrency(userBehavior.avgRechargeAmount) }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="mt-16">
            <h4>用户充电频率分布</h4>
            <el-table :data="[
              { level: '低频用户(1-2次)', count: userBehavior.frequencyDistribution?.low || 0 },
              { level: '中频用户(3-5次)', count: userBehavior.frequencyDistribution?.medium || 0 },
              { level: '高频用户(6次以上)', count: userBehavior.frequencyDistribution?.high || 0 }
            ]" border>
              <el-table-column prop="level" label="用户类型" />
              <el-table-column prop="count" label="用户数量" />
            </el-table>
          </div>
          
          <div class="mt-16">
            <h4>充电高峰期分析</h4>
            <el-table :data="peakHours" border>
              <el-table-column prop="hour" label="时间段" />
              <el-table-column prop="count" label="订单数量" />
              <el-table-column prop="percentage" label="占比">
                <template #default="{row}">{{row.percentage}}%</template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
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

.mt-16 {
  margin-top: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  display: flex;
  gap: 16px;
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

.device-stat {
  padding: 12px;
}

.device-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.device-item {
  margin-bottom: 6px;
  font-size: 14px;
  color: #666;
}
</style>