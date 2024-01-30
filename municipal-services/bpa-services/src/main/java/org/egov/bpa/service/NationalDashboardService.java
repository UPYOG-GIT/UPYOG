package org.egov.bpa.service;



import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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
	
	@Autowired
	BPAConfiguration bpaConfig;
   
	@Primary
	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

	
	public IngestRequest getIngestData() {
		
		
		List<Data> dataList = new ArrayList<>();
		
		  LocalDate specificDate = LocalDate.of(2024, 1, 16);
	        String formattedDate = specificDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

//		LocalDate currentDate = LocalDate.now();
//		String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
//		
//		int targetYear = 2023;
//
//		LocalDate specificDate = LocalDate.of(targetYear, Month.APRIL, 28);
//		String formattedDate = specificDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		

		List<Map<String, Object>> ingestData = repository.getIngestData();
		log.info("ingestData from repository" + ingestData);
		for (Map<String, Object> nationalData : ingestData) {
			Data data = new Data();

			
			String ulbName = (String) nationalData.get("ulb_name");
		
			String ulb = "ch." + ulbName.trim().toLowerCase().replaceAll("\\s", "").replaceAll("-", "");
						data.setUlb(ulb);
			data.setDate(formattedDate);
			data.setModule("OBPS");
			data.setWard((String) nationalData.get("locality"));
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
			metrics.put("StipulatedDays",0);
			
			int totalMetricValue = metrics.values().stream()
			        .filter(value -> value instanceof Number)
			        .mapToInt(value -> ((Number) value).intValue())
			        .sum();
   if (totalMetricValue > 0) {

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
				occupancyBuckets.add(createBucket("RESIDENTIAL", nationalData.get("residential")));
				occupancyBuckets.add(createBucket("INSTITUTIONAL", nationalData.get("institutional")));
				occupancyBuckets.add(createBucket("COMMERCIAL", 0));
				occupancyBuckets.add(createBucket("INDUSTRIAL",0));
				occupancyBuckets.add(createBucket("Mixed Use", 0));
			

				occupancy.put("buckets", occupancyBuckets);
				
				
				Map<String, Object> subOccupancy = new LinkedHashMap<>();
				subOccupancy.put("groupBy", "subOccupancyType");

				List<Map<String, Object>> subOccupancyBuckets = new ArrayList<>();
				subOccupancyBuckets.add(createBucket("RESIDENTIAL.INDIVIDUAL", nationalData.get("residential")));
				subOccupancyBuckets.add(createBucket("RESIDENTIAL.SHARED", nationalData.get("residential")));
				subOccupancyBuckets.add(createBucket("INSTITUTIONAL.SHARED", 0));
				subOccupancyBuckets.add(createBucket("INSTITUTIONAL.INDIVIDUAL", 0));
				subOccupancyBuckets.add(createBucket("COMMERCIAL.SHARED", 0));
				subOccupancyBuckets.add(createBucket("COMMERCIAL.INDIVIDUAL", 0));
				subOccupancyBuckets.add(createBucket("INDUSTRIAL.INDIVIDUAL", 0));
				subOccupancyBuckets.add(createBucket("INDUSTRIAL.SHARED", 0));
				subOccupancyBuckets.add(createBucket("MIXED.INDIVIDUAL", 0));
				subOccupancyBuckets.add(createBucket("MIXED.SHARED", 0));

				subOccupancy.put("buckets", subOccupancyBuckets);

	            Map<String, Object> permits = new LinkedHashMap<>();
	            permits.put("groupBy", "riskType");

	            List<Map<String, Object>> riskTypeBuckets = new ArrayList<>();
	            riskTypeBuckets.add(createBucket("LOW", nationalData.get("low")));
	            riskTypeBuckets.add(createBucket("MEDIUM", 0));
	            riskTypeBuckets.add(createBucket("HIGH", nationalData.get("medhigh")));

	            permits.put("buckets", riskTypeBuckets);

			metrics.put("todaysCollection", todaysCollection);
			metrics.put("permitsIssued", Arrays.asList(occupancy, subOccupancy, permits));

			data.setMetrics(metrics);
			int nonZeroMetricsCount = 0;

		    // Process metrics
		    for (Map.Entry<String, Object> entry : nationalData.entrySet()) {
		        if (entry.getValue() instanceof Integer && (Integer) entry.getValue() != 0) {
		            // Increment count for each non-zero metric
		            nonZeroMetricsCount++;
		        }
		    }

		    // Check if there is at least one non-zero metric before adding to dataList
		    if (hasNonZeroValue(metrics)) {
	            data.setMetrics(metrics);
	            dataList.add(data);
	        }
			ingestRequest.setIngestData(dataList);
			
			log.info("dataList which will be pushed ---" + dataList);
		    //ingestRequest.setRequestInfo(requestInfo);
			
			//pushDataToApi(apiUrl, ingestRequest);


   }
		}
		 return ingestRequest;
		
	}

	private boolean hasNonZeroValue(Map<String, Object> metrics) {
	    for (Map.Entry<String, Object> entry : metrics.entrySet()) {
	        if (entry.getValue() instanceof Map) {
	            if (hasNonZeroValue((Map<String, Object>) entry.getValue())) {
	                return true;
	            }
	        } else if (entry.getValue() instanceof Number) {
	            if (((Number) entry.getValue()).doubleValue() != 0) {
	                return true;
	            }
	        }
	    }
	    return false;
	}


		    private Map<String, Object> createBucket(String name, Object object) {
		        Map<String, Object> bucket = new HashMap<>();
		        bucket.put("name", name);
		        bucket.put("value", object);
		        return bucket;
		    }
		    
		    
			public Map<String, Object> pushDataToApi(String apiUrl) {
				log.info("Pushing data to API...");

				IngestRequest body = getIngestData();

				RequestInfo requestInfo = new RequestInfo();
				// log.info("bodyy---====" + body);

				Map<String, Object> requestInfoData = getAuthToken(bpaConfig.getUsername(), bpaConfig.getPassword(),
						bpaConfig.getGrantType(), bpaConfig.getScope(), bpaConfig.getTenantId(), bpaConfig.getType());

				log.info("requestInfoData-----------" + requestInfoData);

				Map<String, Object> requestData = new HashMap<>();

				Map<String, Object> userRequest = (Map<String, Object>) requestInfoData.get("UserRequest");

				String access_token = (String) requestInfoData.get("access_token");

				requestInfo.setAuthToken(access_token);

				User userInfo = new User();

				userInfo.setUserName((String) userRequest.get("userName"));
				// userInfo.setId((Long) userRequest.get("id"));
				userInfo.setUuid((String) userRequest.get("uuid"));
				userInfo.setName((String) userRequest.get("name"));
				userInfo.setMobileNumber((String) userRequest.get("mobileNumber"));
				userInfo.setType((String) userRequest.get("type"));

				requestInfo.setUserInfo(userInfo);

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
				
				HttpStatus statusCode = responseEntity.getStatusCode();
				int statusCodeValue = statusCode.value();

				System.out.println("HTTP Status Code: " + statusCodeValue);

				// You can also check the status code to take appropriate actions
				if (statusCode.is2xxSuccessful()) {
				  log.info("----Data Pushed Successfully----");
				} else if (statusCode.is4xxClientError()) {
				   log.info("----4xx error----");
				} else if (statusCode.is5xxServerError()) {
					  log.info("----Internal server error---- or Duplicate data found");
				}

				Map<String, Object> responseBody = responseEntity.getBody();
				return responseBody;

			}
		    
		    public Map<String, Object> getAuthToken(String username, String password, String grantType, String scope,
					String tenantId, String userType) {

				String authApi = bpaConfig.getAuthApi();

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

				ResponseEntity<Map> responseEntity = this.restTemplate().exchange(authApi, HttpMethod.POST, requestEntity,
						Map.class);

			

				return responseEntity.getBody();

			}
		    
		    
		    
//		    @Scheduled(cron = "0 59 23 * * ?")
//		    @Scheduled(cron = "0 */5 * * * ?")
//			public void scheduleDataPush() {
//		    	
//		    	log.info("Scheduled task started...");
//				pushDataToApi(bpaConfig.getIngestApi());
//				log.info("Scheduled task completed.");
//		    	
//		    }
//		    
		    
}
