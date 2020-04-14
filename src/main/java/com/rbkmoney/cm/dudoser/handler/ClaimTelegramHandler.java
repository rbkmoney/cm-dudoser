package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.helper.ClaimHelper;
import com.rbkmoney.cm.dudoser.service.ConversationService;
import com.rbkmoney.cm.dudoser.service.FileDownloadService;
import com.rbkmoney.cm.dudoser.service.FileStorageService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.cm.dudoser.service.model.FileInfo;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramParseMode;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendDocumentRequest;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.Change;
import com.rbkmoney.damsel.claim_management.CommentModificationUnit;
import com.rbkmoney.damsel.claim_management.Event;
import com.rbkmoney.damsel.claim_management.FileModificationUnit;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@ClaimHandlerOrder(1)
@RequiredArgsConstructor
public class ClaimTelegramHandler implements ClaimHandler {

    private final TelegramProperties telegramProperties;

    private final TelegramApi telegramApi;

    private final ConversationService conversationService;

    private final FileStorageService fileStorageService;

    private final TemplateService templateService;

    private final FileDownloadService fileDownloadService;

    @Qualifier("telegramRestTemplate")
    private final RestTemplate restTemplate;

    @Override
    public void handle(Event event, ClaimHandlerChain chain) {
        try {
            handleEvent(event);
        } catch (Exception e) {
            log.error("Exception during handle claim event", e);
            throw new ClaimHandlerException(e);
        } finally {
            chain.doFilter(event);
        }
    }

    private void handleEvent(Event event) throws IOException {
        if (!event.getUserInfo().getType().isSetExternalUser()) return;

        Change change = event.getChange();

        if (!change.isSetUpdated()) return;

        String partyId = change.getUpdated().getPartyId();
        long claimId = change.getUpdated().getId();
        String chatId = telegramProperties.getChatId();
        if (ClaimHelper.containsCommentModifications(change)) {
            List<CommentModificationUnit> commentModifications = ClaimHelper.getCommentModifications(change.getUpdated().getChangeset());
            for (CommentModificationUnit commentModification : commentModifications) {
                Conversation conversation = conversationService.getConversation(commentModification.getId());
                Message message = conversation.getMessages().get(conversation.getMessages().size() - 1);

                ClaimData claimData = ClaimData.builder()
                        .id(String.valueOf(claimId))
                        .partyId(partyId)
                        .templateType(TemplateType.TELEGRAM_COMMENT_CHANGE)
                        .comment(message.getText())
                        .build();
                String commentMessage = templateService.buildTemplate(claimData);
                TelegramSendMessageRequest telegramRequest = new TelegramSendMessageRequest(chatId, commentMessage, TelegramParseMode.HTML);
                telegramApi.sendMessage(telegramRequest);
            }
        } else if (ClaimHelper.containsFileModifications(change)) {
            List<FileModificationUnit> fileModifications = ClaimHelper.getFileModifications(change.getUpdated().getChangeset());
            for (FileModificationUnit fileModification : fileModifications) {
                String fileDownloadUrl = fileStorageService.getFileDownloadUrl(
                        fileModification.getId(), Instant.now().plus(30, ChronoUnit.MINUTES));
                FileInfo file = fileDownloadService.requestFile(fileDownloadUrl);
                ClaimData claimData = ClaimData.builder()
                        .id(String.valueOf(claimId))
                        .partyId(partyId)
                        .templateType(TemplateType.TELEGRAM_FILE_CHANGE)
                        .build();
                String fileMessage = templateService.buildTemplate(claimData);

                TelegramSendDocumentRequest documentRequest = new TelegramSendDocumentRequest(chatId, fileMessage, file.getFileData());
                telegramApi.sendDocument(documentRequest, file.getFileName());
            }
        }
    }

}
