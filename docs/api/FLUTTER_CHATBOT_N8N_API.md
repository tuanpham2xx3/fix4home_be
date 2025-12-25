# 🤖 Hướng Dẫn API Chatbot N8N Cho Flutter

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Flutter Implementation](#flutter-implementation)
7. [Error Handling](#error-handling)
8. [Best Practices](#best-practices)

---

## Tổng Quan

Hệ thống Chatbot N8N cung cấp các API để:
- Gửi message đến chatbot và nhận response tự động
- Quản lý session tự động (sessionId 8 số)
- Tự động tạo session mới sau 5 cặp message (user + bot)
- Lưu và truy xuất lịch sử chat theo user
- Lấy lịch sử chat theo session cụ thể

**Đặc điểm:**
- SessionId được tạo tự động (8 số ngẫu nhiên)
- Mỗi session tối đa 5 cặp message (user message + bot response)
- Sau 5 cặp, hệ thống tự động tạo session mới
- Lịch sử chat được lưu vĩnh viễn theo user

---

## Authentication

Tất cả API endpoints yêu cầu **JWT Bearer Token** trong header:

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

## API Endpoints

### 1. Gửi Message Đến Chatbot

**Endpoint:** `POST /api/v1/chatbot/send`

**Mô tả:** Gửi message đến chatbot và nhận response. Session sẽ được tạo tự động nếu chưa có, hoặc sử dụng session hiện tại.

**Request Body:**
```json
{
  "message": "Xin chào, tôi cần tư vấn về sửa chữa điện nước",
  "sessionId": "12345678"  // Optional: 8-digit sessionId (nếu không có sẽ tự tạo)
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Message sent successfully",
  "data": {
    "output": "Xin chào bạn! Tôi là Trợ lý Ảo Chăm sóc Khách hàng của FIX4HOME. Tôi sẵn sàng giúp đỡ bạn về dịch vụ sửa chữa điện nước, điện lạnh, cơ khí tại nhà. Bạn cần giúp đỡ gì hôm nay?",
    "sessionId": "12345678",
    "messageCount": 1,
    "newSession": false,
    "timestamp": "2024-12-24T10:30:00"
  },
  "timestamp": "2024-12-24T10:30:00"
}
```

**Flutter Example:**
```dart
Future<ChatbotMessageResponse> sendMessageToChatbot({
  required String message,
  String? sessionId,
}) async {
  final token = await getAuthToken(); // Lấy token từ storage
  
  final response = await http.post(
    Uri.parse('$baseUrl/api/v1/chatbot/send'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
    body: jsonEncode({
      'message': message,
      if (sessionId != null) 'sessionId': sessionId,
    }),
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    if (apiResponse.data == null) {
      throw Exception('Response data is null');
    }
    return ChatbotMessageResponse.fromJson(apiResponse.data as Map<String, dynamic>);
  } else {
    final errorJson = jsonDecode(response.body);
    throw Exception(errorJson['message'] ?? 'Failed to send message');
  }
}
```

**Lưu ý quan trọng:**
- `sessionId` là **optional** - nếu không gửi, backend sẽ tự động tạo hoặc lấy session hiện tại
- `sessionId` là **8 số** (ví dụ: "12345678"), không phải UUID
- Khi `newSession = true`, nghĩa là session mới đã được tạo (do đạt 5 cặp message)
- Nên lưu `sessionId` vào local storage để có thể gửi lại trong request tiếp theo (tùy chọn)

---

### 2. Lấy Lịch Sử Chat (Pagination)

**Endpoint:** `GET /api/v1/chatbot/history`

**Mô tả:** Lấy lịch sử chat của user hiện tại với pagination.

**Query Parameters:**
- `page` (int, optional, default: 0) - Số trang (0-based)
- `size` (int, optional, default: 20) - Số items mỗi trang

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Chat history retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "sessionId": "12345678",
        "message": "Xin chào",
        "messageType": "USER",
        "pairSequence": 1,
        "createdAt": "2024-12-24T10:00:00"
      },
      {
        "id": 2,
        "sessionId": "12345678",
        "message": "Xin chào bạn! Tôi là Trợ lý Ảo...",
        "messageType": "BOT",
        "pairSequence": 1,
        "createdAt": "2024-12-24T10:00:01"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false
  },
  "timestamp": "2024-12-24T10:35:00"
}
```

**Flutter Example:**
```dart
Future<Page<ChatbotHistoryDTO>> getChatHistory({
  int page = 0,
  int size = 20,
}) async {
  final token = await getAuthToken();
  
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chatbot/history?page=$page&size=$size'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    if (apiResponse.data == null) {
      throw Exception('Response data is null');
    }
    return Page.fromJson(apiResponse.data as Map<String, dynamic>, ChatbotHistoryDTO.fromJson);
  } else {
    throw Exception('Failed to load chat history');
  }
}
```

---

### 3. Lấy Lịch Sử Chat Theo Session

**Endpoint:** `GET /api/v1/chatbot/history/{sessionId}`

**Mô tả:** Lấy tất cả messages trong một session cụ thể.

**Path Parameters:**
- `sessionId` (String, required) - Session ID (8 digits)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Session history retrieved successfully",
  "data": [
    {
      "id": 1,
      "sessionId": "12345678",
      "message": "Xin chào",
      "messageType": "USER",
      "pairSequence": 1,
      "createdAt": "2024-12-24T10:00:00"
    },
    {
      "id": 2,
      "sessionId": "12345678",
      "message": "Xin chào bạn!...",
      "messageType": "BOT",
      "pairSequence": 1,
      "createdAt": "2024-12-24T10:00:01"
    }
  ],
  "timestamp": "2024-12-24T10:35:00"
}
```

