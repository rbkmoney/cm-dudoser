package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.ClaimDocumentData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.helper.ClaimHelper;
import com.rbkmoney.cm.dudoser.service.*;
import com.rbkmoney.cm.dudoser.service.model.FileInfo;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramParseMode;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendDocumentRequest;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.Message;
import com.rbkmoney.questionary.*;
import com.rbkmoney.questionary.manage.Questionary;
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

    private final QuestionaryService questionaryService;

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

        final String partyId = extractPartyId(change);
        final Long claimId = extractClaimId(change);
        final String chatId = telegramProperties.getChatId();

        if (change.isSetCreated()) {
            List<Modification> changeset = change.getCreated().getChangeset();
            if (ClaimHelper.containsDocumentModifications(change)) {
                handleDocumentModification(claimId, partyId, chatId, changeset);
            } else {
                ClaimData claimData = ClaimData.builder()
                        .id(String.valueOf(claimId))
                        .partyId(partyId)
                        .templateType(TemplateType.TELEGRAM_NEW_CLAIM)
                        .build();
                String template = templateService.buildTemplate(claimData);
                TelegramSendMessageRequest messageRequest = new TelegramSendMessageRequest(chatId, template, TelegramParseMode.HTML);
                telegramApi.sendMessage(messageRequest);
            }
        } else if (change.isSetUpdated()) {
            List<Modification> changeset = change.getUpdated().getChangeset();
            if (ClaimHelper.containsCommentModifications(change)) {
                handleCommentModification(claimId, partyId, chatId, changeset);
            } else if (ClaimHelper.containsFileModifications(change)) {
                handleFileModification(claimId, partyId, chatId, changeset);
            }
        }
    }

    private void handleFileModification(Long claimId, String partyId, String chatId, List<Modification> changeSet) {
        List<FileModificationUnit> fileModifications = ClaimHelper.getFileModifications(changeSet);
        for (FileModificationUnit fileModification : fileModifications) {
            String fileDownloadUrl = fileStorageService.getFileDownloadUrl(
                    fileModification.getId(), Instant.now().plus(5, ChronoUnit.MINUTES));
            FileInfo file = fileDownloadService.requestFile(fileDownloadUrl, fileModification.getId());

            if (file.getFileData().length <= 0) {
                log.info("Ignore empty file: {}", fileDownloadUrl);
                return;
            }

            ClaimData claimData = ClaimData.builder()
                    .id(String.valueOf(claimId))
                    .partyId(partyId)
                    .templateType(TemplateType.TELEGRAM_FILE_CHANGE)
                    .build();
            String fileMessage = templateService.buildTemplate(claimData);

            TelegramSendDocumentRequest documentRequest =
                    new TelegramSendDocumentRequest(chatId, fileMessage, file.getFileData(), false, TelegramParseMode.HTML);
            telegramApi.sendDocument(documentRequest, file.getFileName());
        }
    }

    private void handleCommentModification(Long claimId, String partyId, String chatId, List<Modification> changeSet) {
        List<CommentModificationUnit> commentModifications = ClaimHelper.getCommentModifications(changeSet);
        for (CommentModificationUnit commentModification : commentModifications) {
            Conversation conversation = conversationService.getConversation(commentModification.getId());
            Message message = conversation.getMessages().get(conversation.getMessages().size() - 1);

            ClaimData claimData = ClaimData.builder()
                    .id(String.valueOf(claimId))
                    .partyId(partyId)
                    .templateType(TemplateType.TELEGRAM_COMMENT_CHANGE)
                    .comment(message.getText())
                    .build();
            String template  = templateService.buildTemplate(claimData);
            TelegramSendMessageRequest telegramRequest = new TelegramSendMessageRequest(chatId, template, TelegramParseMode.HTML);
            telegramApi.sendMessage(telegramRequest);
        }
    }

    private void handleDocumentModification(Long claimId, String partyId, String chatId, List<Modification> changeSet) {
        List<DocumentModificationUnit> documentModifications = ClaimHelper.getDocumentModifications(changeSet);
        for (DocumentModificationUnit documentModification : documentModifications) {
            Questionary questionary = questionaryService.getQuestionary(documentModification.getId(), partyId);
            Contractor contractor = questionary.getData().getContractor();

            if (contractor == null) return;

            TemplateType templateType = null;
            ClaimDocumentData claimDocumentData = null;
            if (contractor.isSetIndividualEntity()) {
                RussianIndividualEntity individualEntity = contractor.getIndividualEntity().getRussianIndividualEntity();
                RegistrationInfo registrationInfo = individualEntity.getRegistrationInfo();
                IndividualRegistrationInfo individualRegistrationInfo = registrationInfo != null
                        ? registrationInfo.getIndividualRegistrationInfo() : null;
                RussianPrivateEntity russianPrivateEntity = individualEntity.getRussianPrivateEntity();
                templateType = TemplateType.TELEGRAM_IP_DOCUMENT_CHANGE;
                claimDocumentData = ClaimDocumentData.builder()
                        .ownerId(questionary.getOwnerId())
                        .organization(individualEntity.getName())
                        .inn(individualEntity.getInn())
                        .registrationDate(individualRegistrationInfo != null ? individualRegistrationInfo.getRegistrationDate() : null)
                        .registrationAddress(individualRegistrationInfo != null ? individualRegistrationInfo.getRegistrationPlace() : null)
                        .headFio(russianPrivateEntity != null ? russianPrivateEntity.getFio() : null)
                        .build();
            } else if (contractor.isSetLegalEntity()) {
                RussianLegalEntity russianLegalEntity = contractor.getLegalEntity().getRussianLegalEntity();
                RegistrationInfo registrationInfo = russianLegalEntity.getRegistrationInfo();
                LegalRegistrationInfo legalRegistrationInfo = registrationInfo != null ? registrationInfo.getLegalRegistrationInfo() : null;
                LegalOwnerInfo legalOwnerInfo = russianLegalEntity.getLegalOwnerInfo();
                RussianPrivateEntity russianPrivateEntity = legalOwnerInfo != null ? legalOwnerInfo.getRussianPrivateEntity() : null;
                templateType = TemplateType.TELEGRAM_LE_DOCUMENT_CHANGE;
                claimDocumentData = ClaimDocumentData.builder()
                        .ownerId(questionary.getOwnerId())
                        .organization(russianLegalEntity.getName())
                        .inn(russianLegalEntity.getInn())
                        .registrationDate(legalRegistrationInfo != null ? legalRegistrationInfo.getRegistrationDate() : null)
                        .registrationAddress(legalRegistrationInfo != null ? legalRegistrationInfo.getRegistrationAddress() : null)
                        .okato(russianLegalEntity.getOkatoCode())
                        .okpo(russianLegalEntity.getOkpoCode())
                        .headPosition(legalOwnerInfo != null ? legalOwnerInfo.getHeadPosition() : null)
                        .headFio(russianPrivateEntity != null ? russianPrivateEntity.getFio() : null)
                        .build();
            } else {
                throw new IllegalStateException("Unknown contractor type: " + contractor);
            }
            ClaimData claimData = ClaimData.builder()
                    .id(String.valueOf(claimId))
                    .partyId(partyId)
                    .templateType(templateType)
                    .claimDocumentData(claimDocumentData)
                    .build();
            String template = templateService.buildTemplate(claimData);
            TelegramSendMessageRequest telegramSendMessageRequest = new TelegramSendMessageRequest(chatId, template, TelegramParseMode.HTML);
            telegramApi.sendMessage(telegramSendMessageRequest);
        }
    }

    private String extractPartyId(Change change) {
        if (change.isSetCreated()) {
            return change.getCreated().getPartyId();
        } else if (change.isSetUpdated()) {
            return change.getUpdated().getPartyId();
        } else if (change.isSetStatusChanged()) {
            return change.getStatusChanged().getPartyId();
        }
        throw new IllegalArgumentException("Unknown change type: " + change);
    }

    private Long extractClaimId(Change change) {
        if (change.isSetCreated()) {
            return change.getCreated().getId();
        } else if (change.isSetUpdated()) {
            return change.getUpdated().getId();
        } else if (change.isSetStatusChanged()) {
            return change.getStatusChanged().getId();
        }
        throw new IllegalArgumentException("Unknown change type: " + change);
    }

}
