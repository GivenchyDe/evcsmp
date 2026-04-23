import request from '@/utils/request.js'

export const getChargerList = (page, limit, keyword = '') => request.get('/charger/list', { params: { page, limit, keyword } })
export const getChargerById = (id) => request.get(`/charger/${id}`)
export const addCharger = (data) => request.post('/charger/add', data)
export const updateCharger = (data) => request.post('/charger/update', data)
export const deleteCharger = (id) => request.get('/charger/delete', { params: { id } })
