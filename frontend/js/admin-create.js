let lichDienCount = 0;
let hangVeCount = 0;
let refundRuleCount = 0;

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('stellar_token');
    if (!token) { alert("Vui lòng đăng nhập."); window.location.href = "auth.html"; return; }

    try {
        const res = await fetch('http://localhost:8081/api/admin/locations', { headers: { 'Authorization': 'Bearer ' + token } });
        if (res.ok) {
            const list = await res.json();
            let html = '<option value="">-- [ Chọn địa điểm tổ chức ] --</option>';
            list.forEach(d => { html += `<option value="${d.maDiaDiem}">${d.tenDiaDiem} (Sức chứa: ${d.sucChua}) - ${d.tinhThanh}</option>`; });
            document.getElementById('maDiaDiem').innerHTML = html;
        }
    } catch (e) { console.error("Lỗi tải địa điểm:", e); }

    // Toggle Location Sections
    const btnExisting = document.getElementById('btnExistingLoc');
    const btnNew = document.getElementById('btnNewLoc');
    const existingSection = document.getElementById('existingLocSection');
    const newSection = document.getElementById('newLocSection');

    btnExisting.onclick = () => {
        btnExisting.classList.add('active');
        btnNew.classList.remove('active');
        existingSection.style.display = 'block';
        newSection.style.display = 'none';
        document.getElementById('maDiaDiem').required = true;
    };

    btnNew.onclick = () => {
        btnNew.classList.add('active');
        btnExisting.classList.remove('active');
        newSection.style.display = 'block';
        existingSection.style.display = 'none';
        document.getElementById('maDiaDiem').required = false;
        // Load provinces if not already loaded
        if (document.getElementById('province').options.length <= 1) {
            loadProvinces();
        }
    };

    // Provinces API Integration
    async function loadProvinces() {
        const pSelect = document.getElementById('province');
        const wSelect = document.getElementById('ward');

        const provinces = await fetch('https://provinces.open-api.vn/api/v2/p/').then(r => r.json());
        provinces.forEach(p => pSelect.add(new Option(p.name, p.code)));

        pSelect.onchange = async () => {
            wSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
            wSelect.disabled = true;
            if (pSelect.value) {
                const data = await fetch(`https://provinces.open-api.vn/api/v2/p/${pSelect.value}?depth=2`).then(r => r.json());
                if (data.wards) {
                    data.wards.forEach(w => wSelect.add(new Option(w.name, w.code)));
                    wSelect.disabled = false;
                }
            }
        };
    }

    const eventId = new URLSearchParams(window.location.search).get('id');
    if (eventId) {
        document.querySelector('h1').textContent = "CHỈNH SỬa SỰ KIỆN";
        document.title = "Chỉnh sửa Sự Kiện | Ve'ryGood";
        await loadEventData(eventId, token);
        // Không gọi initFlatpickr('.dt-picker') tại đây — loadEventData đã khởi tạo riêng
    } else {
        addLichDien(); addHangVe();
        initFlatpickr('.dt-picker');
    }
});

function initFlatpickr(selector) {
    return flatpickr(selector, { enableTime: true, altInput: true, altFormat: "d/m/Y H:i", dateFormat: "Y-m-d\\TH:i", time_24hr: true, locale: { firstDayOfWeek: 1 } });
}

/**
 * Chuẩn hóa ISO datetime string từ backend (có thể có giây) về format flatpickr chấp nhận
 * VD: "2024-05-01T18:00:00" → "2024-05-01T18:00"
 */
function normalizeDateTime(val) {
    if (!val) return null;
    // Loại bỏ giây và phần timezone nếu có
    const match = val.toString().match(/(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2})/);
    if (match) return `${match[1]}T${match[2]}`;
    return null;
}

