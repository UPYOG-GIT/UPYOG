package org.egov.web.notification.mail.consumer.contract;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Configuration
@ToString
public class EmailInfo {

    @Value("${receiver.emailId}")
    private String emailTo;
    private MailRequestType emailRequestType;
    private MailSubjectType emailSubjectType;
    
}

