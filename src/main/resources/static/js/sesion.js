function leerSesion() {
    const token = localStorage.getItem('token');
    if (!token) return null;
    try {
        const b64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        const bytes = Uint8Array.from(atob(b64), c => c.charCodeAt(0));
        const payload = JSON.parse(new TextDecoder().decode(bytes));
        if (payload.exp && payload.exp * 1000 < Date.now()) {
            localStorage.removeItem('token');
            return null;
        }
        return { username: payload.sub };
    } catch (e) {
        localStorage.removeItem('token');
        return null;
    }
}

function cerrarSesion() {
    localStorage.removeItem('token');
    window.location.href = '/login';
}

function pintarSesion(contenedor) {
    const sesion = leerSesion();
    contenedor.replaceChildren();
    if (sesion) {
        const nombre = document.createElement('span');
        nombre.textContent = sesion.username;
        const salir = document.createElement('a');
        salir.href = '#';
        salir.textContent = 'Salir';
        salir.onclick = e => { e.preventDefault(); cerrarSesion(); };
        contenedor.append(nombre, salir);

        fetch('/api/yo', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } })
            .then(r => r.ok ? r.json() : null)
            .then(d => {
                if (d && String(d.rol).toLowerCase() === 'admin') {
                    const admin = document.createElement('a');
                    admin.href = '/admin';
                    admin.textContent = 'Administrar';
                    contenedor.prepend(admin);
                }
            })
            .catch(() => {});
    } else {
        const entrar = document.createElement('a');
        entrar.href = '/login';
        entrar.textContent = 'Entrar';
        contenedor.append(entrar);
    }
}