async function loadEventData(id, token) {
    try {
        // Thêm timestamp để tránh browser cache dữ liệu cũ
        const res = await fetch(`http://localhost:8081/api/admin/events/${id}?t=${Date.now()}`, { 
            headers: { 'Authorization': 'Bearer ' + token },
            cache: 'no-store' 
        });
        if (!res.ok) throw new Error("Không thể tải dữ liệu.");
        const data = await res.json();
        document.getElementById('tenSuKien').value = data.tenSuKien || '';
        document.getElementById('maDiaDiem').value = data.maDiaDiem || '';
        document.getElementById('eventPoster').value = data.anhBiaUrl || '';
        document.getElementById('eventThumbnail').value = data.anhThumbnailUrl || '';
        document.getElementById('phanLoai').value = data.phanLoai || '';
        document.getElementById('moTa').value = data.moTa || '';

        // Khởi tạo flatpickr TRƯỜC, sau đó dùng setDate() — không gán value trực tiếp
        const fpBD = flatpickr('#thoiGianBD', { enableTime: true, altInput: true, altFormat: "d/m/Y H:i", dateFormat: "Y-m-d\\TH:i", time_24hr: true });
        const fpKT = flatpickr('#thoiGianKT', { enableTime: true, altInput: true, altFormat: "d/m/Y H:i", dateFormat: "Y-m-d\\TH:i", time_24hr: true });
        const fpMoVe = flatpickr('#thoiGianMoBanVe', { enableTime: true, altInput: true, altFormat: "d/m/Y H:i", dateFormat: "Y-m-d\\TH:i", time_24hr: true });
        const fpNgung = flatpickr('#thoiGianNgungBanVe', { enableTime: true, altInput: true, altFormat: "d/m/Y H:i", dateFormat: "Y-m-d\\TH:i", time_24hr: true });

        if (data.thoiGianBD) fpBD.setDate(normalizeDateTime(data.thoiGianBD), true);
        if (data.thoiGianKT) fpKT.setDate(normalizeDateTime(data.thoiGianKT), true);
        if (data.thoiGianMoBanVe) fpMoVe.setDate(normalizeDateTime(data.thoiGianMoBanVe), true);
        if (data.thoiGianNgungBanVe) fpNgung.setDate(normalizeDateTime(data.thoiGianNgungBanVe), true);

        // Lịch diễn và Hạng vé
        if (data.lichDienList?.length) data.lichDienList.forEach(ld => addLichDien(ld));
        else addLichDien();

        if (data.hangVeList?.length) data.hangVeList.forEach(hv => addHangVe(hv));
        else addHangVe();

        // Load Refund Policy
        if (data.refundPolicy) {
            document.getElementById('enableRefundPolicy').checked = true;
            document.getElementById('refundPolicyContainer').style.display = 'block';
            document.getElementById('refundPolicyName').value = data.refundPolicy.name || '';
            if (data.refundPolicy.rules && data.refundPolicy.rules.length > 0) {
                data.refundPolicy.rules.forEach(rule => {
                    addRefundRule(rule);
                });
            } else {
                addRefundRule();
            }
        }

    } catch (e) { alert("Lỗi: " + e.message); window.location.href = "event-management.html"; }
}

// =============================================
// FORM BUILDERS
// =============================================
function addLichDien(data = null) {
    lichDienCount++;
    const id = `ld_${Date.now()}_${Math.random().toString(36).substr(2,9)}`;
    const maLichDien = data?.maLichDien || '';
    document.getElementById('lichDienContainer').insertAdjacentHTML('beforeend', `
        <div class="dynamic-box" id="${id}" data-ma-lich-dien="${maLichDien}">
            <button class="remove-btn" type="button" onclick="removeEl('${id}')"><i class="fa fa-times-circle"></i></button>
            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:20px;">
                <div>
                    <label style="font-size:.85rem;color:#a0a5b5;margin-bottom:5px;display:block;">Tên Phiên Diễn</label>
                    <input type="text" class="form-input ld-name" required placeholder="Đêm 1..." value="${data?.tenLichDien || ''}">
                </div>
                <div>
                    <label style="font-size:.85rem;color:#a0a5b5;margin-bottom:5px;display:block;">Bắt Đầu Lúc</label>
                    <input type="text" class="form-input ld-start" id="ld_start_${id}" required>
                    <div class="time-hint" style="font-size:.7rem;">Ngày/Tháng/Năm</div>
                </div>
                <div>
                    <label style="font-size:.85rem;color:#a0a5b5;margin-bottom:5px;display:block;">Kết Thúc Lúc</label>
                    <input type="text" class="form-input ld-end" id="ld_end_${id}" required>
                    <div class="time-hint" style="font-size:.7rem;">Ngày/Tháng/Năm</div>
                </div>
            </div>
        </div>`);

    // Khởi tạo flatpickr cho 2 input vừa tạo, SAU KHI element đã có trong DOM
    const fpStart = flatpickr(`#ld_start_${id}`, { enableTime: true, altInput: true, altFormat: "d/m/Y H:i", dateFormat: "Y-m-d\\TH:i", time_24hr: true });
    const fpEnd   = flatpickr(`#ld_end_${id}`,   { enableTime: true, altInput: true, altFormat: "d/m/Y H:i", dateFormat: "Y-m-d\\TH:i", time_24hr: true });

    // Nếu có dữ liệu cũ (chế độ edit), dùng setDate() thay vì gán value
    if (data?.thoiGianBatDau) fpStart.setDate(normalizeDateTime(data.thoiGianBatDau), true);
    if (data?.thoiGianKetThuc) fpEnd.setDate(normalizeDateTime(data.thoiGianKetThuc), true);
}


