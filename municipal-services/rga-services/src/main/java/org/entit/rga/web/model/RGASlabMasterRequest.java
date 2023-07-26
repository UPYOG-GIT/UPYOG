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
public class RGASlabMasterRequest {

	@JsonProperty("ids")
	private List<Integer> ids;
	
	@JsonProperty("tenantId")
	private String tenantId = null;
	
	@JsonProperty("fromVal")
	private double fromVal;
	
	@JsonProperty("toVal")
	private double toVal;
	
	@JsonProperty("rate")
	private double rate;

	@JsonProperty("occupancyType")
	private String occupancyType = null;

	@JsonProperty("pCategory")
	private int pCategory;

	@JsonProperty("bCategory")
	private int bCategory;

	@JsonProperty("sCategory")
	private int sCategory;

	@JsonProperty("createdBy")
	private String createdBy = null;

}
