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
async function fetchEventsFromBackend() {
    try {
        const response = await fetch('http://localhost:8081/api/events');
        if (!response.ok) return;
        
        const events = await response.json();
        if (events && events.length > 0) {
            const grid = document.getElementById('eventGrid');
            grid.innerHTML = ''; // Clear mockup hardcoded data
            
            events.forEach((ev, idx) => {
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
                grid.appendChild(card);
            });
            
            // Re-attach cursor hover events to newly generated buttons
            attachCursorEvents(document.querySelectorAll('.event-card, .buy-btn'));
            initFiltering(); // Re-initialize filtering
        }
    } catch (e) {
        console.log('Backend chưa được khởi động (CORS / Connection Refused). Đang sử dụng dữ liệu tĩnh.');
    }
}

// Bắt đầu khởi tạo dữ liệu khi Load trang xong
document.addEventListener('DOMContentLoaded', fetchEventsFromBackend);

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
            if(createEventNav && (roles.includes('ROLE_ORGANIZER') || roles.includes('ROLE_ADMIN'))) {
                createEventNav.style.display = 'inline-block';
            }
        } catch(e) {}
    }
});


