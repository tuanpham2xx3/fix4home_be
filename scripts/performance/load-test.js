// Load Testing Script for Fix4Home Backend using K6
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    // Ramp up to 10 users over 2 minutes
    { duration: '2m', target: 10 },
    // Stay at 10 users for 5 minutes
    { duration: '5m', target: 10 },
    // Ramp up to 50 users over 2 minutes
    { duration: '2m', target: 50 },
    // Stay at 50 users for 5 minutes
    { duration: '5m', target: 50 },
    // Ramp up to 100 users over 2 minutes
    { duration: '2m', target: 100 },
    // Stay at 100 users for 5 minutes
    { duration: '5m', target: 100 },
    // Ramp down to 0 users over 2 minutes
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests must be below 2s
    http_req_failed: ['rate<0.1'], // Error rate must be below 10%
    errors: ['rate<0.1'], // Custom error rate must be below 10%
  },
};

// Base URL for the API
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8100';

// Test data
const TEST_USER = {
  email: 'loadtest@fix4home.com',
  password: 'LoadTest123!',
  fullName: 'Load Test User',
  phoneNumber: '0123456789',
  userType: 'CUSTOMER'
};

let authToken = '';

export function setup() {
  // Setup phase - register a test user and get auth token
  console.log('Setting up load test...');
  
  // Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  check(healthResponse, {
    'Health check status is 200': (r) => r.status === 200,
  });

  return { token: authToken };
}

export default function(data) {
  // Test scenarios with weighted distribution
  const scenario = Math.random();
  
  if (scenario < 0.3) {
    testAuthentication();
  } else if (scenario < 0.6) {
    testServiceSearch();
  } else if (scenario < 0.8) {
    testTechnicianProfile();
  } else {
    testFileOperations();
  }
  
  sleep(1); // Wait between requests
}

function testAuthentication() {
  const group = 'Authentication';
  
  // Login test
  const loginPayload = {
    email: TEST_USER.email,
    password: TEST_USER.password
  };
  
  const loginResponse = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify(loginPayload),
    {
      headers: {
        'Content-Type': 'application/json',
      },
      tags: { name: 'login', group: group },
    }
  );
  
  const loginSuccess = check(loginResponse, {
    'Login status is 200': (r) => r.status === 200,
    'Login response has token': (r) => {
      try {
        const json = JSON.parse(r.body);
        return json.accessToken !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
  
  if (!loginSuccess) {
    errorRate.add(1);
  }
  
  // Extract token for authenticated requests
  if (loginResponse.status === 200) {
    try {
      const responseData = JSON.parse(loginResponse.body);
      authToken = responseData.accessToken;
    } catch (e) {
      console.error('Failed to parse login response:', e);
    }
  }
}

function testServiceSearch() {
  const group = 'Service Search';
  
  // Search services
  const searchResponse = http.get(
    `${BASE_URL}/api/v1/services/search?keyword=plumbing&page=0&size=10`,
    {
      headers: authToken ? { 'Authorization': `Bearer ${authToken}` } : {},
      tags: { name: 'service-search', group: group },
    }
  );
  
  const searchSuccess = check(searchResponse, {
    'Service search status is 200': (r) => r.status === 200,
    'Service search has content': (r) => {
      try {
        const json = JSON.parse(r.body);
        return json.content !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
  
  if (!searchSuccess) {
    errorRate.add(1);
  }
  
  // Get service categories
  const categoriesResponse = http.get(
    `${BASE_URL}/api/v1/services/categories`,
    {
      tags: { name: 'service-categories', group: group },
    }
  );
  
  check(categoriesResponse, {
    'Categories status is 200': (r) => r.status === 200,
  });
}

function testTechnicianProfile() {
  const group = 'Technician Profile';
  
  if (!authToken) {
    console.log('No auth token available for technician profile test');
    return;
  }
  
  // Get technician profiles
  const profilesResponse = http.get(
    `${BASE_URL}/api/v1/technicians?page=0&size=10`,
    {
      headers: { 'Authorization': `Bearer ${authToken}` },
      tags: { name: 'technician-profiles', group: group },
    }
  );
  
  const profilesSuccess = check(profilesResponse, {
    'Technician profiles status is 200': (r) => r.status === 200,
    'Technician profiles has content': (r) => {
      try {
        const json = JSON.parse(r.body);
        return json.content !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
  
  if (!profilesSuccess) {
    errorRate.add(1);
  }
}

function testFileOperations() {
  const group = 'File Operations';
  
  // Test file metadata endpoint (without actual file upload to reduce load)
  const metadataResponse = http.get(
    `${BASE_URL}/api/v1/files/metadata/example.jpg`,
    {
      headers: authToken ? { 'Authorization': `Bearer ${authToken}` } : {},
      tags: { name: 'file-metadata', group: group },
    }
  );
  
  // This might return 404, which is acceptable for load testing
  check(metadataResponse, {
    'File metadata response received': (r) => r.status === 404 || r.status === 200,
  });
}

export function teardown(data) {
  console.log('Load test completed.');
  
  // Optional cleanup
  if (authToken) {
    // Logout if needed
    const logoutResponse = http.post(
      `${BASE_URL}/api/v1/auth/logout`,
      {},
      {
        headers: { 'Authorization': `Bearer ${authToken}` },
      }
    );
    
    console.log('Logout status:', logoutResponse.status);
  }
}

export function handleSummary(data) {
  return {
    'load-test-results.json': JSON.stringify(data, null, 2),
    stdout: '\n' + JSON.stringify(data, null, 2) + '\n',
  };
}
