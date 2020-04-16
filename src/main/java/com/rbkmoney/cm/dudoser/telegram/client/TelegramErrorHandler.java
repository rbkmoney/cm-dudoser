package com.rbkmoney.cm.dudoser.telegram.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class TelegramErrorHandler extends DefaultResponseErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            String tlResp = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            TelegramResponse telegramResponse = objectMapper.readValue(tlResp, TelegramResponse.class);
            String msg = "code: " + response.getStatusCode() + " description: " + telegramResponse.getDescription();

            throw new TelegramClientException(msg);
        }

    }
}
