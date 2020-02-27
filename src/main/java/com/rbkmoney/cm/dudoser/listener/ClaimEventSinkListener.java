package com.rbkmoney.cm.dudoser.listener;

import com.rbkmoney.cm.dudoser.domain.Mail;
import com.rbkmoney.cm.dudoser.service.MailService;
import com.rbkmoney.cm.dudoser.service.RetryService;
import com.rbkmoney.damsel.claim_management.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class ClaimEventSinkListener {

    private final MailService<ClaimStatusChanged> statusChangedMailService;
    private final MailService<CommentModificationUnit> commentChangeMailService;
    private final RetryService retryService;

    @KafkaListener(topics = "${kafka.topics.claim-event-sink.id}", containerFactory = "kafkaListenerContainerFactory")
    public void handle(Event event, Acknowledgment ack) throws TException {
        Change change = event.getChange();

        if (event.getUserInfo() != null && isInternalUser(event)) {
            if (change.isSetStatusChanged()) {
                ClaimStatusChanged claimStatusChanged = change.getStatusChanged();

                String partyId = claimStatusChanged.getPartyId();
                long claimId = claimStatusChanged.getId();

                Mail mail = statusChangedMailService.buildMail(claimStatusChanged, partyId, claimId);

                retryService.repeatableSendMessage(mail);
            } else if (change.isSetUpdated()
                    && getUpdateLastModification(change).isSetClaimModification()
                    && getUpdateLastModification(change).getClaimModification().isSetCommentModification()) {
                ClaimUpdated claimUpdated = getUpdated(change);

                String partyId = claimUpdated.getPartyId();
                long claimId = claimUpdated.getId();
                CommentModificationUnit commentModification = getLastModification(claimUpdated).getClaimModification().getCommentModification();

                Mail mail = commentChangeMailService.buildMail(commentModification, partyId, claimId);

                retryService.repeatableSendMessage(mail);
            }
        }

        ack.acknowledge();
    }

    private boolean isInternalUser(Event event) {
        return event.getUserInfo().getType().equals(internalUser());
    }

    private com.rbkmoney.damsel.claim_management.UserType internalUser() {
        return com.rbkmoney.damsel.claim_management.UserType.internal_user(new InternalUser());
    }

    private ClaimUpdated getUpdated(Change change) {
        return change.getUpdated();
    }

    private Modification getUpdateLastModification(Change change) {
        return getLastModification(getUpdated(change));
    }

    private Modification getLastModification(ClaimUpdated claimUpdated) {
        return claimUpdated.getChangeset().get(claimUpdated.getChangeset().size() - 1);
    }
}
