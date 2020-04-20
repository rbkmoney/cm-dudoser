package com.rbkmoney.cm.dudoser.helper;

import com.rbkmoney.damsel.claim_management.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClaimHelper {

    public static boolean containsCommentModifications(Change change) {
        List<Modification> modifications = getChangeSet(change);
        return modifications.stream()
                .filter(Modification::isSetClaimModification)
                .map(Modification::getClaimModification)
                .anyMatch(ClaimModification::isSetCommentModification);
    }

    public static boolean containsFileModifications(Change change) {
        List<Modification> modifications = getChangeSet(change);
        return modifications.stream()
                .filter(Modification::isSetClaimModification)
                .map(Modification::getClaimModification)
                .anyMatch(ClaimModification::isSetFileModification);

    }

    public static boolean containsDocumentModifications(Change change) {
        List<Modification> modifications = getChangeSet(change);
        return modifications.stream()
                .filter(Modification::isSetClaimModification)
                .map(Modification::getClaimModification)
                .anyMatch(ClaimModification::isSetDocumentModification);

    }

    public static List<CommentModificationUnit> getCommentModifications(List<Modification> modifications) {
        return modifications.stream()
                .filter(Modification::isSetClaimModification)
                .map(Modification::getClaimModification)
                .filter(ClaimModification::isSetCommentModification)
                .map(ClaimModification::getCommentModification)
                .collect(Collectors.toList());
    }

    public static List<FileModificationUnit> getFileModifications(List<Modification> modifications) {
        return modifications.stream()
                .filter(Modification::isSetClaimModification)
                .map(Modification::getClaimModification)
                .filter(ClaimModification::isSetFileModification)
                .map(ClaimModification::getFileModification)
                .collect(Collectors.toList());
    }

    public static List<DocumentModificationUnit> getDocumentModifications(List<Modification> modifications) {
        return modifications.stream()
                .filter(Modification::isSetClaimModification)
                .map(Modification::getClaimModification)
                .filter(ClaimModification::isSetDocumentModification)
                .map(ClaimModification::getDocumentModification)
                .collect(Collectors.toList());
    }

    public static List<Modification> getChangeSet(Change change) {
        List<Modification> modifications = Collections.emptyList();
        if (change.isSetCreated()) {
            modifications = change.getCreated().getChangeset();
        } else if (change.isSetUpdated()) {
            modifications = change.getUpdated().getChangeset();
        }
        return modifications;
    }

}
