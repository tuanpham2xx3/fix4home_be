# Tài Liệu API - Message & Chatbot System cho Flutter

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [Conversation APIs](#conversation-apis)
5. [Message APIs](#message-apis)
6. [Chatbot APIs](#chatbot-apis)
7. [WebSocket APIs](#websocket-apis)
8. [Data Models](#data-models)
9. [Flutter Implementation Examples](#flutter-implementation-examples)

---

## Tổng Quan

Hệ thống Message & Chatbot cung cấp các API để:
- Quản lý conversations (cuộc trò chuyện)
- Gửi/nhận messages
- Tích hợp chatbot với n8n (session management, giới hạn 5 requests/session)
- Real-time messaging qua WebSocket

---

## Authentication

Tất cả API endpoints (trừ public endpoints) yêu cầu **JWT Bearer Token** trong header:

```dart
headers: {
  'Authorization': 'Bearer YOUR_JWT_TOKEN',
  'Content-Type': 'application/json',
}
```

**Lưu ý:** Token có thể lấy từ endpoint login: `POST /api/v1/auth/login`

---

## Base URL

```
Development: http://localhost:8100
Production: https://api.fix4home.com
```

---

## Conversation APIs

### 1. Lấy danh sách conversations

**Endpoint:** `GET /api/v1/chat/conversations`

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Conversations retrieved successfully",
  "data": [
    {
      "id": 1,
      "status": "ACTIVE",
      "conversationType": "CHATBOT",
      "lastMessageAt": "2025-01-15T10:30:00",
      "createdAt": "2025-01-15T09:00:00",
      "updatedAt": "2025-01-15T10:30:00",
      "serviceRequestId": null,
      "servicePostId": null,
      "consultationId": null,
      "customer": {
        "userId": 1,
        "username": "user123",
        "fullName": "Nguyen Van A",
        "email": "user@example.com",
        "phoneNumber": "0123456789",
        "isOnline": false
      },
      "technician": {
        "userId": 2,
        "username": "chatbot_support",
        "fullName": "Chatbot Support",
        "email": "chatbot@fix4home.com",
        "phoneNumber": null,
        "isOnline": true
      },
      "lastMessage": {
        "id": 100,
        "content": "Hello, how can I help you?",
        "messageType": "TEXT",
        "senderName": "chatbot_support",
        "sentAt": "2025-01-15T10:30:00",
        "isRead": false
      },
      "unreadCount": 2
    }
  ],
  "timestamp": "2025-01-15T10:35:00"
}
```

**Flutter Example:**
```dart
Future<List<ConversationDTO>> getConversations() async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chat/conversations'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    return (apiResponse.data as List)
        .map((e) => ConversationDTO.fromJson(e))
        .toList();
  } else {
    throw Exception('Failed to load conversations');
  }
}
```

---

### 2. Lấy conversations với pagination

**Endpoint:** `GET /api/v1/chat/conversations/paginated`

**Query Parameters:**
- `page` (int, default: 0) - Page number (0-based)
- `size` (int, default: 10) - Page size

**Example:**
```dart
Future<Page<ConversationDTO>> getConversationsPaginated({
  int page = 0,
  int size = 10,
}) async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chat/conversations/paginated?page=$page&size=$size'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    return Page.fromJson(apiResponse.data);
  } else {
    throw Exception('Failed to load conversations');
  }
}
```

---

### 3. Lấy chi tiết conversation

**Endpoint:** `GET /api/v1/chat/conversations/{id}`

**Example:**
```dart
Future<ConversationDTO> getConversationById(int id) async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chat/conversations/$id'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    return ConversationDTO.fromJson(apiResponse.data);
  } else {
    throw Exception('Failed to load conversation');
  }
}
```

---

### 4. Tìm hoặc tạo conversation với user

**Endpoint:** `POST /api/v1/chat/conversations/find-or-create?otherUserId={userId}`

**Query Parameters:**
- `otherUserId` (Long, required) - ID của user cần chat

**Example:**
```dart
Future<ConversationDTO> findOrCreateConversation(int otherUserId) async {
  final response = await http.post(
    Uri.parse('$baseUrl/api/v1/chat/conversations/find-or-create?otherUserId=$otherUserId'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    return ConversationDTO.fromJson(apiResponse.data);
  } else {
    throw Exception('Failed to find or create conversation');
  }
}
```

**Lưu ý:** Để tìm conversation với chatbot, truyền userId của chatbot user. Thông thường chatbot conversation đã được tạo tự động khi user đăng ký.

---

### 5. Archive conversation

**Endpoint:** `PUT /api/v1/chat/conversations/{id}/archive`

**Example:**
```dart
Future<void> archiveConversation(int conversationId) async {
  final response = await http.put(
    Uri.parse('$baseUrl/api/v1/chat/conversations/$conversationId/archive'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode != 200) {
    throw Exception('Failed to archive conversation');
  }
}
```

---

## Message APIs

### 1. Lấy messages trong conversation

**Endpoint:** `GET /api/v1/chat/conversations/{conversationId}/messages`

**Query Parameters:**
- `page` (int, default: 0) - Page number (0-based)
- `size` (int, default: 20) - Page size

**Response:**
```json
{
  "success": true,
  "message": "Messages retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "conversationId": 1,
        "content": "Hello!",
        "messageType": "TEXT",
        "attachmentUrl": null,
        "metadata": null,
        "isRead": true,
        "sentAt": "2025-01-15T10:00:00",
        "sender": {
          "userId": 1,
          "username": "user123",
          "fullName": "Nguyen Van A",
          "role": "CUSTOMER"
        }
      },
      {
        "id": 2,
        "conversationId": 1,
        "content": "Hi there! How can I help you?",
        "messageType": "TEXT",
        "attachmentUrl": null,
        "metadata": null,
        "isRead": false,
        "sentAt": "2025-01-15T10:01:00",
        "sender": {
          "userId": 2,
          "username": "chatbot_support",
          "fullName": "Chatbot Support",
          "role": "ADMIN"
        }
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "size": 20,
    "number": 0
  },
  "timestamp": "2025-01-15T10:35:00"
}
```

**Flutter Example:**
```dart
Future<Page<MessageDTO>> getMessages({
  required int conversationId,
  int page = 0,
  int size = 20,
}) async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chat/conversations/$conversationId/messages?page=$page&size=$size'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    return Page.fromJson(apiResponse.data);
  } else {
    throw Exception('Failed to load messages');
  }
}
```

---

### 2. Lấy messages trước một thời điểm (Infinite Scroll)

**Endpoint:** `GET /api/v1/chat/conversations/{conversationId}/messages/before`

**Query Parameters:**
- `beforeDate` (DateTime, required) - ISO 8601 format: "2025-01-15T10:30:00"
- `size` (int, default: 20) - Number of messages to load

**Example:**
```dart
Future<Page<MessageDTO>> getMessagesBefore({
  required int conversationId,
  required DateTime beforeDate,
  int size = 20,
}) async {
  final dateStr = beforeDate.toIso8601String();
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chat/conversations/$conversationId/messages/before?beforeDate=$dateStr&size=$size'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    return Page.fromJson(apiResponse.data);
  } else {
    throw Exception('Failed to load messages');
  }
}
```

---

### 3. Đánh dấu messages đã đọc

**Endpoint:** `POST /api/v1/chat/messages/mark-read`

**Request Body:**
```json
{
  "conversationId": 1,
  "messageIds": [1, 2, 3]  // Optional: specific message IDs, null để mark all
}
```

**Example:**
```dart
Future<void> markMessagesAsRead({
  required int conversationId,
  List<int>? messageIds,
}) async {
  final response = await http.post(
    Uri.parse('$baseUrl/api/v1/chat/messages/mark-read'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
    body: jsonEncode({
      'conversationId': conversationId,
      if (messageIds != null) 'messageIds': messageIds,
    }),
  );
  
  if (response.statusCode != 200) {
    throw Exception('Failed to mark messages as read');
  }
}
```

---

### 4. Lấy unread messages

**Endpoint:** `GET /api/v1/chat/messages/unread`

**Example:**
```dart
Future<List<MessageDTO>> getUnreadMessages() async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chat/messages/unread'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    return (apiResponse.data as List)
        .map((e) => MessageDTO.fromJson(e))
        .toList();
  } else {
    throw Exception('Failed to load unread messages');
  }
}
```

---

## Chatbot APIs

### 1. Gửi message đến chatbot (với session management)

**Endpoint:** `POST /api/v1/chatbot/send`

**Request Body:**
```json
{
  "conversationId": 1,
  "message": "Hello chatbot!",
  "sessionId": "7ddbb723-2fc3-46d7-acf5-d2c505424025"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Message sent to chatbot successfully",
  "data": {
    "sessionId": "7ddbb723-2fc3-46d7-acf5-d2c505424025",
    "messageId": 100,
    "messageSent": true,
    "newSessionCreated": false
  },
  "timestamp": "2025-01-15T10:35:00"
}
```

**Flutter Example:**
```dart
class ChatbotService {
  // Lưu sessionId vào SharedPreferences hoặc state management
  Future<String> getSessionId() async {
    final prefs = await SharedPreferences.getInstance();
    String? sessionId = prefs.getString('chatbot_session_id');
    if (sessionId == null) {
      sessionId = const Uuid().v4(); // Generate UUID
      await prefs.setString('chatbot_session_id', sessionId);
    }
    return sessionId;
  }
  
