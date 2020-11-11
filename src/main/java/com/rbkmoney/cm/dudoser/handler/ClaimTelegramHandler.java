package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.cm.dudoser.helper.ClaimHelper;
import com.rbkmoney.damsel.claim_management.Change;
import com.rbkmoney.damsel.claim_management.Event;
import com.rbkmoney.damsel.claim_management.Modification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static com.rbkmoney.cm.dudoser.util.ChangeUtils.extractClaimId;
import static com.rbkmoney.cm.dudoser.util.ChangeUtils.extractPartyId;

@Slf4j
@Component
@ClaimHandlerOrder(1)
@RequiredArgsConstructor
public class ClaimTelegramHandler implements ClaimHandler {

    private final ClaimModificationHandler claimCreationHandler;
    private final ClaimModificationHandler documentModificationHandler;
    private final ClaimModificationHandler commentModificationHandler;
    private final ClaimModificationHandler fileModificationHandler;

    @Qualifier("telegramRestTemplate")
    private final RestTemplate restTemplate;

    @Override
    public void handle(Event event, ClaimHandlerChain chain) {
        try {
            handleEvent(event);
        } catch (Exception e) {
            log.error("Exception during handle claim event", e);
            throw new ClaimHandlerException(e);
        } finally {
            chain.doFilter(event);
        }
    }

    private void handleEvent(Event event) throws IOException {
        if (!event.getUserInfo().getType().isSetExternalUser()) return;

        Change change = event.getChange();

        final String partyId = extractPartyId(change);
        final Long claimId = extractClaimId(change);

        if (change.isSetCreated()) {
            List<Modification> changeset = change.getCreated().getChangeset();
            if (ClaimHelper.containsDocumentModifications(change)) {
                documentModificationHandler.handleModification(claimId, partyId, changeset);
            } else {
                claimCreationHandler.handleModification(claimId, partyId, changeset);
            }
        } else if (change.isSetUpdated()) {
            List<Modification> changeset = change.getUpdated().getChangeset();
            if (ClaimHelper.containsCommentModifications(change)) {
                commentModificationHandler.handleModification(claimId, partyId, changeset);
            } else if (ClaimHelper.containsFileModifications(change)) {
                fileModificationHandler.handleModification(claimId, partyId, changeset);
            }
        }
    }

}
