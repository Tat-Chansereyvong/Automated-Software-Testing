import { test, expect } from "@playwright/test";
import { LoginPage } from "../pages/LoginPage";

test.describe("E-commerce UI Automation", () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    await loginPage.navigate();
  });

  test("Successful login and manual screenshot capture", async ({ page }) => {
    await loginPage.login("standard_user", "secret_sauce");

    // Assert successful login by checking the URL
    await expect(page).toHaveURL(/.*inventory/);

    // Manual screenshot capture as requested in the lab
    await page.screenshot({
      path: "screenshots/successful-login.png",
      fullPage: true,
    });
  });

  test("Failed login shows error message", async () => {
    await loginPage.login("locked_out_user", "secret_sauce");

    // Assert error message visibility
    await expect(loginPage.errorMessage).toBeVisible();
    await expect(loginPage.errorMessage).toContainText(
      "Sorry, this user has been locked out.",
    );
  });
});
