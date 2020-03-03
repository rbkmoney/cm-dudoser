package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Message;

public interface MessageBuilderService<T> {

    Message build(T change, String partyId, long claimId);

}
