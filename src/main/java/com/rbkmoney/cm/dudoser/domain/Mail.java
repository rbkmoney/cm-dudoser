package com.rbkmoney.cm.dudoser.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Mail {

    private String from;
    private String to;
    private String subject;
    private String content;
    private MailDto mailDto;

}
