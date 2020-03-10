package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.exception.NotFoundException;
import com.rbkmoney.cm.dudoser.exception.ThriftClientException;
import com.rbkmoney.cm.dudoser.meta.*;
import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.trace.ContextUtils;
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

    public String getEmailByClaim(UserInfo userInfo, String partyId, long claimId) {
        Claim claim = getClaim(userInfo, partyId, claimId);

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

    private Claim getClaim(UserInfo userInfo, String partyId, long claimId) {
        try {
            log.info("Trying to get Claim from thrift client, partyId={}, claimId={}", partyId, claimId);

            Claim claim = new WFlow().createServiceFork(
                    () -> {
                        ContextUtils.setCustomMetadataValue(UserIdentityIdExtensionKit.KEY, userInfo.getId());
                        ContextUtils.setCustomMetadataValue(UserIdentityEmailExtensionKit.KEY, userInfo.getEmail());
                        ContextUtils.setCustomMetadataValue(UserIdentityUsernameExtensionKit.KEY, userInfo.getUsername());
                        ContextUtils.setCustomMetadataValue(UserIdentityRealmExtensionKit.KEY, getType(userInfo));
                        return claimManagementClient.getClaim(partyId, claimId);
                    }
            ).call();

            if (claim == null || claim.getChangeset() == null || claim.getChangeset().isEmpty()) {
                throw new NotFoundException(String.format("Changeset from Claim can not be null, partyId=%s, claimId=%s", partyId, claimId));
            }

            return claim;
        } catch (TException ex) {
            throw new ThriftClientException(String.format("Failed to get Claim from thrift client, partyId=%s, claimId=%s", partyId, claimId), ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private UserTypeEnum getType(UserInfo userInfo) {
        if (userInfo.getType().isSetInternalUser()) {
            return UserTypeEnum.internal;
        }
        if (userInfo.getType().isSetExternalUser()) {
            return UserTypeEnum.external;
        }
        throw new IllegalArgumentException();
    }

    private boolean isExternalUser(ModificationUnit modificationUnit) {
        return Optional.ofNullable(modificationUnit)
                .map(ModificationUnit::getUserInfo)
                .map(UserInfo::getType)
                .map(UserType::isSetExternalUser)
                .orElse(false);
    }
}
