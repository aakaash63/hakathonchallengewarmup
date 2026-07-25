import api from './axiosInstance';
export const getFamilyGuidance = data => api.post('/api/family/guidance', data);
