
import { useCallback, useEffect, useState } from "react";

import {
    ParkingSquare,
    CheckCircle2,
    CircleX,
    Wrench,
    RefreshCw,
    CircleAlert,
    CircleHelp,
    LoaderCircle,
} from "lucide-react";

import { celdaService } from "../../services/celdaService";

const estados = {
    DISPONIBLE: {
        nombre: "Disponible",
        color: "success",
        icono: CheckCircle2,
    },
    OCUPADA: {
        nombre: "Ocupada",
        color: "danger",
        icono: CircleX,
    },
    MANTENIMIENTO: {
        nombre: "Mantenimiento",
        color: "warning",
        icono: Wrench,
    },
};

export default function CeldasPage() {
    const [celdas, setCeldas] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");
    const [filtro, setFiltro] = useState("TODAS");

    const cargarCeldas = useCallback(async () => {
        setCargando(true);
        setError("");

        try {
            const data = await celdaService.listar();

            if (!Array.isArray(data)) {
                throw new Error(
                    "El servidor no devolvió una lista válida de celdas."
                );
            }

            setCeldas(data);
        } catch (err) {
            setError(
                err.message || "No se pudieron cargar las celdas."
            );
            setCeldas([]);
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => {
        cargarCeldas();
    }, [cargarCeldas]);

    const disponibles = celdas.filter(
        (celda) => celda.estado === "DISPONIBLE"
    ).length;

    const ocupadas = celdas.filter(
        (celda) => celda.estado === "OCUPADA"
    ).length;

    const mantenimiento = celdas.filter(
        (celda) => celda.estado === "MANTENIMIENTO"
    ).length;

    const celdasFiltradas =
        filtro === "TODAS"
            ? celdas
            : celdas.filter((celda) => celda.estado === filtro);

    return (
        <div className="container-fluid px-2 px-md-3 py-3">
            {/* Encabezado */}
            <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                <div>
                    <h2 className="fw-bold mb-1">
                        Celdas del parqueadero
                    </h2>

                    <p className="text-secondary mb-0">
                        Consulta la disponibilidad de los espacios del parqueadero.
                    </p>
                </div>

                <button
                    type="button"
                    className="btn btn-outline-secondary d-flex align-items-center gap-2"
                    onClick={cargarCeldas}
                    disabled={cargando}
                >
                    <RefreshCw
                        size={17}
                        className={cargando ? "spin" : ""}
                    />
                    Actualizar
                </button>
            </div>

            {/* Mensaje de error */}
            {error && (
                <div
                    className="alert alert-danger d-flex align-items-start gap-2"
                    role="alert"
                >
                    <CircleAlert
                        size={20}
                        className="flex-shrink-0 mt-1"
                    />

                    <div className="flex-grow-1">
                        <strong>Error al cargar las celdas</strong>

                        <p className="mb-2 mt-1">{error}</p>

                        <button
                            type="button"
                            className="btn btn-sm btn-outline-danger"
                            onClick={cargarCeldas}
                            disabled={cargando}
                        >
                            Intentar nuevamente
                        </button>
                    </div>
                </div>
            )}

            {/* Estadísticas */}
            <div className="row g-3 mb-4">
                <div className="col-6 col-xl-3">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body">
                            <div className="d-flex justify-content-between align-items-center gap-2">
                                <span className="text-secondary">
                                    Total de celdas
                                </span>

                                <ParkingSquare
                                    size={23}
                                    className="text-primary"
                                />
                            </div>

                            <h3 className="fw-bold mt-3 mb-0">
                                {celdas.length}
                            </h3>

                            <small className="text-secondary">
                                Espacios registrados
                            </small>
                        </div>
                    </div>
                </div>

                <div className="col-6 col-xl-3">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body">
                            <div className="d-flex justify-content-between align-items-center gap-2">
                                <span className="text-secondary">
                                    Disponibles
                                </span>

                                <CheckCircle2
                                    size={23}
                                    className="text-success"
                                />
                            </div>

                            <h3 className="fw-bold text-success mt-3 mb-0">
                                {disponibles}
                            </h3>

                            <small className="text-secondary">
                                Libres para estacionar
                            </small>
                        </div>
                    </div>
                </div>

                <div className="col-6 col-xl-3">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body">
                            <div className="d-flex justify-content-between align-items-center gap-2">
                                <span className="text-secondary">
                                    Ocupadas
                                </span>

                                <CircleX
                                    size={23}
                                    className="text-danger"
                                />
                            </div>

                            <h3 className="fw-bold text-danger mt-3 mb-0">
                                {ocupadas}
                            </h3>

                            <small className="text-secondary">
                                Espacios ocupados
                            </small>
                        </div>
                    </div>
                </div>

                <div className="col-6 col-xl-3">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body">
                            <div className="d-flex justify-content-between align-items-center gap-2">
                                <span className="text-secondary">
                                    Mantenimiento
                                </span>

                                <Wrench
                                    size={23}
                                    className="text-warning"
                                />
                            </div>

                            <h3 className="fw-bold text-warning mt-3 mb-0">
                                {mantenimiento}
                            </h3>

                            <small className="text-secondary">
                                Fuera de servicio
                            </small>
                        </div>
                    </div>
                </div>
            </div>

            {/* Mapa de celdas */}
            <div className="card border-0 shadow-sm">
                <div className="card-body p-3 p-md-4">
                    <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                        <div>
                            <h5 className="fw-bold mb-1">
                                Mapa de celdas
                            </h5>

                            <p className="text-secondary small mb-0">
                                Consulta cada espacio según su disponibilidad.
                            </p>
                        </div>

                        <select
                            className="form-select"
                            style={{ maxWidth: "230px" }}
                            value={filtro}
                            onChange={(event) =>
                                setFiltro(event.target.value)
                            }
                            aria-label="Filtrar celdas por estado"
                        >
                            <option value="TODAS">
                                Todas las celdas
                            </option>

                            <option value="DISPONIBLE">
                                Disponibles
                            </option>

                            <option value="OCUPADA">
                                Ocupadas
                            </option>

                            <option value="MANTENIMIENTO">
                                Mantenimiento
                            </option>
                        </select>
                    </div>

                    {/* Leyenda */}
                    <div className="d-flex flex-wrap gap-3 mb-4 small">
                        {Object.entries(estados).map(([estado, info]) => (
                            <div
                                key={estado}
                                className="d-flex align-items-center gap-2"
                            >
                                <span
                                    className={`rounded-circle bg-${info.color}`}
                                    style={{
                                        width: "10px",
                                        height: "10px",
                                    }}
                                />

                                <span className="text-secondary">
                                    {info.nombre}
                                </span>
                            </div>
                        ))}
                    </div>

                    {/* Contenido */}
                    {cargando ? (
                        <div className="text-center py-5">
                            <LoaderCircle
                                size={38}
                                className="text-primary"
                            />

                            <p className="text-secondary mt-3 mb-0">
                                Cargando celdas...
                            </p>
                        </div>
                    ) : error ? (
                        <div className="text-center py-5">
                            <CircleAlert
                                size={42}
                                className="text-danger mb-3"
                            />

                            <h6 className="fw-bold">
                                No fue posible cargar el mapa
                            </h6>

                            <p className="text-secondary mb-0">
                                Comprueba la conexión con el servidor e inténtalo de nuevo.
                            </p>
                        </div>
                    ) : celdasFiltradas.length === 0 ? (
                        <div className="text-center py-5">
                            <CircleHelp
                                size={42}
                                className="text-secondary mb-3"
                            />

                            <h6 className="fw-bold">
                                No hay celdas para mostrar
                            </h6>

                            <p className="text-secondary mb-0">
                                {celdas.length === 0
                                    ? "No hay celdas registradas en la base de datos."
                                    : "No existen celdas con el estado seleccionado."}
                            </p>
                        </div>
                    ) : (
                        <div className="row g-3">
                            {celdasFiltradas.map((celda) => {
                                const info =
                                    estados[celda.estado] ||
                                    estados.DISPONIBLE;

                                const IconoEstado = info.icono;

                                return (
                                    <div
                                        className="col-6 col-md-4 col-lg-3 col-xl-2"
                                        key={celda.id ?? celda.codigo}
                                    >
                                        <div
                                            className={`border border-2 border-${info.color} rounded-3 p-3 p-md-4 h-100 text-center`}
                                        >
                                            <IconoEstado
                                                size={34}
                                                strokeWidth={1.8}
                                                className={`text-${info.color} mb-3`}
                                            />

                                            <h5 className="fw-bold mb-3">
                                                {celda.codigo}
                                            </h5>

                                            <span
                                                className={`badge text-bg-${info.color}`}
                                            >
                                                {info.nombre}
                                            </span>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
