package org.delcom.ifs25018pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExceptionTest {

    @Test
    void hierarchy() {
        Throwable cause = new RuntimeException("x");
        DomainException base = new DomainException("m", cause);
        assertEquals("m", base.getMessage());
        assertSame(cause, base.getCause());

        EntityNotFoundException notFound = new EntityNotFoundException(7);
        assertEquals(7, notFound.getId());
        assertEquals("Transaksi dengan ID 7 tidak ditemukan.", notFound.getMessage());
        assertInstanceOf(DomainException.class, notFound);

        ValidationException validation = new ValidationException("bad");
        assertEquals("bad", validation.getMessage());
        assertInstanceOf(DomainException.class, validation);
    }
}
