// Custom cursor
const cursor = document.querySelector('.custom-cursor');
document.addEventListener('mousemove', (e) => {
    cursor.style.left = e.clientX + 'px';
    cursor.style.top = e.clientY + 'px';
});

function logout() {
    if(confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
        localStorage.removeItem('stellar_token');
        localStorage.removeItem('stellar_user');
        window.location.href = 'index.html';
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('stellar_token');
    if(!token) {
        alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để xem trang cá nhân.");
        window.location.href = 'auth.html';
        return;
    }

    try {
        // Fetch Profile
        const profRes = await fetch('http://localhost:8081/api/user/profile', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if(profRes.ok) {
            const profile = await profRes.json();
            document.getElementById('userNameLabel').innerText = profile.fullName || profile.username;
            document.getElementById('userEmailLabel').innerText = profile.email || 'Thành viên Ticket Portal';
            
            if(profile.walletBalance !== undefined) {
                document.getElementById('walletBalance').innerText = profile.walletBalance.toLocaleString('vi-VN') + " VNĐ";
            }
        }

        // Fetch Tickets
        const tickRes = await fetch('http://localhost:8081/api/user/tickets', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const ticketListCont = document.getElementById('ticketListCont');
        
        if(tickRes.ok) {
            const tickets = await tickRes.json();
            if(tickets.length === 0) {
                ticketListCont.innerHTML = '<p style="color:#a0a5b5; text-align:center;">Bạn chưa mua vé nào. Hãy khám phá các sự kiện đang diễn ra trên trang chủ nhé!</p>';
            } else {
                let html = '';
                tickets.forEach(tk => {
                    const d = new Date(tk.bookingTime).toLocaleString('vi-VN');
                    html += `
                        <div class="ticket-item">
                            <div>
                                <h4>SỰ KIỆN: ${tk.eventName}</h4>
                                <div class="ticket-meta">
                                    <span><i class="fa fa-qrcode"></i> INV-${tk.transactionId}</span>
                                    <span><i class="fa fa-ticket"></i> ${tk.ticketCount} vé (Hạng: ${tk.tierName})</span>
                                    <span><i class="fa fa-clock"></i> Khởi tạo: ${d}</span>
                                </div>
                            </div>
                            <div class="ticket-status">
                                Khả dụng
                            </div>
                        </div>
                    `;
                });
                ticketListCont.innerHTML = html;
            }
        }
    } catch(err) {
        document.getElementById('ticketListCont').innerHTML = '<p style="color:red; text-align:center;">Lỗi kết nối máy chủ không ổn định. Xin vui lòng F5 lại trang web.</p>';
        document.getElementById('userNameLabel').innerText = "LỖI KẾT NỐI API";
    }
});
