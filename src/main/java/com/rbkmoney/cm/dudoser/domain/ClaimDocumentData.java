package com.rbkmoney.cm.dudoser.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimDocumentData {

    private String ownerId;

    private String organization;

    private String inn;

    private String registrationDate;

    private String registrationAddress;

    private String okato;

    private String okpo;

    private String headPosition;

    private String headFio;

}
