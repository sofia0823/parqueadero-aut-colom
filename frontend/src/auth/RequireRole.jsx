import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function RequireRole({ role }) {
    const { user } = useAuth();
    if (user?.role !== role) {
        return <Navigate to="/" replace />;
    }
    return <Outlet />;
}
