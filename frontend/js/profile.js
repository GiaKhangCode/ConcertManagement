// profile.js - Chuyên xử lý dữ liệu cá nhân, vé và tài chính
let selectedTicketId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('stellar_token');
    if (!token) {
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
        if (profRes.ok) {
            const profile = await profRes.json();
            console.log("Dữ liệu profile nhận được:", profile);
            document.getElementById('userNameLabel').innerText = profile.fullName || profile.username || "Người dùng";
            document.getElementById('userEmailLabel').innerText = profile.email || 'Thành viên Ve\'ryGood';
            document.getElementById('walletBalance').innerText = (profile.walletBalance || 0).toLocaleString('vi-VN') + " VNĐ";

            // Xử lý vai trò và tab nhà tổ chức
            if (profile.roles && profile.roles.includes('ROLE_ORGANIZER')) {
                document.getElementById('tab-organizer').style.display = 'block';
                document.getElementById('userRoleDis').innerText = "Nhà tổ chức";

                // Đổ dữ liệu vào form nhà tổ chức
                document.getElementById('orgName').value = profile.organizationName || '';
                document.getElementById('orgTaxCode').value = profile.taxCode || '';
                document.getElementById('orgBankInfo').value = profile.bankInfo || '';
                document.getElementById('orgEmail').value = profile.supportEmail || '';
            }
        } else {
            console.error("Lỗi API Profile:", await profRes.text());
        }

        // 2. Fetch Tickets
        loadTickets();
        // 3. Fetch History
        loadHistory();

    } catch (err) {
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
        if (res.ok) {
            const orders = await res.json();
            console.log("DEBUG: Dữ liệu đơn mua từ server:", orders);
            document.getElementById('ticketCount').innerText = orders.length + " đơn mua";
            if (orders.length === 0) {
                ticketListCont.innerHTML = '<p style="color:#a0a5b5; text-align:center;">Bạn chưa có đơn mua nào.</p>';
                return;
            }

            ticketListCont.innerHTML = orders.map(order => {
                const d = new Date(order.bookingTime).toLocaleString('vi-VN');
                const ticketsHtml = order.tickets.map(tk => {
                    const isRefundedOrCanceled = tk.status === 'Đã hoàn vé' || tk.status === 'Đã hủy';
                    const actionsHtml = isRefundedOrCanceled
                        ? `<span style="color: #ff5555; font-weight: bold; font-size: 0.85rem;"><i class="fa fa-undo"></i> ${tk.status}</span>`
                        : `<button class="btn btn-outline small" style="padding: 4px 10px; font-size: 0.7rem;" onclick="showQRCode(${tk.ticketId})"><i class="fa fa-qrcode"></i> Mã QR</button>
                           <button class="btn btn-outline small" style="padding: 4px 10px; font-size: 0.7rem;" onclick="openResaleModal(${tk.ticketId})">Bán lại</button>
                           <button class="btn btn-outline small" style="padding: 4px 10px; font-size: 0.7rem; border-color: #ff5555; color: #ff5555;" onclick="openRefundModal(${tk.ticketId}, ${order.eventId})">Hoàn vé</button>`;

                    return `
                    <div class="ticket-sub-item" style="display: flex; justify-content: space-between; align-items: center; background: rgba(0,0,0,0.2); padding: 10px 15px; margin-top: 10px; border-radius: 8px; border-left: 3px solid ${isRefundedOrCanceled ? '#ff5555' : '#00f3ff'}; opacity: ${isRefundedOrCanceled ? '0.7' : '1'};">
                        <div>
                            <strong style="color: ${isRefundedOrCanceled ? '#ff5555' : '#00f3ff'};">Vé #${tk.ticketId}</strong> - Hạng: ${tk.tierName}
                            <div style="font-size: 0.8rem; color: #a0a5b5; margin-top: 5px;"><i class="fa fa-map-marker-alt"></i> Khu vực: ${tk.zoneName} | <i class="fa fa-chair"></i> Ghế: ${tk.seatInfo}</div>
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            ${actionsHtml}
                        </div>
                    </div>
                    `;
                }).join('');

                return `
                    <div class="ticket-item" style="flex-direction: column; align-items: stretch;">
                        <div style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;" onclick="toggleOrderDetails(${order.transactionId})">
                            <div style="flex: 1;">
                                <h4>SỰ KIỆN: ${order.eventName}</h4>
                                <div class="ticket-meta">
                                    <span><i class="fa fa-receipt"></i> Đơn mua #${order.transactionId}</span>
                                    <span><i class="fa fa-ticket"></i> ${order.tickets.length} vé</span>
                                    <span><i class="fa fa-clock"></i> ${d}</span>
                                </div>
                            </div>
                            <div style="text-align: right;">
                                <div style="color: #50fa7b; font-family: 'Space Mono', monospace; font-size: 1.1rem; font-weight: bold; margin-bottom: 5px;">${order.totalPrice.toLocaleString('vi-VN')} đ</div>
                                <div style="font-size: 0.8rem; color: var(--accent-secondary);"><i class="fa fa-chevron-down" id="icon-order-${order.transactionId}"></i> Xem chi tiết vé</div>
                            </div>
                        </div>
                        <div id="order-details-${order.transactionId}" style="display: none; margin-top: 15px; border-top: 1px dashed rgba(255,255,255,0.1); padding-top: 5px;">
                            ${ticketsHtml}
                        </div>
                    </div>
                `;
            }).join('');
        }
    } catch (e) { console.error("Lỗi khi tải danh sách vé:", e); }
}

