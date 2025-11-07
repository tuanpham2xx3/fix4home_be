# Frontend Integration Guide

## 🎯 Quick Start for Frontend Developers

This guide will help you integrate the Fix4Home API into your frontend application.

## 📦 Option 1: Auto-Generated Client SDK (Recommended)

### For React/Vue/Angular (TypeScript)

#### Step 1: Generate the Client

```bash
cd docs/openapi

# On Windows
generate-clients.bat
# Select option 1 (TypeScript/Axios)

# On Linux/Mac
./generate-clients.sh
# Select option 1 (TypeScript/Axios)
```

#### Step 2: Install in Your Project

```bash
# Copy generated client to your frontend project
cp -r clients/typescript ../../../FIX4HOME_FE/src/api

# Or install as npm package
cd clients/typescript
npm pack
# Then in your frontend project:
npm install /path/to/fix4home-api-client-1.0.0.tgz
```

#### Step 3: Use in Your Code

```typescript
import { Configuration, AuthenticationApi, CustomerManagementApi } from './api';

// Configure API client
const config = new Configuration({
  basePath: 'http://localhost:8100/api/v1',
  accessToken: localStorage.getItem('authToken') || ''
});

// Create API instances
const authApi = new AuthenticationApi(config);
const customerApi = new CustomerManagementApi(config);

// Usage example
async function login(email: string, password: string) {
  try {
    const response = await authApi.login({ email, password });
    const { token, user } = response.data.data;
    
    // Store token
    localStorage.setItem('authToken', token);
    
    return user;
  } catch (error) {
    console.error('Login failed:', error);
    throw error;
  }
}

async function getProfile() {
  try {
    const response = await customerApi.getCustomerProfile();
    return response.data.data;
  } catch (error) {
    console.error('Failed to get profile:', error);
    throw error;
  }
}
```

## 🔧 Option 2: Manual Integration with Axios

### Installation

```bash
npm install axios
```

### Create API Service

```typescript
// src/services/api.ts
import axios, { AxiosInstance } from 'axios';

const BASE_URL = 'http://localhost:8100/api/v1';

class ApiService {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add token to requests
    this.client.interceptors.request.use((config) => {
      const token = localStorage.getItem('authToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Handle responses
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Redirect to login
          localStorage.removeItem('authToken');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Authentication
  async register(data: {
    email: string;
    password: string;
    role: 'CUSTOMER' | 'TECHNICIAN';
    fullName: string;
    phone: string;
  }) {
    const response = await this.client.post('/auth/register', data);
    return response.data;
  }

  async login(email: string, password: string) {
    const response = await this.client.post('/auth/login', { email, password });
    const { token, user } = response.data.data;
    localStorage.setItem('authToken', token);
    return { token, user };
  }

  async logout() {
    await this.client.post('/auth/logout');
    localStorage.removeItem('authToken');
  }

  // Customer
  async getCustomerProfile() {
    const response = await this.client.get('/customers/profile');
    return response.data.data;
  }

  async updateCustomerProfile(data: {
    fullName?: string;
    dateOfBirth?: string;
    gender?: 'MALE' | 'FEMALE' | 'OTHER';
  }) {
    const response = await this.client.put('/customers/profile', data);
    return response.data.data;
  }

  async getAddresses() {
    const response = await this.client.get('/customers/addresses');
    return response.data.data;
  }

  async addAddress(data: {
    recipientName: string;
    recipientPhone: string;
    addressLine: string;
    ward?: string;
    district?: string;
    city: string;
    latitude?: number;
    longitude?: number;
  }) {
    const response = await this.client.post('/customers/addresses', data);
    return response.data.data;
  }

  // Services
  async getAllServices() {
    const response = await this.client.get('/services');
    return response.data.data;
  }

  async getServiceById(id: number) {
    const response = await this.client.get(`/services/${id}`);
    return response.data.data;
  }

  // Service Requests
  async createServiceRequest(data: {
    serviceId: number;
    addressId: number;
    description: string;
    scheduledTime?: string;
  }) {
    const response = await this.client.post('/service-requests', data);
    return response.data.data;
  }

  async getMyServiceRequests() {
    const response = await this.client.get('/service-requests');
    return response.data.data;
  }

  async getServiceRequestById(id: number) {
    const response = await this.client.get(`/service-requests/${id}`);
    return response.data.data;
  }

  async cancelServiceRequest(id: number) {
    const response = await this.client.put(`/service-requests/${id}/cancel`);
    return response.data;
  }

  // Service Posts
  async createServicePost(data: {
    serviceId: number;
    addressId: number;
    title: string;
    description: string;
    estimatedBudget?: number;
    preferredTime?: string;
    type: 'URGENT' | 'CONSULTATION' | 'SCHEDULED' | 'QUOTATION';
    maxTechnicians?: number;
  }) {
    const response = await this.client.post('/service-posts', data);
    return response.data.data;
  }

  async getServicePosts() {
    const response = await this.client.get('/service-posts');
    return response.data.data;
  }

  async getServicePostResponses(postId: number) {
    const response = await this.client.get(`/service-posts/${postId}/responses`);
    return response.data.data;
  }

  // Payments
  async createPayment(data: {
    serviceRequestId: number;
    amount: number;
    paymentMethod: 'CASH' | 'BANK_TRANSFER' | 'CREDIT_CARD' | 'E_WALLET';
    notes?: string;
  }) {
    const response = await this.client.post('/payments', data);
    return response.data.data;
  }

  async getPaymentHistory() {
    const response = await this.client.get('/payments/history');
    return response.data.data;
  }

  // Feedback
  async createFeedback(data: {
    serviceRequestId: number;
    rating: number;
    comment?: string;
  }) {
    const response = await this.client.post('/feedback', data);
    return response.data.data;
  }

  // Notifications
  async getMyNotifications() {
    const response = await this.client.get('/notifications/my');
    return response.data.data;
  }

  async getUnreadCount() {
    const response = await this.client.get('/notifications/unread-count');
    return response.data.data.count;
  }

  async markAsRead(id: number) {
    await this.client.put(`/notifications/${id}/read`);
  }

  // Chat
  async getConversations() {
    const response = await this.client.get('/conversations');
    return response.data.data;
  }

  async getMessages(conversationId: number, page = 0, size = 50) {
    const response = await this.client.get(
      `/conversations/${conversationId}/messages`,
      { params: { page, size } }
    );
    return response.data.data;
  }

  async sendMessage(conversationId: number, data: {
    content: string;
    messageType?: 'TEXT' | 'IMAGE' | 'LOCATION' | 'QUOTATION' | 'FILE';
    attachmentUrl?: string;
  }) {
    const response = await this.client.post(
      `/conversations/${conversationId}/messages`,
      data
    );
    return response.data.data;
  }

  // Technician (if role is TECHNICIAN)
  async getTechnicianProfile() {
    const response = await this.client.get('/technicians/profile');
    return response.data.data;
  }

  async updateTechnicianProfile(data: any) {
    const response = await this.client.put('/technicians/profile', data);
    return response.data.data;
  }

  async getActiveTechnicians() {
    const response = await this.client.get('/technicians/active');
    return response.data.data;
  }

  async acceptServiceRequest(id: number) {
    const response = await this.client.put(`/service-requests/${id}/accept`);
    return response.data;
  }

  async completeServiceRequest(id: number) {
    const response = await this.client.put(`/service-requests/${id}/complete`);
    return response.data;
  }
}

export const api = new ApiService();
export default api;
```

