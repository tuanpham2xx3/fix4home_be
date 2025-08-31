# Tài liệu API giao tiếp giữa FE và BE

## CUSTOMER API

### Đăng nhập / Đăng ký
- Request:
  - Username, Password
- Response:
  - Access token, user profile

### Đăng bài dịch vụ
- Request:
  - Loại bài (Khẩn cấp / Tư vấn)
  - Mô tả, ảnh, chủ đề, địa điểm
- Response:
  - ID bài đăng, trạng thái (Pending)

### Xem danh sách tư vấn
- Request:
  - ID bài đăng
- Response:
  - Danh sách tư vấn, Technician (tên, khoảng giá, nội dung tư vấn)

### Chốt tư vấn
- Request:
  - ID bài đăng, ID Technician, thời gian, địa chỉ, giá
- Response:
  - ID đơn hàng, trạng thái đơn hàng (Pending)

### Thanh toán & đánh giá
- Request:
  - ID đơn hàng, thông tin thanh toán, nội dung đánh giá, số sao
- Response:
  - Xác nhận thanh toán, cập nhật trạng thái đơn (Done)

### Khiếu nại
- Request:
  - ID đơn hàng, lý do, ảnh
- Response:
  - Xác nhận khiếu nại, trạng thái (Complainting)

## TECHNICIAN API

### Đăng ký Technician
- Request:
  - Thông tin cá nhân, địa chỉ, chuyên môn, kinh nghiệm, minh chứng
- Response:
  - Trạng thái chờ duyệt

### Cập nhật trạng thái
- Request:
  - Trạng thái (Online/Offline)
- Response:
  - Xác nhận trạng thái

### Tìm kiếm bài đăng
- Request:
  - Phạm vi, chủ đề, loại bài đăng
- Response:
  - Danh sách bài đăng phù hợp

### Gửi tư vấn & báo giá
- Request:
  - ID bài đăng, nội dung tư vấn, giá
- Response:
  - Xác nhận đã gửi tư vấn

### Nhận và xử lý đơn hàng
- Request:
  - ID đơn hàng, thao tác (accept/reject/done/cancel)
- Response:
  - Xác nhận và cập nhật trạng thái đơn

### Xử lý khiếu nại
- Request:
  - ID đơn hàng, phản hồi (chấp nhận/từ chối), lý do, ảnh
- Response:
  - Xác nhận phản hồi

## ADMIN API

### Duyệt Technician
- Request:
  - ID Technician, trạng thái duyệt (approve/reject)
- Response:
  - Xác nhận trạng thái Technician

### Xử lý khiếu nại
- Request:
  - ID đơn hàng, quyết định xử lý, mô tả
- Response:
  - Thông báo kết quả cho Customer và Technician, cập nhật trạng thái (Complaited)

## SYSTEM API

### Cập nhật trạng thái bài đăng
- Request:
  - ID bài đăng, trạng thái mới
- Response:
  - Xác nhận cập nhật

### Cập nhật trạng thái đơn hàng
- Request:
  - ID đơn hàng, trạng thái mới
- Response:
  - Xác nhận cập nhật

### Gửi thông báo
- Request:
  - User ID, nội dung thông báo
- Response:
  - Xác nhận gửi thông báo

## Dữ liệu FE gọi từ BE
- Thông tin bài đăng, tư vấn, đơn hàng
- Trạng thái hoạt động (online/offline, pending/matched/during/done)
- Thông tin lịch sử thanh toán, đánh giá, khiếu nại

API được thiết kế rõ ràng, đảm bảo sự rõ ràng và hiệu quả trong quá trình triển khai Frontend và Backend.

