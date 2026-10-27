package org.egov.domain.service;

import org.egov.persistence.contract.NotificationRequest;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

     private CustomKafkaTemplate<String, NotificationRequest> kafkaTemplate;

     private String mailNotificationTopic;

     public NotificationService(CustomKafkaTemplate<String, NotificationRequest> kafkaTemplate,
               @Value("${mail.notification.topic}") String mailNotificationTopic) {
          this.kafkaTemplate = kafkaTemplate;
          this.mailNotificationTopic = mailNotificationTopic;
     }

     public void sendEmailNotification(NotificationRequest notificationRequest) {
          log.info("Inside Notification Service");
          notificationRequest.validate();
          log.info("Sending notification to kafka topic: " + notificationRequest.toString());
          kafkaTemplate.send(mailNotificationTopic, notificationRequest);
     }
}
