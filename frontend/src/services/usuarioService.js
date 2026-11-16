import { apiRequest } from './api';

const ENDPOINT = '/usuarios';

export const usuarioService = {
    listar() {
        return apiRequest(ENDPOINT);
    },

    consultar(id) {
        return apiRequest(`${ENDPOINT}/${id}`);
    },

    crear(usuario) {
        return apiRequest(ENDPOINT, {
            method: 'POST',
            body: JSON.stringify(usuario),
        });
    },

    actualizar(id, usuario) {
        return apiRequest(`${ENDPOINT}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(usuario),
        });
    },

    desactivar(id) {
        return apiRequest(`${ENDPOINT}/${id}`, {
            method: 'DELETE',
        });
    },
};
