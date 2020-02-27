package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.service.ClaimService;
import com.rbkmoney.cm.dudoser.service.MessageBuilderService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.damsel.claim_management.ClaimStatus;
import com.rbkmoney.damsel.claim_management.ClaimStatusChanged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.rbkmoney.cm.dudoser.domain.ClaimStatus.valueOf;

@Service
@Qualifier("statusMessageBuilder")
@RequiredArgsConstructor
@Slf4j
public class StatusChangeMessageBuilderServiceImpl implements MessageBuilderService<ClaimStatusChanged> {

    @Value("${mail.from}")
    private String emailFrom;

    @Value("${mail.subject.status}")
    private String subject;

    private final ClaimService claimService;
    private final TemplateService templateService;

    @Override
    public Message build(ClaimStatusChanged claimStatusChanged, String partyId, long claimId) {
        String emailTo = claimService.getEmailByClaim(partyId, claimId);

        return build(emailFrom, emailTo, getContent(claimStatusChanged, claimId), subject, partyId, claimId);
    }

    private String getContent(ClaimStatusChanged claimStatusChanged, long claimId) {
        ClaimData claimData = ClaimData.builder()
                .templateType(TemplateType.STATUSCHANGE)
                .id(String.valueOf(claimId))
                .status(convertStatus(claimStatusChanged.getStatus()))
                .build();

        return templateService.process(claimData);
    }

    private String convertStatus(ClaimStatus tClaimStatus) {
        return valueOf(tClaimStatus.getSetField().getFieldName().toUpperCase()).getCyrillicValue();
    }
}
