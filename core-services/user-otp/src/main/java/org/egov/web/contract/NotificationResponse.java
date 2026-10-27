package org.egov.web.contract;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class NotificationResponse {
    
    private ResponseInfo responseInfo;
    
    @JsonProperty("isSuccessful")
    private boolean successful;

}
