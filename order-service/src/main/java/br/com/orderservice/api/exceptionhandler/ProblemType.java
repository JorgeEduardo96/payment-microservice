package br.com.orderservice.api.exceptionhandler;

import lombok.Getter;

@Getter
public enum ProblemType {

    RESOURCE_NOT_FOUND("Resource not found", "resource-not-found"),
    BUSINESS_ERROR("Business rule violation", "business-error"),
    ENTITY_IN_USE("Entity in use", "entity-in-use"),
    INCOMPREHENSIBLE_MESSAGE("Incomprehensible message", "incomprehensible-message"),
    INVALID_PARAMETER("Invalid parameter", "invalid-parameter"),
    SYSTEM_ERROR("System error", "system-error"),
    INVALID_DATA("Invalid data", "invalid-data"),
    ACCESS_DENIED("Access denied", "access-denied");

    private final String title;
    private final String uri;

    ProblemType(String title, String path) {
        this.title = title;
        this.uri = "https://payment-microservice/" + path;
    }
}