### Usage in React Components

```tsx
// Example: Login Component
import React, { useState } from 'react';
import api from '../services/api';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const { user, token } = await api.login(email, password);
      console.log('Logged in:', user);
      // Redirect to dashboard
      window.location.href = '/dashboard';
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <h1>Login</h1>
      {error && <div className="error">{error}</div>}
      
      <input
        type="email"
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
      />
      
      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      
      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
};
```

```tsx
// Example: Service List Component
import React, { useEffect, useState } from 'react';
import api from '../services/api';

interface Service {
  id: number;
  name: string;
  description: string;
  basePrice: number;
  status: string;
}

export const ServiceList: React.FC = () => {
  const [services, setServices] = useState<Service[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadServices();
  }, []);

  const loadServices = async () => {
    try {
      const data = await api.getAllServices();
      setServices(data);
    } catch (error) {
      console.error('Failed to load services:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h1>Available Services</h1>
      <div className="service-grid">
        {services.map((service) => (
          <div key={service.id} className="service-card">
            <h3>{service.name}</h3>
            <p>{service.description}</p>
            <p className="price">{service.basePrice.toLocaleString()} VND</p>
            <button onClick={() => handleBookService(service.id)}>
              Book Now
            </button>
          </div>
        ))}
      </div>
    </div>
  );

  function handleBookService(serviceId: number) {
    // Navigate to booking page
    window.location.href = `/book/${serviceId}`;
  }
};
```

## 🎨 React Hooks for API

Create custom hooks for better state management:

```typescript
// src/hooks/useAuth.ts
import { useState, useEffect } from 'react';
import api from '../services/api';

export function useAuth() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const profile = await api.getCustomerProfile();
        setUser(profile);
      } catch (error) {
        localStorage.removeItem('authToken');
      }
    }
    setLoading(false);
  };

  const login = async (email: string, password: string) => {
    const { user, token } = await api.login(email, password);
    setUser(user);
    return user;
  };

  const logout = async () => {
    await api.logout();
    setUser(null);
  };

  return { user, loading, login, logout };
}
```

```typescript
// src/hooks/useServices.ts
import { useState, useEffect } from 'react';
import api from '../services/api';

export function useServices() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadServices();
  }, []);

  const loadServices = async () => {
    try {
      setLoading(true);
      const data = await api.getAllServices();
      setServices(data);
      setError(null);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return { services, loading, error, reload: loadServices };
}
```

