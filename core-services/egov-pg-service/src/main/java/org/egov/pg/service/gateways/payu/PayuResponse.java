package org.egov.pg.service.gateways.payu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PayuResponse {
    private String mihpayid;
    private String request_id;
    private String bank_ref_num;
    private String amt;
    private String amount;
    private String transaction_amount;
    private String txnid;
    private String additional_charges;
    private String mode;
    private String status;
    private String unmappedstatus;
    private String key;
    private String cardCategory;
    private String discount;
    private String net_amount_debit;
    private String addedon;
    private String productinfo;
    private String firstname;
    private String lastname;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String email;
    private String phone;
    private String udf1;
    private String udf2;
    private String udf3;
    private String udf4;
    private String udf5;
    private String udf6;
    private String udf7;
    private String udf8;
    private String udf9;
    private String udf10;
    private String hash;
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;
    private String field6;
    private String field7;
    private String field8;
    private String field9;
    private String payment_source;

    @JsonProperty("PG_TYPE")
    private String pgType;

    private String bankcode;
    private String error;
    private String error_code;

    @JsonProperty("error_Message")
    private String error_Message;

    private String cardnum;
    private String cardhash;
    private String issuing_bank;
    private String card_type;
    private String splitInfo;
    private String disc;
    private String card_no;
    private String name_on_card;
    private String Merchant_UTR;
    private String Settled_At;
}
