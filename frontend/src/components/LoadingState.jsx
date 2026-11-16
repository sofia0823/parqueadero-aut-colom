
export default function LoadingState({
    mensaje = 'Cargando información...',
}) {
    return (
        <div className="empty-state" role="status">
            <div className="spinner-border text-primary mb-3" />
            <span>{mensaje}</span>
        </div>
    );
}
