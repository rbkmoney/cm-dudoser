package com.rbkmoney.cm.dudoser.handler.claim.modification;

import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.handler.ClaimModificationHandler;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramParseMode;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.Modification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimCreationHandler implements ClaimModificationHandler {

    private final TelegramProperties telegramProperties;
    private final TelegramApi telegramApi;
    private final TemplateService templateService;

    @Override
    public void handleModification(Long claimId, String partyId, List<Modification> changeSet) {
        String chatId = telegramProperties.getChatId();
        log.info("Handle new claim. claimId={} partyId={} chatId={}", claimId, partyId, chatId);
        ClaimData claimData = ClaimData.builder()
                .id(String.valueOf(claimId))
                .partyId(partyId)
                .templateType(TemplateType.TELEGRAM_NEW_CLAIM)
                .build();
        String template = templateService.buildTemplate(claimData);
        TelegramSendMessageRequest messageRequest = new TelegramSendMessageRequest(chatId, template, TelegramParseMode.HTML);
        telegramApi.sendMessage(messageRequest);
    }

}
