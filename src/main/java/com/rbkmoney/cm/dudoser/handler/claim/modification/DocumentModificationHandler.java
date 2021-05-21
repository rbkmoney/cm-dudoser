package com.rbkmoney.cm.dudoser.handler.claim.modification;

import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.ClaimDocumentData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.handler.ClaimModificationHandler;
import com.rbkmoney.cm.dudoser.helper.ClaimHelper;
import com.rbkmoney.cm.dudoser.service.QuestionaryService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramParseMode;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.DocumentModificationUnit;
import com.rbkmoney.damsel.claim_management.Modification;
import com.rbkmoney.questionary.*;
import com.rbkmoney.questionary.manage.Questionary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentModificationHandler implements ClaimModificationHandler {

    private final TelegramApi telegramApi;
    private final TelegramProperties telegramProperties;
    private final TemplateService templateService;
    private final QuestionaryService questionaryService;

    @Override
    public void handleModification(Long claimId, String partyId, List<Modification> changeSet) {
        String chatId = telegramProperties.getChatId();
        log.info("Handle document modification. claimId={} partyId={} chatId={}", claimId, partyId, chatId);
        List<DocumentModificationUnit> documentModifications = ClaimHelper.getDocumentModifications(changeSet);
        for (DocumentModificationUnit documentModification : documentModifications) {
            Questionary questionary = questionaryService.getQuestionary(documentModification.getId(), partyId);
            Contractor contractor = questionary.getData().getContractor();

            if (contractor == null) {
                return;
            }

            TemplateType templateType = null;
            ClaimDocumentData claimDocumentData = null;
            if (contractor.isSetIndividualEntity()) {
                RussianIndividualEntity individualEntity =
                        contractor.getIndividualEntity().getRussianIndividualEntity();
                RegistrationInfo registrationInfo = individualEntity.getRegistrationInfo();
                IndividualRegistrationInfo individualRegistrationInfo = registrationInfo != null
                        ? registrationInfo.getIndividualRegistrationInfo() : null;
                RussianPrivateEntity russianPrivateEntity = individualEntity.getRussianPrivateEntity();
                templateType = TemplateType.TELEGRAM_IP_DOCUMENT_CHANGE;
                claimDocumentData = ClaimDocumentData.builder()
                        .ownerId(questionary.getOwnerId())
                        .organization(individualEntity.getName())
                        .inn(individualEntity.getInn())
                        .registrationDate(
                                individualRegistrationInfo != null ? individualRegistrationInfo.getRegistrationDate() :
                                        null)
                        .registrationAddress(
                                individualRegistrationInfo != null ? individualRegistrationInfo.getRegistrationPlace() :
                                        null)
                        .headFio(russianPrivateEntity != null ? russianPrivateEntity.getFio() : null)
                        .build();
            } else if (contractor.isSetLegalEntity()) {
                if (contractor.getLegalEntity().isSetRussianLegalEntity()) {
                    RussianLegalEntity russianLegalEntity = contractor.getLegalEntity().getRussianLegalEntity();
                    RegistrationInfo registrationInfo = russianLegalEntity.getRegistrationInfo();
                    LegalRegistrationInfo legalRegistrationInfo =
                            registrationInfo != null ? registrationInfo.getLegalRegistrationInfo() : null;
                    LegalOwnerInfo legalOwnerInfo = russianLegalEntity.getLegalOwnerInfo();
                    RussianPrivateEntity russianPrivateEntity =
                            legalOwnerInfo != null ? legalOwnerInfo.getRussianPrivateEntity() : null;
                    templateType = TemplateType.TELEGRAM_LE_DOCUMENT_CHANGE;
                    claimDocumentData = ClaimDocumentData.builder()
                            .ownerId(questionary.getOwnerId())
                            .organization(russianLegalEntity.getName())
                            .inn(russianLegalEntity.getInn())
                            .registrationDate(
                                    legalRegistrationInfo != null ? legalRegistrationInfo.getRegistrationDate() : null)
                            .registrationAddress(
                                    legalRegistrationInfo != null ? legalRegistrationInfo.getRegistrationAddress() :
                                            null)
                            .okato(russianLegalEntity.getOkatoCode())
                            .okpo(russianLegalEntity.getOkpoCode())
                            .headPosition(legalOwnerInfo != null ? legalOwnerInfo.getHeadPosition() : null)
                            .headFio(russianPrivateEntity != null ? russianPrivateEntity.getFio() : null)
                            .build();
                } else if (contractor.getLegalEntity().isSetInternationalLegalEntity()) {
                    InternationalLegalEntity internationalLegalEntity =
                            contractor.getLegalEntity().getInternationalLegalEntity();
                    templateType = TemplateType.TELEGRAM_ILE_DOCUMENT_CHANGE;
                    claimDocumentData = ClaimDocumentData.builder()
                            .ownerId(questionary.getOwnerId())
                            .internationalActualAddress(internationalLegalEntity.getActualAddress())
                            .internationalTradingName(internationalLegalEntity.getTradingName())
                            .internationalRegisteredAddress(internationalLegalEntity.getRegisteredAddress())
                            .internationalRegisteredNumber(internationalLegalEntity.getRegisteredNumber())
                            .internationalLegalName(internationalLegalEntity.getLegalName())
                            .build();
                }
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
            TelegramSendMessageRequest telegramSendMessageRequest =
                    new TelegramSendMessageRequest(chatId, template, TelegramParseMode.HTML);
            telegramApi.sendMessage(telegramSendMessageRequest);
        }
    }

}
