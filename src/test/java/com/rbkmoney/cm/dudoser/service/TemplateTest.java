package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.cm.dudoser.domain.ClaimData;
import com.rbkmoney.cm.dudoser.domain.ClaimStatus;
import com.rbkmoney.cm.dudoser.domain.TemplateType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CMDudoserApplication.class, webEnvironment = RANDOM_PORT)
public class TemplateTest {

    private static final String COMMENT_TEMPLATE = "ответ";
    private static final String STATUS_TEMPLATE = "изменен";

    @Autowired
    private TemplateService templateService;

    @Test
    public void velocityTemplateTest() throws Exception {
        ClaimData claimData = ClaimData.builder()
                .id("12e")
                .templateType(TemplateType.STATUSCHANGE)
                .status(ClaimStatus.ACCEPTED.getCyrillicValue())
                .build();
        String process = templateService.process(claimData);
        assertTrue(process.contains(STATUS_TEMPLATE));

        claimData = ClaimData.builder()
                .id("12e")
                .templateType(TemplateType.COMMENT)
                .comment("я дед инсайд")
                .build();
        process = templateService.process(claimData);
        assertTrue(process.contains(COMMENT_TEMPLATE));
    }
}
