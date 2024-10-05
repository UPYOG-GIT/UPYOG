package org.egov.bpa.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.NationalDashboardConfig;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.repository.NationalDashboardRepository;
import org.egov.bpa.web.model.Data;
import org.egov.bpa.web.model.IngestRequest;
import org.egov.bpa.web.model.NdbResponse;
import org.egov.bpa.web.model.ResponseInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

	@Autowired
	BPARepository bpaRepository;

	IngestRequest ingestRequest = new IngestRequest();

	@Autowired
	NationalDashboardConfig nationalDashboardConfig;

//	@Primary
//	@Bean
//	public RestTemplate restTemplate() {
//		return new RestTemplate();
//	}

	@Autowired
	private RestTemplate restTemplate;

	public IngestRequest getIngestData(String formattedDate1) {

		List<Data> dataList = new ArrayList<>();

//
//		LocalDate currentDate = LocalDate.now();
//		LocalDate previousDate = currentDate.minusDays(1);
		LocalDate date = LocalDate.parse(formattedDate1);
		String formattedDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
//		String formattedDate = previousDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

//		LocalDate specificDate = LocalDate.of(2023, 12, 15);
//		String formattedDate = specificDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

//	        LocalDate startDate = LocalDate.of(2023, 1, 1);
//	        LocalDate endDate = LocalDate.of(2024, 2, 14);
//	        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//	        Map<String, Object> resultMap = new HashMap<>();
//
//	        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//	            String formattedDate1 = date.format(dateFormatter);

		// Data data = new Data();

		// int targetYear = 2024;

//		LocalDate specificDate = LocalDate.of(targetYear, Month.JANUARY, 1);
//		String formattedDate = specificDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

//		Map<String, Object> ingestDataResult = repository.getIngestData(formattedDate);
		Map<String, Object> ingestDataResult = repository.getIngestData(formattedDate1);
		// log.info("ingestttt_____" + ingestDataResult.get("result1"));
		List<Map<String, Object>> ingestData = (List<Map<String, Object>>) ingestDataResult.get("result1");
		String avgDaysToIssueCertificate = ingestDataResult.get("avg_days_to_issue_certificate").toString();
		String totalPlotArea = ingestDataResult.get("totalPlotArea").toString();
		double totalPlotArea1 = Double.valueOf(totalPlotArea);
		// String landAreaAppliedInSystemForBPA =
		// ingestDataResult.get("land_area_applied_in_system_for_bpa").toString();

		// Remove square brackets if present
		if (avgDaysToIssueCertificate.startsWith("[") && avgDaysToIssueCertificate.endsWith("]")) {
			avgDaysToIssueCertificate = avgDaysToIssueCertificate.substring(1, avgDaysToIssueCertificate.length() - 1);
		}

		// Remove everything after the decimal point
		if (avgDaysToIssueCertificate.contains(".")) {
			avgDaysToIssueCertificate = avgDaysToIssueCertificate.split("\\.")[0];
		}

		int avgDaysToIssueCertificate1 = Integer.parseInt(avgDaysToIssueCertificate);
//		if (landAreaAppliedInSystemForBPA.startsWith("[") && landAreaAppliedInSystemForBPA.endsWith("]")) {
//			landAreaAppliedInSystemForBPA = landAreaAppliedInSystemForBPA.substring(1, landAreaAppliedInSystemForBPA.length() - 1);
//		}
//		

		for (Map<String, Object> nationalData : ingestData) {

			// log.info("nationalDataa----" + nationalData);

			Data data = new Data();

			// log.info("nationalData--" + nationalData);
			String ulbName = (String) nationalData.get("ulb_name");
			// log.info("ulbName--" + ulbName);
			// log.info("date__&**" + nationalData.get("avg_days_to_issue_certificate"));

			String ulb = "ch." + ulbName.trim().toLowerCase().replaceAll("\\s", "").replaceAll("-", "");

			// log.info("ulbbb" + ulb);
			data.setUlb(ulb);
			data.setDate(formattedDate);
			data.setModule("OBPS");
			data.setWard((String) nationalData.get("locality"));
			data.setRegion((String) nationalData.get("tenantId"));
			data.setState("Chhattisgarh");

			HashMap<String, Object> metrics = new HashMap<>();

			/*
			 * metrics.put("ocPlansScrutinized", 0); metrics.put("plansScrutinized",
			 * nationalData.get("initiatedcount")); metrics.put("ocSubmitted", 0);
			 * metrics.put("applicationsSubmitted", nationalData.get("initiatedcount"));
			 * metrics.put("ocIssued", 0); metrics.put("landAreaAppliedInSystemForBPA",
			 * totalPlotArea1); metrics.put("averageDaysToIssuePermit",
			 * avgDaysToIssueCertificate1); metrics.put("averageDaysToIssueOC", 0);
			 * metrics.put("todaysClosedApplicationsOC", 0);
			 * metrics.put("todaysClosedApplicationsPermit",
			 * nationalData.get("todaysApprovedApplicationsWithinSLA"));
			 * metrics.put("todaysCompletedApplicationsWithinSLAPermit",
			 * nationalData.get("todaysApprovedApplicationsWithinSLA"));
			 * metrics.put("slaComplianceOC", 0); metrics.put("slaCompliancePermit", 0);
			 * metrics.put("applicationsWithDeviation", 0); metrics.put("averageDeviation",
			 * 0); metrics.put("ocWithDeviation", 0);
			 * metrics.put("todaysApprovedApplications",
			 * nationalData.get("todaysApprovedApplicationsWithinSLA"));
			 * metrics.put("todaysApprovedApplicationsWithinSLA",
			 * nationalData.get("todaysApprovedApplicationsWithinSLA"));
			 * metrics.put("avgDaysForApplicationApproval", avgDaysToIssueCertificate1);
			 * metrics.put("StipulatedDays", 0);
			 */
			List<Map<String, Object>> todaysCollection = new ArrayList<>();

			Map<String, Object> collectionMode = new LinkedHashMap<>();
			collectionMode.put("groupBy", "paymentMode");

			List<Map<String, Object>> collectionBuckets = new ArrayList<>();
//			collectionBuckets.add(createBucket("UPI", nationalData.get("upi_amt")));
//			collectionBuckets.add(createBucket("DEBIT.CARD", nationalData.get("debit_amt")));
//			collectionBuckets.add(createBucket("CREDIT.CARD", nationalData.get("credit_amt")));
//			collectionBuckets.add(createBucket("CASH", 0));

			collectionBuckets.add(createBucket("Digital", nationalData.get("online_amt")));
			collectionBuckets.add(createBucket("Non Digital", 0));

			collectionMode.put("buckets", collectionBuckets);
			todaysCollection.add(collectionMode);

			Map<String, Object> occupancy = new LinkedHashMap<>();
			occupancy.put("groupBy", "occupancyType");

			List<Map<String, Object>> occupancyBuckets = new ArrayList<>();
			occupancyBuckets.add(createBucket("RESIDENTIAL", nationalData.get("residential")));
			occupancyBuckets.add(createBucket("INSTITUTIONAL", 0));
			occupancyBuckets.add(createBucket("COMMERCIAL", nationalData.get("Commercial")));
			occupancyBuckets.add(createBucket("INDUSTRIAL", nationalData.get("industrial")));
			occupancyBuckets.add(createBucket("Mixed Use", nationalData.get("Mix")));

			occupancy.put("buckets", occupancyBuckets);

			Map<String, Object> subOccupancy = new LinkedHashMap<>();
			subOccupancy.put("groupBy", "subOccupancyType");

			List<Map<String, Object>> subOccupancyBuckets = new ArrayList<>();
			subOccupancyBuckets.add(createBucket("RESIDENTIAL.INDIVIDUAL", nationalData.get("residential")));
			subOccupancyBuckets.add(createBucket("RESIDENTIAL.SHARED", 0));
			subOccupancyBuckets.add(createBucket("INSTITUTIONAL.SHARED", 0));
			subOccupancyBuckets.add(createBucket("INSTITUTIONAL.INDIVIDUAL", 0));
			subOccupancyBuckets.add(createBucket("COMMERCIAL.SHARED", 0));
			subOccupancyBuckets.add(createBucket("COMMERCIAL.INDIVIDUAL", nationalData.get("Commercial")));
			subOccupancyBuckets.add(createBucket("INDUSTRIAL.INDIVIDUAL", nationalData.get("industrial")));
			subOccupancyBuckets.add(createBucket("INDUSTRIAL.SHARED", 0));
			subOccupancyBuckets.add(createBucket("MIXED.INDIVIDUAL", nationalData.get("Mix")));
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

			boolean hasNonZeroMetric = hasNonZeroMetric(metrics);
//			if (hasNonZeroMetric) {

			log.info("countt==" + dataList.size());
			HashMap<String, Object> extraMetrics = data.getMetrics();

			extraMetrics.put("ocPlansScrutinized", 0);
			extraMetrics.put("plansScrutinized", nationalData.get("todaysApplicationSubmitted"));
			extraMetrics.put("ocSubmitted", 0);
			extraMetrics.put("applicationsSubmitted", nationalData.get("todaysApplicationSubmitted"));
			extraMetrics.put("ocIssued", 0);
			extraMetrics.put("landAreaAppliedInSystemForBPA", totalPlotArea1);
			extraMetrics.put("averageDaysToIssuePermit", avgDaysToIssueCertificate1);
			extraMetrics.put("averageDaysToIssueOC", 0);
			extraMetrics.put("todaysClosedApplicationsOC", 0);
			extraMetrics.put("todaysClosedApplicationsPermit", nationalData.get("ApprovedCount"));
			extraMetrics.put("todaysCompletedApplicationsWithinSLAPermit",
					nationalData.get("todaysApprovedApplicationsWithinSLA1"));
			extraMetrics.put("slaComplianceOC", 0);
			extraMetrics.put("slaCompliancePermit", 0);
			extraMetrics.put("applicationsWithDeviation", 0);
			extraMetrics.put("averageDeviation", 0);
			extraMetrics.put("ocWithDeviation", 0);
			extraMetrics.put("todaysApprovedApplications", nationalData.get("ApprovedCount"));
			extraMetrics.put("todaysApprovedApplicationsWithinSLA",
					nationalData.get("todaysApprovedApplicationsWithinSLA1"));
//			extraMetrics.put("pendingApplicationsBeyondTimeline",
//					nationalData.get("	"));
			extraMetrics.put("avgDaysForApplicationApproval", avgDaysToIssueCertificate1);
			extraMetrics.put("StipulatedDays", 0);

			data.setMetrics(extraMetrics);
			dataList.add(data);
//			}

		}
		// log.info("dataList--" + dataList);
		ingestRequest.setIngestData(dataList);
		// }
		return ingestRequest;

	}

	private boolean hasNonZeroMetric(HashMap<String, Object> metrics) {
		// Check top-level metrics
		for (Map.Entry<String, Object> entry : metrics.entrySet()) {
			if (entry.getValue() instanceof Number) {
				if (((Number) entry.getValue()).doubleValue() != 0) {
					return true;
				}
			}
		}
		// Check nested metrics
		// Map<String, Object> nestedMetrics = data.getMetrics();

		for (Map.Entry<String, Object> nestedEntry : metrics.entrySet()) {
			Object nestedValue = nestedEntry.getValue();

			if (nestedValue instanceof List) {
				// Handle lists of metrics, e.g., permitsIssued, todaysCollection
				for (Object listItem : (List<?>) nestedValue) {
					if (listItem instanceof Map) {
						for (Object mapValue : ((Map<?, ?>) listItem).values()) {
							if (mapValue instanceof List) {
								for (Object listItem1 : ((List<?>) mapValue)) {
									for (Object mapValue1 : ((Map<?, ?>) listItem1).values()) {
										if (mapValue1 instanceof Number && ((Number) mapValue1).doubleValue() != 0) {
											return true;
										}
									}
								}
							}
							if (mapValue instanceof Number && ((Number) mapValue).doubleValue() != 0) {
								return true;
							}
						}
					}
				}
			} else if (nestedValue instanceof Map) {
				// Handle nested maps, e.g., permitsIssued with buckets
				for (Object mapValue : ((Map<?, ?>) nestedValue).values()) {
					if (mapValue instanceof Number && ((Number) mapValue).doubleValue() != 0) {
						return true;
					}
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

	public NdbResponse pushDataToApi(String apiUrl) {
//		String formattedDate1 = "";
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		LocalDate specificDate = LocalDate.of(2024, 9, 14);

		LocalDate currentDate = LocalDate.now();
		// LocalDate previousDate = currentDate.minusDays(1);

//		String formattedDate1 = specificDate.format(dateFormatter);
		String formattedDate1 = currentDate.format(dateFormatter);
		IngestRequest body = getIngestData(formattedDate1);
		int noOfRecords = body.getIngestData().size();
		log.info("body.getIngestData().size(): " + noOfRecords);
		// log.info("bodyy---====" + body);

//		LocalDate recordPushedDate = LocalDate.parse(formattedDate1);
//		String environment = apiUrl.contains("upyog.niua.org") ? "Production" : "Testing";
		String environment = nationalDashboardConfig.getEnvironment();
		if (noOfRecords > 0) {

			try {
//				ResponseInfoWrapper responseInfoWrapper = getAuthToken(nationalDashboardConfig.getUsername(),
//						nationalDashboardConfig.getPassword(), nationalDashboardConfig.getGrantType(),
//						nationalDashboardConfig.getScope(), nationalDashboardConfig.getTenantId(),
//						nationalDashboardConfig.getType());
				ResponseInfoWrapper responseInfoWrapper = getAuthToken();
//		Map<String, Object> requestData = new HashMap<>();
				RequestInfo requestInfo = new RequestInfo();

				// log.info("requestInfoData" + requestInfoData);

//		Map<String, Object> userRequest = (Map<String, Object>) requestInfoData.get("UserRequest");
				User userRequest = responseInfoWrapper.getUserRequest();

//		String access_token = (String) requestInfoData.get("access_token");
//		String accessToken = requestInfoData.getAccessToken();

				requestInfo.setAuthToken(responseInfoWrapper.getAccessToken());
				requestInfo.setApiId("asset-services");
				requestInfo.setUserInfo(userRequest);

				ingestRequest.setRequestInfo(requestInfo);
				// ingestRequest.setRequestInfo((RequestInfo) requestInfoData);
//		log.info("rolesss" + roles.toString());
//		log.info("getRoles--" + userInfo.getRoles().toString());

				log.info("requesttInfoo ______ " + ingestRequest.getRequestInfo().toString());

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				HttpEntity<IngestRequest> requestEntity = new HttpEntity<IngestRequest>(body, headers);

//				Map<String, Object> returnResponse = new HashMap<>();
//		String environment = apiUrl.contains("upyog.niua.org") ? "Production" : "Testing";
//		System.out.println("requestEntity: " + requestEntity);

				ResponseEntity<NdbResponse> responseEntity = this.restTemplate.exchange(apiUrl, HttpMethod.POST,
						requestEntity, NdbResponse.class);
				NdbResponse ndbResponse = responseEntity.getBody();

				ndbResponse.setDate(currentDate);
				ndbResponse.setEnvironment(environment);
				ndbResponse.setNoOfRecordsPushed(noOfRecords);
				ndbResponse.setMessageDescription("Record successfully pushed");

				log.info("responseEntity: " + responseEntity.toString());
				log.info("ndbResponse: " + ndbResponse.toString());

				/*
				 * NdbResponseInfoWrapper ndbResponseInfoWrapper = new NdbResponseInfoWrapper();
				 * 
				 * // HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode(); //
				 * int statusCodeValue = statusCode.value();
				 * 
				 * // System.out.println("HTTP Status Code: " + statusCodeValue); //
				 * System.out.println("responseEntity: " + responseEntity.toString()); //
				 * ndbResponseInfoWrapper.getNdbResponseInfo().setDate(currentDate);
				 * ndbResponseInfoWrapper.getNdbResponseInfo().setResponseHash(ndbResponse.
				 * getResponseHash());
				 * ndbResponseInfoWrapper.getNdbResponseInfo().setEnvironment(environment);
				 * 
				 * System.out.println("----Data Pushed Successfully----");
				 * log.info("ndbResponseInfoWrapper: " + ndbResponseInfoWrapper.toString());
				 */
//
//			returnResponse.put("ResponseInfo", ndbResponseInfoWrapper.getResponseHash());

				log.info("----Data Pushed Successfully----");
				return ndbResponse;
			} catch (Exception ex) {
//			System.out.println("Exception : " + ex);
				log.error("ex.getMessage() : " + ex.getMessage());

				NdbResponse ndbResponse = new NdbResponse();
				ndbResponse.setDate(currentDate);
				ndbResponse.setEnvironment(environment);
//			ndbResponse.setErrorMessage(error.getString("message"));
				ndbResponse.setErrorMessage(ex.getMessage());
				ndbResponse.setNoOfRecordsPushed(0);
				ndbResponse.setMessageDescription("Exception while pushing data");
//			bpaRepository.saveDashboardPushRecord(ndbResponseInfoWrapper);
				return ndbResponse;
			}
		} else {
			NdbResponse ndbResponse = new NdbResponse();

			ndbResponse.setEnvironment(environment);
			ndbResponse.setMessageDescription("No Record Available");
			ndbResponse.setDate(currentDate);
			ndbResponse.setNoOfRecordsPushed(0);
//			repository.saveDashboardPushedRecord(ndbResponse);
			return ndbResponse;
		}

	}

	public NdbResponse pushDataToApiManually(String apiUrl, String date) {
//		String formattedDate1 = "";
//		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		LocalDate specificDate = LocalDate.of(2024, 9, 14);

//		LocalDate currentDate = LocalDate.now();
		// LocalDate previousDate = currentDate.minusDays(1);

//		String formattedDate1 = specificDate.format(dateFormatter);
//		String formattedDate1 = currentDate.format(dateFormatter);
		log.info("Environment URL : " + apiUrl + ", Date : " + date);
		String formattedDate1 = date;

		LocalDate recordPushedDate = LocalDate.parse(date);
//		String environment = apiUrl.contains("upyog.niua.org") ? "Production" : "Testing";
		String environment = nationalDashboardConfig.getEnvironment();

		IngestRequest body = getIngestData(formattedDate1);
		// log.info("bodyy---====" + body);
		int noOfRecords = body.getIngestData().size();
		log.info("body.getIngestData().size(): " + body.getIngestData().size());

		if (noOfRecords > 0) {

//			ResponseInfoWrapper responseInfoWrapper = getAuthToken(nationalDashboardConfig.getUsername(),
//					nationalDashboardConfig.getPassword(), nationalDashboardConfig.getGrantType(),
//					nationalDashboardConfig.getScope(), nationalDashboardConfig.getTenantId(),
//					nationalDashboardConfig.getType());
			try {
				ResponseInfoWrapper responseInfoWrapper = getAuthToken();
//		Map<String, Object> requestData = new HashMap<>();
				RequestInfo requestInfo = new RequestInfo();

				// log.info("requestInfoData" + requestInfoData);

//		Map<String, Object> userRequest = (Map<String, Object>) requestInfoData.get("UserRequest");
				User userRequest = responseInfoWrapper.getUserRequest();

				requestInfo.setAuthToken(responseInfoWrapper.getAccessToken());
				requestInfo.setApiId("asset-services");
				requestInfo.setUserInfo(userRequest);

				ingestRequest.setRequestInfo(requestInfo);
				// ingestRequest.setRequestInfo((RequestInfo) requestInfoData);
//		log.info("rolesss" + roles.toString());
//		log.info("getRoles--" + userInfo.getRoles().toString());

				log.info("requesttInfoo ______ " + ingestRequest.getRequestInfo().toString());

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				HttpEntity<IngestRequest> requestEntity = new HttpEntity<IngestRequest>(body, headers);

//			Map<String, Object> returnResponse = new HashMap<>();

//		System.out.println("requestEntity: " + requestEntity);

				ResponseEntity<NdbResponse> responseEntity = this.restTemplate.exchange(apiUrl, HttpMethod.POST,
						requestEntity, NdbResponse.class);
				NdbResponse ndbResponse = responseEntity.getBody();
				ndbResponse.setDate(recordPushedDate);
				ndbResponse.setEnvironment(environment);
				ndbResponse.setNoOfRecordsPushed(noOfRecords);
				ndbResponse.setMessageDescription("Record successfully pushed");

				log.info("responseEntity: " + responseEntity.toString());
				log.info("ndbResponse: " + ndbResponse.toString());

				/*
				 * NdbResponseInfoWrapper ndbResponseInfoWrapper = new NdbResponseInfoWrapper();
				 * 
				 * NdbResponseInfo ndbResponseInfo = new NdbResponseInfo();
				 * 
				 * ndbResponseInfo.setResponseHash(ndbResponse.getResponseHash());
				 * ndbResponseInfo.setDate(recordPushedDate);
				 * ndbResponseInfo.setEnvironment(environment);
				 * 
				 * 
				 * 
				 * ndbResponseInfoWrapper.setNdbResponseInfo(ndbResponseInfo);
				 * ndbResponseInfoWrapper.setResponseInfo(ndbResponse.getResponseInfo());
				 * log.info("ndbResponseInfoWrapper: " + ndbResponseInfoWrapper.toString());
				 */

//				bpaRepository.saveDashboardPushRecord(ndbResponseInfoWrapper);
				repository.saveDashboardPushedRecord(ndbResponse);
				log.info("----Data Pushed Successfully----");
//`
//			returnResponse.put("ResponseInfo", ndbResponseInfoWrapper.getResponseHash());
				return ndbResponse;
			} catch (Exception ex) {
				log.error("Exception : " + ex);
				log.error("ex.getMessage() : " + ex.getMessage());
				/*
				 * Map<String, String> errorMap = new HashMap<>(); String jsonPart =
				 * ex.getMessage().split(" : ")[1].replace("\"", ""); JSONObject jsonObject =
				 * new JSONObject(jsonPart); JSONObject error =
				 * jsonObject.getJSONArray("Errors").getJSONObject(0);
				 */
				/*
				 * NdbResponseInfoWrapper ndbResponseInfoWrapper = new NdbResponseInfoWrapper();
				 * 
				 * NdbResponseInfo ndbResponseInfo = new NdbResponseInfo();
				 * 
				 * NdbErrorMap ndbErrorMap = new NdbErrorMap(); // System.out.println("error: "
				 * + error); errorMap.put("code", error.getString("code"));
				 * errorMap.put("message", error.getString("message"));
				 * errorMap.put("description", error.optString("description",
				 * "No description provided"));
				 * 
				 * ndbErrorMap.setCode(error.getString("code"));
				 * ndbErrorMap.setMessage(error.getString("message")); List<NdbErrorMap>
				 * errorDetailList = new ArrayList<>(); errorDetailList.add(ndbErrorMap);
				 * ndbResponseInfo.setErrors(errorDetailList);
				 * 
				 * ndbResponseInfo.setDate(recordPushedDate);
				 * ndbResponseInfo.setEnvironment(environment);
				 * ndbResponseInfoWrapper.setNdbResponseInfo(ndbResponseInfo);
				 * 
				 * returnResponse.put("ResponseInfo", errorMap);
				 */

				NdbResponse ndbResponse = new NdbResponse();
				ndbResponse.setDate(recordPushedDate);
				ndbResponse.setEnvironment(environment);
//				ndbResponse.setErrorMessage(error.getString("message"));
				ndbResponse.setErrorMessage(ex.getMessage());
				ndbResponse.setNoOfRecordsPushed(0);
				ndbResponse.setMessageDescription("Exception while pushing data");
				repository.saveDashboardPushedRecord(ndbResponse);
//				bpaRepository.saveDashboardPushRecord(ndbResponseInfoWrapper);
				return ndbResponse;

			}
		} else {
			NdbResponse ndbResponse = new NdbResponse();

			ndbResponse.setEnvironment(environment);
			ndbResponse.setMessageDescription("No Record Available");
			ndbResponse.setDate(recordPushedDate);
			ndbResponse.setNoOfRecordsPushed(0);
			repository.saveDashboardPushedRecord(ndbResponse);
			return ndbResponse;
		}

	}

//	public ResponseInfoWrapper getAuthToken(String username, String password, String grantType, String scope,
//			String tenantId, String userType) {
	public ResponseInfoWrapper getAuthToken() {
		String authApi = nationalDashboardConfig.getAuthApi();
		String username = nationalDashboardConfig.getUsername();
		String password = nationalDashboardConfig.getPassword();
		String grantType = nationalDashboardConfig.getGrantType();
		String scope = nationalDashboardConfig.getScope();
		String tenantId = nationalDashboardConfig.getTenantId();
		String userType = nationalDashboardConfig.getType();

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

		ResponseEntity<ResponseInfoWrapper> responseEntity = this.restTemplate.exchange(authApi, HttpMethod.POST,
				requestEntity, ResponseInfoWrapper.class);

		return responseEntity.getBody();

	}

//    @Scheduled(cron = "0 */5 * * * ?")
	@Scheduled(cron = "0 58 23 * * *")
	public void scheduleDataPush() {

		log.info("Scheduled task started...");
		NdbResponse ndbResponse = pushDataToApi(nationalDashboardConfig.getIngestApi());
		repository.saveDashboardPushedRecord(ndbResponse);
//		bpaRepository.saveDashboardPushRecord(ndbResponseInfoWrapper);
		log.info("Scheduled task completed.");
	}

//		    

}
