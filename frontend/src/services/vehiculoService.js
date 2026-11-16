import { apiRequest } from './api';

const ENDPOINT = '/vehiculos';

export const vehiculoService = {
    listar() {
        return apiRequest(ENDPOINT);
    },

    registrar(vehiculo) {
        return apiRequest(ENDPOINT, {
            method: 'POST',
            body: JSON.stringify(vehiculo),
        });
    },

    consultarPorId(id) {
        return apiRequest(`${ENDPOINT}/${id}`);
    },

    actualizar(id, vehiculo) {
        return apiRequest(`${ENDPOINT}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(vehiculo),
        });
    },

    eliminar(id) {
        return apiRequest(`${ENDPOINT}/${id}`, {
            method: 'DELETE',
        });
    },
};