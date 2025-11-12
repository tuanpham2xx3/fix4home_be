# Fix4Home Frontend - Activation Page

Trang web đơn giản để xử lý activation token từ email.

## Cách sử dụng

### 1. Chạy trực tiếp (không cần build)

Mở file `activate.html` trực tiếp trong browser hoặc dùng local server:

```bash
# Option 1: Python
cd FRONTEND
python -m http.server 3000

# Option 2: Node.js (nếu có http-server)
npx http-server -p 3000

# Option 3: PHP
php -S localhost:3000
```

Sau đó truy cập: `http://localhost:3000/activate.html?token=YOUR_TOKEN`

### 2. Cấu hình

Mặc định API URL là `http://localhost:8100/api/v1`. 

Nếu backend chạy ở port khác, sửa trong file `activate.html`:
```javascript
const API_BASE_URL = 'http://localhost:8100/api/v1'; // Thay đổi port nếu cần
```

### 3. Test

Test với các trường hợp:
- Valid token: `http://localhost:3000/activate.html?token=valid-token`
- Invalid token: `http://localhost:3000/activate.html?token=invalid`
- No token: `http://localhost:3000/activate.html`

## Flow

1. User click link trong email: `http://localhost:3000/activate.html?token=xxx`
2. Page load → Đọc token từ URL
3. Gọi API: `POST /api/v1/auth/verify-activation-token`
4. Hiển thị kết quả:
   - Success → Auto redirect đến `/login.html` sau 3 giây
   - Error → Hiển thị thông báo lỗi

## Files

- `activate.html` - Trang activation (HTML + CSS + JS inline)

