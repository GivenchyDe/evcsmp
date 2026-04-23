import request from '@/utils/request.js'

export const getFeedbackList = (page, limit, keyword = '') => request.get('/feedback/list', { params: { page, limit, keyword } })
export const getFeedbackById = (id) => request.get(`/feedback/${id}`)
export const handleFeedback = (data) => request.post('/feedback/handle', data)
