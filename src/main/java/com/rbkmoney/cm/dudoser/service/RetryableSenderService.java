package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryableSenderService {

    private final MailSenderService mailSenderService;
    private final RetryTemplate mailRetryTemplate;

    public void sendToMail(Message message) {
        mailRetryTemplate.execute(context -> mailSenderService.send(message));
    }
}
