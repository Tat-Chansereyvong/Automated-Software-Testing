import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  // Tell Playwright exactly which folder to look in
  testDir: "./tests",

  // Explicitly tell it to find ANY file ending in .spec.ts
  testMatch: "**/*.spec.ts",

  fullyParallel: true,

  // Detailed HTML reporting
  reporter: [["html", { open: "never" }]],

  use: {
    trace: "on-first-retry",
    // Automatically capture screenshots on test failure
    screenshot: "only-on-failure",
  },

  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
