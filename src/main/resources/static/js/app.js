/* ==================== state ==================== */

const state = {
    user: null,          // {id, firstName, phone, role}
    guest: false,
    cart: [],             // {productId, name, price, quantity}
    categories: null,      // [{category, subcategories}]
    productsCache: null,   // all products, for selects
    clientTab: 'catalog',
    clientFilters: { category: '', subcategory: '', search: '', page: 0 },
    warehouseTab: 'orders',
    warehouseStatus: 'CREATED',
    adminTab: 'orders',
    adminStatus: 'CREATED',
};

const ROLE_LABELS = {
    SYSTEM_ADMIN: 'Администратор',
    MANAGER: 'Менеджер',
    CLIENT: 'Клиент',
    WAREHOUSE_STAFF: 'Склад',
    COURIER: 'Курьер',
};

const STATUS_LABELS = {
    CREATED: 'Создан', PAID: 'Оплачен', ASSEMBLED: 'Собран',
    SHIPPED: 'В пути', DELIVERED: 'Доставлен',
    COMPLETED: 'Завершён', CANCELLED: 'Отменён',
};

const CATEGORY_LABELS = {
    DIARY: 'Молочное', BAKERY: 'Выпечка', DRINKS: 'Напитки', SNACKS: 'Снеки',
    MEAT: 'Мясо', GROCERY: 'Бакалея', VEGETABLES: 'Овощи и фрукты',
    FREEZINGS: 'Заморозка', DISHES: 'Готовые блюда',
};

const ALL_STATUSES = ['CREATED', 'PAID', 'ASSEMBLED', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED'];
const ALL_ROLES = ['MANAGER', 'WAREHOUSE_STAFF', 'COURIER', 'SYSTEM_ADMIN'];

/* ==================== helpers ==================== */

function prettify(v) {
    if (!v) return '—';
    return v.toString().toLowerCase().replace(/_/g, ' ')
        .replace(/^./, c => c.toUpperCase());
}

function money(v) {
    return Number(v).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' ₽';
}

function statusBadge(status) {
    return `<span class="badge badge-${status}">${STATUS_LABELS[status] || status}</span>`;
}

function showToast(message, type = 'success') {
    const el = document.getElementById('toast');
    el.textContent = message;
    el.className = 'toast ' + type;
    clearTimeout(showToast._t);
    showToast._t = setTimeout(() => el.classList.add('hidden'), 3200);
}

function esc(s) {
    if (s === null || s === undefined) return '';
    return s.toString().replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]));
}

async function guarded(fn) {
    try {
        await fn();
    } catch (e) {
        console.error(e);
        showToast(e.message || 'Что-то пошло не так', 'error');
    }
}

/* ==================== modal ==================== */

function openModal(title, bodyHtml) {
    document.getElementById('modalBox').innerHTML = `
        <div class="modal-header">
            <h3>${esc(title)}</h3>
            <button data-action="close-modal">✕</button>
        </div>
        <div class="modal-body">${bodyHtml}</div>`;
    document.getElementById('modalOverlay').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('modalOverlay').classList.add('hidden');
    document.getElementById('modalBox').innerHTML = '';
}

/* ==================== bootstrap ==================== */

document.addEventListener('DOMContentLoaded', () => {
    document.body.addEventListener('click', handleClick);
    document.body.addEventListener('submit', handleSubmit);
    document.body.addEventListener('change', handleChange);
    boot();
});

async function boot() {
    if (Session.access()) {
        try {
            const me = await apiFetch('/users/me');
            state.user = me;
            state.guest = false;
            render();
            return;
        } catch (e) {
            Session.clear();
        }
    }
    render();
}

function render() {
    if (!state.user && !state.guest) return renderLogin();
    renderTopbar();
    document.getElementById('topbar').classList.remove('hidden');

    if (state.guest) return renderClient();

    switch (state.user.role) {
        case 'CLIENT': return renderClient();
        case 'WAREHOUSE_STAFF': return renderWarehouse();
        case 'MANAGER':
        case 'SYSTEM_ADMIN': return renderAdmin();
        case 'COURIER': return renderCourier();
        default: return renderClient();
    }
}

/* ==================== topbar ==================== */

