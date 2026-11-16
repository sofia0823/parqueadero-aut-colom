
import { Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Topbar from '../components/Topbar';

export default function DashboardLayout() {
    return (
        <div className="app-shell">
            <Sidebar />

            <main className="main-content">
                <Topbar />

                <Outlet />

                <footer>
                    © 2026 Autos Colombia · Sistema de información de parqueadero
                </footer>
            </main>
        </div>
    );
}
