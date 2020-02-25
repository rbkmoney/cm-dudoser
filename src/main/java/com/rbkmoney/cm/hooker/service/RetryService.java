package com.rbkmoney.cm.hooker.service;

import com.rbkmoney.cm.hooker.domain.AssistantUploadData;

public interface RetryService {

    void repeatableUpload(AssistantUploadData assistantData, String templateData);

}
