package br.com.e2ebrowser;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class ClientCrudE2ETest extends BaseBrowserE2ETest {

    @Test
    void adminCreatesAClientAndReceivesTheCreationNotification() {
        login(page, "admin", "admin123");

        String name = "E2E Browser Client";
        String email = uniqueEmail();
        String cpf = generateValidCpf();

        createClientViaUi(page, name, email, cpf);

        assertThat(page.locator("table")).containsText(email);
        assertThat(page.locator("table")).containsText(name);

        // client-service -> Kafka -> notification-service broadcasts CLIENT_CREATED to every
        // connected ADMIN session over the WebSocket topic.
        page.click("button:has(.mdi-bell-outline)");
        assertThat(page.locator(".v-list-item-title", new Page.LocatorOptions().setHasText("Novo cliente cadastrado")).first()).isVisible();
        assertThat(page.locator(".v-list-item-subtitle", new Page.LocatorOptions().setHasText(name)).first()).isVisible();
    }

    @Test
    void adminEditsAnExistingClient() {
        login(page, "admin", "admin123");

        String originalName = "E2E Original Name";
        String email = uniqueEmail();
        String cpf = generateValidCpf();
        createClientViaUi(page, originalName, email, cpf);

        String updatedName = "E2E Updated Name";
        page.locator("tr", new Page.LocatorOptions().setHasText(email))
                .locator("button:has(.mdi-pencil-outline)")
                .click();

        page.getByLabel("Full Name").fill(updatedName);
        page.click(".v-dialog button:has-text('Save Changes')");
        page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("build/debug-after-save-click.png")));

        assertThat(page.locator("text=Client updated successfully")).isVisible();
        assertThat(page.locator("table")).containsText(updatedName);
    }
}
