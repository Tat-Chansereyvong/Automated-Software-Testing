import { test, expect } from '@playwright/test';

/**
 * Exercise 3 — Full CRUD Spec
 * Flow: create → read → update → delete → verify 404
 *
 * Exercise 6 — Enriched with test.step, severity annotation, and response attachment
 */
test.describe.serial('Products CRUD', () => {
  let createdId: number;

  // ─── CREATE ────────────────────────────────────────────────
  test('POST /products — create a new product', async ({ request }) => {
    // Exercise 6: severity annotation
    test.info().annotations.push({ type: 'severity', description: 'critical' });

    let response: any;

    // Exercise 6: test.step for structured reporting
    await test.step('Send POST request with product data', async () => {
      response = await request.post('/products', {
        data: {
          name: 'Laptop',
          description: 'A powerful laptop',
          price: 999.99,
        },
      });
    });

    await test.step('Verify 201 status', async () => {
      expect(response.status()).toBe(201);
    });

    const body = await response.json();

    await test.step('Verify response body has correct fields', async () => {
      expect(body).toHaveProperty('id');
      expect(body.name).toBe('Laptop');
      expect(body.description).toBe('A powerful laptop');
      expect(body.price).toBe(999.99);
    });

    // Exercise 6: attach response body to Allure report
    await test.info().attach('response body', {
      body: JSON.stringify(body, null, 2),
      contentType: 'application/json',
    });

    createdId = body.id;
  });

  // ─── READ ──────────────────────────────────────────────────
  test('GET /products/:id — read the created product', async ({ request }) => {
    const response = await request.get(`/products/${createdId}`);
    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.id).toBe(createdId);
    expect(body.name).toBe('Laptop');
  });

  // ─── UPDATE ────────────────────────────────────────────────
  test('PATCH /products/:id — update the product', async ({ request }) => {
    const response = await request.patch(`/products/${createdId}`, {
      data: {
        name: 'Gaming Laptop',
        price: 1499.99,
      },
    });
    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.name).toBe('Gaming Laptop');
    expect(body.price).toBe(1499.99);
    // description should remain unchanged
    expect(body.description).toBe('A powerful laptop');
  });

  // ─── DELETE ────────────────────────────────────────────────
  test('DELETE /products/:id — delete the product', async ({ request }) => {
    const response = await request.delete(`/products/${createdId}`);
    expect(response.status()).toBe(204);
  });

  // ─── VERIFY 404 ────────────────────────────────────────────
  test('GET /products/:id — verify 404 after deletion', async ({ request }) => {
    const response = await request.get(`/products/${createdId}`);
    expect(response.status()).toBe(404);
  });
});