function renderTopbar() {
    const nav = document.getElementById('roleNav');
    const label = document.getElementById('userLabel');

    if (state.guest) {
        nav.innerHTML = `
            <button class="nav-btn ${state.clientTab === 'catalog' ? 'active' : ''}" data-action="client-tab" data-tab="catalog">Каталог</button>
            <button class="nav-btn ${state.clientTab === 'cart' ? 'active' : ''}" data-action="client-tab" data-tab="cart">Корзина (${state.cart.length})</button>`;
        label.innerHTML = `Гость <button class="btn btn-ghost btn-sm" data-action="exit-guest" style="margin-left:8px">Войти</button>`;
        document.getElementById('logoutBtn').classList.add('hidden');
        return;
    }

    document.getElementById('logoutBtn').classList.remove('hidden');
    label.innerHTML = `${esc(state.user.firstName)} <span class="role-badge">${ROLE_LABELS[state.user.role] || state.user.role}</span>`;

    if (state.user.role === 'CLIENT') {
        nav.innerHTML = `
            <button class="nav-btn ${state.clientTab === 'catalog' ? 'active' : ''}" data-action="client-tab" data-tab="catalog">Каталог</button>
            <button class="nav-btn ${state.clientTab === 'cart' ? 'active' : ''}" data-action="client-tab" data-tab="cart">Корзина (${state.cart.length})</button>
            <button class="nav-btn ${state.clientTab === 'orders' ? 'active' : ''}" data-action="client-tab" data-tab="orders">Мои заказы</button>`;
    } else if (state.user.role === 'WAREHOUSE_STAFF') {
        nav.innerHTML = `
            <button class="nav-btn ${state.warehouseTab === 'orders' ? 'active' : ''}" data-action="wh-tab" data-tab="orders">Заказы</button>
            <button class="nav-btn ${state.warehouseTab === 'stock' ? 'active' : ''}" data-action="wh-tab" data-tab="stock">Склад</button>`;
    } else if (state.user.role === 'MANAGER' || state.user.role === 'SYSTEM_ADMIN') {
        let tabs = `
            <button class="nav-btn ${state.adminTab === 'orders' ? 'active' : ''}" data-action="admin-tab" data-tab="orders">Заказы</button>
            <button class="nav-btn ${state.adminTab === 'catalog' ? 'active' : ''}" data-action="admin-tab" data-tab="catalog">Каталог</button>
            <button class="nav-btn ${state.adminTab === 'stock' ? 'active' : ''}" data-action="admin-tab" data-tab="stock">Склад</button>`;
        if (state.user.role === 'SYSTEM_ADMIN') {
            tabs += `<button class="nav-btn ${state.adminTab === 'staff' ? 'active' : ''}" data-action="admin-tab" data-tab="staff">Персонал</button>`;
        }
        nav.innerHTML = tabs;
    } else {
        nav.innerHTML = '';
    }
}

/* ==================== login screen ==================== */

function renderLogin() {
    document.getElementById('topbar').classList.add('hidden');
    document.getElementById('viewRoot').innerHTML = `
    <div class="auth-wrap">
        <div class="auth-card">
            <h1>🌙 Darkstore</h1>

            <div class="tabs">
                <button class="tab-btn active" data-action="auth-tab" data-tab="login">Вход</button>
                <button class="tab-btn" data-action="auth-tab" data-tab="register">Регистрация</button>
            </div>

            <form id="loginForm" class="tab-panel active">
                <label>Телефон</label>
                <input id="loginPhone" placeholder="+70000000001" required>
                <label>Пароль</label>
                <input id="loginPassword" type="password" required>
                <button type="submit" class="btn btn-primary btn-block">Войти</button>
            </form>

            <form id="registerForm" class="tab-panel">
                <div class="form-row">
                    <div><label>Имя</label><input id="regFirstName" required></div>
                    <div><label>Фамилия</label><input id="regLastName"></div>
                </div>
                <label>Телефон</label>
                <input id="regPhone" placeholder="+79991234567" required>
                <label>Email (необязательно)</label>
                <input id="regEmail" type="email">
                <label>Пароль</label>
                <input id="regPassword" type="password" minlength="8" required>
                <button type="submit" class="btn btn-primary btn-block">Зарегистрироваться</button>
            </form>

            <p class="hint">Демо-админ: +70000000001 / admin123</p>
            <div class="guest-link"><a data-action="guest-mode">Оформить заказ без регистрации →</a></div>
            <p id="authError" class="error-text hidden"></p>
        </div>
    </div>`;
}

function authError(msg) {
    const el = document.getElementById('authError');
    if (!el) return;
    el.textContent = msg;
    el.classList.remove('hidden');
}

async function doLogin() {
    document.getElementById('authError').classList.add('hidden');
    const phone = document.getElementById('loginPhone').value.trim();
    const password = document.getElementById('loginPassword').value;
    try {
        const auth = await apiFetch('/auth/login', { method: 'POST', body: { phone, password }, skipAuth: true });
        Session.save(auth);
        const me = await apiFetch('/users/me');
        state.user = me;
        state.guest = false;
        showToast('Добро пожаловать, ' + me.firstName + '!');
        render();
    } catch (e) {
        authError(e.message);
    }
}

async function doRegister() {
    document.getElementById('authError').classList.add('hidden');
    const body = {
        firstName: document.getElementById('regFirstName').value.trim(),
        lastName: document.getElementById('regLastName').value.trim(),
        phone: document.getElementById('regPhone').value.trim(),
        email: document.getElementById('regEmail').value.trim() || null,
        password: document.getElementById('regPassword').value,
    };
    try {
        const auth = await apiFetch('/auth/register', { method: 'POST', body, skipAuth: true });
        Session.save(auth);
        const me = await apiFetch('/users/me');
        state.user = me;
        state.guest = false;
        showToast('Регистрация успешна!');
        render();
    } catch (e) {
        authError(e.message);
    }
}

async function doLogout() {
    const refresh = Session.refresh();
    if (refresh) {
        try { await apiFetch('/auth/logout', { method: 'POST', body: { refreshToken: refresh } }); }
        catch (e) { /* best-effort */ }
    }
    Session.clear();
    state.user = null;
    state.guest = false;
    state.cart = [];
    render();
}

/* ==================== shared data loaders ==================== */

async function ensureCategories() {
    if (!state.categories) {
        state.categories = await apiFetch('/products/categories', { skipAuth: true });
    }
    return state.categories;
}

async function ensureAllProducts() {
    if (!state.productsCache) {
        const page = await apiFetch('/products?size=200', { skipAuth: true });
        state.productsCache = page.content;
    }
    return state.productsCache;
}

