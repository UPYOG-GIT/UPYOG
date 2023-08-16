package org.entit.rga.calculator.web.models.landinfo;

import java.util.List;

import org.entit.rga.calculator.web.models.demand.TaxHeadEstimate;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class EstimatesAndSlabs {

	 @JsonProperty("estimates")
	    private List<TaxHeadEstimate> estimates;
}
