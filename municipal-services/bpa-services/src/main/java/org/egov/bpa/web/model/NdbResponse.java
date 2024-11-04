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
public class NdbResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("responseHash")
	private List<Integer> responseHash;

	@JsonProperty("Errors")
	private List<NdbErrorMap> errors;

	@JsonProperty("errorMessage")
	private String errorMessage;

	@JsonProperty("Date")
	private LocalDate date;

	@JsonProperty("environment")
	String environment;

	@JsonProperty("messageDescription")
	String messageDescription;

	@JsonProperty("noOfRecordsPushed")
	private int noOfRecordsPushed = 0;
	
	@JsonProperty("totalNoOfApplications")
	private int totalNoOfApplications = 0;
	
	@JsonProperty("totalApprovedApplications")
	private int totalApprovedApplications = 0;
}