  Future<ChatbotSendResponse> sendMessageToChatbot({
    required int conversationId,
    required String message,
  }) async {
    // Lấy sessionId từ storage
    String sessionId = await getSessionId();
    
    final response = await http.post(
      Uri.parse('$baseUrl/api/v1/chatbot/send'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'conversationId': conversationId,
        'message': message,
        'sessionId': sessionId,
      }),
    );
    
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final apiResponse = ApiResponse.fromJson(json);
      final result = ChatbotSendResponse.fromJson(apiResponse.data);
      
      // Nếu sessionId mới được tạo (do vượt quá 5 requests), lưu lại
      if (result.newSessionCreated == true) {
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('chatbot_session_id', result.sessionId);
      }
      
      return result;
    } else {
      throw Exception('Failed to send message to chatbot');
    }
  }
}
```

**Lưu ý quan trọng:**
- Mỗi session chỉ được gửi **tối đa 5 lần**
- Khi vượt quá 5 lần, backend sẽ tự động tạo sessionId mới
- `newSessionCreated = true` nghĩa là sessionId đã được thay đổi
- **Phải lưu sessionId mới** vào storage để tiếp tục chat
- SessionId được dùng để lưu trữ context trong n8n node memory

---

## WebSocket APIs

WebSocket được sử dụng cho **real-time messaging**. Khi có message mới, server sẽ push qua WebSocket.

### Connection

**Endpoint:** `ws://localhost:8100/ws` hoặc `ws://localhost:8100/ws-native`

