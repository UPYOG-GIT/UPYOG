package org.egov.web.contract;

import static org.springframework.util.StringUtils.isEmpty;

import org.egov.domain.model.MailRequestType;
import org.egov.domain.model.MailSubjectType;
import org.egov.persistence.contract.Email;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class EmailRequest {

    private RequestInfo requestInfo;

    private UserInfo userInfo;

    private Email email;

    private ApplicationDetails applicationDetails;

    public org.egov.persistence.contract.NotificationRequest toDomain() {
        return org.egov.persistence.contract.NotificationRequest.builder()
                .userInfo(userInfo)
                .applicationDetails(applicationDetails)
                .email(email)
                .build();
    }
}
