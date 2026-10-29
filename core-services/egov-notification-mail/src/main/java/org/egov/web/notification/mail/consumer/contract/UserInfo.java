package org.egov.web.notification.mail.consumer.contract;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@ToString
public class UserInfo {

     private String tenantId;
      
     private String mobileNumber;

     private String role_code;
    
}
