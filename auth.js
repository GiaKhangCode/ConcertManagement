// Custom cursor 
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
attachCursorEvents(document.querySelectorAll('a, button, .auth-tab, input'));

function switchTab(tab) {
    document.getElementById('mainAlert').style.display = 'none';
    if(tab === 'login') {
        document.getElementById('tabLogin').classList.add('active');
        document.getElementById('tabRegister').classList.remove('active');
        document.getElementById('loginForm').style.display = 'block';
        document.getElementById('registerForm').style.display = 'none';
    } else {
        document.getElementById('tabRegister').classList.add('active');
        document.getElementById('tabLogin').classList.remove('active');
        document.getElementById('registerForm').style.display = 'block';
        document.getElementById('loginForm').style.display = 'none';
    }
}

function showAlert(msg, isSuccess=false) {
    const alertBox = document.getElementById('mainAlert');
    alertBox.innerText = msg;
    alertBox.style.display = 'block';
    alertBox.style.background = isSuccess ? 'rgba(16, 185, 129, 0.1)' : 'rgba(255, 77, 79, 0.1)';
    alertBox.style.border = `1px solid ${isSuccess ? '#10b981' : '#ff4d4f'}`;
    alertBox.style.color = isSuccess ? '#10b981' : '#ff4d4f';
}

async function handleLogin(e) {
    e.preventDefault();
    const u = document.getElementById('loginUsername').value;
    const p = document.getElementById('loginPassword').value;

    try {
        const res = await fetch('http://localhost:8081/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: p })
        });
        const data = await res.json();
        
        if(res.ok && data.token) {
            localStorage.setItem('stellar_token', data.token);
            localStorage.setItem('stellar_user', data.username);
            localStorage.setItem('stellar_roles', JSON.stringify(data.roles || []));
            showAlert('Đăng nhập thành công! Đang chuyển hướng...', true);
            setTimeout(() => { window.location.href = 'index.html'; }, 1000);
        } else {
            showAlert('Đăng nhập thất bại. Tài khoản hoặc mật khẩu không hợp lệ.');
        }
    } catch(err) {
        showAlert('Hệ thống đang bảo trì. Xin vui lòng thử lại sau.');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const payload = {
        username: document.getElementById('regUsername').value,
        email: document.getElementById('regEmail').value,
        phone: document.getElementById('regPhone').value,
        fullName: document.getElementById('regFullName').value,
        password: document.getElementById('regPassword').value,
    };

    try {
        const res = await fetch('http://localhost:8081/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if(res.ok) {
            showAlert('Tạo tài khoản thành công! Vui lòng làm mới trang để đăng nhập.', true);
            document.getElementById('registerForm').reset();
            setTimeout(() => switchTab('login'), 2000);
        } else {
            showAlert(data.message || 'Lỗi đăng ký. Có thể username/email đã bị trùng.');
        }
    } catch(err) {
        showAlert('Hệ thống đang bảo trì. Không thể đăng ký.');
    }
}
