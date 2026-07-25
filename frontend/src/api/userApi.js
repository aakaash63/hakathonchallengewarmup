import api from './axiosInstance';
export const getProfile     = ()     => api.get('/api/user/profile');
export const saveOnboarding = data   => api.post('/api/user/onboarding', data);
export const updateSafety   = data   => api.put('/api/user/safety-plan', data);
