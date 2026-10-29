package org.egov.web.notification.mail.consumer.contract;
import static org.springframework.util.StringUtils.isEmpty;
import java.util.Map;
import lombok.*;

@AllArgsConstructor 
@NoArgsConstructor
@Builder
@Setter
@Getter
public class NotificationRequest {

    private UserInfo userInfo;

    private EmailInfo emailInfo;

    private ApplicationDetails applicationDetails;

    private Map<String, Object> templateModel;

    public void validate() {
        if (isTenantIdAbsent(userInfo.getTenantId())
                || validateRoleCode(userInfo.getRole_code())) {
            throw new IllegalArgumentException("Invalid Notification Request");
        }
    }

    public boolean isTenantIdAbsent(String tenantId) {
        return isEmpty(tenantId);
    }

    public boolean validateRoleCode(String role_code) {
        return isEmpty(role_code) || !role_code.equals("HRMS_ADMIN");
    }

}