import api from './axiosInstance';
export const getResources  = ()    => api.get('/api/resources');
export const searchResources = q   => api.get(`/api/resources/search?q=${encodeURIComponent(q)}`);
export const explainTopic  = q     => api.post('/api/resources/explain', { question: q });
