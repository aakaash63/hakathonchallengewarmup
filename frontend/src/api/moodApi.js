import api from './axiosInstance';
export const logMood    = data => api.post('/api/moodlogs', data);
export const getMoodHistory = () => api.get('/api/moodlogs/history');
