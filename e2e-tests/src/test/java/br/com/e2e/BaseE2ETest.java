package br.com.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

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

public abstract class BaseE2ETest {

    private static final String GATEWAY_BASE_URL = "http://localhost:8080";
    private static final File COMPOSE_FILE = resolveComposeFile();
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(5);

    // flag to ensure that the stack is started only once, even if multiple test classes extend BaseE2ETest
    private static volatile boolean stackStarted = false;

    @BeforeAll
    static synchronized void startStack() throws Exception {
        if (stackStarted) {
            RestAssured.baseURI = GATEWAY_BASE_URL;
            return;
        }

        composeCommand("up", "--wait", "-d");

        // wait for all services to be healthy
        waitForHealth("api-gateway", "http://localhost:8080/actuator/health");
        waitForHealth("client-service", "http://localhost:8081/actuator/health");
        waitForHealth("order-service", "http://localhost:8082/actuator/health");

        // wait for gateway to be routing (Eureka can take few minutes sometimes)
        waitForGatewayRouting();

        stackStarted = true;

        // shutdown the whole stack (after all tests are done)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("[E2E] Shutdown hook: stopping Docker Compose stack...");
                composeCommand("down", "--volumes");
            } catch (Exception e) {
                System.err.println("[E2E] Error stopping stack: " + e.getMessage());
            }
        }));

        RestAssured.baseURI = GATEWAY_BASE_URL;
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
        System.out.println("[E2E] Waiting for " + name + "...");
        while (Instant.now().isBefore(deadline)) {
            try {
                int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                if (status == 200) {
                    System.out.println("[E2E] " + name + " is UP");
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
    private static void waitForGatewayRouting() throws Exception {
        String probeUrl = GATEWAY_BASE_URL + "/client/00000000-0000-0000-0000-000000000000";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(probeUrl))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        Instant deadline = Instant.now().plus(STARTUP_TIMEOUT);
        System.out.println("[E2E] Waiting for API Gateway routing (Eureka registration)...");
        while (Instant.now().isBefore(deadline)) {
            try {
                int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                if (status != 502 && status != 503 && status != 504) {
                    System.out.println("[E2E] API Gateway routing is READY (status=" + status + ")");
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
}
