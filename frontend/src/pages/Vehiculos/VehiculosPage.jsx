import { useVehiculos } from '../../hooks/useVehiculos';
import VehiculoForm from './VehiculoForm';
import VehiculoTable from './VehiculoTable';

export default function VehiculosPage() {
    const {
        vehiculos,
        form,
        mensaje,
        error,
        cargando,
        guardando,
        cambiar,
        registrar,
        cargar,
    } = useVehiculos();

    return (
        <>
            <section className="welcome">
                <div>
                    <h2>Gestión de vehículos</h2>
                    <p>
                        Administra el registro de vehículos del parqueadero Autos Colombia.
                    </p>
                </div>
                <span className="status-pill">Sistema de gestión</span>
            </section>

            {mensaje && (
                <div className="alert alert-success">{mensaje}</div>
            )}

            {error && (
                <div className="alert alert-danger">{error}</div>
            )}

            <div className="row g-4">
                <div className="col-lg-5">
                    <VehiculoForm
                        form={form}
                        cambiar={cambiar}
                        registrar={registrar}
                        guardando={guardando}
                    />
                </div>

                <div className="col-lg-7">
                    <VehiculoTable
                        vehiculos={vehiculos}
                        cargando={cargando}
                        cargar={cargar}
                    />
                </div>
            </div>
        </>
    );
}