**Flutter Example:**
```dart
Future<List<ChatbotHistoryDTO>> getSessionHistory(String sessionId) async {
  final token = await getAuthToken();
  
  final response = await http.get(
    Uri.parse('$baseUrl/api/v1/chatbot/history/$sessionId'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    final apiResponse = ApiResponse.fromJson(json);
    if (apiResponse.data == null) {
      throw Exception('Response data is null');
    }
    return (apiResponse.data as List)
        .map((e) => ChatbotHistoryDTO.fromJson(e as Map<String, dynamic>))
        .toList();
  } else {
    throw Exception('Failed to load session history');
  }
}
```

---

## Data Models

### ChatbotMessageResponse

```dart
class ChatbotMessageResponse {
  final String output;           // Bot response message
  final String sessionId;        // Current session ID (8 digits)
  final int messageCount;        // Number of message pairs in session
  final bool newSession;        // Whether new session was created
  final DateTime timestamp;     // Response timestamp
  
  ChatbotMessageResponse({
    required this.output,
    required this.sessionId,
    required this.messageCount,
    required this.newSession,
    required this.timestamp,
  });
  
  factory ChatbotMessageResponse.fromJson(Map<String, dynamic> json) {
    return ChatbotMessageResponse(
      output: json['output'] as String? ?? '',
      sessionId: json['sessionId'] as String? ?? '',
      messageCount: json['messageCount'] as int? ?? 0,
      newSession: json['newSession'] as bool? ?? false,
      timestamp: DateTime.parse(json['timestamp'] as String),
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'output': output,
      'sessionId': sessionId,
      'messageCount': messageCount,
      'newSession': newSession,
      'timestamp': timestamp.toIso8601String(),
    };
  }
}
```

### ChatbotHistoryDTO

