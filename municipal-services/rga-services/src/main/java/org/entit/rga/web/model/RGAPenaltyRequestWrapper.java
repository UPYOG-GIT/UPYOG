package org.entit.rga.web.model;

import org.egov.common.contract.request.RequestInfo;

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
public class RGAPenaltyRequestWrapper {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;
	
	@JsonProperty("RGAPenaltyRequest")
	private RGAPenaltyRequest rgaPenaltyRequest;
	
	
}
