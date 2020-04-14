package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.service.model.FileInfo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private static final Pattern FILE_ATTACHMENT_REGEX = Pattern.compile("filename\\=.*");

    private static final String UNKNOWN_FILE_NAME = "unknown.dat";

    private final RestTemplate restTemplate;

    public FileInfo requestFile(String url) {
        try {
            return restTemplate.execute(url, HttpMethod.GET, null, clientHttpResponse -> {
                HttpHeaders headers = clientHttpResponse.getHeaders();
                List<String> contentDispositionHeader = headers.get("content-disposition");
                String fileName = parseFileName(contentDispositionHeader);
                try (InputStream in = clientHttpResponse.getBody();
                     ByteArrayOutputStream out = new ByteArrayOutputStream();
                     ReadableByteChannel channel = Channels.newChannel(in)) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(in.available());
                    while (channel.read(byteBuffer) > 0) {
                        out.write(byteBuffer.array(), 0, byteBuffer.position());
                        byteBuffer.clear();
                    }

                    return new FileInfo(fileName, out.toByteArray());
                }
            });
        } catch (RestClientException e) {
            throw new RequestFileException(url, e);
        }
    }

//    private FileHolder requestFile(String url) throws IOException {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
//
//            HttpHeaders responseHeaders = response.getHeaders();
//            List<String> contentDispositionHeader = headers.get("content-disposition");
//            String fileName = parseFileName(contentDispositionHeader);
//            byte[] fileData = null;
//            try (InputStream in = new ByteArrayInputStream(response.getBody());
//                 ByteArrayOutputStream out = new ByteArrayOutputStream();
//                 ReadableByteChannel channel = Channels.newChannel(in)) {
//                ByteBuffer byteBuffer = ByteBuffer.allocate(in.available());
//                while (channel.read(byteBuffer) > 0) {
//                    out.write(byteBuffer.array(), 0, byteBuffer.position());
//                    byteBuffer.clear();
//                }
//
//                fileData = out.toByteArray();
//            }
//
//            return new FileHolder(fileName, fileData);
//        } catch (RestClientException e) {
//            throw new RequestFileException(url, e);
//        }
//    }

    private String parseFileName(List<String> headerAttachment) {
        try {
            if (!CollectionUtils.isEmpty(headerAttachment)) {
                String attachmentInfo = headerAttachment.stream().findFirst().orElse(null);
                if (attachmentInfo != null) {
                    Matcher matcher = FILE_ATTACHMENT_REGEX.matcher(attachmentInfo);
                    if (matcher.find()) {
                        return matcher.group(0).split("=")[1];
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception during parse attachment header", e);
        }
        return UNKNOWN_FILE_NAME;
    }

    @Data
    private final static class FileHolder {

        private final String fileName;

        private final byte[] fileData;

    }

}
