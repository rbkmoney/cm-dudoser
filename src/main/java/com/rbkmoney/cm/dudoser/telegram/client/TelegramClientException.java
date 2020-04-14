package com.rbkmoney.cm.dudoser.telegram.client;

public class TelegramClientException extends RuntimeException {
    public TelegramClientException() {
        super();
    }

    public TelegramClientException(String message) {
        super(message);
    }

    public TelegramClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public TelegramClientException(Throwable cause) {
        super(cause);
    }
}
