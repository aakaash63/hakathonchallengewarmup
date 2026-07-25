import api from './axiosInstance';
export const generateScript = data => api.post('/api/scripts/generate', data);
export const getScriptHistory = ()  => api.get('/api/scripts/history');
