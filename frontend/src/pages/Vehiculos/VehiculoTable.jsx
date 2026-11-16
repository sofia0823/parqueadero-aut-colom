export default function VehiculoTable({
    vehiculos,
    cargando,
    cargar,
}) {
    return (
        <section className="panel">
            <div className="panel-heading">
                <div>
                    <h3>Vehículos registrados</h3>
                    <p>Listado de vehículos guardados en la base de datos.</p>
                </div>

                <button
                    className="btn btn-outline-secondary btn-sm"
                    onClick={cargar}
                    disabled={cargando}
                >
                    ↻ Actualizar
                </button>
            </div>

            {cargando ? (
                <div className="empty-state">Cargando información...</div>
            ) : vehiculos.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">▤</div>
                    <strong>Aún no hay vehículos</strong>
                    <span>Registra el primer vehículo con el formulario.</span>
                </div>
            ) : (
                <div className="table-responsive">
                    <table className="table align-middle">
                        <thead>
                            <tr>
                                <th>PLACA</th>
                                <th>PROPIETARIO</th>
                                <th>TIPO</th>
                                <th>CONTACTO</th>
                            </tr>
                        </thead>

                        <tbody>
                            {vehiculos.map((v) => (
                                <tr key={v.id}>
                                    <td>
                                        <span className="plate">{v.placa}</span>
                                    </td>
                                    <td>{v.propietario}</td>
                                    <td>{v.tipo || '—'}</td>
                                    <td>{v.telefono || '—'}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );
}