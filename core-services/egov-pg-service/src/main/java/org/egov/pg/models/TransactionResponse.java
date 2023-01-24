package org.egov.pg.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TransactionResponse {

	private String txnid;
	
	private String encResp;
}