```dart
class ChatbotHistoryDTO {
  final int id;
  final String sessionId;       // Session ID (8 digits)
  final String message;         // Message content
  final String messageType;     // "USER" or "BOT"
  final int pairSequence;       // Sequence number of message pair
  final DateTime createdAt;     // When message was created
  
  ChatbotHistoryDTO({
    required this.id,
    required this.sessionId,
    required this.message,
    required this.messageType,
    required this.pairSequence,
    required this.createdAt,
  });
  
  factory ChatbotHistoryDTO.fromJson(Map<String, dynamic> json) {
    return ChatbotHistoryDTO(
      id: json['id'] as int? ?? 0,
      sessionId: json['sessionId'] as String? ?? '',
      message: json['message'] as String? ?? '',
      messageType: json['messageType'] as String? ?? 'USER',
      pairSequence: json['pairSequence'] as int? ?? 0,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'sessionId': sessionId,
      'message': message,
      'messageType': messageType,
      'pairSequence': pairSequence,
      'createdAt': createdAt.toIso8601String(),
    };
  }
  
  bool get isUserMessage => messageType == 'USER';
  bool get isBotMessage => messageType == 'BOT';
}
```

### SendChatbotMessageRequest

```dart
class SendChatbotMessageRequest {
  final String message;         // User message (required)
  final String? sessionId;      // Optional: 8-digit sessionId
  
  SendChatbotMessageRequest({
    required this.message,
    this.sessionId,
  });
  
  Map<String, dynamic> toJson() {
    return {
      'message': message,
      if (sessionId != null) 'sessionId': sessionId,
    };
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

### Page (Pagination)

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
  
  factory Page.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) {
    return Page(
      content: (json['content'] as List? ?? [])
          .map((e) => fromJsonT(e as Map<String, dynamic>))
          .toList(),
      totalElements: json['totalElements'] as int? ?? 0,
      totalPages: json['totalPages'] as int? ?? 0,
      size: json['size'] as int? ?? 20,
      number: json['number'] as int? ?? 0,
      first: json['first'] as bool? ?? false,
      last: json['last'] as bool? ?? false,
    );
  }
}
```

---

## Flutter Implementation

### Complete Chatbot Service

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class ChatbotService {
  final String baseUrl;
  final Future<String> Function() getAuthToken;
  
  ChatbotService({
    required this.baseUrl,
    required this.getAuthToken,
  });
  
  /// Gửi message đến chatbot
  Future<ChatbotMessageResponse> sendMessage({
    required String message,
    String? sessionId,
  }) async {
    final token = await getAuthToken();
    
    final response = await http.post(
      Uri.parse('$baseUrl/api/v1/chatbot/send'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'message': message,
        if (sessionId != null) 'sessionId': sessionId,
      }),
    );
    
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final apiResponse = ApiResponse.fromJson(json);
      if (apiResponse.data == null) {
        throw Exception('Response data is null');
      }
      return ChatbotMessageResponse.fromJson(apiResponse.data as Map<String, dynamic>);
    } else {
      final errorJson = jsonDecode(response.body);
      throw Exception(errorJson['message'] ?? 'Failed to send message');
    }
  }
  
  /// Lấy lịch sử chat với pagination
  Future<Page<ChatbotHistoryDTO>> getChatHistory({
    int page = 0,
    int size = 20,
  }) async {
    final token = await getAuthToken();
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/v1/chatbot/history?page=$page&size=$size'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final apiResponse = ApiResponse.fromJson(json);
      if (apiResponse.data == null) {
        throw Exception('Response data is null');
      }
      return Page.fromJson(apiResponse.data as Map<String, dynamic>, ChatbotHistoryDTO.fromJson);
    } else {
      throw Exception('Failed to load chat history');
    }
  }
  
  /// Lấy lịch sử chat theo session
  Future<List<ChatbotHistoryDTO>> getSessionHistory(String sessionId) async {
    final token = await getAuthToken();
    
    final response = await http.get(
      Uri.parse('$baseUrl/api/v1/chatbot/history/$sessionId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );
    
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final apiResponse = ApiResponse.fromJson(json);
      if (apiResponse.data == null) {
        throw Exception('Response data is null');
      }
      return (apiResponse.data as List)
          .map((e) => ChatbotHistoryDTO.fromJson(e as Map<String, dynamic>))
          .toList();
    } else {
      throw Exception('Failed to load session history');
    }
  }
}
```

### Complete Chatbot Screen Example

```dart
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ChatbotScreen extends StatefulWidget {
  const ChatbotScreen({Key? key}) : super(key: key);
  
  @override
  State<ChatbotScreen> createState() => _ChatbotScreenState();
}

