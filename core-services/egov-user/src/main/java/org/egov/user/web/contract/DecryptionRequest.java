package org.egov.user.web.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.model.User;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class DecryptionRequest {
    
    private String encryptedSwsUser;

    

}


