package com.rbkmoney.cm.hooker.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AssistantUploadData {

    private String partyId;
    private long claimId;
    private int revision;
    private Instant updatedAt;

    public String userId;
    public String userName;
    private String targetAddress;
    public UserType userType;

}