**Protocol:** STOMP over WebSocket

**Authentication:** JWT token trong header khi connect

### Flutter Implementation với `stomp_dart_client`

**1. Install package:**
```yaml
dependencies:
  stomp_dart_client: ^1.0.0
  web_socket_channel: ^2.4.0
```

**2. Connection Setup:**
```dart
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class WebSocketService {
  StompClient? stompClient;
  String? token;
  
  void connect(String wsUrl, String jwtToken) {
    this.token = jwtToken;
    
    stompClient = StompClient(
      config: StompConfig(
        url: wsUrl,
        onConnect: onConnect,
        onStompError: onError,
        onWebSocketError: onError,
        reconnectDelay: const Duration(seconds: 5),
        connectionTimeout: const Duration(seconds: 10),
        stompConnectHeaders: {
          'Authorization': 'Bearer $jwtToken',
        },
        webSocketConnectHeaders: {
          'Authorization': 'Bearer $jwtToken',
        },
        beforeConnect: () async {
          await Future.delayed(const Duration(milliseconds: 300));
        },
      ),
    );
    
    stompClient!.activate();
  }
  
  void onConnect(StompFrame frame) {
    print('WebSocket connected');
    // Subscribe to messages
    subscribeToMessages();
  }
  
  void onError(StompFrame frame) {
    print('WebSocket error: ${frame.body}');
  }
  
  // Subscribe to receive messages
  void subscribeToMessages() {
    // Subscribe to user-specific messages
    stompClient!.subscribe(
      destination: '/user/queue/messages',
      callback: (frame) {
        final messageJson = jsonDecode(frame.body!);
        final message = MessageDTO.fromJson(messageJson);
        // Handle received message
        _onMessageReceived(message);
      },
    );
    
    // Subscribe to read status updates
    stompClient!.subscribe(
      destination: '/user/queue/read-status',
      callback: (frame) {
        final statusJson = jsonDecode(frame.body!);
        // Handle read status update
        _onReadStatusUpdate(statusJson);
      },
    );
  }
  
  // Send message via WebSocket (optional, có thể dùng REST API thay thế)
  void sendMessage({
    required int conversationId,
    required String content,
  }) {
    stompClient!.send(
      destination: '/app/chat.send',
      body: jsonEncode({
        'conversationId': conversationId,
        'content': content,
        'messageType': 'TEXT',
      }),
    );
  }
  
  // Send typing indicator
  void sendTypingIndicator({
    required int conversationId,
  }) {
    stompClient!.send(
      destination: '/app/chat.typing',
      body: jsonEncode({
        'conversationId': conversationId,
      }),
    );
  }
  
  // Subscribe to typing indicators
  void subscribeToTypingIndicator(int conversationId, Function(Map) callback) {
    stompClient!.subscribe(
      destination: '/topic/conversation/$conversationId/typing',
      callback: (frame) {
        final indicator = jsonDecode(frame.body!);
        callback(indicator);
      },
    );
  }
  
  void disconnect() {
    stompClient?.deactivate();
  }
}
```