function subcategoryOptions(categoryCode, selected) {
    const cat = (state.categories || []).find(c => c.category === categoryCode);
    const subs = cat ? cat.subcategories : [];
    return `<option value="">Все подкатегории</option>` +
        subs.map(s => `<option value="${s}" ${s === selected ? 'selected' : ''}>${prettify(s)}</option>`).join('');
}

function categoryOptions(selected) {
    return `<option value="">Все категории</option>` +
        Object.keys(CATEGORY_LABELS).map(c =>
            `<option value="${c}" ${c === selected ? 'selected' : ''}>${CATEGORY_LABELS[c]}</option>`).join('');
}

/* ==================== client: catalog ==================== */

function renderClient() {
    const root = document.getElementById('viewRoot');
    if (state.clientTab === 'cart') return renderCart();
    if (state.clientTab === 'orders' && !state.guest) return renderMyOrders();
    renderCatalog();
}

async function renderCatalog() {
    const root = document.getElementById('viewRoot');
    root.innerHTML = `<div class="empty-state">Загрузка каталога…</div>`;
    await guarded(async () => {
        await ensureCategories();
        const f = state.clientFilters;
        const params = new URLSearchParams();
        if (f.category) params.set('category', f.category);
        if (f.subcategory) params.set('subcategory', f.subcategory);
        if (f.search) params.set('search', f.search);
        params.set('page', f.page);
        params.set('size', 12);
        const page = await apiFetch('/products?' + params.toString(), { skipAuth: true });

        root.innerHTML = `
            <div class="filters">
                <div>
                    <label>Категория</label>
                    <select id="fCategory">${categoryOptions(f.category)}</select>
                </div>
                <div>
                    <label>Подкатегория</label>
                    <select id="fSubcategory">${subcategoryOptions(f.category, f.subcategory)}</select>
                </div>
                <div style="flex:2">
                    <label>Поиск</label>
                    <input id="fSearch" placeholder="Молоко, хлеб…" value="${esc(f.search)}">
                </div>
                <div><button class="btn btn-primary" data-action="apply-filters">Найти</button></div>
            </div>
            <div class="grid" id="productGrid">
                ${page.content.length ? page.content.map(productCard).join('') : '<div class="empty-state">Ничего не найдено</div>'}
            </div>
            <div class="filters" style="justify-content:center;margin-top:20px">
                <button class="btn btn-ghost btn-sm" data-action="page-prev" ${page.number === 0 ? 'disabled' : ''}>← Назад</button>
                <span class="muted" style="align-self:center">Стр. ${page.number + 1} из ${Math.max(page.totalPages, 1)}</span>
                <button class="btn btn-ghost btn-sm" data-action="page-next" ${page.number + 1 >= page.totalPages ? 'disabled' : ''}>Вперёд →</button>
            </div>`;
    });
}

function productCard(p) {
    const img = p.pictureUrl || '/images/milk.png';
    return `
    <div class="card product-card">
        <img class="product-img" src="${esc(img)}" alt="${esc(p.name)}" onerror="this.src='/images/milk.png'">
        <div class="cat">${CATEGORY_LABELS[p.category] || p.category}${p.subCategory ? ' · ' + prettify(p.subCategory) : ''}</div>
        <div class="name">${esc(p.name)}</div>
        <div class="desc">${esc(p.description || '')}</div>
        <div class="price">${money(p.price)}</div>
        <button class="btn btn-primary btn-sm" data-action="add-to-cart"
                data-id="${p.id}" data-name="${esc(p.name)}" data-price="${p.price}">
            В корзину
        </button>
    </div>`;
}

/* ==================== client: cart & checkout ==================== */

function cartTotal() {
    return state.cart.reduce((sum, i) => sum + i.price * i.quantity, 0);
}

function renderCart() {
    const root = document.getElementById('viewRoot');
    const isGuest = state.guest;

    root.innerHTML = `
        <div class="section-title"><h2>Корзина</h2></div>
        ${state.cart.length === 0 ? '<div class="empty-state">Корзина пуста. Загляните в каталог 🙂</div>' : `
        <div class="card">
            ${state.cart.map(i => `
                <div class="cart-row">
                    <div>
                        <div style="font-weight:600">${esc(i.name)}</div>
                        <div class="muted">${money(i.price)} / шт</div>
                    </div>
                    <div class="qty-control">
                        <button data-action="cart-dec" data-id="${i.productId}">−</button>
                        <span>${i.quantity}</span>
                        <button data-action="cart-inc" data-id="${i.productId}">+</button>
                        <button data-action="cart-remove" data-id="${i.productId}" style="margin-left:8px;color:var(--red)">✕</button>
                    </div>
                </div>`).join('')}
            <div class="cart-total"><span>Итого</span><span>${money(cartTotal())}</span></div>
        </div>

        <div class="card" style="margin-top:16px">
            <div class="section-title"><h2>Оформление заказа</h2></div>
            <form id="checkoutForm">
                <div class="form-row">
                    <div><label>Имя</label><input id="coName" value="${isGuest ? '' : esc(state.user.firstName)}"></div>
                    <div><label>Телефон *</label><input id="coPhone" required value="${isGuest ? '' : esc(state.user.phone || '')}"></div>
                </div>
                <label>Способ оплаты</label>
                <select id="coPayment">
                    <option value="CARD">Картой курьеру</option>
                    <option value="ONLINE">Онлайн</option>
                    ${isGuest ? '' : '<option value="CASH">Наличными</option>'}
                </select>
                ${isGuest ? '<p class="muted">Оплата наличными доступна только зарегистрированным пользователям</p>' : ''}
                <label>Город *</label><input id="coCity" required value="Москва">
                <div class="form-row">
                    <div><label>Улица *</label><input id="coStreet" required></div>
                    <div><label>Дом *</label><input id="coHouse" required></div>
                </div>
                <div class="form-row">
                    <div><label>Подъезд</label><input id="coEntrance"></div>
                    <div><label>Этаж</label><input id="coFloor"></div>
                    <div><label>Квартира</label><input id="coApartment"></div>
                </div>
                <label>Комментарий</label>
                <textarea id="coComment" rows="2"></textarea>
                <button type="submit" class="btn btn-primary btn-block">Оформить заказ на ${money(cartTotal())}</button>
            </form>
        </div>`}`;
}

