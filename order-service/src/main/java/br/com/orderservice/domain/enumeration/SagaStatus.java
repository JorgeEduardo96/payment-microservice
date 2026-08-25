package br.com.orderservice.domain.enumeration;

public enum SagaStatus {

    // Order persisted, saga row created, payment not requested yet.
    STARTED,

    // gRPC call to payment-service sent successfully; waiting for the Kafka
    // payment-topic event (or a timeout) to reach a terminal state.
    PAYMENT_REQUESTED,

    // Payment confirmed as PAID via Kafka - saga finished successfully.
    COMPLETED,

    // Payment declined, gRPC call failed after retries, or a timeout fired -
    // the order is being/was cancelled to compensate.
    COMPENSATED,

    // Compensation itself failed repeatedly (e.g. DB unavailable) - terminal
    // state that requires manual intervention.
    FAILED
}
