## Article API Documentation

### 1. Tổng quan

- **Mục đích**: Quản lý và truy xuất bài viết tin tức
  - **Public endpoints**: Cho phép frontend lấy danh sách bài viết đã publish, tìm kiếm, xem chi tiết
  - **Admin endpoints**: Cho phép admin tạo, sửa, xóa, publish/unpublish bài viết
- **Yêu cầu quyền**: 
  - **Public endpoints**: Không cần authentication
  - **Admin endpoints**: Tài khoản phải có role **ADMIN**
- **Authentication**: Sử dụng JWT Bearer Token cho admin endpoints:
  - **Header**: `Authorization: Bearer <access_token>`

---

## 2. PUBLIC ENDPOINTS (Cho Frontend)

### 2.1. Lấy danh sách bài viết đã publish

- **Method**: `GET`
- **URL**: `/api/v1/articles`
- **Mô tả**: Lấy danh sách tất cả bài viết đã được publish, có phân trang
- **Authentication**: Không cần

- **Query params**:
  - **page** (optional, int):
    - Số trang, bắt đầu từ `0`
    - Nếu không truyền: mặc định `0`
  - **limit** (optional, int):
    - Số bản ghi mỗi trang
    - Mặc định `10`, tối đa `100`

- **Response 200** (`application/json`):

```json
{
  "success": true,
  "message": "Published articles retrieved successfully",
  "data": {
    "articles": [
      {
        "id": 1,
        "title": "Hướng dẫn sửa chữa điều hòa tại nhà",
        "shortDescription": "Bài viết hướng dẫn cách tự sửa chữa điều hòa đơn giản",
        "slug": "huong-dan-sua-chua-dieu-hoa-tai-nha",
        "content": {
          "type": "structured",
          "blocks": [
            {
              "type": "paragraph",
              "content": "Nội dung bài viết..."
            },
            {
              "type": "heading",
              "level": 2,
              "content": "Các bước thực hiện"
            }
          ]
        },
        "heroImage": {
          "url": "https://example.com/image.jpg",
          "alt": "Hình ảnh minh họa",
          "caption": "Mô tả hình ảnh"
        },
        "metaDescription": "Mô tả SEO",
        "metaKeywords": "sửa chữa, điều hòa, tại nhà",
        "status": "PUBLISHED",
        "sections": [
          {
            "title": "Phần 1",
            "content": "Nội dung phần 1",
            "bulletPoints": ["Điểm 1", "Điểm 2"]
          }
        ],
        "contactInfo": {
          "websiteUrl": "https://fix4home.com",
          "bookingPhone": "0901234567",
          "consultationPhones": ["0901234567", "0901234568"]
        },
        "metadata": {
          "author": "Admin",
          "authorId": 1,
          "createdAt": "2025-01-15T10:00:00",
          "updatedAt": "2025-01-15T10:00:00",
          "publishedAt": "2025-01-15T11:00:00",
          "tags": ["sửa chữa", "điều hòa"],
          "category": "Hướng dẫn"
        },
        "createdAt": "2025-01-15T10:00:00",
        "updatedAt": "2025-01-15T10:00:00",
        "publishedAt": "2025-01-15T11:00:00"
      }
    ],
    "total": 50,
    "page": 0,
    "limit": 10,
    "totalPages": 5
  }
}
```

---

### 2.2. Tìm kiếm bài viết

- **Method**: `GET`
- **URL**: `/api/v1/articles/search`
- **Mô tả**: Tìm kiếm bài viết đã publish theo từ khóa
- **Authentication**: Không cần

- **Query params**:
  - **keyword** (optional, string):
    - Từ khóa tìm kiếm (tìm trong title, shortDescription, content)
  - **page** (optional, int):
    - Số trang, bắt đầu từ `0`
    - Mặc định `0`
  - **limit** (optional, int):
    - Số bản ghi mỗi trang
    - Mặc định `10`, tối đa `100`

- **Response 200** (`application/json`):

```json
{
  "success": true,
  "message": "Articles retrieved successfully",
  "data": {
    "articles": [...],
    "total": 10,
    "page": 0,
    "limit": 10,
    "totalPages": 1
  }
}
```

