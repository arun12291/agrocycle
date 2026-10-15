// =====================================================
// AgroCycle — Common JS (all pages)
// =====================================================

const API = '';
let currentUser = null;

// ── CSRF Token ──
function getCsrfToken() {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : '';
}

// ── Fetch helper ──
async function apiFetch(url, options = {}) {
  const defaults = {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken(), ...options.headers }
  };
  const res = await fetch(API + url, { ...defaults, ...options, headers: { ...defaults.headers, ...(options.headers || {}) } });
  if (res.status === 401) {
    window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.href);
    return null;
  }
  return res;
}

// ── Auth state ──
async function checkAuth() {
  try {
    const res = await fetch('/api/auth/me', { credentials: 'include' });
    if (res.ok) {
      currentUser = await res.json();
      updateNavForUser(currentUser);
      return currentUser;
    }
  } catch(e) {}
  updateNavForGuest();
  return null;
}

function updateNavForUser(user) {
  document.getElementById('loginBtn')?.classList.add('hidden');
  document.getElementById('registerBtn')?.classList.add('hidden');
  const menu = document.getElementById('userMenu');
  if (menu) {
    menu.classList.remove('hidden');
    const greet = document.getElementById('userGreet');
    if (greet) greet.textContent = `👋 ${user.fullName.split(' ')[0]}`;
  }
  // Show notification bell for authenticated users
  document.getElementById('notifBell')?.classList.remove('hidden');
  pollNotifications();
}

function updateNavForGuest() {
  document.getElementById('userMenu')?.classList.add('hidden');
  document.getElementById('notifBell')?.classList.add('hidden');
}

async function logout() {
  await apiFetch('/api/auth/logout', { method: 'POST' });
  currentUser = null;
  window.location.href = '/';
}

// ── Notifications ──
let notifPollInterval = null;

async function pollNotifications() {
  await loadNotifCount();
  if (!notifPollInterval) {
    notifPollInterval = setInterval(loadNotifCount, 30000); // Poll every 30s
  }
}

async function loadNotifCount() {
  try {
    const res = await fetch('/api/notifications/unread-count', { credentials: 'include' });
    if (res.ok) {
      const data = await res.json();
      const el = document.getElementById('notifCount');
      if (el) {
        el.textContent = data.count;
        el.style.display = data.count > 0 ? 'inline' : 'none';
      }
    }
  } catch(e) {}
}

function toggleNotifPanel() {
  const panel = document.getElementById('notifPanel');
  if (panel) {
    panel.classList.toggle('hidden');
    if (!panel.classList.contains('hidden')) loadNotifications();
  }
}

async function loadNotifications() {
  const list = document.getElementById('notifList');
  if (!list) return;
  list.innerHTML = '<div class="loading">Loading...</div>';
  try {
    const res = await fetch('/api/notifications?limit=20', { credentials: 'include' });
    if (!res.ok) { list.innerHTML = '<div class="loading">Failed to load</div>'; return; }
    const notifs = await res.json();
    if (!notifs.length) { list.innerHTML = '<div class="empty-state"><p>No notifications yet</p></div>'; return; }
    list.innerHTML = notifs.map(n => `
      <div class="notif-item ${n.isRead ? '' : 'unread'}" onclick="readNotif(${n.id}, ${n.relatedOrderId})">
        <div class="notif-title">${escHtml(n.title)}</div>
        <div class="notif-msg">${escHtml(n.message)}</div>
        <div class="notif-time">${timeAgo(n.createdAt)}</div>
      </div>`).join('');
  } catch(e) { list.innerHTML = '<div class="loading">Error loading notifications</div>'; }
}

async function readNotif(id, orderId) {
  await fetch(`/api/notifications/${id}/read`, { method: 'POST', credentials: 'include', headers: { 'X-XSRF-TOKEN': getCsrfToken() } });
  await loadNotifCount();
  await loadNotifications();
  if (orderId) window.location.href = `/dashboard.html?tab=orders&id=${orderId}`;
}

async function markAllRead() {
  await fetch('/api/notifications/read-all', { method: 'POST', credentials: 'include', headers: { 'X-XSRF-TOKEN': getCsrfToken() } });
  await loadNotifCount();
  await loadNotifications();
}

// ── Chat widget ──
function toggleChat() { document.getElementById('chatBox')?.classList.toggle('hidden'); }

async function sendChat() {
  const input = document.getElementById('chatInput');
  const msg = input?.value?.trim();
  if (!msg) return;
  appendChat(msg, 'user');
  input.value = '';
  try {
    const res = await fetch('/api/assistant/chat', {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken() },
      body: JSON.stringify({ message: msg })
    });
    const data = await res.json();
    appendChat(data.reply || 'Sorry, I could not understand that.', 'bot');
  } catch(e) { appendChat('Unable to reach assistant. Please try again.', 'bot'); }
}

function appendChat(text, who) {
  const msgs = document.getElementById('chatMessages');
  if (!msgs) return;
  const div = document.createElement('div');
  div.className = `chat-msg ${who}`;
  div.textContent = text;
  msgs.appendChild(div);
  msgs.scrollTop = msgs.scrollHeight;
}

// ── Navbar mobile toggle ──
function toggleNav() { document.getElementById('navLinks')?.classList.toggle('open'); }

// ── Utilities ──
function escHtml(str) {
  if (!str) return '';
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function timeAgo(dateStr) {
  if (!dateStr) return '';
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

function fmtDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function fmtCurrency(val) {
  if (val == null) return '—';
  return '₹' + parseFloat(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function statusBadge(status) {
  return `<span class="status-badge status-${status}">${status.replace(/_/g,' ')}</span>`;
}

function showModal(html) {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.innerHTML = `<div class="modal">${html}</div>`;
  overlay.addEventListener('click', e => { if(e.target === overlay) overlay.remove(); });
  document.body.appendChild(overlay);
  return overlay;
}

function showAlert(msg, type='error') {
  const el = document.getElementById('alertBox');
  if (el) { el.className = `alert-${type}`; el.textContent = msg; el.classList.remove('hidden'); }
  else { alert(msg); }
}

// ── Initialize on every page ──
document.addEventListener('DOMContentLoaded', () => { checkAuth(); });
