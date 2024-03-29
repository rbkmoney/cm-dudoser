package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.service.ClaimService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.damsel.claim_management.ClaimStatus;
import com.rbkmoney.damsel.claim_management.ClaimStatusChanged;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.rbkmoney.cm.dudoser.domain.ClaimStatus.valueOf;

@Service
@Qualifier("statusMessageBuilder")
public class StatusChangeMessageBuilderServiceImpl extends AbstractMessageBuilderService<ClaimStatusChanged> {

    private final TemplateService templateService;

    public StatusChangeMessageBuilderServiceImpl(ClaimService claimService,
                                                 @Value("${mail.from}") String emailFrom,
                                                 @Value("${mail.subject.status}") String subject,
                                                 TemplateService templateService) {
        super(claimService, emailFrom, subject);
        this.templateService = templateService;
    }

    protected String getContent(ClaimStatusChanged claimStatusChanged, long claimId) {
        ClaimData claimData = ClaimData.builder()
                .templateType(TemplateType.STATUS_CHANGE)
                .id(String.valueOf(claimId))
                .status(convertStatus(claimStatusChanged.getStatus()))
                .build();

        return templateService.buildTemplate(claimData);
    }

    private String convertStatus(ClaimStatus claimStatus) {
        return valueOf(claimStatus.getSetField().getFieldName().toUpperCase()).getCyrillicValue();
    }
}
