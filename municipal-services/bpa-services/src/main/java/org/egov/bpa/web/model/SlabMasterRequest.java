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
public class SlabMasterRequest {

	@JsonProperty("tenantId")
	private String tenantId = null;
	
	@JsonProperty("payTypeId")
	private String payTypeId = null;

	@JsonProperty("fromVal")
	private String fromVal = null;
	
	@JsonProperty("toVal")
	private String toVal = null;
	
	@JsonProperty("rateRes")
	private String rateRes = null;

	@JsonProperty("rateComm")
	private String rateComm = null;

	@JsonProperty("rateInd")
	private String rateInd = null;

	@JsonProperty("operation")
	private String operation = null;

	@JsonProperty("pCategory")
	private String pCategory = null;

	@JsonProperty("bCategory")
	private String bCategory = null;

	@JsonProperty("sCategory")
	private String sCategory = null;

	@JsonProperty("createdBy")
	private String createdBy = null;

	@JsonProperty("multpVal")
	private String multpVal = null;
	
	@JsonProperty("maxLimit")
	private String maxLimit = null;

}
