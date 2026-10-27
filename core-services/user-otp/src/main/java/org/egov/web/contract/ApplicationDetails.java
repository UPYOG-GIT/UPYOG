package org.egov.web.contract;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@ToString
public class ApplicationDetails {

    @JsonProperty("dateOfSubmission")
    private String dateOfSubmission;

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
    private String transactionDate;

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
