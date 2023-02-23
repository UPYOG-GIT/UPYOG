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
	private int payTypeId;

	@JsonProperty("fromVal")
	private double fromVal;
	
	@JsonProperty("toVal")
	private double toVal;
	
	@JsonProperty("rateRes")
	private double rateRes;

	@JsonProperty("rateComm")
	private double rateComm;

	@JsonProperty("rateInd")
	private double rateInd;

	@JsonProperty("operation")
	private String operation = null;

	@JsonProperty("pCategory")
	private int pCategory;

	@JsonProperty("bCategory")
	private int bCategory;

	@JsonProperty("sCategory")
	private int sCategory;

	@JsonProperty("createdBy")
	private String createdBy = null;

	@JsonProperty("multpVal")
	private double multpVal;
	
	@JsonProperty("maxLimit")
	private double maxLimit;

}
