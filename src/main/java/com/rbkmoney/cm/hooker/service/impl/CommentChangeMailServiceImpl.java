package com.rbkmoney.cm.hooker.service.impl;

import com.rbkmoney.cm.hooker.domain.ClaimData;
import com.rbkmoney.cm.hooker.domain.Mail;
import com.rbkmoney.cm.hooker.domain.MailDto;
import com.rbkmoney.cm.hooker.service.ClaimService;
import com.rbkmoney.cm.hooker.service.MailService;
import com.rbkmoney.cm.hooker.service.MessageService;
import com.rbkmoney.cm.hooker.service.TemplateService;
import com.rbkmoney.damsel.claim_management.CommentModificationUnit;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentChangeMailServiceImpl implements MailService<CommentModificationUnit> {

    @Value("${mail.from}")
    private String mailFrom;

    private final ClaimService claimService;
    private final TemplateService templateService;
    private final MessageService messageService;

    @Override
    public Mail buildMail(CommentModificationUnit commentModification, String partyId, long claimId) {
        String emailTo = claimService.getEmailByClaim(partyId, claimId);

        String subject = "Добавлен новый комментарий по вашей заявки на подключение к RBK.money";

        String content = getContent(commentModification, claimId);

        MailDto mailDto = MailDto.builder()
                .partyId(partyId)
                .claimId(claimId)
                .build();

        return Mail.builder()
                .from(mailFrom)
                .to(emailTo)
                .subject(subject)
                .content(content)
                .mailDto(mailDto)
                .build();
    }

    private String getContent(CommentModificationUnit commentModification, long claimId) {
        Conversation conversation = messageService.getConversation(commentModification.getId());

        Message message = conversation.getMessages().get(conversation.getMessages().size() - 1);

        ClaimData claimData = ClaimData.builder()
                .id(String.valueOf(claimId))
                .comment(message.getText())
                .build();

        return templateService.process(claimData);
    }
}