async function submitCheckout() {
    const body = {
        customerName: document.getElementById('coName').value.trim() || null,
        customerPhone: document.getElementById('coPhone').value.trim(),
        comment: document.getElementById('coComment').value.trim() || null,
        paymentType: document.getElementById('coPayment').value,
        address: {
            city: document.getElementById('coCity').value.trim(),
            street: document.getElementById('coStreet').value.trim(),
            house: document.getElementById('coHouse').value.trim(),
            entrance: document.getElementById('coEntrance').value.trim() || null,
            floor: document.getElementById('coFloor').value.trim() || null,
            apartment: document.getElementById('coApartment').value.trim() || null,
        },
        items: state.cart.map(i => ({ productId: i.productId, quantity: i.quantity })),
    };

    await guarded(async () => {
        await apiFetch('/orders', { method: 'POST', body, skipAuth: state.guest });
        state.cart = [];
        showToast('Заказ оформлен!');
        state.clientTab = state.guest ? 'catalog' : 'orders';
        render();
    });
}

/* ==================== client: my orders ==================== */

async function renderMyOrders() {
    const root = document.getElementById('viewRoot');
    root.innerHTML = `<div class="empty-state">Загрузка…</div>`;
    await guarded(async () => {
        const orders = await apiFetch('/orders/my');
        root.innerHTML = `
            <div class="section-title"><h2>Мои заказы</h2></div>
            ${orders.length === 0 ? '<div class="empty-state">Заказов пока нет</div>' :
                orders.map(o => orderCard(o, { showCancel: true })).join('')}`;
    });
}

function orderCard(o, opts = {}) {
    const canCancel = opts.showCancel && (o.status === 'CREATED' || o.status === 'PAID');
    return `
    <div class="card order-card">
        <div class="order-head">
            <div>
                <div class="oid">#${o.id.slice(0, 8)}</div>
                <div>${o.customerName ? esc(o.customerName) + ' · ' : ''}${esc(o.customerPhone)}</div>
            </div>
            ${statusBadge(o.status)}
        </div>
        <div class="order-items">
            ${o.items.map(i => `<div>${i.quantity} × ${esc(i.productName)} — ${money(i.priceAtPurchase)}</div>`).join('')}
        </div>
        <div class="muted">${esc(o.address.city)}, ${esc(o.address.street)} ${esc(o.address.house)}${o.address.apartment ? ', кв. ' + esc(o.address.apartment) : ''}</div>
        <div class="order-head" style="margin-top:10px;margin-bottom:0">
            <span class="order-total">${money(o.totalPrice)}</span>
            ${canCancel ? `<button class="btn btn-red btn-sm" data-action="cancel-order" data-id="${o.id}">Отменить заказ</button>` : ''}
        </div>
        ${opts.extraActions ? `<div class="order-actions">${opts.extraActions(o)}</div>` : ''}
    </div>`;
}

/* ==================== warehouse ==================== */

function renderWarehouse() {
    if (state.warehouseTab === 'stock') return renderStockOverview('viewRoot', false);
    return renderStaffOrders('viewRoot', 'warehouse');
}

/**
 * Общий рендер списка заказов и для склада, и для админки.
 * mode: 'warehouse' — только кнопка "Заказ собран"
 *       'admin'      — полный набор переходов статуса + правка состава
 */
async function renderStaffOrders(rootId, mode) {
    const root = document.getElementById(rootId);
    const statusVar = mode === 'admin' ? state.adminStatus : state.warehouseStatus;
    root.innerHTML = `<div class="empty-state">Загрузка заказов…</div>`;

    await guarded(async () => {
        const orders = await apiFetch('/orders/status/' + statusVar);
        const statusSelectAction = mode === 'admin' ? 'admin-status-filter' : 'wh-status-filter';

        root.innerHTML = `
            <div class="section-title">
                <h2>Заказы</h2>
                <select id="statusFilter" data-action-change="${statusSelectAction}">
                    ${ALL_STATUSES.map(s => `<option value="${s}" ${s === statusVar ? 'selected' : ''}>${STATUS_LABELS[s]}</option>`).join('')}
                </select>
            </div>
            ${orders.length === 0 ? '<div class="empty-state">Заказов со статусом «' + STATUS_LABELS[statusVar] + '» нет</div>' :
                orders.map(o => staffOrderCard(o, mode)).join('')}`;
    });
}

