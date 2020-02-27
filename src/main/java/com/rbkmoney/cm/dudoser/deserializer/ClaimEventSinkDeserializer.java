package com.rbkmoney.cm.dudoser.deserializer;

import com.rbkmoney.damsel.claim_management.Event;
import com.rbkmoney.kafka.common.serialization.AbstractThriftDeserializer;

public class ClaimEventSinkDeserializer extends AbstractThriftDeserializer<Event> {

    @Override
    public Event deserialize(String s, byte[] bytes) {
        return super.deserialize(bytes, new Event());
    }

}
