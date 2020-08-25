package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component
@RequiredArgsConstructor
public class TemplateService {

    private static final String TITLE = "claimManagementData";
    private static final String STATUS_CHANGED_TEMPLATE = "vm/StatusChangedEntity.vm";
    private static final String COMMENT_CHANGED_TEMPLATE = "vm/CommentChangedEntity.vm";
    private static final String TELEGRAM_COMMENT_CHANGE_TEMPLATE = "vm/TelegramCommentChange.vm";
    private static final String TELEGRAM_FILE_CHANGE_TEMPLATE = "vm/TelegramFileChange.vm";
    private static final String TELEGRAM_IP_CREATED_TEMPLATE = "vm/TelegramCreatedIndividualEntity.vm";
    private static final String TELEGRAM_LE_CREATED_TEMPLATE = "vm/TelegramCreatedLegalEntity.vm";
    private static final String TELEGRAM_ILE_CREATED_TEMPLATE = "vm/TelegramCreatedInternationLegalEntity.vm";
    private static final String TELEGRAM_NEW_CLAIM_TEMPLATE = "vm/TelegramNewClaim.vm";

    private final VelocityEngine claimManagementTemplateEngine;

    public String buildTemplate(ClaimData data) {
        VelocityContext headerContext = new VelocityContext();
        headerContext.put(TITLE, data);

        Template template = buildTemplateBasedOnType(data);

        return build(template, headerContext);
    }

    private Template buildTemplateBasedOnType(ClaimData data) {
        switch (data.getTemplateType()) {
            case STATUS_CHANGE:
                return claimManagementTemplateEngine.getTemplate(STATUS_CHANGED_TEMPLATE);
            case COMMENT:
                return claimManagementTemplateEngine.getTemplate(COMMENT_CHANGED_TEMPLATE);
            case TELEGRAM_FILE_CHANGE:
                return claimManagementTemplateEngine.getTemplate(TELEGRAM_FILE_CHANGE_TEMPLATE);
            case TELEGRAM_COMMENT_CHANGE:
                return claimManagementTemplateEngine.getTemplate(TELEGRAM_COMMENT_CHANGE_TEMPLATE);
            case TELEGRAM_IP_DOCUMENT_CHANGE:
                return claimManagementTemplateEngine.getTemplate(TELEGRAM_IP_CREATED_TEMPLATE);
            case TELEGRAM_LE_DOCUMENT_CHANGE:
                return claimManagementTemplateEngine.getTemplate(TELEGRAM_LE_CREATED_TEMPLATE);
            case TELEGRAM_ILE_DOCUMENT_CHANGE:
                return claimManagementTemplateEngine.getTemplate(TELEGRAM_ILE_CREATED_TEMPLATE);
            case TELEGRAM_NEW_CLAIM:
                return claimManagementTemplateEngine.getTemplate(TELEGRAM_NEW_CLAIM_TEMPLATE);
            default:
                throw new NotFoundException("templateType not found: " + data.getTemplateType());
        }
    }

    private String build(Template template, VelocityContext context) {
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }
}
