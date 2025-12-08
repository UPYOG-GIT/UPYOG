package org.egov.pg.service.gateways.razorpay;

import lombok.Data;

@Data
public class RazorPayResponse {
    private String encResp;
    private String orderNo;
    private String crossSellUrl;
    
}
