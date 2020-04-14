package com.rbkmoney.cm.dudoser.telegram.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegramMessage {

    @JsonProperty("message_id")
    private String id;

    @JsonProperty("from")
    private TelegramUser user;

    @JsonProperty("date")
    private Long date;

    @JsonProperty("chat")
    private TelegramChat chat;

    @JsonProperty("document")
    private TelegramDocument document;

    @JsonProperty("text")
    private String text;

    @JsonProperty("caption")
    private String caption;

}
