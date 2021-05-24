package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.damsel.claim_management.Event;

import java.util.Iterator;
import java.util.List;

public class ClaimHandlerChain {

    private final Iterator<ClaimHandler> iterator;

    public ClaimHandlerChain(List<ClaimHandler> handlers) {
        this.iterator = handlers.iterator();
    }

    public void doFilter(Event event) {
        while (iterator.hasNext()) {
            ClaimHandler claimHandler = iterator.next();
            claimHandler.handle(event, this);
        }
    }

}
