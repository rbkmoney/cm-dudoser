package com.rbkmoney.cm.dudoser.service;

import com.rbkmoney.cm.dudoser.domain.Message;
import com.rbkmoney.cm.dudoser.exception.MailSendException;
import com.sun.mail.smtp.SMTPAddressFailedException;
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
            log.info("Trying to send message to mail, partyId={}, claimId={}, email={}",
                    message.getPartyId(), message.getClaimId(), message.getTo());
            MimeMessage mimeMessage = getMimeMessage(message);
            mailSender.send(mimeMessage);
            return true;
        } catch (org.springframework.mail.MailSendException ex) {
            if (ex.getCause() instanceof SMTPAddressFailedException) {
                log.error("Exception during send message to mail", ex);
                return true;
            }
            throw mailSendException(message, ex);
        } catch (MessagingException | MailException ex) {
            throw mailSendException(message, ex);
        }
    }

    private MimeMessage getMimeMessage(Message message) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        helper.setFrom(message.getFrom());
        helper.setTo(message.getTo());
        helper.setSubject(message.getSubject());
        helper.setText(message.getContent(), true);
        return mimeMessage;
    }

    private MailSendException mailSendException(Message message, Exception ex) {
        return new MailSendException(
                String.format("Received exception while sending message to mail, partyId=%s, claimId=%s, email=%s",
                        message.getPartyId(), message.getClaimId(), message.getTo()), ex);
    }
}
