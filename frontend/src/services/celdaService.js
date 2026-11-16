
import { apiRequest } from './api';

export const celdaService = {
    listar() {
        return apiRequest('/celdas');
    },

    listarDisponibles() {
        return apiRequest('/celdas/disponibles');
    },
};

export default celdaService;
