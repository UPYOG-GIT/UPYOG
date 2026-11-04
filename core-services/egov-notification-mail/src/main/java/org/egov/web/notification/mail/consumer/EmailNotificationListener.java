package org.egov.web.notification.mail.consumer;

import java.util.HashMap;

import org.egov.web.notification.mail.consumer.contract.EmailRequest;
import org.egov.web.notification.mail.consumer.contract.NotificationRequest;
import org.egov.web.notification.mail.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailNotificationListener {

    private EmailService emailService;
    private ObjectMapper objectMapper;

    @Autowired
    public EmailNotificationListener(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topics.notification.mail.name}")
    public void listen(final HashMap<String, Object> record) {
        EmailRequest emailRequest = objectMapper.convertValue(record, EmailRequest.class);
        emailService.sendEmail(emailRequest.getEmail());

    }

    @KafkaListener(topics = "${kafka.topics.notification.mail.listener}")
    public void listenProcess(final HashMap<String, Object> record) {

        try {
            log.info("START: Email notification processing started for record: " + record);
            NotificationRequest notificationRequest = null;
            notificationRequest = objectMapper.convertValue(record, NotificationRequest.class);
            if (notificationRequest == null || notificationRequest.getEmailInfo() == null) {
                throw new IllegalArgumentException("Invalid Email Request");
            } else {
                notificationRequest.validate();
                emailService.sendEmailInfo(notificationRequest);
                log.info("END: Email notification processing completed for record: ");
            }
        } catch (Exception e) {
            log.error("Email service failed");
            e.printStackTrace();
        }
    }

}
