// profile.js - Chuyên xử lý dữ liệu cá nhân, vé và tài chính
let selectedTicketId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('stellar_token');
    if(!token) {
        alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        window.location.href = 'auth.html';
        return;
    }

    initProfile();
});

async function initProfile() {
    const token = localStorage.getItem('stellar_token');
    console.log("Khởi tạo Profile với token:", token ? "Đã có" : "Chưa có");
    try {
        // 1. Fetch Full Profile
        console.log("Đang gọi API profile...");
        const profRes = await fetch('http://localhost:8081/api/user/profile', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        console.log("Kết quả API profile:", profRes.status);
        if(profRes.ok) {
            const profile = await profRes.json();
            console.log("Dữ liệu profile nhận được:", profile);
            document.getElementById('userNameLabel').innerText = profile.fullName || profile.username || "Người dùng";
            document.getElementById('userEmailLabel').innerText = profile.email || 'Thành viên Ve\'ryGood';
            document.getElementById('walletBalance').innerText = (profile.walletBalance || 0).toLocaleString('vi-VN') + " VNĐ";
        } else {
            console.error("Lỗi API Profile:", await profRes.text());
        }

        // 2. Fetch Tickets
        loadTickets();
        // 3. Fetch History
        loadHistory();

    } catch(err) {
        console.error("Lỗi khi tải thông tin hồ sơ:", err);
    }
}

async function loadTickets() {
    const token = localStorage.getItem('stellar_token');
    const ticketListCont = document.getElementById('ticketListCont');
    try {
        const res = await fetch('http://localhost:8081/api/user/tickets', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if(res.ok) {
            const tickets = await res.json();
            console.log("DEBUG: Dữ liệu vé từ server:", tickets);
            document.getElementById('ticketCount').innerText = tickets.length + " vé";
            if(tickets.length === 0) {
                ticketListCont.innerHTML = '<p style="color:#a0a5b5; text-align:center;">Bạn chưa sở hữu vé nào.</p>';
                return;
            }
            
            ticketListCont.innerHTML = tickets.map(tk => {
                const d = new Date(tk.bookingTime).toLocaleString('vi-VN');
                return `
                    <div class="ticket-item">
                        <div style="flex: 1;">
                            <h4>SỰ KIỆN: ${tk.eventName}</h4>
                            <div class="ticket-meta">
                                <span><i class="fa fa-qrcode"></i> TIC-${tk.ticketId} (Đơn: ${tk.transactionId})</span>
                                <span><i class="fa fa-ticket"></i> ${tk.ticketCount} vé (${tk.tierName})</span>
                                <span><i class="fa fa-clock"></i> ${d}</span>
                            </div>
                        </div>
                        <div style="display: flex; gap: 10px; align-items: center;">
                            <button class="btn btn-outline small" style="padding: 5px 12px; font-size: 0.75rem;" onclick="openResaleModal(${tk.ticketId})">BÁN LẠI</button>
                            <button class="btn btn-outline small" style="padding: 5px 12px; font-size: 0.75rem; border-color: #ff5555; color: #ff5555;" onclick="openRefundModal(${tk.ticketId})">HOÀN VÉ</button>
                            <div class="ticket-status">Khả dụng</div>
                        </div>
                    </div>
                `;
            }).join('');
        }
    } catch(e) { console.error("Lỗi khi tải danh sách vé:", e); }
}

async function loadHistory() {
    const token = localStorage.getItem('stellar_token');
    const container = document.getElementById('historyListCont');
    try {
        const res = await fetch('http://localhost:8081/api/finance/wallet/history', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if(res.ok) {
            const history = await res.json();
            if(history.length === 0) {
                container.innerHTML = '<p style="color:#a0a5b5; text-align:center; padding: 20px;">Chưa có giao dịch nào.</p>';
                return;
            }
            container.innerHTML = history.map(h => {
                const isPlus = h.loaiBienDong === 'Tăng';
                const color = isPlus ? '#50fa7b' : '#ff5555';
                const icon = isPlus ? 'fa-arrow-down' : 'fa-arrow-up';
                const bg = isPlus ? 'rgba(80, 250, 123, 0.1)' : 'rgba(255, 85, 85, 0.1)';
                
                return `
                    <div class="history-item">
                        <div class="history-icon" style="background: ${bg}; color: ${color};">
                            <i class="fa ${icon}"></i>
                        </div>
                        <div>
                            <div style="font-weight: 600;">${h.noiDung || h.loaiGiaoDich}</div>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">${new Date(h.thoiGian).toLocaleString('vi-VN')}</div>
                        </div>
                        <div class="history-amount" style="color: ${color};">
                            ${isPlus ? '+' : '-'}${h.soTien.toLocaleString('vi-VN')} đ
                        </div>
                    </div>
                `;
            }).join('');
        }
    } catch(e) { console.error("Lỗi khi tải lịch sử:", e); }
}

// Logic nạp tiền
document.getElementById('topUpBtn')?.addEventListener('click', async () => {
    const amount = prompt("Nhập số tiền nạp (VNĐ):", "500000");
    if(!amount) return;
    
    const token = localStorage.getItem('stellar_token');
    try {
        const res = await fetch('http://localhost:8081/api/finance/wallet/deposit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ amount: parseFloat(amount) })
        });
        if(res.ok) {
            alert("Nạp tiền thành công!");
            initProfile();
        }
    } catch(e) { alert("Lỗi kết nối"); }
});

// Tab Switching
function switchTab(tab) {
    document.getElementById('tab-tickets').classList.toggle('active', tab === 'tickets');
    document.getElementById('tab-history').classList.toggle('active', tab === 'history');
    document.getElementById('tickets-tab-content').style.display = tab === 'tickets' ? 'block' : 'none';
    document.getElementById('history-tab-content').style.display = tab === 'history' ? 'block' : 'none';
}

// Modal Helpers
function openModal(id) { document.getElementById(id).style.display = 'block'; }
function closeModal(id) { document.getElementById(id).style.display = 'none'; }

function openResaleModal(ticketId) {
    selectedTicketId = ticketId;
    openModal('resaleModal');
}

function openRefundModal(ticketId) {
    selectedTicketId = ticketId;
    openModal('refundModal');
}

document.getElementById('confirmResaleBtn')?.addEventListener('click', async () => {
    const price = document.getElementById('resalePrice').value;
    if(!price) return alert("Vui lòng nhập giá bán");
    if(!selectedTicketId || selectedTicketId === 'undefined') {
        return alert("Lỗi: Không xác định được mã vé. Vui lòng tải lại trang.");
    }
    
    const token = localStorage.getItem('stellar_token');
    try {
        const res = await fetch(`http://localhost:8081/api/finance/tickets/${selectedTicketId}/resale`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ price: parseFloat(price) })
        });
        if(res.ok) {
            alert("Niêm yết bán lại thành công!");
            closeModal('resaleModal');
            loadTickets();
        } else {
            const data = await res.json();
            alert("Lỗi: " + data.message);
        }
    } catch(e) { alert("Lỗi kết nối"); }
});

document.getElementById('confirmRefundBtn')?.addEventListener('click', async () => {
    const reason = document.getElementById('refundReason').value;
    if(!selectedTicketId || selectedTicketId === 'undefined') {
        return alert("Lỗi: Không xác định được mã vé. Vui lòng tải lại trang.");
    }
    const token = localStorage.getItem('stellar_token');
    try {
        const res = await fetch(`http://localhost:8081/api/finance/tickets/${selectedTicketId}/refund`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ reason: reason })
        });
        if(res.ok) {
            alert("Yêu cầu hoàn tiền đã được gửi.");
            closeModal('refundModal');
            loadTickets();
        } else {
            const data = await res.json();
            alert("Lỗi: " + data.message);
        }
    } catch(e) { alert("Lỗi kết nối"); }
});
