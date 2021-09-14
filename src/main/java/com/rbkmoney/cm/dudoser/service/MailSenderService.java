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
import javax.mail.SendFailedException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

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
        } catch (MailException ex) {
            Boolean existSmtpAddressFailedException = Optional.of(ex)
                    .filter(e -> e instanceof org.springframework.mail.MailSendException)
                    .map(e -> (org.springframework.mail.MailSendException) e)
                    .stream()
                    .flatMap(e -> Arrays.stream(e.getMessageExceptions()))
                    .filter(e -> e instanceof SendFailedException)
                    .findAny()
                    .map(Throwable::getCause)
                    .map(e -> e instanceof SMTPAddressFailedException)
                    .orElse(false);
            if (existSmtpAddressFailedException) {
                log.error("Error with SMTP when send message to mail, should be ignored, " +
                                "partyId={}, claimId={}, email={}",
                        message.getPartyId(), message.getClaimId(), message.getTo(), ex);
                return true;
            }
            throw mailSendException(message, ex);
        } catch (MessagingException ex) {
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
