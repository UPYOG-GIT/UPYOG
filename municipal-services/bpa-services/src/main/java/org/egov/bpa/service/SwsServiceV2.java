package org.egov.bpa.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class SwsServiceV2 {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Lazy
	private BPAService bpaService;

	public ResponseEntity<String> updateStatusToSws(BPARequest bpaRequest, String bpaAction) {

		try {
//			log.info("bpaRequest : " + bpaRequest.toString());
			BPA bpa = bpaRequest.getBPA();
//			log.info("SWS bpa : " + bpa.toString());
			String bpaStatus = bpa.getStatus();
//			log.info("bpaStatus: "+bpaStatus);
			Map<String, Object> requestBody = new HashMap<>();

//			log.info("bpa.getWorkflow().getAssignes(): " + bpa.getWorkflow().getAssignes());
//			log.info("bpa.getWorkflow().getAssignes().isEmpty() : " + bpa.getWorkflow().getAssignes().isEmpty());
//			log.info("bpa.getWorkflow().getAssignes().size(): " + bpa.getWorkflow().getAssignes().size());

			Map<String, Object> tokenResponse = getToken();

			Map<String, Object> additionalDetails = (Map<String, Object>) bpa.getAdditionalDetails();
			String swsServiceId = additionalDetails.get("swsServiceId").toString();

			requestBody.put("applicationNo", bpa.getSwsApplicationId().toString());
			requestBody.put("serviceId", swsServiceId);

			String apiUrl = "";

			if (bpaStatus.equalsIgnoreCase("REJECTED")) {
				requestBody.put("status", 12);
				requestBody.put("PaymentStatus", 1);
				requestBody.put("BankTransId", "0");
				requestBody.put("ChallanNo", "0");
				requestBody.put("PaymentAmount", "0");
				requestBody.put("Remarks", "");
				apiUrl = "https://swpstgapi.csmpl.com/IndustryService/UpdateApplicationProgressStatus";
			}

			if (bpaAction != null && bpaAction.equalsIgnoreCase("PAY")) {
				HttpHeaders paymentHeaders = new HttpHeaders();
//				headers.setContentType(MediaType.APPLICATION_JSON);
				paymentHeaders.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

				String paymentApiUrl = "";

				Map<String, Object> bpaTokenResponse = getBpaAuthToken(
						bpaRequest.getRequestInfo().getUserInfo().getMobileNumber(),
						bpaRequest.getRequestInfo().getUserInfo().getTenantId());

				if (bpaStatus.equalsIgnoreCase("APPROVED"))
					paymentApiUrl = "https://www.niwaspass.com/collection-services/payments/BPA.NC_SAN_FEE/_search?tenantId="
							+ bpa.getTenantId() + "&consumerCodes=" + bpa.getApplicationNo();
				else
					paymentApiUrl = "https://www.niwaspass.com/collection-services/payments/BPA.NC_APP_FEE/_search?tenantId="
							+ bpa.getTenantId() + "&consumerCodes=" + bpa.getApplicationNo();

				Map<String, Object> paymentRequestBody = new HashMap<>();

				if ((boolean) bpaTokenResponse.get("success")) {
					bpaRequest.getRequestInfo().setAuthToken(bpaTokenResponse.get("access_token").toString());
				}

				paymentRequestBody.put("RequestInfo", bpaRequest.getRequestInfo());

				HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(paymentRequestBody, paymentHeaders);

				log.info("requestEntity16 : " + requestEntity.toString());

				ResponseEntity<Map> response = restTemplate.exchange(paymentApiUrl, HttpMethod.POST, requestEntity,
						Map.class);

				Map<String, Object> responseBody = response.getBody();

//				log.info("response16 " + response.toString());

				List<Map<String, Object>> payments = (List<Map<String, Object>>) responseBody.get("Payments");

				if (payments != null && !payments.isEmpty()) {
					String challanNo = ((List<Map<String, Object>>) payments.get(0).get("paymentDetails")).get(0)
							.get("receiptNumber").toString();

					String paymentAmount = payments.get(0).get("totalAmountPaid").toString();

					String txnId = payments.get(0).get("transactionNumber").toString();
					Map<String, Object> paymentUpdateRequestBody = new HashMap<>();

					paymentUpdateRequestBody.put("status", 11);
					paymentUpdateRequestBody.put("PaymentStatus", 1);
					paymentUpdateRequestBody.put("BankTransId", txnId);
					paymentUpdateRequestBody.put("ChallanNo", challanNo);
					paymentUpdateRequestBody.put("PaymentAmount", paymentAmount);
					paymentUpdateRequestBody.put("Remarks", "Amount Paid");

					String paymentUpdateApiUrl = "https://swpstgapi.csmpl.com/IndustryService/UpdateApplicationProgressStatus";

					HttpHeaders paymentUpdateHeaders = new HttpHeaders();
					paymentUpdateHeaders.setContentType(MediaType.APPLICATION_JSON);
					paymentUpdateHeaders.set("Authorization", "Bearer " + tokenResponse.get("data"));

					HttpEntity<Map<String, Object>> paymentUpdaterequestEntity = new HttpEntity<>(
							paymentUpdateRequestBody, paymentUpdateHeaders);

					log.info("requestEntity17 : " + paymentUpdaterequestEntity.toString());

					ResponseEntity<String> paymentUpdateresponse = restTemplate.exchange(paymentUpdateApiUrl,
							HttpMethod.POST, paymentUpdaterequestEntity, String.class);

					log.info("response17 " + paymentUpdateresponse.toString());
				}

//				String paymentAmount = response.getBody().getJSONArray("Payments").getJSONObject(0)
//						.get("totalAmountPaid").toString();
//				String txnId = response.getBody().getJSONArray("Payments").getJSONObject(0)
//						.getString("transactionNumber");
//				String challanNo = response.getBody().getJSONArray("Payments").getJSONObject(0)
//						.getJSONArray("paymentDetails").getJSONObject(0).getString("receiptNumber");

			}

			if (bpaStatus.equalsIgnoreCase("APPROVED")) {
				String fileByte = getFileStoreId(bpaRequest);
				Map<String, Object> docListMap = new HashMap<>();
				docListMap.put("documentInByte", fileByte);
				List<Map<String, Object>> docList = new ArrayList<>();
				docList.add(docListMap);
				requestBody.put("documentList", docList);
				requestBody.put("status", 11);
				apiUrl = "https://swpstgapi.csmpl.com/IndustryService/UpdateApprovalDoc";
			}

			if (bpaAction != null && !bpaAction.equalsIgnoreCase("PAY")) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set("Authorization", "Bearer " + tokenResponse.get("data"));

				HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

				log.info("requestEntity15 : " + requestEntity.toString());

				ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
						String.class);

				log.info("response15 " + response.toString());
				return ResponseEntity.ok(response.getBody().toString());
			}
			return null;
		} catch (Exception ex) {
			String error = ex.toString();
			log.error("Error15: " + error);
			return ResponseEntity.ok(error);
		}
	}

	public ResponseEntity<String> updateStatusToSwsIntiatedApplication(BPARequest bpaRequest) {

		try {
//			String fileByte = getFileStoreId(bpaRequest);

			BPA bpa = bpaRequest.getBPA();
//			log.info("SWS BPA: "+bpa.toString());

			Map<String, Object> tokenResponse = getToken();

			Map<String, Object> requestBody = new HashMap<>();
			Map<String, Object> additionalDetails = (Map<String, Object>) bpa.getAdditionalDetails();
			String swsServiceId = additionalDetails.get("swsServiceId").toString();
//		requestBody.put("swsAuthToken", swsAuthToken);
			requestBody.put("status", 10);
			requestBody.put("applicationNo", bpa.getSwsApplicationId().toString());
			requestBody.put("serviceId", swsServiceId);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + tokenResponse.get("data"));

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

			log.info("requestEntity14 : " + requestEntity.toString());
			String apiUrl = "https://swpstgapi.csmpl.com/IndustryService/UpdateApplyStatus";
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

			log.info("response14 " + response.toString());
			return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
//			String error = ex.toString();
			log.error("Error14: " + ex.toString());
			return ResponseEntity.ok(ex.toString());
		}
	}

	public int getSwsStatusCode(String bpaStatus) {

		if (bpaStatus.equalsIgnoreCase("INITIATED") || bpaStatus.equalsIgnoreCase("CITIZEN_APPROVAL_INPROCESS")
				|| bpaStatus.equalsIgnoreCase("INPROGRESS") || bpaStatus.equalsIgnoreCase("PENDING_APPL_FEE")) {
			return 11;
		} else if (bpaStatus.equalsIgnoreCase("DOC_VERIFICATION_INPROGRESS_BY_ENGINEER")
				|| bpaStatus.equalsIgnoreCase("DOC_VERIFICATION_INPROGRESS_BY_BUILDER")
				|| bpaStatus.equalsIgnoreCase("APPROVAL_INPROGRESS")
				|| bpaStatus.equalsIgnoreCase("POST_FEE_APPROVAL_INPROGRESS")
				|| bpaStatus.equalsIgnoreCase("POST_FEE_APPROVAL_INPROGRESS_BY_BUILDER")
				|| bpaStatus.equalsIgnoreCase("PENDING_SANC_FEE_PAYMENT")) {
			return 1;
		} else if (bpaStatus.equalsIgnoreCase("APPROVED")) {
			return 5;
		} else if (bpaStatus.equalsIgnoreCase("REJECTED")) {
			return 6;
		} else if (bpaStatus.equalsIgnoreCase("CITIZEN_ACTION_PENDING_AT_DOC_VERIF")) {
			return 3;
		}

		return 0;
	}

	public String convertDate(Long millisecondDate) {
//		long milliseconds = 1686483704738L;  // Example milliseconds

		// Convert milliseconds to Instant
		Instant instant = Instant.ofEpochMilli(millisecondDate);

		// Define the desired date format and set the time zone to IST
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.withZone(ZoneId.of("Asia/Kolkata"));

		// Format the Instant to the desired string format
		String formattedDate = formatter.format(instant);

		return formattedDate;
	}

	public Map<String, Object> getRecieverUserDetails(BPARequest bpaRequest) {
		String assigneeUuid = bpaRequest.getBPA().getWorkflow().getAssignes().get(0);

		Map<String, Object> userReturn = new HashMap<>();

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("pageSize", 10);
		requestBody.put("RequestInfo", bpaRequest.getRequestInfo());

		List<String> uuidList = new ArrayList<>();
		uuidList.add(assigneeUuid);
		requestBody.put("uuid", uuidList);

		try {
			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

			log.info("requestEntity16 : " + requestEntity.toString());
			String apiUrl = "https://www.niwaspass.com/user/_search";
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

			log.info("response16 " + response.toString());
			JSONObject userResponse = new JSONObject(response.getBody().toString());
			String recieverName = userResponse.getJSONArray("user").getJSONObject(0).get("name").toString();
			String recieverDesignation = userResponse.getJSONArray("user").getJSONObject(0).get("type").toString();

			userReturn.put("name", recieverName);
			userReturn.put("designation", recieverDesignation);
			return userReturn;
//				return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
//				String error = ex.toString();
			log.error("Error16: " + ex.toString());
			userReturn.put("error", ex.toString());
			return userReturn;
//				return ResponseEntity.ok(ex.toString());
		}
	}

	public ResponseEntity<String> pushDatatoSws(BPARequest bpaRequest) {

		try {
			String fileByte = getFileStoreId(bpaRequest);

			BPA bpa = bpaRequest.getBPA();
			Map<String, Object> requestBody = new HashMap<>();
//		requestBody.put("swsAuthToken", swsAuthToken);
			requestBody.put("swsApplicationNo", bpa.getSwsApplicationId());
			requestBody.put("applicationNoInProject", bpa.getApplicationNo());
//		requestBody.put("districtId", "");
			requestBody.put("swsApplicationStatusCode", 1);
			requestBody.put("statusDescriptionInProject", "");
			requestBody.put("lastAction", "");
			requestBody.put("actionTakerName", "");
			requestBody.put("actionTakerDesignation", "");
			requestBody.put("applicationReceiverName", "");
			requestBody.put("applicationReceiverDesignation", "");
			requestBody.put("lastActionDate", "");
			requestBody.put("anyOtherRemark", "");
			requestBody.put("isCertificateProvided", 0);
			requestBody.put("projectCertificateNumber", "");
			requestBody.put("inspectionDate", "");
			requestBody.put("applicationSubmissionDate", "");
			requestBody.put("applicationResubmissionDate", "");
			requestBody.put("objectionDate", "");
			requestBody.put("rejectionDate", "");
			requestBody.put("approvalDate", "");
//		requestBody.put("isModelVerified", "");
//		requestBody.put("message", "");
			requestBody.put("certificateAmendmentNo", "");
			requestBody.put("isAnySubsidyProvided", 0);
			requestBody.put("subsidyAmountInRs", 0);
//		HttpHeaders headers = new HttpHeaders();
////		headers.setContentType(MediaType.APPLICATION_JSON);
//		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//		headers.set("swskey", "5227439299922");
//
//		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//		log.info("requestEntity : " + requestEntity.toString());
//		String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/SwsService/getswsprofile";
			return ResponseEntity.ok(fileByte);
		} catch (Exception ex) {
			String error = ex.toString();
			log.error("Error1: " + error);
			return ResponseEntity.ok(error);
		}
	}

	public ResponseEntity<String> pushDataToSwsDirectly(String requestData) {
		JSONObject data = new JSONObject(requestData);
		HashMap<String, Object> requestBody = jsonToMap(data);
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.set("swskey", "5227439299922");

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		log.info("requestEntity1 : " + requestEntity.toString());
		String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/SwsService/updatestatus";

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

			log.info("response " + response.toString());
			return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
			String error = ex.toString();
			log.error("Error2: " + error);
			return ResponseEntity.ok(error);
		}

	}

	public ResponseEntity<String> getStatusCodeList() {
		return null;
	}

	private HashMap<String, Object> jsonToMap(JSONObject jsonObject) {
		HashMap<String, Object> map = new HashMap<>();
		Iterator<String> keys = jsonObject.keys();

		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.get(key);

			if (value instanceof JSONArray) {
				value = jsonArrayToList((JSONArray) value);
			} else if (value instanceof JSONObject) {
				value = jsonToMap((JSONObject) value);
			}

			map.put(key, value);
		}

		return map;
	}

	private Object jsonArrayToList(JSONArray array) {
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = jsonArrayToList((JSONArray) value);
			} else if (value instanceof JSONObject) {
				value = jsonToMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	private String getFileStoreId(BPARequest bpaRequest) {
//		JSONObject data = new JSONObject(bpaRequest.toString1());
//		HashMap<String, Object> requestBody = jsonToMap(data);
		BPA bpa = bpaRequest.getBPA();
		BPASearchCriteria criteria = new BPASearchCriteria();
		String tenantId = bpa.getTenantId();
		criteria.setTenantId(tenantId);
		criteria.setApplicationNo(bpa.getApplicationNo());
		List<BPA> bpas = bpaService.search(criteria, bpaRequest.getRequestInfo());
		String edcrDetailsResponse = getEdcrDetails(bpas.get(0), bpaRequest.getRequestInfo());

		JSONObject edcrDetailsObject = new JSONObject(edcrDetailsResponse);
//		String edcrDetails = edcrDetailsObject.getJSONArray("edcrDetail").get(0).toString();
//		List<String> edcrList = new ArrayList<>();
//		edcrList.add(edcrDetails);

//		log.info("edcrDetails: " + edcrDetails.toString());

//		bpaRequest.getBPA().setEdcrDetail(edcrList);
//		bpaRequest.getBPA().setLandInfo(bpas.get(0).getLandInfo());
//		bpa.setEdcrDetail(edcrList);
		bpa.setLandInfo(bpas.get(0).getLandInfo());
		Map<String, Object> swsAdditionalDetail = new HashMap<>();
		JSONArray floors = edcrDetailsObject.getJSONArray("edcrDetail").getJSONObject(0).getJSONObject("planDetail")
				.getJSONArray("blocks").getJSONObject(0).getJSONObject("building").getJSONArray("floors");
		if (floors.length() > 1) {
			swsAdditionalDetail.put("gFloorBA",
					floors.getJSONObject(0).getJSONArray("occupancies").getJSONObject(0).get("builtUpArea"));
			swsAdditionalDetail.put("fFloorBA",
					floors.getJSONObject(1).getJSONArray("occupancies").getJSONObject(0).get("builtUpArea"));
		} else {
			swsAdditionalDetail.put("gFloorBA",
					floors.getJSONObject(0).getJSONArray("occupancies").getJSONObject(0).get("builtUpArea"));
		}

//		swsAdditionalDetail.put("gFloorBA",
//				edcrDetailsObject.getJSONArray("edcrDetail").getJSONObject(0).getJSONObject("planDetail")
//						.getJSONArray("blocks").getJSONObject(0).getJSONObject("building").getJSONArray("floors")
//						.getJSONObject(0).getJSONArray("occupancies").getJSONObject(0).get("builtUpArea"));

		bpa.setSwsAdditionalDetails(swsAdditionalDetail);

		Map<String, Object> requestBody = new HashMap<>();
//		log.info("bpaRequest: " + bpaRequest.toString());
		List<BPA> bpaList = new ArrayList<>();
		bpaList.add(bpa);
//		requestBody.put("Bpa", bpas);
		requestBody.put("Bpa", bpaList);
		requestBody.put("RequestInfo", bpaRequest.getRequestInfo());

		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//		HttpEntity<BPARequest> requestEntity = new HttpEntity<>(bpaRequest, headers);

		log.info("requestEntity2 : " + requestEntity.toString());
		String apiUrl = "https://www.niwaspass.com/pdf-service/v1/_create?tenantId=" + tenantId
				+ "&key=buildingpermit-sws";

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

//			log.info("response " + response.toString());
			JSONObject responseBody = new JSONObject(response.getBody().toString());
			String fileStoreId = responseBody.getJSONArray("filestoreIds").get(0).toString();
//			log.info("fileStoreId: " + fileStoreId);

			String fileByte = getPermitOrder(tenantId, fileStoreId,
					bpaRequest.getRequestInfo().getMsgId().split("|")[0]);
			return fileByte;
//			return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
			String error = ex.toString();
			log.error("Error3: " + error);
			return error;
		}
	}

	private String getPermitOrder(String tenantId, String fileStoreIds, String msgId) {
		HashMap<String, Object> requestBody = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

//		log.info("requestEntity3 : " + requestEntity.toString());
		String apiUrl = "https://www.niwaspass.com/filestore/v1/files/url?tenantId=" + tenantId + "&fileStoreIds="
				+ fileStoreIds + "&_=" + msgId;

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity,
					String.class);

