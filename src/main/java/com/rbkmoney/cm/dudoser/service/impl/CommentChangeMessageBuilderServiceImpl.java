package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.service.ClaimService;
import com.rbkmoney.cm.dudoser.service.ConversationService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.damsel.claim_management.CommentModificationUnit;
import com.rbkmoney.damsel.messages.Conversation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Qualifier("commentChangeMessageBuilder")
public class CommentChangeMessageBuilderServiceImpl extends AbstractMessageBuilderService<CommentModificationUnit> {

    private final ConversationService conversationService;
    private final TemplateService templateService;

    public CommentChangeMessageBuilderServiceImpl(ClaimService claimService,
                                                  @Value("${mail.from}") String emailFrom,
                                                  @Value("${mail.subject.comment}") String subject,
                                                  ConversationService conversationService,
                                                  TemplateService templateService) {
        super(claimService, emailFrom, subject);
        this.conversationService = conversationService;
        this.templateService = templateService;
    }

    protected String getContent(CommentModificationUnit commentModification, long claimId) {
        Conversation conversation = conversationService.getConversation(commentModification.getId());

        com.rbkmoney.damsel.messages.Message message =
                conversation.getMessages().get(conversation.getMessages().size() - 1);

        ClaimData claimData = ClaimData.builder()
                .templateType(TemplateType.COMMENT)
                .id(String.valueOf(claimId))
                .comment(message.getText())
                .build();

        return templateService.buildTemplate(claimData);
    }
}
