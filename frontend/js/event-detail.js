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
        
        // Hiển thị mô tả
        const descEl = document.getElementById('eventDescription');
        if (ev.description && ev.description.trim() !== "") {
            descEl.innerText = ev.description;
        } else {
            descEl.innerHTML = `
                <p>Vượt qua mọi giới hạn của không gian và thời gian, sân khấu Live mang đến những màn trình diễn bùng nổ cùng công nghệ thiết kế ánh sáng tân tiến nhất. Chúng tôi kiến tạo nên một vũ trụ trải nghiệm đa giác quan, nơi bạn có thể chạm vào âm ba và nhìn thấy từng nhịp đập của cảm xúc.</p>
                <p style="margin-top:20px;">Hãy cùng hàng ngàn khán giả khác hòa mình vào không khí cuồng nhiệt này và lưu giữ những kí ức phi thường. Hệ thống vé điện tử của chúng tôi đảm bảo chỗ ngồi có hạn luôn được bảo mật tuyệt vời. Chốt vé ngay để giành lấy tấm vé thông hành của bạn nhé!</p>
            `;
        }
        
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
        
        // Xử lý Lịch
        if (ev.schedules && ev.schedules.length > 0) {
            initCalendar(ev.schedules, new Date(ev.startDate));
        } else {
            document.querySelector('.calendar-card').style.display = 'none';
        }
        
    } catch(e) {
        console.error(e);
        document.getElementById('loading').innerHTML = "<span style='color:red'>Lỗi tải dữ liệu. API Backend cổng 8081 bị mất kết nối!</span>";
    }
});

function initCalendar(schedules, baseDate) {
    const tabsContainer = document.getElementById('monthTabs');
    const startMonth = baseDate.getMonth();
    const startYear = baseDate.getFullYear();
    
    // Tạo 5 tab tháng
    for (let i = 0; i < 5; i++) {
        const d = new Date(startYear, startMonth + i, 1);
        const m = d.getMonth() + 1;
        const y = d.getFullYear();
        
        const count = schedules.filter(s => {
            const sd = new Date(s.startTime);
            return sd.getMonth() === d.getMonth() && sd.getFullYear() === d.getFullYear();
        }).length;

        const tab = document.createElement('div');
        tab.className = `month-tab ${i === 0 ? 'active' : ''}`;
        tab.innerHTML = `Th ${m} <span>${count} suất diễn</span>`;
        tab.onclick = () => {
            document.querySelectorAll('.month-tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            renderCalendar(d.getMonth(), d.getFullYear(), schedules);
        };
        tabsContainer.appendChild(tab);
    }
    
    // Render tháng đầu tiên
    renderCalendar(startMonth, startYear, schedules);
}

function renderCalendar(month, year, schedules) {
    const container = document.getElementById('calendarDays');
    const title = document.getElementById('currentMonthYear');
    container.innerHTML = '';
    
    const monthNames = ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"];
    title.innerText = `${monthNames[month]}, ${year}`;

    const firstDay = new Date(year, month, 1).getDay(); // 0: CN, 1: T2...
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    
    // Chỉnh sửa firstDay để Thứ 2 là cột đầu tiên (0 -> 6, 1 -> 0, 2 -> 1...)
    let startingPos = firstDay === 0 ? 6 : firstDay - 1;

    // Các ô trống đầu tháng
    for (let i = 0; i < startingPos; i++) {
        const empty = document.createElement('div');
        empty.className = 'day-cell';
        container.appendChild(empty);
    }

    // Các ngày trong tháng
    for (let day = 1; day <= daysInMonth; day++) {
        const cell = document.createElement('div');
        cell.className = 'day-cell current-month';
        cell.innerText = day < 10 ? '0' + day : day;
        
        // Kiểm tra xem ngày này có suất diễn không
        const hasEvent = schedules.some(s => {
            const sd = new Date(s.startTime);
            return sd.getDate() === day && sd.getMonth() === month && sd.getFullYear() === year;
        });

        if (hasEvent) {
            cell.classList.add('has-event');
        }

        container.appendChild(cell);
    }
}

// Update Header UI based on Auth JWT Token
document.addEventListener('DOMContentLoaded', () => {
    const currentUser = localStorage.getItem('stellar_user');
    const authBtn = document.querySelector('.navbar .btn.btn-outline');
    if(authBtn && currentUser) {
        authBtn.innerHTML = `<i class="fa fa-user-astronaut"></i> ${currentUser}`;
        authBtn.href = "profile.html";
        authBtn.className = 'btn btn-account';
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
            const myTicketsNav = document.getElementById('myTicketsNav');

            if(myTicketsNav) myTicketsNav.style.display = 'inline-flex';

            if(createEventNav && (roles.includes('ROLE_ORGANIZER') || roles.includes('ROLE_ADMIN'))) {
                createEventNav.style.display = 'inline-flex';
            }
            if(revenueNav && (roles.includes('ROLE_ORGANIZER') || roles.includes('ROLE_ADMIN'))) {
                revenueNav.style.display = 'inline-flex';
            }
            if(adminNav && roles.includes('ROLE_ADMIN')) {
                adminNav.style.display = 'inline-flex';
            }
        } catch(e) {}
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
