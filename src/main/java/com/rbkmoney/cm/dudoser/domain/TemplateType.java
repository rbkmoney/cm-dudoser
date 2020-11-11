package com.rbkmoney.cm.dudoser.domain;

public enum TemplateType {

    STATUS_CHANGE,
    COMMENT,
    TELEGRAM_FILE_CHANGE,
    TELEGRAM_FILE_CHANGE_WITHOUT_FILE,
    TELEGRAM_COMMENT_CHANGE,
    TELEGRAM_IP_DOCUMENT_CHANGE, // Individual entity
    TELEGRAM_LE_DOCUMENT_CHANGE, // Legal entity
    TELEGRAM_ILE_DOCUMENT_CHANGE, // International legal entity
    TELEGRAM_NEW_CLAIM; // New claim notification, without additional info

}