function addHangVe(data = null) {
    hangVeCount++;
    const hvId = `hv_${Date.now()}_${Math.random().toString(36).substr(2,9)}`;
    const kvContId = `kv_cont_${hvId}`;
    const maHangVe = data?.maHangVe || '';
    document.getElementById('hangVeContainer').insertAdjacentHTML('beforeend', `
        <div class="dynamic-box" id="${hvId}" data-ma-hang-ve="${maHangVe}" style="border-left-color:#ffb86c;background:rgba(255,184,108,0.05);">
            <button class="remove-btn" type="button" onclick="removeEl('${hvId}')"><i class="fa fa-times-circle"></i></button>
            <div style="display:grid;grid-template-columns:2fr 1fr 1fr;gap:20px;">
                <div>
                    <label style="font-size:.85rem;color:#ffb86c;margin-bottom:5px;display:block;"><i class="fa fa-star"></i> Tên Hạng Vé</label>
                    <input type="text" class="form-input hv-name" required placeholder="VIP, General..." value="${data?.tenHangVe || ''}">
                </div>
                <div>
                    <label style="font-size:.85rem;color:#ffb86c;margin-bottom:5px;display:block;">Đơn Giá (VNĐ)</label>
                    <input type="number" class="form-input hv-price" required min="0" placeholder="1000000" value="${data?.giaNiemYet || ''}">
                </div>
                <div>
                    <label style="font-size:.85rem;color:#ffb86c;margin-bottom:5px;display:block;">Số Lượng Phát Hành</label>
                    <input type="number" class="form-input hv-qty" required min="1" placeholder="100" value="${data?.tongSoLuong || ''}">
                </div>
            </div>
            <div style="margin-top:25px;padding:25px;background:rgba(0,0,0,0.5);border-radius:15px;border:1px dashed rgba(255,184,108,0.4);">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px;">
                    <span style="font-size:.95rem;color:#ffb86c;font-family:'Space Mono',monospace;">
                        <i class="fa fa-map-marker-alt" style="margin-right:10px;"></i>KHU VỰC (ZONES)
                    </span>
                    <button type="button" class="btn btn-outline add-btn-anim"
                        style="padding:5px 15px;font-size:.75rem;border-color:#ffb86c;color:#ffb86c;"
                        onclick="addKhuVuc('${kvContId}')">+ THÊM KHU VỰC</button>
                </div>
                <div id="${kvContId}" class="khuvuc-list-wrap"></div>
            </div>
        </div>`);
    if (data?.khuVucList?.length) data.khuVucList.forEach(kv => addKhuVuc(kvContId, kv));
    else addKhuVuc(kvContId);
}

