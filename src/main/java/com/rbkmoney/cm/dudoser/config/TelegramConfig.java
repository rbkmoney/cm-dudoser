package com.rbkmoney.cm.dudoser.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramErrorHandler;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class TelegramConfig {

    public static final String BASE_URL = "https://api.telegram.org/bot";

    @Bean
    public RestTemplate telegramRestTemplate(TelegramProperties telegramProperties,
                                             ObjectMapper objectMapper) throws NoSuchAlgorithmException, KeyManagementException {
        return new RestTemplateBuilder()
                .rootUri(BASE_URL + telegramProperties.getToken())
                .errorHandler(new TelegramErrorHandler(objectMapper))
                .build();
    }

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
