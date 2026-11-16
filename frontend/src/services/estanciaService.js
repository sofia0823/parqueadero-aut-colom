
import { apiRequest } from './api';

/**
 * Servicio para administrar entradas y salidas de vehículos.
 */
export const estanciaService = {
    /**
     * Consultar las estancias activas.
     * GET /api/estancias/activas
     */
    listarActivas() {
        return apiRequest('/estancias/activas');
    },

    /**
     * Consultar el historial de estancias.
     * GET /api/estancias
     */
    listarTodas() {
        return apiRequest('/estancias');
    },

    /**
     * Consultar los vehículos registrados.
     * GET /api/vehiculos
     */
    listarVehiculos() {
        return apiRequest('/vehiculos');
    },

    /**
     * Consultar las celdas disponibles.
     * GET /api/estancias/celdas-disponibles
     */
    listarCeldasDisponibles() {
        return apiRequest('/estancias/celdas-disponibles');
    },

    /**
     * Registrar la entrada de un vehículo.
     * POST /api/estancias/entrada
     *
     * datos = {
     *   vehiculoId: number,
     *   celdaId: number,
     *   registradoPor?: number
     * }
     */
    registrarEntrada(datos) {
        return apiRequest('/estancias/entrada', {
            method: "POST",
            body: JSON.stringify(datos),
        });
    },

    /**
     * Registrar la salida de un vehículo.
     * POST /api/estancias/{id}/salida
     */
    registrarSalida(id) {
        if (id == null || !Number.isInteger(Number(id)) || Number(id) <= 0) {
            return Promise.reject(
                new Error("El identificador de la estancia no es válido.")
            );
        }

        return apiRequest(`/estancias/${Number(id)}/salida`, {
            method: "POST",
        });
    },
};

export default estanciaService;
