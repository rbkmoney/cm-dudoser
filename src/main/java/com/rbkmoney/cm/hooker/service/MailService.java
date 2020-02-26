package com.rbkmoney.cm.hooker.service;

import com.rbkmoney.cm.hooker.domain.Mail;

public interface MailService<T> {

    Mail buildMail(T change, String partyId, long claimId);

}
