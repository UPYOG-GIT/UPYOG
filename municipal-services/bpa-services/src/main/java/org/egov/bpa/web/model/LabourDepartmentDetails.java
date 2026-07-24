package org.egov.bpa.web.model;

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
public class LabourDepartmentDetails {

    @JsonProperty("dateOfSubmission")
    private Long dateOfSubmission;

    @JsonProperty("karyType")
    private String karyType;

    @JsonProperty("sthapnaPata")
    private String sthapnaPata;

    @JsonProperty("ulb")
    private String ulb;

    @JsonProperty("proposalNo")
    private String proposalNo;

    @JsonProperty("constructionArea")
    private Double constructionArea;

    @JsonProperty("estimatedCess")
    private Double estimatedCess;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("transactionRefNo")
    private String transactionRefNo;

    @JsonProperty("cessAmount")
    private Double cessAmount;

    @JsonProperty("transactionDate")
    private Long transactionDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("receiptNo")
    private String receiptNo;

    @JsonProperty("bankAmount")
    private Double bankAmount;

    @JsonProperty("ulbCode")
    private String ulbCode;

    @JsonProperty("ulbName")
    private String ulbName;
}