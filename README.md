# Payment Microservice

## Overview

This project demonstrates a modern and resilient microservices architecture using **Spring Boot**, **Gradle**, **Apache Kafka**
as the event backbone, **WebSocket (STOMP)** for real-time, push-based communication with the frontend, and **Keycloak**
for authentication/authorization. It follows best practices for scalability, decoupling, and observability with a
didactic purpose.

---

## Architecture

- **Independent Microservices:** Each service is responsible for a specific domain (e.g., Client, Order, Payment).
- **Isolated Databases:** Each microservice maintains its own SQL database (_Database per Service_).
- **Synchronous and Asynchronous Communication:**
    - REST using Spring Web.
    - gRPC for high-performance internal communication.
    - Kafka for event-driven communication.
    - WebSocket (STOMP) for real-time notifications pushed to the frontend.
- **Authentication & Authorization:** Keycloak issues JWTs (Authorization Code + PKCE from the browser); the API Gateway
  validates every request and enforces role-based access (see [Authentication & Authorization](#authentication--authorization)).
- **Shared Library:** A common Gradle library with Kafka utilities, validations, and custom exceptions.

![Payment Microservice's diagram](payment_microservice_diagram.png)

---

## Event-Driven Architecture

- **Apache Kafka** as the event backbone.
- **Producers and Consumers** with `KafkaTemplate` and `@KafkaListener`.
- Full support for:
    - **Transactional Outbox Pattern** in `client-service` and `payment-service` — see details below.
    - **Dead Letter Queue (DLQ)** for failed messages (consumer-side, complementary to the outbox).
    - **Configurable retries** with fallback using Resilience4j.
    - **Idempotency** in consumers to avoid reprocessing.
- **Events propagate state changes** between services, reducing coupling and increasing scalability.

---

## Transactional Outbox Pattern

`client-service` and `payment-service` both persist a domain change and the corresponding Kafka event **in the same local
database transaction**, instead of writing to the database and publishing to Kafka as two separate, non-atomic steps.

### The problem it solves

Without an outbox, a service typically does:

```
1. save entity to DB           (commit)
2. publish event to Kafka      (separate, unrelated operation)
```

If the process crashes — or Kafka is unreachable — between steps 1 and 2, the database and the event stream permanently
diverge: the state change happened, but nothing downstream ever finds out (or, in the opposite ordering, an event is
published for a change that never actually got committed). This is the classic **dual-write problem**.

### How it works here

1. The business write (e.g. inserting a `Client` or a `Payment`) and an `outbox` row describing the event to be published
   are persisted **in the same `@Transactional` method**, so they either both commit or both roll back together.
2. Once that transaction **commits**, a `@TransactionalEventListener(phase = AFTER_COMMIT)` fires and hands the new
   outbox row's id to an `OutboxPublisher`, which sends it to Kafka and marks it `PUBLISHED` (or `FAILED`, with the
   error recorded, if Kafka itself is unreachable at that moment).
3. A scheduled `OutboxRecoveryJob` periodically re-publishes any `PENDING`/`FAILED` outbox rows older than a short grace
   period — this is what makes the pattern resilient to Kafka being down at decision-time: the event is never lost,
   only delayed, because it was durably persisted before anything ever touched Kafka.

### The `outbox` table

| Column           | Purpose                                                                                          |
|------------------|---------------------------------------------------------------------------------------------------|
| `id`             | Outbox row identity; also what the `AFTER_COMMIT` listener/publisher key off of.                  |
| `aggregate_type` | The kind of entity the event is about (e.g. `Client`, `Payment`) — useful for tracing/auditing.    |
| `aggregate_id`   | The id of that entity, so a given event can be traced back to the record that caused it.           |
| `event_type`     | Which topic/decision this row maps to (e.g. `client-created`, `payment-processed`).                |
| `payload`        | The serialized event body actually sent to Kafka — captured at decision time, not recomputed later.|
| `status`         | `PENDING` → `PROCESSING` → `PUBLISHED`, or `FAILED` if Kafka rejected/timed out the send.           |
| `attempts`       | How many times a (re)publish was attempted — visibility into flaky sends.                          |
| `last_error`     | The last failure message, for troubleshooting without needing to dig through logs.                 |
| `created_at`     | Drives the recovery job's "older than N seconds" retry query.                                      |
| `published_at`   | Set once the send actually succeeds — an audit trail of when the event really left the building.   |

### Why `payment-service` needed more than just an outbox table

`payment-service` used to be **entirely stateless**: a gRPC call came in, a `PAID`/`FAILED` decision was computed on the
spot, and it was published to Kafka directly — nothing was ever persisted. That meant:

- **No idempotency** — a retried gRPC call (e.g. after a client-side timeout) could re-decide and re-publish the same
  payment twice.
- **Silently lost decisions** — if the Kafka send failed, the exception was only logged; the order stayed stuck in
  `PENDING_PAYMENT` forever with zero record that a decision had ever been made.

Since the outbox pattern requires *something transactional to piggyback on*, adding it here meant introducing a real
`payment` table first, with `order_id` as a **unique, database-enforced idempotency key** — not just an in-app check,
since two concurrent requests for the same order could both pass an application-level lookup before either commits.
The DB-level unique constraint (and the `DataIntegrityViolationException` it triggers) is the real safety net for that
race. The now-durable decision is published via the same outbox mechanism described above.

### DLQ and outbox are complementary, not redundant

The outbox pattern addresses **producer-side** reliability (never losing a decision once it's made). The existing
**Dead Letter Queue** (`DeadLetterPublishingRecoverer`) addresses a different, **consumer-side** failure mode: a
message that was published successfully but that a *consumer* repeatedly fails to process (e.g. a poison-pill payload
or a transient downstream error). Both mechanisms stay in place, each covering a different half of the pipeline.

---

## Authentication & Authorization

**Keycloak** (`keycloak/realm-export.json`) is the identity provider for the whole platform. The frontend logs in via the
standard **Authorization Code + PKCE** flow; the API Gateway validates the resulting JWT on every request and maps
Keycloak's `realm_access.roles` claim into Spring Security authorities.

**Realm roles:**

| Role     | Who                                                          | Access                                                                                              |
|----------|---------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| `ADMIN`  | Staff managing the platform                                  | Full access, including client registration (`POST`/`PUT /client`) and the admin notification feed   |
| `USER`   | Regular staff (read-only)                                    | Can read clients/orders, cannot register/edit clients                                                |
| `CLIENT` | A registered customer (see below)                             | Can only see and receive real-time updates about their own orders                                    |

**Client registration also provisions a login.** When a client is registered (`POST /client`), `client-service` also creates
a matching Keycloak user (role `CLIENT`, default password `client123`, username = the client's email) via a dedicated
`client-service` service account scoped only to `manage-users`/`view-realm` on this realm — never the realm superadmin.
The client's business-entity id is stored as a custom `clientId` user attribute and surfaced in the JWT via a protocol
mapper, so the frontend and `notification-service` can tell which Client a logged-in user corresponds to.

**Real-time notifications are role-aware, not one-size-fits-all:**

- `ADMIN` sessions connect to a broadcast topic (`/topic/notifications`) and get notified when a new client registers.
- `CLIENT` sessions are bound to their own STOMP session (via the `clientId` claim) and are notified individually
  (`/user/queue/notifications`) when their order's payment is confirmed or fails — never broadcast to anyone else.
- `notification-service`'s `StompAuthChannelInterceptor` enforces this at the STOMP `CONNECT` frame itself, since the
  Gateway can only see the initial HTTP upgrade request, not the STOMP-level `Authorization` header sent afterward.

**Payment processing** is decided (`PAID`/`FAILED`) and durably persisted (`payment-service`) before being published to
Kafka via the transactional outbox pattern (see [Transactional Outbox Pattern](#transactional-outbox-pattern) below) —
`order-service` updates the order's status from that event, and `notification-service` pushes the WebSocket update plus
(for `PAID`) a confirmation email via SendGrid.

---

## Internationalization (i18n)

The frontend supports **English, Portuguese, and Spanish** via `vue-i18n`.

- Translation keys live in `frontend/src/locales/{en,pt,es}.json`, mirrored 1:1 across the three files.
- All view/component templates (`App.vue`, `HomeView`, `ClientsView`, `OrdersView`, `CallbackView`, `NotificationBell`,
  route titles) render text through `t()`/`$t()` — no hardcoded UI strings.
- A language switcher (globe icon in the app bar) lets the user change locale at runtime; the choice is persisted to
  `localStorage` and restored on reload. English is the default when nothing is stored.
- Locale-sensitive formatting (e.g. notification timestamps) follows the active locale instead of a fixed one.

---

## Best Practices Followed

- Clear separation between domains, layers, and responsibilities.
- **Transactional Outbox Pattern** (`client-service`, `payment-service`) for reliable event publishing — see
  [Transactional Outbox Pattern](#transactional-outbox-pattern).
- Retry and Fallback with **Resilience4j**.
- **Unit, integration, and E2E tests** with significant coverage.
- Observability using:
    - **Zipkin** (distributed tracing).
    - **Prometheus + Grafana** (metrics and dashboards).
- Docker containers orchestrated via `docker compose`.
- CI with **GitHub Actions** — build, E2E tests, and push on the `main` branch.
- **Eureka** for service discovery.
- Global validations and standardized exception handling.
- Environment profiles (`application-docker.yml`, `application-dev.yml`, etc).

---

## Project Structure

```
payment-microservice/
├── api-gateway/           # API Gateway — Spring Cloud Gateway (JWT validation, routing)
├── client-service/        # Client microservice (also provisions Keycloak logins)
├── notification-service/  # Notification microservice (SendGrid + WebSocket/STOMP)
├── order-service/         # Order microservice
├── payment-service/       # Payment microservice (gRPC server)
├── service-registry/      # Eureka Server
├── shared-lib/            # Common shared library
├── frontend/              # Vue 3 + Vuetify SPA
├── keycloak/              # Realm exports (roles, clients, seed users)
├── e2e-tests/             # End-to-end tests (REST API, full stack)
├── e2e-browser-tests/     # End-to-end tests (real browser via Playwright, full stack)
├── docker-compose.yml     # Full stack with observability tools
└── docker-compose-e2e.yml # Lightweight stack (incl. frontend) for both E2E modules
```

---

## Testing Strategy

The project follows the **test pyramid** with three layers:

```
        /\
       /E2E\          <- full stack via Docker Compose
      /------\
     /  Integ  \      <- per service, with TestContainers (Kafka, gRPC)
    /------------\
   /  Unit Tests  \   <- isolated, per class/method
  /----------------\
```

### Unit Tests

- Location: `src/test/java` in each service
- Tools: JUnit 5, Mockito, AssertJ
- Run: `./gradlew test`

### Integration Tests

- Location: `src/integration/java` in each service
- Tools: TestContainers (Kafka), Spring Boot Test
- Run: `./gradlew integrationTest`

### End-to-End Tests (E2E)

- Location: `e2e-tests/`
- Tools: REST Assured, Awaitility, Docker Compose
- The full stack — including a Keycloak instance seeded from `keycloak/realm-export-e2e.json` — is started automatically
  before the tests and shut down after
- `BaseE2ETest` fetches a real `admin` access token from Keycloak once and attaches it to every request by default
  (via `RestAssured.requestSpecification`), so most test classes don't need to know anything about auth. The `frontend`
  client only has direct (password) grants enabled in this test realm — the real app never uses that flow.

**What is tested:**

| Scenario                                          | Endpoint                 |
|----------------------------------------------------|--------------------------|
| Create client                                      | `POST /client`           |
| Fetch client by ID                                 | `GET /client/{id}`       |
| Update client                                      | `PUT /client/{id}`       |
| Create order (starts as `PENDING_PAYMENT`)         | `POST /order`            |
| Payment processed asynchronously via Kafka         | `GET /order/client/{id}` |
| Final status matches payment-service logic         | `GET /order/client/{id}` |
| List all orders for a client                       | `GET /order/client/{id}` |
| Validation and 404 scenarios                       | Various                  |
| Request without a token is rejected (401)          | `POST /client`           |
| Non-admin (`USER`) can't register clients (403)    | `POST /client`           |
| Non-admin can still read clients (200)             | `GET /client`            |

**Important:** the `order-service` maintains its own client database populated via Kafka events.
The E2E tests account for this by waiting for the Kafka event to be consumed before placing an order.

**Run E2E tests locally:**

```bash
# Images must be available locally (build or pull first)
./gradlew :e2e-tests:test
```

> The regular `./gradlew build` does **not** run E2E tests — they are executed as a dedicated step in CI.

### Browser E2E Tests (Playwright, Java)

- Location: `e2e-browser-tests/`
- Tools: Playwright for Java, JUnit 5, Docker Compose
- Unlike `e2e-tests` (which drives the REST API directly and authenticates via ROPC), this module
  drives a real Chromium browser against the actual frontend, going through the real
  **Authorization Code + PKCE** login flow — navigating to the app, filling Keycloak's hosted
  login form, and asserting on what a user actually sees.
- The full stack — including a `frontend` container — is started from `docker-compose-e2e.yml`
  automatically before the tests and shut down after, the same way `e2e-tests` does.

**What is tested:**

| Scenario                                                                  |
|-----------------------------------------------------------------------------|
| Login/logout through the real Keycloak hosted login page                 |
| `ADMIN` sees the full navigation (Clients, Orders); `USER`/`CLIENT` don't see the Clients nav item and are blocked (with a warning toast) if they navigate to `/clients` directly |
| `ADMIN` creates and edits a client through the UI                        |
| `ADMIN`'s notification bell receives a "client created" notification in real time |
| A `CLIENT` places an order through the UI and sees its status flip from *Pending Payment* to *Paid*/*Failed* **without a manual refresh**, driven entirely by the WebSocket push |
| That same `CLIENT` receives their own payment confirmation/failure notification in real time |

**Run browser E2E tests locally:**

```bash
# Images must be available locally (build or pull first)
./gradlew :e2e-browser-tests:test

# To watch the browser instead of running headless:
./gradlew :e2e-browser-tests:test -Dheadless=false
```

> Like `e2e-tests`, this module is excluded from `./gradlew build` and runs as a dedicated CI step,
> after the Docker images are built.

**Watching the tests run (`-Dheadless=false`) on Windows:** Playwright's bundled Chromium requires
the Microsoft Visual C++ Redistributable (x64) to launch on Windows; without it, the browser fails
to start with a "side-by-side configuration" error. `BaseBrowserE2ETest` detects Windows and falls
back to launching the system's installed Microsoft Edge instead (`channel: "msedge"`, same Chromium
engine underneath) so the suite works out of the box — you'll see an Edge window, not Chromium.
Installing the VC++ Redistributable lets Playwright's own Chromium run instead, if preferred.
CI (Linux) always uses the bundled Chromium.

### Frontend Tests

- Location: `frontend/src/**/__test__`
- Tools: Vitest, Vue Test Utils, @pinia/testing
- Coverage: stores, API layer, components, and views
- Run: `npm run test` (inside `frontend/`)

---

## How to Run Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/jorgeeduardo96/payment-microservice.git
   cd payment-microservice
   ```

2. **Build the modules:**
   ```bash
   ./gradlew clean build
   ```

3. **Start the services using Docker Compose:**
   ```bash
   docker compose up
   ```

4. **Access the services:**

| Service     | URL                                   |
|-------------|----------------------------------------|
| Frontend    | http://localhost:8000                   |
| API Gateway | http://localhost:8080                   |
| Keycloak    | http://localhost:8180 (admin / admin)   |
| Eureka      | http://localhost:8761                   |
| Kafka UI    | http://localhost:8085                   |
| Zipkin      | http://localhost:9411                   |
| Grafana     | http://localhost:3000 (admin / admin)   |
| Prometheus  | http://localhost:9091                   |

5. **Log in to the frontend** with one of the seeded users (see `keycloak/realm-export.json`):

| Username | Password    | Role(s)       |
|----------|-------------|---------------|
| `admin`  | `admin123`  | `ADMIN`, `USER` |
| `demo`   | `demo123`   | `USER`        |

   Registering a client from the app (as `admin`) also creates a matching Keycloak login for that client
   (username = the client's email, password `client123`, role `CLIENT`).

---

## CI/CD Pipeline

The GitHub Actions workflow runs on every push or pull request to `main`:

```
Build all services (unit + integration tests)
        |
        v
Run frontend tests
        |
        v
Build Docker images (local)
        |
        v
Run E2E tests (REST API)
        |
        v
Run Browser E2E tests (Playwright)
        |
        v
Push Docker images to Docker Hub (only if all tests pass)
```
