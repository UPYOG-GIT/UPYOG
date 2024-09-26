package org.egov.bpa.web.model;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NdbResponseInfoWrapper {

	
	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;
	
	@JsonProperty("NDB")
	private NdbResponseInfo ndbResponseInfo;
	
//	@JsonProperty("responseHash")
//	private List<Integer> responseHash;
//	
//	@JsonProperty("Errors")
//	private List<NdbErrorMap> errors;
//	
//	@JsonProperty("PushedDate")
//	private LocalDateTime pushedDate;
}
