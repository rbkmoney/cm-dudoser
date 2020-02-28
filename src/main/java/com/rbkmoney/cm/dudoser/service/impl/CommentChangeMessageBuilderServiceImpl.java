package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.service.ClaimService;
import com.rbkmoney.cm.dudoser.service.ConversationService;
import com.rbkmoney.cm.dudoser.service.MessageBuilderService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.damsel.claim_management.CommentModificationUnit;
import com.rbkmoney.damsel.messages.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Qualifier("commentChangeMessageBuilder")
@RequiredArgsConstructor
@Slf4j
public class CommentChangeMessageBuilderServiceImpl implements MessageBuilderService<CommentModificationUnit> {

    @Value("${mail.from}")
    private String emailFrom;

    @Value("${mail.subject.comment}")
    private String subject;

    private final ClaimService claimService;
    private final TemplateService templateService;
    private final ConversationService conversationService;

    @Override
    public Message build(CommentModificationUnit commentModification, String partyId, long claimId) {
        String emailTo = claimService.getEmailByClaim(partyId, claimId);

        return build(emailFrom, emailTo, getContent(commentModification, claimId), subject, partyId, claimId);
    }

    private String getContent(CommentModificationUnit commentModification, long claimId) {
        Conversation conversation = conversationService.getConversation(commentModification.getId());

        com.rbkmoney.damsel.messages.Message message = conversation.getMessages().get(conversation.getMessages().size() - 1);

        ClaimData claimData = ClaimData.builder()
                .templateType(TemplateType.COMMENT)
                .id(String.valueOf(claimId))
                .comment(message.getText())
                .build();

        return templateService.process(claimData);
    }
}
