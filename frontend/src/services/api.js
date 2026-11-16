const API_URL = '/api';

let csrfToken = null;

export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

function readCsrfCookie() {
    const cookie = document.cookie
        .split(';')
        .map((item) => item.trim())
        .find((item) => item.startsWith('XSRF-TOKEN='));

    return cookie ? decodeURIComponent(cookie.slice('XSRF-TOKEN='.length)) : null;
}

async function ensureCsrfToken() {
    const cookieToken = readCsrfCookie();
    if (cookieToken) {
        csrfToken = cookieToken;
        return csrfToken;
    }

    const response = await fetch(`${API_URL}/auth/csrf`, {
        credentials: 'same-origin',
        headers: { Accept: 'application/json' },
    });

    if (!response.ok) {
        throw new ApiError('No fue posible preparar la sesión segura.', response.status);
    }

    await response.json();
    csrfToken = readCsrfCookie();
    if (!csrfToken) {
        throw new Error('El servidor no entregó la cookie de protección CSRF.');
    }

    return csrfToken;
}

export function clearCsrfToken() {
    csrfToken = null;
}

export async function apiRequest(endpoint, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const headers = new Headers(options.headers || {});
    if (options.body != null && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
    }
    headers.set('Accept', 'application/json');

    if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
        headers.set('X-XSRF-TOKEN', await ensureCsrfToken());
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        method,
        credentials: 'same-origin',
        headers,
    });

    const text = await response.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!response.ok) {
        const message =
            typeof data === 'object' && data !== null
                ? data.mensaje || data.message
                : null;
        throw new ApiError(
            message || `Error HTTP ${response.status}.`,
            response.status
        );
    }

    return data;
}