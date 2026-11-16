import { useEffect, useState } from 'react';
import { estanciaService } from '../../services/estanciaService';
import { novedadService } from '../../services/novedadService';
import { vehiculoService } from '../../services/vehiculoService';

export default function NovedadesPage() {
    const [novedades, setNovedades] = useState([]);
    const [vehiculos, setVehiculos] = useState([]);
    const [estancias, setEstancias] = useState([]);
    const [tipoVinculo, setTipoVinculo] = useState('vehiculo');
    const [vinculoId, setVinculoId] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState('');
    const [mensaje, setMensaje] = useState('');

    async function cargar() {
        setCargando(true);
        try {
            const [listaNovedades, listaVehiculos, listaEstancias] = await Promise.all([
                novedadService.listar(),
                vehiculoService.listar(),
                estanciaService.listarTodas(),
            ]);
            setNovedades(listaNovedades);
            setVehiculos(listaVehiculos);
            setEstancias(listaEstancias);
            setError('');
        } catch (fallo) {
            setError(fallo.message);
        } finally {
            setCargando(false);
        }
    }

    useEffect(() => {
        cargar();
    }, []);

    async function registrar(evento) {
        evento.preventDefault();
        setGuardando(true);
        setError('');
        setMensaje('');
        try {
            const destino = tipoVinculo === 'vehiculo'
                ? { vehiculoId: Number(vinculoId) }
                : { estanciaId: Number(vinculoId) };
            await novedadService.registrar({ ...destino, descripcion });
            setMensaje('La novedad fue registrada.');
            setDescripcion('');
            setVinculoId('');
            await cargar();
        } catch (fallo) {
            setError(fallo.message);
        } finally {
            setGuardando(false);
        }
    }

    const opciones = tipoVinculo === 'vehiculo' ? vehiculos : estancias;

    return (
        <>
            <section className="welcome">
                <div>
                    <h2>Novedades</h2>
                    <p>Registra y consulta observaciones asociadas a un vehículo o estancia.</p>
                </div>
                <span className="status-pill">Administrador y operario</span>
            </section>

            {mensaje && <div className="alert alert-success">{mensaje}</div>}
            {error && <div className="alert alert-danger">{error}</div>}

            <div className="row g-4">
                <div className="col-lg-5">
                    <form className="card border-0 shadow-sm" onSubmit={registrar}>
                        <div className="card-body">
                            <h3 className="h5 mb-3">Registrar novedad</h3>
                            <label className="form-label" htmlFor="tipoVinculo">
                                Asociar a
                            </label>
                            <select
                                id="tipoVinculo"
                                className="form-select mb-3"
                                value={tipoVinculo}
                                onChange={(e) => {
                                    setTipoVinculo(e.target.value);
                                    setVinculoId('');
                                }}
                            >
                                <option value="vehiculo">Vehículo</option>
                                <option value="estancia">Estancia</option>
                            </select>
                            <label className="form-label" htmlFor="vinculoId">
                                {tipoVinculo === 'vehiculo' ? 'Vehículo *' : 'Estancia *'}
                            </label>
                            <select
                                id="vinculoId"
                                className="form-select mb-3"
                                value={vinculoId}
                                onChange={(e) => setVinculoId(e.target.value)}
                                required
                            >
                                <option value="">Selecciona una opción</option>
                                {opciones.map((opcion) => (
                                    <option key={opcion.id} value={opcion.id}>
                                        {tipoVinculo === 'vehiculo'
                                            ? `${opcion.placa} — ${opcion.propietario}`
                                            : `Estancia #${opcion.id} — ${opcion.vehiculo?.placa || ''}`}
                                    </option>
                                ))}
                            </select>
                            <label className="form-label" htmlFor="descripcion">
                                Descripción *
                            </label>
                            <textarea
                                id="descripcion"
                                className="form-control mb-3"
                                rows={4}
                                maxLength={1000}
                                value={descripcion}
                                onChange={(e) => setDescripcion(e.target.value)}
                                required
                            />
                            <button
                                className="btn btn-primary"
                                disabled={guardando || !vinculoId}
                            >
                                {guardando ? 'Guardando…' : 'Registrar novedad'}
                            </button>
                            <p className="small text-secondary mt-2 mb-0">
                                La fecha y el responsable se asignan desde el servidor y la sesión.
                            </p>
                        </div>
                    </form>
                </div>

                <div className="col-lg-7">
                    <div className="card border-0 shadow-sm">
                        <div className="card-body">
                            <h3 className="h5">Historial de novedades</h3>
                            {cargando ? (
                                <p>Cargando novedades…</p>
                            ) : novedades.length === 0 ? (
                                <p className="text-secondary">No hay novedades registradas.</p>
                            ) : (
                                <div className="list-group list-group-flush">
                                    {novedades.map((novedad) => (
                                        <article className="list-group-item px-0" key={novedad.id}>
                                            <div className="d-flex justify-content-between gap-3">
                                                <strong>
                                                    {novedad.placa
                                                        ? `Vehículo ${novedad.placa}`
                                                        : `Estancia #${novedad.estanciaId}`}
                                                </strong>
                                                <time className="text-secondary small">
                                                    {new Date(novedad.fechaHora).toLocaleString()}
                                                </time>
                                            </div>
                                            <p className="mb-1 mt-2">{novedad.descripcion}</p>
                                            <small className="text-secondary">
                                                Responsable: {novedad.responsable}
                                            </small>
                                        </article>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
