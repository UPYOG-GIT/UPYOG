package org.egov.web.notification.mail.controller;

import org.egov.web.notification.mail.consumer.contract.ApplicationDetails;
import org.egov.web.notification.mail.consumer.contract.EmailInfo;
import org.egov.web.notification.mail.consumer.contract.MailRequestType;
import org.egov.web.notification.mail.consumer.contract.MailSubjectType;
import org.egov.web.notification.mail.consumer.contract.NotificationRequest;
import org.egov.web.notification.mail.consumer.contract.UserInfo;
import org.egov.web.notification.mail.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/mail/test")
public class TestEmailController {

    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public String sendTestEmail() {
        UserInfo userInfo = new UserInfo("cg.birgaon", "6464646473", "HRMS_ADMIN");
        EmailInfo emailInfo = new EmailInfo(
                "dev4entitcs@gmail.com",
                MailRequestType.SEND_MAIL_TO_EXTERNAL_ORGANIZATION,
                MailSubjectType.MAIL_FOR_LABOUR_CESS_PAYMENT_DETAILS);
        ApplicationDetails applicationDetails = new ApplicationDetails(
                "28-10-26",
                "abc",
                "Birgaon",
                "cg.birgaon",
                "CG-04-BP-28-10-2026",
                567.0,
                2000.0,
                "25675656767",
                null,
                2000.0,
                "28-10-26",
                "Approved",
                "2453",
                2000.0,
                "cg.birgaon",
                "Birgaon");
        NotificationRequest notificationRequest = new NotificationRequest(
                userInfo, emailInfo, applicationDetails, null);

        try {
            emailService.sendEmailInfo(notificationRequest);
            return "Email sent successfully";
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Email dispatch failed", exception);
        }
    }
}
