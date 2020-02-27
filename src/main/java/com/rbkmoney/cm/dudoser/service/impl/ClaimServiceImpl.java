package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.service.ClaimManagementService;
import com.rbkmoney.cm.dudoser.service.ClaimService;
import com.rbkmoney.damsel.claim_management.Claim;
import com.rbkmoney.damsel.claim_management.ExternalUser;
import com.rbkmoney.damsel.claim_management.ModificationUnit;
import com.rbkmoney.damsel.claim_management.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private final ClaimManagementService claimManagementService;

    @Override
    public String getEmailByClaim(String partyId, long claimId) {
        Claim claim = claimManagementService.getClaim(partyId, claimId);

        String emailTo = claim.getChangeset().stream()
                .filter(this::isExternalUser)
                .findFirst()
                .map(ModificationUnit::getUserInfo)
                .map(UserInfo::getEmail)
                .orElse(null);

        if (emailTo == null) {
            throw new RuntimeException(String.format("ExternalUser info from \"Claim\" can not be null, partyId=%s, claimId=%s", partyId, claimId));
        }

        return emailTo;
    }

    private boolean isExternalUser(ModificationUnit modificationUnit) {
        return Optional.ofNullable(modificationUnit)
                .map(ModificationUnit::getUserInfo)
                .map(UserInfo::getType)
                .map(userType -> userType.equals(externalUser()))
                .orElse(false);
    }

    private com.rbkmoney.damsel.claim_management.UserType externalUser() {
        return com.rbkmoney.damsel.claim_management.UserType.external_user(new ExternalUser());
    }
}
