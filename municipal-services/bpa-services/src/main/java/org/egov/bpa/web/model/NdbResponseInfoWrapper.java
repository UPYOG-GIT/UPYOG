package org.egov.bpa.web.model;

import java.time.LocalDate;
import java.util.List;

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

	@JsonProperty("NdbResponseInfo")
	NdbResponseInfo ndbResponseInfo;

//	@JsonProperty("responseHash")
//	private List<Integer> responseHash;
//
//	@JsonProperty("Errors")
//	private List<NdbErrorMap> errors;
//
//	@JsonProperty("Date")
//	private LocalDate date;
//
//	@JsonProperty("environment")
//	String environment;
}
