package org.egov.bpa.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ProposalDetails {

    @JsonProperty(received)
    private Long received;

    @JsonProperty("resolved")
    private Long resolved;

    @JsonProperty("pending")
    private Long pending;

    @JsonProperty("rejected")
    private Long rejected;

    @JsonProperty("resolvedWithIn")
    private Long resolvedWithIn;

    @JsonProperty("resolvedBeyond")
    private Long resolvedBeyond;

    @JsonProperty("pendingWithIn")
    private Long pendingWithIn;

    @JsonProperty("pendingBeyond")
    private Long pendingBeyond;
}