**3. Sử dụng:**
```dart
final wsService = WebSocketService();
wsService.connect('ws://localhost:8100/ws-native', jwtToken);

// Lắng nghe messages
wsService.onMessageReceived = (message) {
  // Update UI với message mới
  setState(() {
    messages.add(message);
  });
};
```

---

## Data Models

### ConversationDTO

```dart
class ConversationDTO {
  final int id;
  final String status; // ACTIVE, ARCHIVED, BLOCKED
  final String conversationType; // BUSINESS, FREE, CHATBOT
  final DateTime? lastMessageAt;
  final DateTime createdAt;
  final DateTime updatedAt;
  final int? serviceRequestId;
  final int? servicePostId;
  final int? consultationId;
  final ParticipantDTO customer;
  final ParticipantDTO technician;
  final MessagePreviewDTO? lastMessage;
  final int unreadCount;
  
  ConversationDTO({
    required this.id,
    required this.status,
    required this.conversationType,
    this.lastMessageAt,
    required this.createdAt,
    required this.updatedAt,
    this.serviceRequestId,
    this.servicePostId,
    this.consultationId,
    required this.customer,
    required this.technician,
    this.lastMessage,
    required this.unreadCount,
  });
  
  factory ConversationDTO.fromJson(Map<String, dynamic> json) {
    return ConversationDTO(
      id: json['id'],
      status: json['status'],
      conversationType: json['conversationType'],
      lastMessageAt: json['lastMessageAt'] != null 
          ? DateTime.parse(json['lastMessageAt']) 
          : null,
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
      serviceRequestId: json['serviceRequestId'],
      servicePostId: json['servicePostId'],
      consultationId: json['consultationId'],
      customer: ParticipantDTO.fromJson(json['customer']),
      technician: ParticipantDTO.fromJson(json['technician']),
      lastMessage: json['lastMessage'] != null 
          ? MessagePreviewDTO.fromJson(json['lastMessage']) 
          : null,
      unreadCount: json['unreadCount'] ?? 0,
    );
  }
}

class ParticipantDTO {
  final int userId;
  final String username;
  final String fullName;
  final String email;
  final String? phoneNumber;
  final bool isOnline;
  
  ParticipantDTO({
    required this.userId,
    required this.username,
    required this.fullName,
    required this.email,
    this.phoneNumber,
    required this.isOnline,
  });
  
  factory ParticipantDTO.fromJson(Map<String, dynamic> json) {
    return ParticipantDTO(
      userId: json['userId'],
      username: json['username'],
      fullName: json['fullName'],
      email: json['email'],
      phoneNumber: json['phoneNumber'],
      isOnline: json['isOnline'] ?? false,
    );
  }
}

class MessagePreviewDTO {
  final int id;
  final String content;
  final String messageType;
  final String senderName;
  final DateTime sentAt;
  final bool isRead;
  
  MessagePreviewDTO({
    required this.id,
    required this.content,
    required this.messageType,
    required this.senderName,
    required this.sentAt,
    required this.isRead,
  });
  
  factory MessagePreviewDTO.fromJson(Map<String, dynamic> json) {
    return MessagePreviewDTO(
      id: json['id'],
      content: json['content'],
      messageType: json['messageType'],
      senderName: json['senderName'],
      sentAt: DateTime.parse(json['sentAt']),
      isRead: json['isRead'],
    );
  }
}
```

