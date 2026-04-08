// Data tracking arrays
let lichDienCount = 0;
let hangVeCount = 0;

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('stellar_token');
    if(!token) {
        alert("Vui lòng đăng nhập để có thể truy cập cổng Quản trị viên.");
        window.location.href = "auth.html";
        return;
    }

    // Nạp dropdown
    try {
        const res = await fetch('http://localhost:8081/api/admin/locations', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if(res.ok) {
            const diadiems = await res.json();
            let ddHtml = '<option value="">-- [ Chọn địa điểm tổ chức ] --</option>';
            diadiems.forEach(d => {
                ddHtml += `<option value="${d.maDiaDiem}">${d.tenDiaDiem} (Sức chứa: ${d.sucChua}) - ${d.tinhThanh}</option>`;
            });
            document.getElementById('maDiaDiem').innerHTML = ddHtml;
        }
    } catch(e) { console.error("Lỗi tải danh sách địa điểm."); }

    // Init 1 instance để user đỡ tốn công ấn Added (Chỉ khi tạo mới)
    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('id');

    if (eventId) {
        // Chế độ chỉnh sửa
        document.querySelector('h1').textContent = "CHỈNH SỬA SỰ KIỆN";
        document.title = "Chỉnh sửa Sự Kiện | Ve'ryGood";
        await loadEventData(eventId, token);
    } else {
        // Chế độ tạo mới
        addLichDien();
        addHangVe();
    }

    // Khởi tạo Flatpickr cho các ô tĩnh
    initFlatpickr('.dt-picker');
});


function initFlatpickr(selector) {
    return flatpickr(selector, {
        enableTime: true,
        altInput: true,
        altFormat: "d/m/Y H:i",
        dateFormat: "Y-m-dTH:i",
        time_24hr: true,
        locale: {
            firstDayOfWeek: 1
        }
    });
}

