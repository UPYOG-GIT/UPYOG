package org.egov.ndb.config;

import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
	
	
	@Value("${egov.nationalDashboard.ndbSaveTopic}")
	private String ndbSaveTopic;

	
	


	
}
