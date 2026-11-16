import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';

export default function LoginPage() {
    const { user, login } = useAuth();
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [submitting, setSubmitting] = useState(false);

    if (user) return <Navigate to="/" replace />;

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');
        setSubmitting(true);
        try {
            await login(username.trim(), password);
            navigate('/', { replace: true });
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="container min-vh-100 d-flex align-items-center justify-content-center py-4">
            <section className="card shadow-sm border-0 w-100" style={{ maxWidth: 420 }}>
                <div className="card-body p-4 p-md-5">
                    <p className="text-primary fw-semibold mb-2">AUTOS COLOMBIA</p>
                    <h1 className="h3 mb-2">Iniciar sesión</h1>
                    <p className="text-secondary mb-4">
                        Ingresa con tu cuenta de personal del parqueadero.
                    </p>
                    {error && <div className="alert alert-danger" role="alert">{error}</div>}
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label className="form-label" htmlFor="username">Usuario</label>
                            <input
                                autoComplete="username"
                                className="form-control"
                                id="username"
                                onChange={(event) => setUsername(event.target.value)}
                                required
                                value={username}
                            />
                        </div>
                        <div className="mb-4">
                            <label className="form-label" htmlFor="password">Contraseña</label>
                            <input
                                autoComplete="current-password"
                                className="form-control"
                                id="password"
                                onChange={(event) => setPassword(event.target.value)}
                                required
                                type="password"
                                value={password}
                            />
                        </div>
                        <button className="btn btn-primary w-100" disabled={submitting} type="submit">
                            {submitting ? 'Validando...' : 'Entrar'}
                        </button>
                    </form>
                </div>
            </section>
        </main>
    );
}
