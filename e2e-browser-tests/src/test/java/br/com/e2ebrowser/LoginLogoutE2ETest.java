package br.com.e2ebrowser;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class LoginLogoutE2ETest extends BaseBrowserE2ETest {

    @Test
    void adminLogsInAndSeesTheFullNavigation() {
        login(page, "admin", "admin123");

        assertThat(page.locator(".v-navigation-drawer >> text=Dashboard")).isVisible();
        assertThat(page.locator(".v-navigation-drawer >> text=Clients")).isVisible();
        assertThat(page.locator(".v-navigation-drawer >> text=Orders")).isVisible();
        assertThat(page.locator("button:has-text('admin')")).isVisible();
    }

    @Test
    void regularUserLogsInWithoutSeeingTheClientsNavItem() {
        login(page, "demo", "demo123");

        assertThat(page.locator(".v-navigation-drawer >> text=Dashboard")).isVisible();
        assertThat(page.locator(".v-navigation-drawer >> text=Orders")).isVisible();
        assertThat(page.locator(".v-navigation-drawer >> text=Clients")).hasCount(0);
    }

    @Test
    void adminCanLogOutAndIsSentBackToTheLoginPage() {
        login(page, "admin", "admin123");

        page.click("button:has-text('admin')");
        page.click("text=Logout");

        page.waitForSelector("#username", new Page.WaitForSelectorOptions().setTimeout(30_000));
        assertThat(page.locator("#username")).isVisible();
    }
}
