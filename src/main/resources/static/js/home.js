// =====================================================
// AgroCycle — Home Page JS
// =====================================================

const CATEGORIES = {
  PADDY_STRAW_RICE_HUSK: { icon: '🌾', name: 'Paddy Straw & Rice Husk' },
  WHEAT_STRAW: { icon: '🌿', name: 'Wheat Straw' },
  SUGARCANE_LEAVES_BAGASSE: { icon: '🎋', name: 'Sugarcane & Bagasse' },
  COTTON_STALKS: { icon: '🌼', name: 'Cotton Stalks' },
  GROUNDNUT_SHELLS: { icon: '🥜', name: 'Groundnut Shells' },
  COCONUT_HUSK: { icon: '🥥', name: 'Coconut Husk' },
  MAIZE_STALKS: { icon: '🌽', name: 'Maize Stalks' },
  VEGETABLE_WASTE: { icon: '🥦', name: 'Vegetable Waste' },
  FRUIT_WASTE: { icon: '🍎', name: 'Fruit Waste' },
  OTHER_RESIDUES: { icon: '♻️', name: 'Other Residues' }
};

async function loadHome() {
  renderCategories();
  await Promise.all([loadFeaturedListings(), loadImpactStats()]);
}

function renderCategories() {
  const grid = document.getElementById('categoryGrid');
  if (!grid) return;
  grid.innerHTML = Object.entries(CATEGORIES).map(([key, cat]) => `
    <div class="category-card" onclick="window.location.href='/marketplace.html?category=${key}'">
      <div class="category-icon">${cat.icon}</div>
      <div class="category-name">${cat.name}</div>
    </div>`).join('');
}

async function loadFeaturedListings() {
  const container = document.getElementById('featuredListings');
  if (!container) return;
  try {
    const res = await fetch('/api/listings/public/featured?count=6');
    const listings = await res.json();
    if (!listings.length) {
      container.innerHTML = '<div class="empty-state"><div class="empty-icon">📦</div><p>No listings yet. <a href="/register.html">Register</a> and post the first one!</p></div>';
      return;
    }
    container.innerHTML = listings.map(renderListingCard).join('');
  } catch(e) {
    container.innerHTML = '<div class="empty-state"><p>Unable to load listings</p></div>';
  }
}

function renderListingCard(l) {
  const img = l.imageUrls && l.imageUrls.length
    ? `<img class="listing-img" src="${escHtml(l.imageUrls[0])}" alt="${escHtml(l.productName)}" loading="lazy" onerror="this.style.display='none'">`
    : `<div class="listing-img">${CATEGORIES[l.category]?.icon || '📦'}</div>`;
  const badge = l.listingType === 'I_HAVE'
    ? '<span class="listing-badge badge-have">I HAVE</span>'
    : '<span class="listing-badge badge-need">I NEED</span>';
  return `
    <div class="listing-card" onclick="window.location.href='/listing-detail.html?id=${l.id}'">
      ${img}
      <div class="listing-body">
        ${badge}
        <div class="listing-title">${escHtml(l.productName)}</div>
        <div class="listing-meta">${CATEGORIES[l.category]?.name || l.category}</div>
        <div class="listing-price">${fmtCurrency(l.pricePerUnit)} / ${escHtml(l.unit)}</div>
        <div class="listing-qty">Available: ${l.availableQuantity} ${escHtml(l.unit)}</div>
        <div class="listing-location">📍 ${escHtml(l.district)}, ${escHtml(l.state)}</div>
      </div>
    </div>`;
}

async function loadImpactStats() {
  try {
    const res = await fetch('/api/admin/stats');
    if (!res.ok) return;
    const data = await res.json();
    const tonnes = data.estimatedMaterialReusedKg ? (data.estimatedMaterialReusedKg / 1000).toFixed(1) : 0;
    document.getElementById('totalMaterial').textContent = tonnes + ' T';
    document.getElementById('totalTransactions').textContent = data.completedTransactions || 0;
    document.getElementById('totalListings').textContent = data.activeListings || 0;
  } catch(e) {
    // Stats optional — admin endpoint, may be unauthorized
    document.getElementById('totalMaterial').textContent = 'Growing';
    document.getElementById('totalTransactions').textContent = 'Growing';
    document.getElementById('totalListings').textContent = 'Growing';
  }
}

function heroSearch() {
  const q = document.getElementById('heroSearch')?.value?.trim();
  if (q) window.location.href = `/marketplace.html?keyword=${encodeURIComponent(q)}`;
  else window.location.href = '/marketplace.html';
}

// Search suggestions
let suggTimeout = null;
document.addEventListener('DOMContentLoaded', () => {
  const input = document.getElementById('heroSearch');
  if (input) {
    input.addEventListener('input', () => {
      clearTimeout(suggTimeout);
      const q = input.value.trim();
      if (q.length < 2) { hideSuggestions(); return; }
      suggTimeout = setTimeout(() => fetchSuggestions(q), 300);
    });
    input.addEventListener('keypress', e => { if(e.key==='Enter') heroSearch(); });
    document.addEventListener('click', e => { if (!input.contains(e.target)) hideSuggestions(); });
  }
  loadHome();
});

async function fetchSuggestions(q) {
  try {
    const res = await fetch(`/api/assistant/suggestions?q=${encodeURIComponent(q)}`);
    const items = await res.json();
    const box = document.getElementById('searchSuggestions');
    if (!box) return;
    if (!items.length) { hideSuggestions(); return; }
    box.innerHTML = items.map(s => `<div class="suggestion-item" onclick="selectSuggestion('${escHtml(s)}')">${escHtml(s)}</div>`).join('');
    box.classList.remove('hidden');
  } catch(e) {}
}

function selectSuggestion(val) {
  document.getElementById('heroSearch').value = val;
  hideSuggestions();
  heroSearch();
}

function hideSuggestions() {
  document.getElementById('searchSuggestions')?.classList.add('hidden');
}
