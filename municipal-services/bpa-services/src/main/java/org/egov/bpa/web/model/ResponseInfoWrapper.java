package org.egov.bpa.web.model;

import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResponseInfoWrapper {
	@JsonProperty("access_token")
	String accessToken;
	
	@JsonProperty("token_type")
	String tokenType;
	
	@JsonProperty("refresh_token")
	String refreshToken;
	
	@JsonProperty("expires_in")
	String expiresIn;
	
	@JsonProperty("scope")
	String scope;
	
	@JsonProperty("ResponseInfo")
	ResponseInfo responseInfo;
	
	@JsonProperty("UserRequest")
	User userRequest;
}
