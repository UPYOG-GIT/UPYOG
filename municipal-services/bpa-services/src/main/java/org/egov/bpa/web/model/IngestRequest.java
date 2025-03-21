
package org.egov.bpa.web.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;

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
public class IngestRequest {

    @Valid
    @NotNull
    @NotEmpty
    @JsonProperty("Data")
    private List<Data> ingestData;

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

}
