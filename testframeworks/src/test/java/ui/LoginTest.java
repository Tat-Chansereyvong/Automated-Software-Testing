package ui;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.LoginPage;

public class LoginTest extends BaseTest {

    @DataProvider(name = "loginData")
    public Object[][] getData() {
        return new Object[][] {
                { "standard_user", "secret_sauce", true },
                { "locked_out_user", "secret_sauce", false },
                { "invalid_user", "invalid_pass", false }
        };
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String username, String password, boolean expectedSuccess) {
        LoginPage loginPage = new LoginPage(driver);
        driver.get("https://www.saucedemo.com/");

        loginPage.login(username, password);

        if (!expectedSuccess) {
            Assert.assertTrue(loginPage.getErrorMessage().contains("Epic sadface"));
        } else {
            Assert.assertEquals(driver.getCurrentUrl(), "https://www.saucedemo.com/inventory.html");
        }
    }
}