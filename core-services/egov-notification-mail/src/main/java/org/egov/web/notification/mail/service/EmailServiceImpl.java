package org.egov.web.notification.mail.service;

import javax.mail.internet.MimeMessage;

import org.egov.web.notification.mail.consumer.contract.Email;
import org.egov.web.notification.mail.consumer.contract.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;


@Service 
@RequiredArgsConstructor 
@Slf4j 
@Primary 
public class EmailServiceImpl  implements EmailService {
    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig; // FreeMarker Configuration

    
    @Getter
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendEmail(Email email) {
        // Implement the logic to send email here
        System.out.println("Sending email to: " );
    }

    @Override
    public void sendEmailInfo(NotificationRequest notificationRequest) {

        Map<String, Object> templateModel = notificationRequest.getTemplateModel();
        templateModel.put("proposalNo", notificationRequest.getApplicationDetails().getProposalNo());
        templateModel.put("karyType", notificationRequest.getApplicationDetails().getKaryType());
        templateModel.put("sthapnaPata", notificationRequest.getApplicationDetails().getSthapnaPata());
        templateModel.put("constructionArea", notificationRequest.getApplicationDetails().getConstructionArea());
        templateModel.put("ulbName", notificationRequest.getApplicationDetails().getUlbName());
        templateModel.put("ulbCode", notificationRequest.getApplicationDetails().getUlbCode());
        templateModel.put("estimatedCess", notificationRequest.getApplicationDetails().getEstimatedCess());
        templateModel.put("cessAmount", notificationRequest.getApplicationDetails().getCessAmount());
        templateModel.put("bankAmount", notificationRequest.getApplicationDetails().getBankAmount());
        templateModel.put("transactionId", notificationRequest.getApplicationDetails().getTransactionId());
        templateModel.put("transactionRefNo", notificationRequest.getApplicationDetails().getTransactionRefNo());
        templateModel.put("transactionDate", notificationRequest.getApplicationDetails().getTransactionDate());
        templateModel.put("receiptNo", notificationRequest.getApplicationDetails().getReceiptNo());
        templateModel.put("status", notificationRequest.getApplicationDetails().getStatus());

        // Implement the logic to send email here
       log.info("Sending email to: " + notificationRequest.getEmailInfo().getEmailTo());
        try {
            // 1. Load FTL template and process model map
            String templateFileName =  "labour-department-payment-detail-mail.ftl";
            Template template = freemarkerConfig.getTemplate(templateFileName);
            
            String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
                    template, templateModel
                    // notificationRequest.getTemplateModel()
            );

            // 2. Build and dispatch email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(notificationRequest.getEmailInfo().getEmailTo());
            if(notificationRequest.getEmailInfo().getEmailSubjectType().equals("MAIL_FOR_LABOUR_CESS_PAYMENT_DETAILS")) {
              helper.setSubject("Hi IM Deeksha ");
            }
            
            helper.setText(htmlContent, true); // Send as HTML

            mailSender.send(message);
            log.info("FTL Templated email [{}] successfully sent to: {}", templateFileName, notificationRequest.getEmailInfo().getEmailTo());

        } catch (Exception e) {
            log.error("Failed to send FTL templated email to {}", notificationRequest.getEmailInfo().getEmailTo(), e);
            throw new RuntimeException("Email dispatch failed", e);
        }
    }

}
