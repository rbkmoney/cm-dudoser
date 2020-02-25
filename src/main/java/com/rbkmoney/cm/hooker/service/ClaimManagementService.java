package com.rbkmoney.cm.hooker.service;

import com.rbkmoney.damsel.claim_management.Claim;

public interface ClaimManagementService {

    Claim getClaim(String partyId, Long claimId);
}
