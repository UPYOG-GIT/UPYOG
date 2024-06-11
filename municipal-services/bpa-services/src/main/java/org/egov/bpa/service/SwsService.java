package org.egov.bpa.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
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
	
	 public static HashMap<String, Object> jsonToMap(JSONObject jsonObject) {
	        HashMap<String, Object> map = new HashMap<>();

	        Iterator<String> keys = jsonObject.keys();
	        while (keys.hasNext()) {
	            String key = keys.next();
	            Object value = jsonObject.get(key);
	            map.put(key, value);
	        }

	        return map;
	    }
}
