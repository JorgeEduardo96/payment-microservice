package br.com.e2ebrowser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Boots the full stack via docker-compose-e2e.yml (same compose file used by the REST-Assured
 * e2e-tests module, now with a "frontend" service added) and drives it through a real Chromium
 * browser with Playwright. Unlike e2e-tests — which authenticates via ROPC and never touches a
 * browser — every test here goes through the actual Authorization Code + PKCE flow: navigate to
 * the app, get redirected to Keycloak's hosted login page, type credentials, and land back on the
 * frontend with a real session.
 */
public abstract class BaseBrowserE2ETest {

    protected static final String FRONTEND_URL = "http://localhost:8000";
    private static final String GATEWAY_BASE_URL = "http://localhost:8080";
    private static final String KEYCLOAK_BASE_URL = "http://localhost:8180";
    private static final String REALM = "payment-microservice";
    private static final File COMPOSE_FILE = resolveComposeFile();
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(5);

    private static volatile boolean stackStarted = false;

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static synchronized void startStackAndBrowser() throws Exception {
        if (!stackStarted) {
            composeCommand("up", "--wait", "-d");

            waitForHealth("keycloak", KEYCLOAK_BASE_URL + "/realms/" + REALM);
            waitForHealth("api-gateway", GATEWAY_BASE_URL + "/actuator/health");
            waitForHealth("client-service", "http://localhost:8081/actuator/health");
            waitForHealth("order-service", "http://localhost:8082/actuator/health");
            waitForHealth("frontend", FRONTEND_URL);

            String adminToken = fetchAccessToken("admin", "admin123");
            waitForGatewayRouting(adminToken);

            stackStarted = true;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("[Browser E2E] Shutdown hook: stopping Docker Compose stack...");
                    composeCommand("down", "--volumes");
                } catch (Exception e) {
                    System.err.println("[Browser E2E] Error stopping stack: " + e.getMessage());
                }
            }));
        }

        playwright = Playwright.create();
        boolean headless = !"false".equalsIgnoreCase(System.getProperty("headless", "true"));
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            // Playwright's bundled Chromium requires the MSVC runtime to be present on Windows;
            // rather than depending on that, drive the system's installed Edge (Chromium-based)
            // for local dev. CI (Linux) keeps using the bundled Chromium downloaded above.
            options.setChannel("msedge");
        }
        browser = playwright.chromium().launch(options);
    }

    @AfterAll
    static void stopBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void newContextAndPage() {
        context = newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    /** A fresh, isolated browser context (own cookies/localStorage) — use for a second logged-in user. */
    protected BrowserContext newContext() {
        return browser.newContext();
    }

    /**
     * Drives the real Authorization Code + PKCE flow: opens the app, fills Keycloak's hosted
     * login form, and waits until the SPA has finished the callback round-trip and rendered the
     * authenticated dashboard.
     */
    protected void login(Page targetPage, String username, String password) {
        targetPage.navigate(FRONTEND_URL);
        targetPage.waitForSelector("#username", new Page.WaitForSelectorOptions().setTimeout(30_000));
        targetPage.fill("#username", username);
        targetPage.fill("#password", password);
        targetPage.click("#kc-login");
        targetPage.waitForSelector(
                ".v-navigation-drawer >> text=Dashboard",
                new Page.WaitForSelectorOptions().setTimeout(30_000));
    }

    /**
     * Creates a client through the real Clients UI (as an already-logged-in admin) and waits for
     * the success toast. Client registration also provisions a matching Keycloak login for that
     * client (username = email, password "client123", role CLIENT) — see client-service.
     */
    protected void createClientViaUi(Page adminPage, String name, String email, String cpf) {
        adminPage.click(".v-navigation-drawer >> text=Clients");
        adminPage.click("button:has-text('New Client')");
        adminPage.getByLabel("Full Name").fill(name);
        adminPage.getByLabel("Email").fill(email);
        adminPage.getByLabel("CPF").fill(cpf);
        adminPage.click(".v-dialog button:has-text('Create Client')");
        adminPage.waitForSelector("text=Client created successfully", new Page.WaitForSelectorOptions().setTimeout(15_000));
    }

    /**
     * Fetches a real access token from Keycloak via the Resource Owner Password Credentials grant,
     * for API-based test-data setup only (never for the assertions themselves, which always go
     * through the real UI). Only enabled on the "frontend" client in this e2e realm.
     */
    protected static String fetchAccessToken(String username, String password) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String form = "grant_type=password&client_id=frontend&username=" + username + "&password=" + password;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(KEYCLOAK_BASE_URL + "/realms/" + REALM + "/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch token for " + username + ": " + response.statusCode() + " - " + response.body());
        }

        Matcher matcher = Pattern.compile("\"access_token\":\"([^\"]+)\"").matcher(response.body());
        if (!matcher.find()) {
            throw new RuntimeException("No access_token in Keycloak response: " + response.body());
        }
        return matcher.group(1);
    }

    // Helpers

    protected static String uniqueEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@e2e.com";
    }

    protected static String generateValidCpf() {
        Random random = new Random();
        int[] digits = new int[9];

        do {
            for (int i = 0; i < 9; i++) {
                digits[i] = random.nextInt(10);
            }
        } while (allSameDigits(digits));

        int sum = 0;
        for (int i = 0; i < 9; i++) sum += digits[i] * (10 - i);
        int d1 = 11 - (sum % 11);
        if (d1 >= 10) d1 = 0;

        sum = 0;
        for (int i = 0; i < 9; i++) sum += digits[i] * (11 - i);
        sum += d1 * 2;
        int d2 = 11 - (sum % 11);
        if (d2 >= 10) d2 = 0;

        return String.format("%d%d%d.%d%d%d.%d%d%d-%d%d",
                digits[0], digits[1], digits[2],
                digits[3], digits[4], digits[5],
                digits[6], digits[7], digits[8],
                d1, d2);
    }

    // Internal infrastructure methods

    private static void composeCommand(String... args) throws Exception {
        String[] full = new String[4 + args.length];
        full[0] = "docker";
        full[1] = "compose";
        full[2] = "-f";
        full[3] = COMPOSE_FILE.getAbsolutePath();
        System.arraycopy(args, 0, full, 4, args.length);

        ProcessBuilder pb = new ProcessBuilder(full);
        pb.inheritIO();
        int exit = pb.start().waitFor();
        if (exit != 0) {
            throw new RuntimeException("docker compose " + String.join(" ", args) + " failed (exit " + exit + ")");
        }
    }

    private static void waitForHealth(String name, String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        Instant deadline = Instant.now().plus(STARTUP_TIMEOUT);
        System.out.println("[Browser E2E] Waiting for " + name + "...");
        while (Instant.now().isBefore(deadline)) {
            try {
                int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                if (status == 200) {
                    System.out.println("[Browser E2E] " + name + " is UP");
                    return;
                }
            } catch (Exception ignored) {
            }
            Thread.sleep(3_000);
        }
        throw new RuntimeException("Timeout waiting for " + name);
    }

    /**
     * Wait until API Gateway can route to client-service via Eureka.
     * A GET /client/{uuid} with a random UUID should return 404 (not 502/503) when routing is ready.
     */
    private static void waitForGatewayRouting(String adminToken) throws Exception {
        String probeUrl = GATEWAY_BASE_URL + "/client/00000000-0000-0000-0000-000000000000";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(probeUrl))
                .timeout(Duration.ofSeconds(3))
                .header("Authorization", "Bearer " + adminToken)
                .GET()
                .build();

        Instant deadline = Instant.now().plus(STARTUP_TIMEOUT);
        System.out.println("[Browser E2E] Waiting for API Gateway routing (Eureka registration)...");
        while (Instant.now().isBefore(deadline)) {
            try {
                int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                if (status != 502 && status != 503 && status != 504) {
                    System.out.println("[Browser E2E] API Gateway routing is READY (status=" + status + ")");
                    return;
                }
            } catch (Exception ignored) {
            }
            Thread.sleep(3_000);
        }
        throw new RuntimeException("Timeout waiting for API Gateway routing");
    }

    private static boolean allSameDigits(int[] digits) {
        for (int i = 1; i < digits.length; i++) {
            if (digits[i] != digits[0]) return false;
        }
        return true;
    }

    private static File resolveComposeFile() {
        return Paths.get(System.getProperty("user.dir"))
                .resolve("../docker-compose-e2e.yml")
                .normalize()
                .toAbsolutePath()
                .toFile();
    }

    /**
     * Scales a wait margin up for CI's slower, shared runners (e.g. Kafka consumer group
     * rebalancing on a freshly-started broker can take noticeably longer there than on a local
     * dev machine).
     */
    protected static long ciAwareTimeoutMs(long localTimeoutMs, long ciTimeoutMs) {
        return System.getProperty("CI", "").isEmpty() ? localTimeoutMs : ciTimeoutMs;
    }
}
