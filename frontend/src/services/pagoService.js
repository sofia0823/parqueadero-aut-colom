
import { apiRequest } from './api';

export const pagoService = {
    async calcularLiquidacion(estanciaId) {
        return apiRequest(`/estancias/${estanciaId}/liquidacion`);
    },

    async confirmarPago(estanciaId, datos) {
        return apiRequest(`/estancias/${estanciaId}/pago-salida`, {
            method: "POST",
            body: JSON.stringify(datos),
        });
    },
};
