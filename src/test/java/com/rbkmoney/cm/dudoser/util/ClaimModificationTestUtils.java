package com.rbkmoney.cm.dudoser.util;

import com.rbkmoney.damsel.claim_management.*;
import com.rbkmoney.damsel.messages.Conversation;
import com.rbkmoney.damsel.messages.Message;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClaimModificationTestUtils {

    public static Change getClaimCreated() {
        ClaimCreated changed = random(ClaimCreated.class, "changeset");
        changed.setChangeset(List.of(getClaimModification(getCommentModification())));
        return Change.created(changed);
    }

    public static Change getClaimCreated(Modification... modifications) {
        ClaimCreated changed = random(ClaimCreated.class, "changeset");
        changed.setChangeset(List.of(modifications));
        return Change.created(changed);
    }

    public static Change getClaimTestPartyCreated() {
        ClaimCreated changed = random(ClaimCreated.class, "changeset");
        changed.setChangeset(List.of(getClaimModification(getCommentModification())));
        changed.setPartyId("test");
        return Change.created(changed);
    }

    public static Change getClaimUpdated(Modification... modifications) {
        ClaimUpdated changed = random(ClaimUpdated.class, "changeset");
        changed.setChangeset(List.of(modifications));
        return Change.updated(changed);
    }

    public static Change getClaimStatus(ClaimStatus claimStatus) {
        ClaimStatusChanged changed = random(ClaimStatusChanged.class, "status");
        changed.setStatus(claimStatus);
        return Change.status_changed(changed);
    }

    public static Conversation getConversation() {
        List<Message> messages = randomListOf(5, Message.class);

        Conversation conversation = random(Conversation.class, "messages");
        conversation.setMessages(messages);
        return conversation;
    }

    public static ClaimModification getCommentModification() {
        CommentModificationUnit modificationUnit = new CommentModificationUnit();
        modificationUnit.setId("asd");
        modificationUnit.setModification(CommentModification.creation(new CommentCreated()));
        return ClaimModification.comment_modification(modificationUnit);
    }

    public static ClaimModification getStatusModification(ClaimStatus claimStatus) {
        StatusModificationUnit modificationUnit = new StatusModificationUnit();
        modificationUnit.setStatus(claimStatus);
        modificationUnit.setModification(StatusModification.change(new StatusChanged()));
        return ClaimModification.status_modification(modificationUnit);
    }

    public static ClaimModification getFileModification() {
        FileModificationUnit modificationUnit = new FileModificationUnit();
        modificationUnit.setId("asd");
        modificationUnit.setModification(FileModification.creation(new FileCreated()));
        return ClaimModification.file_modification(modificationUnit);
    }

    public static ClaimModification getDocumentModification() {
        DocumentModificationUnit documentModificationUnit = new DocumentModificationUnit();
        documentModificationUnit.setId("asd");
        documentModificationUnit.setModification(DocumentModification.creation(new DocumentCreated()));
        return ClaimModification.document_modification(documentModificationUnit);
    }

    public static UserInfo getUserInfo(UserType userType) {
        UserInfo userInfo = random(UserInfo.class, "type");
        userInfo.setType(userType);
        return userInfo;
    }

    public static Modification getClaimModification(ClaimModification modification) {
        return Modification.claim_modification(modification);
    }

    public static ClaimStatus getClaimPending() {
        return ClaimStatus.pending(new ClaimPending());
    }

    public static ClaimStatus getClaimAccepted() {
        return ClaimStatus.accepted(new ClaimAccepted());
    }

    public static ClaimStatus getClaimDenied() {
        return ClaimStatus.denied(new ClaimDenied());
    }

    public static UserType getExternalUser() {
        return UserType.external_user(new ExternalUser());
    }

    public static UserType getInternalUser() {
        return UserType.internal_user(new InternalUser());
    }

    private Claim getClaim(String email) {
        ModificationUnit modificationUnit = getModificationUnit(getExternalUser(), getCommentModification());
        modificationUnit.getUserInfo().setEmail(email);
        List<ModificationUnit> changeset = List.of(
                getModificationUnit(getInternalUser(), getCommentModification()),
                modificationUnit,
                getModificationUnit(getInternalUser(), getCommentModification()),
                getModificationUnit(getInternalUser(), getStatusModification(getClaimPending())),
                getModificationUnit(getExternalUser(), getFileModification())
        );
        return getClaim(getClaimPending(), changeset);
    }

    private Claim getClaim(ClaimStatus status, List<ModificationUnit> changeset) {
        Claim claim = random(Claim.class, "status", "changeset", "metadata");
        claim.setStatus(status);
        claim.setChangeset(changeset);
        return claim;
    }

    private ModificationUnit getModificationUnit(UserType userType, ClaimModification claimModification) {
        ModificationUnit modification = random(ModificationUnit.class, "modification", "user_info");
        modification.setUserInfo(getUserInfo(userType));
        modification.setModification(getClaimModification(claimModification));
        return modification;
    }

}
