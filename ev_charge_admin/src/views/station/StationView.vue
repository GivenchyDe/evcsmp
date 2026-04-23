<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { getStationList, addStation, updateStation, deleteStation } from '@/api/station.js'
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
const listKeyword = ref('')
const searchKeyword = ref('')
const searchResults = ref([])
const showSearchResults = ref(false)

let mapInstance = null
let markerInstance = null
let geocoderInstance = null
let placeSearchInstance = null

const rules = {
  name: [{ required: true, message: '请输入网点名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getStationList(page.value, limit.value, listKeyword.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => {
  page.value = 1
  fetchList()
}

const handleReset = () => {
  listKeyword.value = ''
  page.value = 1
  fetchList()
}

const handleAdd = () => {
  dialogTitle.value = '新增网点'
  form.value = { status: 1, longitude: '', latitude: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑网点'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  const api = form.value.id ? updateStation : addStation
  const res = await api(form.value)
  if (res.success) {
    ElMessage.success(dialogTitle.value + '成功')
    dialogVisible.value = false
    fetchList()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该网点吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteStation(row.id)
    if (res.success) { ElMessage.success('删除成功'); fetchList() }
  })
}

const initMap = async () => {
  if (!window.AMapLoader) {
    ElMessage.warning('地图加载中，请稍候')
    return
  }
  try {
    const AMap = await window.AMapLoader.load({
      key: 'b2a8b0efb263570bbae57789d763c7a6',
      version: '2.0',
      plugins: ['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.Marker', 'AMap.Scale']
    })

    const lng = form.value.longitude ? parseFloat(form.value.longitude) : 114.3055
    const lat = form.value.latitude ? parseFloat(form.value.latitude) : 30.5928

    mapInstance = new AMap.Map('station-map', {
      zoom: 14,
      center: [lng, lat],
    })

    mapInstance.addControl(new AMap.Scale())

    geocoderInstance = new AMap.Geocoder()
    placeSearchInstance = new AMap.PlaceSearch({ pageSize: 8 })

    // 如果已有经纬度，显示标记
    if (form.value.longitude && form.value.latitude) {
      setMarker(parseFloat(form.value.longitude), parseFloat(form.value.latitude))
    }

    // 地图点击事件
    mapInstance.on('click', (e) => {
      const { lng, lat } = e.lnglat
      setMarker(lng, lat)
      form.value.longitude = lng.toFixed(7)
      form.value.latitude = lat.toFixed(7)

      geocoderInstance.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.regeocode) {
          form.value.address = result.regeocode.formattedAddress
        }
      })
    })
  } catch (err) {
    console.error('地图初始化失败', err)
    ElMessage.error('地图加载失败，请检查网络')
  }
}

const setMarker = (lng, lat) => {
  if (markerInstance) {
    markerInstance.setPosition([lng, lat])
  } else {
    markerInstance = new window.AMap.Marker({
      position: [lng, lat],
      draggable: true,
    })
    markerInstance.on('dragend', (e) => {
      const { lng, lat } = e.lnglat
      form.value.longitude = lng.toFixed(7)
      form.value.latitude = lat.toFixed(7)
      geocoderInstance.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.regeocode) {
          form.value.address = result.regeocode.formattedAddress
        }
      })
    })
    mapInstance.add(markerInstance)
  }
}

const onSearch = () => {
  if (!searchKeyword.value.trim() || !placeSearchInstance) return
  placeSearchInstance.search(searchKeyword.value, (status, result) => {
    if (status === 'complete' && result.info === 'OK') {
      searchResults.value = result.poiList.pois
      showSearchResults.value = true
    } else {
      searchResults.value = []
      ElMessage.info('未找到相关地点')
    }
  })
}

const selectSearchResult = (poi) => {
  const lng = parseFloat(poi.location.lng)
  const lat = parseFloat(poi.location.lat)
  mapInstance.setCenter([lng, lat])
  setMarker(lng, lat)
  form.value.longitude = lng.toFixed(7)
  form.value.latitude = lat.toFixed(7)
  form.value.address = poi.address || poi.name
  showSearchResults.value = false
  searchKeyword.value = poi.name
}

const destroyMap = () => {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
    markerInstance = null
    geocoderInstance = null
    placeSearchInstance = null
  }
}

watch(dialogVisible, (val) => {
  if (val) {
    nextTick(() => {
      setTimeout(() => initMap(), 200)
    })
  } else {
    destroyMap()
    searchKeyword.value = ''
    searchResults.value = []
    showSearchResults.value = false
  }
})

onMounted(fetchList)
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>网点管理</span>
          <div class="search-box">
            <el-input v-model="listKeyword" placeholder="搜索网点名称/地址" size="small" clearable style="width:220px" @keyup.enter="handleSearch" />
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
            <el-button type="primary" size="small" @click="handleAdd">新增网点</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="网点名称" width="150" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="['warning','success','info'][row.status] || 'info'" size="small">
              {{ ['维护中','正常运营','已停用'][row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="page" v-model:page-size="limit" :total="total" layout="total, prev, pager, next" @current-change="fetchList" />
    </el-card>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="860px" :close-on-click-modal="false" destroy-on-close>
      <el-row :gutter="16">
        <!-- 左侧表单 -->
        <el-col :span="10">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
            <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="网点名称" /></el-form-item>
            <el-form-item label="地址" prop="address"><el-input v-model="form.address" type="textarea" :rows="2" placeholder="点击地图选点自动填充" /></el-form-item>
            <el-form-item label="经度"><el-input v-model="form.longitude" readonly placeholder="点击地图获取" /></el-form-item>
            <el-form-item label="纬度"><el-input v-model="form.latitude" readonly placeholder="点击地图获取" /></el-form-item>
            <el-form-item label="电话"><el-input v-model="form.phone" placeholder="联系电话" /></el-form-item>
            <el-form-item label="状态">
              <el-select v-model="form.status">
                <el-option label="维护中" :value="0" />
                <el-option label="正常运营" :value="1" />
                <el-option label="已停用" :value="2" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-col>

        <!-- 右侧地图 -->
        <el-col :span="14">
          <div class="map-box">
            <div class="map-search">
              <el-input v-model="searchKeyword" placeholder="搜索地点" size="small" @keyup.enter="onSearch">
                <template #append><el-button size="small" @click="onSearch">搜索</el-button></template>
              </el-input>
              <div v-if="showSearchResults && searchResults.length" class="search-results">
                <div
                  v-for="item in searchResults"
                  :key="item.id"
                  class="search-item"
                  @click="selectSearchResult(item)"
                >
                  <div class="search-name">{{ item.name }}</div>
                  <div class="search-addr">{{ item.address }}</div>
                </div>
              </div>
            </div>
            <div id="station-map" style="width:100%;height:380px;border-radius:6px;"></div>
            <div class="map-tip">点击地图选点，或搜索后选择地点</div>
          </div>
        </el-col>
      </el-row>

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

.map-box { position: relative; }
.map-search {
  position: absolute;
  top: 8px;
  left: 8px;
  right: 8px;
  z-index: 10;
}
.search-results {
  position: absolute;
  top: 36px;
  left: 0;
  right: 0;
  max-height: 220px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.search-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}
.search-item:hover { background: #f5f7fa; }
.search-item:last-child { border-bottom: none; }
.search-name { font-size: 13px; font-weight: 500; color: #333; }
.search-addr { font-size: 12px; color: #999; margin-top: 2px; }
.map-tip {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin-top: 6px;
}
</style>
