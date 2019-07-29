package org.datavaultplatform.webapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class<?> entityType, String entityId) {
        super("Unable to find " + entityType.getName() + " with ID " + entityId);
    }
}
