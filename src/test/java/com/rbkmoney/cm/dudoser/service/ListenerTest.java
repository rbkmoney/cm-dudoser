package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.cm.dudoser.handler.ClaimHandler;
import com.rbkmoney.cm.dudoser.listener.ClaimEventSinkListener;
import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.Message;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CMDudoserApplication.class, webEnvironment = RANDOM_PORT)
public class ListenerTest {

    @MockBean
    private MailSenderService mailSenderService;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private ConversationService conversationService;

    @Autowired
    private ClaimHandler claimHandler;

    private String email = "no-reply@rbk.com";
    private ClaimEventSinkListener listener;

    @Before
    public void setUp() throws Exception {
        when(mailSenderService.send(any())).thenReturn(true);
        when(conversationService.getConversation(anyString())).thenReturn(getConversation());
        when(claimService.getEmailByClaim(anyString(), anyLong())).thenReturn(email);
        listener = new ClaimEventSinkListener(claimHandler);
    }

    @Test
    public void testExternalUser() throws Exception {
        //filtered
        sendMail(listener, getEvent(getExternalUser(), getClaimCreated()), 0);
        sendMail(listener, getEvent(getExternalUser(), getClaimStatus(getClaimPending())), 0);
        sendMail(listener, getEvent(getExternalUser(), getClaimUpdated(getClaimModification(getCommentModification()))), 0);
    }

    @Test
    public void testInternalUser() throws Exception {
        //filtered
        sendMail(listener, getEvent(getInternalUser(), getClaimCreated()), 0);

        sendMail(listener, getEvent(getInternalUser(), getClaimStatus(getClaimAccepted())), 1);
        sendMail(listener, getEvent(getInternalUser(), getClaimStatus(getClaimDenied())), 1);
        sendMail(listener, getEvent(getInternalUser(), getClaimStatus(getClaimPending())), 1);

        // filtered
        sendMail(listener, getEvent(getInternalUser(), getClaimUpdated(getClaimModification(getNotCommentModification()))), 0);

        sendMail(listener, getEvent(getInternalUser(), getClaimUpdated(getClaimModification(getCommentModification()))), 1);
    }

    private void sendMail(ClaimEventSinkListener listener, Event event, int times) throws TException {
        reset(mailSenderService);
        listener.handle(event, () -> {
        });
        verify(mailSenderService, times(times)).send(any());
    }

    private Event getEvent(UserType userType, Change change) {
        Event event = random(Event.class, "change", "user_info");
        event.setUserInfo(getUserInfo(userType));
        event.setChange(change);
        return event;
    }

    private Change getClaimCreated() {
        ClaimCreated changed = random(ClaimCreated.class, "changeset");
        changed.setChangeset(List.of(getClaimModification(getCommentModification())));
        return Change.created(changed);
    }

    private Change getClaimUpdated(Modification modification) {
        ClaimUpdated changed = random(ClaimUpdated.class, "changeset");
        changed.setChangeset(List.of(modification));
        return Change.updated(changed);
    }

    private Change getClaimStatus(ClaimStatus claimStatus) {
        ClaimStatusChanged changed = random(ClaimStatusChanged.class, "status");
        changed.setStatus(claimStatus);
        return Change.status_changed(changed);
    }

    private Claim getClaim(String email) {
        ModificationUnit modificationUnit = getModificationUnit(getExternalUser(), getCommentModification());
        modificationUnit.getUserInfo().setEmail(email);
        List<ModificationUnit> changeset = List.of(
                getModificationUnit(getInternalUser(), getCommentModification()),
                modificationUnit,
                getModificationUnit(getInternalUser(), getCommentModification()),
                getModificationUnit(getInternalUser(), getStatusModification(getClaimPending())),
                getModificationUnit(getExternalUser(), getNotCommentModification())
        );
        return getClaim(getClaimPending(), changeset);
    }

    private Conversation getConversation() {
        List<Message> messages = randomListOf(5, Message.class);

        Conversation conversation = random(Conversation.class, "messages");
        conversation.setMessages(messages);
        return conversation;
    }

    private Claim getClaim(ClaimStatus status, List<ModificationUnit> changeset) {
        Claim claim = random(Claim.class, "status", "changeset", "metadata");
        claim.setStatus(status);
        claim.setChangeset(changeset);
        return claim;
    }

    private ClaimModification getCommentModification() {
        CommentModificationUnit modificationUnit = new CommentModificationUnit();
        modificationUnit.setId("asd");
        modificationUnit.setModification(CommentModification.creation(new CommentCreated()));
        return ClaimModification.comment_modification(modificationUnit);
    }

    private ClaimModification getStatusModification(ClaimStatus claimStatus) {
        StatusModificationUnit modificationUnit = new StatusModificationUnit();
        modificationUnit.setStatus(claimStatus);
        modificationUnit.setModification(StatusModification.change(new StatusChanged()));
        return ClaimModification.status_modification(modificationUnit);
    }

    private ClaimModification getNotCommentModification() {
        FileModificationUnit modificationUnit = new FileModificationUnit();
        modificationUnit.setId("asd");
        modificationUnit.setModification(FileModification.creation(new FileCreated()));
        return ClaimModification.file_modification(modificationUnit);
    }

    private ModificationUnit getModificationUnit(UserType userType, ClaimModification claimModification) {
        ModificationUnit modification = random(ModificationUnit.class, "modification", "user_info");
        modification.setUserInfo(getUserInfo(userType));
        modification.setModification(getClaimModification(claimModification));
        return modification;
    }

    private UserInfo getUserInfo(UserType userType) {
        UserInfo userInfo = random(UserInfo.class, "type");
        userInfo.setType(userType);
        return userInfo;
    }

    private Modification getClaimModification(ClaimModification modification) {
        return Modification.claim_modification(modification);
    }

    private ClaimStatus getClaimPending() {
        return ClaimStatus.pending(new ClaimPending());
    }

    private ClaimStatus getClaimAccepted() {
        return ClaimStatus.accepted(new ClaimAccepted());
    }

    private ClaimStatus getClaimDenied() {
        return ClaimStatus.denied(new ClaimDenied());
    }

    private UserType getExternalUser() {
        return UserType.external_user(new ExternalUser());
    }

    private UserType getInternalUser() {
        return UserType.internal_user(new InternalUser());
    }
}
