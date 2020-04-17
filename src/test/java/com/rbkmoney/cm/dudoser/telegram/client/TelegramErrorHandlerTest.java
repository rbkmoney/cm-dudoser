package com.rbkmoney.cm.dudoser.telegram.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cm.dudoser.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TelegramErrorHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;

    private TelegramErrorHandler telegramErrorHandler;

    @Before
    public void setUp() throws Exception {
        telegramErrorHandler = new TelegramErrorHandler(objectMapper);
    }

    @Test(expected = TelegramClientException.class)
    public void testErrorResponse() throws IOException {
        ClientHttpResponse httpResponse = mock(ClientHttpResponse.class);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(httpResponse.getBody()).thenReturn(TestHelper.getClassPathResource("errorResponse.json", getClass()).getInputStream());
        telegramErrorHandler.handleError(httpResponse);
    }

}
