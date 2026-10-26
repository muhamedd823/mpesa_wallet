package com.safaricom.wallet.exception;

import org.springframework.http.HttpStatus;

public class WalletException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public WalletException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }

    // --- Factory methods for every condition in the spec error table ---

    public static WalletException notFound(String code, String message) {
        return new WalletException(code, message, HttpStatus.NOT_FOUND);
    }

    public static WalletException conflict(String code, String message) {
        return new WalletException(code, message, HttpStatus.CONFLICT);
    }

    public static WalletException unprocessable(String code, String message) {
        return new WalletException(code, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static WalletException serviceUnavailable() {
        return new WalletException("SERVICE_UNAVAILABLE", "Database unreachable", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
