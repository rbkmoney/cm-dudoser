package com.rbkmoney.cm.dudoser.telegram.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class TelegramMediaRequest extends TelegramRequest {

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("parse_mode")
    private TelegramParseMode parseMode;

    public TelegramMediaRequest(String chatId, Boolean disableNotification, String caption,
                                TelegramParseMode parseMode) {
        super(chatId, disableNotification);
        this.caption = caption;
        this.parseMode = parseMode;
    }
}
