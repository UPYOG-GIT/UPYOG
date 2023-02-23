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
	private double fromVal = 0d;
	
	@JsonProperty("toVal")
	private double toVal = 0d;
	
	@JsonProperty("rateRes")
	private double rateRes = 0d;

	@JsonProperty("rateComm")
	private double rateComm = 0d;

	@JsonProperty("rateInd")
	private double rateInd = 0d;

	@JsonProperty("operation")
	private String operation = null;

	@JsonProperty("pCategory")
	private int pCategory = 0;

	@JsonProperty("bCategory")
	private int bCategory = 0;

	@JsonProperty("sCategory")
	private int sCategory = 0;

	@JsonProperty("createdBy")
	private String createdBy = null;

	@JsonProperty("multpVal")
	private double multpVal = 0d;
	
	@JsonProperty("maxLimit")
	private double maxLimit = 0d;

}
