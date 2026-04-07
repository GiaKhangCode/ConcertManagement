// Custom cursor logic
const cursor = document.querySelector('.custom-cursor');
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
attachCursorEvents(document.querySelectorAll('a, button, .mascot-companion'));

document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('id');
    
    if(!eventId) {
        document.getElementById('loading').innerHTML = "<span style='color:red'>ID Sự Kiện không hợp lệ! Vui lòng quay lại trang chủ.</span>";
        return;
    }

    try {
        const response = await fetch('http://localhost:8081/api/events/' + eventId);
        if (!response.ok) throw new Error("API Fail");
        const ev = await response.json();
        
        document.getElementById('loading').style.display = 'none';
        document.getElementById('appContainer').style.display = 'block';
        
        // Map DOM elements
        document.getElementById('eventTitle').innerText = ev.title;
        document.getElementById('eventDate').innerText = new Date(ev.startDate).toLocaleString('vi-VN');
        document.getElementById('eventLocation').innerText = ev.location;
        document.getElementById('eventStatus').innerText = ev.status;
        document.getElementById('eventPoster').src = ev.image;
        
        const tiersContainer = document.getElementById('ticketTiersContainer');
        if(ev.ticketTiers && ev.ticketTiers.length > 0) {
            ev.ticketTiers.forEach(t => {
                const row = document.createElement('div');
                row.className = 'tier-row';
                row.innerHTML = `
                    <div class="tier-header">
                        <div>
                            <div class="tier-name">${t.name}</div>
                            <div class="tier-price">${t.price.toLocaleString('vi-VN')} VNĐ</div>
                        </div>
                        <button onclick="handleBooking(${eventId}, ${t.id}, '${t.name}')" class="btn btn-primary glow-btn" onmouseenter="attachCursorEvents([this])" style="padding: 12px 25px; border-radius:10px; font-weight:bold; border:none; cursor:pointer;">
                            <i class="fa fa-ticket" style="margin-right: 8px;"></i> MUA
                        </button>
                    </div>
                `;
                tiersContainer.appendChild(row);
            });
        } else {
            tiersContainer.innerHTML = '<p style="color:#a0a5b5; text-align:center;">Sự kiện này chưa cập nhật phân khúc vé.</p>';
        }
        
    } catch(e) {
        document.getElementById('loading').innerHTML = "<span style='color:red'>Lỗi tải dữ liệu. API Backend cổng 8081 bị mất kết nối!</span>";
    }
});

// Update Header UI based on Auth JWT Token
document.addEventListener('DOMContentLoaded', () => {
    const currentUser = localStorage.getItem('stellar_user');
    const authBtn = document.querySelector('.navbar .btn.btn-outline');
    if(authBtn && currentUser) {
        authBtn.innerHTML = `<i class="fa fa-user-astronaut" style="margin-right: 8px;"></i> ${currentUser}`;
        authBtn.href = "profile.html";
        authBtn.classList.remove('btn-outline');
        authBtn.style.background = 'linear-gradient(90deg, var(--primary-purple), var(--primary-cyan))';
        authBtn.style.color = '#fff';
        authBtn.style.border = 'none';
        
        attachCursorEvents([authBtn]);
    } else if(authBtn) {
        authBtn.href = "auth.html";
    }
});

// Logic mua vé chuyển trang
window.handleBooking = function(eventId, ticketTierId, tierName) {
    const token = localStorage.getItem('stellar_token');
    if(!token) {
        alert("Bạn cần đăng nhập để có thể đặt vé sự kiện này!");
        window.location.href = "auth.html";
        return;
    }
    
    // Thu thập tham số đơn giá từ DOM hiện tại nếu cần hoặc lấy theo tierId.
    // Tạm thời truyền qua URL để lấy dữ liệu.
    window.location.href = `booking.html?eventId=${eventId}&tierId=${ticketTierId}&tierName=${encodeURIComponent(tierName)}`;
}
