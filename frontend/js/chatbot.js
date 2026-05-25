// chatbot.js - Xử lý logic tương tác với Gemini AI

document.addEventListener('DOMContentLoaded', () => {
    // Inject HTML Chat window nếu chưa có
    if (!document.getElementById('chatWindow')) {
        const chatHTML = `
        <div class="chat-window" id="chatWindow">
            <div class="chat-header">
                <h3><i class="fa fa-robot"></i> Ve'ryGood AI</h3>
                <button class="chat-close" id="chatCloseBtn"><i class="fa fa-times"></i></button>
            </div>
            <div class="chat-body" id="chatBody">
                <div class="chat-msg msg-bot">
                    Xin chào! Mình là trợ lý AI của Ve'ryGood. Mình có thể giúp bạn tìm kiếm sự kiện, giải đáp thắc mắc về giá vé, cách thức đặt vé. Bạn cần giúp gì nào?
                </div>
            </div>
            <div class="chat-input-area">
                <input type="text" class="chat-input" id="chatInput" placeholder="Nhập tin nhắn..." autocomplete="off">
                <button class="chat-send-btn" id="chatSendBtn">
                    <i class="fa fa-paper-plane"></i>
                </button>
            </div>
        </div>`;
        
        // Thêm vào cuối body
        document.body.insertAdjacentHTML('beforeend', chatHTML);
    }

    const mascot = document.getElementById('mascotCompanion');
    const chatWindow = document.getElementById('chatWindow');
    const closeBtn = document.getElementById('chatCloseBtn');
    const sendBtn = document.getElementById('chatSendBtn');
    const chatInput = document.getElementById('chatInput');
    const chatBody = document.getElementById('chatBody');

    // Mở chat khi click mascot
    if (mascot) {
        mascot.addEventListener('click', () => {
            if (chatWindow.style.display === 'flex') {
                chatWindow.style.display = 'none';
            } else {
                chatWindow.style.display = 'flex';
                chatInput.focus();
                
                // Ẩn tooltip lời chào
                const tooltip = document.getElementById('mascotTooltip');
                if (tooltip) tooltip.style.display = 'none';
            }
        });
    }

    // Đóng chat
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            chatWindow.style.display = 'none';
        });
    }

    // Xử lý gửi tin nhắn
    const sendMessage = async () => {
        const message = chatInput.value.trim();
        if (!message) return;

        // Xóa text input
        chatInput.value = '';

        // Thêm tin nhắn của User
        appendMessage(message, 'user');

        // Hiển thị loading indicator
        const typingId = showTypingIndicator();

        try {
            // Gọi API
            const response = await fetch('http://localhost:8081/api/chatbot/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ message: message })
            });

            const data = await response.json();
            
            // Xóa loading
            removeTypingIndicator(typingId);

            if (response.ok && data.response) {
                // Thêm tin nhắn của Bot
                appendMessage(formatBotResponse(data.response), 'bot');
            } else {
                appendMessage("Rất tiếc, máy chủ AI đang gặp trục trặc.", 'bot');
            }
        } catch (error) {
            console.error('Chatbot Error:', error);
            removeTypingIndicator(typingId);
            appendMessage("Lỗi kết nối đến máy chủ. Vui lòng kiểm tra mạng và thử lại.", 'bot');
        }
    };

    // Gửi khi bấm nút
    if (sendBtn) {
        sendBtn.addEventListener('click', sendMessage);
    }

    // Gửi khi bấm Enter
    if (chatInput) {
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    }

    // Helper: Thêm tin nhắn vào giao diện
    function appendMessage(text, sender) {
        const msgDiv = document.createElement('div');
        msgDiv.className = `chat-msg msg-${sender}`;
        
        // Nếu là bot, có thể có thẻ HTML. User thì chỉ dùng text để tránh XSS
        if (sender === 'bot') {
            msgDiv.innerHTML = text;
        } else {
            msgDiv.textContent = text;
        }

        chatBody.appendChild(msgDiv);
        scrollToBottom();
    }

    // Helper: Hiển thị typing
    function showTypingIndicator() {
        const id = 'typing-' + Date.now();
        const typingDiv = document.createElement('div');
        typingDiv.className = 'typing-indicator';
        typingDiv.id = id;
        typingDiv.innerHTML = `
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
        `;
        chatBody.appendChild(typingDiv);
        scrollToBottom();
        return id;
    }

    // Helper: Xóa typing
    function removeTypingIndicator(id) {
        const typingDiv = document.getElementById(id);
        if (typingDiv) {
            typingDiv.remove();
        }
    }

    // Helper: Scroll xuống cuối
    function scrollToBottom() {
        chatBody.scrollTop = chatBody.scrollHeight;
    }

    // Helper: Format đơn giản (Markdown basic)
    function formatBotResponse(text) {
        // Thay thế in đậm
        let formatted = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        // Thay thế danh sách
        formatted = formatted.replace(/- (.*?)(?=\n|$)/g, '<li>$1</li>');
        // Gom thẻ li vào ul (rất basic)
        formatted = formatted.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');
        // Đổi newline thành br
        formatted = formatted.replace(/\n/g, '<br>');
        return formatted;
    }
});
