package com.rbkmoney.cm.dudoser.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component("telegramProperties")
@ConfigurationProperties("telegram")
public class TelegramProperties {

    private Boolean enable;

    private String token;

    private Long chatId;

    public String getChatId() {
        return "-100" + chatId.toString();
    }
}
