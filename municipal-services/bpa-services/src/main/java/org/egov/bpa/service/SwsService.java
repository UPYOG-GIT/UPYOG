package org.egov.bpa.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
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
			log.error("Error: " + error);
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

		log.info("requestEntity : " + requestEntity.toString());
		String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/SwsService/updatestatus";

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
					String.class);

			log.info("response " + response.toString());
			return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
			String error = "Error: " + ex;
			log.error("Error: " + error);
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
		JSONObject data = new JSONObject(bpaRequest.toString1());
		HashMap<String, Object> requestBody = jsonToMap(data);
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		log.info("requestEntity : " + requestEntity.toString());
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
			String fileByte = getPermitOrder(bpaRequest.getBPA().getTenantId(), fileStoreId);
			return fileByte;
//			return ResponseEntity.ok(response.getBody().toString());
		} catch (Exception ex) {
			String error = ex.toString();
			log.error("Error: " + error);
			return error;
		}
	}

	private String getPermitOrder(String tenantId, String fileStoreIds) {
//		HashMap<String, Object> requestBody = jsonToMap(data);
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);

		log.info("requestEntity : " + requestEntity.toString());
		String apiUrl = "https://www.niwaspass.com/filestore/v1/files/url?tenantId=" + tenantId + "&fileStoreIds="
				+ fileStoreIds;

		try {
			// Make the API call using RestTemplatex1x
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
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
			log.error("Error: " + error);
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
