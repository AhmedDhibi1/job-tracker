package com.jobtracker.shared.application.exception;

import java.util.UUID;


public class EntityNotFoundException extends DomainException {

    private static final String ERROR_CODE = "ENTITY_NOT_FOUND";

    private final Class<?> entityType;

    private final UUID entityId;

    
    public EntityNotFoundException(Class<?> entityType, UUID entityId) {
        super(
                ERROR_CODE,
                entityType.getSimpleName() + " not found with id: " + entityId
        );
        this.entityType = entityType;
        this.entityId   = entityId;
    }

   
    public Class<?> getEntityType() {
        return entityType;
    }

    
    public UUID getEntityId() {
        return entityId;
    }
}