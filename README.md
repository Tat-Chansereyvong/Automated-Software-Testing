# API Testing Lab

NestJS Products API with Playwright API tests and Allure reporting.

## Quick Start

```bash
# 1. Install dependencies
npm install

# 2. Run all tests (auto-starts the NestJS server)
npx playwright test

# 3. Generate Allure report
npx allure generate allure-results -o allure-report --clean

# 4. Open Allure report in browser
npx allure open allure-report
```

## Project Structure

```
api-testing-lab/
├── src/                          # NestJS application (port 3000)
│   ├── main.ts                   # Bootstrap + ValidationPipe
│   ├── app.module.ts             # Root module
│   ├── products/                 # Products CRUD resource
│   │   ├── products.controller.ts
│   │   ├── products.service.ts
│   │   ├── dto/create-product.dto.ts
│   │   ├── dto/update-product.dto.ts
│   │   └── entities/product.entity.ts
│   └── auth/                     # Auth module (login + guard)
│       ├── auth.controller.ts
│       ├── auth.service.ts
│       └── auth.guard.ts
├── tests/                        # Playwright API tests
│   ├── auth.setup.ts             # Login setup project
│   ├── auth.spec.ts              # Auth protected route tests
│   ├── products.crud.spec.ts     # Full CRUD spec (enriched)
│   └── products.negative.spec.ts # Negative / edge-case tests
├── playwright.config.ts          # Playwright + Allure config
└── .github/workflows/api-tests.yml  # CI pipeline
```

## Lab Exercises Covered

| # | Exercise | Files |
|---|----------|-------|
| 1 | Scaffold NestJS + products resource | `src/` |
| 2 | Playwright + Allure + webServer | `playwright.config.ts` |
| 3 | Full CRUD spec | `tests/products.crud.spec.ts` |
| 4 | Negative tests (400, 404) | `tests/products.negative.spec.ts` |
| 5 | Auth login setup + protected route | `src/auth/`, `tests/auth.*` |
| 6 | test.step, severity, attachment | `tests/products.crud.spec.ts` |
| 7 | Allure report generation | `npm run allure:generate` |
| 8 | GitHub Actions CI | `.github/workflows/api-tests.yml` |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/products` | Create a product |
| GET | `/products` | List all products |
| GET | `/products/:id` | Get product by ID |
| PATCH | `/products/:id` | Update a product |
| DELETE | `/products/:id` | Delete a product |
| POST | `/auth/login` | Login (`admin`/`admin123`) |
| GET | `/auth/profile` | Protected route (requires Bearer token) |
