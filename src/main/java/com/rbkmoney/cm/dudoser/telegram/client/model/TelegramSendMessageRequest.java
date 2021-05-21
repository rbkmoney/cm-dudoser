package com.rbkmoney.cm.dudoser.telegram.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TelegramSendMessageRequest extends TelegramRequest {

    @JsonProperty("text")
    private String text;

    @JsonProperty("parse_mode")
    private TelegramParseMode parseMode;

    public TelegramSendMessageRequest(String chatId, String text) {
        this(chatId, text, null, false);
    }

    public TelegramSendMessageRequest(String chatId,
                                      String text,
                                      TelegramParseMode parseMode) {
        this(chatId, text, parseMode, false);
    }

    public TelegramSendMessageRequest(String chatId,
                                      String text,
                                      TelegramParseMode parseMode,
                                      Boolean disableNotification) {
        super(chatId, disableNotification);
        this.text = text;
        this.parseMode = parseMode;
    }
}
