package com.rbkmoney.cm.hooker.service.impl;

import com.rbkmoney.cm.hooker.service.ClaimManagementService;
import com.rbkmoney.damsel.claim_management.Claim;
import com.rbkmoney.damsel.claim_management.ClaimManagementSrv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimManagementServiceImpl implements ClaimManagementService {

    private final ClaimManagementSrv.Iface claimManagementClient;

    @Override
    public Claim getClaim(String partyId, Long claimId) {
        try {
            log.info("Trying to get claim from thrift client, partyId={}, claimId={}", partyId, claimId);

            Claim claim = claimManagementClient.getClaim(partyId, claimId);

            if (claim == null || claim.getChangeset() == null || claim.getChangeset().isEmpty()) {
                throw new RuntimeException(String.format("Claim changeset can not be null, partyId=%s, claimId=%s", partyId, claimId));
            }

            return claim;
        } catch (TException ex) {
            log.error("Failed to get claim from thrift client, partyId={}, claimId={}", partyId, claimId, ex);
            throw new RuntimeException("Some problem with \"claimManagementClient\"", ex);
        }
    }
}
