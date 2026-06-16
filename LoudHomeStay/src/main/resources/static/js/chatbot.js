// Chatbot Widget Logic
document.addEventListener("DOMContentLoaded", function () {
  // 1. Tự động chèn HTML của Chatbot vào trang khi tải
  const chatbotHtml = `
        <button id="chatbot-launcher" class="pulse" title="Trợ lý tư vấn AI">
            <i class="fas fa-comments"></i>
            <span class="chatbot-tooltip">LoudBooking AI</span>
        </button>
        <div id="chatbot-window">
            <div class="chatbot-header">
                <div class="chatbot-info">
                    <div class="chatbot-avatar">LH</div>
                    <div class="chatbot-status-container">
                        <h4 class="chatbot-title">LoudBooking AI</h4>
                        <span class="chatbot-status">Sẵn sàng tư vấn</span>
                    </div>
                </div>
                <button class="chatbot-close-btn">&times;</button>
            </div>
            <div class="chatbot-messages" id="chatbot-messages-list">
                <div class="chat-message bot">
                    Chào bạn! Mình là trợ lý ảo <strong>LoudBooking AI</strong>. Bạn muốn tìm kiếm khách sạn, kiểm tra phòng trống hay cần thông tin ưu đãi nào không ạ? Mình rất sẵn lòng hỗ trợ bạn!
                </div>
            </div>
            <div class="chatbot-suggestions">
                <button class="suggestion-btn" data-query="Tìm khách sạn ở Hà Nội">Tìm khách sạn ở Hà Nội</button>  
            </div>
            <div class="chatbot-input-area">
                <input type="text" class="chatbot-input" id="chatbot-text-input" placeholder="Nhập câu hỏi của bạn...">
                <button class="chatbot-send-btn" id="chatbot-send-button">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        </div>
    `;

  document.body.insertAdjacentHTML("beforeend", chatbotHtml);

  // 2. Lấy các phần tử DOM
  const launcher = document.getElementById("chatbot-launcher");
  const windowEl = document.getElementById("chatbot-window");
  const closeBtn = document.querySelector(".chatbot-close-btn");
  const messagesList = document.getElementById("chatbot-messages-list");
  const textInput = document.getElementById("chatbot-text-input");
  const sendBtn = document.getElementById("chatbot-send-button");
  const suggestionBtns = document.querySelectorAll(".suggestion-btn");

  // Quản lý lịch sử hội thoại tạm thời (tối đa 10 tin nhắn gần nhất)
  let chatHistory = [];

  // 3. Sự kiện Bật/Tắt cửa sổ chat
  launcher.addEventListener("click", () => {
    windowEl.classList.toggle("open");
    launcher.classList.remove("pulse"); // Tắt hiệu ứng nhấp nháy khi đã mở
    if (windowEl.classList.contains("open")) {
      textInput.focus();
      scrollToBottom();
    }
  });

  closeBtn.addEventListener("click", () => {
    windowEl.classList.remove("open");
  });

  // 4. Xử lý các câu hỏi gợi ý nhanh
  suggestionBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
      const query = btn.getAttribute("data-query");
      textInput.value = query;
      sendMessage();
    });
  });

  // 5. Gửi tin nhắn bằng phím Enter hoặc nút bấm
  sendBtn.addEventListener("click", sendMessage);
  textInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
      sendMessage();
    }
  });

  function sendMessage() {
    const messageText = textInput.value.trim();
    if (!messageText) return;

    // Hiển thị tin nhắn người dùng
    appendMessage(messageText, "user");
    textInput.value = "";

    // Hiển thị trạng thái đang xử lý (Typing Indicator)
    const typingIndicator = showTypingIndicator();

    // Chuẩn bị payload gửi backend
    const payload = {
      message: messageText,
      history: chatHistory,
    };

    // Gọi API Backend
    fetch("/api/chatbot/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("API Response Error");
        }
        return res.json();
      })
      .then((data) => {
        removeTypingIndicator(typingIndicator);

        const reply = data.reply;
        appendMessage(reply, "bot");

        // Cập nhật lịch sử hội thoại
        chatHistory.push({ role: "user", content: messageText });
        chatHistory.push({ role: "assistant", content: reply });

        // Giới hạn lịch sử lưu trữ (chỉ giữ 10 tin gần nhất để tránh tràn payload)
        if (chatHistory.length > 10) {
          chatHistory = chatHistory.slice(chatHistory.length - 10);
        }
      })
      .catch((err) => {
        console.error("Lỗi kết nối chatbot:", err);
        removeTypingIndicator(typingIndicator);
        appendMessage(
          "Hệ thống không phản hồi. Anh vui lòng thử lại sau hoặc kiểm tra kết nối mạng nhé.",
          "bot",
        );
      });
  }

  // 6. Helper hiển thị tin nhắn lên khung chat
  function appendMessage(text, sender) {
    const messageDiv = document.createElement("div");
    messageDiv.classList.add("chat-message", sender);

    if (sender === "bot") {
      messageDiv.innerHTML = parseMarkdown(text);
    } else {
      messageDiv.textContent = text;
    }

    messagesList.appendChild(messageDiv);
    scrollToBottom();
  }

  // 7. Helper hiển thị Typing Indicator
  function showTypingIndicator() {
    const indicatorDiv = document.createElement("div");
    indicatorDiv.classList.add("chat-message", "bot", "typing-container");

    const typingEl = document.createElement("div");
    typingEl.classList.add("typing-indicator");
    typingEl.innerHTML = `
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
        `;

    indicatorDiv.appendChild(typingEl);
    messagesList.appendChild(indicatorDiv);
    scrollToBottom();
    return indicatorDiv;
  }

  function removeTypingIndicator(indicatorDiv) {
    if (indicatorDiv && indicatorDiv.parentNode) {
      indicatorDiv.parentNode.removeChild(indicatorDiv);
    }
  }

  function scrollToBottom() {
    messagesList.scrollTop = messagesList.scrollHeight;
  }

  // 8. Markdown Parser đơn giản
  function parseMarkdown(text) {
    if (!text) return "";

    // Tránh XSS
    let html = text
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;");

    // In đậm: **text** -> <strong>text</strong>
    html = html.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");

    // In nghiêng: *text* -> <em>text</em>
    html = html.replace(/\*(.*?)\*/g, "<em>$1</em>");

    // Danh sách dạng gạch đầu dòng
    const lines = html.split("\n");
    let inList = false;

    for (let i = 0; i < lines.length; i++) {
      let line = lines[i].trim();
      if (line.startsWith("- ") || line.startsWith("* ")) {
        let content = line.substring(2);
        if (!inList) {
          lines[i] = "<ul><li>" + content + "</li>";
          inList = true;
        } else {
          lines[i] = "<li>" + content + "</li>";
        }
      } else {
        if (inList) {
          lines[i] = "</ul>" + lines[i];
          inList = false;
        }
      }
    }

    if (inList) {
      lines[lines.length - 1] += "</ul>";
    }

    html = lines.join("\n");
    // Thay xuống dòng \n bằng <br>
    html = html.replace(/\n/g, "<br>");

    return html;
  }
});
