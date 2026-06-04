package com.jobtracker.shared.application.exception;


public class ConcurrentModificationException extends DomainException {

    private static final String ERROR_CODE = "OPTIMISTIC_LOCK_CONFLICT";

    private final String resourceType;

    private final String resourceId;

    
    public ConcurrentModificationException(String resourceType, String resourceId) {
        super(
                ERROR_CODE,
                "Concurrent modification conflict on " + resourceType + " with id: " + resourceId
                        + ". Please retry your request."
        );
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
    }

   
    public ConcurrentModificationException(
            String resourceType,
            String resourceId,
            Throwable cause) {
        super(
                ERROR_CODE,
                "Concurrent modification conflict on " + resourceType + " with id: " + resourceId
                        + ". Please retry your request.",
                cause
        );
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
    }

   
    public String getResourceType() {
        return resourceType;
    }

    
    public String getResourceId() {
        return resourceId;
    }
}