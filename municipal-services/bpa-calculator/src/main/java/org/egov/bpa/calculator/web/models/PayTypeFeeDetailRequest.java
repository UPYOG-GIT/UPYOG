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

    private int id;
	
	private int payTypeId=0;
	
	private String feeType = null;
	
	private String tenantId=null;
	
	private String billId=null;
	
	private String applicationNo = null;
	
	private String unit = null;
	
	private String chargesTypeName = null;
	
	private double propPlotArea;
	
	@JsonProperty("amount")
	private double amount;
	
    private double rate;
	
	private String type = null;
		
	private int srNo;
    
	
}
