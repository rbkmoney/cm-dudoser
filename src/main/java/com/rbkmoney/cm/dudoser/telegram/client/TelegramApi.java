package com.rbkmoney.cm.dudoser.telegram.client;

import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramMessage;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramResponse;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendDocumentRequest;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class TelegramApi {

    private final RestTemplate telegramRestTemplate;

    private final TelegramProperties telegramProperties;

    public TelegramApi(@Qualifier("telegramRestTemplate") RestTemplate telegramRestTemplate,
                       TelegramProperties telegramProperties) {
        this.telegramRestTemplate = telegramRestTemplate;
        this.telegramProperties = telegramProperties;
    }

    public TelegramMessage sendMessage(TelegramSendMessageRequest sendMessageRequest) {
        log.info("Send telegram message. ChatId={} Text={}", sendMessageRequest.getChatId(), sendMessageRequest.getText());
        ResponseEntity<TelegramResponse<TelegramMessage>> response = telegramRestTemplate.exchange(
                "/sendMessage",
                HttpMethod.POST,
                new HttpEntity<TelegramSendMessageRequest>(sendMessageRequest),
                new ParameterizedTypeReference<>() {
                }
        );

        return extractResponse(response);
    }

    public TelegramMessage sendDocument(TelegramSendDocumentRequest sendDocumentRequest, String fileName) {
        log.info("Send telegram document. ChatId={} Filename={}", sendDocumentRequest.getChatId(), fileName);
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("chat_id", sendDocumentRequest.getChatId());
        formData.add("document", new ByteArrayResource(sendDocumentRequest.getDocument()) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        formData.add("disable_notification", sendDocumentRequest.getDisableNotification());
        if (sendDocumentRequest.getCaption() != null) {
            formData.add("caption", sendDocumentRequest.getCaption());
        }
        if (sendDocumentRequest.getParseMode() != null) {
            formData.add("parse_mode", sendDocumentRequest.getParseMode().name());
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<TelegramResponse<TelegramMessage>> response = telegramRestTemplate.exchange(
                "/sendDocument",
                HttpMethod.POST,
                new HttpEntity<>(formData, httpHeaders),
                new ParameterizedTypeReference<>() {
                }
        );

        return extractResponse(response);
    }

    private <R> R extractResponse(ResponseEntity<TelegramResponse<R>> response) {
        TelegramResponse<R> body = response.getBody();
        if (!body.getOk()) {
            throw new TelegramClientException(body.getErrorCode() + " - " + body.getDescription());
        }
        return body.getResult();
    }

}
