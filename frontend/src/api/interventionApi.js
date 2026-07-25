import api from './axiosInstance';
export const sendIntervention = data => api.post('/api/intervention/respond', data);
