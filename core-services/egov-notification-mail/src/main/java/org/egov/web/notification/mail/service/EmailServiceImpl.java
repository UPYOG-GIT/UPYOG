package org.egov.web.notification.mail.service;

import javax.mail.internet.MimeMessage;

import org.egov.web.notification.mail.config.EmailProperties;
import org.egov.web.notification.mail.consumer.contract.Email;
import org.egov.web.notification.mail.consumer.contract.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.HashMap;
import java.util.Map;


@Service 
@RequiredArgsConstructor 
@Slf4j 
@Primary 
public class EmailServiceImpl  implements EmailService {
    @Autowired 
    private JavaMailSender mailSender;

    @Autowired 
    private Configuration freemarkerConfig;
    
    @Autowired
    private EmailProperties emailProperties;
    

    @Getter
    @Value("${spring.mail.username}")
    private String senderEmail;
    @Getter 
    @Value ("${spring.mail.password}")
    private String springMailPassword;
    
    @Getter 
    @Value("${spring.mail.port}")
    private int springmailport;
    @Getter 
    @Value ("${spring.mail.protocol}")
    private String springmailprotocol;
    @Getter 
    @Value ("${spring.mail.properties.mail.smtp.auth}")
    private String springmailpropertiesmailsmtpauth;

    @Getter 
    @Value ("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String springmailpropertiesmailsmtpstarttlsenable;
    @Getter 
    @Value ("${spring.mail.properties.mail.smtp.starttls.required}")
    private String mailSmtpStarttlsRequired;
    @Getter 
    @Value ("${spring.mail.receiver.username}")
    private String mailReceiverUsername;


    

    public void sendEmail(Email email) {
        // Implement the logic to send email here
        System.out.println("Sending email to: " );
    }

    @Override
    public void sendEmailInfo(NotificationRequest notificationRequest) {

        Map<String, Object> templateModel = new HashMap<>();
        if (notificationRequest.getTemplateModel() != null) {
            templateModel.putAll(notificationRequest.getTemplateModel());
        }
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
        
          if(templateModel.isEmpty()) {
            log.error("Template model is null or empty for email notification");
            throw new IllegalArgumentException("Template model is null or empty for email notification");
          }
        
        // Implement the logic to send email here
       log.info("Sending email to: " + notificationRequest.getEmailInfo().getEmailTo());

       log.info("gettiong mail details " + getSenderEmail() + getSpringMailPassword() + getSpringmailport() + getSpringmailprotocol() + getSpringmailpropertiesmailsmtpauth() + getSpringmailpropertiesmailsmtpstarttlsenable() + getMailSmtpStarttlsRequired() + getMailReceiverUsername());
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
            log.info("Sending email to: " + notificationRequest.getEmailInfo().getEmailTo());

            helper.setFrom(emailProperties.getMailSenderUsername());
            helper.setTo(notificationRequest.getEmailInfo().getEmailTo());
            
            if(notificationRequest.getEmailInfo().getEmailSubjectType().equals("MAIL_FOR_LABOUR_CESS_PAYMENT_DETAILS")) {
            log.info("Setting subject for MAIL_FOR_LABOUR_CESS_PAYMENT_DETAILS");
              helper.setSubject("Hi IM Deeksha");
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
