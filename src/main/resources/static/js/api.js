const API_BASE = '/api/v1';

const Session = {
    save(auth) {
        localStorage.setItem('ds_access', auth.accessToken);
        localStorage.setItem('ds_refresh', auth.refreshToken);
        localStorage.setItem('ds_role', auth.role);
        localStorage.setItem('ds_uid', auth.userId);
        localStorage.setItem('ds_name', auth.firstName);
    },
    clear() {
        ['ds_access', 'ds_refresh', 'ds_role', 'ds_uid', 'ds_name']
            .forEach(k => localStorage.removeItem(k));
    },
    access() { return localStorage.getItem('ds_access'); },
    refresh() { return localStorage.getItem('ds_refresh'); },
    role() { return localStorage.getItem('ds_role'); },
};

/**
 * @param {string} path       путь после /api/v1
 * @param {object} opts       method, body, skipAuth
 */
async function apiFetch(path, opts = {}) {
    const { method = 'GET', body, skipAuth = false, _retry = true } = opts;

    const headers = { 'Content-Type': 'application/json' };
    const token = Session.access();
    if (token && !skipAuth) headers['Authorization'] = 'Bearer ' + token;

    const resp = await fetch(API_BASE + path, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (resp.status === 401 && !skipAuth && _retry && Session.refresh()) {
        const ok = await tryRefresh();
        if (ok) return apiFetch(path, { ...opts, _retry: false });
    }

    if (resp.status === 204) return null;

    const text = await resp.text();
    let data = null;
    if (text) {
        try { data = JSON.parse(text); } catch (e) { data = text; }
    }

    if (!resp.ok) {
        let message = 'Ошибка запроса (' + resp.status + ')';
        if (data && data.message) message = data.message;
        if (data && data.fieldErrors) {
            message = Object.entries(data.fieldErrors)
                .map(([f, m]) => `${f}: ${m}`).join('; ');
        }
        const err = new Error(message);
        err.status = resp.status;
        err.data = data;
        throw err;
    }

    return data;
}

async function tryRefresh() {
    try {
        const resp = await fetch(API_BASE + '/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: Session.refresh() }),
        });
        if (!resp.ok) { Session.clear(); return false; }
        const auth = await resp.json();
        Session.save(auth);
        return true;
    } catch (e) {
        Session.clear();
        return false;
    }
}