function addKhuVuc(containerId, data = null) {
    const id = `kv_${Date.now()}_${Math.random().toString(36).substr(2,9)}`;
    const capacity = data?.sucChuaKv || 0;
    const maKhuVuc = data?.maKhuVuc || '';
    document.getElementById(containerId).insertAdjacentHTML('beforeend', `
        <div class="sub-box kv-item" id="${id}" data-ma-khu-vuc="${maKhuVuc}">
            <div style="display:flex;gap:20px;align-items:flex-end;">
                <div style="flex:2;">
                    <label style="font-size:.8rem;color:#a0a5b5;margin-bottom:5px;display:block;">Tên Khu Vực</label>
                    <input type="text" class="form-input kv-name" required placeholder="Khán đài A, Zone VIP..." value="${data?.tenKhuVuc || ''}">
                </div>
                <div style="flex:1;">
                    <label style="font-size:.8rem;color:#a0a5b5;margin-bottom:5px;display:block;">Sức Chứa</label>
                    <input type="number" class="form-input kv-capacity" required min="1" value="${capacity || ''}" placeholder="50">
                </div>
                <button class="remove-btn" style="position:static;color:#ff5555;padding:15px;flex:0;background:rgba(255,0,0,0.1);border-radius:10px;" type="button" onclick="removeEl('${id}')">
                    <i class="fa fa-trash-alt"></i>
                </button>
            </div>
            <div class="kv-seat-wrap" id="kv_seat_${id}"></div>
        </div>`);

    // Gắn event listener cho ô sức chứa
    const capInput = document.querySelector(`#${id} .kv-capacity`);
    capInput.addEventListener('input', () => buildSeatConfig(id, data));
    capInput.addEventListener('change', () => buildSeatConfig(id, data));

    // Nếu đã có sức chứa (chế độ edit) → hiện config ngay
    if (capacity > 0) buildSeatConfig(id, data);
}

// Tạo block cấu hình ghế bên dưới khu vực
function buildSeatConfig(kvId, data = null) {
    const el = document.getElementById(kvId);
    if (!el) return;
    const capacity = parseInt(el.querySelector('.kv-capacity').value) || 0;
    const wrapEl = document.getElementById(`kv_seat_${kvId}`);
    if (!wrapEl) return;

    if (capacity <= 0) { wrapEl.innerHTML = ''; return; }

    // Đã có rồi thì cập nhật preview, không tạo lại
    if (wrapEl.querySelector('.kv-rows')) { updateKvPreview(kvId); return; }

    // Ưu tiên dùng data.rowConfigs nếu có (chế độ edit)
    const rowConfigs = (data && data.rowConfigs && data.rowConfigs.length > 0) ? data.rowConfigs : null;

    wrapEl.innerHTML = `
        <div class="kv-seat-config">
            <div class="kv-seat-config-title"><i class="fa fa-chair"></i> CẤU HÌNH GHẾ NGỒI</div>
            <div id="rows_container_${kvId}" class="kv-rows-list" style="display: flex; flex-direction: column; gap: 10px; margin-bottom: 15px;">
                <!-- Rows will be added here -->
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <button type="button" class="btn btn-outline small" style="padding: 5px 15px; font-size: 0.75rem; border-color: #50fa7b; color: #50fa7b;" 
                    onclick="addRowToKv('${kvId}')">
                    <i class="fa fa-plus"></i> THÊM HÀNG
                </button>
                <div class="kv-total-label" id="kv_total_${kvId}" style="font-weight: bold;"></div>
            </div>
            <div class="kv-error-msg" id="kv_error_${kvId}" style="color: #ff5555; font-size: 0.8rem; margin-top: 10px; display: none;">
                <i class="fa fa-exclamation-triangle"></i> Tổng số ghế không được vượt quá sức chứa khu vực!
            </div>
        </div>`;

    if (rowConfigs) {
        rowConfigs.forEach(rc => addRowToKv(kvId, rc.rowLabel, rc.seatCount));
    } else {
        // Mặc định tạo 1 hàng A nếu là khu vực mới
        addRowToKv(kvId, 'A', 10);
    }
    updateKvPreview(kvId);
}

