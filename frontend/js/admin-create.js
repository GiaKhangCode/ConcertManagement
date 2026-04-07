// Data tracking arrays
let lichDienCount = 0;
let hangVeCount = 0;

const cursor = document.querySelector('.custom-cursor');
document.addEventListener('mousemove', (e) => {
    cursor.style.left = e.clientX + 'px';
    cursor.style.top = e.clientY + 'px';
});

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

    // Init 1 instance để user đỡ tốn công ấn Added
    addLichDien();
    addHangVe();
});

function addLichDien() {
    lichDienCount++;
    const id = `ld_${Date.now()}`;
    const html = `
        <div class="dynamic-box" id="${id}">
            <button class="remove-btn" type="button" onclick="removeEl('${id}')"><i class="fa fa-times-circle"></i></button>
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px;">
                <div>
                    <label style="font-size: 0.85rem; color: #a0a5b5; margin-bottom:5px; display:block;">Tên Phiên Diễn (VD: Đêm 1)</label>
                    <input type="text" class="form-input ld-name" required placeholder="Day 1, Đêm nghệ thuật...">
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #a0a5b5; margin-bottom:5px; display:block;">Bắt Đầu Lúc</label>
                    <input type="datetime-local" class="form-input ld-start" required>
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #a0a5b5; margin-bottom:5px; display:block;">Kết Thúc Lúc</label>
                    <input type="datetime-local" class="form-input ld-end" required>
                </div>
            </div>
        </div>
    `;
    document.getElementById('lichDienContainer').insertAdjacentHTML('beforeend', html);
}

function addHangVe() {
    hangVeCount++;
    const hvId = `hv_${Date.now()}`;
    const kvContId = `kv_cont_${hvId}`;
    
    const html = `
        <div class="dynamic-box" id="${hvId}" style="border-left-color: #ffb86c; background: rgba(255, 184, 108, 0.05);">
            <button class="remove-btn" type="button" onclick="removeEl('${hvId}')"><i class="fa fa-times-circle"></i></button>
            
            <div style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 20px;">
                <div>
                    <label style="font-size: 0.85rem; color: #ffb86c; margin-bottom:5px; display:block;"><i class="fa fa-star"></i> Tên Hạng Vé</label>
                    <input type="text" class="form-input hv-name" required placeholder="Ví dụ: Khu vực VIP, Mới Nhất...">
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #ffb86c; margin-bottom:5px; display:block;">Đơn Giá (VNĐ)</label>
                    <input type="number" class="form-input hv-price" required min="0" placeholder="1000000">
                </div>
                <div>
                    <label style="font-size: 0.85rem; color: #ffb86c; margin-bottom:5px; display:block;">Số Lượng Phát Hành</label>
                    <input type="number" class="form-input hv-qty" required min="1" placeholder="100">
                </div>
            </div>
            
            <!-- Khu vực con (Zones) -->
            <div style="margin-top: 25px; padding: 25px; background: rgba(0,0,0,0.5); border-radius: 15px; border: 1px dashed rgba(255, 184, 108, 0.4);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <span style="font-size: 0.95rem; color: #ffb86c; font-family: 'Space Mono', monospace;"><i class="fa fa-map-marker-alt" style="margin-right:10px;"></i>DANH SÁCH KHU VỰC THUỘC HẠNG VÉ (ZONES)</span>
                    <button type="button" class="btn btn-outline" style="padding: 5px 15px; font-size: 0.75rem; border-color: #ffb86c; color: #ffb86c;" onclick="addKhuVuc('${kvContId}')">+ THÊM KHU VỰC</button>
                </div>
                <div id="${kvContId}" class="khuvuc-list-wrap"></div>
            </div>
        </div>
    `;
    document.getElementById('hangVeContainer').insertAdjacentHTML('beforeend', html);
    addKhuVuc(kvContId);
}

function addKhuVuc(containerId) {
    const id = `kv_${Date.now()}`;
    const html = `
        <div class="sub-box kv-item" id="${id}" style="display: flex; gap: 20px; align-items: flex-end;">
            <div style="flex: 2;">
                <label style="font-size: 0.8rem; color: #a0a5b5; margin-bottom:5px; display:block;">Tên Khu Vực / Cửa (Cổng Center, Zone A...)</label>
                <input type="text" class="form-input kv-name" required placeholder="Sảnh Chính, Khán đài A...">
            </div>
            <div style="flex: 1;">
                <label style="font-size: 0.8rem; color: #a0a5b5; margin-bottom:5px; display:block;">Sức Chứa Khu Vực (Max Seats)</label>
                <input type="number" class="form-input kv-capacity" required min="0" value="50">
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
        anhBiaUrl: document.getElementById('anhBiaUrl').value,
        phanLoai: document.getElementById('phanLoai').value,
        lichDienList: lichDienList,
        hangVeList: hangVeList
    };

    const submitBtn = document.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i> ĐANG LƯU THÔNG TIN KHỞI TẠO...';

    try {
        const response = await fetch('http://localhost:8081/api/admin/events/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        
        if(response.ok) {
            alert(`✅ Khởi tạo Sự kiện Liên hoàn Thành công!\nSự kiện đã được lưu vào Cơ sở dữ liệu. Mã sự kiện sinh ra: [ ${data.eventId} ]`);
            window.location.href = "index.html"; 
        } else {
            alert("❌ Lỗi Trùng Lặp hoặc Lỗi Cú pháp Truyền JSON: " + (data.message || 'Kiểm tra lại hệ thống'));
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa fa-check-circle" style="margin-right: 15px;"></i> XÁC NHẬN VÀ LƯU SỰ KIỆN';
        }
    } catch(err) {
        alert("⚠️ Không thể kết nối tới Database. Máy chủ 8081 không phản hồi. Vui lòng kiểm tra lại Backend.");
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa fa-check-circle" style="margin-right: 15px;"></i> XÁC NHẬN VÀ LƯU SỰ KIỆN';
    }
});
