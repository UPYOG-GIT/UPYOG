package org.egov.fsm.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.fsm.config.NationalDashboardConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NationalDashboardService {

    private final NationalDashboardConfig config;
    private final RestTemplate restTemplate;

    public NationalDashboardService(NationalDashboardConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * Runs every day using the configured cron expression. The job stays disabled until
     * the FSM aggregation query and deployment credentials have been configured.
     */
    @Scheduled(cron = "${egov.nationalDashboard.cron:0 * * * * *}")
    public void scheduleDataPush() {
        if (!config.isEnabled()) {
            return;
        }
        try {
            pushDataToNationalDashboard(LocalDate.now());
        } catch (Exception exception) {
            log.error("FSM National Dashboard data push failed", exception);
        }
    }

    public void pushDataToNationalDashboard(LocalDate reportDate) {
        AuthTokenResponse tokenResponse = getAuthToken();

        List<Map<String, Object>> data = getIngestData(reportDate);
        if (data.isEmpty()) {
            log.info("No FSM National Dashboard data available for {}", reportDate);
            return;
        }

        
        // RequestInfo requestInfo = new RequestInfo();
        // requestInfo.setApiId("fsm-services");
        // requestInfo.setAuthToken(tokenResponse.getAccessToken());
        // requestInfo.setUserInfo(tokenResponse.getUserRequest());

        // Map<String, Object> request = new LinkedHashMap<>();
        // request.put("RequestInfo", requestInfo);
        // request.put("Data", data);

        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // ResponseEntity<String> response = restTemplate.exchange(config.getIngestApi(), HttpMethod.POST,
        //         new HttpEntity<>(request, headers), String.class);
        // log.info("FSM National Dashboard data push completed for {} with status {}", reportDate,
        //         response.getStatusCodeValue());
    }

    /**
     * Replace this empty result with the FSM aggregation query. Each map must represent
     * one element of the NIUA Data array and include date, module, ward, ulb, region,
     * state, and metrics.
     */
    protected List<Map<String, Object>> getIngestData(LocalDate reportDate) {
        return Collections.emptyList();
    }

    private AuthTokenResponse getAuthToken() {
        if (!StringUtils.hasText(config.getPassword())) {
            throw new IllegalStateException("egov.nationalDashboard.password must be configured");
        }

        AuthTokenResponse tokenResponse = null;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", config.getUsername());
        form.add("password", config.getPassword());
        form.add("grant_type", config.getGrantType());
        form.add("scope", config.getScope());
        form.add("tenantId", config.getTenantId());
        form.add("userType", config.getType());

        log.info("STARTED GENERATING TOKEN" + form.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            ResponseEntity<AuthTokenResponse> response = restTemplate.exchange(config.getAuthApi(), HttpMethod.POST,
                new HttpEntity<>(form, headers), AuthTokenResponse.class);
            tokenResponse = response.getBody();
        } catch (Exception e) {
            log.error("Error while generating token for National Dashboard", e);
            throw new IllegalStateException("Failed to generate National Dashboard auth token", e);
        }
        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.getAccessToken())
                || tokenResponse.getUserRequest() == null) {
            throw new IllegalStateException("National Dashboard token response is incomplete");
        }
        log.info("TOKEN GENERATED SUCCESSFULLY" + tokenResponse.toString());
        return tokenResponse;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    private static class AuthTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("UserRequest")
        private User userRequest;
    }
}
