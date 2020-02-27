package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.exception.MailSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderService {

    private final JavaMailSender mailSender;

    public boolean send(Message message) {
        try {
            log.info("Trying to send mail message, partyId={}, claimId={}, email={}", message.getPartyId(), message.getClaimId(), message.getTo());

            MimeMessage mimeMessage = getMimeMessage(message);

            mailSender.send(mimeMessage);

            log.info("Mail message has been sent, partyId={}, claimId={}, email={}", message.getPartyId(), message.getClaimId(), message.getTo());
            return true;
        } catch (MessagingException | MailException ex) {
            log.error("Received exception while sending mail message, partyId={}, claimId={}, email={}", message.getPartyId(), message.getClaimId(), message.getTo(), ex);
            throw new MailSendException("Failed to send", ex);
        }
    }

    private MimeMessage getMimeMessage(Message message) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        helper.setFrom(message.getFrom());
        helper.setTo(message.getTo());
        helper.setSubject(message.getSubject());
        helper.setText(message.getContent(), false);
        return mimeMessage;
    }
}
