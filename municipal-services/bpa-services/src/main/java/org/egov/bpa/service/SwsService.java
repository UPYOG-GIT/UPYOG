package org.egov.bpa.service;

import java.util.ArrayList;
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

	public void pushDatatoSws(BPARequest bpaRequest) {

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
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.set("swskey", "5227439299922");

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		log.info("requestEntity : " + requestEntity.toString());
		String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/SwsService/getswsprofile";
	}

	public ResponseEntity<String> pushDataToSwsDirectly(String requestData) {
		JSONObject data = new JSONObject(requestData);
//		HashMap<String, Object> requestBody = jsonToMap(data);
		
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("swsApplicationNo", 112406110758169L);
		requestBody.put("applicationNoInProject", "CG-BP-2024-06-11-001974");
//		requestBody.put("districtId", "");
		requestBody.put("swsApplicationStatusCode", 5);
		requestBody.put("statusDescriptionInProject", "Approved");
		requestBody.put("lastAction", "");
		requestBody.put("actionTakerName", "");
		requestBody.put("actionTakerDesignation", "");
		requestBody.put("applicationReceiverName", "");
		requestBody.put("applicationReceiverDesignation", "");
		requestBody.put("lastActionDate", "2024-06-11T12:01:44.738Z");
		requestBody.put("anyOtherRemark", "");
		requestBody.put("isCertificateProvided", 1);
		requestBody.put("projectCertificateNumber", "CG-BP-2024-06-11-000502");
		requestBody.put("inspectionDate", "");
		requestBody.put("applicationSubmissionDate", "");
		requestBody.put("applicationResubmissionDate", "");
		requestBody.put("objectionDate", "");
		requestBody.put("rejectionDate", "");
		requestBody.put("approvalDate", "2024-06-11T12:01:44.738Z");
		requestBody.put("isModelVerified", false);
		requestBody.put("message", "");
		requestBody.put("certificateAmendmentNo", 0);
		requestBody.put("isAnySubsidyProvided", 0);
		requestBody.put("subsidyAmountInRs", 0);
		
		
		List<Map<String, Object>> documentList = new ArrayList<>();
		Map<String, Object> documentInByteMap = new HashMap<>();
		documentInByteMap.put("documentInByte", "[B@15db9742");

		documentList.add(documentInByteMap);
		
		requestBody.put("documentList", documentList);
		
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
}