---

### 2.3. Lấy chi tiết bài viết theo ID

- **Method**: `GET`
- **URL**: `/api/v1/articles/{id}`
- **Mô tả**: Lấy chi tiết một bài viết đã publish theo ID
- **Authentication**: Không cần

- **Path params**:
  - **id** (required, long): ID của bài viết

- **Response 200** (`application/json`):

```json
{
  "success": true,
  "message": "Article details retrieved successfully",
  "data": {
    "id": 1,
    "title": "Hướng dẫn sửa chữa điều hòa tại nhà",
    "shortDescription": "Bài viết hướng dẫn cách tự sửa chữa điều hòa đơn giản",
    "slug": "huong-dan-sua-chua-dieu-hoa-tai-nha",
    "content": {...},
    "heroImage": {...},
    "metaDescription": "Mô tả SEO",
    "metaKeywords": "sửa chữa, điều hòa, tại nhà",
    "status": "PUBLISHED",
    "sections": [...],
    "contactInfo": {...},
    "metadata": {...},
    "createdAt": "2025-01-15T10:00:00",
    "updatedAt": "2025-01-15T10:00:00",
    "publishedAt": "2025-01-15T11:00:00"
  }
}
```

- **Response 404**:
  - Khi bài viết không tồn tại hoặc chưa được publish
  - Message code: `ARTICLE_NOT_FOUND`

---

### 2.4. Lấy chi tiết bài viết theo slug

- **Method**: `GET`
- **URL**: `/api/v1/articles/slug/{slug}`
- **Mô tả**: Lấy chi tiết một bài viết đã publish theo slug (URL-friendly)
- **Authentication**: Không cần

- **Path params**:
  - **slug** (required, string): Slug của bài viết (ví dụ: `huong-dan-sua-chua-dieu-hoa-tai-nha`)

- **Response 200**: Tương tự như endpoint lấy theo ID

- **Response 404**:
  - Khi bài viết không tồn tại hoặc chưa được publish
  - Message code: `ARTICLE_NOT_FOUND`

---

## 3. ADMIN ENDPOINTS

### 3.1. Tạo bài viết mới

- **Method**: `POST`
- **URL**: `/api/v1/articles`
- **Mô tả**: Admin tạo bài viết mới (mặc định status: DRAFT)
- **Authentication**: Cần role ADMIN

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`
  - `Content-Type: application/json`

- **Request body** (`CreateArticleRequest`):

```json
{
  "title": "Tiêu đề bài viết",
  "shortDescription": "Mô tả ngắn",
  "slug": "tieu-de-bai-viet",
  "content": {
    "type": "structured",
    "blocks": [
      {
        "type": "paragraph",
        "content": "Nội dung đoạn văn"
      },
      {
        "type": "heading",
        "level": 2,
        "content": "Tiêu đề phụ"
      },
      {
        "type": "unorderedList",
        "items": ["Mục 1", "Mục 2"],
        "spacing": "medium"
      },
      {
        "type": "image",
        "url": "https://example.com/image.jpg",
        "alt": "Mô tả hình ảnh",
        "caption": "Chú thích"
      }
    ]
  },
  "heroImage": {
    "url": "https://example.com/hero.jpg",
    "alt": "Hình ảnh hero",
    "caption": "Chú thích hero"
  },
  "metaDescription": "Mô tả SEO",
  "metaKeywords": "từ khóa 1, từ khóa 2",
  "sections": [
    {
      "title": "Tiêu đề section",
      "content": "Nội dung section",
      "bulletPoints": ["Điểm 1", "Điểm 2"]
    }
  ],
  "contactInfo": {
    "websiteUrl": "https://fix4home.com",
    "bookingPhone": "0901234567",
    "consultationPhones": ["0901234567"]
  },
  "metadata": {
    "tags": ["tag1", "tag2"],
    "category": "Danh mục"
  }
}
```

- **Response 201** (`application/json`):

```json
{
  "success": true,
  "message": "Article created successfully",
  "data": {
    "id": 1,
    "title": "Tiêu đề bài viết",
    "status": "DRAFT",
    ...
  }
}
```

---

### 3.2. Cập nhật bài viết

- **Method**: `PUT`
- **URL**: `/api/v1/articles/{id}`
- **Mô tả**: Admin cập nhật thông tin bài viết (tất cả fields đều optional)
- **Authentication**: Cần role ADMIN

- **Path params**:
  - **id** (required, long): ID của bài viết

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`
  - `Content-Type: application/json`

