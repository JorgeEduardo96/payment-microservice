package br.com.clientservice.domain.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " with id [" + id + "] was not found.");
    }
}
