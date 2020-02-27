package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.Mail;
import com.rbkmoney.cm.dudoser.service.EmailService;
import com.rbkmoney.cm.dudoser.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryServiceImpl implements RetryService {

    private final RetryTemplate retryTemplate;
    private final EmailService uploadService;

    @Override
    public void repeatableSendMessage(Mail mail) {
        retryTemplate.execute(context -> uploadService.sendMessage(mail));
    }
}
