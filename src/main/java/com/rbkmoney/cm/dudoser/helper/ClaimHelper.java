package com.rbkmoney.cm.dudoser.helper;

import com.rbkmoney.damsel.claim_management.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClaimHelper {

    public static boolean containsCommentModifications(Change change) {
        if (change.isSetUpdated()) {
            return change.getUpdated().getChangeset().stream()
                    .filter(Modification::isSetClaimModification)
                    .map(Modification::getClaimModification)
                    .anyMatch(ClaimModification::isSetCommentModification);
        }
        return false;
    }

    public static boolean containsFileModifications(Change change) {
        if (change.isSetUpdated()) {
            return change.getUpdated().getChangeset().stream()
                    .filter(Modification::isSetClaimModification)
                    .map(Modification::getClaimModification)
                    .anyMatch(ClaimModification::isSetFileModification);
        }
        return false;
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

}
