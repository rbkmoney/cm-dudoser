package com.rbkmoney.cm.hooker.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimData {

    private TemplateType templateType;
    private String id;
    private String status;
    private String comment;

}
