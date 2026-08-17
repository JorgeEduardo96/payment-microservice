package br.com.clientservice.domain.exception;

public class KeycloakUserProvisioningException extends RuntimeException {

    public KeycloakUserProvisioningException(String message) {
        super(message);
    }

    public KeycloakUserProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