class _ChatbotScreenState extends State<ChatbotScreen> {
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final ChatbotService _chatbotService = ChatbotService(
    baseUrl: 'http://localhost:8100',
    getAuthToken: () async {
      final prefs = await SharedPreferences.getInstance();
      return prefs.getString('auth_token') ?? '';
    },
  );
  
  List<ChatbotHistoryDTO> _messages = [];
  String? _currentSessionId;
  bool _isLoading = false;
  bool _isSending = false;
  
  @override
  void initState() {
    super.initState();
    _loadChatHistory();
  }
  
  Future<void> _loadChatHistory() async {
    setState(() => _isLoading = true);
    try {
      final page = await _chatbotService.getChatHistory(page: 0, size: 50);
      setState(() {
        _messages = page.content.reversed.toList(); // Reverse để hiển thị từ cũ đến mới
        if (_messages.isNotEmpty) {
          _currentSessionId = _messages.first.sessionId;
        }
        _isLoading = false;
      });
      _scrollToBottom();
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error loading history: $e')),
        );
      }
    }
  }
  
  Future<void> _sendMessage() async {
    final message = _messageController.text.trim();
    if (message.isEmpty || _isSending) return;
    
    _messageController.clear();
    setState(() => _isSending = true);
    
    try {
      // Gửi message
      final response = await _chatbotService.sendMessage(
        message: message,
        sessionId: _currentSessionId,
      );
      
      // Update sessionId nếu có session mới
      if (response.newSession) {
        setState(() => _currentSessionId = response.sessionId);
      } else if (_currentSessionId == null) {
        setState(() => _currentSessionId = response.sessionId);
      }
      
      // Thêm user message và bot response vào UI
      setState(() {
        final pairSequence = response.messageCount;
        _messages.add(ChatbotHistoryDTO(
          id: DateTime.now().millisecondsSinceEpoch,
          sessionId: response.sessionId,
          message: message,
          messageType: 'USER',
          pairSequence: pairSequence,
          createdAt: DateTime.now(),
        ));
        _messages.add(ChatbotHistoryDTO(
          id: DateTime.now().millisecondsSinceEpoch + 1,
          sessionId: response.sessionId,
          message: response.output,
          messageType: 'BOT',
          pairSequence: pairSequence,
          createdAt: DateTime.now(),
        ));
        _isSending = false;
      });
      
      _scrollToBottom();
    } catch (e) {
      setState(() => _isSending = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error sending message: $e')),
        );
      }
    }
  }
  
  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
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
        title: const Text('Chatbot Hỗ Trợ'),
        actions: [
          if (_currentSessionId != null)
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Center(
                child: Text(
                  'Session: $_currentSessionId',
                  style: const TextStyle(fontSize: 12),
                ),
              ),
            ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : _messages.isEmpty
                    ? const Center(
                        child: Text('Chưa có tin nhắn nào. Hãy bắt đầu trò chuyện!'),
                      )
                    : ListView.builder(
                        controller: _scrollController,
                        padding: const EdgeInsets.all(16),
                        itemCount: _messages.length,
                        itemBuilder: (context, index) {
                          final message = _messages[index];
                          final isUser = message.isUserMessage;
                          
                          return Align(
                            alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
                            child: Container(
                              margin: const EdgeInsets.only(bottom: 8),
                              padding: const EdgeInsets.symmetric(
                                horizontal: 16,
                                vertical: 10,
                              ),
                              decoration: BoxDecoration(
                                color: isUser ? Colors.blue : Colors.grey[300],
                                borderRadius: BorderRadius.circular(16),
                              ),
                              constraints: BoxConstraints(
                                maxWidth: MediaQuery.of(context).size.width * 0.75,
                              ),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    message.message,
                                    style: TextStyle(
                                      color: isUser ? Colors.white : Colors.black87,
                                    ),
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    _formatTime(message.createdAt),
                                    style: TextStyle(
                                      fontSize: 10,
                                      color: isUser
                                          ? Colors.white70
                                          : Colors.black54,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          );
                        },
                      ),
          ),
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.2),
                  spreadRadius: 1,
                  blurRadius: 5,
                ),
              ],
            ),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _messageController,
                    decoration: const InputDecoration(
                      hintText: 'Nhập tin nhắn...',
                      border: OutlineInputBorder(),
                      contentPadding: EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 10,
                      ),
                    ),
                    maxLines: null,
                    textInputAction: TextInputAction.send,
                    onSubmitted: (_) => _sendMessage(),
                    enabled: !_isSending,
                  ),
                ),
                const SizedBox(width: 8),
                IconButton(
                  icon: _isSending
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.send),
                  onPressed: _isSending ? null : _sendMessage,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
  
  String _formatTime(DateTime dateTime) {
    return '${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  }
  
  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }
}
```

---

## Error Handling

### Common Error Codes

- **401 Unauthorized**: Token không hợp lệ hoặc hết hạn
- **403 Forbidden**: Không có quyền truy cập
- **400 Bad Request**: Request body không hợp lệ (ví dụ: message quá dài)
- **500 Internal Server Error**: Lỗi server hoặc n8n webhook không phản hồi

### Error Response Format

```json
{
  "success": false,
  "message": "Error message",
  "timestamp": "2024-12-24T10:35:00"
}
```

### Error Handling Example

```dart
try {
  final response = await _chatbotService.sendMessage(message: 'Hello');
  // Handle success
} on http.ClientException catch (e) {
  // Network error
  print('Network error: $e');
} on FormatException catch (e) {
  // JSON parsing error
  print('JSON error: $e');
} catch (e) {
  // Other errors
  print('Error: $e');
}
```

---

## Best Practices

1. **Session Management:**
   - `sessionId` là **optional** - backend tự động quản lý
   - Có thể lưu `sessionId` vào local storage để theo dõi (không bắt buộc)
   - Khi `newSession = true`, có thể cập nhật `sessionId` đã lưu

2. **Message History:**
   - Sử dụng pagination để load lịch sử (không load tất cả cùng lúc)
   - Cache messages locally để giảm số lượng API calls
   - Implement pull-to-refresh để reload messages

3. **UI/UX:**
   - Hiển thị loading indicator khi đang gửi message
   - Disable send button khi đang gửi để tránh duplicate
   - Auto-scroll đến message mới nhất
   - Hiển thị timestamp cho mỗi message

4. **Error Handling:**
   - Hiển thị error message thân thiện với user
   - Retry mechanism cho network errors
   - Log errors để debug

5. **Performance:**
   - Debounce cho input nếu cần
   - Lazy load lịch sử khi scroll
   - Cache responses nếu phù hợp

---

## Testing

### Test với cURL

```bash
# Gửi message (không có sessionId)
curl -X POST "http://localhost:8100/api/v1/chatbot/send" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Xin chào"
  }'

# Gửi message (có sessionId)
curl -X POST "http://localhost:8100/api/v1/chatbot/send" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tôi cần tư vấn",
    "sessionId": "12345678"
  }'

# Lấy lịch sử
curl -X GET "http://localhost:8100/api/v1/chatbot/history?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Lấy lịch sử theo session
curl -X GET "http://localhost:8100/api/v1/chatbot/history/12345678" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Support

Nếu có thắc mắc hoặc gặp vấn đề, vui lòng:
1. Kiểm tra logs trong backend
2. Verify JWT token còn hợp lệ
3. Kiểm tra n8n webhook đang hoạt động
4. Liên hệ development team

---

## Changelog

**Version 1.0.0 (2024-12-24):**
- Initial release
- Session management tự động
- Auto-rotate session sau 5 cặp message
- Lịch sử chat với pagination

