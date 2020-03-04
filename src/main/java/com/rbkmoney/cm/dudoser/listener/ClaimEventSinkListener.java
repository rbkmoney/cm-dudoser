package com.rbkmoney.cm.dudoser.listener;

import com.rbkmoney.cm.dudoser.handler.ClaimHandler;
import com.rbkmoney.damsel.claim_management.Event;
import com.rbkmoney.damsel.claim_management.InternalUser;
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
        log.info("Handle claim management Event get started, occuredAt={}", event.getOccuredAt());

        if (event.getUserInfo() != null && isInternalUser(event)) {
            claimHandler.handle(event);
        }

        log.info("Handle claim management Event finished, occuredAt={}", event.getOccuredAt());

        ack.acknowledge();
    }

    private boolean isInternalUser(Event event) {
        return event.getUserInfo().getType().equals(internalUser());
    }

    private com.rbkmoney.damsel.claim_management.UserType internalUser() {
        return com.rbkmoney.damsel.claim_management.UserType.internal_user(new InternalUser());
    }
}