## 🔄 Environment Configuration

```typescript
// src/config/api.config.ts
export const API_CONFIG = {
  development: {
    baseURL: 'http://localhost:8100/api/v1',
    timeout: 10000,
  },
  staging: {
    baseURL: 'https://staging-api.fix4home.com/api/v1',
    timeout: 15000,
  },
  production: {
    baseURL: 'https://api.fix4home.com/api/v1',
    timeout: 15000,
  },
};

const env = process.env.NODE_ENV || 'development';
export const apiConfig = API_CONFIG[env as keyof typeof API_CONFIG];
```

## 🧪 Testing API Integration

```typescript
// src/__tests__/api.test.ts
import api from '../services/api';

describe('API Service', () => {
  it('should login successfully', async () => {
    const result = await api.login('test@example.com', 'password123');
    expect(result).toHaveProperty('token');
    expect(result).toHaveProperty('user');
  });

  it('should get services', async () => {
    const services = await api.getAllServices();
    expect(Array.isArray(services)).toBe(true);
  });

  it('should handle authentication errors', async () => {
    await expect(
      api.login('wrong@example.com', 'wrongpassword')
    ).rejects.toThrow();
  });
});
```

## 📱 Vue.js Integration

```typescript
// src/services/api.ts
import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8100/api/v1',
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default {
  auth: {
    login: (email: string, password: string) =>
      client.post('/auth/login', { email, password }),
    register: (data: any) => client.post('/auth/register', data),
    logout: () => client.post('/auth/logout'),
  },
  services: {
    getAll: () => client.get('/services'),
    getById: (id: number) => client.get(`/services/${id}`),
  },
  // ... more endpoints
};
```

```vue
<!-- Example Vue Component -->
<template>
  <div>
    <h1>Services</h1>
    <div v-if="loading">Loading...</div>
    <div v-else>
      <div v-for="service in services" :key="service.id">
        <h3>{{ service.name }}</h3>
        <p>{{ service.description }}</p>
        <p>{{ service.basePrice }} VND</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/services/api';

const services = ref([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const response = await api.services.getAll();
    services.value = response.data.data;
  } catch (error) {
    console.error('Failed to load services:', error);
  } finally {
    loading.value = false;
  }
});
</script>
```

## 🎯 Best Practices

### 1. Error Handling

```typescript
// Create a centralized error handler
export function handleApiError(error: any) {
  if (error.response) {
    // Server responded with error
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        return 'Invalid request. Please check your input.';
      case 401:
        localStorage.removeItem('authToken');
        window.location.href = '/login';
        return 'Please login again.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'Resource not found.';
      case 500:
        return 'Server error. Please try again later.';
      default:
        return data.message || 'An error occurred.';
    }
  } else if (error.request) {
    // Request made but no response
    return 'No response from server. Please check your connection.';
  } else {
    // Error in request setup
    return error.message;
  }
}
```

### 2. Loading States

```typescript
// Create a loading state hook
import { useState } from 'react';

export function useApiCall<T>(apiFunction: (...args: any[]) => Promise<T>) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = async (...args: any[]) => {
    try {
      setLoading(true);
      setError(null);
      const result = await apiFunction(...args);
      setData(result);
      return result;
    } catch (err: any) {
      const errorMessage = handleApiError(err);
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { data, loading, error, execute };
}

// Usage
const { data, loading, error, execute } = useApiCall(api.getAllServices);

useEffect(() => {
  execute();
}, []);
```

### 3. TypeScript Types

Generate types from OpenAPI spec or define manually:

```typescript
// src/types/api.types.ts
export interface User {
  id: number;
  username: string;
  email: string;
  phoneNumber: string;
  role: 'CUSTOMER' | 'TECHNICIAN' | 'ADMIN';
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING_APPROVAL' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
}

export interface Service {
  id: number;
  name: string;
  description: string;
  basePrice: number;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface ServiceRequest {
  id: number;
  customerId: number;
  serviceId: number;
  technicianId?: number;
  addressId: number;
  description: string;
  status: 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  scheduledTime?: string;
  completedTime?: string;
  price?: number;
  createdAt: string;
}

// ... more types
```

## 🚀 Next Steps

1. **Test the API** - Open `swagger-ui.html` and try out endpoints
2. **Generate Client** - Run the generation script for your language
3. **Integrate** - Copy the service file to your project
4. **Build** - Start building your frontend features
5. **Deploy** - Configure production API URLs

## 📚 Additional Resources

- [OpenAPI Documentation](./README.md)
- [Swagger UI](./swagger-ui.html)
- [API Guide](../api/README_API.md)
- [Postman Collection](../collections/)

## 🆘 Need Help?

If you encounter issues:
1. Check the [API Guide](../api/README_API.md)
2. Test endpoints in Swagger UI
3. Verify your authentication token
4. Check network console for errors
5. Contact the backend team

Happy coding! 🎉

