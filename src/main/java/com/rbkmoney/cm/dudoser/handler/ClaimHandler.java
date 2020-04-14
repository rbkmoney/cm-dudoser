package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.damsel.claim_management.Event;

public interface ClaimHandler {

    void handle(Event event, ClaimHandlerChain chain);

}
