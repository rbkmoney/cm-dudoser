package com.rbkmoney.cm.dudoser.util;

import com.rbkmoney.damsel.claim_management.Change;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChangeUtils {

    public static String extractPartyId(Change change) {
        if (change.isSetCreated()) {
            return change.getCreated().getPartyId();
        } else if (change.isSetUpdated()) {
            return change.getUpdated().getPartyId();
        } else if (change.isSetStatusChanged()) {
            return change.getStatusChanged().getPartyId();
        }
        throw new IllegalArgumentException("Unknown change type: " + change);
    }

    public static Long extractClaimId(Change change) {
        if (change.isSetCreated()) {
            return change.getCreated().getId();
        } else if (change.isSetUpdated()) {
            return change.getUpdated().getId();
        } else if (change.isSetStatusChanged()) {
            return change.getStatusChanged().getId();
        }
        throw new IllegalArgumentException("Unknown change type: " + change);
    }

}
