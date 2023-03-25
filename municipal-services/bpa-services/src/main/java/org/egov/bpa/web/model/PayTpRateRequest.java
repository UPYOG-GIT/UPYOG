package org.egov.bpa.web.model;

import java.util.List;

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

	@JsonProperty("ids")
	private List<Integer> ids;
	
	@JsonProperty("typeId")
	private int typeId;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("unitId")
	private String unitId = null;

	@JsonProperty("calCon")
	private String calCon = null;

	@JsonProperty("calCact")
	private String calCact = null;

	@JsonProperty("pCategory")
	private int pCategory;

	@JsonProperty("bCategory")
	private int bCategory;

	@JsonProperty("sCategory")
	private int sCategory;

	@JsonProperty("rateRes")
	private double rateRes;

	@JsonProperty("rateComm")
	private double rateComm;

	@JsonProperty("rateInd")
	private double rateInd;

	@JsonProperty("createdBy")
	private String createdBy = null;

	@JsonProperty("perVal")
	private int perVal;

}
