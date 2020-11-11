package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.damsel.claim_management.Modification;

import java.util.List;

public interface ClaimModificationHandler {

    void handleModification(Long claimId, String partyId, List<Modification> changeSet);

}
