package com.rbkmoney.cm.dudoser.exception;

public class ThriftClientException extends RuntimeException {

    public ThriftClientException() {
    }

    public ThriftClientException(String message) {
        super(message);
    }

    public ThriftClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThriftClientException(Throwable cause) {
        super(cause);
    }

    public ThriftClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
