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
public class PayTpRateRequest {

	@JsonProperty("typeId")
	private int typeId = 0;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("unitId")
	private String unitId = null;

	@JsonProperty("calCon")
	private String calCon = null;

	@JsonProperty("calCact")
	private String calCact = null;

	@JsonProperty("pCategory")
	private String pCategory = null;

	@JsonProperty("bCategory")
	private String bCategory = null;

	@JsonProperty("sCategory")
	private String sCategory = null;

	@JsonProperty("rateRes")
	private String rateRes = null;

	@JsonProperty("rateComm")
	private String rateComm = null;

	@JsonProperty("rateInd")
	private String rateInd = null;

	@JsonProperty("createdBy")
	private String createdBy = null;

	@JsonProperty("perVal")
	private String perVal = null;

}
