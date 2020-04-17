package com.rbkmoney.cm.dudoser.service;

public class RequestFileException extends RuntimeException {

    private final String url;

    public RequestFileException(String url, Throwable e) {
        super(e);
        this.url = url;
    }

    @Override
    public String getMessage() {
        return "Failed to request file: " + url;
    }
}
