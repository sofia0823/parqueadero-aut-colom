import { useEffect, useState } from 'react';
import { usuarioService } from '../../services/usuarioService';

const FORMULARIO_VACIO = {
    tipoDocumento: '',
    numeroDocumento: '',
    nombres: '',
    telefono: '',
    correo: '',
};

export default function UsuariosPage() {
    const [usuarios, setUsuarios] = useState([]);
    const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
    const [seleccionado, setSeleccionado] = useState(null);
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);
    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');

    async function cargar() {
        setCargando(true);
        try {
            setUsuarios(await usuarioService.listar());
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

    function cambiar(campo, valor) {
        setFormulario((actual) => ({ ...actual, [campo]: valor }));
    }

    function seleccionar(usuario) {
        setSeleccionado(usuario.id);
        setFormulario({
            tipoDocumento: usuario.tipoDocumento,
            numeroDocumento: usuario.numeroDocumento,
            nombres: usuario.nombres,
            telefono: usuario.telefono || '',
            correo: usuario.correo || '',
        });
        setMensaje('');
        setError('');
    }

    function limpiar() {
        setSeleccionado(null);
        setFormulario(FORMULARIO_VACIO);
    }

    async function guardar(evento) {
        evento.preventDefault();
        setGuardando(true);
        setError('');
        setMensaje('');
        const datos = {
            ...formulario,
            telefono: formulario.telefono || null,
            correo: formulario.correo || null,
        };
        try {
            if (seleccionado) {
                await usuarioService.actualizar(seleccionado, datos);
                setMensaje('Los datos del usuario fueron actualizados.');
            } else {
                await usuarioService.crear(datos);
                setMensaje('El usuario fue registrado.');
            }
            limpiar();
            await cargar();
        } catch (fallo) {
            setError(fallo.message);
        } finally {
            setGuardando(false);
        }
    }

    async function desactivar(usuario) {
        if (!window.confirm(`¿Desactivar a ${usuario.nombres}?`)) return;
        setError('');
        setMensaje('');
        try {
            await usuarioService.desactivar(usuario.id);
            setMensaje('El usuario fue desactivado; su historial se conserva.');
            if (seleccionado === usuario.id) limpiar();
            await cargar();
        } catch (fallo) {
            setError(fallo.message);
        }
    }

    return (
        <>
            <section className="welcome">
                <div>
                    <h2>Gestión de usuarios</h2>
                    <p>Administra los datos de identificación y contacto.</p>
                </div>
                <span className="status-pill">Solo administrador</span>
            </section>

            {mensaje && <div className="alert alert-success">{mensaje}</div>}
            {error && <div className="alert alert-danger">{error}</div>}

            <div className="row g-4">
                <div className="col-lg-5">
                    <form className="card border-0 shadow-sm" onSubmit={guardar}>
                        <div className="card-body">
                            <h3 className="h5 mb-3">
                                {seleccionado ? 'Actualizar usuario' : 'Nuevo usuario'}
                            </h3>
                            <label className="form-label" htmlFor="tipoDocumento">
                                Tipo de identificación *
                            </label>
                            <select
                                id="tipoDocumento"
                                className="form-select mb-3"
                                value={formulario.tipoDocumento}
                                onChange={(e) => cambiar('tipoDocumento', e.target.value)}
                                required
                            >
                                <option value="">Selecciona un tipo</option>
                                {['CC', 'CE', 'TI', 'PASAPORTE', 'NIT'].map((tipo) => (
                                    <option key={tipo} value={tipo}>{tipo}</option>
                                ))}
                            </select>
                            <label className="form-label" htmlFor="numeroDocumento">
                                Número de identificación *
                            </label>
                            <input
                                id="numeroDocumento"
                                className="form-control mb-3"
                                maxLength={25}
                                value={formulario.numeroDocumento}
                                onChange={(e) => cambiar('numeroDocumento', e.target.value)}
                                required
                            />
                            <label className="form-label" htmlFor="nombres">Nombres *</label>
                            <input
                                id="nombres"
                                className="form-control mb-3"
                                maxLength={100}
                                value={formulario.nombres}
                                onChange={(e) => cambiar('nombres', e.target.value)}
                                required
                            />
                            <label className="form-label" htmlFor="telefono">Teléfono</label>
                            <input
                                id="telefono"
                                className="form-control mb-3"
                                maxLength={30}
                                value={formulario.telefono}
                                onChange={(e) => cambiar('telefono', e.target.value)}
                            />
                            <label className="form-label" htmlFor="correo">Correo</label>
                            <input
                                id="correo"
                                type="email"
                                className="form-control mb-3"
                                maxLength={120}
                                value={formulario.correo}
                                onChange={(e) => cambiar('correo', e.target.value)}
                            />
                            <div className="d-flex gap-2">
                                <button className="btn btn-primary" disabled={guardando}>
                                    {guardando ? 'Guardando…' : seleccionado ? 'Guardar cambios' : 'Crear usuario'}
                                </button>
                                {seleccionado && (
                                    <button
                                        type="button"
                                        className="btn btn-outline-secondary"
                                        onClick={limpiar}
                                    >
                                        Cancelar
                                    </button>
                                )}
                            </div>
                        </div>
                    </form>
                </div>

                <div className="col-lg-7">
                    <div className="card border-0 shadow-sm">
                        <div className="card-body">
                            <h3 className="h5">Usuarios registrados</h3>
                            {cargando ? (
                                <p>Cargando usuarios…</p>
                            ) : usuarios.length === 0 ? (
                                <p className="text-secondary">No hay usuarios registrados.</p>
                            ) : (
                                <div className="table-responsive">
                                    <table className="table align-middle">
                                        <thead>
                                            <tr>
                                                <th>Identificación</th>
                                                <th>Nombre</th>
                                                <th>Contacto</th>
                                                <th>Estado</th>
                                                <th>Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {usuarios.map((usuario) => (
                                                <tr key={usuario.id}>
                                                    <td>{usuario.tipoDocumento} {usuario.numeroDocumento}</td>
                                                    <td>{usuario.nombres}</td>
                                                    <td>{usuario.telefono || usuario.correo || '—'}</td>
                                                    <td>{usuario.activo ? 'Activo' : 'Inactivo'}</td>
                                                    <td>
                                                        <div className="d-flex gap-2">
                                                            <button
                                                                type="button"
                                                                className="btn btn-sm btn-outline-primary"
                                                                onClick={() => seleccionar(usuario)}
                                                            >
                                                                Ver / editar
                                                            </button>
                                                            {usuario.activo && (
                                                                <button
                                                                    type="button"
                                                                    className="btn btn-sm btn-outline-danger"
                                                                    onClick={() => desactivar(usuario)}
                                                                >
                                                                    Desactivar
                                                                </button>
                                                            )}
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
