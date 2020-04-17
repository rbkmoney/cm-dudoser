package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.service.model.FileInfo;
import com.rbkmoney.file.storage.FileData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final RestTemplate restTemplate;

    private final FileStorageService fileStorageService;

    public FileInfo requestFile(String url, String fileId) {
        try {
            FileData fileData = fileStorageService.getFileData(fileId);
            String fileName = fileData.getFileName();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            return new FileInfo(fileName, response.getBody());
        } catch (RestClientException e) {
            throw new RequestFileException(url, e);
        }
    }

}
