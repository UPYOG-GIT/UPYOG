package org.egov.bpa.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PayTypeRequest {

	@JsonProperty("tenantId")
	private String tenantId=null;

	@JsonProperty("chargesTypeName")
	private String chargesTypeName = null;
	
	@JsonProperty("paymentType")
	private String paymentType = null;

	@JsonProperty("createdBy")
	private String createdBy = null;
	
	@JsonProperty("updatedBy")
	private String updatedBy = null;
	
	@JsonProperty("defunt")
	private String defunt = null;
	
	@JsonProperty("optflag")
	private String optFlag = null;
	
	@JsonProperty("hrnh")
	private String hrnh = null;
	
	
	
	
}
