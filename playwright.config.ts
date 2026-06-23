import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,          // run tests serially for CRUD ordering
  retries: 0,
  timeout: 30_000,
  reporter: [
    ['list'],                    // console output
    ['allure-playwright'],       // generates allure-results/
  ],
  use: {
    baseURL: 'http://localhost:3000',
    extraHTTPHeaders: {
      'Content-Type': 'application/json',
    },
  },

  /* Start the NestJS server before running tests */
  webServer: {
    command: 'npm run start',
    port: 3000,
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
    stdout: 'pipe',
    stderr: 'pipe',
  },

  /* Projects: setup (login) runs first, then the main tests */
  projects: [
    {
      name: 'login',
      testMatch: /auth\.setup\.ts/,
    },
    {
      name: 'default',
      testMatch: /.*\.spec\.ts/,
      testIgnore: /auth\.setup\.ts/,
      dependencies: ['login'],
    },
  ],
});
