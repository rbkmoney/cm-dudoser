package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.helper.ClaimHelper;
import com.rbkmoney.cm.dudoser.service.MessageBuilderService;
import com.rbkmoney.cm.dudoser.service.RetryableSenderService;
import com.rbkmoney.damsel.claim_management.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ClaimHandlerOrder(0)
@RequiredArgsConstructor
public class ClaimMailHandler implements ClaimHandler {

    private final MessageBuilderService<ClaimStatusChanged> statusMessageBuilder;
    private final MessageBuilderService<CommentModificationUnit> commentChangeMessageBuilder;
    private final RetryableSenderService retryableSenderService;

    public void handle(Event event, ClaimHandlerChain chain) {
        try {
            handleEvent(event);
        } catch (Exception ex) {
            log.error("Exception during handle claim event", ex);
            throw new ClaimHandlerException(ex);
        } finally {
            chain.doFilter(event);
        }
    }

    private void handleEvent(Event event) {
        Change change = event.getChange();

        if (change.isSetStatusChanged()) {
            ClaimStatusChanged claimStatusChanged = change.getStatusChanged();

            String partyId = claimStatusChanged.getPartyId();
            long claimId = claimStatusChanged.getId();

            log.info("Handle status change event get started, claimId={}, partyId={}", claimId, partyId);

            Message message = statusMessageBuilder.build(claimStatusChanged, event.getUserInfo(), partyId, claimId);

            retryableSenderService.sendToMail(message);

            log.info("Handle status change event finished, claimId={}, partyId={}", partyId, claimId);
        } else if (ClaimHelper.containsCommentModifications(change)) {
            ClaimUpdated claimUpdated = change.getUpdated();

            String partyId = claimUpdated.getPartyId();
            long claimId = claimUpdated.getId();

            List<CommentModificationUnit> commentModifications = ClaimHelper.getCommentModifications(change.getUpdated().getChangeset());

            for (CommentModificationUnit commentModification : commentModifications) {
                log.info("Handle comment modification update change event get started, claimId={}, partyId={}, commentId={}", claimId, partyId, commentModification.getId());

                Message message = commentChangeMessageBuilder.build(commentModification, event.getUserInfo(), partyId, claimId);

                retryableSenderService.sendToMail(message);

                log.info("Handle comment modification update change event finished, claimId={}, partyId={}, commentId={}", claimId, partyId, commentModification.getId());
            }
        }
    }
}