- **Request body** (`UpdateArticleRequest`):
  - Tất cả fields đều optional, chỉ cần gửi các field muốn cập nhật
  - Cấu trúc tương tự `CreateArticleRequest`

- **Response 200**:

```json
{
  "success": true,
  "message": "Article updated successfully",
  "data": {
    "id": 1,
    "title": "Tiêu đề đã cập nhật",
    ...
  }
}
```

- **Response 404**:
  - Khi bài viết không tồn tại
  - Message code: `ARTICLE_NOT_FOUND`

---

### 3.3. Publish bài viết

- **Method**: `POST`
- **URL**: `/api/v1/articles/{id}/publish`
- **Mô tả**: Admin publish bài viết (chuyển status từ DRAFT/UNPUBLISHED sang PUBLISHED)
- **Authentication**: Cần role ADMIN

- **Path params**:
  - **id** (required, long): ID của bài viết

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`

- **Response 200**:

```json
{
  "success": true,
  "message": "Article published successfully",
  "data": {
    "id": 1,
    "status": "PUBLISHED",
    "publishedAt": "2025-01-15T11:00:00",
    ...
  }
}
```

---

### 3.4. Unpublish bài viết

- **Method**: `POST`
- **URL**: `/api/v1/articles/{id}/unpublish`
- **Mô tả**: Admin unpublish bài viết (chuyển status từ PUBLISHED sang UNPUBLISHED)
- **Authentication**: Cần role ADMIN

- **Path params**:
  - **id** (required, long): ID của bài viết

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`

- **Response 200**:

```json
{
  "success": true,
  "message": "Article unpublished successfully",
  "data": {
    "id": 1,
    "status": "UNPUBLISHED",
    ...
  }
}
```

---

### 3.5. Xóa bài viết

- **Method**: `DELETE`
- **URL**: `/api/v1/articles/{id}`
- **Mô tả**: Admin xóa bài viết vĩnh viễn
- **Authentication**: Cần role ADMIN

- **Path params**:
  - **id** (required, long): ID của bài viết

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`

- **Response 200**:

```json
{
  "success": true,
  "message": "Article deleted successfully",
  "data": null
}
```

- **Response 404**:
  - Khi bài viết không tồn tại
  - Message code: `ARTICLE_NOT_FOUND`

---

### 3.6. Lấy danh sách tất cả bài viết (Admin)

- **Method**: `GET`
- **URL**: `/api/v1/articles/admin`
- **Mô tả**: Admin lấy danh sách tất cả bài viết (bao gồm cả DRAFT, UNPUBLISHED), có phân trang và lọc theo status
- **Authentication**: Cần role ADMIN

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`

- **Query params**:
  - **status** (optional, string):
    - Giá trị hợp lệ: `DRAFT`, `PUBLISHED`, `UNPUBLISHED`
    - Nếu không truyền: lấy tất cả trạng thái
  - **page** (optional, int):
    - Số trang, bắt đầu từ `0`
    - Mặc định `0`
  - **limit** (optional, int):
    - Số bản ghi mỗi trang
    - Mặc định `10`, tối đa `100`

- **Response 200**:

```json
{
  "success": true,
  "message": "Articles retrieved successfully",
  "data": {
    "articles": [...],
    "total": 50,
    "page": 0,
    "limit": 10,
    "totalPages": 5
  }
}
```

---

### 3.7. Xem chi tiết bài viết (Admin)

- **Method**: `GET`
- **URL**: `/api/v1/articles/admin/{id}`
- **Mô tả**: Admin xem chi tiết bài viết (bao gồm cả DRAFT, UNPUBLISHED)
- **Authentication**: Cần role ADMIN

