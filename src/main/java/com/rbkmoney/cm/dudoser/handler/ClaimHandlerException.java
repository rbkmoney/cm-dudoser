package com.rbkmoney.cm.dudoser.handler;

public class ClaimHandlerException extends RuntimeException {
    public ClaimHandlerException() {
        super();
    }

    public ClaimHandlerException(String message) {
        super(message);
    }

    public ClaimHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClaimHandlerException(Throwable cause) {
        super(cause);
    }
}
