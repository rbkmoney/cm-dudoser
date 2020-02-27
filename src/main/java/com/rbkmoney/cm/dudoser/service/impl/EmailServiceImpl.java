package com.rbkmoney.cm.dudoser.service.impl;

import com.rbkmoney.cm.dudoser.domain.Mail;
import com.rbkmoney.cm.dudoser.domain.MailDto;
import com.rbkmoney.cm.dudoser.exception.UploadException;
import com.rbkmoney.cm.dudoser.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public boolean sendMessage(Mail mail) {
        MailDto mailDto = mail.getMailDto();
        try {
            log.info("Trying to send message on email, partyId={}, claimId={}, email={}", mailDto.getPartyId(), mailDto.getClaimId(), mail.getTo());

            MimeMessage message = getMimeMessage(mail);

            mailSender.send(message);

            log.info("Message on email has been sent, partyId={}, claimId={}, email={}", mailDto.getPartyId(), mailDto.getClaimId(), mail.getTo());
            return true;
        } catch (MessagingException | MailException ex) {
            log.error("Received exception while sending message on email, partyId={}, claimId={}, email={}", mailDto.getPartyId(), mailDto.getClaimId(), mail.getTo(), ex);
            throw new UploadException("Failed to sendMessage", ex);
        }
    }

    private MimeMessage getMimeMessage(Mail mail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mail.getFrom());
        helper.setTo(mail.getTo());
        helper.setSubject(mail.getSubject());
        helper.setText(mail.getContent(), false);
        return message;
    }
}
