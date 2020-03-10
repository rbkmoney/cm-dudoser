package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.service.ClaimService;
import com.rbkmoney.cm.dudoser.service.MessageBuilderService;
import com.rbkmoney.damsel.claim_management.UserInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractMessageBuilderService<T> implements MessageBuilderService<T> {

    private final ClaimService claimService;
    private final String emailFrom;
    private final String subject;

    @Override
    public Message build(T change, UserInfo userInfo, String partyId, long claimId) {
        String emailTo = claimService.getEmailByClaim(userInfo, partyId, claimId);

        return build(emailFrom, emailTo, getContent(change, claimId), subject, partyId, claimId);
    }

    protected abstract String getContent(T change, long claimId);

    private Message build(String emailFrom, String emailTo, String content, String subject, String partyId, long claimId) {
        return Message.builder()
                .from(emailFrom)
                .to(emailTo)
                .subject(subject)
                .content(content)
                .partyId(partyId)
                .claimId(claimId)
                .build();
    }
}
