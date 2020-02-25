package com.rbkmoney.cm.hooker.service;

import com.rbkmoney.cm.hooker.domain.AssistantUploadData;
import com.rbkmoney.cm.hooker.domain.ClaimData;
import com.rbkmoney.damsel.claim_management.ClaimStatusChanged;
import com.rbkmoney.damsel.claim_management.CommentModificationUnit;

public interface ClaimService {

    AssistantUploadData getAssistantUploadData(String partyId, long claimId);

    ClaimData getCommentChangeClaimData(long claimId, CommentModificationUnit commentModification);

    ClaimData getStatusChangeClaimData(long claimId, ClaimStatusChanged claimStatusChanged);

}
