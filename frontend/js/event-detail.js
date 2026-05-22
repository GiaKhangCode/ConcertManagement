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
        window.eventSchedules = ev.schedules; // Lưu lại để dùng cho handleBooking
        
        // Map DOM elements
        document.getElementById('eventTitle').innerText = ev.title;
        document.getElementById('eventDate').innerText = new Date(ev.startDate).toLocaleString('vi-VN');
        document.getElementById('eventLocation').innerText = ev.location;
        document.getElementById('eventStatus').innerText = ev.status;
        window.currentEventStatus = ev.status; // Lưu trạng thái sự kiện
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
        // Hiển thị Nhà tổ chức
        if (ev.organizer) {
            document.getElementById('organizerSection').style.display = 'block';
            document.getElementById('organizerName').innerText = ev.organizer.name;
            document.getElementById('organizerEmail').innerText = ev.organizer.email;
        }

        
        // Hiển thị Nhà tài trợ
        const sponsorsSect = document.getElementById('sponsorsSection');
        const sponsorsList = document.getElementById('sponsorsList');
        if (ev.sponsors && ev.sponsors.length > 0) {
            sponsorsSect.style.display = 'block';
            sponsorsList.innerHTML = ev.sponsors.map(s => `
                <div style="background: rgba(80, 250, 123, 0.05); border: 1px solid rgba(80, 250, 123, 0.2); padding: 15px; border-radius: 12px; text-align: center; transition: all 0.3s;" onmouseover="this.style.borderColor='#50fa7b'; this.style.transform='translateY(-5px)'" onmouseout="this.style.borderColor='rgba(80, 250, 123, 0.2)'; this.style.transform='none'">
                    <div style="font-size: 0.75rem; color: #50fa7b; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 5px;">Hạng ${s.rank}</div>
                    <div style="font-weight: bold; color: #fff; font-size: 1.1rem;">${s.name}</div>
                </div>
            `).join('');
        }
        
        // Hiển thị chính sách hoàn tiền
        const policyCont = document.getElementById('refundPolicyContainer');
        if (ev.refundPolicy && ev.refundPolicy.rules && ev.refundPolicy.rules.length > 0) {
            policyCont.style.display = 'block';
            document.getElementById('refundPolicyName').innerText = ev.refundPolicy.name || 'Tiêu chuẩn';
            
            // Sắp xếp rules theo số giờ trước sự kiện giảm dần
            const sortedRules = ev.refundPolicy.rules.sort((a, b) => b.hoursBefore - a.hoursBefore);
            
            const rulesHtml = sortedRules.map(r => `
                <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.05); padding: 12px 20px; border-radius: 10px; border-left: 3px solid #50fa7b;">
                    <span style="color: #fff;">Hủy trước <strong>${r.hoursBefore} giờ</strong></span>
                    <span style="color: #50fa7b; font-weight: bold; font-family: 'Unbounded', sans-serif;">Hoàn ${r.percentage}%</span>
                </div>
            `).join('');
            
            document.getElementById('refundRulesList').innerHTML = rulesHtml;
        } else {
            policyCont.style.display = 'block';
            policyCont.innerHTML = `
                <h3 style="font-family: 'Unbounded', sans-serif; color: #ff5555; margin-bottom: 10px; font-size: 1.3rem;">
                    <i class="fa fa-ban"></i> KHÔNG HỖ TRỢ HOÀN VÉ
                </h3>
                <div style="color: #a0a5b5; font-size: 1rem;">Sự kiện này không áp dụng chính sách hoàn tiền. Vui lòng cân nhắc kỹ trước khi mua vé.</div>
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
    const authBtn = document.getElementById('authBtn');
    const accountDropdown = document.getElementById('accountDropdown');
    const userNameLabel = document.getElementById('userNameDropdown');

    if (currentUser && accountDropdown) {
        // Logged In state
        if (authBtn) authBtn.style.display = 'none';
        accountDropdown.style.display = 'inline-block';
        if (userNameLabel) {
            try {
                const userData = JSON.parse(currentUser);
                const name = userData.hoTen || userData.name || currentUser;
                userNameLabel.textContent = name;
            } catch(e) {
                userNameLabel.textContent = currentUser;
            }
        }
    } else if (authBtn) {
        // Logged Out state
        authBtn.style.display = 'inline-flex';
        authBtn.href = "auth.html";
        if (accountDropdown) accountDropdown.style.display = 'none';
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

function logout() {
    if(confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
        localStorage.removeItem('stellar_token');
        localStorage.removeItem('stellar_user');
        localStorage.removeItem('stellar_roles');
        window.location.href = 'index.html';
    }
}

// Logic mua vé chuyển trang
let pendingBookingInfo = null;

window.handleBooking = function(eventId, ticketTierId, tierName) {
    if (window.currentEventStatus === 'Đã kết thúc') {
        showMascotMessage("Sự kiện này đã kết thúc, bạn không thể mua vé nữa nha! 😢", true);
        return;
    }

    const token = localStorage.getItem('stellar_token');
    if(!token) {
        alert("Bạn cần đăng nhập để có thể đặt vé sự kiện này!");
        window.location.href = "auth.html";
        return;
    }
    
    // Lưu lại thông tin đặt vé để dùng sau khi qua captcha
    pendingBookingInfo = { eventId, ticketTierId, tierName };
    
    // Hiển thị modal captcha chống bot
    document.getElementById('captchaModal').style.display = 'flex';
    initCaptcha();
}

function executeBookingNavigation() {
    if(!pendingBookingInfo) return;
    const { eventId, ticketTierId, tierName } = pendingBookingInfo;
    const schedules = window.eventSchedules || [];
    const maLichDien = (schedules.length > 0 && schedules[0].id) ? schedules[0].id : eventId;

    window.location.href = `booking.html?eventId=${eventId}&tierId=${ticketTierId}&tierName=${encodeURIComponent(tierName)}&scheduleId=${maLichDien}`;
}

// --- Logic Captcha Chống Bot ---
let captchaX = 0;
let captchaY = 0;
let isDraggingCaptcha = false;
let startClientX = 0;

function initCaptcha() {
    const bgCanvas = document.getElementById('captchaBg');
    const pieceCanvas = document.getElementById('captchaPiece');
    const bgCtx = bgCanvas.getContext('2d');
    const pieceCtx = pieceCanvas.getContext('2d');
    
    // Đặt lại slider
    const sliderBtn = document.getElementById('sliderBtn');
    const track = document.getElementById('sliderTrack');
    sliderBtn.style.left = '0px';
    track.style.width = '0px';
    sliderBtn.innerHTML = '<i class="fa fa-arrow-right"></i>';
    sliderBtn.style.background = '#2f80ed';
    
    const img = new Image();
    img.src = document.getElementById('eventPoster').src || 'assets/test.png';
    img.onload = () => {
        bgCtx.clearRect(0, 0, 310, 180);
        pieceCtx.clearRect(0, 0, 310, 180);
        
        // Vẽ background
        bgCtx.drawImage(img, 0, 0, 310, 180);
        
        // Random vị trí lỗ hổng
        captchaX = Math.floor(Math.random() * 140) + 100;
        captchaY = Math.floor(Math.random() * 90) + 30;
        const r = 8;
        const size = 45;
        
        // Vẽ mảnh ghép cắt ra
        pieceCtx.save();
        drawPuzzlePiece(pieceCtx, captchaX, captchaY, size, r);
        pieceCtx.clip();
        pieceCtx.drawImage(img, 0, 0, 310, 180);
        pieceCtx.restore();
        
        // Vẽ viền mảnh ghép
        pieceCtx.save();
        drawPuzzlePiece(pieceCtx, captchaX, captchaY, size, r);
        pieceCtx.lineWidth = 2;
        pieceCtx.strokeStyle = 'rgba(255, 255, 255, 0.9)';
        pieceCtx.stroke();
        pieceCtx.restore();
        
        // Vẽ lỗ trống trên nền
        bgCtx.save();
        drawPuzzlePiece(bgCtx, captchaX, captchaY, size, r);
        bgCtx.fillStyle = 'rgba(0, 0, 0, 0.6)';
        bgCtx.fill();
        bgCtx.lineWidth = 2;
        bgCtx.strokeStyle = 'rgba(255, 255, 255, 0.5)';
        bgCtx.stroke();
        bgCtx.restore();
        
        // Dịch chuyển canvas của mảnh ghép sang trái cùng
        pieceCanvas.style.transform = `translateX(-${captchaX}px)`;
    };
}

function drawPuzzlePiece(ctx, x, y, size, r) {
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x + size / 2 - r, y);
    ctx.arc(x + size / 2, y, r, Math.PI, 0, false);
    ctx.lineTo(x + size, y);
    ctx.lineTo(x + size, y + size / 2 - r);
    ctx.arc(x + size, y + size / 2, r, 1.5 * Math.PI, 0.5 * Math.PI, false);
    ctx.lineTo(x + size, y + size);
    ctx.lineTo(x, y + size);
    ctx.lineTo(x, y);
    ctx.closePath();
}

function closeCaptcha() {
    document.getElementById('captchaModal').style.display = 'none';
}

// Sự kiện Slider Captcha
document.addEventListener('DOMContentLoaded', () => {
    const sliderBtn = document.getElementById('sliderBtn');
    if (sliderBtn) {
        sliderBtn.addEventListener('mousedown', (e) => {
            isDraggingCaptcha = true;
            startClientX = e.clientX;
            sliderBtn.style.cursor = 'grabbing';
        });
        
        document.addEventListener('mousemove', (e) => {
            if (!isDraggingCaptcha) return;
            let moveX = e.clientX - startClientX;
            if (moveX < 0) moveX = 0;
            if (moveX > 260) moveX = 260; // 310 - 50 (chiều rộng nút)
            
            sliderBtn.style.left = moveX + 'px';
            document.getElementById('sliderTrack').style.width = moveX + 'px';
            
            document.getElementById('captchaPiece').style.transform = `translateX(${moveX - captchaX}px)`;
        });
        
        document.addEventListener('mouseup', (e) => {
            if (!isDraggingCaptcha) return;
            isDraggingCaptcha = false;
            sliderBtn.style.cursor = 'grab';
            
            const currentLeft = parseInt(sliderBtn.style.left || '0');
            if (Math.abs(currentLeft - captchaX) < 10) {
                // Thành công
                sliderBtn.innerHTML = '<i class="fa fa-check"></i>';
                sliderBtn.style.background = '#50fa7b'; // Xanh lá
                setTimeout(() => {
                    closeCaptcha();
                    executeBookingNavigation(); // Tiến hành chuyển trang
                }, 500);
            } else {
                // Thất bại
                sliderBtn.style.background = '#ff5555'; // Đỏ
                sliderBtn.innerHTML = '<i class="fa fa-times"></i>';
                setTimeout(() => {
                    initCaptcha(); // Reset lại captcha nếu sai
                }, 500);
            }
        });
    }
});

function showMascotMessage(msg, isError = false) {
    const tooltip = document.getElementById('mascotTooltip');
    const mascot = document.getElementById('mascotCompanion');
    if (tooltip) {
        tooltip.innerText = msg;
        tooltip.classList.add('show');
        if (isError) {
            tooltip.classList.add('error');
            if(mascot) mascot.style.animation = 'shake 0.5s ease';
        } else {
            tooltip.classList.remove('error');
            if(mascot) mascot.style.animation = 'floatMascot 4s ease-in-out infinite';
        }
        
        setTimeout(() => {
            tooltip.classList.remove('show');
            if(mascot && isError) mascot.style.animation = 'floatMascot 4s ease-in-out infinite';
        }, 5000);
    }
}