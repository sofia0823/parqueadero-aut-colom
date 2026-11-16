
import {
    BrowserRouter,
    Navigate,
    Routes,
    Route,
} from 'react-router-dom';

import DashboardLayout from '../layouts/DashboardLayout';
import { AuthProvider } from '../auth/AuthContext';
import RequireAuth from '../auth/RequireAuth';
import RequireRole from '../auth/RequireRole';

import DashboardPage from '../pages/Dashboard/DashboardPage';
import VehiculosPage from '../pages/Vehiculos/VehiculosPage';
import EntradasSalidasPage from '../pages/EntradasSalidas/EntradasSalidasPage';
import CeldasPage from '../pages/Celdas/CeldasPage';
import PagosMensualesPage from '../pages/PagosMensuales/PagosMensualesPage';
import LoginPage from '../pages/Login/LoginPage';
import UsuariosPage from '../pages/Usuarios/UsuariosPage';
import NovedadesPage from '../pages/Novedades/NovedadesPage';

export default function AppRouter() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route element={<RequireAuth />}>
                        <Route element={<DashboardLayout />}>
                            <Route index element={<DashboardPage />} />
                            <Route path="vehiculos" element={<VehiculosPage />} />
                            <Route path="entradas-salidas" element={<EntradasSalidasPage />} />
                            <Route path="novedades" element={<NovedadesPage />} />
                            <Route element={<RequireRole role="ADMINISTRADOR" />}>
                                <Route path="celdas" element={<CeldasPage />} />
                                <Route path="pagos-mensuales" element={<PagosMensualesPage />} />
                                <Route path="usuarios" element={<UsuariosPage />} />
                            </Route>
                        </Route>
                    </Route>
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