async function loadEventData(id, token) {
    try {
        const res = await fetch(`http://localhost:8081/api/admin/events/${id}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!res.ok) throw new Error("Không thể tải dữ liệu sự kiện.");

        const data = await res.json();

        // Điền thông tin cơ bản
        document.getElementById('tenSuKien').value = data.tenSuKien;
        document.getElementById('maDiaDiem').value = data.maDiaDiem;
        document.getElementById('thoiGianBD').value = data.thoiGianBD;
        document.getElementById('thoiGianKT').value = data.thoiGianKT;
        document.getElementById('thoiGianMoBanVe').value = data.thoiGianMoBanVe;
        document.getElementById('thoiGianNgungBanVe').value = data.thoiGianNgungBanVe;
        document.getElementById('eventPoster').value = data.anhBiaUrl;
        document.getElementById('eventThumbnail').value = data.anhThumbnailUrl;
        document.getElementById('phanLoai').value = data.phanLoai;
        document.getElementById('moTa').value = data.moTa || '';

        // Điền Lịch diễn
        if (data.lichDienList && data.lichDienList.length > 0) {
            data.lichDienList.forEach(ld => {
                addLichDien(ld);
            });
        }

        // Điền Hạng vé
        if (data.hangVeList && data.hangVeList.length > 0) {
            data.hangVeList.forEach(hv => {
                addHangVe(hv);
            });
        }
    } catch (e) {
        alert("Lỗi: " + e.message);
        window.location.href = "event-management.html";
    }
}

function addLichDien(data = null) {
    lichDienCount++;
    const id = `ld_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const html = `
        <div class="dynamic-box" id="${id}">
            <button class="remove-btn" type="button" onclick="removeEl('${id}')"><i class="fa fa-times-circle"></i></button>
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px;">
                <div>
                    <label style="font-size: 0.85rem; color: #a0a5b5; margin-bottom:5px; display:block;">Tên Phiên Diễn (VD: Đêm 1)</label>
                    <input type="text" class="form-input ld-name" required placeholder="Day 1, Đêm nghệ thuật..." value="${data ? data.tenLichDien : ''}">
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #a0a5b5; margin-bottom:5px; display:block;">Bắt Đầu Lúc</label>
                    <input type="text" class="form-input ld-start dt-picker-dynamic" required placeholder="Ngày/Tháng/Năm H:i" value="${data ? data.thoiGianBatDau : ''}">
                    <div class="time-hint" style="font-size:0.7rem;">Ngày/Tháng/Năm</div>
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #a0a5b5; margin-bottom:5px; display:block;">Kết Thúc Lúc</label>
                    <input type="text" class="form-input ld-end dt-picker-dynamic" required placeholder="Ngày/Tháng/Năm H:i" value="${data ? data.thoiGianKetThuc : ''}">
                    <div class="time-hint" style="font-size:0.7rem;">Ngày/Tháng/Năm</div>
                </div>
            </div>
        </div>
    `;
    document.getElementById('lichDienContainer').insertAdjacentHTML('beforeend', html);
    
    // Khởi tạo Flatpickr cho các ô vừa thêm mới
    initFlatpickr('.dt-picker-dynamic');
}

function addHangVe(data = null) {
    hangVeCount++;
    const hvId = `hv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const kvContId = `kv_cont_${hvId}`;
    
    const html = `
        <div class="dynamic-box" id="${hvId}" style="border-left-color: #ffb86c; background: rgba(255, 184, 108, 0.05);">
            <button class="remove-btn" type="button" onclick="removeEl('${hvId}')"><i class="fa fa-times-circle"></i></button>
            
            <div style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 20px;">
                <div>
                    <label style="font-size: 0.85rem; color: #ffb86c; margin-bottom:5px; display:block;"><i class="fa fa-star"></i> Tên Hạng Vé</label>
                    <input type="text" class="form-input hv-name" required placeholder="Ví dụ: Khu vực VIP, Mới Nhất..." value="${data ? data.tenHangVe : ''}">
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #ffb86c; margin-bottom:5px; display:block;">Đơn Giá (VNĐ)</label>
                    <input type="number" class="form-input hv-price" required min="0" placeholder="1000000" value="${data ? data.giaNiemYet : ''}">
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #ffb86c; margin-bottom:5px; display:block;">Số Lượng Phát Hành</label>
                    <input type="number" class="form-input hv-qty" required min="1" placeholder="100" value="${data ? data.tongSoLuong : ''}">
                </div>
            </div>
            
            <!-- Khu vực con (Zones) -->
            <div style="margin-top: 25px; padding: 25px; background: rgba(0,0,0,0.5); border-radius: 15px; border: 1px dashed rgba(255, 184, 108, 0.4);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <span style="font-size: 0.95rem; color: #ffb86c; font-family: 'Space Mono', monospace;"><i class="fa fa-map-marker-alt" style="margin-right:10px;"></i>DANH SÁCH KHU VỰC THUỘC HẠNG VÉ (ZONES)</span>
                    <button type="button" class="btn btn-outline add-btn-anim" style="padding: 5px 15px; font-size: 0.75rem; border-color: #ffb86c; color: #ffb86c;" onclick="addKhuVuc('${kvContId}')">+ THÊM KHU VỰC</button>
                </div>
                <div id="${kvContId}" class="khuvuc-list-wrap"></div>
            </div>
        </div>
    `;
    document.getElementById('hangVeContainer').insertAdjacentHTML('beforeend', html);
    
    if (data && data.khuVucList && data.khuVucList.length > 0) {
        data.khuVucList.forEach(kv => addKhuVuc(kvContId, kv));
    } else {
        addKhuVuc(kvContId);
    }
}

function addKhuVuc(containerId, data = null) {
    const id = `kv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const html = `
        <div class="sub-box kv-item" id="${id}" style="display: flex; gap: 20px; align-items: flex-end;">
            <div style="flex: 2;">
                <label style="font-size: 0.8rem; color: #a0a5b5; margin-bottom:5px; display:block;">Tên Khu Vực / Cửa (Cổng Center, Zone A...)</label>
                <input type="text" class="form-input kv-name" required placeholder="Sảnh Chính, Khán đài A..." value="${data ? data.tenKhuVuc : ''}">
            </div>
            <div style="flex: 1;">
                <label style="font-size: 0.8rem; color: #a0a5b5; margin-bottom:5px; display:block;">Sức Chứa Khu Vực (Max Seats)</label>
                <input type="number" class="form-input kv-capacity" required min="0" value="${data ? data.sucChuaKv : '50'}">
            </div>
            <button class="remove-btn" style="position: static; color: #ff5555; padding: 15px; flex: 0; background:rgba(255,0,0,0.1); border-radius:10px;" type="button" onclick="removeEl('${id}')"><i class="fa fa-trash-alt"></i></button>
        </div>
    `;
    document.getElementById(containerId).insertAdjacentHTML('beforeend', html);
}

function removeEl(id) { document.getElementById(id).remove(); }

document.getElementById('createEventForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('stellar_token');

    // DOM Scraping to Nested array Lịch Diễn
    const ldNodes = document.querySelectorAll('#lichDienContainer .dynamic-box');
    const lichDienList = [];
    ldNodes.forEach(node => {
        lichDienList.push({
            tenLichDien: node.querySelector('.ld-name').value,
            thoiGianBatDau: node.querySelector('.ld-start').value,
            thoiGianKetThuc: node.querySelector('.ld-end').value
        });
    });

    // DOM Scraping to Nested array Hạng Vé => Phân nhánh Khu Vực
    const hvNodes = document.querySelectorAll('#hangVeContainer .dynamic-box');
    const hangVeList = [];
    hvNodes.forEach(node => {
        const khuVucList = [];
        const kvNodes = node.querySelectorAll('.kv-item');
        kvNodes.forEach(kv => {
            khuVucList.push({
                tenKhuVuc: kv.querySelector('.kv-name').value,
                sucChuaKv: parseInt(kv.querySelector('.kv-capacity').value) || 0
            });
        });

        hangVeList.push({
            tenHangVe: node.querySelector('.hv-name').value,
            giaNiemYet: parseFloat(node.querySelector('.hv-price').value),
            tongSoLuong: parseInt(node.querySelector('.hv-qty').value),
            khuVucList: khuVucList
        });
    });

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
        lichDienList: lichDienList,
        hangVeList: hangVeList
    };

    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('id');
    const isEdit = !!eventId;

    const submitBtn = document.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = isEdit ? '<i class="fa fa-spinner fa-spin"></i> ĐANG CẬP NHẬT THÔNG TIN...' : '<i class="fa fa-spinner fa-spin"></i> ĐANG LƯU THÔNG TIN KHỞI TẠO...';

    const apiUrl = isEdit 
        ? `http://localhost:8081/api/admin/events/update/${eventId}`
        : 'http://localhost:8081/api/admin/events/create';
    
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const response = await fetch(apiUrl, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        
        if(response.ok) {
            if (isEdit) {
                showMascotMessage("✅ Cập nhật Sự kiện Thành công! " + (data.message || ''));
                setTimeout(() => window.location.href = "event-management.html", 2500);
            } else {
                showMascotMessage(`✅ Khởi tạo Thành công! Mã sự kiện: [ ${data.eventId} ]`);
                setTimeout(() => window.location.href = "index.html", 3000); 
            }
        } else {
            showMascotMessage("❌ Lỗi: " + (data.message || 'Kiểm tra lại hệ thống'), true);
            submitBtn.disabled = false;
            submitBtn.innerHTML = isEdit ? '<i class="fa fa-check-circle" style="margin-right: 15px;"></i> LƯU THAY ĐỔI' : '<i class="fa fa-check-circle" style="margin-right: 15px;"></i> XÁC NHẬN VÀ LƯU SỰ KIỆN';
        }
    } catch(err) {
        showMascotMessage("⚠️ Không thể kết nối tới Database. Máy chủ 8081 không phản hồi. Vui lòng kiểm tra lại Backend.", true);
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa fa-check-circle" style="margin-right: 15px;"></i> XÁC NHẬN VÀ LƯU SỰ KIỆN';
    }
});
