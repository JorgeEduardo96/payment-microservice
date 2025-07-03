# 🧾 Payment Microservice

## 🔍 Overview

This project demonstrates a modern and resilient microservices architecture using **Spring Boot**, **Gradle**, and **Apache Kafka** as the event backbone. It follows best practices for scalability, decoupling, and observability — with a
didactic purpose.

---

## 🧱 Architecture

- **Independent Microservices:** Each service is responsible for a specific domain (e.g., Client, Order, Payment).
- **Isolated Databases:** Each microservice maintains its own SQL database (_Database per Service_).
- **Synchronous and Asynchronous Communication:**
    - REST using Spring Web.
    - gRPC for high-performance communication.
    - Kafka for event-driven communication.
- **Shared Library:** A common Gradle library with kafka utilities, validations, and custom exceptions.

---

## ⚙️ Event-Driven Architecture

- **Apache Kafka** as the event backbone.
- **Producers and Consumers** with `KafkaTemplate` and `@KafkaListener`.
- Full support for:
    - ✅ **Dead Letter Queue (DLQ)** for failed messages.
    - 🔁 **Configurable retries** with fallback using Resilience4j.
    - 🧩 **Idempotency** in consumers to avoid reprocessing.
- **Events propagate state changes** between services, reducing coupling and increasing scalability.

---

## ✅ Best Practices Followed

- 📦 Clear separation between domains, layers, and responsibilities.
- 🔁 Retry and Fallback with **Resilience4j**.
- 🧪 **Unit and integration tests** with significant coverage.
- 📊 Observability using:
    - **Zipkin** (distributed tracing).
    - **Prometheus + Grafana** (metrics and dashboards).
- 🐳 Docker containers orchestrated via `docker-compose`.
- 🚀 CI with **GitHub Actions** for builds on the `main` branch.
- 🌐 **Eureka** for service discovery.
- 🧼 Global validations and standardized exception handling.
- 📄 Environment profiles (`application-docker.yml`, `application-dev.yml`, etc).

---

## 📂 Project Structure

- `service-registry` — Eureka Server
- `api-gateway` — API Gateway using Spring Cloud Gateway
- `client-service` — Client microservice
- `notification-service` — Notification microservice
- `order-service` — Order microservice
- `payment-service` — Payment microservice
- `shared-lib` — Common shared library
- `docker-compose.yml` — Orchestration of services + Kafka, Grafana, etc

---

## ▶️ How to Run Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/payment-microservice.git
   cd payment-microservice
2. **Build the modules:**
   ```bash
   ./gradlew clean build
   ```
3. **Start the services using Docker Compose:**
   ```bash
   docker-compose up
   ```
4. **Access os services:**

- Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Kafka UI: http://localhost:8085
- Zipkin: http://localhost:9411
- Grafana: http://localhost:3000 (default login: admin / admin)
