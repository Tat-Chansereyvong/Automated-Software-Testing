import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Exercise 5 — Auth Protected Route Tests
 * Reads the saved token from the login setup project
 * and uses it to access a protected endpoint.
 */
const AUTH_FILE = path.join(__dirname, '..', '.auth', 'token.json');

let accessToken: string;

test.beforeAll(() => {
  // Read the token saved by the 'login' setup project
  const data = JSON.parse(fs.readFileSync(AUTH_FILE, 'utf-8'));
  accessToken = data.access_token;
});

test.describe('Auth Protected Routes', () => {
  test('GET /auth/profile — access with valid token', async ({ request }) => {
    const response = await request.get('/auth/profile', {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body).toHaveProperty('message', 'Protected data');
    expect(body.user).toHaveProperty('username', 'admin');
  });

  test('GET /auth/profile — reject without token → 401', async ({ request }) => {
    const response = await request.get('/auth/profile');

    expect(response.status()).toBe(401);
  });

  test('GET /auth/profile — reject with bad token → 401', async ({ request }) => {
    const response = await request.get('/auth/profile', {
      headers: {
        Authorization: 'Bearer invalid.token.here',
      },
    });

    expect(response.status()).toBe(401);
  });
});
