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
public class PayTypeFeeDetailRequest {

	@JsonProperty("payTypeId")
	private int payTypeId=0;
	
	@JsonProperty("tenantId")
	private String tenantId=null;
	
	@JsonProperty("billId")
	private String billId=null;
	
	@JsonProperty("applicationNo")
	private String applicationNo = null;
	
	@JsonProperty("unitId")
	private String unitId = null;
	
	@JsonProperty("payId")
	private String payId = null;
	
	@JsonProperty("chargesTypeName")
	private String chargesTypeName = null;
	
	@JsonProperty("amount")
	private String amount = null;
	
	@JsonProperty("statusType")
	private String statusType = null;
	
	@JsonProperty("propValue")
	private String propValue = null;
	
	@JsonProperty("value")
	private String value = null;
	
	@JsonProperty("status")
	private String status = null;
	
	@JsonProperty("createdBy")
	private String createdBy = null;
	
	@JsonProperty("paymentType")
	private String paymentType = null;
	
	
}
