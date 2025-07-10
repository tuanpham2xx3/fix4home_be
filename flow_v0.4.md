# Chi tiết Flow hệ thống

## 1. CUSTOMER FLOW

### Đăng nhập
- Xác thực: sử dụng access token và cookies

### Đăng tìm dịch vụ
- **Khẩn cấp**:
  - Tìm Technician gần nhất (đang Online)
  - Trao đổi nhanh, nhận báo giá
  - Chốt đơn hoặc tìm người khác

- **Tư vấn**:
  - Đăng bài kèm mô tả, ảnh, chủ đề, địa điểm
  - Chờ Technician tư vấn và báo giá
  - Trao đổi qua chat
  - Chốt đơn, đặt lịch hẹn

### Quản lý bài đăng
- Trạng thái bài:
  - **Pending**: chờ tư vấn
  - **Matched**: đã chọn Technician

### Chốt đơn
- Đặt lịch: thời gian, địa chỉ, khoảng giá, thông tin 2 bên
- Trạng thái đơn hàng:
  - **Pending**: mới tạo
  - **During**: đang thực hiện
  - **Done**: hoàn thành
  - **Cancel**: hủy
  - **Complainting**: đang khiếu nại
  - **Complaited**: khiếu nại đã giải quyết

### Thanh toán & đánh giá
- Thanh toán đơn hàng
- Đánh giá chất lượng dịch vụ

### Khiếu nại & xử lý
- Tạo đơn khiếu nại (lý do, ảnh)
- Nhận và phản hồi khiếu nại
- Chuyển Admin nếu chưa thỏa thuận được

## 2. TECHNICIAN FLOW

### Đăng ký
- Thông tin cá nhân, địa chỉ, chuyên môn, kinh nghiệm, minh chứng
- Chờ duyệt từ Admin

### Quản lý trạng thái
- **Online**: nhận đơn khẩn cấp
- **Offline**: không nhận đơn

### Tìm bài đăng
- Theo phạm vi khu vực, chủ đề, loại đơn

### Tư vấn & Báo giá
- Xem bài đăng, gửi tư vấn, báo giá
- Trao đổi qua chat
- Chỉnh sửa báo giá (nếu có)

### Xử lý đơn hàng
- **Khẩn cấp**: nhận thông báo, phản hồi nhanh
- **Tư vấn**: được chọn bởi Customer
- Chuyển trạng thái: During → Done hoặc Cancel

### Khiếu nại & xử lý
- Nhận thông tin khiếu nại
- Chấp nhận hoặc từ chối khiếu nại, phản hồi (lý do, ảnh)
- Xử lý qua Admin nếu cần

## 3. ADMIN FLOW

### Duyệt Technician
- Kiểm tra thông tin, xác minh và phê duyệt

### Xử lý khiếu nại
- Nhận khiếu nại chưa được giải quyết từ Customer/Technician
- Xử lý và quyết định giải pháp
- Thông báo kết quả xử lý đến Customer và Technician

## 4. HỆ THỐNG XỬ LÝ

### Quản lý bài đăng
- Lọc và hiển thị theo phạm vi và chủ đề
- Lưu trạng thái và cập nhật (Pending, Matched)

### Quản lý đơn hàng
- Thông tin đơn: Customer, Technician, thời gian, giá, địa chỉ
- Lưu và chuyển đổi trạng thái

### Quản lý thanh toán & đánh giá
- Xử lý thanh toán từ Customer
- Lưu và hiển thị đánh giá

### Xử lý khiếu nại
- Tiếp nhận, xử lý sơ bộ
- Chuyển Admin khi không có thỏa thuận
- Thông báo trạng thái xử lý

## API & FE/BE Interaction
- **Customer API**:
  - Đăng nhập, đăng ký
  - Đăng bài, xem tư vấn
  - Chốt đơn, quản lý trạng thái
  - Thanh toán, đánh giá
  - Khiếu nại

- **Technician API**:
  - Đăng ký, quản lý trạng thái online/offline
  - Xem và tư vấn bài đăng
  - Nhận đơn, chuyển trạng thái đơn
  - Xử lý khiếu nại

- **Admin API**:
  - Quản lý và duyệt Technician
  - Xử lý khiếu nại

- **System API**:
  - Quản lý trạng thái bài đăng, đơn hàng
  - Xử lý thông báo và chuyển trạng thái tự động
  - Lưu lịch sử giao dịch, đánh giá, khiếu nại

Dữ liệu FE gọi từ BE:
- Thông tin bài đăng, đơn hàng, tư vấn, trạng thái, thanh toán, đánh giá, khiếu nại
- Tương tác thời gian thực (tin nhắn, thông báo nhanh)

Hoàn thiện API theo các yêu cầu trên sẽ đảm bảo tính rõ ràng và đầy đủ để triển khai Backend và giao tiếp với Frontend.

