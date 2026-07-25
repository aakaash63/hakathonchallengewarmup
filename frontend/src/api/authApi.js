import api from './axiosInstance';
export const login    = data => api.post('/api/auth/login', data);
export const signup   = data => api.post('/api/auth/signup', data);
