// Custom cursor
const cursor = document.querySelector('.custom-cursor');
let interactables = document.querySelectorAll('a, button, input, .event-card');

document.addEventListener('mousemove', (e) => {
    cursor.style.left = e.clientX + 'px';
    cursor.style.top = e.clientY + 'px';
});

function attachCursorEvents(els) {
    els.forEach(el => {
        el.addEventListener('mouseenter', () => {
            cursor.style.transform = 'translate(-50%, -50%) scale(2.5)';
            cursor.style.background = 'transparent';
            cursor.style.border = '1px solid white';
        });
        
        el.addEventListener('mouseleave', () => {
            cursor.style.transform = 'translate(-50%, -50%) scale(1)';
            cursor.style.background = '#fff';
            cursor.style.border = 'none';
        });
    });
}
attachCursorEvents(interactables);

// Category Filtering logic
const catPills = document.querySelectorAll('.cat-pill');
function initFiltering() {
    const eventCards = document.querySelectorAll('.event-card');
    catPills.forEach(pill => {
        pill.addEventListener('click', () => {
            catPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            const filter = pill.getAttribute('data-filter');
            eventCards.forEach(card => {
                if (filter === 'all' || card.getAttribute('data-category') === filter) {
                    card.style.display = 'flex';
                    card.style.animation = 'none';
                    card.offsetHeight; /* trigger reflow */
                    card.style.animation = null; 
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });
}
initFiltering();

// Hologram 3D Hero Effect based on mouse
const heroVisual = document.getElementById('heroVisual');
const hologramCard = document.querySelector('.hologram-card');

if (heroVisual && hologramCard) {
    heroVisual.addEventListener('mousemove', (e) => {
        const rect = heroVisual.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const centerX = rect.width / 2;
        const centerY = rect.height / 2;
        const rotateX = ((y - centerY) / centerY) * -10; 
        const rotateY = ((x - centerX) / centerX) * 10;
        hologramCard.style.transform = `rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(20px)`;
    });

    heroVisual.addEventListener('mouseleave', () => {
        hologramCard.style.transform = `rotateX(-5deg) rotateY(15deg)`;
    });
}

// BACKEND API INTEGRATION
async function fetchEventsFromBackend(keyword = '') {
    try {
        const url = keyword ? `http://localhost:8081/api/events?keyword=${encodeURIComponent(keyword)}` : 'http://localhost:8081/api/events';
        const response = await fetch(url);
        if (!response.ok) return;
        
        const events = await response.json();
        if (events && events.length > 0) {
            // Populate Trending (Carousel)
            const grid = document.getElementById('eventGrid');
            if (grid) {
                grid.innerHTML = ''; 
                events.forEach((ev, idx) => {
                    const card = createEventCard(ev, idx);
                    grid.appendChild(card);
                });
                // Clone for infinite carousel
                const cards = grid.querySelectorAll('.event-card');
                cards.forEach(card => grid.appendChild(card.cloneNode(true)));
            }

            // Populate Category Grids
            const categories = ['music', 'theater', 'workshop', 'sport'];
            categories.forEach(cat => {
                const catGrid = document.getElementById(`${cat}Grid`);
                if (catGrid) {
                    catGrid.innerHTML = '';
                    const catEvents = events.filter(ev => ev.category === cat);
                    catEvents.forEach((ev, idx) => {
                        const card = createEventCard(ev, idx);
                        catGrid.appendChild(card);
                    });
                }
            });
            
            // Render Sự kiện nổi bật (Featured Event)
            const featured = events.find(ev => ev.isFeatured) || events[0];
            updateFeaturedHero(featured);

            // Re-attach events
            attachCursorEvents(document.querySelectorAll('.event-card, .buy-btn'));
            initFiltering(); 
            initScrollReveal(); 
        }
    } catch (e) {
        console.log('Backend chưa được khởi động. Đang sử dụng dữ liệu tĩnh.');
    }
}

function createEventCard(ev, idx) {
    const card = document.createElement('article');
    card.className = `event-card fade-up stagger-${(idx % 3) + 1}`;
    card.setAttribute('data-category', ev.category);
    
    const priceVal = ev.startingPrice ? ev.startingPrice.toLocaleString('vi-VN') : '0';
    const dateVal = ev.date ? new Date(ev.date).toLocaleDateString('vi-VN') : '';
    
    card.innerHTML = `
        <div class="card-image" style="background-image: url('${ev.image}'); background-position: top center; background-size: cover; transition: transform 0.5s;">
            <div class="status-badge ${ev.status === 'Đã kết thúc' ? 'alert' : 'live'}">${ev.status}</div>
        </div>
        <div class="card-body">
            <h3>${ev.title}</h3>
            <div class="event-info">
                <span class="date">${dateVal}</span>
                <span class="location">${ev.location || 'Chưa cập nhật'}</span>
            </div>
            <div class="card-footer">
                <div class="price">Từ <span>${priceVal}đ</span></div>
                <button class="buy-btn" onclick="window.location.href='event-detail.html?id=${ev.id}'">Mua</button>
            </div>
        </div>
    `;
    return card;
}

function updateFeaturedHero(featured) {
    const heroTitle = document.querySelector('.feature-content h1');
    const heroDesc = document.querySelector('.feature-content p');
    const featureSpans = document.querySelectorAll('.feature-meta span');
    const heroBuyBtn = document.querySelector('.feature-content .btn-primary');
    const heroDetailBtn = document.querySelector('.feature-content .btn-secondary');
    const hologramCard = document.querySelector('.hologram-card');

    if(heroTitle && featured) {
        const feDate = featured.date ? new Date(featured.date).toLocaleDateString('vi-VN') : '';
        heroTitle.innerHTML = `${featured.title}`;
        if(heroDesc) heroDesc.innerHTML = `Sự kiện đang dẫn đầu xu hướng hiện nay. Số lượng vé có hạn, hãy nhanh tay!`;
        
        const dateIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>`;
        const locIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>`;
        
        if(featureSpans.length >= 2) {
            featureSpans[0].innerHTML = `${dateIcon} ${feDate}`;
            featureSpans[1].innerHTML = `${locIcon} ${featured.location || 'Chưa cập nhật'}`;
        }
        
        if(heroBuyBtn) heroBuyBtn.onclick = () => window.location.href = `event-detail.html?id=${featured.id}`;
        if(heroDetailBtn) heroDetailBtn.onclick = () => window.location.href = `event-detail.html?id=${featured.id}`;
        
        if(hologramCard) {
            hologramCard.style.backgroundImage = `url('${featured.poster || featured.image}')`;
            hologramCard.style.backgroundSize = 'cover';
            hologramCard.style.backgroundPosition = 'center';
        }
    }
}

// Mascot Interactivity
const mascot = document.getElementById('mascotCompanion');
if (mascot) {
    mascot.addEventListener('click', () => {
        const events = document.querySelectorAll('.event-card h3');
        if (events.length > 0) {
            const randomEvent = events[Math.floor(Math.random() * events.length)].innerText;
            const tooltip = mascot.querySelector('.mascot-tooltip');
            const originalText = tooltip.innerText;
            tooltip.innerText = `Bạn nên xem thử: ${randomEvent}!`;
            tooltip.style.opacity = '1';
            tooltip.style.transform = 'translateY(0)';
            
            setTimeout(() => {
                tooltip.innerText = originalText;
            }, 4000);
        }
    });
}

// Scroll Reveal Animation
function initScrollReveal() {
    const revealElements = document.querySelectorAll('.fade-up');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });

    revealElements.forEach(el => observer.observe(el));
}

// Bắt đầu khởi tạo dữ liệu khi Load trang xong
document.addEventListener('DOMContentLoaded', () => {
    fetchEventsFromBackend();
    initScrollReveal();

    // Thêm chức năng tìm kiếm
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                const keyword = e.target.value.trim();
                fetchEventsFromBackend(keyword);
            }
        });
    }
});