function staffOrderCard(o, mode) {
    let actions = '';

    if (mode === 'warehouse') {
        const canAssemble = o.status === 'CREATED' || o.status === 'PAID';
        actions = `
            <button class="btn btn-green btn-sm" data-action="set-status" data-id="${o.id}" data-status="ASSEMBLED" ${canAssemble ? '' : 'disabled'}>
                ✅ Заказ собран
            </button>
            <button class="btn btn-ghost btn-sm" data-action="edit-items" data-id="${o.id}" ${o.status === 'CREATED' || o.status === 'PAID' ? '' : 'disabled'}>
                ✏️ Изменить состав
            </button>`;
    } else if (mode === 'courier-lite') {
        if (o.status === 'ASSEMBLED') {
            actions = `<button class="btn btn-blue btn-sm" data-action="set-status" data-id="${o.id}" data-status="SHIPPED">🚚 Забрал, везу</button>`;
        } else if (o.status === 'SHIPPED') {
            actions = `<button class="btn btn-green btn-sm" data-action="set-status" data-id="${o.id}" data-status="DELIVERED">📦 Доставлено</button>`;
        } else {
            actions = `<span class="muted">Нет доступных действий</span>`;
        }
    } else {
        actions = nextStatusButtons(o).map(s =>
            `<button class="btn ${s === 'CANCELLED' ? 'btn-red' : 'btn-blue'} btn-sm" data-action="set-status" data-id="${o.id}" data-status="${s}">
                ${STATUS_LABELS[s]}
            </button>`).join('') +
            `<button class="btn btn-ghost btn-sm" data-action="edit-items" data-id="${o.id}" ${o.status === 'CREATED' || o.status === 'PAID' ? '' : 'disabled'}>
                ✏️ Изменить состав
            </button>`;
    }

    return `
    <div class="card order-card">
        <div class="order-head">
            <div>
                <div class="oid">#${o.id.slice(0, 8)}</div>
                <div>${o.customerName ? esc(o.customerName) + ' · ' : ''}${esc(o.customerPhone)}</div>
            </div>
            ${statusBadge(o.status)}
        </div>
        <div class="order-items">
            ${o.items.map(i => `<div>${i.quantity} × ${esc(i.productName)} — ${money(i.priceAtPurchase)}</div>`).join('')}
        </div>
        <div class="muted">${esc(o.address.city)}, ${esc(o.address.street)} ${esc(o.address.house)}${o.address.apartment ? ', кв. ' + esc(o.address.apartment) : ''}</div>
        <div class="order-head" style="margin-top:10px;margin-bottom:0">
            <span class="order-total">${money(o.totalPrice)}</span>
        </div>
        <div class="order-actions">${actions}</div>
    </div>`;
}

/** Следующие допустимые статусы согласно машине состояний в OrderService. */
function nextStatusButtons(o) {
    switch (o.status) {
        case 'CREATED': return ['PAID', 'ASSEMBLED', 'CANCELLED'];
        case 'PAID': return ['ASSEMBLED', 'CANCELLED'];
        case 'ASSEMBLED': return ['SHIPPED'];
        case 'SHIPPED': return ['DELIVERED'];
        case 'DELIVERED': return ['COMPLETED'];
        default: return [];
    }
}

/* ==================== stock overview (warehouse + admin) ==================== */

async function renderStockOverview(rootId, editable) {
    const root = document.getElementById(rootId);
    root.innerHTML = `<div class="empty-state">Загрузка склада…</div>`;

    await guarded(async () => {
        const products = await ensureAllProducts();
        const stocks = await Promise.all(products.map(p =>
            apiFetch('/admin/stock/' + p.id).catch(() => null)));

        const rows = products.map((p, idx) => {
            const s = stocks[idx];
            if (!s) return '';
            const low = s.availableQuantity <= 5;
            return `
            <tr>
                <td>${esc(p.name)}</td>
                <td>${CATEGORY_LABELS[p.category] || p.category}</td>
                <td>${s.quantity}</td>
                <td>${s.reservedQuantity}</td>
                <td class="${low ? 'stock-low' : ''}">${s.availableQuantity}</td>
                <td>${money(s.purchasePrice)}</td>
                <td><button class="btn btn-primary btn-sm" data-action="add-stock" data-id="${p.id}" data-name="${esc(p.name)}">+ Приход</button></td>
            </tr>`;
        }).join('');

        root.innerHTML = `
            <div class="section-title"><h2>Остатки на складе</h2></div>
            <div class="card">
                <table>
                    <thead><tr>
                        <th>Товар</th><th>Категория</th><th>Всего</th><th>Резерв</th><th>Доступно</th><th>Закупка</th><th></th>
                    </tr></thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>`;
    });
}

function openAddStockModal(productId, productName) {
    openModal('Приход товара: ' + productName, `
        <form id="addStockForm" data-product-id="${productId}">
            <label>Количество</label>
            <input id="stockQty" type="number" min="1" required>
            <label>Закупочная цена за ед.</label>
            <input id="stockPrice" type="number" min="0" step="0.01">
            <button type="submit" class="btn btn-primary btn-block">Добавить на склад</button>
        </form>`);
}

