package org.entit.rga.web.model;

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
public class RGAPenaltyRequest {

	@JsonProperty("ids")
	private List<Integer> ids;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("fromVal")
	private double fromVal;
	
	@JsonProperty("toVal")
	private double toVal;

	@JsonProperty("createdBy")
	private String createdBy = null;

	@JsonProperty("occupancyType")
	private String occupancyType = null;

	@JsonProperty("multipyPenalty")
	private String multipyPenalty = null;

}