### MessageDTO

```dart
class MessageDTO {
  final int id;
  final int conversationId;
  final String content;
  final String messageType; // TEXT, IMAGE, LOCATION, SYSTEM, QUOTATION, FILE
  final String? attachmentUrl;
  final String? metadata;
  final bool isRead;
  final DateTime sentAt;
  final SenderDTO? sender;
  
  MessageDTO({
    required this.id,
    required this.conversationId,
    required this.content,
    required this.messageType,
    this.attachmentUrl,
    this.metadata,
    required this.isRead,
    required this.sentAt,
    this.sender,
  });
  
  factory MessageDTO.fromJson(Map<String, dynamic> json) {
    return MessageDTO(
      id: json['id'],
      conversationId: json['conversationId'],
      content: json['content'],
      messageType: json['messageType'],
      attachmentUrl: json['attachmentUrl'],
      metadata: json['metadata'],
      isRead: json['isRead'] ?? false,
      sentAt: DateTime.parse(json['sentAt']),
      sender: json['sender'] != null 
          ? SenderDTO.fromJson(json['sender']) 
          : null,
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'conversationId': conversationId,
      'content': content,
      'messageType': messageType,
      'attachmentUrl': attachmentUrl,
      'metadata': metadata,
      'isRead': isRead,
      'sentAt': sentAt.toIso8601String(),
      'sender': sender?.toJson(),
    };
  }
}

class SenderDTO {
  final int userId;
  final String username;
  final String fullName;
  final String role;
  
  SenderDTO({
    required this.userId,
    required this.username,
    required this.fullName,
    required this.role,
  });
  
  factory SenderDTO.fromJson(Map<String, dynamic> json) {
    return SenderDTO(
      userId: json['userId'],
      username: json['username'],
      fullName: json['fullName'],
      role: json['role'],
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'userId': userId,
      'username': username,
      'fullName': fullName,
      'role': role,
    };
  }
}
```

### ChatbotSendResponse

```dart
class ChatbotSendResponse {
  final String sessionId;
  final int messageId;
  final bool messageSent;
  final bool newSessionCreated;
  
  ChatbotSendResponse({
    required this.sessionId,
    required this.messageId,
    required this.messageSent,
    required this.newSessionCreated,
  });
  
  factory ChatbotSendResponse.fromJson(Map<String, dynamic> json) {
    return ChatbotSendResponse(
      sessionId: json['sessionId'],
      messageId: json['messageId'],
      messageSent: json['messageSent'] ?? false,
      newSessionCreated: json['newSessionCreated'] ?? false,
    );
  }
}
```

### ApiResponse

```dart
class ApiResponse<T> {
  final bool success;
  final String message;
  final T? data;
  final DateTime timestamp;
  
  ApiResponse({
    required this.success,
    required this.message,
    this.data,
    required this.timestamp,
  });
  
  factory ApiResponse.fromJson(Map<String, dynamic> json) {
    return ApiResponse(
      success: json['success'] ?? false,
      message: json['message'] ?? '',
      data: json['data'],
      timestamp: DateTime.parse(json['timestamp']),
    );
  }
}
```

