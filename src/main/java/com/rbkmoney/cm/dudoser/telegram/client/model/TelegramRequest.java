package com.rbkmoney.cm.dudoser.telegram.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelegramRequest {

    @JsonProperty("chat_id")
    private final String chatId;

    @JsonProperty("disable_notification")
    private final Boolean disableNotification;

    public TelegramRequest(String chatId) {
        this(chatId, null);
    }

    public TelegramRequest(String chatId, Boolean disableNotification) {
        this.chatId = chatId;
        this.disableNotification = disableNotification;
    }
}