window.toggleOrderDetails = function (orderId) {
    const el = document.getElementById('order-details-' + orderId);
    const icon = document.getElementById('icon-order-' + orderId);
    if (el.style.display === 'none') {
        el.style.display = 'block';
        icon.classList.replace('fa-chevron-down', 'fa-chevron-up');
    } else {
        el.style.display = 'none';
        icon.classList.replace('fa-chevron-up', 'fa-chevron-down');
    }
}

async function loadHistory() {
    const token = localStorage.getItem('stellar_token');
    const container = document.getElementById('historyListCont');
    try {
        const res = await fetch('http://localhost:8081/api/finance/wallet/history', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok) {
            const history = await res.json();
            if (history.length === 0) {
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
    } catch (e) { console.error("Lỗi khi tải lịch sử:", e); }
}

// Logic nạp tiền
document.getElementById('topUpBtn')?.addEventListener('click', async () => {
    const amount = prompt("Nhập số tiền nạp (VNĐ):", "500000");
    if (!amount) return;

    const token = localStorage.getItem('stellar_token');
    try {
        const res = await fetch('http://localhost:8081/api/finance/wallet/deposit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ amount: parseFloat(amount) })
        });
        if (res.ok) {
            alert("Nạp tiền thành công!");
            initProfile();
        }
    } catch (e) { alert("Lỗi kết nối"); }
});

// Tab Switching
function switchTab(tab) {
    document.getElementById('tab-tickets').classList.toggle('active', tab === 'tickets');
    document.getElementById('tab-history').classList.toggle('active', tab === 'history');
    document.getElementById('tab-organizer').classList.toggle('active', tab === 'organizer');

    document.getElementById('tickets-tab-content').style.display = tab === 'tickets' ? 'block' : 'none';
    document.getElementById('history-tab-content').style.display = tab === 'history' ? 'block' : 'none';
    document.getElementById('organizer-tab-content').style.display = tab === 'organizer' ? 'block' : 'none';
}

async function saveOrganizerInfo() {
    const token = localStorage.getItem('stellar_token');
    const data = {
        organizationName: document.getElementById('orgName').value,
        taxCode: document.getElementById('orgTaxCode').value,
        bankInfo: document.getElementById('orgBankInfo').value,
        supportEmail: document.getElementById('orgEmail').value
    };

    if (!data.organizationName?.trim() || !data.taxCode?.trim() || !data.bankInfo?.trim() || !data.supportEmail?.trim()) {
        alert("Vui lòng điền đầy đủ tất cả các thông tin nhà tổ chức!");
        return;
    }

    try {
        const res = await fetch('http://localhost:8081/api/user/organizer-setup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            alert("Cập nhật thông tin nhà tổ chức thành công!");
            initProfile(); // Tải lại để cập nhật UI
        } else {
            const err = await res.json();
            alert("Lỗi: " + (err.message || "Không thể cập nhật thông tin"));
        }
    } catch (e) {
        console.error("Lỗi khi lưu thông tin nhà tổ chức:", e);
        alert("Lỗi kết nối máy chủ");
    }
}

// Modal Helpers
function openModal(id) { document.getElementById(id).style.display = 'block'; }
function closeModal(id) { document.getElementById(id).style.display = 'none'; }

function openResaleModal(ticketId) {
    selectedTicketId = ticketId;
    openModal('resaleModal');
}

function openRefundModal(ticketId, eventId) {
    selectedTicketId = ticketId;

    // Reset modal
    const refundInfoCont = document.getElementById('refundInfo');
    const formSection = document.getElementById('refundFormSection');
    const disclaimer = document.getElementById('refundDisclaimer');
    const modalActions = document.getElementById('refundModalActions');
    const confirmBtn = document.getElementById('confirmRefundBtn');
    document.getElementById('refundReason').value = '';

    refundInfoCont.innerHTML = '<span style="color:#a0a5b5;"><i class="fa fa-spinner fa-spin" style="margin-right:8px;"></i>Đang tải thông tin chính sách...</span>';
    formSection.style.display = 'none';
    disclaimer.style.display = 'none';
    confirmBtn.style.display = 'none';

    openModal('refundModal');

    fetch('http://localhost:8081/api/events/' + eventId)
        .then(res => res.json())
        .then(ev => {
            if (ev.refundPolicy && ev.refundPolicy.rules && ev.refundPolicy.rules.length > 0) {
                // Co chinh sach: hien day du form
                const sortedRules = ev.refundPolicy.rules.sort((a, b) => b.hoursBefore - a.hoursBefore);
                let html = `<div style="font-size:0.82rem; color:#50fa7b; font-weight:700; margin-bottom:10px; text-transform:uppercase; letter-spacing:0.5px;"><i class="fa fa-shield-halved" style="margin-right:6px;"></i>${ev.refundPolicy.name || 'Ch\u00ednh s\u00e1ch ho\u00e0n ti\u1ec1n'}</div><div style="display:flex; flex-direction:column; gap:8px;">`;
                sortedRules.forEach(r => {
                    html += `<div style="display:flex; justify-content:space-between; align-items:center; background:rgba(255,255,255,0.04); padding:10px 14px; border-radius:8px; border-left:3px solid #50fa7b;"><span style="color:#ccc; font-size:0.85rem;">H\u1ee7y tr\u01b0\u1edbc <strong style="color:#fff;">${r.hoursBefore} gi\u1edd</strong></span><span style="color:#50fa7b; font-weight:700; font-size:0.95rem;">Ho\u00e0n ${r.percentage}%</span></div>`;
                });
                html += '</div>';
                refundInfoCont.innerHTML = html;
                formSection.style.display = 'block';
                disclaimer.style.display = 'flex';
                confirmBtn.style.display = 'inline-flex';
            } else {
                // Khong co chinh sach: chi hien thong bao
                refundInfoCont.innerHTML = `
                    <div style="text-align:center; padding:10px 0;">
                        <div style="font-size:2.5rem; margin-bottom:12px; opacity:0.7;">🚫</div>
                        <div style="color:#ff5555; font-weight:700; font-size:1rem; margin-bottom:8px;">
                            SỰ KIỆN KHÔNG HỖ TRỢ HOÀN VÉ
                        </div>
                        <div style="font-size:0.85rem; color:#a0a5b5; line-height:1.6;">
                            Ban tổ chức không áp dụng chính sách hoàn tiền cho sự kiện này.<br>
                        </div>
                    </div>`;
                formSection.style.display = 'none';
                disclaimer.style.display = 'none';
                confirmBtn.style.display = 'none';
            }
        })
        .catch(e => {
            console.error('Loi tai chinh sach hoan tien', e);
            refundInfoCont.innerHTML = '<span style="color:#ff5555;"><i class="fa fa-circle-xmark" style="margin-right:6px;"></i>Khong the tai chinh sach hoan tien.</span>';
            formSection.style.display = 'none';
            disclaimer.style.display = 'none';
            confirmBtn.style.display = 'none';
        });
}

document.getElementById('confirmResaleBtn')?.addEventListener('click', async () => {
    const price = document.getElementById('resalePrice').value;
    if (!price) return alert("Vui lòng nhập giá bán");
    if (!selectedTicketId || selectedTicketId === 'undefined') {
        return alert("Lỗi: Không xác định được mã vé. Vui lòng tải lại trang.");
    }

    const token = localStorage.getItem('stellar_token');
    try {
        const res = await fetch(`http://localhost:8081/api/finance/tickets/${selectedTicketId}/resale`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ price: parseFloat(price) })
        });
        if (res.ok) {
            alert("Niêm yết bán lại thành công!");
            closeModal('resaleModal');
            loadTickets();
        } else {
            const data = await res.json();
            alert("Lỗi: " + data.message);
        }
    } catch (e) { alert("Lỗi kết nối"); }
});

document.getElementById('confirmRefundBtn')?.addEventListener('click', async () => {
    const reason = document.getElementById('refundReason').value;
    if (!selectedTicketId || selectedTicketId === 'undefined') {
        return alert("Lỗi: Không xác định được mã vé. Vui lòng tải lại trang.");
    }
    const token = localStorage.getItem('stellar_token');
    try {
        const res = await fetch(`http://localhost:8081/api/finance/tickets/${selectedTicketId}/refund`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ reason: reason })
        });
        if (res.ok) {
            alert("Hoàn tiền tự động thành công!");
            closeModal('refundModal');
            loadTickets();
            initProfile();
        } else {
            const data = await res.json();
            alert("Lỗi: " + data.message);
        }
    } catch (e) { alert("Lỗi kết nối"); }
});

function showQRCode(ticketId) {
    const container = document.getElementById('qrcodeContainer');
    const idLabel = document.getElementById('qrTicketId');

    if (!container || !idLabel) return;

    // Clear previous QR
    container.innerHTML = "";
    idLabel.innerText = "TIC-" + ticketId;

    // Generate new QR
    new QRCode(container, {
        text: ticketId.toString(),
        width: 200,
        height: 200,
        colorDark: "#000000",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.H
    });

    openModal('qrModal');
}