### Page (cho pagination)

```dart
class Page<T> {
  final List<T> content;
  final int totalElements;
  final int totalPages;
  final int size;
  final int number;
  final bool first;
  final bool last;
  
  Page({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.size,
    required this.number,
    required this.first,
    required this.last,
  });
  
  factory Page.fromJson(Map<String, dynamic> json, T Function(Map) fromJsonT) {
    return Page(
      content: (json['content'] as List)
          .map((e) => fromJsonT(e as Map<String, dynamic>))
          .toList(),
      totalElements: json['totalElements'],
      totalPages: json['totalPages'],
      size: json['size'],
      number: json['number'],
      first: json['first'] ?? false,
      last: json['last'] ?? false,
    );
  }
}
```

---

## Flutter Implementation Examples

### Complete Chat Screen Example

```dart
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class ChatScreen extends StatefulWidget {
  final int conversationId;
  final ConversationDTO conversation;
  
  const ChatScreen({
    Key? key,
    required this.conversationId,
    required this.conversation,
  }) : super(key: key);
  
  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  List<MessageDTO> _messages = [];
  bool _isLoading = false;
  bool _isLoadingMore = false;
  bool _hasMore = true;
  String? _sessionId; // For chatbot
  WebSocketService? _wsService;
  
  @override
  void initState() {
    super.initState();
    _loadMessages();
    _initWebSocket();
    _loadSessionId();
  }
  
  Future<void> _loadSessionId() async {
    final prefs = await SharedPreferences.getInstance();
    String? sessionId = prefs.getString('chatbot_session_id');
    if (sessionId == null) {
      sessionId = const Uuid().v4();
      await prefs.setString('chatbot_session_id', sessionId);
    }
    setState(() {
      _sessionId = sessionId;
    });
  }
  
  void _initWebSocket() {
    // Get token from storage
    final token = 'YOUR_JWT_TOKEN'; // Lấy từ storage
    _wsService = WebSocketService();
    _wsService!.connect('ws://localhost:8100/ws-native', token);
    
    // Listen to messages
    _wsService!.onMessageReceived = (message) {
      if (message.conversationId == widget.conversationId) {
        setState(() {
          _messages.insert(0, message);
        });
        _scrollToBottom();
      }
    };
  }
  
  Future<void> _loadMessages({bool loadMore = false}) async {
    if (loadMore) {
      if (!_hasMore || _isLoadingMore) return;
      setState(() => _isLoadingMore = true);
    } else {
      setState(() => _isLoading = true);
    }
    
    try {
      DateTime? beforeDate;
      if (loadMore && _messages.isNotEmpty) {
        beforeDate = _messages.last.sentAt;
      }
      
      Page<MessageDTO> page;
      if (loadMore && beforeDate != null) {
        page = await getMessagesBefore(
          conversationId: widget.conversationId,
          beforeDate: beforeDate,
        );
      } else {
        page = await getMessages(conversationId: widget.conversationId);
      }
      
      setState(() {
        if (loadMore) {
          _messages.addAll(page.content);
        } else {
          _messages = page.content.reversed.toList(); // Reverse để hiển thị từ cũ đến mới
        }
        _hasMore = !page.last;
        _isLoading = false;
        _isLoadingMore = false;
      });
      
      if (!loadMore) {
        _scrollToBottom();
        _markAsRead();
      }
    } catch (e) {
      setState(() {
        _isLoading = false;
        _isLoadingMore = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading messages: $e')),
      );
    }
  }
  
  Future<void> _sendMessage() async {
    final content = _messageController.text.trim();
    if (content.isEmpty) return;
    
    _messageController.clear();
    
    // Check if it's chatbot conversation
    final isChatbot = widget.conversation.conversationType == 'CHATBOT';
    
    try {
      if (isChatbot && _sessionId != null) {
        // Send to chatbot API
        final response = await sendMessageToChatbot(
          conversationId: widget.conversationId,
          message: content,
          sessionId: _sessionId!,
        );
        
        // Update sessionId if new one was created
        if (response.newSessionCreated) {
          final prefs = await SharedPreferences.getInstance();
          await prefs.setString('chatbot_session_id', response.sessionId);
          setState(() {
            _sessionId = response.sessionId;
          });
        }
      } else {
        // Send normal message via WebSocket or REST API
        _wsService?.sendMessage(
          conversationId: widget.conversationId,
          content: content,
        );
      }
      
      // Reload messages to get the new one
      await _loadMessages();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error sending message: $e')),
      );
    }
  }
  
  Future<void> _markAsRead() async {
    try {
      await markMessagesAsRead(conversationId: widget.conversationId);
    } catch (e) {
      // Silent fail
    }
  }
  
  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          0,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.conversation.technician.fullName),
      ),
      body: Column(
        children: [
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : ListView.builder(
                    controller: _scrollController,
                    reverse: true,
                    itemCount: _messages.length + (_hasMore ? 1 : 0),
                    itemBuilder: (context, index) {
                      if (index == _messages.length) {
                        return _isLoadingMore
                            ? const Center(child: Padding(
                                padding: EdgeInsets.all(8.0),
                                child: CircularProgressIndicator(),
                              ))
                            : Center(
                                child: TextButton(
                                  onPressed: () => _loadMessages(loadMore: true),
                                  child: const Text('Load more'),
                                ),
                              );
                      }
                      
                      final message = _messages[index];
                      final isMe = message.sender?.userId == currentUserId;
                      
                      return MessageBubble(
                        message: message,
                        isMe: isMe,
                      );
                    },
                  ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _messageController,
                    decoration: const InputDecoration(
                      hintText: 'Type a message...',
                      border: OutlineInputBorder(),
                    ),
                    maxLines: null,
                    textInputAction: TextInputAction.send,
                    onSubmitted: (_) => _sendMessage(),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton(
                  icon: const Icon(Icons.send),
                  onPressed: _sendMessage,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
  
  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    _wsService?.disconnect();
    super.dispose();
  }
}
```

