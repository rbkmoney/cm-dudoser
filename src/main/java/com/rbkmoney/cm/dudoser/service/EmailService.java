package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Mail;

public interface EmailService {

    boolean sendMessage(Mail mail);

}
