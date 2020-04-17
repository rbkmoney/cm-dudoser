package com.rbkmoney.cm.dudoser.telegram.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegramResponse<T> {
    @JsonProperty("ok")
    private Boolean ok;

    @JsonProperty("result")
    private T result;

    @JsonProperty("error_code")
    private Integer errorCode;

    @JsonProperty("description")
    private String description;
}
