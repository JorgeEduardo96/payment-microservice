package br.com.clientservice.domain.service;

public class KeycloakUserProvisioningException extends RuntimeException {

    public KeycloakUserProvisioningException(String message) {
        super(message);
    }

    public KeycloakUserProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
