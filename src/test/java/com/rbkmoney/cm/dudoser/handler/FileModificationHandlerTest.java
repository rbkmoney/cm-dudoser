package com.rbkmoney.cm.dudoser.handler;

import com.rbkmoney.cm.dudoser.config.TemplateConfig;
import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import com.rbkmoney.cm.dudoser.handler.claim.modification.FileModificationHandler;
import com.rbkmoney.cm.dudoser.service.FileDownloadService;
import com.rbkmoney.cm.dudoser.service.FileStorageService;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import com.rbkmoney.cm.dudoser.service.model.FileInfo;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.Change;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static com.rbkmoney.cm.dudoser.util.ClaimModificationTestUtils.*;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {
                TemplateConfig.class,
                TemplateService.class,
                FileModificationHandler.class
        })
@EnableConfigurationProperties(TelegramProperties.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {"telegram.files.send.enable=false", "telegram.chatId=1208034847"})
public class FileModificationHandlerTest {

    private static final String TEST_FILENAME = "testFileName";

    @Autowired
    private ClaimModificationHandler fileModificationHandler;

    @Autowired
    private TemplateService templateService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private FileDownloadService fileDownloadService;

    @MockBean
    private TelegramApi telegramApi;

    @Before
    public void setUp() {
        when(fileStorageService.getFileDownloadUrl(anyString(), any(Instant.class)))
                .thenReturn("testUrl");
        FileInfo fileInfo = new FileInfo(TEST_FILENAME, new byte[] {1, 2, 3});
        when(fileDownloadService.requestFile(anyString(), anyString())).thenReturn(fileInfo);
    }

    @Test
    public void chooseFileSendingMethodTst() {
        Change claimUpdated = getClaimUpdated(
                getClaimModification(getFileModification())
        );
        fileModificationHandler
                .handleModification(random(Long.class), random(String.class), claimUpdated.getUpdated().getChangeset());
        verify(fileStorageService, only()).getFileDownloadUrl(anyString(), any(Instant.class));
        verify(fileDownloadService, only()).requestFile(anyString(), anyString());
        verify(telegramApi, only()).sendMessage(any(TelegramSendMessageRequest.class));
    }

    @Test
    public void testTemplateServiceMesage() {
        String expectedMessage = "Новый файл \"testFileName\" в заявке <a href=\"http://iddqd.rbk.money/" +
                "claim-mgt/party/PartyId/claim/101\">101</a>\n";
        ClaimData claimData = ClaimData.builder()
                .id("101")
                .partyId("PartyId")
                .templateType(TemplateType.TELEGRAM_FILE_CHANGE_WITHOUT_FILE)
                .comment(TEST_FILENAME)
                .build();
        String message = templateService.buildTemplate(claimData);
        assertEquals(expectedMessage, message);
    }

}

