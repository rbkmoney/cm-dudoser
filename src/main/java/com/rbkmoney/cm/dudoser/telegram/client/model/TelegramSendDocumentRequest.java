package com.rbkmoney.cm.dudoser.telegram.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TelegramSendDocumentRequest extends TelegramMediaRequest {

    @JsonProperty("document")
    private final byte[] document;

    public TelegramSendDocumentRequest(String chatId,
                                       String caption,
                                       byte[] document) {
        this(chatId, document, false, caption, null);
    }

    public TelegramSendDocumentRequest(String chatId,
                                       byte[] document,
                                       Boolean disableNotification,
                                       String caption,
                                       TelegramParseMode parseMode) {
        super(chatId, disableNotification, caption, parseMode);
        this.document = document;
    }
}
