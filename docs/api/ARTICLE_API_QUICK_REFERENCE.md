## Article API - Quick Reference

### Public Endpoints (Frontend - Không cần authentication)

#### 1. Lấy danh sách bài viết đã publish
```
GET /api/v1/articles?page=0&limit=10
```

#### 2. Tìm kiếm bài viết
```
GET /api/v1/articles/search?keyword=sửa chữa&page=0&limit=10
```

#### 3. Lấy chi tiết bài viết theo ID
```
GET /api/v1/articles/{id}
```

#### 4. Lấy chi tiết bài viết theo slug
```
GET /api/v1/articles/slug/{slug}
```

---

### Admin Endpoints (Cần authentication)

#### 1. Tạo bài viết mới
```
POST /api/v1/articles
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "title": "Tiêu đề",
  "shortDescription": "Mô tả ngắn",
  "slug": "tieu-de",
  "content": {
    "type": "structured",
    "blocks": [...]
  }
}
```

#### 2. Cập nhật bài viết
```
PUT /api/v1/articles/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "title": "Tiêu đề mới",
  ...
}
```

#### 3. Publish bài viết
```
POST /api/v1/articles/{id}/publish
Authorization: Bearer <admin_token>
```

#### 4. Unpublish bài viết
```
POST /api/v1/articles/{id}/unpublish
Authorization: Bearer <admin_token>
```

#### 5. Xóa bài viết
```
DELETE /api/v1/articles/{id}
Authorization: Bearer <admin_token>
```

#### 6. Lấy danh sách tất cả bài viết (Admin)
```
GET /api/v1/articles/admin?status=PUBLISHED&page=0&limit=10
Authorization: Bearer <admin_token>
```

#### 7. Xem chi tiết bài viết (Admin)
```
GET /api/v1/articles/admin/{id}
Authorization: Bearer <admin_token>
```

---

### Response Structure

#### ArticleListResponse
```json
{
  "success": true,
  "message": "...",
  "data": {
    "articles": [...],
    "total": 50,
    "page": 0,
    "limit": 10,
    "totalPages": 5
  }
}
```

#### ArticleDTO
```json
{
  "id": 1,
  "title": "...",
  "shortDescription": "...",
  "slug": "...",
  "content": {...},
  "heroImage": {...},
  "status": "PUBLISHED",
  "sections": [...],
  "contactInfo": {...},
  "metadata": {...},
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-01-15T10:00:00",
  "publishedAt": "2025-01-15T11:00:00"
}
```

---

### ArticleStatus Values
- `DRAFT`: Bài viết nháp
- `PUBLISHED`: Bài viết đã publish
- `UNPUBLISHED`: Bài viết đã unpublish

---

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | int | No | 0 | Số trang (0-based) |
| limit | int | No | 10 | Số bản ghi mỗi trang (max: 100) |
| status | string | No | - | Lọc theo status (DRAFT, PUBLISHED, UNPUBLISHED) - chỉ dùng cho admin endpoint |
| keyword | string | No | - | Từ khóa tìm kiếm - chỉ dùng cho search endpoint |

---

### Error Codes

- `ARTICLE_NOT_FOUND`: Bài viết không tồn tại
- `ARTICLE_SLUG_ALREADY_EXISTS`: Slug đã tồn tại
- `401 Unauthorized`: Thiếu hoặc token không hợp lệ
- `403 Forbidden`: Không có quyền ADMIN

---

### Ví dụ cURL

#### Lấy danh sách bài viết (Public)
```bash
curl -X GET "http://localhost:8100/api/v1/articles?page=0&limit=10"
```

#### Tìm kiếm bài viết (Public)
```bash
curl -X GET "http://localhost:8100/api/v1/articles/search?keyword=sửa%20chữa&page=0&limit=10"
```

#### Lấy chi tiết theo slug (Public)
```bash
curl -X GET "http://localhost:8100/api/v1/articles/slug/huong-dan-sua-chua-dieu-hoa-tai-nha"
```

#### Tạo bài viết (Admin)
```bash
curl -X POST "http://localhost:8100/api/v1/articles" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tiêu đề bài viết",
    "shortDescription": "Mô tả ngắn",
    "slug": "tieu-de-bai-viet",
    "content": {
      "type": "structured",
      "blocks": [
        {
          "type": "paragraph",
          "content": "Nội dung..."
        }
      ]
    }
  }'
```

#### Publish bài viết (Admin)
```bash
curl -X POST "http://localhost:8100/api/v1/articles/1/publish" \
  -H "Authorization: Bearer <admin_token>"
```

---

Xem chi tiết đầy đủ: [`ARTICLE_API_DOCUMENTATION.md`](./ARTICLE_API_DOCUMENTATION.md)

