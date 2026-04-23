import request from '@/utils/request.js'

export const login = (username, password) => {
  return request.get('/admin/login', { params: { username, password } })
}

export const getAdminList = (page, limit, keyword = '') => {
  return request.get('/admin/list', { params: { page, limit, keyword } })
}

export const addAdmin = (data) => {
  return request.post('/admin/add', data)
}

export const updateAdmin = (data) => {
  return request.post('/admin/update', data)
}
