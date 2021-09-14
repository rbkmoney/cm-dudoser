package com.rbkmoney.cm.dudoser.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.rbkmoney.cm.dudoser.CMDudoserApplication;
import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.exception.MailSendException;
import com.sun.mail.smtp.SMTPAddressFailedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.BodyPart;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    // отправляем сообщение на фейковый почтовой сервер
    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

    @Autowired
    private MailSenderService mailSenderService;
    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void sendingMessageFlowTest() throws Exception {
        Message message = createMessage();

        mailSenderService.send(message);

        MimeMessage[] receivedMessages = smtpServerRule.getMessages();

        assertEquals(1, receivedMessages.length);

        MimeMessage receivedMessage = receivedMessages[0];

        assertEquals(message.getSubject(), receivedMessage.getSubject());
        assertEquals(message.getTo(), receivedMessage.getAllRecipients()[0].toString());
        assertTrue(
                getTextFromMimeMultipart((MimeMultipart) receivedMessage.getContent()).contains(message.getContent()));
    }

    @Test(expected = MailSendException.class)
    public void incorrectAddressTest() {
        Message message = createMessage();
        message.setTo("asd, asd");

        mailSenderService.send(message);
    }

    @Test(expected = MailSendException.class)
    public void connectionRefusedTest() {
        JavaMailSenderImpl sender = getJavaMailSender(2524);

        Message message = createMessage();

        MailSenderService senderService = new MailSenderService(sender);
        senderService.send(message);
    }

    @Test
    public void shouldHandleSmtpAddressFailedException() {
        JavaMailSenderImpl sender = Mockito.mock(JavaMailSenderImpl.class);
        Mockito.when(sender.createMimeMessage())
                .thenReturn(mailSender.createMimeMessage());
        Mockito.doAnswer(
                invocation -> {
                    var map = new HashMap<Object, Exception>();
                    map.put(
                            "asd",
                            new SendFailedException(
                                    "asd",
                                    new SMTPAddressFailedException(new InternetAddress(), "asd", 1, "asd")));
                    throw new org.springframework.mail.MailSendException(map);
                })
                .when(sender)
                .send(any(MimeMessage.class));
        Message message = createMessage();
        MailSenderService senderService = new MailSenderService(sender);
        senderService.send(message);
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

    private Message createMessage() {
        return Message.builder()
                .from("no-reply@rbk.com")
                .to("info@rbk.com")
                .subject("Spring Mail Integration Testing with JUnit and GreenMail Example")
                .content("<span style=\"color: #2d2d2d; font-style: italic\">" +
                        "We show how to write Integration Tests using Spring and GreenMail.</span>")
                .build();
    }

    private JavaMailSenderImpl getJavaMailSender(int port) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("localhost");
        sender.setPort(port);
        sender.setUsername("username");
        sender.setPassword("secret");
        return sender;
    }

    public static class SmtpServerRule extends ExternalResource {

        private GreenMail smtpServer;
        private final int port;

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
