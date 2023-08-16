package org.entit.rga.calculator.web.models;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.entit.rga.calculator.web.models.demand.TaxHeadEstimate;
import org.entit.rga.calculator.web.models.rga.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Calculation {


	@JsonProperty("applicationNumber")
	private String applicationNumber = null;

	@JsonProperty("RGA")
	private RGA rga = null;

	@NotNull
	@JsonProperty("tenantId")
	@Size(min = 2, max = 256)
	private String tenantId = null;

	@JsonProperty("taxHeadEstimates")
	List<TaxHeadEstimate> taxHeadEstimates;
	
	@JsonProperty("feeType")
	String feeType = null;
}
