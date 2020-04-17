package com.rbkmoney.cm.dudoser.telegram.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramDocument {

    @JsonProperty("file_id")
   private String fileId;

    @JsonProperty("file_unique_id")
    private String fileUniqueId;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("file_size")
    private String fileSize;

}
