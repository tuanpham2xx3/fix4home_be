# Flutter Message & Chatbot API - Quick Reference

## 🔗 Base URL
```
Development: http://localhost:8100
Production: https://api.fix4home.com
```

## 🔑 Authentication
Tất cả endpoints yêu cầu JWT token:
```
Authorization: Bearer {token}
```

---

## 📝 Endpoints Summary

### Conversations

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/v1/chat/conversations` | Lấy danh sách conversations |
| GET | `/api/v1/chat/conversations/paginated?page=0&size=10` | Lấy với pagination |
| GET | `/api/v1/chat/conversations/{id}` | Chi tiết conversation |
| POST | `/api/v1/chat/conversations/find-or-create?otherUserId={id}` | Tìm hoặc tạo conversation |
| PUT | `/api/v1/chat/conversations/{id}/archive` | Archive conversation |

### Messages

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/v1/chat/conversations/{id}/messages?page=0&size=20` | Lấy messages |
| GET | `/api/v1/chat/conversations/{id}/messages/before?beforeDate={date}&size=20` | Lấy messages cũ hơn (infinite scroll) |
| POST | `/api/v1/chat/messages/mark-read` | Đánh dấu đã đọc |
| GET | `/api/v1/chat/messages/unread` | Lấy unread messages |

### Chatbot

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/v1/chatbot/send` | Gửi message đến chatbot (với session management) |

### WebSocket

| Endpoint | Mô tả |
|----------|-------|
| `ws://localhost:8100/ws-native` | WebSocket connection (STOMP) |
| `/user/queue/messages` | Subscribe để nhận messages |
| `/app/chat.send` | Send message qua WebSocket |

---

## 💬 Request/Response Examples

### 1. Get Conversations

**Request:**
```dart
GET /api/v1/chat/conversations
Headers: Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "conversationType": "CHATBOT",
      "lastMessage": {...},
      "unreadCount": 2
    }
  ]
}
```

---

### 2. Get Messages

**Request:**
```dart
GET /api/v1/chat/conversations/1/messages?page=0&size=20
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "content": "Hello!",
        "sentAt": "2025-01-15T10:00:00",
        "sender": {...}
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

### 3. Send Message to Chatbot

**Request:**
```dart
POST /api/v1/chatbot/send
Body: {
  "conversationId": 1,
  "message": "Hello chatbot!",
  "sessionId": "uuid-here"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "sessionId": "uuid-here",
    "messageId": 100,
    "messageSent": true,
    "newSessionCreated": false  // true nếu session mới được tạo
  }
}
```

**⚠️ Lưu ý:** 
- Mỗi session chỉ được gửi tối đa 5 lần
- Nếu `newSessionCreated = true`, phải lưu sessionId mới
- SessionId dùng để lưu context trong n8n node memory

---

### 4. Mark Messages as Read

**Request:**
```dart
POST /api/v1/chat/messages/mark-read
Body: {
  "conversationId": 1,
  "messageIds": [1, 2, 3]  // Optional, null để mark all
}
```

---

## 🔌 WebSocket Quick Setup

```dart
// 1. Connect
final wsService = WebSocketService();
wsService.connect('ws://localhost:8100/ws-native', jwtToken);

// 2. Subscribe to messages
wsService.subscribeToMessages();

// 3. Listen callback
wsService.onMessageReceived = (message) {
  // Update UI
};

// 4. Send message (optional)
wsService.sendMessage(
  conversationId: 1,
  content: "Hello!",
);
```

---

## 📦 Data Models (Tóm tắt)

### ConversationDTO
```dart
{
  id: int,
  conversationType: "CHATBOT" | "FREE" | "BUSINESS",
  lastMessage: MessagePreviewDTO?,
  unreadCount: int,
  customer: ParticipantDTO,
  technician: ParticipantDTO
}
```

### MessageDTO
```dart
{
  id: int,
  conversationId: int,
  content: String,
  messageType: "TEXT" | "IMAGE" | "FILE" | ...,
  sentAt: DateTime,
  sender: SenderDTO?,
  isRead: bool
}
```

---

## 🎯 Common Flows

### Flow 1: Chat với Chatbot

```dart
// 1. Get chatbot conversation (đã có sẵn khi đăng ký)
final conversation = await findOrCreateConversation(chatbotUserId);

// 2. Get sessionId từ storage (hoặc tạo mới)
String sessionId = await getSessionId(); // UUID

// 3. Send message
final response = await sendMessageToChatbot(
  conversationId: conversation.id,
  message: "Hello!",
  sessionId: sessionId,
);

// 4. Update sessionId nếu cần
if (response.newSessionCreated) {
  await saveSessionId(response.sessionId);
}

// 5. Nhận response qua WebSocket hoặc reload messages
```

### Flow 2: Chat thông thường

```dart
// 1. Get conversation
final conversation = await findOrCreateConversation(otherUserId);

// 2. Load messages
final messages = await getMessages(conversationId: conversation.id);

// 3. Send via WebSocket
wsService.sendMessage(
  conversationId: conversation.id,
  content: "Hello!",
);

// 4. Nhận real-time qua WebSocket subscription
```

---

## ⚡ Tips & Tricks

1. **SessionId cho Chatbot:**
   - Lưu vào SharedPreferences
   - Format: UUID (ví dụ: `7ddbb723-2fc3-46d7-acf5-d2c505424025`)
   - Update khi `newSessionCreated = true`

2. **WebSocket:**
   - Reconnect tự động khi mất kết nối
   - Subscribe lại sau reconnect
   - Disconnect khi app vào background (optional)

3. **Message Loading:**
   - Dùng pagination
   - Infinite scroll với `getMessagesBefore`
   - Reverse list để hiển thị từ cũ đến mới

4. **Performance:**
   - Cache conversations locally
   - Lazy load messages
   - Debounce typing indicator

---

## 📚 Full Documentation

Xem chi tiết tại: [FLUTTER_MESSAGE_CHATBOT_API.md](./FLUTTER_MESSAGE_CHATBOT_API.md)




