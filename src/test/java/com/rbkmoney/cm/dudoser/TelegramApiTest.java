package com.rbkmoney.cm.dudoser;

import com.rbkmoney.cm.dudoser.config.TelegramConfig;
import com.rbkmoney.cm.dudoser.config.properties.TelegramProperties;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramClientException;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramMessage;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendDocumentRequest;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

import java.io.IOException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class TelegramApiTest extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("telegramRestTemplate")
    private RestTemplate telegramRestTemplate;

    @Autowired
    private TelegramApi telegramApi;

    @Autowired
    private TelegramProperties telegramProperties;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() throws Exception {
        RestGatewaySupport gateway = new RestGatewaySupport();
        gateway.setRestTemplate(telegramRestTemplate);
        mockServer = MockRestServiceServer.createServer(gateway);
    }

    @Test
    public void sendMessageTest() throws IOException {
        mockServer.expect(requestTo(TelegramConfig.BASE_URL + telegramProperties.getToken() + "/sendMessage"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(TestHelper.getClassPathResource("sendMessage.json", getClass()),
                        MediaType.APPLICATION_JSON));

        String chatId = "506658066";
        String text = "Just some message";
        TelegramMessage message = telegramApi.sendMessage(new TelegramSendMessageRequest(chatId, text));
        Assert.assertEquals(chatId, message.getChat().getId().toString());
        Assert.assertEquals(text, message.getText());
    }

    @Test
    public void sendDocumentTest() {
        mockServer.expect(requestTo(TelegramConfig.BASE_URL + telegramProperties.getToken() + "/sendDocument"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(TestHelper.getClassPathResource("sendDocument.json", getClass()),
                        MediaType.APPLICATION_JSON));

        String chatId = "546564632";
        String caption = "file text";
        TelegramMessage message = telegramApi.sendDocument(
                new TelegramSendDocumentRequest(chatId, caption, new byte[] {}), "test.dat");
        Assert.assertEquals(chatId, message.getChat().getId().toString());
        Assert.assertEquals(caption, message.getCaption());
    }

    @Test(expected = TelegramClientException.class)
    public void sendDocumentErrorTest() {
        mockServer.expect(requestTo(TelegramConfig.BASE_URL + telegramProperties.getToken() + "/sendDocument"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(TestHelper.getClassPathResource("telegram/client/errorResponse.json", getClass()),
                                MediaType.APPLICATION_JSON));
        TelegramMessage message = telegramApi.sendDocument(
                new TelegramSendDocumentRequest("testChatId", "captionText", new byte[] {}), "test.dat");
    }
}