---

## Error Handling

### Common Error Codes

- **401 Unauthorized**: Token không hợp lệ hoặc hết hạn
- **403 Forbidden**: Không có quyền truy cập
- **404 Not Found**: Conversation hoặc message không tồn tại
- **400 Bad Request**: Request body không hợp lệ
- **500 Internal Server Error**: Lỗi server

### Error Response Format

```json
{
  "success": false,
  "message": "Error message",
  "timestamp": "2025-01-15T10:35:00"
}
```

---

## Best Practices

1. **Session Management cho Chatbot:**
   - Luôn lưu sessionId vào SharedPreferences
   - Kiểm tra `newSessionCreated` và update sessionId nếu cần
   - SessionId nên là UUID format

2. **WebSocket Connection:**
   - Reconnect tự động khi mất kết nối
   - Subscribe lại các topics sau khi reconnect
   - Disconnect khi app đi vào background (tuỳ chọn)

3. **Message Loading:**
   - Sử dụng pagination để load messages
   - Implement infinite scroll với `getMessagesBefore`
   - Cache messages locally để giảm số lượng API calls

4. **Read Status:**
   - Mark messages as read khi user xem conversation
   - Có thể mark all hoặc mark specific messages

5. **Real-time Updates:**
   - Sử dụng WebSocket để nhận messages mới
   - Update UI ngay khi nhận được message qua WebSocket
   - Sync với server khi cần (pull-to-refresh)

---

## Testing

### Test Endpoints với cURL

```bash
# Get conversations
curl -X GET "http://localhost:8100/api/v1/chat/conversations" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Send message to chatbot
curl -X POST "http://localhost:8100/api/v1/chatbot/send" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": 1,
    "message": "Hello chatbot!",
    "sessionId": "7ddbb723-2fc3-46d7-acf5-d2c505424025"
  }'
```

---

## Support

Nếu có thắc mắc hoặc gặp vấn đề, vui lòng liên hệ development team hoặc check logs trong backend.




