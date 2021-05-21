package com.rbkmoney.cm.dudoser.handler.claim.modification;

import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.handler.ClaimModificationHandler;
import com.rbkmoney.cm.dudoser.helper.ClaimHelper;
import com.rbkmoney.cm.dudoser.service.ConversationService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramParseMode;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.CommentModificationUnit;
import com.rbkmoney.damsel.claim_management.Modification;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentModificationHandler implements ClaimModificationHandler {

    private final TelegramApi telegramApi;
    private final TelegramProperties telegramProperties;
    private final TemplateService templateService;
    private final ConversationService conversationService;

    @Override
    public void handleModification(Long claimId, String partyId, List<Modification> changeSet) {
        String chatId = telegramProperties.getChatId();
        log.info("Handle comment modification claim. claimId={} partyId={} chatId={}", claimId, partyId, chatId);
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
            String template = templateService.buildTemplate(claimData);
            TelegramSendMessageRequest telegramRequest =
                    new TelegramSendMessageRequest(chatId, template, TelegramParseMode.HTML);
            telegramApi.sendMessage(telegramRequest);
        }
    }

}
