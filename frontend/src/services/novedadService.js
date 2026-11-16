import { apiRequest } from './api';

const ENDPOINT = '/novedades';

export const novedadService = {
    listar() {
        return apiRequest(ENDPOINT);
    },

    consultar(id) {
        return apiRequest(`${ENDPOINT}/${id}`);
    },

    registrar(novedad) {
        return apiRequest(ENDPOINT, {
            method: 'POST',
            body: JSON.stringify(novedad),
        });
    },
};
