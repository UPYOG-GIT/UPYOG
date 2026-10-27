package org.egov.web.controller;

import org.egov.domain.service.NotificationService;
import org.egov.web.contract.EmailRequest;
import org.egov.web.contract.NotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class NotificationController {

    private NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/v1/sendNotificationToLabourDepartment")
    public NotificationResponse sendEmailNotification(@RequestBody EmailRequest emailRequest) {
        log.info("Sending notification to labour department");
        notificationService.sendEmailNotification(emailRequest.toDomain());
        return NotificationResponse.builder().responseInfo(null).successful(true).build();
    }
}