// Update Header UI based on Auth JWT Token
document.addEventListener('DOMContentLoaded', () => {
    const currentUser = localStorage.getItem('stellar_user');
    const authBtn = document.getElementById('authBtn') || document.querySelector('.navbar .btn.btn-outline');
    if(authBtn && currentUser) {
        authBtn.innerHTML = `<i class="fa fa-user" style="margin-right: 8px;"></i> ${currentUser}`;
        authBtn.href = "profile.html";
        authBtn.classList.remove('btn-outline');
        authBtn.style.background = 'linear-gradient(90deg, var(--primary-purple), var(--primary-cyan))';
        authBtn.style.color = '#fff';
        authBtn.style.border = 'none';
        
        attachCursorEvents([authBtn]);
    } else if(authBtn) {
        authBtn.href = "auth.html";
    }

    // Role-based Access Control UI
    const rolesStr = localStorage.getItem('stellar_roles');
    if(rolesStr) {
        try {
            const roles = JSON.parse(rolesStr);
            const createEventNav = document.getElementById('createEventNav');
            const revenueNav = document.getElementById('revenueNav');
            const adminNav = document.getElementById('adminNav');

            if(createEventNav && (roles.includes('ROLE_ORGANIZER') || roles.includes('ROLE_ADMIN'))) {
                createEventNav.style.display = 'inline-block';
            }
            if(revenueNav && (roles.includes('ROLE_ORGANIZER') || roles.includes('ROLE_ADMIN'))) {
                revenueNav.style.display = 'inline-block';
            }
            if(adminNav && roles.includes('ROLE_ADMIN')) {
                adminNav.style.display = 'inline-block';
            }
        } catch(e) {}
    }
});


