const axios = require('axios');

const API_URL = 'http://localhost:8080/api';

const randomString = () => Math.random().toString(36).substring(7);

const testUser1 = {
  email: `testuser1_${randomString()}@example.com`,
  password: 'password123',
  firstName: 'Test',
  lastName: 'User1',
};

const testUser2 = {
  email: `testuser2_${randomString()}@example.com`,
  password: 'password123',
  firstName: 'Test',
  lastName: 'User2',
};

let token1;
let userId1;
let token2;
let userId2;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const runTests = async () => {
  try {
    // Wait for services to start up
    await sleep(60000);

    // Register user 1
    console.log('Running test: Register user 1');
    const registerResponse1 = await axios.post(`${API_URL}/auth/register`, testUser1);
    userId1 = registerResponse1.data.userId;
    console.log('...passed');

    // Register user 2
    console.log('Running test: Register user 2');
    const registerResponse2 = await axios.post(`${API_URL}/auth/register`, testUser2);
    userId2 = registerResponse2.data.userId;
    console.log('...passed');

    // Log in with user 1
    console.log('Running test: Log in with user 1');
    const loginResponse1 = await axios.post(`${API_URL}/auth/login`, {
      email: testUser1.email,
      password: testUser1.password,
    });
    token1 = loginResponse1.data.token;
    console.log('...passed');

    // Log in with user 2
    console.log('Running test: Log in with user 2');
    const loginResponse2 = await axios.post(`${API_URL}/auth/login`, {
      email: testUser2.email,
      password: testUser2.password,
    });
    token2 = loginResponse2.data.token;
    console.log('...passed');

    // Get user 1's wallet
    console.log('Running test: Get user 1\'s wallet');
    await axios.get(`${API_URL}/wallet/${userId1}`, {
      headers: { Authorization: `Bearer ${token1}` },
    });
    console.log('...passed');

    // Credit user 1's wallet
    console.log('Running test: Credit user 1\'s wallet');
    await axios.post(
      `${API_URL}/wallet/${userId1}/credit`,
      { amount: 100, description: 'Initial credit' },
      { headers: { Authorization: `Bearer ${token1}` } }
    );
    console.log('...passed');

    // Debit user 1's wallet
    console.log('Running test: Debit user 1\'s wallet');
    await axios.post(
      `${API_URL}/wallet/${userId1}/debit`,
      { amount: 50, description: 'Initial debit' },
      { headers: { Authorization: `Bearer ${token1}` } }
    );
    console.log('...passed');

    // Make a payment from user 1 to user 2
    console.log('Running test: Make a payment from user 1 to user 2');
    await axios.post(
      `${API_URL}/payment`,
      { payerUserId: userId1, payeeUserId: userId2, amount: 25, currency: 'USD', description: 'Test payment' },
      { headers: { Authorization: `Bearer ${token1}` } }
    );
    console.log('...passed');

    console.log('All tests passed!');
  } catch (error) {
    console.error('Tests failed:');
    if (error.response) {
      console.error(error.response.data);
    } else {
      console.error(error.message);
    }
  }
};

runTests();
