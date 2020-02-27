package com.rbkmoney.cm.dudoser.listener;

import com.rbkmoney.cm.dudoser.handler.ClaimHandler;
import com.rbkmoney.damsel.claim_management.*;
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
        eventLogging(event);

        if (event.getUserInfo() != null && isInternalUser(event)) {
            claimHandler.handle(event);
        }

        ack.acknowledge();
    }

    private void eventLogging(Event event) {
        String recLogMessage = "status - %s, id - %s, party id - %s";
        String message;
        Change change = event.getChange();
        if (change.isSetCreated()) {
            ClaimCreated created = change.getCreated();
            message = String.format(recLogMessage, "created", created.getId(), created.getPartyId());
        } else if (change.isSetUpdated()) {
            ClaimUpdated updated = change.getUpdated();
            message = String.format(recLogMessage, "updated", updated.getId(), updated.getPartyId());
        } else if (change.isSetStatusChanged()) {
            ClaimStatusChanged statusChanged = change.getStatusChanged();
            message = String.format(recLogMessage, "status changed", statusChanged.getId(), statusChanged.getPartyId());
        } else {
            message = "change type not found";
        }

        log.info("New record received from kafka ({})", message);
    }

    private boolean isInternalUser(Event event) {
        return event.getUserInfo().getType().equals(internalUser());
    }

    private com.rbkmoney.damsel.claim_management.UserType internalUser() {
        return com.rbkmoney.damsel.claim_management.UserType.internal_user(new InternalUser());
    }
}
