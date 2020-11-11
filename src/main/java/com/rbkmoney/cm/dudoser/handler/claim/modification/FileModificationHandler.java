package com.rbkmoney.cm.dudoser.handler.claim.modification;

import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.handler.ClaimModificationHandler;
import com.rbkmoney.cm.dudoser.helper.ClaimHelper;
import com.rbkmoney.cm.dudoser.service.FileDownloadService;
import com.rbkmoney.cm.dudoser.service.FileStorageService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.cm.dudoser.service.model.FileInfo;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramParseMode;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendDocumentRequest;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.FileModificationUnit;
import com.rbkmoney.damsel.claim_management.Modification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileModificationHandler implements ClaimModificationHandler {

    private final TelegramApi telegramApi;
    private final TelegramProperties telegramProperties;
    private final TemplateService templateService;
    private final FileStorageService fileStorageService;
    private final FileDownloadService fileDownloadService;

    @Value("${telegram.files.send.enable}")
    private boolean isSendFile;

    @Override
    public void handleModification(Long claimId, String partyId, List<Modification> changeSet) {
        String chatId = telegramProperties.getChatId();
        log.info("Handle file modification claim. claimId={} partyId={} chatId={}", claimId, partyId, chatId);
        List<FileModificationUnit> fileModifications = ClaimHelper.getFileModifications(changeSet);
        for (FileModificationUnit fileModification : fileModifications) {
            String fileDownloadUrl = fileStorageService.getFileDownloadUrl(
                    fileModification.getId(), Instant.now().plus(5, ChronoUnit.MINUTES));
            FileInfo file = fileDownloadService.requestFile(fileDownloadUrl, fileModification.getId());

            if (file.getFileData().length <= 0) {
                log.info("Ignore empty file: {}", fileDownloadUrl);
                return;
            }

            if (isSendFile) {
                sendFileData(claimId, partyId, chatId, file);
            } else {
                sendFileInfoMessage(claimId, partyId, chatId, file);
            }
        }
    }

    private void sendFileData(Long claimId, String partyId, String chatId, FileInfo file) {
        ClaimData claimData = ClaimData.builder()
                .id(String.valueOf(claimId))
                .partyId(partyId)
                .templateType(TemplateType.TELEGRAM_FILE_CHANGE)
                .build();
        String fileMessage = templateService.buildTemplate(claimData);
        TelegramSendDocumentRequest documentRequest = new TelegramSendDocumentRequest(
                chatId,
                fileMessage,
                file.getFileData(),
                false,
                TelegramParseMode.HTML
        );
        telegramApi.sendDocument(documentRequest, file.getFileName());
    }

    private void sendFileInfoMessage(Long claimId, String partyId, String chatId, FileInfo file) {
        ClaimData claimData = ClaimData.builder()
                .id(String.valueOf(claimId))
                .partyId(partyId)
                .templateType(TemplateType.TELEGRAM_FILE_CHANGE_WITHOUT_FILE)
                .comment(file.getFileName())
                .build();
        String message = templateService.buildTemplate(claimData);
        TelegramSendMessageRequest telegramRequest =
                new TelegramSendMessageRequest(chatId, message, TelegramParseMode.HTML);
        telegramApi.sendMessage(telegramRequest);
    }

}
