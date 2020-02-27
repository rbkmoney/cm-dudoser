package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.exception.NotFoundException;
import com.rbkmoney.cm.dudoser.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component
@RequiredArgsConstructor
public class ClaimTemplateServiceImpl implements TemplateService {

    private final VelocityEngine claimManagementTemplateEngine;

    @Override
    public String process(ClaimData data) {
        VelocityContext headerContext = new VelocityContext();
        headerContext.put("claimManagementData", data);

        Template template = buildTemplate(data);

        return build(template, headerContext);
    }

    private Template buildTemplate(ClaimData data) {
        switch (data.getTemplateType()) {
            case STATUSCHANGE:
                return claimManagementTemplateEngine.getTemplate("vm/StatusChangedEntity.vm");
            case COMMENT:
                return claimManagementTemplateEngine.getTemplate("vm/CommentChangedEntity.vm");
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
