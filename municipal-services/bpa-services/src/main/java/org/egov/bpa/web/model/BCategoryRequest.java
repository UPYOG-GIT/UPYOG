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
public class BCategoryRequest {

	@JsonProperty("id")
	private int id;
	
	@JsonProperty("tenantId")
	private String tenantId=null;

	@JsonProperty("desc")
	private String desc = null;
	
	@JsonProperty("createdBy")
	private String createdBy = null;
	
	@JsonProperty("updatedBy")
	private String updatedBy = null;
	
	@JsonProperty("defunt")
	private String defunt = null;
	
	
	
	
}
