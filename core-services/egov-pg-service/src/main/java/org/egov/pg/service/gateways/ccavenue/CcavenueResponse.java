package org.egov.pg.service.gateways.ccavenue;

import lombok.Data;

@Data
public class CcavenueResponse {
    private String encResp;
    private String orderNo;
    private String crossSellUrl;
    
}
