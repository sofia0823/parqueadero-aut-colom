
export default function AlertMessage({ mensaje, error }) {
    return (
        <>
            {mensaje && (
                <div className="alert alert-success" role="status">
                    {mensaje}
                </div>
            )}

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}
        </>
    );
}
