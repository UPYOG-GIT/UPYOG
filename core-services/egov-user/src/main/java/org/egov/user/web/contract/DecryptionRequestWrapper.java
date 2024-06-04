package org.egov.user.web.contract;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class DecryptionRequestWrapper {
    private RequestInfo requestInfo;

    @NotNull
    @Valid
    private DecryptionRequest decryptionRequest;

}


