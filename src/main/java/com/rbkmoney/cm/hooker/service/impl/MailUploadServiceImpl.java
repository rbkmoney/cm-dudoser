package com.rbkmoney.cm.hooker.service.impl;

import com.rbkmoney.cm.hooker.domain.AssistantUploadData;
import com.rbkmoney.cm.hooker.exception.UploadException;
import com.rbkmoney.cm.hooker.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailUploadServiceImpl implements UploadService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    @Override
    public boolean upload(AssistantUploadData assistantData, String data) {
        try {
            log.info(
                    "Trying to upload data by email, partyId={}, claimId={}, revision={}, updatedAt={}, userId={}, username={}, email={}, userType={}",
                    assistantData.getPartyId(), assistantData.getClaimId(), assistantData.getRevision(), assistantData.getUpdatedAt(),
                    assistantData.getUserId(), assistantData.getUserName(), assistantData.getTargetAddress(), assistantData.getUserType()
            );

            MimeMessage message = getMimeMessage(assistantData.getTargetAddress(), data);

            mailSender.send(message);

            log.info(
                    "Data has been upload by email, partyId={}, claimId={}, revision={}, updatedAt={}, userId={}, username={}, email={}, userType={}",
                    assistantData.getPartyId(), assistantData.getClaimId(), assistantData.getRevision(), assistantData.getUpdatedAt(),
                    assistantData.getUserId(), assistantData.getUserName(), assistantData.getTargetAddress(), assistantData.getUserType()
            );
            return true;
        } catch (MessagingException | MailException ex) {
            log.error(
                    "Received exception while uploading data by email, partyId={}, claimId={}, revision={}, updatedAt={}, userId={}, username={}, email={}, userType={}",
                    assistantData.getPartyId(), assistantData.getClaimId(), assistantData.getRevision(), assistantData.getUpdatedAt(),
                    assistantData.getUserId(), assistantData.getUserName(), assistantData.getTargetAddress(), assistantData.getUserType(),
                    ex
            );
            throw new UploadException("Failed to upload data by email", ex);
        }
    }

    private MimeMessage getMimeMessage(String mailTo, String data) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject("Изменение статуса вашей заявки на подключение к RBK.money");
        helper.setFrom(mailFrom);
        helper.setTo(mailTo);
        helper.setText(data, false);
        return message;
    }
}
