let lichDienCount = 0;
let hangVeCount = 0;
let refundRuleCount = 0;

async function safeParseJson(response, stepName) {
    const text = await response.text();
    try {
        return text ? JSON.parse(text) : {};
    } catch (e) {
        console.error(`[${stepName}] Lỗi giải mã JSON. Nội dung nhận được:`, text);
        throw new Error(`[${stepName}] Lỗi hệ thống: Phản hồi không đúng định dạng.`);
    }
}

/**
 * Biến đổi một thẻ <select> thông thường thành một combobox có tính năng tìm kiếm (Autocomplete)
 * @param {string} selectId - ID của thẻ select gốc
 * @param {string} placeholder - Văn bản gợi ý khi chưa chọn
 */
function setupAutocomplete(selectId, placeholder = "Tìm kiếm...") {
    const select = document.getElementById(selectId);
    if (!select) return null;

    // Tạo container
    const container = document.createElement('div');
    container.className = 'autocomplete-container';
    select.parentNode.insertBefore(container, select);

    // Tạo input giả để tìm kiếm và hiển thị giá trị đã chọn
    const wrapper = document.createElement('div');
    wrapper.className = 'autocomplete-input-wrapper';
    
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'form-input';
    input.placeholder = placeholder;
    input.autocomplete = 'off';
    
    const icon = document.createElement('i');
    icon.className = 'fa fa-chevron-down';
    
    wrapper.appendChild(input);
    wrapper.appendChild(icon);
    container.appendChild(wrapper);

    // Tạo danh sách kết quả
    const results = document.createElement('div');
    results.className = 'autocomplete-results';
    container.appendChild(results);

    // Ẩn select gốc
    select.style.display = 'none';

    let options = [];

    const updateOptionsList = () => {
        options = Array.from(select.options).map(opt => ({
            text: opt.text,
            value: opt.value,
            element: opt
        })).filter(opt => opt.value !== "");
        
        // Cập nhật giá trị hiển thị ban đầu nếu có
        if (select.selectedIndex >= 0 && select.value !== "") {
            input.value = select.options[select.selectedIndex].text;
        } else {
            input.value = "";
        }
    };

    const renderResults = (filter = "") => {
        results.innerHTML = "";
        const filtered = options.filter(opt => 
            opt.text.toLowerCase().includes(filter.toLowerCase())
        );

        if (filtered.length === 0) {
            results.innerHTML = `<div class="autocomplete-no-results">Không tìm thấy kết quả phù hợp</div>`;
            return;
        }

        filtered.forEach(opt => {
            const div = document.createElement('div');
            div.className = 'autocomplete-option';
            if (opt.value === select.value) div.classList.add('selected');
            div.textContent = opt.text;
            div.onclick = () => {
                select.value = opt.value;
                input.value = opt.text;
                container.classList.remove('active');
                select.dispatchEvent(new Event('change'));
            };
            results.appendChild(div);
        });
    };

    // Events
    input.onfocus = () => {
        updateOptionsList();
        renderResults(""); // Hiển thị tất cả kết quả khi click vào để người dùng dễ chọn lại
        container.classList.add('active');
        input.select(); // Bôi đen text để dễ dàng xóa/thay thế
    };

    input.oninput = () => {
        renderResults(input.value);
    };

    // Đóng khi click ngoài
    document.addEventListener('click', (e) => {
        if (!container.contains(e.target)) {
            container.classList.remove('active');
            // Reset input về giá trị đã chọn nếu người dùng xóa sạch mà không chọn cái mới
            if (select.selectedIndex >= 0 && select.value !== "") {
                input.value = select.options[select.selectedIndex].text;
            } else if (!select.value) {
                input.value = "";
            }
        }
    });

    // Theo dõi thay đổi của select gốc để cập nhật input (ví dụ khi load dữ liệu edit)
    const observer = new MutationObserver(() => {
        updateOptionsList();
        input.disabled = select.disabled;
        if (select.disabled) container.classList.add('disabled');
        else container.classList.remove('disabled');
    });
    observer.observe(select, { childList: true, attributes: true, attributeFilter: ['disabled'] });

    // Khởi tạo trạng thái disabled ban đầu
    input.disabled = select.disabled;
    if (select.disabled) container.classList.add('disabled');

    // Hỗ trợ cập nhật input khi giá trị select thay đổi từ code
    select.addEventListener('change', () => {
        if (select.selectedIndex >= 0 && select.value !== "") {
            input.value = select.options[select.selectedIndex].text;
        } else {
            input.value = "";
        }
    });

    return { refresh: updateOptionsList };
}

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('stellar_token');
    if (!token) { alert("Vui lòng đăng nhập."); window.location.href = "auth.html"; return; }

    try {
        const res = await fetch('http://localhost:8081/api/admin/locations', { headers: { 'Authorization': 'Bearer ' + token } });
        if (res.ok) {
            const list = await safeParseJson(res, "Tải danh sách địa điểm");
            let html = '<option value="">-- [ Chọn địa điểm tổ chức ] --</option>';
            list.forEach(d => { html += `<option value="${d.maDiaDiem}">${d.tenDiaDiem} (Sức chứa: ${d.sucChua}) - ${d.tinhThanh}</option>`; });
            document.getElementById('maDiaDiem').innerHTML = html;
            
            // Khởi tạo autocomplete cho maDiaDiem sau khi đã tải xong data
            setupAutocomplete('maDiaDiem', "Tìm kiếm địa điểm...");
        }

        // Khởi tạo autocomplete cho Phân loại sự kiện
        setupAutocomplete('phanLoai', "-- [ Chọn thể loại ] --");
        
        // Kiểm tra thông tin nhà tổ chức
        const profRes = await fetch('http://localhost:8081/api/user/profile', { headers: { 'Authorization': 'Bearer ' + token } });
        if (profRes.ok) {
            const profile = await safeParseJson(profRes, "Tải thông tin cá nhân");
            const isOrganizer = profile.roles && profile.roles.includes('ROLE_ORGANIZER');
            const isAdmin = profile.roles && profile.roles.includes('ROLE_ADMIN');
            
            if (isOrganizer && !isAdmin && !profile.organizationName) {
                alert("⚠️ Bạn cần thiết lập thông tin nhà tổ chức trước khi tạo sự kiện!");
                window.location.href = "profile.html";
                return;
            }
            
            // Hiển thị form nếu hợp lệ
            document.getElementById('main-admin-container').style.display = 'block';
        }

        // Tải danh sách nghệ sĩ có sẵn
        const artRes = await fetch('http://localhost:8081/api/admin/artists', { headers: { 'Authorization': 'Bearer ' + token } });
        if (artRes.ok) {
            const artists = await safeParseJson(artRes, "Tải danh sách nghệ sĩ");
            window.artistOptionsHTML = artists.map(a => `<option value="${a.tenNgheSi}">${a.tenNgheSi}</option>`).join('');
        }

    } catch (e) { 
        console.error("Lỗi khởi tạo:", e);
        // Nếu lỗi API vẫn cho hiện để không bị kẹt trang trắng (hoặc có thể xử lý khác tùy UI)
        document.getElementById('main-admin-container').style.display = 'block';
    }

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

        // Khởi tạo autocomplete cho Tỉnh/Thành
        setupAutocomplete('province', "Chọn Tỉnh/Thành...");
        // Khởi tạo autocomplete cho Phường/Xã
        setupAutocomplete('ward', "Chọn Phường/Xã...");

        pSelect.onchange = async () => {
            wSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
            wSelect.disabled = true;
            
            // Dispatch event để autocomplete của ward biết là options đã bị xóa
            wSelect.dispatchEvent(new Event('change'));

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
        const data = await safeParseJson(res, "Tải dữ liệu sự kiện để sửa");
        document.getElementById('tenSuKien').value = data.tenSuKien || '';
        
        // Gán địa điểm và kích hoạt sự kiện để autocomplete cập nhật UI
        const maDiaDiemEl = document.getElementById('maDiaDiem');
        if (maDiaDiemEl) {
            maDiaDiemEl.value = data.maDiaDiem || '';
            maDiaDiemEl.dispatchEvent(new Event('change'));
        }

        document.getElementById('eventPoster').value = data.anhBiaUrl || '';
        document.getElementById('eventThumbnail').value = data.anhThumbnailUrl || '';
        
        // Gán phân loại và kích hoạt sự kiện
        const phanLoaiEl = document.getElementById('phanLoai');
        if (phanLoaiEl) {
            phanLoaiEl.value = data.phanLoai || '';
            phanLoaiEl.dispatchEvent(new Event('change'));
        }

        document.getElementById('moTa').value = data.moTa || '';

        // Hiển thị lý do từ chối nếu có
        const rejectionDiv = document.getElementById('rejectionReason');
        const rejectionText = document.getElementById('rejectionReasonText');
        if (data.lyDoTuChoi && data.lyDoTuChoi.trim()) {
            rejectionDiv.style.display = 'block';
            rejectionText.textContent = data.lyDoTuChoi;
        } else {
            rejectionDiv.style.display = 'none';
        }

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

        // Load Sponsors
        if (data.sponsors && data.sponsors.length > 0) {
            data.sponsors.forEach(s => addSponsorRow(s));
        }

        // Load Artists
        if (data.ngheSiList && data.ngheSiList.length > 0) {
            data.ngheSiList.forEach(ns => addArtistRow(ns));
        }

        // Load Stage Builder (SeatMap)
        try {
            const seatmapRes = await fetch(`http://localhost:8081/api/admin/events/${id}/seatmap`, { headers: { 'Authorization': 'Bearer ' + token } });
            if (seatmapRes.ok) {
                const seatmapData = await seatmapRes.json();
                if (seatmapData && seatmapData.duLieuCanvas && typeof canvas !== 'undefined') {
                    document.getElementById('enableStageBuilder').checked = true;
                    toggleStageBuilder();
                    canvas.loadFromJSON(seatmapData.duLieuCanvas, function() {
                        canvas.getObjects().forEach(obj => {
                            if (obj.stroke === '#ffffff11') canvas.sendToBack(obj);
                        });
                        canvas.renderAll();
                    });
                }
            }
        } catch (e) { console.log('Sự kiện chưa có sơ đồ.'); }

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
                <div style="flex:0; min-width: 50px;">
                    <label style="font-size:.8rem;color:#a0a5b5;margin-bottom:5px;display:block;">Màu</label>
                    <input type="color" class="kv-color" value="${data?.mauSac || '#3B82F6'}" style="width: 100%; height: 38px; border: none; cursor: pointer; background: transparent;" onchange="if(window.updateTicketTypeDropdown) updateTicketTypeDropdown()">
                </div>
                <button class="remove-btn" style="position:static;color:#ff5555;padding:15px;flex:0;background:rgba(255,0,0,0.1);border-radius:10px;" type="button" onclick="removeEl('${id}')">
                    <i class="fa fa-trash-alt"></i>
                </button>
            </div>
        </div>`);
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
// SPONSOR BUILDERS
// =============================================
function addSponsorRow(data = null) {
    const id = `sp_${Date.now()}_${Math.random().toString(36).substr(2,9)}`;
    const html = `
        <div class="dynamic-box" id="${id}" style="border-left-color: #50fa7b; background: rgba(80, 250, 123, 0.05); padding: 20px;">
            <button class="remove-btn" type="button" onclick="removeEl('${id}')"><i class="fa fa-times-circle"></i></button>
            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 20px;">
                <div>
                    <label style="font-size: 0.85rem; color: #50fa7b; margin-bottom: 5px; display: block;">Tên Nhà Tài Trợ</label>
                    <input type="text" class="form-input sp-name" required placeholder="VD: Pepsi, Heineken..." value="${data?.name || ''}">
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #50fa7b; margin-bottom: 5px; display: block;">Hạng Tài Trợ</label>
                    <select class="form-input sp-rank">
                        <option value="Kim Cương" ${data?.rank === 'Kim Cương' ? 'selected' : ''}>Kim Cương</option>
                        <option value="Vàng" ${data?.rank === 'Vàng' ? 'selected' : ''}>Vàng</option>
                        <option value="Bạc" ${data?.rank === 'Bạc' ? 'selected' : ''}>Bạc</option>
                        <option value="Đồng" ${data?.rank === 'Đồng' || !data?.rank ? 'selected' : ''}>Đồng</option>
                    </select>
                </div>
            </div>
        </div>
    `;
    document.getElementById('sponsorsContainer').insertAdjacentHTML('beforeend', html);
}

// =============================================
// ARTIST BUILDERS
// =============================================
function toggleArtistMode(rowId, mode) {
    const row = document.getElementById(rowId);
    if (!row) return;
    const btnEx = row.querySelector('.btn-art-existing');
    const btnNew = row.querySelector('.btn-art-new');
    const secEx = row.querySelector('.art-existing-section');
    const secNew = row.querySelector('.art-new-section');

    if (mode === 'existing') {
        btnEx.classList.add('active');
        btnNew.classList.remove('active');
        secEx.style.display = 'block';
        secNew.style.display = 'none';
        row.dataset.mode = 'existing';
    } else {
        btnNew.classList.add('active');
        btnEx.classList.remove('active');
        secNew.style.display = 'block';
        secEx.style.display = 'none';
        row.dataset.mode = 'new';
    }
}

function addArtistRow(data = null) {
    const id = `art_${Date.now()}_${Math.random().toString(36).substr(2,9)}`;
    // Mặc định hiển thị select nếu không có data hoặc data đã có mã nghệ sĩ, còn lại là new
    const isNew = data && !data.maNgheSi && data.tenNgheSi;
    const initialMode = isNew ? 'new' : 'existing';

    const html = `
        <div class="dynamic-box" id="${id}" data-mode="${initialMode}" style="border-left-color: #ff55ff; background: rgba(255, 85, 255, 0.05); padding: 20px;">
            <button class="remove-btn" type="button" onclick="removeEl('${id}')"><i class="fa fa-times-circle"></i></button>
            <div style="display: flex; gap: 15px; margin-bottom: 15px;">
                <button type="button" class="btn btn-outline small btn-art-existing ${initialMode === 'existing' ? 'active' : ''}" onclick="toggleArtistMode('${id}', 'existing')">Chọn có sẵn</button>
                <button type="button" class="btn btn-outline small btn-art-new ${initialMode === 'new' ? 'active' : ''}" onclick="toggleArtistMode('${id}', 'new')">Thêm mới</button>
            </div>
            
            <div class="art-existing-section" style="display: ${initialMode === 'existing' ? 'block' : 'none'};">
                <label style="font-size: 0.85rem; color: #ff55ff; margin-bottom: 5px; display: block;">Chọn Nghệ Sĩ</label>
                <select class="form-input art-select" style="width: 100%;">
                    <option value="">-- Chọn nghệ sĩ --</option>
                    ${window.artistOptionsHTML || ''}
                </select>
            </div>

            <div class="art-new-section" style="display: ${initialMode === 'new' ? 'block' : 'none'};">
                <label style="font-size: 0.85rem; color: #ff55ff; margin-bottom: 5px; display: block;">Tên Nghệ Sĩ Mới</label>
                <input type="text" class="form-input art-name" placeholder="VD: Sơn Tùng M-TP..." value="${isNew ? data.tenNgheSi : ''}">
            </div>
        </div>
    `;
    document.getElementById('artistsContainer').insertAdjacentHTML('beforeend', html);

    // Set giá trị nếu có
    if (!isNew && data && data.tenNgheSi) {
        const select = document.getElementById(id).querySelector('.art-select');
        select.value = data.tenNgheSi;
        setupAutocomplete(select.id || (select.id = 'select_' + id), "Tìm kiếm nghệ sĩ...");
    } else {
        const select = document.getElementById(id).querySelector('.art-select');
        setupAutocomplete(select.id || (select.id = 'select_' + id), "Tìm kiếm nghệ sĩ...");
    }
}

// =============================================
// SUBMIT — 1 lần duy nhất, ghế tạo luôn trong backend
// =============================================
document.getElementById('createEventForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('stellar_token');
    const existingId = new URLSearchParams(window.location.search).get('id');
    const isEdit = !!existingId;
    const submitBtn = document.getElementById('submitEventBtn');

    // Validation cơ bản trước khi xử lý
    // Chỉ validate các trường text thông thường, KHÔNG validate datetime (do flatpickr dùng hidden input)
    const requiredFields = [
        { id: 'tenSuKien', name: 'Tên sự kiện' },
        { id: 'eventPoster', name: 'URL Ảnh Poster' },
        { id: 'eventThumbnail', name: 'URL Ảnh Thumbnail' }
    ];

    for (const field of requiredFields) {
        const el = document.getElementById(field.id);
        if (el && !el.value.trim()) {
            alert(`Vui lòng nhập: ${field.name}`);
            el.focus();
            return;
        }
    }

    // Validate địa điểm
    const isNewLoc = document.getElementById('btnNewLoc').classList.contains('active');
    if (!isNewLoc && !document.getElementById('maDiaDiem').value) {
        alert('Vui lòng chọn địa điểm tổ chức!');
        return;
    }

    // Validate datetime: đọc trực tiếp value của input gốc (flatpickr ghi vào đây)
    const dateFields = [
        { id: 'thoiGianMoBanVe', name: 'Thời gian mở bán vé' },
        { id: 'thoiGianNgungBanVe', name: 'Thời gian ngừng bán vé' },
        { id: 'thoiGianBD', name: 'Thời gian bắt đầu sự kiện' },
        { id: 'thoiGianKT', name: 'Thời gian kết thúc sự kiện' }
    ];
    for (const field of dateFields) {
        const el = document.getElementById(field.id);
        if (el && !el.value) {
            alert(`Vui lòng chọn: ${field.name}`);
            return;
        }
    }

    console.log('[Submit] Validation passed, building payload...');

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
    try {
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
    } catch (e) {
        if (e.message === "Validation failed") return;
        throw e;
    }

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
        lichDienList, hangVeList, refundPolicy,
        sponsors: Array.from(document.querySelectorAll('#sponsorsContainer .dynamic-box')).map(node => ({
            name: node.querySelector('.sp-name').value.trim(),
            rank: node.querySelector('.sp-rank').value
        })),
        ngheSiList: Array.from(document.querySelectorAll('#artistsContainer .dynamic-box')).map(node => {
            const mode = node.dataset.mode;
            const tenNs = mode === 'existing' ? node.querySelector('.art-select').value.trim() : node.querySelector('.art-name').value.trim();
            return { tenNgheSi: tenNs };
        }).filter(ns => ns.tenNgheSi)
    };

    try {
        let maDiaDiemFinal = document.getElementById('maDiaDiem').value;

        // Nếu người dùng chọn "Thêm địa điểm mới"
        if (document.getElementById('btnNewLoc').classList.contains('active')) {
            const pSel = document.getElementById('province');
            const wSel = document.getElementById('ward');
            
            if (!pSel.value || !wSel.value) {
                alert("Vui lòng chọn Tỉnh/Thành và Phường/Xã!");
                submitBtn.disabled = false;
                submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
                return;
            }

            const provinceName = pSel.options[pSel.selectedIndex].text;
            const wardName = wSel.options[wSel.selectedIndex].text;

            const locPayload = {
                tenDiaDiem: document.getElementById('newTenDiaDiem').value.trim(),
                sucChua: parseInt(document.getElementById('newSucChua').value) || 0,
                tinhThanh: provinceName,
                phuongXa: wardName,
                soNhaTenDuong: document.getElementById('newSoNhaTenDuong').value.trim()
            };

            if (!locPayload.tenDiaDiem || locPayload.sucChua <= 0) {
                alert("Vui lòng nhập tên địa điểm và sức chứa hợp lệ!");
                submitBtn.disabled = false;
                submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
                return;
            }

            const locRes = await fetch('http://localhost:8081/api/admin/locations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
                body: JSON.stringify(locPayload)
            });

            if (!locRes.ok) {
                const locErrText = await locRes.text();
                console.error("Lỗi tạo địa điểm:", locErrText);
                throw new Error("Lỗi tạo địa điểm: " + locErrText);
            }

            // Fetch lại danh sách địa điểm và tìm địa điểm vừa tạo theo tên.
            const listRes = await fetch('http://localhost:8081/api/admin/locations', { headers: { 'Authorization': 'Bearer ' + token } });
            const listText = await listRes.text();
            let list = [];
            try { list = JSON.parse(listText); } catch(e) { console.error("Lỗi parse list địa điểm sau tạo:", listText); }
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
        const data = await safeParseJson(response, isEdit ? "Cập nhật sự kiện" : "Tạo sự kiện mới");

        if (response.ok) {
            const savedId = isEdit ? existingId : data.eventId;
            
            // LƯU SƠ ĐỒ SÂN KHẤU NẾU CÓ BẬT VÀ CÓ DỮ LIỆU
            const stageData = typeof getStageBuilderData === 'function' ? getStageBuilderData() : null;
            if (stageData) {
                try {
                    await fetch(`http://localhost:8081/api/admin/events/${savedId}/seatmap`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
                        body: JSON.stringify(stageData)
                    });
                } catch (err) {
                    console.error("Lỗi khi lưu sơ đồ:", err);
                }
            }

            const successMsg = data.message || `${isEdit ? 'Cập nhật' : 'Tạo'} thành công!`;
            showMascotMessage(`✅ ${successMsg}`);
            setTimeout(() => window.location.href = 'event-management.html', 3000);
        } else {
            // Hiển thị nội dung lỗi cụ thể từ Backend
            const errMsg = data.message || 'Vui lòng kiểm tra lại dữ liệu nhập vào.';
            showMascotMessage("❌ Lỗi: " + errMsg, true);
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
        }
    } catch (err) {
        console.error("Lỗi khi lưu sự kiện:", err);
        showMascotMessage("⚠️ Lỗi hệ thống hoặc kết nối: " + err.message, true);
        submitBtn.disabled = false;
        submitBtn.innerHTML = `<i class="fa fa-check-circle" style="margin-right:15px;"></i>${isEdit ? 'LƯU THAY ĐỔI' : 'XÁC NHẬN VÀ LƯU SỰ KIỆN'}`;
    }
});
