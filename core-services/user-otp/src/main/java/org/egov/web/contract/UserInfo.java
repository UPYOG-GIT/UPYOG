package org.egov.web.contract;

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
