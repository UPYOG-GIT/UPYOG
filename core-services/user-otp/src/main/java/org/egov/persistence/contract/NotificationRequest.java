package org.egov.persistence.contract;

import static org.springframework.util.StringUtils.isEmpty;

import org.apache.commons.lang3.StringUtils;
import org.egov.web.contract.ApplicationDetails;
import org.egov.web.contract.UserInfo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class NotificationRequest {

    private UserInfo userInfo;

    private Email email;

    private ApplicationDetails applicationDetails;

    public void validate() {
        if (isTenantIdAbsent(userInfo.getTenantId())
                || validateRoleCode(userInfo.getRole_code())
                || isMobileNumberAbsent()
                || isMobileNumberNumeric()
                || isMobileNumberValidLength()) {
            throw new IllegalArgumentException("Invalid Notification Request");
        }
    }

    public boolean isTenantIdAbsent(String tenantId) {
        return isEmpty(tenantId);
    }

    public boolean validateRoleCode(String role_code) {
        return isEmpty(role_code) || !role_code.equals("HRMS_ADMIN");
    }

    public boolean isMobileNumberNumeric() {
        return !StringUtils.isNumeric(userInfo.getMobileNumber());
    }

    public boolean isMobileNumberValidLength() {
        return !(userInfo.getMobileNumber() != null && userInfo.getMobileNumber().matches("^[0-9]{10,13}$"));
    }

    public boolean isMobileNumberAbsent() {
        return isEmpty(userInfo.getMobileNumber());
    }
}