async function submitAddStock(form) {
    const productId = form.dataset.productId;
    const quantity = Number(document.getElementById('stockQty').value);
    const purchasePriceRaw = document.getElementById('stockPrice').value;
    const body = { quantity, purchasePrice: purchasePriceRaw ? Number(purchasePriceRaw) : null };

    await guarded(async () => {
        await apiFetch('/admin/stock/' + productId + '/add', { method: 'POST', body });
        closeModal();
        showToast('Остаток обновлён');
        state.productsCache = null; // сброс кэша, чтобы перечитать актуальные цифры не нужен, но пересчёт остатков — заново
        if (state.user.role === 'WAREHOUSE_STAFF') renderStockOverview('viewRoot', true);
        else renderStockOverview('viewRoot', true);
    });
}

/* ==================== edit order items modal ==================== */

async function openEditItemsModal(orderId) {
    await guarded(async () => {
        const order = await apiFetch('/orders/' + orderId);
        await ensureAllProducts();

        const rowsHtml = order.items.map(it => itemRow(it.id, it.productId, it.quantity, true)).join('');

        openModal('Состав заказа #' + orderId.slice(0, 8), `
            <form id="editItemsForm" data-order-id="${orderId}">
                <div id="itemRows">${rowsHtml}</div>
                <button type="button" class="btn btn-ghost btn-sm" data-action="add-item-row">+ Добавить позицию</button>
                <button type="submit" class="btn btn-primary btn-block" style="margin-top:12px">Сохранить изменения</button>
            </form>`);
    });
}

let itemRowCounter = 0;

function itemRow(existingOrderItemId, productId, quantity, isExisting) {
    itemRowCounter++;
    const rowId = 'row_' + itemRowCounter;
    const options = (state.productsCache || []).map(p =>
        `<option value="${p.id}" ${p.id === productId ? 'selected' : ''}>${esc(p.name)} (${money(p.price)})</option>`).join('');

    return `
    <div class="item-row" id="${rowId}" data-order-item-id="${isExisting ? existingOrderItemId : ''}">
        <select class="itemProduct">${options}</select>
        <input class="itemQty" type="number" min="1" value="${quantity || 1}">
        ${isExisting ? `<button type="button" class="btn btn-red btn-sm" data-action="remove-item-row" data-row="${rowId}">✕</button>` : ''}
    </div>`;
}

async function submitEditItems(form) {
    const orderId = form.dataset.orderId;
    const rows = form.querySelectorAll('.item-row');
    const items = [];

    rows.forEach(row => {
        const select = row.querySelector('.itemProduct');
        const qtyInput = row.querySelector('.itemQty');
        const orderItemId = row.dataset.orderItemId || null;

        if (row.dataset.removed === 'true') {
            // quantity всё равно должно пройти @Positive-валидацию на бэке,
            // хотя в ветке remove оно не используется
            items.push({ orderItemId, productId: select.value, quantity: 1, remove: true });
            return;
        }

        items.push({
            orderItemId: orderItemId || null,
            productId: select.value,
            quantity: Number(qtyInput.value),
            remove: false,
        });
    });

    if (items.length === 0) {
        showToast('Добавьте хотя бы одну позицию', 'error');
        return;
    }

    await guarded(async () => {
        await apiFetch('/orders/' + orderId + '/items', { method: 'PUT', body: { items } });
        closeModal();
        showToast('Состав заказа обновлён');
        render();
    });
}

/* ==================== admin ==================== */

function renderAdmin() {
    if (state.adminTab === 'catalog') return renderAdminCatalog();
    if (state.adminTab === 'stock') return renderStockOverview('viewRoot', true);
    if (state.adminTab === 'staff') return renderStaff();
    return renderStaffOrders('viewRoot', 'admin');
}

async function renderAdminCatalog() {
    const root = document.getElementById('viewRoot');
    root.innerHTML = `<div class="empty-state">Загрузка каталога…</div>`;
    await guarded(async () => {
        await ensureCategories();
        const page = await apiFetch('/products?size=200');

        root.innerHTML = `
            <div class="section-title">
                <h2>Каталог</h2>
                <button class="btn btn-primary btn-sm" data-action="new-product">+ Добавить товар</button>
            </div>
            <div class="card">
                <table>
                    <thead><tr><th>Товар</th><th>Категория</th><th>Подкатегория</th><th>Цена</th><th></th></tr></thead>
                    <tbody>
                        ${page.content.map(p => `
                        <tr>
                            <td>${esc(p.name)}</td>
                            <td>${CATEGORY_LABELS[p.category] || p.category}</td>
                            <td>${prettify(p.subCategory)}</td>
                            <td>${money(p.price)}</td>
                            <td>
                                <button class="btn btn-ghost btn-sm" data-action="edit-product" data-id="${p.id}">✏️</button>
                                <button class="btn btn-red btn-sm" data-action="delete-product" data-id="${p.id}">🗑</button>
                            </td>
                        </tr>`).join('')}
                    </tbody>
                </table>
            </div>`;
    });
}

function productFormFields(p = {}) {
    return `
        <label>Название *</label><input id="pfName" required value="${esc(p.name || '')}">
        <label>Описание</label><textarea id="pfDesc" rows="2">${esc(p.description || '')}</textarea>
        <label>URL картинки</label><input id="pfPicture" value="${esc(p.pictureUrl || '/images/milk.png')}">
        <label>Цена *</label><input id="pfPrice" type="number" min="0.01" step="0.01" required value="${p.price || ''}">
        <label>Категория *</label>
        <select id="pfCategory">${categoryOptions(p.category)}</select>
        <label>Подкатегория</label>
        <select id="pfSubcategory">${subcategoryOptions(p.category, p.subCategory)}</select>`;
}

