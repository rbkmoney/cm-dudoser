package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.damsel.claim_management.Event;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ClaimHandlerProcessor {

    private final List<ClaimHandler> claimHandlers;

    public ClaimHandlerProcessor(List<ClaimHandler> claimHandlers) {
        this.claimHandlers = claimHandlers.stream()
                .sorted((claimHandler, claimHandlerSec) -> {
                    ClaimHandlerOrder firstClaimOrderAnnotation =
                            AnnotationUtils.findAnnotation(claimHandler.getClass(), ClaimHandlerOrder.class);
                    ClaimHandlerOrder secClaimOrderAnnotation =
                            AnnotationUtils.findAnnotation(claimHandlerSec.getClass(), ClaimHandlerOrder.class);
                    int firstClaimOrder = firstClaimOrderAnnotation != null ? firstClaimOrderAnnotation.value() : 0;
                    int secClaimOrder = secClaimOrderAnnotation != null ? secClaimOrderAnnotation.value() : 0;

                    return Integer.compare(firstClaimOrder, secClaimOrder);
                })
                .collect(Collectors.toList());
    }

    public void processEvent(Event event) {
        ClaimHandlerChain claimHandlerChain = new ClaimHandlerChain(claimHandlers);
        claimHandlerChain.doFilter(event);
    }

}
