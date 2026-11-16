
export default function ModulePlaceholder({ title, description }) {
    return (
        <div className="container-fluid py-4">
            <div className="card shadow-sm border-0">
                <div className="card-body p-4">
                    <h2 className="mb-3">{title}</h2>
                    <p className="text-muted mb-0">{description}</p>
                </div>
            </div>
        </div>
    );
}
