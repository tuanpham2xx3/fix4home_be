# 📰 Hướng Dẫn Sử Dụng Article API Cho Frontend

## 📋 Tổng Quan

API này cung cấp các endpoint để frontend lấy và hiển thị bài viết tin tức (news articles) trên trang web.

**Đặc điểm quan trọng:**
- ✅ **Tất cả endpoint public đều KHÔNG CẦN authentication**
- ✅ Chỉ trả về bài viết có status = `PUBLISHED`
- ✅ Hỗ trợ phân trang
- ✅ Hỗ trợ tìm kiếm theo keyword

---

## 🌐 Base URL

```
http://localhost:8100/api/v1/articles
```

*(Thay đổi domain cho production)*

---

## 📚 Danh Sách Endpoints

### 1. Lấy Danh Sách Tất Cả Bài Viết Public

**Endpoint:** `GET /api/v1/articles`

**Mô tả:** Lấy danh sách tất cả bài viết đã được publish, có phân trang, sắp xếp theo ngày publish mới nhất

**Authentication:** ❌ Không cần

**Query Parameters:**

| Parameter | Type | Required | Default | Mô tả |
|-----------|------|----------|---------|-------|
| `page` | Integer | ❌ | `0` | Số trang (0-based) |
| `limit` | Integer | ❌ | `10` | Số bài viết mỗi trang (tối đa 100) |

**Request Example:**

```javascript
// JavaScript/TypeScript
const fetchArticles = async (page = 0, limit = 10) => {
  try {
    const response = await fetch(
      `http://localhost:8100/api/v1/articles?page=${page}&limit=${limit}`
    );
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    
    if (result.success) {
      const { articles, total, page, limit, totalPages } = result.data;
      console.log(`Có ${total} bài viết, hiển thị trang ${page + 1}/${totalPages}`);
      return result.data;
    } else {
      throw new Error(result.message || 'Failed to fetch articles');
    }
  } catch (error) {
    console.error('Error fetching articles:', error);
    throw error;
  }
};

// Sử dụng
const articlesData = await fetchArticles(0, 10);
articlesData.articles.forEach(article => {
  console.log(article.title);
});
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Published articles retrieved successfully",
  "data": {
    "articles": [
      {
        "id": 1,
        "title": "Hướng dẫn sửa chữa điều hòa tại nhà",
        "shortDescription": "Bài viết hướng dẫn cách tự sửa chữa điều hòa đơn giản tại nhà",
        "slug": "huong-dan-sua-chua-dieu-hoa-tai-nha",
        "status": "PUBLISHED",
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
          "url": "http://localhost:8100/api/v1/files/download/image.jpg",
          "alt": "Hình ảnh minh họa",
          "caption": "Mô tả hình ảnh"
        },
        "metaDescription": "Mô tả SEO cho bài viết",
        "metaKeywords": "sửa chữa, điều hòa, tại nhà",
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
  },
  "timestamp": "2025-01-15T12:00:00"
}
```

---

### 2. Lấy Chi Tiết Bài Viết Theo ID

**Endpoint:** `GET /api/v1/articles/{id}`

**Mô tả:** Lấy thông tin chi tiết của một bài viết theo ID

**Authentication:** ❌ Không cần

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `id` | Long | ✅ | ID của bài viết |

**Request Example:**

```javascript
const fetchArticleById = async (articleId) => {
  try {
    const response = await fetch(
      `http://localhost:8100/api/v1/articles/${articleId}`
    );
    
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('Bài viết không tồn tại hoặc chưa được publish');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    
    if (result.success) {
      return result.data; // ArticleDTO
    } else {
      throw new Error(result.message || 'Failed to fetch article');
    }
  } catch (error) {
    console.error('Error fetching article:', error);
    throw error;
  }
};

// Sử dụng
const article = await fetchArticleById(1);
console.log(article.title);
console.log(article.content);
```

**Response (200 OK):** Tương tự như object trong array `articles` của endpoint 1

**Error (404 Not Found):**

```json
{
  "success": false,
  "message": "Article not found with ID: 999",
  "data": null,
  "timestamp": "2025-01-15T12:00:00"
}
```

---

### 3. Lấy Chi Tiết Bài Viết Theo Slug

**Endpoint:** `GET /api/v1/articles/slug/{slug}`

**Mô tả:** Lấy thông tin chi tiết của một bài viết theo slug (friendly URL)

**Authentication:** ❌ Không cần

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `slug` | String | ✅ | Slug của bài viết (VD: "huong-dan-sua-chua-dieu-hoa-tai-nha") |

**Request Example:**

```javascript
const fetchArticleBySlug = async (slug) => {
  try {
    const response = await fetch(
      `http://localhost:8100/api/v1/articles/slug/${encodeURIComponent(slug)}`
    );
    
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('Bài viết không tồn tại hoặc chưa được publish');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    
    if (result.success) {
      return result.data; // ArticleDTO
    } else {
      throw new Error(result.message || 'Failed to fetch article');
    }
  } catch (error) {
    console.error('Error fetching article:', error);
    throw error;
  }
};

