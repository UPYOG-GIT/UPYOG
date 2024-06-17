package org.egov.bpa.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
import org.egov.bpa.web.model.user.UserDetailResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SwsService {

	@Autowired
	private RestTemplate restTemplate;

	public ResponseEntity<String> updateStatusToSws(BPARequest bpaRequest) {

		try {
//			log.info("bpaRequest : "+bpaRequest.toString());
			BPA bpa = bpaRequest.getBPA();
			log.info("SWS bpa : " + bpa.toString());
			String bpaStatus = bpa.getStatus();
//			log.info("bpaStatus: "+bpaStatus);
			int swsStatusCode = getSwsStatusCode(bpaStatus);
//			log.info("swsStatusCode: "+swsStatusCode);
			String modifiedDate = convertDate(bpa.getAuditDetails().getLastModifiedTime());
//			log.info("modifiedDate: "+modifiedDate);
			String createdDate = convertDate(bpa.getAuditDetails().getCreatedTime());
//			log.info("createdDate: "+createdDate);
			String recieverName = "Citizen";
			String recieverDesignation = "Citizen";
			Map<String, Object> requestBody = new HashMap<>();

			log.info("bpa.getWorkflow().getAssignes(): " + bpa.getWorkflow().getAssignes());
			log.info("bpa.getWorkflow().getAssignes().isEmpty() : " + bpa.getWorkflow().getAssignes().isEmpty());
			log.info("bpa.getWorkflow().getAssignes().size(): " + bpa.getWorkflow().getAssignes().size());
			if (bpa.getWorkflow().getAssignes() != null || !bpa.getWorkflow().getAssignes().isEmpty()
					|| bpa.getWorkflow().getAssignes().size() != 0) {
				log.info("inside if in sws");
				Map<String, Object> userResponse = getRecieverUserDetails(bpaRequest);

				log.info("userResponse: " + userResponse.toString());
				if (!userResponse.containsKey("error")) {
					recieverName = userResponse.get("name").toString();
					recieverDesignation = userResponse.get("designation").toString();
				}
			}
//		requestBody.put("swsAuthToken", swsAuthToken);
			requestBody.put("swsApplicationNo", bpa.getSwsApplicationId());
			requestBody.put("applicationNoInProject", bpa.getApplicationNo());
			requestBody.put("districtId", "");
			requestBody.put("swsApplicationStatusCode", swsStatusCode);
			requestBody.put("statusDescriptionInProject", bpaStatus);
			requestBody.put("lastAction", bpa.getWorkflow().getAction());
			requestBody.put("actionTakerName", bpaRequest.getRequestInfo().getUserInfo().getName());
			requestBody.put("actionTakerDesignation", bpaRequest.getRequestInfo().getUserInfo().getType());
			requestBody.put("applicationReceiverName", recieverName);
			requestBody.put("applicationReceiverDesignation", recieverDesignation);
			requestBody.put("lastActionDate", modifiedDate);
			requestBody.put("anyOtherRemark", "");
			requestBody.put("isCertificateProvided", bpaStatus.equalsIgnoreCase("APPROVED") ? 1 : 0);
			requestBody.put("projectCertificateNumber",
					bpaStatus.equalsIgnoreCase("APPROVED") ? bpa.getApplicationNo() : "");
			requestBody.put("inspectionDate", "");
			requestBody.put("applicationSubmissionDate", "");
			requestBody.put("applicationResubmissionDate", "");
			requestBody.put("objectionDate", swsStatusCode == 3 ? modifiedDate : "");
			requestBody.put("rejectionDate", bpaStatus.equalsIgnoreCase("REJECTED") ? modifiedDate : "");
			requestBody.put("approvalDate", bpaStatus.equalsIgnoreCase("APPROVED") ? modifiedDate : "");
			requestBody.put("isModelVerified", "");
			requestBody.put("message", "");
			requestBody.put("certificateAmendmentNo", "");
			requestBody.put("isAnySubsidyProvided", 0);
			requestBody.put("subsidyAmountInRs", 0);

			if (bpaStatus.equalsIgnoreCase("APPROVED")) {
				String fileByte = getFileStoreId(bpaRequest);
				Map<String, Object> docListMap = new HashMap<>();
				docListMap.put("documentInByte", fileByte);
				List<Map<String, Object>> docList = new ArrayList<>();
				docList.add(docListMap);
				requestBody.put("documentList", docList);

			} else {
				requestBody.put("documentList", "");
			}
			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set("swskey", "5227439299922");

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

			log.info("requestEntity15 : " + requestEntity.toString());
			String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/SwsService/updatestatus";
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

			log.info("response15 " + response.toString());
			return ResponseEntity.ok(response.getBody().toString());
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
			Map<String, Object> requestBody = new HashMap<>();
//		requestBody.put("swsAuthToken", swsAuthToken);
			requestBody.put("swsApplicationNo", bpa.getSwsApplicationId());
			requestBody.put("applicationNoInProject", bpa.getApplicationNo());
			requestBody.put("districtId", "");
			requestBody.put("swsApplicationStatusCode", 11);
			requestBody.put("statusDescriptionInProject", bpa.getWorkflow().getAction());
			requestBody.put("lastAction", bpa.getStatus());
			requestBody.put("actionTakerName", bpaRequest.getRequestInfo().getUserInfo().getName());
			requestBody.put("actionTakerDesignation", "Architect");
			requestBody.put("applicationReceiverName", bpa.getLandInfo().getOwners().get(0).getName());
			requestBody.put("applicationReceiverDesignation", "Citizen");
			requestBody.put("lastActionDate", convertDate(bpa.getAuditDetails().getLastModifiedTime()));
			requestBody.put("anyOtherRemark", "");
			requestBody.put("isCertificateProvided", 0);
			requestBody.put("projectCertificateNumber", "");
			requestBody.put("inspectionDate", "");
			requestBody.put("applicationSubmissionDate", convertDate(bpa.getAuditDetails().getCreatedTime()));
			requestBody.put("applicationResubmissionDate", "");
			requestBody.put("objectionDate", "");
			requestBody.put("rejectionDate", "");
			requestBody.put("approvalDate", "");
			requestBody.put("isModelVerified", false);
			requestBody.put("message", "");
			requestBody.put("documentList", "");
			requestBody.put("certificateAmendmentNo", "");
			requestBody.put("isAnySubsidyProvided", 0);
			requestBody.put("subsidyAmountInRs", 0);
			HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set("swskey", "5227439299922");

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

			log.info("requestEntity14 : " + requestEntity.toString());
			String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/SwsService/updatestatus";
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
		Map<String, Object> requestBody = new HashMap<>();

		List<BPA> bpaList = new ArrayList<>();
		bpaList.add(bpaRequest.getBPA());
		requestBody.put("Bpa", bpaList);
		requestBody.put("RequestInfo", bpaRequest.getRequestInfo());

		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//		HttpEntity<BPARequest> requestEntity = new HttpEntity<>(bpaRequest, headers);

		log.info("requestEntity2 : " + requestEntity.toString());
		String apiUrl = "https://www.niwaspass.com/pdf-service/v1/_create?tenantId=" + bpaRequest.getBPA().getTenantId()
				+ "&key=buildingpermit";

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

//			log.info("response " + response.toString());
			JSONObject responseBody = new JSONObject(response.getBody().toString());
			String fileStoreId = responseBody.getJSONArray("filestoreIds").get(0).toString();
			log.info("fileStoreId: " + fileStoreId);

			String fileByte = getPermitOrder(bpaRequest.getBPA().getTenantId(), fileStoreId,
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

		log.info("requestEntity3 : " + requestEntity.toString());
		String apiUrl = "https://www.niwaspass.com/filestore/v1/files/url?tenantId=" + tenantId + "&fileStoreIds="
				+ fileStoreIds + "&_=" + msgId;

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity,
					String.class);

//			log.info("response " + response.toString());
			JSONObject responseBody = new JSONObject(response.getBody().toString());
			String fileUrl = responseBody.getJSONArray("fileStoreIds").getJSONObject(0).get("url").toString();
			log.info("fileUrl: " + fileUrl);
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

			log.info("base64String: " + base64String);

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
}
