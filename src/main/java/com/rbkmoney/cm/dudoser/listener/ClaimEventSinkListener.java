package com.rbkmoney.cm.dudoser.listener;

import com.rbkmoney.cm.dudoser.handler.ClaimHandler;
import com.rbkmoney.damsel.claim_management.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class ClaimEventSinkListener {

    private final ClaimHandler claimHandler;

    @KafkaListener(topics = "${kafka.topics.claim-event-sink.id}", containerFactory = "kafkaListenerContainerFactory")
    public void handle(Event event, Acknowledgment ack) throws TException {
        log.info("Handle claim management Event get started, event={}", event);

        if (event.getUserInfo() != null && event.getUserInfo().getType().isSetInternalUser()) {
            claimHandler.handle(event);
        }

        log.info("Handle claim management Event finished, event={}", event);

        ack.acknowledge();
    }
}
