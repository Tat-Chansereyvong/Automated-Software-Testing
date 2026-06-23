import { test, expect } from '@playwright/test';

/**
 * Exercise 4 — Negative Tests
 * - POST invalid body → 400
 * - GET non-existent id → 404
 */
test.describe('Products Negative Tests', () => {
  test('POST /products with invalid body → 400', async ({ request }) => {
    // Send a body with wrong types / missing required fields
    const response = await request.post('/products', {
      data: {
        name: '',           // empty string fails @IsNotEmpty()
        price: 'not-a-num', // string fails @IsNumber()
        // description is missing → fails @IsNotEmpty()
      },
    });

    expect(response.status()).toBe(400);

    const body = await response.json();
    // ValidationPipe returns an array of error messages
    expect(body).toHaveProperty('message');
    expect(Array.isArray(body.message)).toBeTruthy();
  });

  test('POST /products with empty body → 400', async ({ request }) => {
    const response = await request.post('/products', {
      data: {},
    });

    expect(response.status()).toBe(400);
  });

  test('GET /products/99999 (non-existent) → 404', async ({ request }) => {
    const response = await request.get('/products/99999');

    expect(response.status()).toBe(404);

    const body = await response.json();
    expect(body.message).toContain('not found');
  });

  test('PATCH /products/99999 (non-existent) → 404', async ({ request }) => {
    const response = await request.patch('/products/99999', {
      data: { name: 'Ghost' },
    });

    expect(response.status()).toBe(404);
  });

  test('DELETE /products/99999 (non-existent) → 404', async ({ request }) => {
    const response = await request.delete('/products/99999');

    expect(response.status()).toBe(404);
  });
});
