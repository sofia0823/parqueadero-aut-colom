import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let mounted = true;
        authService.session()
            .then((session) => {
                if (mounted) setUser(session);
            })
            .catch((requestError) => {
                if (!mounted) return;
                if (requestError.status !== 401) setError(requestError);
            })
            .finally(() => {
                if (mounted) setLoading(false);
            });

        return () => {
            mounted = false;
        };
    }, []);

    async function login(username, password) {
        const session = await authService.login(username, password);
        setUser(session);
        setError(null);
        return session;
    }

    async function logout() {
        await authService.logout();
        setUser(null);
    }

    const value = useMemo(
        () => ({ user, loading, error, login, logout }),
        [user, loading, error]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth debe usarse dentro de AuthProvider.');
    }
    return context;
}
