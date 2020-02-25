package com.rbkmoney.cm.hooker.service.impl;

import com.rbkmoney.cm.hooker.domain.AssistantUploadData;
import com.rbkmoney.cm.hooker.service.RetryService;
import com.rbkmoney.cm.hooker.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryServiceImpl implements RetryService {

    private final RetryTemplate retryTemplate;
    private final UploadService uploadService;

    @Override
    public void repeatableUpload(AssistantUploadData assistantData, String templateData) {
        retryTemplate.execute(context -> uploadService.upload(assistantData, templateData));
    }
}
