package com.rbkmoney.cm.hooker.listener;

import com.rbkmoney.cm.hooker.domain.AssistantUploadData;
import com.rbkmoney.cm.hooker.domain.ClaimData;
import com.rbkmoney.cm.hooker.domain.UserType;
import com.rbkmoney.cm.hooker.service.ClaimService;
import com.rbkmoney.cm.hooker.service.RetryService;
import com.rbkmoney.cm.hooker.service.TemplateService;
import com.rbkmoney.damsel.claim_management.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class ClaimEventSinkListener {

    private final ClaimService claimService;
    private final TemplateService templateService;
    private final RetryService retryService;

    @KafkaListener(topics = "${kafka.topics.claim-event-sink.id}", containerFactory = "kafkaListenerContainerFactory")
    public void handle(Event event, Acknowledgment ack) throws TException {
        Change change = event.getChange();

        if (change.isSetStatusChanged()) {
            ClaimStatusChanged claimStatusChanged = change.getStatusChanged();

            String partyId = claimStatusChanged.getPartyId();
            long claimId = claimStatusChanged.getId();

            AssistantUploadData assistantData = claimService.getAssistantUploadData(partyId, claimId);

            if (assistantData.getUserType() == UserType.internal_user) {
                ClaimData claimData = claimService.getStatusChangeClaimData(claimId, claimStatusChanged);

                buildTemplateAndUpload(claimData, assistantData);
            }
        } else if (change.isSetUpdated()
                && getUpdateLastModification(change).isSetClaimModification()
                && getUpdateLastModification(change).getClaimModification().isSetCommentModification()) {
            ClaimUpdated claimUpdated = getUpdated(change);

            String partyId = claimUpdated.getPartyId();
            long claimId = claimUpdated.getId();

            AssistantUploadData assistantData = claimService.getAssistantUploadData(partyId, claimId);

            if (assistantData.getUserType() == UserType.internal_user) {
                CommentModificationUnit commentModification = getLastModification(claimUpdated).getClaimModification().getCommentModification();

                ClaimData claimData = claimService.getCommentChangeClaimData(claimId, commentModification);

                buildTemplateAndUpload(claimData, assistantData);
            }
        }

        ack.acknowledge();
    }

    private void buildTemplateAndUpload(ClaimData claimData, AssistantUploadData assistantData) {
        String templateData = templateService.process(claimData);

        retryService.repeatableUpload(assistantData, templateData);
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
