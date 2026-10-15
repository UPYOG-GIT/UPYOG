package org.egov.fsm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class NationalDashboardConfig {

    @Value("${egov.nationalDashboard.enabled:false}")
    private boolean enabled;

    @Value("${egov.nationalDashboard.cron:0 58 23 * * *}")
    private String cron;

    @Value("${egov.nationalDashboard.username:CH_NDA_USER}")
    private String username;

    @Value("${egov.nationalDashboard.password:}")
    private String password;

    @Value("${egov.nationalDashboard.grantType:password}")
    private String grantType;

    @Value("${egov.nationalDashboard.scope:read}")
    private String scope;

    @Value("${egov.nationalDashboard.tenantId:pg}")
    private String tenantId;

    @Value("${egov.nationalDashboard.type:SYSTEM}")
    private String type;

    @Value("${egov.nationalDashboard.authApi:https://upyog-test.niua.org/user/oauth/token}")
    private String authApi;

    @Value("${egov.nationalDashboard.ingestApi:https://upyog-test.niua.org/national-dashboard/metric/_ingest}")
    private String ingestApi;
}
