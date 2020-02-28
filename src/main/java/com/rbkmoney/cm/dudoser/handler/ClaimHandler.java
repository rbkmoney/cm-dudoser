package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.service.MailSenderService;
import com.rbkmoney.cm.dudoser.service.MessageBuilderService;
import com.rbkmoney.damsel.claim_management.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimHandler {

    private final MessageBuilderService<ClaimStatusChanged> statusMessageBuilder;
    private final MessageBuilderService<CommentModificationUnit> commentChangeMessageBuilder;
    private final MailSenderService mailSenderService;
    private final RetryTemplate retryTemplate;

    public void handle(Event event) {
        try {
            handleEvent(event);
        } catch (Exception ex) {
            log.error("Some problem when handle", ex);
            throw ex;
        }
    }

    private void handleEvent(Event event) {
        Change change = event.getChange();

        if (change.isSetStatusChanged()) {
            ClaimStatusChanged claimStatusChanged = change.getStatusChanged();

            String partyId = claimStatusChanged.getPartyId();
            long claimId = claimStatusChanged.getId();

            log.info("Handle status change event with id {} for party {} get started", claimId, partyId);

            Message message = statusMessageBuilder.build(claimStatusChanged, partyId, claimId);

            retryTemplate.execute(context -> mailSenderService.send(message));

            log.info("Handle status change event with party id '{}' and claim id '{}' finished", partyId, claimId);
        } else if (containsCommentModifications(change)) {
            ClaimUpdated claimUpdated = change.getUpdated();

            String partyId = claimUpdated.getPartyId();
            long claimId = claimUpdated.getId();

            List<CommentModificationUnit> commentModifications = change.getUpdated().getChangeset().stream()
                    .filter(Modification::isSetClaimModification)
                    .map(Modification::getClaimModification)
                    .filter(ClaimModification::isSetCommentModification)
                    .map(ClaimModification::getCommentModification)
                    .collect(Collectors.toList());

            for (CommentModificationUnit commentModification : commentModifications) {
                log.info("Handle comment modification update change event with id {} for party {} get started", claimId, partyId);

                Message message = commentChangeMessageBuilder.build(commentModification, partyId, claimId);

                retryTemplate.execute(context -> mailSenderService.send(message));

                log.info("Handle comment modification update change event with party id '{}' and claim id '{}' finished", partyId, claimId);
            }
        }
    }

    private boolean containsCommentModifications(Change change) {
        if (change.isSetUpdated()) {
            return change.getUpdated().getChangeset().stream()
                    .filter(Modification::isSetClaimModification)
                    .map(Modification::getClaimModification)
                    .filter(ClaimModification::isSetCommentModification)
                    .map(claimModification -> true)
                    .findFirst()
                    .orElse(false);
        }

        return false;
    }
}
