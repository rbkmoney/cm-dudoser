package com.rbkmoney.cm.dudoser.config;

import com.rbkmoney.cm.dudoser.handler.ClaimHandler;
import com.rbkmoney.cm.dudoser.listener.ClaimEventSinkListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.claim-event-sink.enabled", havingValue = "true")
    public ClaimEventSinkListener paymentEventsKafkaListener(ClaimHandler claimHandler) {
        return new ClaimEventSinkListener(claimHandler);
    }
}
