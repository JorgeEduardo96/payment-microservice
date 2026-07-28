package br.com.e2ebrowser;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class OrderPaymentFlowE2ETest extends BaseBrowserE2ETest {

    @Test
    void clientPlacesAnOrderAndSeesTheStatusAndPaymentNotificationUpdateReactively() {
        login(page, "admin", "admin123");

        String name = "E2E Order Flow Client";
        String email = uniqueEmail();
        String cpf = generateValidCpf();
        createClientViaUi(page, name, email, cpf);

        try (BrowserContext clientContext = newContext()) {
            Page clientPage = clientContext.newPage();
            login(clientPage, email, "client123");

            clientPage.click(".v-navigation-drawer >> text=Orders");
            clientPage.click("button:has-text('New Order')");

            clientPage.getByLabel("Total (R$)").fill("150.00");
            // Vuetify's VSelect overlays a non-interactive helper div on top of the readonly
            // combobox input, which intercepts a normal click — force it, matching how a real
            // click on the field's visible surface (not the exact input coordinates) behaves.
            clientPage.getByLabel("Payment Method").click(new Locator.ClickOptions().setForce(true));
            clientPage.click(".v-list-item:has-text('Card')");
            clientPage.getByLabel("Shipping Address").fill("Rua E2E, 123");
            clientPage.click(".v-dialog button:has-text('Place Order')");

            assertThat(clientPage.locator("text=Order placed!")).isVisible();

            // order-service updates the status asynchronously — payment-service simulates a ~3s
            // processing delay, then publishes to Kafka, and the row must flip away from
            // "Pending Payment" to a final status WITHOUT a manual refresh, driven purely by the
            // WebSocket push handled in the notifications store.
            Locator statusCell = clientPage.locator("table tbody tr").first().locator("td").nth(3);
            assertThat(statusCell).not().containsText(
                    "Pending Payment",
                    new LocatorAssertions.ContainsTextOptions().setTimeout(20_000));

            boolean paid = statusCell.textContent().contains("Paid");
            String expectedNotificationTitle = paid ? "Payment confirmed" : "Payment failed";

            clientPage.click("button:has(.mdi-bell-outline)");
            assertThat(clientPage.locator(
                    ".v-list-item-title", new Page.LocatorOptions().setHasText(expectedNotificationTitle)).first())
                    .isVisible();
        }
    }
}
