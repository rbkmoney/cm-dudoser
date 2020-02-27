package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Message;

public interface MessageBuilderService<T> {

    Message build(T change, String partyId, long claimId);

    default Message build(String emailFrom, String emailTo, String content, String subject, String partyId, long claimId) {
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
