import { useCallback, useEffect, useState } from "react";
import {
    CheckCircle2,
    CircleAlert,
    RefreshCw,
    Wallet,
} from "lucide-react";

import { mensualidadService } from "../../services/mensualidadService";

function periodoActual() {
    const hoy = new Date();
    return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, "0")}`;
}

function fechaLocal() {
    const hoy = new Date();
    return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, "0")}-${String(hoy.getDate()).padStart(2, "0")}`;
}

function moneda(valor) {
    return new Intl.NumberFormat("es-CO", {
        style: "currency",
        currency: "COP",
        maximumFractionDigits: 2,
    }).format(Number(valor || 0));
}

function fechaHora(valor) {
    if (!valor) return "—";
    const fecha = new Date(valor);
    return Number.isNaN(fecha.getTime())
        ? valor
        : fecha.toLocaleString("es-CO", {
              dateStyle: "medium",
              timeStyle: "short",
          });
}

export default function PagosMensualesPage() {
    const [periodo, setPeriodo] = useState(periodoActual);
    const [membresias, setMembresias] = useState([]);
    const [estados, setEstados] = useState([]);
    const [usuarioId, setUsuarioId] = useState("");
    const [valor, setValor] = useState("");
    const [metodo, setMetodo] = useState("EFECTIVO");
    const [nuevoUsuarioId, setNuevoUsuarioId] = useState("");
    const [fechaInicio, setFechaInicio] = useState(fechaLocal);
    const [cargando, setCargando] = useState(true);
    const [guardandoPago, setGuardandoPago] = useState(false);
    const [guardandoMembresia, setGuardandoMembresia] = useState(false);
    const [error, setError] = useState("");
    const [mensaje, setMensaje] = useState("");

    const cargarDatos = useCallback(async () => {
        setCargando(true);
        setError("");

        try {
            const [listaMembresias, listaEstados] = await Promise.all([
                mensualidadService.listarMembresias(),
                mensualidadService.consultarEstado(periodo),
            ]);
            setMembresias(Array.isArray(listaMembresias) ? listaMembresias : []);
            setEstados(Array.isArray(listaEstados) ? listaEstados : []);
        } catch (e) {
            setError(e.message || "No fue posible consultar las mensualidades.");
        } finally {
            setCargando(false);
        }
    }, [periodo]);

    useEffect(() => {
        cargarDatos();
    }, [cargarDatos]);

    async function registrarPago(evento) {
        evento.preventDefault();
        setError("");
        setMensaje("");
        setGuardandoPago(true);

        try {
            await mensualidadService.registrarPago({
                usuarioId: Number(usuarioId),
                periodo,
                valor: Number(valor),
                metodo,
            });
            setMensaje("El pago mensual se registró correctamente.");
            setValor("");
            await cargarDatos();
        } catch (e) {
            setError(e.message || "No fue posible registrar el pago.");
        } finally {
            setGuardandoPago(false);
        }
    }

    async function registrarMembresia(evento) {
        evento.preventDefault();
        setError("");
        setMensaje("");
        setGuardandoMembresia(true);

        try {
            await mensualidadService.registrarMembresia({
                usuarioId: Number(nuevoUsuarioId),
                fechaInicio,
            });
            setMensaje("El usuario quedó inscrito como mensualista.");
            setNuevoUsuarioId("");
            await cargarDatos();
        } catch (e) {
            setError(e.message || "No fue posible inscribir al mensualista.");
        } finally {
            setGuardandoMembresia(false);
        }
    }

    async function finalizarMembresia(id) {
        setError("");
        setMensaje("");

        try {
            await mensualidadService.finalizarMembresia(id);
            setMensaje("La membresía se finalizó correctamente.");
            await cargarDatos();
        } catch (e) {
            setError(e.message || "No fue posible finalizar la membresía.");
        }
    }

    return (
        <div className="container-fluid px-2 px-md-3 py-3">
            <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                <div>
                    <span className="text-primary fw-semibold small">
                        ADMINISTRACIÓN DEL PARQUEADERO
                    </span>
                    <h2 className="fw-bold mb-1">Pagos mensuales</h2>
                    <p className="text-secondary mb-0">
                        Administra las membresías y consulta los pagos por período.
                    </p>
                </div>

                <button
                    type="button"
                    className="btn btn-outline-secondary d-flex align-items-center gap-2"
                    onClick={cargarDatos}
                    disabled={cargando}
                >
                    <RefreshCw size={17} />
                    {cargando ? "Actualizando..." : "Actualizar"}
                </button>
            </div>

            {error && (
                <div className="alert alert-danger d-flex align-items-start gap-2" role="alert">
                    <CircleAlert size={20} className="flex-shrink-0" />
                    <div>{error}</div>
                </div>
            )}

            {mensaje && (
                <div className="alert alert-success d-flex align-items-start gap-2" role="status">
                    <CheckCircle2 size={20} className="flex-shrink-0" />
                    <div>{mensaje}</div>
                </div>
            )}

            <div className="row g-3 mb-4">
                <div className="col-12 col-xl-5">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body p-3 p-md-4">
                            <h5 className="fw-bold mb-3">Inscribir mensualista</h5>
                            <form onSubmit={registrarMembresia}>
                                <div className="mb-3">
                                    <label htmlFor="nuevoUsuarioId" className="form-label">
                                        ID del usuario
                                    </label>
                                    <input
                                        id="nuevoUsuarioId"
                                        className="form-control"
                                        type="number"
                                        min="1"
                                        required
                                        value={nuevoUsuarioId}
                                        onChange={(evento) => setNuevoUsuarioId(evento.target.value)}
                                        disabled={guardandoMembresia}
                                    />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="fechaInicio" className="form-label">
                                        Fecha de inicio
                                    </label>
                                    <input
                                        id="fechaInicio"
                                        className="form-control"
                                        type="date"
                                        required
                                        value={fechaInicio}
                                        onChange={(evento) => setFechaInicio(evento.target.value)}
                                        disabled={guardandoMembresia}
                                    />
                                </div>
                                <button
                                    type="submit"
                                    className="btn btn-outline-primary"
                                    disabled={guardandoMembresia || !nuevoUsuarioId}
                                >
                                    {guardandoMembresia ? "Inscribiendo..." : "Inscribir"}
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <div className="col-12 col-xl-7">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body p-3 p-md-4">
                            <h5 className="fw-bold mb-3">Registrar pago mensual</h5>
                            <form onSubmit={registrarPago}>
                                <div className="row g-3">
                                    <div className="col-12 col-md-6">
                                        <label htmlFor="usuarioId" className="form-label">
                                            Mensualista
                                        </label>
                                        <select
                                            id="usuarioId"
                                            className="form-select"
                                            required
                                            value={usuarioId}
                                            onChange={(evento) => setUsuarioId(evento.target.value)}
                                            disabled={cargando || guardandoPago}
                                        >
                                            <option value="">Selecciona un usuario</option>
                                            {membresias.map((membresia) => (
                                                <option
                                                    key={membresia.id}
                                                    value={membresia.usuarioId}
                                                >
                                                    #{membresia.usuarioId} — {membresia.nombres}
                                                    {membresia.estado === "FINALIZADA" ? " (finalizada)" : ""}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="col-12 col-md-3">
                                        <label htmlFor="periodo" className="form-label">
                                            Período
                                        </label>
                                        <input
                                            id="periodo"
                                            className="form-control"
                                            type="month"
                                            required
                                            value={periodo}
                                            onChange={(evento) => setPeriodo(evento.target.value)}
                                            disabled={guardandoPago}
                                        />
                                    </div>
                                    <div className="col-12 col-md-3">
                                        <label htmlFor="valor" className="form-label">
                                            Valor (COP)
                                        </label>
                                        <input
                                            id="valor"
                                            className="form-control"
                                            type="number"
                                            min="0.01"
                                            step="0.01"
                                            required
                                            value={valor}
                                            onChange={(evento) => setValor(evento.target.value)}
                                            disabled={guardandoPago}
                                        />
                                    </div>
                                    <div className="col-12 col-md-6">
                                        <label htmlFor="metodo" className="form-label">
                                            Método de pago
                                        </label>
                                        <select
                                            id="metodo"
                                            className="form-select"
                                            value={metodo}
                                            onChange={(evento) => setMetodo(evento.target.value)}
                                            disabled={guardandoPago}
                                        >
                                            <option value="EFECTIVO">Efectivo</option>
                                            <option value="TARJETA">Tarjeta</option>
                                            <option value="TRANSFERENCIA">Transferencia</option>
                                        </select>
                                    </div>
                                    <div className="col-12 col-md-6 d-flex align-items-end">
                                        <button
                                            type="submit"
                                            className="btn btn-primary d-flex align-items-center gap-2"
                                            disabled={guardandoPago || !usuarioId || !valor}
                                        >
                                            <Wallet size={17} />
                                            {guardandoPago ? "Registrando..." : "Registrar pago"}
                                        </button>
                                    </div>
                                </div>
                            </form>
                            <p className="form-text mt-3 mb-0">
                                La fecha y hora del pago se toman del servidor. Se permiten períodos anteriores;
                                los períodos futuros no se aceptan.
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div className="card border-0 shadow-sm mb-4">
                <div className="card-body p-3 p-md-4">
                    <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
                        <div>
                            <h5 className="fw-bold mb-1">Estado de mensualidades</h5>
                            <p className="text-secondary small mb-0">
                                El vencimiento se calcula desde el período y la fecha del servidor.
                            </p>
                        </div>
                        <span className="badge text-bg-light border">{periodo}</span>
                    </div>
                    <div className="table-responsive">
                        <table className="table table-hover align-middle mb-0">
                            <thead className="table-light">
                                <tr>
                                    <th>Usuario</th>
                                    <th>Documento</th>
                                    <th>Período</th>
                                    <th>Estado</th>
                                    <th>Valor</th>
                                    <th>Fecha de pago</th>
                                </tr>
                            </thead>
                            <tbody>
                                {cargando ? (
                                    <tr><td colSpan="6" className="text-center py-4">Consultando...</td></tr>
                                ) : estados.length === 0 ? (
                                    <tr><td colSpan="6" className="text-center text-secondary py-4">No hay mensualistas para este período.</td></tr>
                                ) : estados.map((item) => (
                                    <tr key={`${item.usuarioId}-${item.periodo}`}>
                                        <td>{item.nombres}</td>
                                        <td>{item.numeroDocumento}</td>
                                        <td>{item.periodo}</td>
                                        <td>
                                            <span className={`badge ${
                                                item.estado === "PAGADO"
                                                    ? "text-bg-success"
                                                    : item.estado === "VENCIDO"
                                                        ? "text-bg-danger"
                                                        : "text-bg-warning"
                                            }`}>
                                                {item.estado}
                                            </span>
                                        </td>
                                        <td>{moneda(item.valor)}</td>
                                        <td>{fechaHora(item.fechaPago)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div className="card border-0 shadow-sm">
                <div className="card-body p-3 p-md-4">
                    <h5 className="fw-bold mb-3">Membresías registradas</h5>
                    <div className="table-responsive">
                        <table className="table table-hover align-middle mb-0">
                            <thead className="table-light">
                                <tr>
                                    <th>ID usuario</th>
                                    <th>Nombre</th>
                                    <th>Documento</th>
                                    <th>Inicio</th>
                                    <th>Fin</th>
                                    <th>Estado</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                {membresias.length === 0 ? (
                                    <tr><td colSpan="7" className="text-center text-secondary py-4">No hay membresías registradas.</td></tr>
                                ) : membresias.map((membresia) => (
                                    <tr key={membresia.id}>
                                        <td>{membresia.usuarioId}</td>
                                        <td>{membresia.nombres}</td>
                                        <td>{membresia.numeroDocumento}</td>
                                        <td>{membresia.fechaInicio}</td>
                                        <td>{membresia.fechaFin || "—"}</td>
                                        <td>{membresia.estado}</td>
                                        <td>
                                            {membresia.estado === "ACTIVA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-sm btn-outline-danger"
                                                    onClick={() => finalizarMembresia(membresia.id)}
                                                >
                                                    Finalizar
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
