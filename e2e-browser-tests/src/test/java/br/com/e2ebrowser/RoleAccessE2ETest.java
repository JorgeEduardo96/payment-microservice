package br.com.e2ebrowser;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class RoleAccessE2ETest extends BaseBrowserE2ETest {

    @Test
    void clientRoleUserCannotSeeOrReachTheClientsPage() {
        login(page, "admin", "admin123");

        String name = "E2E Role Access Client";
        String email = uniqueEmail();
        String cpf = generateValidCpf();
        createClientViaUi(page, name, email, cpf);

        try (BrowserContext clientContext = newContext()) {
            Page clientPage = clientContext.newPage();
            login(clientPage, email, "client123");

            assertThat(clientPage.locator(".v-navigation-drawer >> text=Dashboard")).isVisible();
            assertThat(clientPage.locator(".v-navigation-drawer >> text=Orders")).isVisible();
            assertThat(clientPage.locator(".v-navigation-drawer >> text=Clients")).hasCount(0);

            // Direct navigation must also be blocked server-side by the route guard, not just hidden in the UI.
            clientPage.navigate(FRONTEND_URL + "/clients");
            clientPage.waitForSelector(
                    "text=You do not have permission to access this page",
                    new Page.WaitForSelectorOptions().setTimeout(15_000));
            assertThat(clientPage).hasURL(FRONTEND_URL + "/");
        }
    }

    @Test
    void regularUserRoleCannotReachTheClientsPageInTheUiEither() {
        login(page, "demo", "demo123");

        assertThat(page.locator(".v-navigation-drawer >> text=Clients")).hasCount(0);

        page.navigate(FRONTEND_URL + "/clients");
        page.waitForSelector(
                "text=You do not have permission to access this page",
                new Page.WaitForSelectorOptions().setTimeout(15_000));
        assertThat(page).hasURL(FRONTEND_URL + "/");
    }
}
