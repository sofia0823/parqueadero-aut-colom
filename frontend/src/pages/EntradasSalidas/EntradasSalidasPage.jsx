
import { useCallback, useEffect, useState } from "react";

import {
    ArrowDownToLine,
    ArrowUpFromLine,
    CarFront,
    CheckCircle2,
    CircleAlert,
    Clock3,
    ParkingSquare,
    RefreshCw,
    ShieldCheck,
    UserRound,
    Wallet,
} from "lucide-react";

import { estanciaService } from "../../services/estanciaService";
import LiquidacionPagoModal from "../../components/estancias/LiquidacionPagoModal";

export default function EntradasSalidasPage() {
    const [vehiculos, setVehiculos] = useState([]);
    const [celdas, setCeldas] = useState([]);
    const [estancias, setEstancias] = useState([]);

    const [vehiculoId, setVehiculoId] = useState("");
    const [celdaId, setCeldaId] = useState("");

    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);

    const [estanciaParaPago, setEstanciaParaPago] = useState(null);

    const [error, setError] = useState("");
    const [mensaje, setMensaje] = useState("");

    const cargarDatos = useCallback(async () => {
        setCargando(true);
        setError("");

        try {
            const [
                listaVehiculos,
                listaCeldas,
                listaEstancias,
            ] = await Promise.all([
                estanciaService.listarVehiculos(),
                estanciaService.listarCeldasDisponibles(),
                estanciaService.listarActivas(),
            ]);

            setVehiculos(
                Array.isArray(listaVehiculos) ? listaVehiculos : []
            );

            setCeldas(
                Array.isArray(listaCeldas) ? listaCeldas : []
            );

            setEstancias(
                Array.isArray(listaEstancias) ? listaEstancias : []
            );
        } catch (e) {
            setError(
                e.message ||
                    "No fue posible cargar los datos del parqueadero."
            );
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => {
        cargarDatos();
    }, [cargarDatos]);

    const vehiculosDisponibles = vehiculos.filter(
        (vehiculo) =>
            !estancias.some(
                (estancia) =>
                    estancia.vehiculo?.id === vehiculo.id
            )
    );

    async function registrarEntrada(evento) {
        evento.preventDefault();

        setError("");
        setMensaje("");

        if (!vehiculoId || !celdaId) {
            setError(
                "Selecciona un vehículo y una celda disponible."
            );
            return;
        }

        setGuardando(true);

        try {
            await estanciaService.registrarEntrada({
                vehiculoId: Number(vehiculoId),
                celdaId: Number(celdaId),
            });

            setVehiculoId("");
            setCeldaId("");
            setMensaje("La entrada se registró correctamente.");

            await cargarDatos();
        } catch (e) {
            setError(
                e.message || "No fue posible registrar la entrada."
            );
        } finally {
            setGuardando(false);
        }
    }

    function registrarSalida(estancia) {
        setError("");
        setMensaje("");
        setEstanciaParaPago(estancia);
    }

    async function manejarPagoConfirmado() {
        const placa =
            estanciaParaPago?.vehiculo?.placa || "del vehículo";

        setEstanciaParaPago(null);
        setMensaje(
            `El pago y la salida ${placa} se registraron correctamente.`
        );

        await cargarDatos();
    }

    function formatoFecha(fecha) {
        if (!fecha) return "—";

        const fechaValida = new Date(fecha);

        if (Number.isNaN(fechaValida.getTime())) {
            return fecha;
        }

        return fechaValida.toLocaleString("es-CO", {
            dateStyle: "medium",
            timeStyle: "short",
        });
    }

    return (
        <div className="container-fluid px-2 px-md-3 py-3">
            {/* Encabezado */}
            <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                <div>
                    <div className="d-flex align-items-center gap-2 mb-2">
                        <div className="bg-primary bg-opacity-10 text-primary rounded-3 p-2">
                            <ParkingSquare size={25} />
                        </div>

                        <span className="text-primary fw-semibold small">
                            ADMINISTRACIÓN DEL PARQUEADERO
                        </span>
                    </div>

                    <h2 className="fw-bold mb-1">
                        Entradas y salidas
                    </h2>

                    <p className="text-secondary mb-0">
                        Controla los ingresos, las estancias activas y los pagos de salida.
                    </p>
                </div>

                <button
                    type="button"
                    className="btn btn-outline-secondary d-flex align-items-center gap-2"
                    onClick={() => {
                        setMensaje("");
                        cargarDatos();
                    }}
                    disabled={cargando}
                >
                    <RefreshCw
                        size={17}
                        className={cargando ? "spinner-border-sm" : ""}
                    />
                    {cargando ? "Actualizando..." : "Actualizar"}
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
                        <strong>Ha ocurrido un problema</strong>
                        <div>{error}</div>
                    </div>

                    <button
                        type="button"
                        className="btn-close"
                        aria-label="Cerrar mensaje"
                        onClick={() => setError("")}
                    />
                </div>
            )}

            {/* Mensaje de éxito */}
            {mensaje && (
                <div
                    className="alert alert-success d-flex align-items-start gap-2"
                    role="status"
                >
                    <CheckCircle2
                        size={20}
                        className="flex-shrink-0 mt-1"
                    />

                    <div className="flex-grow-1">
                        <strong>Operación exitosa</strong>
                        <div>{mensaje}</div>
                    </div>

                    <button
                        type="button"
                        className="btn-close"
                        aria-label="Cerrar mensaje"
                        onClick={() => setMensaje("")}
                    />
                </div>
            )}

            {/* Estadísticas */}
            <div className="row g-3 mb-4">
                <div className="col-6 col-xl-4">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body p-3 p-md-4">
                            <div className="d-flex justify-content-between align-items-start gap-2">
                                <div>
                                    <p className="text-secondary small mb-2">
                                        Vehículos estacionados
                                    </p>

                                    <h3 className="fw-bold mb-1">
                                        {estancias.length}
                                    </h3>

                                    <small className="text-secondary">
                                        Estancias activas
                                    </small>
                                </div>

                                <div className="rounded-3 p-2 bg-primary bg-opacity-10 text-primary">
                                    <CarFront size={24} />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-6 col-xl-4">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body p-3 p-md-4">
                            <div className="d-flex justify-content-between align-items-start gap-2">
                                <div>
                                    <p className="text-secondary small mb-2">
                                        Celdas disponibles
                                    </p>

                                    <h3 className="fw-bold text-success mb-1">
                                        {celdas.length}
                                    </h3>

                                    <small className="text-secondary">
                                        Espacios libres
                                    </small>
                                </div>

                                <div className="rounded-3 p-2 bg-success bg-opacity-10 text-success">
                                    <ParkingSquare size={24} />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-12 col-xl-4">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body p-3 p-md-4">
                            <div className="d-flex justify-content-between align-items-start gap-2">
                                <div>
                                    <p className="text-secondary small mb-2">
                                        Vehículos registrados
                                    </p>

                                    <h3 className="fw-bold mb-1">
                                        {vehiculos.length}
                                    </h3>

                                    <small className="text-secondary">
                                        En el sistema
                                    </small>
                                </div>

                                <div className="rounded-3 p-2 bg-info bg-opacity-10 text-info">
                                    <UserRound size={24} />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Formulario de entrada */}
            <div className="card border-0 shadow-sm mb-4">
                <div className="card-body p-3 p-md-4">
                    <div className="d-flex align-items-center gap-3 mb-4">
                        <div className="rounded-3 bg-primary bg-opacity-10 text-primary p-3">
                            <ArrowDownToLine size={25} />
                        </div>

                        <div>
                            <h5 className="fw-bold mb-1">
                                Registrar entrada
                            </h5>

                            <p className="text-secondary small mb-0">
                                Asigna un vehículo a una celda disponible.
                            </p>
                        </div>
                    </div>

                    {cargando && vehiculos.length === 0 ? (
                        <div className="d-flex align-items-center gap-2 text-secondary py-2">
                            <RefreshCw size={17} />
                            Cargando vehículos y celdas...
                        </div>
                    ) : (
                        <form onSubmit={registrarEntrada}>
                            <div className="row g-3 align-items-end">
                                <div className="col-12 col-lg-5">
                                    <label
                                        htmlFor="vehiculoId"
                                        className="form-label fw-semibold"
                                    >
                                        Vehículo
                                    </label>

                                    <select
                                        id="vehiculoId"
                                        className="form-select"
                                        value={vehiculoId}
                                        onChange={(evento) =>
                                            setVehiculoId(evento.target.value)
                                        }
                                        required
                                        disabled={cargando || guardando}
                                    >
                                        <option value="">
                                            Selecciona una placa
                                        </option>

                                        {vehiculosDisponibles.map((vehiculo) => (
                                            <option
                                                key={vehiculo.id}
                                                value={vehiculo.id}
                                            >
                                                {vehiculo.placa}
                                                {" — "}
                                                {vehiculo.propietario || "Sin propietario"}
                                            </option>
                                        ))}
                                    </select>

                                    {!cargando &&
                                        vehiculosDisponibles.length === 0 && (
                                            <small className="text-secondary d-block mt-2">
                                                No hay vehículos disponibles para ingresar.
                                            </small>
                                        )}
                                </div>

                                <div className="col-12 col-lg-4">
                                    <label
                                        htmlFor="celdaId"
                                        className="form-label fw-semibold"
                                    >
                                        Celda disponible
                                    </label>

                                    <select
                                        id="celdaId"
                                        className="form-select"
                                        value={celdaId}
                                        onChange={(evento) =>
                                            setCeldaId(evento.target.value)
                                        }
                                        required
                                        disabled={cargando || guardando}
                                    >
                                        <option value="">
                                            Selecciona una celda
                                        </option>

                                        {celdas.map((celda) => (
                                            <option
                                                key={celda.id}
                                                value={celda.id}
                                            >
                                                {celda.codigo || `Celda ${celda.id}`}
                                                {celda.tipo ? ` — ${celda.tipo}` : ""}
                                            </option>
                                        ))}
                                    </select>

                                    {!cargando && celdas.length === 0 && (
                                        <small className="text-secondary d-block mt-2">
                                            No hay celdas disponibles.
                                        </small>
                                    )}
                                </div>

                                <div className="col-12 col-lg-3">
                                    <button
                                        type="submit"
                                        className="btn btn-primary w-100 d-flex align-items-center justify-content-center gap-2"
                                        disabled={
                                            cargando ||
                                            guardando ||
                                            !vehiculoId ||
                                            !celdaId
                                        }
                                    >
                                        {guardando ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm" />
                                                Registrando...
                                            </>
                                        ) : (
                                            <>
                                                <ArrowDownToLine size={18} />
                                                Registrar entrada
                                            </>
                                        )}
                                    </button>
                                </div>
                            </div>
                        </form>
                    )}
                </div>
            </div>

            {/* Vehículos estacionados */}
            <div className="card border-0 shadow-sm">
                <div className="card-body p-3 p-md-4">
                    <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                        <div className="d-flex align-items-center gap-3">
                            <div className="rounded-3 bg-warning bg-opacity-10 text-warning p-3">
                                <Clock3 size={25} />
                            </div>

                            <div>
                                <h5 className="fw-bold mb-1">
                                    Vehículos dentro del parqueadero
                                </h5>

                                <p className="text-secondary small mb-0">
                                    Consulta las estancias activas y registra sus pagos y salidas.
                                </p>
                            </div>
                        </div>

                        <span className="badge rounded-pill text-bg-primary px-3 py-2">
                            {estancias.length} estacionados
                        </span>
                    </div>

                    {cargando ? (
                        <div className="text-center py-5">
                            <div
                                className="spinner-border text-primary"
                                role="status"
                            >
                                <span className="visually-hidden">
                                    Cargando...
                                </span>
                            </div>

                            <p className="text-secondary mt-3 mb-0">
                                Consultando estancias activas...
                            </p>
                        </div>
                    ) : estancias.length === 0 ? (
                        <div className="text-center py-5">
                            <div className="rounded-circle bg-light d-inline-flex p-4 mb-3">
                                <CarFront
                                    size={36}
                                    className="text-secondary"
                                />
                            </div>

                            <h6 className="fw-bold">
                                No hay vehículos estacionados
                            </h6>

                            <p className="text-secondary mb-0">
                                Los vehículos registrados aparecerán aquí cuando ingresen.
                            </p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th className="py-3">Placa</th>
                                        <th className="py-3">Propietario</th>
                                        <th className="py-3">Celda</th>
                                        <th className="py-3">Tipo</th>
                                        <th className="py-3">Fecha de entrada</th>
                                        <th className="py-3 text-end">
                                            Acción
                                        </th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {estancias.map((estancia) => (
                                        <tr key={estancia.id}>
                                            <td>
                                                <span className="badge text-bg-light border text-dark px-3 py-2">
                                                    {estancia.vehiculo?.placa || "—"}
                                                </span>
                                            </td>

                                            <td>
                                                <div className="d-flex align-items-center gap-2">
                                                    <UserRound
                                                        size={17}
                                                        className="text-secondary flex-shrink-0"
                                                    />

                                                    <span>
                                                        {estancia.vehiculo?.propietario || "—"}
                                                    </span>
                                                </div>
                                            </td>

                                            <td>
                                                <span className="badge text-bg-secondary">
                                                    {estancia.celda?.codigo || "—"}
                                                </span>
                                            </td>

                                            <td>
                                                {estancia.vehiculo?.tipo || "—"}
                                            </td>

                                            <td>
                                                <div className="d-flex align-items-center gap-2 text-secondary">
                                                    <Clock3 size={16} />

                                                    <span>
                                                        {formatoFecha(estancia.entrada)}
                                                    </span>
                                                </div>
                                            </td>

                                            <td className="text-end">
                                                <button
                                                    type="button"
                                                    className="btn btn-sm btn-primary d-inline-flex align-items-center gap-2"
                                                    onClick={() =>
                                                        registrarSalida(estancia)
                                                    }
                                                >
                                                    <Wallet size={16} />
                                                    Cobrar y salir
                                                    <ArrowUpFromLine size={16} />
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Información de tarifa */}
            <div className="d-flex align-items-center gap-2 text-secondary small mt-3">
                <ShieldCheck size={16} />

                <span>
                    La tarifa es de $2.000 COP por cada hora iniciada.
                    El pago se confirma antes de liberar la celda.
                </span>
            </div>

            {/* Modal de liquidación y pago */}
            {estanciaParaPago && (
                <LiquidacionPagoModal
                    estancia={estanciaParaPago}
                    onCerrar={() => setEstanciaParaPago(null)}
                    onPagoConfirmado={manejarPagoConfirmado}
                />
            )}
        </div>
    );
}
