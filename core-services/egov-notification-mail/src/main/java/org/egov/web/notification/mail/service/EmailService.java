package org.egov.web.notification.mail.service;

import org.egov.web.notification.mail.consumer.contract.Email;
import org.egov.web.notification.mail.consumer.contract.NotificationRequest;

public interface EmailService {
    void sendEmail(Email email);
    void sendEmailInfo(NotificationRequest notificationRequest);
}
