package com.rbkmoney.cm.hooker.service.impl;

import com.rbkmoney.cm.hooker.domain.ClaimData;
import com.rbkmoney.cm.hooker.domain.Mail;
import com.rbkmoney.cm.hooker.domain.MailDto;
import com.rbkmoney.cm.hooker.domain.TemplateType;
import com.rbkmoney.cm.hooker.exception.NotFoundException;
import com.rbkmoney.cm.hooker.service.ClaimService;
import com.rbkmoney.cm.hooker.service.MailService;
import com.rbkmoney.cm.hooker.service.TemplateService;
import com.rbkmoney.damsel.claim_management.ClaimStatus;
import com.rbkmoney.damsel.claim_management.ClaimStatusChanged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Qualifier("statusChangedMailService")
@RequiredArgsConstructor
@Slf4j
public class StatusChangeMailServiceImpl implements MailService<ClaimStatusChanged> {

    @Value("${mail.from}")
    private String mailFrom;

    private final ClaimService claimService;
    private final TemplateService templateService;

    @Override
    public Mail buildMail(ClaimStatusChanged claimStatusChanged, String partyId, long claimId) {
        String emailTo = claimService.getEmailByClaim(partyId, claimId);

        String subject = "Изменение статуса вашей заявки на подключение к RBK.money";

        String content = getContent(claimStatusChanged, claimId);

        MailDto mailDto = MailDto.builder()
                .partyId(partyId)
                .claimId(claimId)
                .build();

        return Mail.builder()
                .from(mailFrom)
                .to(emailTo)
                .subject(subject)
                .content(content)
                .mailDto(mailDto)
                .build();
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
