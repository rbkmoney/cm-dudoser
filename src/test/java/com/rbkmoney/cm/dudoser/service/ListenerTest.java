package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.cm.dudoser.filter.TestEventFilter;
import com.rbkmoney.cm.dudoser.handler.ClaimHandlerProcessor;
import com.rbkmoney.cm.dudoser.listener.ClaimEventSinkListener;
import com.rbkmoney.cm.dudoser.service.model.FileInfo;
import com.rbkmoney.cm.dudoser.telegram.client.TelegramApi;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendDocumentRequest;
import com.rbkmoney.cm.dudoser.telegram.client.model.TelegramSendMessageRequest;
import com.rbkmoney.damsel.claim_management.Change;
import com.rbkmoney.damsel.claim_management.Event;
import com.rbkmoney.damsel.claim_management.UserType;
import com.rbkmoney.questionary.Contractor;
import com.rbkmoney.questionary.IndividualEntity;
import com.rbkmoney.questionary.RussianIndividualEntity;
import com.rbkmoney.questionary.manage.Questionary;
import com.rbkmoney.questionary.manage.QuestionaryData;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static com.rbkmoney.cm.dudoser.util.ClaimModificationTestUtils.*;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CMDudoserApplication.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = "telegram.files.send.enable=true")
public class ListenerTest {

    @MockBean
    private RetryableSenderService retryableSenderService;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private ConversationService conversationService;

    @Autowired
    private ClaimHandlerProcessor claimHandlerProcessor;

    @Autowired
    private TestEventFilter testEventFilter;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private FileDownloadService fileDownloadService;

    @MockBean
    private QuestionaryService questionaryService;

    @MockBean
    private TelegramApi telegramApi;

    private String email = "no-reply@rbk.com";
    private ClaimEventSinkListener listener;

    @Before
    public void setUp() {
        doNothing().when(retryableSenderService).sendToMail(any());
        when(conversationService.getConversation(anyString())).thenReturn(getConversation());
        when(claimService.getEmailByClaim(any(), anyString(), anyLong())).thenReturn(email);
        when(fileStorageService.getFileDownloadUrl(anyString(), any(Instant.class)))
                .thenReturn("testUrl");
        FileInfo fileInfo = new FileInfo("testFileName", new byte[]{1, 2 ,3});
        when(fileDownloadService.requestFile(anyString(), anyString())).thenReturn(fileInfo);
        listener = new ClaimEventSinkListener(claimHandlerProcessor, testEventFilter);
    }

    @Test
    public void testExternalUser() throws Exception {
        //если тип пользователя External, то сервис пропускает любой тип получаемого change
        sendMail(listener, getEvent(getExternalUser(), getClaimCreated()), 0);
        sendMail(listener, getEvent(getExternalUser(), getClaimTestPartyCreated()), 0);
        sendMail(listener, getEvent(getExternalUser(), getClaimStatus(getClaimPending())), 0);
        sendMail(listener, getEvent(getExternalUser(), getClaimUpdated(getClaimModification(getCommentModification()))), 0);
    }

    @Test
    public void testInternalUser() throws Exception {
        //change типа created пропускается
        sendMail(listener, getEvent(getInternalUser(), getClaimCreated()), 0);

        sendMail(listener, getEvent(getInternalUser(), getClaimStatus(getClaimAccepted())), 1);
        sendMail(listener, getEvent(getInternalUser(), getClaimStatus(getClaimDenied())), 1);
        sendMail(listener, getEvent(getInternalUser(), getClaimStatus(getClaimPending())), 1);

        //modification типа НЕ comment пропускается
        sendMail(listener, getEvent(getInternalUser(), getClaimUpdated(getClaimModification(getFileModification()))), 0);

        sendMail(listener, getEvent(getInternalUser(), getClaimUpdated(getClaimModification(getCommentModification()))), 1);

        Change claimUpdated = getClaimUpdated(
                getClaimModification(getCommentModification()),
                getClaimModification(getFileModification()),
                getClaimModification(getCommentModification())
        );

        sendMail(listener, getEvent(getInternalUser(), claimUpdated), 2);
    }

    @Test
    public void testFileTelegramHandler() throws TException {
        Event event = getEvent(getExternalUser(), getClaimUpdated(
                getClaimModification(getFileModification())
        ));

        listener.handle(event, () -> {});

        verify(fileStorageService, only()).getFileDownloadUrl(anyString(), any(Instant.class));
        verify(fileDownloadService, only()).requestFile(anyString(), anyString());
        verify(telegramApi, only()).sendDocument(any(TelegramSendDocumentRequest.class), anyString());
    }

    @Test
    public void testCommentTelegramHandler() throws TException {
        Event event = getEvent(getExternalUser(), getClaimUpdated(
                getClaimModification(getCommentModification())
        ));

        listener.handle(event, () -> {});

        verify(telegramApi, only()).sendMessage(any(TelegramSendMessageRequest.class));
    }

    @Test
    public void testDocumentTelegramHandler() throws TException {
        when(questionaryService.getQuestionary(anyString(), anyString())).thenReturn(buildQuestionary());

        Event event = getEvent(getExternalUser(), getClaimCreated(
                getClaimModification(getDocumentModification())
        ));

        listener.handle(event, () -> {});

        verify(telegramApi, only()).sendMessage(any(TelegramSendMessageRequest.class));
    }

    @Test
    public void testNewClaimTelegramHandler() throws TException {
        Event event = getEvent(getExternalUser(), getClaimCreated());

        listener.handle(event, () -> {});

        verify(telegramApi, only()).sendMessage(any(TelegramSendMessageRequest.class));
    }

    private void sendMail(ClaimEventSinkListener listener, Event event, int timesSending) throws TException {
        reset(retryableSenderService);
        listener.handle(event, () -> {
        });
        verify(retryableSenderService, times(timesSending)).sendToMail(any());
    }

    private Event getEvent(UserType userType, Change change) {
        Event event = random(Event.class, "change", "user_info");
        event.setUserInfo(getUserInfo(userType));
        event.setChange(change);
        return event;
    }

    private Questionary buildQuestionary() {
        Questionary questionary = new Questionary();
        questionary.setId("testId");
        questionary.setPartyId("testPartyId");
        questionary.setOwnerId("ownerId");
        QuestionaryData questionaryData = new QuestionaryData();
        IndividualEntity individualEntity = new IndividualEntity();
        individualEntity.setRussianIndividualEntity(new RussianIndividualEntity());
        questionaryData.setContractor(Contractor.individual_entity(individualEntity));
        questionary.setData(questionaryData);

        return questionary;
    }

}
