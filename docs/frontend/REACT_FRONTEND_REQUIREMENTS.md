# FIX4HOME REACT FRONTEND REQUIREMENTS

## Tổng quan dự án

Fix4Home là nền tảng kết nối khách hàng với thợ sửa chữa dịch vụ tại nhà. Backend đã được phát triển hoàn chỉnh với Spring Boot, frontend cần phát triển bằng React để cung cấp giao diện người dùng hiện đại và responsive.

## Mục lục

1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Vai trò người dùng](#vai-trò-người-dùng)
3. [Tech Stack Requirements](#tech-stack-requirements)
4. [Cấu trúc dự án](#cấu-trúc-dự-án)
5. [Tính năng chính](#tính-năng-chính)
6. [API Integration](#api-integration)
7. [UI/UX Requirements](#uiux-requirements)
8. [Bảo mật](#bảo-mật)
9. [Performance](#performance)
10. [Testing](#testing)
11. [Deployment](#deployment)

---

## Tổng quan hệ thống

### Backend API
- **Base URL**: `http://localhost:8100/api/v1`
- **Authentication**: JWT Bearer Token
- **Response Format**: JSON với cấu trúc chuẩn
- **Total Endpoints**: 113+ API endpoints
- **Documentation**: Swagger UI tại `/swagger-ui.html`

### Cấu trúc Response chuẩn
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Vai trò người dùng

### 1. CUSTOMER (Khách hàng)
**Quyền hạn chính:**
- Đăng ký/đăng nhập tài khoản
- Quản lý thông tin cá nhân và địa chỉ
- Tạo yêu cầu dịch vụ (Service Request)
- Tạo bài đăng dịch vụ (Service Post) 
- Chat với thợ sửa chữa
- Thanh toán dịch vụ
- Đánh giá feedback cho thợ
- Xem lịch sử dịch vụ

### 2. TECHNICIAN (Thợ sửa chữa)
**Quyền hạn chính:**
- Đăng ký/đăng nhập tài khoản
- Quản lý profile và kỹ năng
- Xem danh sách Service Request khả dụng
- Nhận/từ chối yêu cầu dịch vụ
- Tạo consultation/quotation cho Service Post
- Chat với khách hàng
- Cập nhật trạng thái công việc
- Xem thu nhập và thống kê
- Trả lời feedback

### 3. ADMIN (Quản trị viên)
**Quyền hạn chính:**
- Dashboard tổng quan hệ thống
- Quản lý tất cả người dùng
- Duyệt đăng ký thợ sửa chữa
- Quản lý dịch vụ và kỹ năng
- Quản lý thanh toán và báo cáo
- Thống kê và analytics
- Quản lý notification hệ thống
- Backup và maintenance

---

## Tech Stack Requirements

### Core Framework
```json
{
  "framework": "React 18+",
  "language": "TypeScript",
  "bundler": "Vite hoặc Create React App",
  "package_manager": "npm hoặc yarn"
}
```

### Essential Libraries

#### State Management
- **Redux Toolkit** hoặc **Zustand** - Quản lý state toàn cục
- **React Query/TanStack Query** - Server state management và caching

#### Routing
- **React Router v6** - Client-side routing

#### UI Framework & Styling
- **Material-UI (MUI)** hoặc **Ant Design** - Component library chính
- **Styled-components** hoặc **Emotion** - CSS-in-JS (nếu cần customize)
- **Tailwind CSS** (optional) - Utility-first CSS

#### Form Handling
- **React Hook Form** - Form validation và handling
- **Yup** hoặc **Zod** - Schema validation

#### HTTP Client
- **Axios** - HTTP requests với interceptors

#### Real-time Communication
- **Socket.io-client** - WebSocket cho chat và notifications
- **React Query** với WebSocket integration

#### Date & Time
- **date-fns** hoặc **dayjs** - Date manipulation

#### File Upload
- **React Dropzone** - Drag & drop file upload

#### Maps & Location
- **Google Maps API** hoặc **Mapbox** - Hiển thị bản đồ và địa chỉ
- **React Google Maps** hoặc **React Map GL**

#### Notifications
- **React Hot Toast** hoặc **React Toastify** - Toast notifications

#### Development Tools
- **ESLint** + **Prettier** - Code linting và formatting
- **Husky** - Git hooks
- **TypeScript** - Type safety

#### Testing
- **Jest** - Unit testing
- **React Testing Library** - Component testing
- **Cypress** hoặc **Playwright** - E2E testing

---

## Cấu trúc dự án

```
fix4home-frontend/
├── public/
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── api/                    # API calls và service layer
│   │   ├── auth.ts
│   │   ├── customer.ts
│   │   ├── technician.ts
│   │   ├── admin.ts
│   │   ├── services.ts
│   │   ├── chat.ts
│   │   └── index.ts
│   ├── components/             # Reusable components
│   │   ├── common/
│   │   │   ├── Header.tsx
│   │   │   ├── Footer.tsx
│   │   │   ├── Loading.tsx
│   │   │   ├── ErrorBoundary.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   ├── forms/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   └── ProfileForm.tsx
│   │   ├── ui/                 # Base UI components
│   │   │   ├── Button.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Table.tsx
│   │   │   └── Card.tsx
│   │   └── layout/
│   │       ├── AppLayout.tsx
│   │       └── DashboardLayout.tsx
│   ├── pages/                  # Page components
│   │   ├── auth/
│   │   │   ├── Login.tsx
│   │   │   ├── Register.tsx
│   │   │   └── ForgotPassword.tsx
│   │   ├── customer/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Services.tsx
│   │   │   ├── ServiceRequest.tsx
│   │   │   ├── Profile.tsx
│   │   │   └── History.tsx
│   │   ├── technician/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── AvailableJobs.tsx
│   │   │   ├── MyJobs.tsx
│   │   │   ├── Profile.tsx
│   │   │   └── Earnings.tsx
│   │   ├── admin/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── UserManagement.tsx
│   │   │   ├── ServiceManagement.tsx
│   │   │   └── Reports.tsx
│   │   ├── chat/
│   │   │   ├── ChatList.tsx
│   │   │   └── ChatRoom.tsx
│   │   └── public/
│   │       ├── Home.tsx
│   │       ├── About.tsx
│   │       └── Contact.tsx
│   ├── hooks/                  # Custom hooks
│   │   ├── useAuth.ts
│   │   ├── useSocket.ts
│   │   ├── useLocalStorage.ts
│   │   └── useDebounce.ts
│   ├── store/                  # State management
│   │   ├── auth/
│   │   ├── user/
│   │   ├── services/
│   │   └── chat/
│   ├── types/                  # TypeScript types
│   │   ├── api.ts
│   │   ├── user.ts
│   │   ├── service.ts
│   │   └── index.ts
│   ├── utils/                  # Utility functions
│   │   ├── auth.ts
│   │   ├── formatters.ts
│   │   ├── validators.ts
│   │   └── constants.ts
│   ├── assets/                 # Static assets
│   │   ├── images/
│   │   ├── icons/
│   │   └── styles/
│   ├── config/                 # Configuration
│   │   ├── api.ts
│   │   ├── routes.ts
│   │   └── theme.ts
│   ├── App.tsx
│   ├── index.tsx
│   └── setupTests.ts
├── package.json
├── tsconfig.json
├── vite.config.ts              # Nếu dùng Vite
└── README.md
```

---

## Tính năng chính

### 1. Authentication & Authorization

#### Components cần thiết:
- **LoginForm**: Form đăng nhập với username/email + password
- **RegisterForm**: Form đăng ký với role selection
- **ForgotPassword**: Reset password functionality
- **ProtectedRoute**: Route protection based on roles

#### API Integration:
```typescript
// Auth API calls
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/logout
POST /api/v1/auth/refresh-token
```

#### Features:
- JWT token management với auto-refresh
- Remember me functionality
- Role-based route protection
- Session management
- Logout from all devices

### 2. Dashboard theo Role

#### Customer Dashboard
**Components:**
- **DashboardStats**: Tổng quan thống kê cá nhân
- **RecentServices**: Dịch vụ gần đây
- **QuickActions**: Các action nhanh (Tạo request, tìm thợ)
- **Notifications**: Thông báo mới

**Metrics hiển thị:**
- Tổng số dịch vụ đã sử dụng
- Dịch vụ đang tiến hành
- Số tiền đã chi tiêu
- Đánh giá trung bình đã nhận

#### Technician Dashboard
**Components:**
- **EarningsOverview**: Tổng quan thu nhập
- **JobStats**: Thống kê công việc
- **AvailableJobs**: Việc khả dụng
- **UpcomingSchedule**: Lịch sắp tới

**Metrics hiển thị:**
- Thu nhập tháng này/tuần này
- Số công việc hoàn thành
- Rating trung bình
- Số tin nhắn chưa đọc

#### Admin Dashboard
**Components:**
- **SystemOverview**: Tổng quan hệ thống
- **UserGrowth**: Biểu đồ tăng trưởng người dùng
- **RevenueChart**: Biểu đồ doanh thu
- **PendingApprovals**: Duyệt chờ xử lý

**Metrics hiển thị:**
- Tổng số users theo role
- Doanh thu hệ thống
- Số giao dịch hôm nay
- Tỷ lệ hoàn thành dịch vụ

### 3. Service Management

#### Customer Side - Service Request
**Components:**
- **ServiceBrowser**: Duyệt danh sách dịch vụ
- **ServiceRequestForm**: Tạo yêu cầu dịch vụ mới
- **ServiceRequestList**: Danh sách yêu cầu của customer
- **ServiceRequestDetail**: Chi tiết yêu cầu và tracking

**API Integration:**
```typescript
GET /api/v1/services                    // Danh sách dịch vụ
POST /api/v1/service-requests           // Tạo yêu cầu
GET /api/v1/service-requests/my         // Yêu cầu của tôi
GET /api/v1/service-requests/{id}       // Chi tiết yêu cầu
PUT /api/v1/service-requests/{id}/cancel // Hủy yêu cầu
```

#### Customer Side - Service Post
**Components:**
- **ServicePostForm**: Đăng bài tìm thợ
- **ServicePostList**: Danh sách bài đăng
- **ConsultationList**: Danh sách consultation nhận được

#### Technician Side
**Components:**
- **AvailableJobsList**: Danh sách việc khả dụng
- **JobApplicationForm**: Form apply việc
- **MyJobsList**: Việc đã nhận
- **JobTracker**: Theo dõi trạng thái công việc

**API Integration:**
```typescript
GET /api/v1/service-requests/available     // Việc khả dụng
PUT /api/v1/service-requests/{id}/accept   // Nhận việc
PUT /api/v1/service-requests/{id}/decline  // Từ chối
PUT /api/v1/service-requests/{id}/start    // Bắt đầu
PUT /api/v1/service-requests/{id}/complete // Hoàn thành
```

### 4. Chat System

#### Real-time Components:
- **ChatList**: Danh sách conversation
- **ChatRoom**: Phòng chat với tin nhắn real-time
- **MessageBubble**: Component tin nhắn
- **FileUpload**: Upload file/hình ảnh
- **TypingIndicator**: Hiển thị đang typing

#### Features cần implement:
- Real-time messaging với Socket.io
- File/image sharing
- Message status (sent, delivered, read)
- Typing indicators
- Message search và history
- Emoji support

#### API Integration:
```typescript
GET /api/v1/chat/conversations         // Danh sách conversation
GET /api/v1/chat/conversations/{id}/messages // Tin nhắn
POST /api/v1/chat/conversations/{id}/messages // Gửi tin nhắn
WebSocket connection cho real-time
```

### 5. Payment System

#### Components:
- **PaymentMethods**: Quản lý phương thức thanh toán
- **PaymentForm**: Form thanh toán
- **PaymentHistory**: Lịch sử giao dịch
- **PaymentStatus**: Tracking trạng thái thanh toán

#### API Integration:
```typescript
GET /api/v1/payments/methods           // Phương thức thanh toán
POST /api/v1/payments/create           // Tạo thanh toán
GET /api/v1/payments/my                // Lịch sử của tôi
GET /api/v1/payments/my/stats          // Thống kê thanh toán
```

### 6. Profile Management

#### Customer Profile:
- **ProfileForm**: Chỉnh sửa thông tin cá nhân
- **AddressManager**: Quản lý địa chỉ
- **PreferencesSettings**: Cài đặt tùy chọn

#### Technician Profile:
- **TechnicianProfileForm**: Profile thợ sửa chữa
- **SkillsManager**: Quản lý kỹ năng
- **PortfolioGallery**: Thư viện công việc
- **AvailabilitySchedule**: Lịch rảnh

#### API Integration:
```typescript
// Customer
GET /api/v1/customers/profile
PUT /api/v1/customers/profile
GET /api/v1/customers/addresses
POST /api/v1/customers/addresses

// Technician  
GET /api/v1/technicians/me
PUT /api/v1/technicians/me
GET /api/v1/technicians/me/skills
PUT /api/v1/technicians/me/skills
```

### 7. Admin Management

#### User Management:
- **UserList**: Danh sách người dùng với filtering
- **UserDetail**: Chi tiết người dùng
- **TechnicianApproval**: Duyệt đăng ký thợ
- **BulkActions**: Thao tác hàng loạt

#### Service Management:
- **ServiceCRUD**: CRUD operations cho dịch vụ
- **SkillManagement**: Quản lý skills
- **CategoryManagement**: Quản lý danh mục

#### Reports & Analytics:
- **RevenueReport**: Báo cáo doanh thu
- **UserAnalytics**: Phân tích người dùng
- **ServiceAnalytics**: Phân tích dịch vụ
- **SystemHealth**: Tình trạng hệ thống

### 8. Notification System

#### Components:
- **NotificationCenter**: Trung tâm thông báo
- **NotificationItem**: Item thông báo
- **NotificationSettings**: Cài đặt thông báo
- **PushNotificationHandler**: Xử lý push notification

#### Features:
- In-app notifications
- Real-time notifications
- Notification categories
- Mark as read/unread
- Notification preferences

---

## API Integration

### HTTP Client Setup

```typescript
// api/client.ts
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8100/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor để thêm JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor để handle common errors
apiClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    if (error.response?.status === 401) {
      // Handle token refresh hoặc redirect to login
    }
    return Promise.reject(error);
  }
);
```

### API Service Layer

```typescript
// api/auth.ts
export const authAPI = {
  login: (credentials: LoginRequest) => 
    apiClient.post('/auth/login', credentials),
  
  register: (userData: RegisterRequest) =>
    apiClient.post('/auth/register', userData),
    
  logout: () => apiClient.post('/auth/logout'),
  
  refreshToken: () => apiClient.post('/auth/refresh-token'),
};

// api/services.ts
export const servicesAPI = {
  getAll: () => apiClient.get('/services'),
  getById: (id: string) => apiClient.get(`/services/${id}`),
  search: (query: string) => apiClient.get(`/services/search?q=${query}`),
};
```

### Error Handling

```typescript
// utils/errorHandler.ts
export const handleAPIError = (error: any) => {
  if (error.response) {
    // Server error with response
    const { status, data } = error.response;
    switch (status) {
      case 400:
        return { message: data.message || 'Dữ liệu không hợp lệ' };
      case 401:
        return { message: 'Vui lòng đăng nhập lại' };
      case 403:
        return { message: 'Bạn không có quyền thực hiện thao tác này' };
      case 404:
        return { message: 'Không tìm thấy dữ liệu' };
      case 500:
        return { message: 'Lỗi hệ thống, vui lòng thử lại sau' };
      default:
        return { message: data.message || 'Đã xảy ra lỗi' };
    }
  } else if (error.request) {
    // Network error
    return { message: 'Lỗi kết nối mạng' };
  } else {
    // Other error
    return { message: 'Đã xảy ra lỗi không xác định' };
  }
};
```

---

## UI/UX Requirements

### Design System

#### Color Palette
```css
:root {
  /* Primary Colors */
  --primary-500: #2563eb;        /* Main brand color */
  --primary-600: #1d4ed8;
  --primary-700: #1e40af;
  
  /* Secondary Colors */
  --secondary-500: #f59e0b;      /* Accent/Warning */
  --secondary-600: #d97706;
  
  /* Status Colors */
  --success-500: #10b981;        /* Success states */
  --error-500: #ef4444;          /* Error states */
  --warning-500: #f59e0b;        /* Warning states */
  --info-500: #3b82f6;           /* Info states */
  
  /* Neutral Colors */
  --gray-50: #f9fafb;
  --gray-100: #f3f4f6;
  --gray-500: #6b7280;
  --gray-900: #111827;
}
```

#### Typography
- **Font Family**: Inter, system-ui, sans-serif
- **Heading Scale**: 
  - H1: 2.5rem (40px)
  - H2: 2rem (32px)
  - H3: 1.5rem (24px)
  - H4: 1.25rem (20px)
- **Body Text**: 1rem (16px)
- **Small Text**: 0.875rem (14px)

#### Spacing System
- **Base unit**: 4px
- **Scale**: 4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 64px

### Component Guidelines

#### Buttons
```typescript
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'outline' | 'ghost';
  size: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  loading?: boolean;
  icon?: ReactNode;
}
```

#### Cards
- Shadow: subtle drop shadows
- Border radius: 8px
- Padding: 16px hoặc 24px
- Hover states: subtle scale/shadow changes

#### Forms
- Label placement: top của input
- Error states: red border + error message
- Focus states: blue outline
- Validation: real-time validation với debounce

### Responsive Design

#### Breakpoints
```css
/* Mobile first approach */
/* xs: 0px */
/* sm: 640px */
/* md: 768px */
/* lg: 1024px */
/* xl: 1280px */
/* 2xl: 1536px */
```

#### Mobile Optimizations
- Touch-friendly button sizes (min 44px)
- Thumb-friendly navigation
- Optimized image loading
- Swipe gestures cho carousels
- Bottom navigation cho mobile

### Accessibility (a11y)

#### WCAG 2.1 AA Compliance
- **Color contrast**: Minimum 4.5:1 cho normal text
- **Keyboard navigation**: Tất cả interactive elements
- **Screen reader support**: Proper ARIA labels
- **Focus management**: Visible focus indicators
- **Alt text**: Cho tất cả images

#### Implementation
```typescript
// Example accessible button
<Button
  aria-label="Create new service request"
  aria-describedby="helper-text"
  onClick={handleCreate}
>
  Create Request
</Button>
```

---

## Bảo mật

### Authentication & Authorization

#### JWT Token Management
```typescript
// Token storage và security
const TokenManager = {
  setTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem('accessToken', accessToken);
    // Refresh token should be stored in httpOnly cookie (backend)
  },
  
  getAccessToken: () => localStorage.getItem('accessToken'),
  
  clearTokens: () => {
    localStorage.removeItem('accessToken');
    // Clear refresh token cookie
  },
  
  isTokenExpired: (token: string) => {
    try {
      const decoded = jwt_decode(token);
      return decoded.exp < Date.now() / 1000;
    } catch {
      return true;
    }
  }
};
```

#### Route Protection
```typescript
// components/ProtectedRoute.tsx
interface ProtectedRouteProps {
  children: ReactNode;
  allowedRoles: Role[];
  fallback?: ReactNode;
}

const ProtectedRoute: FC<ProtectedRouteProps> = ({ 
  children, 
  allowedRoles, 
  fallback 
}) => {
  const { user, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  if (!allowedRoles.includes(user.role)) {
    return fallback || <AccessDenied />;
  }
  
  return <>{children}</>;
};
```

### Input Validation & Sanitization

#### Form Validation với Yup
```typescript
// validation/schemas.ts
export const loginSchema = yup.object({
  usernameOrEmail: yup
    .string()
    .required('Username hoặc email là bắt buộc')
    .min(3, 'Tối thiểu 3 ký tự'),
  password: yup
    .string()
    .required('Mật khẩu là bắt buộc')
    .min(6, 'Mật khẩu tối thiểu 6 ký tự')
});

export const serviceRequestSchema = yup.object({
  serviceId: yup.number().required('Vui lòng chọn dịch vụ'),
  addressId: yup.number().required('Vui lòng chọn địa chỉ'),
  description: yup
    .string()
    .required('Mô tả là bắt buộc')
    .max(1000, 'Mô tả không được vượt quá 1000 ký tự'),
  urgency: yup
    .string()
    .oneOf(['LOW', 'MEDIUM', 'HIGH'], 'Mức độ ưu tiên không hợp lệ')
});
```

#### XSS Prevention
```typescript
import DOMPurify from 'dompurify';

// Sanitize user input trước khi render
const sanitizeHtml = (html: string) => {
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'p', 'br'],
    ALLOWED_ATTR: []
  });
};

// Usage in component
<div dangerouslySetInnerHTML={{ 
  __html: sanitizeHtml(userContent) 
}} />
```

### Content Security Policy (CSP)

```html
<!-- public/index.html -->
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; 
               script-src 'self' 'unsafe-inline'; 
               style-src 'self' 'unsafe-inline' fonts.googleapis.com;
               font-src 'self' fonts.gstatic.com;
               img-src 'self' data: https:;
               connect-src 'self' ws://localhost:*;">
```

---

## Performance

### Code Splitting & Lazy Loading

```typescript
// Lazy load pages
const CustomerDashboard = lazy(() => import('../pages/customer/Dashboard'));
const TechnicianDashboard = lazy(() => import('../pages/technician/Dashboard'));
const AdminDashboard = lazy(() => import('../pages/admin/Dashboard'));

// Route-based code splitting
<Route 
  path="/customer/dashboard" 
  element={
    <Suspense fallback={<Loading />}>
      <CustomerDashboard />
    </Suspense>
  } 
/>
```

### Image Optimization

```typescript
// components/OptimizedImage.tsx
interface OptimizedImageProps {
  src: string;
  alt: string;
  width?: number;
  height?: number;
  lazy?: boolean;
}

const OptimizedImage: FC<OptimizedImageProps> = ({
  src,
  alt,
  width,
  height,
  lazy = true
}) => {
  return (
    <img
      src={src}
      alt={alt}
      width={width}
      height={height}
      loading={lazy ? 'lazy' : 'eager'}
      style={{ objectFit: 'cover' }}
    />
  );
};
```

### Caching Strategy

#### React Query Configuration
```typescript
// config/queryClient.ts
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
      retry: 3,
      refetchOnWindowFocus: false,
    },
  },
});

// Usage với cache keys
const useServices = () => {
  return useQuery({
    queryKey: ['services'],
    queryFn: () => servicesAPI.getAll(),
    staleTime: 10 * 60 * 1000, // Cache 10 minutes
  });
};
```

#### Service Worker (optional)
```typescript
// public/sw.js - Basic service worker cho caching
const CACHE_NAME = 'fix4home-v1';
const urlsToCache = [
  '/',
  '/static/css/main.css',
  '/static/js/main.js',
  '/manifest.json'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(urlsToCache))
  );
});
```

### Bundle Optimization

#### Webpack Bundle Analyzer (nếu dùng CRA)
```bash
npm install --save-dev webpack-bundle-analyzer
npx webpack-bundle-analyzer build/static/js/*.js
```

#### Vite Bundle Analyzer
```bash
npm install --save-dev rollup-plugin-visualizer
```

---

## Testing

### Unit Testing với Jest & React Testing Library

```typescript
// __tests__/components/Button.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../components/Button';

describe('Button Component', () => {
  test('renders with correct text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  test('calls onClick handler when clicked', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    
    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  test('shows loading state correctly', () => {
    render(<Button loading>Submit</Button>);
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
  });
});
```

#### API Mocking với MSW
```typescript
// __tests__/mocks/handlers.ts
import { rest } from 'msw';

export const handlers = [
  rest.get('/api/v1/services', (req, res, ctx) => {
    return res(
      ctx.json({
        success: true,
        data: [
          { id: 1, name: 'Sửa điện', price: 200000 },
          { id: 2, name: 'Sửa nước', price: 150000 }
        ]
      })
    );
  }),

  rest.post('/api/v1/auth/login', (req, res, ctx) => {
    return res(
      ctx.json({
        success: true,
        data: {
          accessToken: 'mock-jwt-token',
          user: { id: 1, username: 'testuser', role: 'CUSTOMER' }
        }
      })
    );
  }),
];
```

### Integration Testing

```typescript
// __tests__/integration/ServiceRequest.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ServiceRequestForm } from '../pages/customer/ServiceRequestForm';

const createTestQueryClient = () => new QueryClient({
  defaultOptions: { queries: { retry: false } }
});

describe('Service Request Integration', () => {
  test('creates service request successfully', async () => {
    const queryClient = createTestQueryClient();
    
    render(
      <QueryClientProvider client={queryClient}>
        <ServiceRequestForm />
      </QueryClientProvider>
    );

    // Fill form
    fireEvent.change(screen.getByLabelText('Mô tả dịch vụ'), {
      target: { value: 'Cần sửa điện' }
    });

    // Submit
    fireEvent.click(screen.getByText('Tạo yêu cầu'));

    // Wait for success message
    await waitFor(() => {
      expect(screen.getByText('Tạo yêu cầu thành công')).toBeInTheDocument();
    });
  });
});
```

### E2E Testing với Cypress

```typescript
// cypress/e2e/customer-flow.cy.ts
describe('Customer Service Request Flow', () => {
  beforeEach(() => {
    cy.login('customer@test.com', 'password123');
  });

  it('should create a new service request', () => {
    cy.visit('/customer/dashboard');
    cy.get('[data-testid="create-request-btn"]').click();
    
    // Fill service request form
    cy.get('[data-testid="service-select"]').select('Sửa điện');
    cy.get('[data-testid="address-select"]').select('Nhà riêng');
    cy.get('[data-testid="description"]').type('Cần sửa ổ cắm điện');
    cy.get('[data-testid="urgency"]').select('MEDIUM');
    
    cy.get('[data-testid="submit-btn"]').click();
    
    // Verify success
    cy.get('[data-testid="success-message"]').should('be.visible');
    cy.url().should('include', '/customer/requests');
  });
});
```

---

## Deployment

### Environment Configuration

```typescript
// config/environment.ts
export const config = {
  API_BASE_URL: process.env.REACT_APP_API_URL || 'http://localhost:8100/api/v1',
  SOCKET_URL: process.env.REACT_APP_SOCKET_URL || 'http://localhost:8100',
  GOOGLE_MAPS_API_KEY: process.env.REACT_APP_GOOGLE_MAPS_KEY,
  ENVIRONMENT: process.env.NODE_ENV || 'development',
  VERSION: process.env.REACT_APP_VERSION || '1.0.0',
};

// Environment validation
const requiredEnvVars = [
  'REACT_APP_API_URL',
  'REACT_APP_GOOGLE_MAPS_KEY'
];

requiredEnvVars.forEach(envVar => {
  if (!process.env[envVar]) {
    throw new Error(`Missing required environment variable: ${envVar}`);
  }
});
```

### Build Scripts

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "build:staging": "NODE_ENV=staging npm run build",
    "build:production": "NODE_ENV=production npm run build",
    "preview": "vite preview",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "test:e2e": "cypress run",
    "lint": "eslint src --ext .ts,.tsx",
    "lint:fix": "eslint src --ext .ts,.tsx --fix",
    "type-check": "tsc --noEmit"
  }
}
```

### Docker Configuration

```dockerfile
# Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine AS production

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```nginx
# nginx.conf
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Handle React Router
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy
    location /api/ {
        proxy_pass http://backend:8100;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket proxy
    location /socket.io/ {
        proxy_pass http://backend:8100;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Static assets caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Deploy Fix4Home Frontend

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      
      - run: npm ci
      - run: npm run lint
      - run: npm run type-check
      - run: npm run test:coverage
      - run: npm run build

  deploy-staging:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to staging
        run: |
          # Deploy logic here
          echo "Deploying to staging..."

  deploy-production:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to production
        run: |
          # Deploy logic here
          echo "Deploying to production..."
```

---

## Kết luận

Document này cung cấp roadmap chi tiết để phát triển frontend React cho hệ thống Fix4Home. Các requirement đã được thiết kế để:

1. **Tương thích hoàn toàn** với backend Spring Boot hiện có
2. **Scalable và maintainable** cho việc phát triển lâu dài
3. **User-friendly** với UX/UI hiện đại
4. **Security-first** với các best practices bảo mật
5. **Performance-optimized** cho trải nghiệm người dùng tốt nhất

### Next Steps

1. **Setup dự án**: Khởi tạo React project với tech stack đã định
2. **Authentication flow**: Implement đăng nhập/đăng ký trước
3. **Dashboard**: Phát triển dashboard cho từng role
4. **Core features**: Implement từng tính năng chính theo thứ tự ưu tiên
5. **Testing**: Viết tests song song với development
6. **Deployment**: Setup CI/CD và deployment pipeline

### Priority Order

1. **Phase 1**: Authentication + Dashboard cơ bản
2. **Phase 2**: Service Request/Post management
3. **Phase 3**: Chat system + Real-time features  
4. **Phase 4**: Payment integration
5. **Phase 5**: Admin panel + Analytics
6. **Phase 6**: Advanced features + Optimizations

---

**Last Updated**: January 2024  
**Version**: 1.0  
**Author**: Fix4Home Development Team
