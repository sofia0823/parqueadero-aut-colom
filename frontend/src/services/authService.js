import { apiRequest, clearCsrfToken } from './api';

export const authService = {
    login(username, password) {
        return apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password }),
        });
    },

    session() {
        return apiRequest('/auth/session');
    },

    async logout() {
        await apiRequest('/auth/logout', { method: 'POST' });
        clearCsrfToken();
    },
};
