package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.exception.ThriftClientException;
import com.rbkmoney.file.storage.FileData;
import com.rbkmoney.file.storage.FileStorageSrv;
import com.rbkmoney.geck.common.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageSrv.Iface fileStorageClient;

    public String getFileDownloadUrl(String fileId, Instant expire) {
        String timestamp = TypeUtil.temporalToString(expire);
        try {
            return fileStorageClient.generateDownloadUrl(fileId, timestamp);
        } catch (TException e) {
            String errMsg = String.format("Exception during generate download url. fileId=%s", fileId);
            throw new ThriftClientException(errMsg, e);
        }
    }

    public FileData getFileData(String fileId) {
        try {
            return fileStorageClient.getFileData(fileId);
        } catch (TException e) {
            throw new ThriftClientException(String.format("Get file data failed. fileId=%s", fileId));
        }
    }

}