function addRowToKv(kvId, label = '', qty = 10) {
    const container = document.getElementById(`rows_container_${kvId}`);
    if (!container) return;
    
    const rowId = `row_${Date.now()}_${Math.random().toString(36).substr(2,9)}`;
    const rowHtml = `
        <div class="kv-row-item" id="${rowId}" style="display: grid; grid-template-columns: 1fr 1fr 40px; gap: 10px; align-items: center; background: rgba(255,255,255,0.03); padding: 8px; border-radius: 8px;">
            <div>
                <input type="text" class="form-input row-label" value="${label}" placeholder="Tên hàng (A, B...)" style="padding: 8px;">
            </div>
            <div>
                <input type="number" class="form-input row-qty" value="${qty}" min="1" placeholder="Số ghế" style="padding: 8px;">
            </div>
            <button type="button" onclick="removeRow('${rowId}', '${kvId}')" style="background: rgba(255,85,85,0.1); color: #ff5555; border: none; border-radius: 5px; cursor: pointer; height: 35px;">
                <i class="fa fa-times"></i>
            </button>
        </div>`;
    
    container.insertAdjacentHTML('beforeend', rowHtml);
    
    // Gắn sự kiện để update preview
    const rowEl = document.getElementById(rowId);
    rowEl.querySelector('.row-label').addEventListener('input', () => updateKvPreview(kvId));
    rowEl.querySelector('.row-qty').addEventListener('input', () => updateKvPreview(kvId));
    
    updateKvPreview(kvId);
}

function removeRow(rowId, kvId) {
    document.getElementById(rowId)?.remove();
    updateKvPreview(kvId);
}

function updateKvPreview(kvId) {
    const el = document.getElementById(kvId);
    if (!el) return;
    const container = document.getElementById(`rows_container_${kvId}`);
    const capacityInput = el.querySelector('.kv-capacity');
    if (!container || !capacityInput) return;

    const rowItems = container.querySelectorAll('.kv-row-item');
    let total = 0;
    rowItems.forEach(item => {
        const qty = parseInt(item.querySelector('.row-qty').value) || 0;
        total += qty;
    });

    const capacity = parseInt(capacityInput.value) || 0;
    const totalEl = document.getElementById(`kv_total_${kvId}`);
    const errorEl = document.getElementById(`kv_error_${kvId}`);
    
    if (totalEl) {
        const isOver = total > capacity;
        totalEl.innerHTML = `Tổng: <strong style="color:${isOver ? '#ff5555' : '#50fa7b'}">${total}</strong> / ${capacity} ghế`;
        
        if (errorEl) {
            errorEl.style.display = isOver ? 'block' : 'none';
        }
    }
}

function autoRowLabels(capacity) {
    const n = Math.min(Math.max(1, Math.ceil(capacity / 10)), 26);
    return Array.from({ length: n }, (_, i) => String.fromCharCode(65 + i));
}

function removeEl(id) { document.getElementById(id)?.remove(); }

function toggleRefundPolicy() {
    const isEnabled = document.getElementById('enableRefundPolicy').checked;
    const container = document.getElementById('refundPolicyContainer');
    if (isEnabled) {
        container.style.display = 'block';
        if (refundRuleCount === 0) addRefundRule();
    } else {
        container.style.display = 'none';
    }
}

function addRefundRule(data = null) {
    refundRuleCount++;
    const ruleId = `rule_${Date.now()}`;
    const hours = data?.hoursBefore !== undefined ? data.hoursBefore : '';
    const percent = data?.percentage !== undefined ? data.percentage : '';
    const html = `
        <div class="rule-item" id="${ruleId}" style="display: grid; grid-template-columns: 1fr 1fr 40px; gap: 10px; align-items: center; background: rgba(255,255,255,0.03); padding: 10px; border-radius: 8px; margin-bottom: 10px; border-left: 3px solid #50fa7b;">
            <div>
                <label style="font-size: 0.8rem; color: #a0a5b5;">Hủy trước (Số giờ)</label>
                <input type="number" class="form-input rule-hours" required min="0" placeholder="VD: 48" style="padding: 10px;" value="${hours}">
            </div>
            <div>
                <label style="font-size: 0.8rem; color: #a0a5b5;">Tỷ lệ hoàn (%)</label>
                <input type="number" class="form-input rule-percent" required min="1" max="100" placeholder="VD: 80" style="padding: 10px;" value="${percent}">
            </div>
            <button type="button" onclick="removeEl('${ruleId}')" style="background: rgba(255,85,85,0.1); color: #ff5555; border: none; border-radius: 5px; cursor: pointer; height: 40px; margin-top: 18px;">
                <i class="fa fa-times"></i>
            </button>
        </div>
    `;
    document.getElementById('refundRulesList').insertAdjacentHTML('beforeend', html);
}

