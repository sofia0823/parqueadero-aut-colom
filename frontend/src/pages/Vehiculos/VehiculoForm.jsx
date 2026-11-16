export default function VehiculoForm({
    form,
    cambiar,
    registrar,
    guardando,
}) {
    return (
        <section className="panel h-100">
            <div className="panel-heading">
                <div>
                    <h3>Registrar vehículo</h3>
                    <p>Ingresa la información del propietario y vehículo.</p>
                </div>
                <span className="step-badge">01</span>
            </div>

            <form onSubmit={registrar} className="vehicle-form">
                <label className="form-label">Placa del vehículo *</label>
                <input
                    className="form-control"
                    name="placa"
                    value={form.placa}
                    onChange={cambiar}
                    placeholder="Ej. ABC123"
                    maxLength={10}
                    required
                />

                <label className="form-label">Nombre del propietario *</label>
                <input
                    className="form-control"
                    name="propietario"
                    value={form.propietario}
                    onChange={cambiar}
                    placeholder="Nombre completo"
                    maxLength={60}
                    required
                />

                <label className="form-label">Teléfono de contacto</label>
                <input
                    className="form-control"
                    name="telefono"
                    value={form.telefono}
                    onChange={cambiar}
                    placeholder="Ej. 3001234567"
                    maxLength={30}
                />

                <label className="form-label">Tipo de vehículo</label>
                <select
                    className="form-select"
                    name="tipo"
                    value={form.tipo}
                    onChange={cambiar}
                >
                    <option>Automóvil</option>
                    <option>Motocicleta</option>
                    <option>Camioneta</option>
                    <option>Otro</option>
                </select>

                <button
                    className="btn btn-primary w-100 mt-3"
                    type="submit"
                    disabled={guardando}
                >
                    {guardando ? 'Registrando...' : '+ Registrar vehículo'}
                </button>
            </form>

            <p className="form-note">
                * Campos obligatorios. La placa debe ser única.
            </p>
        </section>
    );
}