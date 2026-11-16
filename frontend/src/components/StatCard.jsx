
export default function StatCard({
    icono,
    color = 'blue',
    titulo,
    valor,
    descripcion,
}) {
    return (
        <div className="stat-card">
            <span className={`stat-icon ${color}`}>
                {icono}
            </span>

            <div>
                <small>{titulo}</small>

                <h3 className={typeof valor === 'string' ? 'stat-word' : ''}>
                    {valor}
                </h3>

                <span className="stat-foot">{descripcion}</span>
            </div>
        </div>
    );
}
