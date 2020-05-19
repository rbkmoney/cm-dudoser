package com.rbkmoney.cm.dudoser.filter;

import com.rbkmoney.damsel.claim_management.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Predicate;

@Service
public class TestEventFilter implements Predicate<Event> {

    @Value("${filter.test.party-ids}")
    private List<String> partyIds;

    @Override
    public boolean test(Event event) {
        if (!CollectionUtils.isEmpty(partyIds) && event.isSetChange()) {
            String partyId = null;
            if (event.getChange().isSetCreated()) {
                partyId = event.getChange().getCreated().getPartyId();
            } else if (event.getChange().isSetStatusChanged()) {
                partyId = event.getChange().getStatusChanged().getPartyId();
            } else if (event.getChange().isSetUpdated()) {
                partyId = event.getChange().getUpdated().getPartyId();
            }
            return partyIds.contains(partyId);
        }
        return false;
    }

}
