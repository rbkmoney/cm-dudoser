package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Mail;

public interface MailService<T> {

    Mail buildMail(T change, String partyId, long claimId);

}
