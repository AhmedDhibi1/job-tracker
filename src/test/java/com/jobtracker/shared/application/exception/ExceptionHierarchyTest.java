package com.jobtracker.shared.application.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the base exception hierarchy:
 * {@link DomainException}, {@link EntityNotFoundException},
 * {@link ConcurrentModificationException}.
 */
class ExceptionHierarchyTest {

    // ── Concrete DomainException stub ─────────────────────────────────────────

    static final class TestDomainException extends DomainException {
        TestDomainException(String code, String message) {
            super(code, message);
        }
        TestDomainException(String code, String message, Throwable cause) {
            super(code, message, cause);
        }
    }

    // ── DomainException ───────────────────────────────────────────────────────

    @Test
    void domainException_storesErrorCode() {
        var ex = new TestDomainException("MY_ERROR_CODE", "something went wrong");
        assertThat(ex.getErrorCode()).isEqualTo("MY_ERROR_CODE");
    }

    @Test
    void domainException_storesMessage() {
        var ex = new TestDomainException("CODE", "something went wrong");
        assertThat(ex.getMessage()).isEqualTo("something went wrong");
    }

    @Test
    void domainException_storesCause() {
        var cause = new RuntimeException("root cause");
        var ex    = new TestDomainException("CODE", "wrapper message", cause);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void domainException_isSubtypeOfRuntimeException() {
        var ex = new TestDomainException("CODE", "msg");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ── EntityNotFoundException ───────────────────────────────────────────────

    @Test
    void entityNotFoundException_storesEntityType() {
        var id = UUID.randomUUID();
        var ex = new EntityNotFoundException(String.class, id);
        assertThat(ex.getEntityType()).isEqualTo(String.class);
    }

    @Test
    void entityNotFoundException_storesEntityId() {
        var id = UUID.randomUUID();
        var ex = new EntityNotFoundException(String.class, id);
        assertThat(ex.getEntityId()).isEqualTo(id);
    }

    @Test
    void entityNotFoundException_generatesDescriptiveMessage() {
        var id = UUID.randomUUID();
        var ex = new EntityNotFoundException(String.class, id);
        assertThat(ex.getMessage())
                .contains("String")
                .contains(id.toString());
    }

    @Test
    void entityNotFoundException_hasCorrectErrorCode() {
        var ex = new EntityNotFoundException(String.class, UUID.randomUUID());
        assertThat(ex.getErrorCode()).isEqualTo("ENTITY_NOT_FOUND");
    }

    @Test
    void entityNotFoundException_isSubtypeOfDomainException() {
        var ex = new EntityNotFoundException(String.class, UUID.randomUUID());
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    // ── ConcurrentModificationException ──────────────────────────────────────

    @Test
    void concurrentModificationException_storesResourceType() {
        var ex = new ConcurrentModificationException("JobApplication", "abc-123");
        assertThat(ex.getResourceType()).isEqualTo("JobApplication");
    }

    @Test
    void concurrentModificationException_storesResourceId() {
        var ex = new ConcurrentModificationException("JobApplication", "abc-123");
        assertThat(ex.getResourceId()).isEqualTo("abc-123");
    }

    @Test
    void concurrentModificationException_generatesDescriptiveMessage() {
        var ex = new ConcurrentModificationException("JobApplication", "abc-123");
        assertThat(ex.getMessage())
                .contains("JobApplication")
                .contains("abc-123");
    }

    @Test
    void concurrentModificationException_hasCorrectErrorCode() {
        var ex = new ConcurrentModificationException("JobApplication", "abc-123");
        assertThat(ex.getErrorCode()).isEqualTo("OPTIMISTIC_LOCK_CONFLICT");
    }

    @Test
    void concurrentModificationException_acceptsCause() {
        var cause = new RuntimeException("jpa optimistic lock");
        var ex    = new ConcurrentModificationException("JobApplication", "abc-123", cause);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void concurrentModificationException_isSubtypeOfDomainException() {
        var ex = new ConcurrentModificationException("JobApplication", "abc-123");
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    // ── Inheritance hierarchy integrity ───────────────────────────────────────

    @Test
    void allExceptions_areRuntimeExceptions() {
        assertThat(new TestDomainException("C", "m"))
                .isInstanceOf(RuntimeException.class);
        assertThat(new EntityNotFoundException(String.class, UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class);
        assertThat(new ConcurrentModificationException("T", "1"))
                .isInstanceOf(RuntimeException.class);
    }
}