// =============================================
// SUBMIT — 1 lần duy nhất, ghế tạo luôn trong backend
// =============================================
document.getElementById('createEventForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('stellar_token');

    const lichDienList = [];
    document.querySelectorAll('#lichDienContainer .dynamic-box').forEach(node => {
        const maLichDien = node.dataset.maLichDien;
        lichDienList.push({
            maLichDien: maLichDien ? parseInt(maLichDien) : null,
            tenLichDien: node.querySelector('.ld-name').value,
            thoiGianBatDau: node.querySelector('.ld-start').value,
            thoiGianKetThuc: node.querySelector('.ld-end').value
        });
    });

    const hangVeList = [];
    document.querySelectorAll('#hangVeContainer .dynamic-box').forEach(hvNode => {
        const maHangVe = hvNode.dataset.maHangVe;
        const khuVucList = [];
        hvNode.querySelectorAll('.kv-item').forEach(kvEl => {
            const maKhuVuc = kvEl.dataset.maKhuVuc;
            const kvData = {
                maKhuVuc: maKhuVuc ? parseInt(maKhuVuc) : null,
                tenKhuVuc: kvEl.querySelector('.kv-name').value.trim(),
                sucChuaKv: parseInt(kvEl.querySelector('.kv-capacity').value) || 0
            };
            // Thu thập cấu hình hàng ghế (rowConfigs)
            const rowItems = kvEl.querySelectorAll('.kv-row-item');
            if (rowItems.length > 0) {
                const rowConfigs = [];
                let totalSeatsInRows = 0;
                rowItems.forEach(item => {
                    const label = item.querySelector('.row-label').value.trim();
                    const qty = parseInt(item.querySelector('.row-qty').value) || 0;
                    if (label && qty > 0) {
                        rowConfigs.push({ rowLabel: label, seatCount: qty });
                        totalSeatsInRows += qty;
                    }
                });

                const capacity = parseInt(kvEl.querySelector('.kv-capacity').value) || 0;
                if (totalSeatsInRows > capacity) {
                    alert(`Khu vực "${kvData.tenKhuVuc}" có tổng số ghế (${totalSeatsInRows}) vượt quá sức chứa (${capacity})!`);
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
                    throw new Error("Validation failed");
                }

                if (rowConfigs.length > 0) {
                    kvData.rowConfigs = rowConfigs;
                }
            }
            khuVucList.push(kvData);
        });
        hangVeList.push({
            maHangVe: maHangVe ? parseInt(maHangVe) : null,
            tenHangVe: hvNode.querySelector('.hv-name').value,
            giaNiemYet: parseFloat(hvNode.querySelector('.hv-price').value),
            tongSoLuong: parseInt(hvNode.querySelector('.hv-qty').value),
            khuVucList
        });
    });

    const existingId = new URLSearchParams(window.location.search).get('id');
    const isEdit = !!existingId;

    const submitBtn = document.getElementById('submitEventBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<i class="fa fa-spinner fa-spin"></i> ${isEdit ? 'ĐANG CẬP NHẬT...' : 'ĐANG LƯU...'}`;

    let refundPolicy = null;
    if (document.getElementById('enableRefundPolicy').checked) {
        const policyName = document.getElementById('refundPolicyName').value.trim();
        if (!policyName) {
            alert("Vui lòng nhập tên chính sách hoàn tiền!");
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
            return;
        }
        
        const rules = [];
        document.querySelectorAll('.rule-item').forEach(el => {
            const hours = parseInt(el.querySelector('.rule-hours').value) || 0;
            const percent = parseFloat(el.querySelector('.rule-percent').value) || 0;
            if (percent > 0) {
                rules.push({ hoursBefore: hours, percentage: percent });
            }
        });
        
        if (rules.length === 0) {
            alert("Vui lòng thêm ít nhất 1 quy tắc hoàn tiền!");
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
            return;
        }
        
        refundPolicy = { name: policyName, rules };
    }

    const payload = {
        tenSuKien: document.getElementById('tenSuKien').value,
        maDiaDiem: parseInt(document.getElementById('maDiaDiem').value),
        thoiGianBD: document.getElementById('thoiGianBD').value,
        thoiGianKT: document.getElementById('thoiGianKT').value,
        thoiGianMoBanVe: document.getElementById('thoiGianMoBanVe').value,
        thoiGianNgungBanVe: document.getElementById('thoiGianNgungBanVe').value,
        anhBiaUrl: document.getElementById('eventPoster').value,
        anhThumbnailUrl: document.getElementById('eventThumbnail').value,
        phanLoai: document.getElementById('phanLoai').value,
        moTa: document.getElementById('moTa').value,
        lichDienList, hangVeList, refundPolicy
    };

    try {
        let maDiaDiemFinal = document.getElementById('maDiaDiem').value;

        // Nếu người dùng chọn "Thêm địa điểm mới"
        if (document.getElementById('btnNewLoc').classList.contains('active')) {
            const provinceName = document.getElementById('province').options[document.getElementById('province').selectedIndex].text;
            const wardName = document.getElementById('ward').options[document.getElementById('ward').selectedIndex].text;

            const locPayload = {
                tenDiaDiem: document.getElementById('newTenDiaDiem').value,
                sucChua: parseInt(document.getElementById('newSucChua').value) || 0,
                tinhThanh: provinceName,
                phuongXa: wardName,
                soNhaTenDuong: document.getElementById('newSoNhaTenDuong').value
            };

            const locRes = await fetch('http://localhost:8081/api/admin/locations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
                body: JSON.stringify(locPayload)
            });

            if (!locRes.ok) {
                const errData = await locRes.json();
                throw new Error("Lỗi tạo địa điểm: " + errData.message);
            }

            // Backend của chúng ta hiện tại chỉ trả về Map.of("message", "Thêm địa điểm thành công!")
            // Chúng ta nên lấy ID từ danh sách mới hoặc backend nên trả về ID.
            // Để đơn giản và chắc chắn, tôi sẽ fetch lại danh sách địa điểm và tìm địa điểm vừa tạo theo tên.
            const listRes = await fetch('http://localhost:8081/api/admin/locations', { headers: { 'Authorization': 'Bearer ' + token } });
            const list = await listRes.json();
            const createdLoc = list.find(l => l.tenDiaDiem === locPayload.tenDiaDiem);
            if (createdLoc) {
                maDiaDiemFinal = createdLoc.maDiaDiem;
            } else {
                throw new Error("Không tìm thấy địa điểm vừa tạo.");
            }
        }

        if (!maDiaDiemFinal) {
            throw new Error("Vui lòng chọn hoặc thêm địa điểm.");
        }

        payload.maDiaDiem = parseInt(maDiaDiemFinal);

        const response = await fetch(
            isEdit ? `http://localhost:8081/api/admin/events/update/${existingId}` : 'http://localhost:8081/api/admin/events/create',
            { method: isEdit ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token }, body: JSON.stringify(payload) }
        );
        const data = await response.json();

        if (response.ok) {
            const savedId = isEdit ? existingId : data.eventId;
            const successMsg = data.message || `${isEdit ? 'Cập nhật' : 'Tạo'} thành công!`;
            showMascotMessage(`✅ ${successMsg}`);
            setTimeout(() => window.location.href = 'event-management.html', 3000);
        } else {
            showMascotMessage("❌ Lỗi: " + (data.message || 'Vui lòng thử lại'), true);
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
        }
    } catch (err) {
        showMascotMessage("⚠️ Không thể kết nối backend (port 8081).", true);
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa fa-check-circle" style="margin-right:15px;"></i> XÁC NHẬN VÀ LƯU SỰ KIỆN';
    }
});
