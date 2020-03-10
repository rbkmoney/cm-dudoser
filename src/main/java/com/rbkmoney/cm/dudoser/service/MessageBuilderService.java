package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.damsel.claim_management.UserInfo;

public interface MessageBuilderService<T> {

    Message build(T change, UserInfo userInfo, String partyId, long claimId);

}
