package com.rbkmoney.cm.hooker.service;

import com.rbkmoney.cm.hooker.domain.Mail;

public interface RetryService {

    void repeatableSendMessage(Mail mail);

}
