package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.exception.NotFoundException;
import com.rbkmoney.cm.dudoser.exception.ThriftClientException;
import com.rbkmoney.damsel.claim_management.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimManagementSrv.Iface claimManagementClient;

    public String getEmailByClaim(String partyId, long claimId) {
        Claim claim = getClaim(partyId, claimId);

        String emailTo = claim.getChangeset().stream()
                .filter(this::isExternalUser)
                .findFirst()
                .map(ModificationUnit::getUserInfo)
                .map(UserInfo::getEmail)
                .orElse(null);

        if (emailTo == null) {
            throw new NotFoundException(String.format("ExternalUser info from Claim can not be null, partyId=%s, claimId=%s", partyId, claimId));
        }

        return emailTo;
    }

    private Claim getClaim(String partyId, long claimId) {
        try {
            log.info("Trying to get Claim from thrift client, partyId={}, claimId={}", partyId, claimId);

            Claim claim = claimManagementClient.getClaim(partyId, claimId);

            if (claim == null || claim.getChangeset() == null || claim.getChangeset().isEmpty()) {
                throw new NotFoundException(String.format("Changeset from Claim can not be null, partyId=%s, claimId=%s", partyId, claimId));
            }

            return claim;
        } catch (TException ex) {
            throw new ThriftClientException(String.format("Failed to get Claim from thrift client, partyId=%s, claimId=%s", partyId, claimId), ex);
        }
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
