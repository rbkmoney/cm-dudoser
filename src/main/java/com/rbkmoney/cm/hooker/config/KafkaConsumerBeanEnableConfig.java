package com.rbkmoney.cm.hooker.config;

import com.rbkmoney.cm.hooker.listener.ClaimEventSinkListener;
import com.rbkmoney.cm.hooker.service.MailService;
import com.rbkmoney.cm.hooker.service.RetryService;
import com.rbkmoney.damsel.claim_management.ClaimStatusChanged;
import com.rbkmoney.damsel.claim_management.CommentModificationUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.claim-event-sink.enabled", havingValue = "true")
    public ClaimEventSinkListener paymentEventsKafkaListener(MailService<ClaimStatusChanged> statusChangedMailService,
                                                             MailService<CommentModificationUnit> commentChangeMailService,
                                                             RetryService retryService) {
        return new ClaimEventSinkListener(statusChangedMailService, commentChangeMailService, retryService);
    }
}