- **Path params**:
  - **id** (required, long): ID của bài viết

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`

- **Response 200**: Tương tự như endpoint public, nhưng có thể lấy được cả bài viết DRAFT/UNPUBLISHED

- **Response 404**:
  - Khi bài viết không tồn tại
  - Message code: `ARTICLE_NOT_FOUND`

---

## 4. Cấu trúc dữ liệu

### 4.1. ArticleStatus Enum

- `DRAFT`: Bài viết nháp, chưa publish
- `PUBLISHED`: Bài viết đã publish, hiển thị cho public
- `UNPUBLISHED`: Bài viết đã unpublish, không hiển thị cho public

### 4.2. ContentBlock Types

- `paragraph`: Đoạn văn bản
- `heading`: Tiêu đề (có `level` từ 1-6)
- `orderedList`: Danh sách có thứ tự
- `unorderedList`: Danh sách không có thứ tự
- `image`: Hình ảnh
- `link`: Liên kết
- `section`: Section với nội dung lồng nhau

---

## 5. Tóm tắt nhanh cho Frontend

### Public Endpoints (Không cần authentication):

- **Lấy danh sách bài viết đã publish**:
  - `GET /api/v1/articles?page=0&limit=10`

- **Tìm kiếm bài viết**:
  - `GET /api/v1/articles/search?keyword=sửa chữa&page=0&limit=10`

- **Lấy chi tiết bài viết theo ID**:
  - `GET /api/v1/articles/{id}`

- **Lấy chi tiết bài viết theo slug**:
  - `GET /api/v1/articles/slug/{slug}`

### Admin Endpoints (Cần authentication):

- **Tạo bài viết**: `POST /api/v1/articles`
- **Cập nhật bài viết**: `PUT /api/v1/articles/{id}`
- **Publish bài viết**: `POST /api/v1/articles/{id}/publish`
- **Unpublish bài viết**: `POST /api/v1/articles/{id}/unpublish`
- **Xóa bài viết**: `DELETE /api/v1/articles/{id}`
- **Lấy danh sách tất cả bài viết**: `GET /api/v1/articles/admin?status=PUBLISHED&page=0&limit=10`
- **Xem chi tiết bài viết (admin)**: `GET /api/v1/articles/admin/{id}`

---

## 6. Ví dụ sử dụng

### Frontend - Lấy danh sách bài viết:

```javascript
// Fetch danh sách bài viết
const response = await fetch('/api/v1/articles?page=0&limit=10');
const data = await response.json();
console.log(data.data.articles); // Danh sách bài viết
console.log(data.data.total); // Tổng số bài viết
```

### Frontend - Tìm kiếm:

```javascript
const keyword = 'sửa chữa';
const response = await fetch(`/api/v1/articles/search?keyword=${encodeURIComponent(keyword)}&page=0&limit=10`);
const data = await response.json();
```

### Frontend - Lấy chi tiết theo slug:

```javascript
const slug = 'huong-dan-sua-chua-dieu-hoa-tai-nha';
const response = await fetch(`/api/v1/articles/slug/${slug}`);
const data = await response.json();
```

### Admin - Tạo bài viết:

```javascript
const response = await fetch('/api/v1/articles', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer <admin_token>',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    title: 'Tiêu đề bài viết',
    shortDescription: 'Mô tả ngắn',
    slug: 'tieu-de-bai-viet',
    content: {
      type: 'structured',
      blocks: [
        {
          type: 'paragraph',
          content: 'Nội dung...'
        }
      ]
    }
  })
});
```

---

## 7. Lưu ý

1. **Public endpoints** chỉ trả về bài viết có status `PUBLISHED`
2. **Admin endpoints** có thể truy cập tất cả bài viết bất kể status
3. **Slug** phải là unique, không được trùng với bài viết khác
4. **Pagination**: Page bắt đầu từ `0`, limit mặc định là `10`, tối đa `100`
5. Tất cả timestamps đều theo format ISO 8601: `yyyy-MM-ddTHH:mm:ss`

