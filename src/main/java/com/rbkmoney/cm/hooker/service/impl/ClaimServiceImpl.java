package com.rbkmoney.cm.hooker.service.impl;

import com.rbkmoney.cm.hooker.domain.AssistantUploadData;
import com.rbkmoney.cm.hooker.domain.ClaimData;
import com.rbkmoney.cm.hooker.exception.NotFoundException;
import com.rbkmoney.cm.hooker.service.ClaimManagementService;
import com.rbkmoney.cm.hooker.service.ClaimService;
import com.rbkmoney.cm.hooker.service.MessageService;
import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.Message;
import com.rbkmoney.geck.common.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private final ClaimManagementService claimManagementService;
    private final MessageService messageService;

    @Override
    public AssistantUploadData getAssistantUploadData(String partyId, long claimId) {
        Claim claim = claimManagementService.getClaim(partyId, claimId);

        UserInfo userInfo = claim.getChangeset().get(0).getUserInfo();

        return AssistantUploadData.builder()
                .partyId(claim.getPartyId())
                .claimId(claim.getId())
                .revision(claim.getRevision())
                .updatedAt(TypeUtil.stringToInstant(claim.getUpdatedAt()))
                .userId(userInfo.getId())
                .userName(userInfo.getUsername())
                .targetAddress(userInfo.getEmail())
                .userType(com.rbkmoney.cm.hooker.domain.UserType.valueOf(userInfo.getType().getSetField().getFieldName()))
                .build();
    }

    @Override
    public ClaimData getCommentChangeClaimData(long claimId, CommentModificationUnit commentModification) {
        Conversation conversation = messageService.getConversation(commentModification.getId());

        Message message = conversation.getMessages().get(conversation.getMessages().size() - 1);

        return ClaimData.builder()
                .id(String.valueOf(claimId))
                .comment(message.getText())
                .build();
    }

    @Override
    public ClaimData getStatusChangeClaimData(long claimId, ClaimStatusChanged claimStatusChanged) {
        return ClaimData.builder()
                .id(String.valueOf(claimId))
                .status(convertStatus(claimStatusChanged.getStatus()))
                .build();
    }

    private String convertStatus(ClaimStatus tClaimStatus) {
        com.rbkmoney.cm.hooker.domain.ClaimStatus claimStatus = convertClaimStatus(tClaimStatus);
        return resolve(claimStatus);
    }

    private com.rbkmoney.cm.hooker.domain.ClaimStatus convertClaimStatus(ClaimStatus claimStatus) {
        return com.rbkmoney.cm.hooker.domain.ClaimStatus.valueOf(claimStatus.getSetField().getFieldName());
    }

    private String resolve(com.rbkmoney.cm.hooker.domain.ClaimStatus claimStatus) {
        switch (claimStatus) {
            case denied:
                return "Отклонена";
            case review:
                return "На рассмотрении";
            case pending:
                return "В ожидании";
            case revoked:
                return "Отозвана";
            case accepted:
                return "Подтверждена";
            case pending_acceptance:
                return "В ожидании подтверждения";
            default:
                throw new NotFoundException("claimStatus not found");
        }
    }
}
