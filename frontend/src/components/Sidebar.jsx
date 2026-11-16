
import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
    LayoutDashboard,
    ArrowLeftRight,
    Grid2X2,
    Menu,
    X,
    CarFront,
    Wallet,
    Users,
    ClipboardList,
    LogOut
} from 'lucide-react';
import { useAuth } from '../auth/AuthContext';

const enlaces = [
    { ruta: '/', icono: LayoutDashboard, texto: 'Panel de vehículos' },
    { ruta: '/entradas-salidas', icono: ArrowLeftRight, texto: 'Entradas y salidas' },
    { ruta: '/celdas', icono: Grid2X2, texto: 'Celdas' },
    { ruta: '/pagos-mensuales', icono: Wallet, texto: 'Pagos mensuales' },
    { ruta: '/usuarios', icono: Users, texto: 'Usuarios' },
    { ruta: '/novedades', icono: ClipboardList, texto: 'Novedades' }
];

export default function Sidebar() {
    const [abierto, setAbierto] = useState(false);
    const [error, setError] = useState('');
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const colorMenu = '#172554';
    const colorTexto = '#cbd5e1';
    const enlacesVisibles = user?.role === 'ADMINISTRADOR'
        ? enlaces
        : enlaces.filter(({ ruta }) =>
            !['/pagos-mensuales', '/celdas', '/usuarios'].includes(ruta)
        );

    async function cerrarSesion() {
        setError('');
        try {
            await logout();
            navigate('/login', { replace: true });
        } catch (logoutError) {
            setError(logoutError.message);
        }
    }

    return (
        <>
            {/* Botón hamburguesa */}
            <button
                type="button"
                className="btn d-md-none position-fixed top-0 start-0 m-2"
                style={{
                    zIndex: 1100,
                    background: colorMenu,
                    color: '#fff',
                    borderRadius: 10
                }}
                onClick={() => setAbierto(!abierto)}
                aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'}
                aria-expanded={abierto}
            >
                {abierto ? <X size={23} /> : <Menu size={23} />}
            </button>

            {/* Fondo oscuro en móviles */}
            {abierto && (
                <div
                    className="position-fixed top-0 start-0 w-100 h-100 d-md-none"
                    style={{
                        background: 'rgba(15, 23, 42, 0.55)',
                        zIndex: 1040
                    }}
                    onClick={() => setAbierto(false)}
                />
            )}

            {/* Menú lateral */}
            <aside
                className="d-flex flex-column position-fixed top-0 start-0 h-100"
                style={{
                    width: 260,
                    maxWidth: '85vw',
                    background: colorMenu,
                    color: '#fff',
                    zIndex: 1050,
                    transform:
                        typeof window !== 'undefined' &&
                        window.innerWidth < 768
                            ? abierto
                                ? 'translateX(0)'
                                : 'translateX(-100%)'
                            : 'translateX(0)',
                    transition: 'transform 0.25s ease',
                    overflowY: 'auto',
                    boxShadow: '4px 0 20px rgba(15, 23, 42, 0.12)'
                }}
            >
                {/* Encabezado */}
                <div
                    className="d-flex align-items-center gap-3 p-3"
                    style={{ borderBottom: '1px solid #334155' }}
                >
                    <div
                        className="d-flex align-items-center justify-content-center rounded-3"
                        style={{
                            width: 44,
                            height: 44,
                            background: '#2563eb',
                            flexShrink: 0
                        }}
                    >
                        <CarFront size={26} color="#fff" />
                    </div>

                    <div className="d-flex flex-column">
                        <strong style={{ fontSize: 16 }}>
                            Autos Colombia
                        </strong>
                        <small style={{ color: '#94a3b8', fontSize: 12 }}>
                            Gestión de parqueadero
                        </small>
                    </div>
                </div>

                {/* Título */}
                <div className="px-3 pt-4 pb-2">
                    <small
                        className="fw-semibold"
                        style={{
                            color: '#94a3b8',
                            fontSize: 11,
                            letterSpacing: 1
                        }}
                    >
                        MENÚ PRINCIPAL
                    </small>
                </div>

                {/* Enlaces */}
                <nav className="d-flex flex-column gap-1 px-2">
                    {enlacesVisibles.map(({ ruta, icono: Icono, texto }) => (
                        <NavLink
                            key={ruta}
                            to={ruta}
                            end={ruta === '/'}
                            onClick={() => setAbierto(false)}
                            className="d-flex align-items-center gap-3 text-decoration-none rounded-3 px-3 py-3"
                            style={({ isActive }) => ({
                                color: isActive ? '#fff' : colorTexto,
                                background: isActive ? '#2563eb' : 'transparent',
                                fontWeight: isActive ? 600 : 400,
                                fontSize: 14,
                                transition: 'background 0.2s ease'
                            })}
                        >
                            <Icono size={20} strokeWidth={1.9} />
                            <span>{texto}</span>
                        </NavLink>
                    ))}
                </nav>

                {/* Pie */}
                <div
                    className="mt-auto p-3"
                    style={{ borderTop: '1px solid #334155' }}
                >
                    {error && <small className="d-block text-warning mb-2">{error}</small>}
                    <div className="d-flex align-items-center gap-2">
                        <span
                            style={{
                                width: 8,
                                height: 8,
                                borderRadius: '50%',
                                background: '#22c55e'
                            }}
                        />
                        <small style={{ color: '#94a3b8' }}>
                            {user?.role === 'ADMINISTRADOR' ? 'Administrador' : 'Operario'}
                        </small>
                    </div>
                    <button
                        className="btn btn-outline-light btn-sm w-100 mt-3 d-flex align-items-center justify-content-center gap-2"
                        onClick={cerrarSesion}
                        type="button"
                    >
                        <LogOut size={15} />
                        Cerrar sesión
                    </button>
                </div>
            </aside>
        </>
    );
}
