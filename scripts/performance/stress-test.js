// Stress Testing Script for Fix4Home Backend using K6
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Stress test configuration - pushing the system beyond normal capacity
export const options = {
  stages: [
    // Ramp up quickly to 100 users
    { duration: '1m', target: 100 },
    // Ramp up to 200 users
    { duration: '2m', target: 200 },
    // Stay at 200 users for 3 minutes
    { duration: '3m', target: 200 },
    // Ramp up to 300 users (stress level)
    { duration: '2m', target: 300 },
    // Stay at 300 users for 5 minutes
    { duration: '5m', target: 300 },
    // Ramp up to 500 users (breaking point test)
    { duration: '2m', target: 500 },
    // Stay at 500 users for 3 minutes
    { duration: '3m', target: 500 },
    // Ramp down quickly
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<5000'], // 95% of requests under 5s (stress conditions)
    http_req_failed: ['rate<0.3'], // Accept higher error rate during stress
    errors: ['rate<0.3'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8100';

export default function() {
  // Aggressive testing - multiple rapid requests
  const scenario = Math.random();
  
  if (scenario < 0.4) {
    stressAuthentication();
  } else if (scenario < 0.7) {
    stressServiceOperations();
  } else {
    stressSearchOperations();
  }
  
  // Reduced sleep time for stress testing
  sleep(0.5);
}

function stressAuthentication() {
  // Rapid authentication attempts
  const loginPayload = {
    email: `stress${Math.floor(Math.random() * 1000)}@fix4home.com`,
    password: 'StressTest123!'
  };
  
  const loginResponse = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify(loginPayload),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'stress-login' },
    }
  );
  
  check(loginResponse, {
    'Stress login response received': (r) => r.status >= 200 && r.status < 500,
  }) || errorRate.add(1);
}

function stressServiceOperations() {
  // Rapid service requests
  const serviceResponse = http.get(
    `${BASE_URL}/api/v1/services/search?keyword=test&page=${Math.floor(Math.random() * 10)}`,
    { tags: { name: 'stress-services' } }
  );
  
  check(serviceResponse, {
    'Stress service response received': (r) => r.status >= 200 && r.status < 500,
  }) || errorRate.add(1);
  
  // Immediate follow-up request
  const categoriesResponse = http.get(
    `${BASE_URL}/api/v1/services/categories`,
    { tags: { name: 'stress-categories' } }
  );
  
  check(categoriesResponse, {
    'Stress categories response received': (r) => r.status >= 200 && r.status < 500,
  }) || errorRate.add(1);
}

function stressSearchOperations() {
  // Multiple rapid search requests
  const searches = [
    'plumbing',
    'electrical',
    'painting',
    'cleaning',
    'repair'
  ];
  
  const keyword = searches[Math.floor(Math.random() * searches.length)];
  
  const searchResponse = http.get(
    `${BASE_URL}/api/v1/enhanced-search?q=${keyword}&limit=20`,
    { tags: { name: 'stress-search' } }
  );
  
  check(searchResponse, {
    'Stress search response received': (r) => r.status >= 200 && r.status < 500,
  }) || errorRate.add(1);
}
