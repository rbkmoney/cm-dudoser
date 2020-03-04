package com.rbkmoney.cm.dudoser.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ClaimStatus {

    PENDING("В ожидании"),
    REVIEW("На рассмотрении"),
    PENDING_ACCEPTANCE("В ожидании подтверждения"),
    ACCEPTED("Подтверждена"),
    DENIED("Отклонена"),
    REVOKED("Отозвана");

    private final String cyrillicValue;

}
