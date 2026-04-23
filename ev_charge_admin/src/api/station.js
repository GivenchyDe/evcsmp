import request from '@/utils/request.js'

export const getStationList = (page, limit, keyword = '') => request.get('/station/list', { params: { page, limit, keyword } })
export const getStationById = (id) => request.get(`/station/${id}`)
export const addStation = (data) => request.post('/station/add', data)
export const updateStation = (data) => request.post('/station/update', data)
export const deleteStation = (id) => request.get('/station/delete', { params: { id } })
