package org.delcom.ifs25018pbo2026springbootcashflow.domain.exception;

/** Dilempar saat terjadi kegagalan validasi aturan bisnis atau input domain. */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
