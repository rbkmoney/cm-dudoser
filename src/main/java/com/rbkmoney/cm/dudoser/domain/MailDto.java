package com.rbkmoney.cm.dudoser.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailDto {

    private String partyId;
    private long claimId;

}
