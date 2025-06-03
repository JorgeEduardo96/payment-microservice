package br.com.orderservice.domain.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, UUID id) {
        super(entity + " with id [" + id + "] was not found.");
    }
}
