package com.rbkmoney.cm.hooker.config;

import com.rbkmoney.cm.hooker.listener.ClaimEventSinkListener;
import com.rbkmoney.cm.hooker.service.ClaimService;
import com.rbkmoney.cm.hooker.service.RetryService;
import com.rbkmoney.cm.hooker.service.TemplateService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.claim-event-sink.enabled", havingValue = "true")
    public ClaimEventSinkListener paymentEventsKafkaListener(ClaimService claimService,
                                                             TemplateService templateService,
                                                             RetryService retryService) {
        return new ClaimEventSinkListener(claimService, templateService, retryService);
    }
}
