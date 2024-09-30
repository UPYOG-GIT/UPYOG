package org.egov.bpa.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class NationalDashboardConfig {

	@Value("${egov.nationalDashboard.username}")
	private String username;

	@Value("${egov.nationalDashboard.password}")
	private String password;

	@Value("${egov.nationalDashboard.grantType}")
	private String grantType;

	@Value("${egov.nationalDashboard.scope}")
	private String scope;

	@Value("${egov.nationalDashboard.tenantId}")
	private String tenantId;

	@Value("${egov.nationalDashboard.type}")
	private String type;

	@Value("${egov.nationalDashboard.authApi}")
	private String authApi;

	@Value("${egov.nationalDashboard.ingestApi}")
	private String ingestApi;
	
	
	@Value("${egov.nationalDashboard.environment}")
	private String environment;
	
	
}
