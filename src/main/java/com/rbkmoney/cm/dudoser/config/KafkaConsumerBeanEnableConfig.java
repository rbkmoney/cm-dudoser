package com.rbkmoney.cm.dudoser.config;

import com.rbkmoney.cm.dudoser.handler.ClaimHandler;
import com.rbkmoney.cm.dudoser.handler.ClaimHandlerProcessor;
import com.rbkmoney.cm.dudoser.listener.ClaimEventSinkListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.List;

@EnableKafka
@Configuration
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.claim-event-sink.enabled", havingValue = "true")
    public ClaimEventSinkListener paymentEventsKafkaListener(ClaimHandlerProcessor claimHandlerProcessor) {
        return new ClaimEventSinkListener(claimHandlerProcessor);
    }

    @Bean
    public ClaimHandlerProcessor claimHandlerProcessor(List<ClaimHandler> claimHandlers) {
        return new ClaimHandlerProcessor(claimHandlers);
    }


}
