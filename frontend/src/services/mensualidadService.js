import { apiRequest } from "./api";

const API = "/mensualidades";

export const mensualidadService = {
    listarMembresias() {
        return apiRequest(`${API}/membresias`);
    },

    registrarMembresia(datos) {
        return apiRequest(`${API}/membresias`, {
            method: "POST",
            body: JSON.stringify(datos),
        });
    },

    finalizarMembresia(id) {
        return apiRequest(`${API}/membresias/${id}/finalizar`, {
            method: "POST",
        });
    },

    consultarEstado(periodo) {
        return apiRequest(
            `${API}/estado?periodo=${encodeURIComponent(periodo)}`
        );
    },

    registrarPago(datos) {
        return apiRequest(`${API}/pagos`, {
            method: "POST",
            body: JSON.stringify(datos),
        });
    },
};