async function openNewProductModal() {
    await ensureCategories();
    openModal('Новый товар', `
        <form id="productForm">
            ${productFormFields()}
            <button type="submit" class="btn btn-primary btn-block">Создать</button>
        </form>`);
}

async function openEditProductModal(productId) {
    await guarded(async () => {
        await ensureCategories();
        const p = await apiFetch('/products/' + productId, { skipAuth: true });
        openModal('Редактировать товар', `
            <form id="productForm" data-product-id="${productId}">
                ${productFormFields(p)}
                <button type="submit" class="btn btn-primary btn-block">Сохранить</button>
            </form>`);
    });
}

async function submitProductForm(form) {
    const body = {
        name: document.getElementById('pfName').value.trim(),
        description: document.getElementById('pfDesc').value.trim() || null,
        pictureUrl: document.getElementById('pfPicture').value.trim() || null,
        price: Number(document.getElementById('pfPrice').value),
        category: document.getElementById('pfCategory').value,
        subCategory: document.getElementById('pfSubcategory').value || null,
    };
    const productId = form.dataset.productId;

    await guarded(async () => {
        if (productId) {
            await apiFetch('/products/' + productId, { method: 'PATCH', body });
            showToast('Товар обновлён');
        } else {
            await apiFetch('/products', { method: 'POST', body });
            showToast('Товар создан');
        }
        closeModal();
        state.productsCache = null;
        renderAdminCatalog();
    });
}

async function deleteProduct(productId) {
    if (!confirm('Убрать товар из каталога?')) return;
    await guarded(async () => {
        await apiFetch('/products/' + productId, { method: 'DELETE' });
        showToast('Товар удалён из каталога');
        state.productsCache = null;
        renderAdminCatalog();
    });
}

/* ==================== admin: staff ==================== */

async function renderStaff() {
    const root = document.getElementById('viewRoot');
    root.innerHTML = `<div class="empty-state">Загрузка…</div>`;
    await guarded(async () => {
        const users = await apiFetch('/users');
        root.innerHTML = `
            <div class="section-title">
                <h2>Персонал и пользователи</h2>
                <button class="btn btn-primary btn-sm" data-action="new-staff">+ Создать сотрудника</button>
            </div>
            <div class="card">
                <table>
                    <thead><tr><th>Имя</th><th>Телефон</th><th>Email</th><th>Роль</th></tr></thead>
                    <tbody>
                        ${users.map(u => `
                        <tr>
                            <td>${esc(u.firstName)} ${esc(u.lastName || '')}</td>
                            <td>${esc(u.phone)}</td>
                            <td>${esc(u.email || '—')}</td>
                            <td>${ROLE_LABELS[u.role] || u.role}</td>
                        </tr>`).join('')}
                    </tbody>
                </table>
            </div>`;
    });
}

function openNewStaffModal() {
    openModal('Новый сотрудник', `
        <form id="staffForm">
            <div class="form-row">
                <div><label>Имя *</label><input id="sfFirstName" required></div>
                <div><label>Фамилия</label><input id="sfLastName"></div>
            </div>
            <label>Телефон *</label><input id="sfPhone" placeholder="+79991234567" required>
            <label>Email</label><input id="sfEmail" type="email">
            <label>Пароль *</label><input id="sfPassword" type="password" minlength="8" required>
            <label>Роль *</label>
            <select id="sfRole">${ALL_ROLES.map(r => `<option value="${r}">${ROLE_LABELS[r]}</option>`).join('')}</select>
            <button type="submit" class="btn btn-primary btn-block">Создать</button>
        </form>`);
}

async function submitStaffForm() {
    const body = {
        firstName: document.getElementById('sfFirstName').value.trim(),
        lastName: document.getElementById('sfLastName').value.trim(),
        phone: document.getElementById('sfPhone').value.trim(),
        email: document.getElementById('sfEmail').value.trim() || null,
        password: document.getElementById('sfPassword').value,
        role: document.getElementById('sfRole').value,
    };
    await guarded(async () => {
        await apiFetch('/users/staff', { method: 'POST', body });
        closeModal();
        showToast('Сотрудник создан');
        renderStaff();
    });
}

/* ==================== courier (минимальный вид) ==================== */

function renderCourier() {
    document.getElementById('roleNav').innerHTML = `
        <button class="nav-btn ${state.warehouseTab === 'orders' ? 'active' : ''}" data-action="wh-tab" data-tab="orders">Мои доставки</button>`;
    state.warehouseStatus = ALL_STATUSES.includes(state.warehouseStatus) ? state.warehouseStatus : 'ASSEMBLED';
    renderStaffOrders('viewRoot', 'courier-lite');
}

/* ==================== event dispatch ==================== */

