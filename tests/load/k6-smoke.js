import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 10,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost';

export default function () {
  // Simple health checks
  let res = http.get(`${BASE_URL}/api/actuator/health`);
  check(res, {
    'gateway healthy': (r) => r.status === 200,
  });

  // Optional: lightweight login if credentials provided
  const email = __ENV.K6_EMAIL;
  const password = __ENV.K6_PASSWORD;

  if (email && password) {
    const loginRes = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email, password }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    check(loginRes, {
      'login ok': (r) => r.status === 200,
    });
  }

  sleep(1);
}


