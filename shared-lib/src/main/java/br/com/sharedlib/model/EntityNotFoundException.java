package br.com.sharedlib.model;

import java.io.Serial;
import java.io.Serializable;

public class EntityNotFoundException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String entity, String id) {
        super(String.format("%s not found for ID: %s", entity, id));
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
