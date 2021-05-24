package com.rbkmoney.cm.dudoser.exception;

public class ThriftClientException extends RuntimeException {

    public ThriftClientException(String message) {
        super(message);
    }

    public ThriftClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
