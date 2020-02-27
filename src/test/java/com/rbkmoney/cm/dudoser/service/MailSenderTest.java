package com.rbkmoney.cm.dudoser.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.cm.dudoser.domain.Message;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CMDudoserApplication.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {
        "mail.host=localhost",
        "mail.port=2525",
        "mail.username=username",
        "mail.password=secret",
})
public class MailSenderTest {

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

    @Autowired
    private MailSenderService emailService;

    @Test
    public void test() throws Exception {
        Message message = Message.builder()
                .from("no-reply@rbk.com")
                .to("info@rbk.com")
                .subject("Spring Mail Integration Testing with JUnit and GreenMail Example")
                .content("We show how to write Integration Tests using Spring and GreenMail.")
                .build();

        emailService.send(message);

        MimeMessage[] receivedMessages = smtpServerRule.getMessages();

        assertEquals(1, receivedMessages.length);

        MimeMessage receivedMessage = receivedMessages[0];

        assertEquals(message.getSubject(), receivedMessage.getSubject());
        assertEquals(message.getTo(), receivedMessage.getAllRecipients()[0].toString());
        assertTrue(getTextFromMimeMultipart((MimeMultipart) receivedMessage.getContent()).contains(message.getContent()));
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        String result = "";
        int partCount = mimeMultipart.getCount();
        for (int i = 0; i < partCount; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                // result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
                result = html;
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    public static class SmtpServerRule extends ExternalResource {

        private GreenMail smtpServer;
        private int port;

        public SmtpServerRule(int port) {
            this.port = port;
        }

        @Override
        protected void before() throws Throwable {
            super.before();
            smtpServer = new GreenMail(new ServerSetup(port, null, "smtp"));
            smtpServer.start();
        }

        public MimeMessage[] getMessages() {
            return smtpServer.getReceivedMessages();
        }

        @Override
        protected void after() {
            super.after();
            smtpServer.stop();
        }
    }
}
