import request from '@/utils/request.js'

export const getOrderList = (page, limit, keyword = '') => request.get('/chargingOrder/list', { params: { page, limit, keyword } })
export const getOrderById = (id) => request.get(`/chargingOrder/${id}`)
export const deleteOrder = (id) => request.get('/chargingOrder/delete', { params: { id } })