function handleClick(e) {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const action = btn.dataset.action;

    switch (action) {
        // ---- auth screen ----
        case 'auth-tab': {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b === btn));
            document.getElementById('loginForm').classList.toggle('active', btn.dataset.tab === 'login');
            document.getElementById('registerForm').classList.toggle('active', btn.dataset.tab === 'register');
            break;
        }
        case 'guest-mode':
            state.guest = true;
            state.clientTab = 'catalog';
            render();
            break;
        case 'exit-guest':
            state.guest = false;
            state.cart = [];
            render();
            break;
        case 'logout':
            guarded(doLogout);
            break;

        // ---- nav ----
        case 'client-tab':
            state.clientTab = btn.dataset.tab;
            render();
            break;
        case 'wh-tab':
            state.warehouseTab = btn.dataset.tab;
            render();
            break;
        case 'admin-tab':
            state.adminTab = btn.dataset.tab;
            render();
            break;

        // ---- catalog ----
        case 'apply-filters':
            state.clientFilters.category = document.getElementById('fCategory').value;
            state.clientFilters.subcategory = document.getElementById('fSubcategory').value;
            state.clientFilters.search = document.getElementById('fSearch').value.trim();
            state.clientFilters.page = 0;
            renderCatalog();
            break;
        case 'page-prev':
            state.clientFilters.page = Math.max(0, state.clientFilters.page - 1);
            renderCatalog();
            break;
        case 'page-next':
            state.clientFilters.page += 1;
            renderCatalog();
            break;

        // ---- cart ----
        case 'add-to-cart': {
            const id = btn.dataset.id, name = btn.dataset.name, price = Number(btn.dataset.price);
            const existing = state.cart.find(i => i.productId === id);
            if (existing) existing.quantity += 1;
            else state.cart.push({ productId: id, name, price, quantity: 1 });
            renderTopbar();
            showToast(name + ' добавлен в корзину');
            break;
        }
        case 'cart-inc': {
            const item = state.cart.find(i => i.productId === btn.dataset.id);
            if (item) item.quantity += 1;
            renderCart(); renderTopbar();
            break;
        }
        case 'cart-dec': {
            const item = state.cart.find(i => i.productId === btn.dataset.id);
            if (item) { item.quantity -= 1; if (item.quantity <= 0) state.cart = state.cart.filter(i => i !== item); }
            renderCart(); renderTopbar();
            break;
        }
        case 'cart-remove':
            state.cart = state.cart.filter(i => i.productId !== btn.dataset.id);
            renderCart(); renderTopbar();
            break;

        // ---- my orders ----
        case 'cancel-order':
            if (confirm('Отменить заказ?')) {
                guarded(async () => {
                    await apiFetch('/orders/my/' + btn.dataset.id + '/cancel', { method: 'PATCH' });
                    showToast('Заказ отменён');
                    renderMyOrders();
                });
            }
            break;

        // ---- staff order actions ----
        case 'set-status':
            guarded(async () => {
                await apiFetch('/orders/' + btn.dataset.id + '/status?status=' + btn.dataset.status, { method: 'PATCH' });
                showToast('Статус обновлён: ' + STATUS_LABELS[btn.dataset.status]);
                render();
            });
            break;
        case 'edit-items':
            if (!btn.disabled) openEditItemsModal(btn.dataset.id);
            break;
        case 'add-item-row':
            document.getElementById('itemRows').insertAdjacentHTML('beforeend', itemRow(null, null, 1, false));
            break;
        case 'remove-item-row': {
            const row = document.getElementById(btn.dataset.row);
            if (!row) break;
            if (row.dataset.orderItemId) {
                // существующая позиция: не стираем из DOM, а помечаем на удаление,
                // иначе submitEditItems просто не узнает, что её нужно снять с брони
                row.dataset.removed = 'true';
                row.style.display = 'none';
            } else {
                row.remove(); // новая, ещё не сохранённая строка — можно просто убрать
            }
            break;
        }

        // ---- stock ----
        case 'add-stock':
            openAddStockModal(btn.dataset.id, btn.dataset.name);
            break;

        // ---- admin catalog ----
        case 'new-product':
            openNewProductModal();
            break;
        case 'edit-product':
            openEditProductModal(btn.dataset.id);
            break;
        case 'delete-product':
            deleteProduct(btn.dataset.id);
            break;

        // ---- staff management ----
        case 'new-staff':
            openNewStaffModal();
            break;

        // ---- modal ----
        case 'close-modal':
            closeModal();
            break;
    }
}

function handleSubmit(e) {
    const form = e.target;
    if (form.id === 'loginForm') { e.preventDefault(); doLogin(); }
    else if (form.id === 'registerForm') { e.preventDefault(); doRegister(); }
    else if (form.id === 'checkoutForm') { e.preventDefault(); submitCheckout(); }
    else if (form.id === 'addStockForm') { e.preventDefault(); submitAddStock(form); }
    else if (form.id === 'editItemsForm') { e.preventDefault(); submitEditItems(form); }
    else if (form.id === 'productForm') { e.preventDefault(); submitProductForm(form); }
    else if (form.id === 'staffForm') { e.preventDefault(); submitStaffForm(); }
}

function handleChange(e) {
    const el = e.target;

    if (el.id === 'fCategory') {
        state.clientFilters.category = el.value;
        state.clientFilters.subcategory = '';
        renderCatalog();
        return;
    }
    if (el.id === 'pfCategory') {
        const sub = document.getElementById('pfSubcategory');
        if (sub) sub.innerHTML = subcategoryOptions(el.value, null);
        return;
    }
    if (el.id === 'statusFilter') {
        const action = el.dataset.actionChange;
        if (action === 'admin-status-filter') { state.adminStatus = el.value; renderStaffOrders('viewRoot', 'admin'); }
        else if (action === 'wh-status-filter') {
            state.warehouseStatus = el.value;
            if (state.user && state.user.role === 'COURIER') renderStaffOrders('viewRoot', 'courier-lite');
            else renderStaffOrders('viewRoot', 'warehouse');
        }
    }
}