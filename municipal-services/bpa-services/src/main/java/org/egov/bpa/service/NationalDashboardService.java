package org.egov.bpa.service;



import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.repository.NationalDashboardRepository;
import org.egov.bpa.web.model.Data;
import org.egov.bpa.web.model.IngestRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class NationalDashboardService {
	@Autowired
	NationalDashboardRepository repository;
	
	IngestRequest ingestRequest = new IngestRequest();
   
	@Primary
	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

	
	public IngestRequest getIngestData() {
		
		
		List<Data> dataList = new ArrayList<>();

		LocalDate currentDate = LocalDate.now();
		String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		Data data = new Data();

		List<Map<String, Object>> ingestData = repository.getIngestData();
		
		for (Map<String, Object> nationalData : ingestData) {

			//System.out.println("nationalData--" + nationalData);

			data.setDate(formattedDate);
			data.setModule("OBPAS");
			data.setWard((String) nationalData.get("locality"));
			data.setUlb((String) nationalData.get("tenantid"));
			data.setRegion((String) nationalData.get("tenantId"));
			data.setState("Chhattisgarh");

			HashMap<String, Object> metrics = new HashMap<>();

			metrics.put("ocPlansScrutinized", 0);
			metrics.put("plansScrutinized", nationalData.get("initiatedcount"));
			metrics.put("ocSubmitted", 0);
			metrics.put("applicationsSubmitted", nationalData.get("initiatedcount"));
			metrics.put("ocIssued", 0);
			metrics.put("landAreaAppliedInSystemForBPA", 0);
			metrics.put("averageDaysToIssuePermit", 0);
			metrics.put("averageDaysToIssueOC", 0);
			metrics.put("todaysClosedApplicationsOC", 0);
			metrics.put("todaysClosedApplicationsPermit", 0);
			metrics.put("todaysCompletedApplicationsWithinSLAPermit",
					nationalData.get("todaysApprovedApplicationsWithinSLA"));
			metrics.put("slaComplianceOC", 0);
			metrics.put("slaCompliancePermit", 0);
			metrics.put("applicationsWithDeviation", 0);
			metrics.put("averageDeviation", 0);
			metrics.put("ocWithDeviation", 0);
			metrics.put("todaysApprovedApplications", nationalData.get("todaysApprovedApplicationsWithinSLA"));
			metrics.put("todaysApprovedApplicationsWithinSLA", nationalData.get("todaysApprovedApplicationsWithinSLA"));
			metrics.put("avgDaysForApplicationApproval", 0);
			metrics.put("StipulatedDays", nationalData.get("tenantid"));

			List<Map<String, Object>> todaysCollection = new ArrayList<>();

			Map<String, Object> collectionMode = new LinkedHashMap<>();
			collectionMode.put("groupBy", "paymentMode");

			List<Map<String, Object>> collectionBuckets = new ArrayList<>();
			collectionBuckets.add(createBucket("UPI", nationalData.get("upi")));
			collectionBuckets.add(createBucket("DEBIT.CARD", nationalData.get("debit_card")));
			collectionBuckets.add(createBucket("CREDIT.CARD", nationalData.get("credit_card")));
			collectionBuckets.add(createBucket("CASH", 0));

			collectionMode.put("buckets", collectionBuckets);
			todaysCollection.add(collectionMode);
			
			 Map<String, Object> occupancy = new LinkedHashMap<>();
	            occupancy.put("groupBy", "occupancyType");

	            List<Map<String, Object>> occupancyBuckets = new ArrayList<>();
	            occupancyBuckets.add(createBucket("Residential", nationalData.get("residential")));
	            occupancyBuckets.add(createBucket("Institutional", nationalData.get("institutional")));
	            occupancyBuckets.add(createBucket("Mixed Use", 0));
	            occupancyBuckets.add(createBucket("Institutional", 0));


	            occupancy.put("buckets", occupancyBuckets);

	            Map<String, Object> permits = new LinkedHashMap<>();
	            permits.put("groupBy", "riskType");

	            List<Map<String, Object>> riskTypeBuckets = new ArrayList<>();
	            riskTypeBuckets.add(createBucket("LOW", nationalData.get("low")));
	            riskTypeBuckets.add(createBucket("MEDIUM", 0));
	            riskTypeBuckets.add(createBucket("HIGH", nationalData.get("medhigh")));

	            permits.put("buckets", riskTypeBuckets);

			metrics.put("todaysCollection", todaysCollection);
			metrics.put("permitIssued", Arrays.asList(occupancy, permits));

			data.setMetrics(metrics);
			 dataList.add(data);
			ingestRequest.setIngestData(dataList);
		    //ingestRequest.setRequestInfo(requestInfo);
			
			//pushDataToApi(apiUrl, ingestRequest);


		       
		}
		 return ingestRequest;
		
	}

		    private Map<String, Object> createBucket(String name, Object object) {
		        Map<String, Object> bucket = new HashMap<>();
		        bucket.put("name", name);
		        bucket.put("value", object);
		        return bucket;
		    }
		    
		    
		

			public Map<String, Object> pushDataToApi(String apiUrl, RequestInfo requestInfo) {

				IngestRequest body = getIngestData();
				log.info("bodyy---====" + body);

				Map<String, Object> requestInfoData = getAuthToken("CH_NDA_USER", "upyogTest@123", "password", "read",
						"pg", "SYSTEM");

				log.info("requestInfoData-----------" + requestInfoData);

				Map<String, Object> requestData = new HashMap<>();

				Map<String, Object> userRequest = (Map<String, Object>) requestInfoData.get("UserRequest");

				String access_token = (String) requestInfoData.get("access_token");
//				String apiId = (String) requestInfoData.get("apiId");
//				String ver = (String) requestInfoData.get("ver");
//				Long ts = (Long) requestInfoData.get("ts");
//				String action = (String) requestInfoData.get("action");
//				String did = (String) requestInfoData.get("did");
//				String key = (String) requestInfoData.get("key");
//				String msgId = (String) requestInfoData.get("msgId");
				
				log.info("accerr " + access_token);
				log.info("userName" + (String) userRequest.get("userName"));
				log.info("name" + (String) userRequest.get("name"));
				log.info("mobileNumber" + (String) userRequest.get("mobileNumber"));
			//	log.info("userName" + (String) userRequest.get("userName"));
				log.info("type" + (String) userRequest.get("type"));

				requestInfo.setAuthToken(access_token);
				requestInfo.setUserInfo((User) userRequest);

				User userInfo = new User();
				userInfo.setUserName((String) userRequest.get("userName"));
				userInfo.setId((Long) userRequest.get("id"));
				userInfo.setUuid((String) userRequest.get("uuid"));
				userInfo.setName((String) userRequest.get("name"));
				userInfo.setMobileNumber((String) userRequest.get("mobileNumber"));
				userInfo.setType((String) userRequest.get("type"));
				
				
				log.info("accerr " + access_token);
				log.info("userName" + (String) userRequest.get("userName"));
				log.info("name" + (String) userRequest.get("name"));
				log.info("mobileNumber" + (String) userRequest.get("mobileNumber"));
			//	log.info("userName" + (String) userRequest.get("userName"));
				log.info("type" + (String) userRequest.get("type"));
				

				List<Map<String, Object>> roles = (List<Map<String, Object>>) userRequest.get("roles");

				List<Role> userRole = new ArrayList<>();

				for (Map<String, Object> role : roles) {

					Role rolee = new Role();
					rolee.setName((String) role.get("name"));
					rolee.setCode((String) role.get("code"));
					rolee.setTenantId((String) role.get("tenantId"));

					userRole.add(rolee);
				}

				userInfo.setRoles(userRole);

				ingestRequest.setRequestInfo(requestInfo);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				HttpEntity<IngestRequest> requestEntity = new HttpEntity<IngestRequest>(body, headers);

				ResponseEntity<Map> responseEntity = this.restTemplate().exchange(apiUrl, HttpMethod.POST,
						requestEntity, Map.class);

				Map<String, Object> responseBody = responseEntity.getBody();
				return responseBody;

			}
		    
		    public Map<String, Object> getAuthToken(String username, String password, String grantType, String scope,
					String tenantId, String userType) {

				String apiUrl = "https://upyog-test.niua.org/user/oauth/token";

				MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();

				requestBody.add("username", username);
				requestBody.add("password", password);
				requestBody.add("grant_type", grantType);
				requestBody.add("scope", scope);
				requestBody.add("tenantId", tenantId);
				requestBody.add("userType", userType);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

				headers.add("Authorization", "Basic ZWdvdi11c2VyLWNsaWVudDo=");

				HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

				System.out.println("requestEntity" + requestEntity);

				ResponseEntity<Map> responseEntity = this.restTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity,
						Map.class);

				
				log.info("Response Body: " + responseEntity.getBody());
				log.info("Response Headers: " + responseEntity.getHeaders());

				return responseEntity.getBody();

			}
}