// Sử dụng
const article = await fetchArticleBySlug('huong-dan-sua-chua-dieu-hoa-tai-nha');
console.log(article.title);
```

**Response (200 OK):** Tương tự như endpoint 2

**Error (404 Not Found):**

```json
{
  "success": false,
  "message": "Article not found with slug: invalid-slug",
  "data": null,
  "timestamp": "2025-01-15T12:00:00"
}
```

---

### 4. Tìm Kiếm Bài Viết

**Endpoint:** `GET /api/v1/articles/search`

**Mô tả:** Tìm kiếm bài viết theo keyword, trả về kết quả có phân trang

**Authentication:** ❌ Không cần

**Query Parameters:**

| Parameter | Type | Required | Default | Mô tả |
|-----------|------|----------|---------|-------|
| `keyword` | String | ❌ | - | Từ khóa tìm kiếm (tìm trong title, shortDescription, content) |
| `page` | Integer | ❌ | `0` | Số trang (0-based) |
| `limit` | Integer | ❌ | `10` | Số bài viết mỗi trang (tối đa 100) |

**Request Example:**

```javascript
const searchArticles = async (keyword, page = 0, limit = 10) => {
  try {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString()
    });
    
    if (keyword && keyword.trim()) {
      params.append('keyword', keyword.trim());
    }
    
    const response = await fetch(
      `http://localhost:8100/api/v1/articles/search?${params.toString()}`
    );
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    
    if (result.success) {
      return result.data; // ArticleListResponseDTO
    } else {
      throw new Error(result.message || 'Failed to search articles');
    }
  } catch (error) {
    console.error('Error searching articles:', error);
    throw error;
  }
};

// Sử dụng
// Tìm kiếm với keyword
const searchResults = await searchArticles('sửa chữa', 0, 10);
console.log(`Tìm thấy ${searchResults.total} bài viết`);

// Nếu không có keyword, trả về tất cả bài viết (giống endpoint 1)
const allArticles = await searchArticles('', 0, 10);
```

**Response (200 OK):** Tương tự như endpoint 1 (ArticleListResponseDTO)

---

## 💻 Ví Dụ Sử Dụng Trong React/Next.js

### Hook React để Fetch Articles

```typescript
// hooks/useArticles.ts
import { useState, useEffect } from 'react';

interface Article {
  id: number;
  title: string;
  shortDescription: string;
  slug: string;
  heroImage?: {
    url: string;
    alt?: string;
    caption?: string;
  };
  publishedAt: string;
  // ... other fields
}

interface UseArticlesResult {
  articles: Article[];
  loading: boolean;
  error: string | null;
  total: number;
  page: number;
  totalPages: number;
  fetchArticles: (page?: number, limit?: number) => Promise<void>;
}

export const useArticles = (initialPage = 0, initialLimit = 10): UseArticlesResult => {
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(initialPage);
  const [totalPages, setTotalPages] = useState(0);

  const fetchArticles = async (pageNum = 0, limit = 10) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8100'}/api/v1/articles?page=${pageNum}&limit=${limit}`
      );
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const result = await response.json();
      
      if (result.success) {
        setArticles(result.data.articles);
        setTotal(result.data.total);
        setPage(result.data.page);
        setTotalPages(result.data.totalPages);
      } else {
        throw new Error(result.message || 'Failed to fetch articles');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setArticles([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchArticles(initialPage, initialLimit);
  }, []);

  return {
    articles,
    loading,
    error,
    total,
    page,
    totalPages,
    fetchArticles
  };
};
```

### Component React để Hiển Thị Danh Sách Bài Viết

```tsx
// components/ArticleList.tsx
import { useArticles } from '@/hooks/useArticles';
import Link from 'next/link';

