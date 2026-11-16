
import { useAuth } from '../auth/AuthContext';

export default function Topbar() {
    const { user } = useAuth();
    const roleLabel = user?.role === 'ADMINISTRADOR' ? 'Administrador' : 'Operario';
    const initials = user?.username?.slice(0, 2).toUpperCase() || 'AC';

    return (
        <header className="topbar">
            <div>
                <span className="eyebrow">SISTEMA DE INFORMACIÓN</span>
                <h1>Panel de control</h1>
            </div>

            <div className="user-chip">
                <div className="avatar">{initials}</div>

                <div>
                    <strong>{user?.username}</strong>
                    <small>{roleLabel}</small>
                </div>
            </div>
        </header>
    );
}
