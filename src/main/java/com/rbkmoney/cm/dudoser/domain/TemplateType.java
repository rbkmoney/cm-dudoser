package com.rbkmoney.cm.dudoser.domain;

public enum TemplateType {

    STATUS_CHANGE,
    COMMENT,
    TELEGRAM_FILE_CHANGE,
    TELEGRAM_COMMENT_CHANGE,
    TELEGRAM_IP_DOCUMENT_CHANGE, // Individual entity
    TELEGRAM_LE_DOCUMENT_CHANGE, // Legal entity
    TELEGRAM_NEW_CLAIM; // New claim notification, without additional info

}
