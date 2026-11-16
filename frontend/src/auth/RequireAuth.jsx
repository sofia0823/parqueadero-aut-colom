import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function RequireAuth() {
    const { user, loading, error } = useAuth();

    if (loading) {
        return <main className="container py-5">Validando la sesión...</main>;
    }
    if (error) {
        return (
            <main className="container py-5">
                <div className="alert alert-danger" role="alert">
                    No fue posible validar la sesión: {error.message}
                </div>
                <button
                    className="btn btn-outline-primary"
                    onClick={() => window.location.reload()}
                    type="button"
                >
                    Reintentar
                </button>
            </main>
        );
    }
    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
}