export const ArticleList = () => {
  const { articles, loading, error, total, page, totalPages, fetchArticles } = useArticles(0, 10);

  if (loading) {
    return <div>Đang tải...</div>;
  }

  if (error) {
    return <div className="error">Lỗi: {error}</div>;
  }

  return (
    <div className="article-list">
      <h2>Tất Cả Bài Viết ({total})</h2>
      
      <div className="articles-grid">
        {articles.map((article) => (
          <article key={article.id} className="article-card">
            {article.heroImage && (
              <img 
                src={article.heroImage.url} 
                alt={article.heroImage.alt || article.title}
                className="article-image"
              />
            )}
            <h3>{article.title}</h3>
            <p>{article.shortDescription}</p>
            <Link href={`/articles/${article.slug}`}>
              <a>Đọc thêm →</a>
            </Link>
            <small>{new Date(article.publishedAt).toLocaleDateString('vi-VN')}</small>
          </article>
        ))}
      </div>

      {/* Pagination */}
      <div className="pagination">
        <button 
          disabled={page === 0} 
          onClick={() => fetchArticles(page - 1)}
        >
          Trước
        </button>
        <span>Trang {page + 1} / {totalPages}</span>
        <button 
          disabled={page >= totalPages - 1} 
          onClick={() => fetchArticles(page + 1)}
        >
          Sau
        </button>
      </div>
    </div>
  );
};
```

### Component Tìm Kiếm

```tsx
// components/ArticleSearch.tsx
import { useState } from 'react';

export const ArticleSearch = () => {
  const [keyword, setKeyword] = useState('');
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!keyword.trim()) {
      setResults([]);
      return;
    }

    setLoading(true);
    
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8100'}/api/v1/articles/search?keyword=${encodeURIComponent(keyword)}`
      );
      
      const result = await response.json();
      
      if (result.success) {
        setResults(result.data.articles);
      }
    } catch (error) {
      console.error('Search error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="article-search">
      <form onSubmit={handleSearch}>
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Tìm kiếm bài viết..."
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Đang tìm...' : 'Tìm kiếm'}
        </button>
      </form>

      {results.length > 0 && (
        <div className="search-results">
          <h3>Tìm thấy {results.length} bài viết:</h3>
          {results.map((article) => (
            <div key={article.id}>
              <h4>{article.title}</h4>
              <p>{article.shortDescription}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
```

---

## 🔍 Lưu Ý Quan Trọng

### 1. Chỉ Trả Về Bài Viết PUBLISHED
- Tất cả endpoint public chỉ trả về bài viết có `status = "PUBLISHED"`
- Bài viết có status `DRAFT` hoặc `UNPUBLISHED` sẽ không xuất hiện trong kết quả

### 2. Phân Trang
- Sử dụng `page` (0-based) và `limit` để phân trang
- Giá trị `limit` tối đa là 100
- Luôn check `totalPages` để biết có thể điều hướng đến trang nào

### 3. Hero Image URL
- `heroImage.url` có thể là URL đầy đủ hoặc relative path
- Nếu là relative path, cần prepend base URL của backend
- Ví dụ: Nếu `heroImage.url = "/api/v1/files/download/image.jpg"`, cần thêm domain

### 4. Slug vs ID
- Ưu tiên sử dụng `slug` cho SEO-friendly URLs
- Sử dụng `id` nếu cần performance tốt hơn (nhưng ít SEO-friendly hơn)

### 5. Error Handling
- Luôn check `response.ok` trước khi parse JSON
- Check `result.success` trong response
- Xử lý các trường hợp:
  - `404`: Bài viết không tồn tại hoặc chưa publish
  - `500`: Server error
  - Network error: Không kết nối được server

---

## 📝 Ví Dụ CURL

```bash
# Lấy danh sách bài viết
curl -X GET "http://localhost:8100/api/v1/articles?page=0&limit=10"

# Lấy chi tiết bài viết theo ID
curl -X GET "http://localhost:8100/api/v1/articles/1"

# Lấy chi tiết bài viết theo slug
curl -X GET "http://localhost:8100/api/v1/articles/slug/huong-dan-sua-chua-dieu-hoa-tai-nha"

# Tìm kiếm bài viết
curl -X GET "http://localhost:8100/api/v1/articles/search?keyword=sửa chữa&page=0&limit=10"
```

---

## 🎯 Checklist Khi Implement

- [ ] Setup base URL (có thể từ environment variable)
- [ ] Implement error handling cho tất cả requests
- [ ] Implement loading states
- [ ] Implement pagination UI
- [ ] Handle empty states (không có bài viết)
- [ ] Format dates đúng locale (vi-VN)
- [ ] Handle hero image URLs (absolute vs relative)
- [ ] Implement SEO meta tags cho từng bài viết
- [ ] Cache responses nếu cần (React Query, SWR, etc.)
- [ ] Optimize images (lazy loading, responsive images)

---

## 📞 Support

Nếu có vấn đề khi sử dụng API, vui lòng liên hệ backend team.

**Chúc code vui vẻ! 🚀**