//			log.info("response " + response.toString());
			JSONObject responseBody = new JSONObject(response.getBody().toString());
			String fileUrl = responseBody.getJSONArray("fileStoreIds").getJSONObject(0).get("url").toString();
//			log.info("fileUrl: " + fileUrl);
			String fileByte = getfileByte(fileUrl);

			return fileByte;
//			return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
			String error = ex.toString();
			log.error("Error4: " + error);
			return error;
		}
	}

	private String getfileByte(String fileUrl) {
//		String filePath = "C:\\Users\\Entit\\Downloads\\1718267934751IKVIxksWTp.pdf";

		try {
			byte[] fileBytes = downloadFileFromUrl(fileUrl);

			String base64String = encodeToBase64(fileBytes);

//			log.info("base64String: " + base64String);

			return base64String;
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	public static String encodeToBase64(byte[] fileBytes) {
		return Base64.getEncoder().encodeToString(fileBytes);
	}

	private static byte[] downloadFileFromUrl(String fileUrl) throws IOException {
		URL url = new URL(fileUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		try (InputStream inputStream = connection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}

			return baos.toByteArray();
		}
	}

	public String getEdcrDetails(BPA bpa, RequestInfo requestInfo) {
		HashMap<String, Object> requestBody = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		requestBody.put("RequestInfo", requestInfo);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

//		log.info("requestEntity3 : " + requestEntity.toString());
		String apiUrl = "https://www.niwaspass.com/edcr/rest/dcr/scrutinydetails?tenantId=" + bpa.getTenantId()
				+ "&edcrNumber=" + bpa.getEdcrNumber();

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

//			log.info("response " + response.toString());
//			JSONObject responseBody = new JSONObject(response.getBody().toString());
			String responseBody = response.getBody().toString();
//			String fileUrl = responseBody.getJSONArray("fileStoreIds").getJSONObject(0).get("url").toString();
//			log.info("fileUrl: " + fileUrl);
//			String fileByte = getfileByte(fileUrl);

			return responseBody;
//			return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
			String error = ex.toString();
			log.error("Error4: " + error);
			return error;
		}
	}

	public Map<String, Object> getToken() {

		Map<String, Object> responseBody = new HashMap<>();

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("grantType", "password");
		requestBody.put("username", "Urbanstg");
		requestBody.put("password", "password@128");
		requestBody.put("clientId", "Urbchceb");
		requestBody.put("deptId", 6);

		// Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// headers.set("Authorization", "Bearer your_access_token"); // if needed

		// Combine body + headers
		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		try {
			String apiUrl = "https://swpstgapi.csmpl.com/customAuth/token";

			// POST call
			ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);

			// Handle response
			if (response.getStatusCode().is2xxSuccessful()) {
				responseBody = response.getBody();

				log.info("Token: " + responseBody.get("data"));

				return responseBody;

			} else {
				log.error("API call failed with status: " + response.getStatusCode());
				responseBody.put("Error", "API call failed with status: " + response.getStatusCode());
				responseBody.put("success", "false");
				return responseBody;
			}

		} catch (Exception ex) {
			log.error("Error calling API: " + ex.getMessage());
			responseBody.put("Error", "Error calling API: " + ex.getMessage());
			responseBody.put("success", "false");
			return responseBody;
		}
	}

	private Map<String, Object> getBpaAuthToken(String mobileNumber, String tenantId) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		Map<String, Object> responseBody = new HashMap<>();

		formData.add("tenantId", tenantId);
		formData.add("username", mobileNumber);
		formData.add("password", "123456"); // or your actual name
		formData.add("userType", "CITIZEN");
		formData.add("scope", "read");
		formData.add("grant_type", "password");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//		headers.setBasicAuth("ZWdvdi11c2VyLWNsaWVudDo=");
		headers.set("authorization", "Basic ZWdvdi11c2VyLWNsaWVudDo=");

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
		try {
			String apiUrl = "https://www.niwaspass.com/user/oauth/token";

			// POST call
			ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);

			// Handle response
			if (response.getStatusCode().is2xxSuccessful()) {
				responseBody = response.getBody();
				responseBody.put("success", true);
				return responseBody;

			} else {
				log.error("API call failed with status: " + response.getStatusCode());
				responseBody.put("Error", "API call failed with status: " + response.getStatusCode());
				responseBody.put("success", "false");
				return responseBody;
			}

		} catch (Exception ex) {
			log.error("Error calling API: " + ex.getMessage());
			responseBody.put("Error", "Error calling API: " + ex.getMessage());
			responseBody.put("success", "false");
			return responseBody;
		}
	}
}
