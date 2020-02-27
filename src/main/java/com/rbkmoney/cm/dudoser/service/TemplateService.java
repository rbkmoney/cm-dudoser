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

    private final VelocityEngine claimManagementTemplateEngine;

    public String process(ClaimData data) {
        VelocityContext headerContext = new VelocityContext();
        headerContext.put(TITLE, data);

        Template template = buildTemplate(data);

        return build(template, headerContext);
    }

    private Template buildTemplate(ClaimData data) {
        switch (data.getTemplateType()) {
            case STATUSCHANGE:
                return claimManagementTemplateEngine.getTemplate(STATUS_CHANGED_TEMPLATE);
            case COMMENT:
                return claimManagementTemplateEngine.getTemplate(COMMENT_CHANGED_TEMPLATE);
            default:
                throw new NotFoundException("templateType not found");
        }
    }

    private String build(Template template, VelocityContext context) {
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }
}
