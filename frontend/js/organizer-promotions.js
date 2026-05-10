document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('stellar_token');
    if (!token) {
        window.location.href = 'auth.html';
        return;
    }
    loadEvents();
    loadPromotions();
    
    // Khởi tạo Flatpickr
    initFlatpickr('.dt-picker');
});

function initFlatpickr(selector) {
    return flatpickr(selector, { 
        enableTime: true, 
        altInput: true, 
        altFormat: "d/m/Y H:i", 
        dateFormat: "Y-m-d\\TH:i", 
        time_24hr: true, 
        locale: { firstDayOfWeek: 1 } 
    });
}

async function loadEvents() {
    const token = localStorage.getItem('stellar_token');
    try {
        const res = await fetch('http://localhost:8081/api/organizer/revenue', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const data = await res.json();
        const select = document.getElementById('eventSelect');
        select.innerHTML = '';
        if (data.chiTietSuKien) {
            data.chiTietSuKien.forEach(ev => {
                const opt = document.createElement('option');
                opt.value = ev.maSuKien;
                opt.textContent = ev.tenSuKien;
                select.appendChild(opt);
            });
        }
    } catch (e) {
        console.error("Lỗi tải danh sách sự kiện", e);
    }
}

async function loadPromotions() {
    const token = localStorage.getItem('stellar_token');
    const listContainer = document.getElementById('promotionList');
    listContainer.innerHTML = '<div class="empty-state">Đang tải...</div>';

    try {
        console.log("Đang tải danh sách chiến dịch...");
        const res = await fetch('http://localhost:8081/api/promotions/organizer', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        
        if (!res.ok) {
            const errorMsg = await res.text();
            console.error("Lỗi API:", errorMsg);
            listContainer.innerHTML = `<div class="empty-state">Lỗi hệ thống (${res.status}). Vui lòng thử lại sau.</div>`;
            return;
        }

        const allCampaigns = await res.json();
        console.log("Dữ liệu nhận được:", allCampaigns);

        if (!allCampaigns || !Array.isArray(allCampaigns) || allCampaigns.length === 0) {
            console.warn("Danh sách chiến dịch trống hoặc không phải mảng");
            listContainer.innerHTML = '<div class="empty-state">Bạn chưa có chiến dịch khuyến mãi nào. Hãy nhấn nút "Tạo chiến dịch" để bắt đầu.</div>';
            return;
        }

        listContainer.innerHTML = '';
        allCampaigns.forEach(cp => {
            if (!cp) return;
            
            const card = document.createElement('div');
            card.className = 'promotion-card fade-up';
            
            let statusClass = 'status-upcoming';
            if (cp.trangThai === 'Đang diễn ra') statusClass = 'status-ongoing';
            if (cp.trangThai === 'Đã kết thúc') statusClass = 'status-ended';

            const eventName = (cp.suKien && cp.suKien.tenSuKien) ? cp.suKien.tenSuKien : "Sự kiện không xác định";
            const startDate = cp.thoiDiemBD ? new Date(cp.thoiDiemBD).toLocaleDateString('vi-VN') : "---";
            const endDate = cp.thoiDiemKT ? new Date(cp.thoiDiemKT).toLocaleDateString('vi-VN') : "---";

            card.innerHTML = `
                <div class="promotion-header">
                    <div>
                        <h3 style="margin:0; font-size:1.1rem;">${cp.tenChienDich || 'Chiến dịch không tên'}</h3>
                        <p style="font-size:0.8rem; color:var(--text-muted); margin-top:5px;">
                            Sự kiện: <b>${eventName}</b> | 
                            Thời gian: ${startDate} - ${endDate}
                        </p>
                    </div>
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span class="status-pill ${statusClass}">${cp.trangThai || 'Không xác định'}</span>
                        <button class="btn btn-outline small" onclick="openCodeModal(${cp.maChienDich})"><i class="fa fa-plus"></i> Thêm Mã</button>
                        <button class="btn btn-outline small" style="color:#ff3366;" onclick="deleteCampaign(${cp.maChienDich})"><i class="fa fa-trash"></i></button>
                    </div>
                </div>
                <div class="codes-list" id="codes-${cp.maChienDich}">
                    ${cp.danhSachMaGiamGia && cp.danhSachMaGiamGia.length > 0 ? 
                        cp.danhSachMaGiamGia.map(code => {
                            const discountDisplay = code.loaiGiam === 'Theo phần trăm' ? 
                                code.soLuongGiam + '%' : 
                                Number(code.soLuongGiam).toLocaleString() + 'đ';
                            return `
                                <div class="code-item">
                                    <div>
                                        <span class="code-text">${code.maGiamGia}</span>
                                        <span style="font-size:0.75rem; margin-left:10px; color:var(--text-muted);">
                                            Giảm ${discountDisplay} 
                                            (Đã dùng ${code.soLuotDaSuDung || 0}/${code.luotDungToiDa || 0})
                                        </span>
                                    </div>
                                    <button onclick="deleteCode(${code.id})" style="background:none; border:none; color:#ff3366; cursor:pointer;"><i class="fa fa-times"></i></button>
                                </div>
                            `;
                        }).join('') : '<p style="font-size:0.8rem; color:var(--text-muted); text-align:center;">Chưa có mã giảm giá nào cho chiến dịch này.</p>'
                    }
                </div>
            `;
            listContainer.appendChild(card);
        });
        
        // Kích hoạt hiệu ứng xuất hiện cho các thẻ vừa thêm
        if (typeof initScrollReveal === 'function') initScrollReveal();

    } catch (e) {
        console.error("Lỗi khi render danh sách:", e);
        listContainer.innerHTML = `<div class="empty-state">Lỗi hiển thị dữ liệu: ${e.message}</div>`;
    }
}

function openCampaignModal() {
    document.getElementById('campaignModal').style.display = 'block';
}

function closeCampaignModal() {
    document.getElementById('campaignModal').style.display = 'none';
}

async function saveCampaign() {
    const token = localStorage.getItem('stellar_token');
    const body = {
        tenChienDich: document.getElementById('campaignName').value,
        thoiDiemBD: document.getElementById('campaignStart').value,
        thoiDiemKT: document.getElementById('campaignEnd').value,
        trangThai: "Đang diễn ra", // Mặc định tạo là đang diễn ra để test nhanh
        suKien: { maSuKien: document.getElementById('eventSelect').value }
    };

    try {
        const res = await fetch('http://localhost:8081/api/promotions/campaign', {
            method: 'POST',
            headers: { 
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        });
        if (res.ok) {
            alert("Tạo chiến dịch thành công!");
            closeCampaignModal();
            loadPromotions();
        } else {
            const err = await res.json();
            alert(err.message || "Lỗi tạo chiến dịch");
        }
    } catch (e) {
        console.error(e);
        alert("Lỗi kết nối");
    }
}

function openCodeModal(campaignId) {
    document.getElementById('currentCampaignId').value = campaignId;
    document.getElementById('codeModal').style.display = 'block';
}

function closeCodeModal() {
    document.getElementById('codeModal').style.display = 'none';
}

async function saveCode() {
    const token = localStorage.getItem('stellar_token');
    const body = {
        maGiamGia: document.getElementById('discountCode').value.toUpperCase(),
        loaiGiam: document.getElementById('discountType').value,
        soLuongGiam: document.getElementById('discountValue').value,
        giamToiDa: document.getElementById('maxDiscount').value,
        luotDungToiDa: document.getElementById('maxUsage').value,
        chienDich: { maChienDich: Number(document.getElementById('currentCampaignId').value) }
    };

    try {
        const res = await fetch('http://localhost:8081/api/promotions/code', {
            method: 'POST',
            headers: { 
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        });
        if (res.ok) {
            alert("Tạo mã giảm giá thành công!");
            closeCodeModal();
            loadPromotions();
        } else {
            const err = await res.json();
            alert(err.message || "Lỗi tạo mã");
        }
    } catch (e) {
        console.error(e);
        alert("Lỗi kết nối");
    }
}

async function deleteCampaign(id) {
    if (!confirm("Bạn có chắc muốn xóa chiến dịch này? Toàn bộ mã giảm giá liên quan sẽ bị xóa.")) return;
    const token = localStorage.getItem('stellar_token');
    try {
        await fetch(`http://localhost:8081/api/promotions/campaign/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        loadPromotions();
    } catch (e) { console.error(e); }
}

async function deleteCode(id) {
    if (!confirm("Xóa mã này?")) return;
    const token = localStorage.getItem('stellar_token');
    try {
        await fetch(`http://localhost:8081/api/promotions/code/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        loadPromotions();
    } catch (e) { console.error(e); }
}
