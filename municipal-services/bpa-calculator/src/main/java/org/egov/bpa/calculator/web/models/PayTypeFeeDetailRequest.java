package org.egov.bpa.calculator.web.models;

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

	@JsonProperty("id")
    private int id;
	
	@JsonProperty("payTypeId")
	private int payTypeId=0;
	
	@JsonProperty("feeType")
	private String feeType = null;
	
	@JsonProperty("tenantId")
	private String tenantId=null;
	
	@JsonProperty("billId")
	private String billId=null;
	
	@JsonProperty("applicationNo")
	private String applicationNo = null;
	
	@JsonProperty("unit")
	private String unit = null;
	
	@JsonProperty("chargesTypeName")
	private String chargesTypeName = null;
	
	@JsonProperty("propPlotArea")
	private double propPlotArea;
	
	@JsonProperty("amount")
	private double amount;
	
	@JsonProperty("rate")
    private double rate;
	
	@JsonProperty("type")
	private String type = null;
		
	@JsonProperty("createdBy")
	private String createdBy = null;
	
	@JsonProperty("updatedBy")
    private String updatedBy = null;
	
}
