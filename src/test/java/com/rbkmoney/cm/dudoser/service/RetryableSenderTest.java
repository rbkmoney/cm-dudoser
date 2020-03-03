package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.exception.MailSendException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CMDudoserApplication.class, webEnvironment = RANDOM_PORT)
public class RetryableSenderTest {

    @MockBean
    private MailSenderService mailSenderService;

    @Autowired
    private RetryableSenderService retryableSenderService;

    @Value("${mail.retry.max.attempts:3}")
    private int maxAttempts;

    @Test
    public void test() {
        AtomicInteger atomicInt = new AtomicInteger(0);
        when(mailSenderService.send(any())).thenAnswer(
                invocation -> {
                    int increment = atomicInt.getAndIncrement();
                    if (increment < maxAttempts - 1) {
                        throw new MailSendException();
                    }
                    return true;
                }
        );

        try {
            retryableSenderService.sendToMail(Message.builder().build());
        } finally {
            verify(mailSenderService, times(maxAttempts)).send(any());
        }
    }
}
