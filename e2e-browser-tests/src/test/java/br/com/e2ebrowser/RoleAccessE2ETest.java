package br.com.e2ebrowser;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PageAssertions;
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

            // Direct navigation must also be blocked server-side by the route guard, not just
            // hidden in the UI. The warning toast the guard fires auto-dismisses after 4s
            // (v-snackbar timeout), which races against page load — assert on the redirect
            // itself (a stable postcondition) rather than the transient toast text.
            clientPage.navigate(FRONTEND_URL + "/clients");
            assertThat(clientPage).hasURL(FRONTEND_URL + "/",
                    new PageAssertions.HasURLOptions().setTimeout(ciAwareTimeoutMs(30_000, 45_000)));
        }
    }

    @Test
    void regularUserRoleCannotReachTheClientsPageInTheUiEither() {
        login(page, "demo", "demo123");

        assertThat(page.locator(".v-navigation-drawer >> text=Clients")).hasCount(0);

        page.navigate(FRONTEND_URL + "/clients");
        assertThat(page).hasURL(FRONTEND_URL + "/",
                new PageAssertions.HasURLOptions().setTimeout(ciAwareTimeoutMs(30_000, 45_000)));
    }
}
