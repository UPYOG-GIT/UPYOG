package org.egov.bpa.web.model;

import java.time.LocalDateTime;
import java.util.List;

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
public class NdbResponseInfo {

	@JsonProperty("responseHash")
	private List<Integer> responseHash;

	@JsonProperty("Errors")
	private List<NdbErrorMap> errors;

	@JsonProperty("PushedDate")
	private LocalDateTime pushedDate;
}