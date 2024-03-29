package com.rbkmoney.cm.dudoser.config;

import com.rbkmoney.cm.dudoser.exception.MailSendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

@EnableRetry
@Configuration
public class RetryConfig {

    @Value("${mail.retry.backoff.period:1000}")
    private long backOffPeriod;

    @Value("${mail.retry.max.attempts:3}")
    private int maxAttempts;

    @Bean
    public RetryTemplate mailRetryTemplate() {
        FixedBackOffPolicy policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(backOffPeriod);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(
                new SimpleRetryPolicy(maxAttempts, Collections.singletonMap(MailSendException.class, true))
        );
        retryTemplate.setBackOffPolicy(policy);
        return retryTemplate;
    }
}
