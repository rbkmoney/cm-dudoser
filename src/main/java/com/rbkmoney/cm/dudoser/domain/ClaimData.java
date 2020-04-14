package com.rbkmoney.cm.dudoser.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimData {

    private TemplateType templateType;
    private String id;
    private String partyId;
    private String status;
    private String comment;
    private String fileDownloadUrl;

}
