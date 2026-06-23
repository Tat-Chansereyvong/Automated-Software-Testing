import { test as setup } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Exercise 5 — Auth Setup Project
 * Logs in and saves the access token to a JSON file
 * so other tests can reuse it.
 */
const AUTH_FILE = path.join(__dirname, '..', '.auth', 'token.json');

setup('login and save token', async ({ request }) => {
  // POST to /auth/login with demo credentials
  const response = await request.post('/auth/login', {
    data: {
      username: 'admin',
      password: 'admin123',
    },
  });

  setup.expect(response.status()).toBe(201);

  const body = await response.json();
  setup.expect(body).toHaveProperty('access_token');

  // Save the token to a shared file
  const dir = path.dirname(AUTH_FILE);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  fs.writeFileSync(AUTH_FILE, JSON.stringify(body, null, 2));
  console.log('✅ Auth token saved to', AUTH_FILE);
});
