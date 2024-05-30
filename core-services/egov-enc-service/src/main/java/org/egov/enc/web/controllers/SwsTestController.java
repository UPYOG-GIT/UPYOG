package org.egov.enc.web.controllers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/sws")
public class SwsTestController {

	@Autowired
	private RestTemplate restTemplate;

	@PostMapping("/_getprofile")
	public ResponseEntity<String> getProfile(@RequestBody String swsAuthToken) {

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("swsAuthToken", swsAuthToken);
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.set("swskey", "5227439299922");

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		log.info("requestEntity : " + requestEntity.toString());
		String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/SwsService/getswsprofile";

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
	
	
	@PostMapping("/_getapplicationdetails")
	public ResponseEntity<String> getApplicationDetails(@RequestBody JSONObject data) {

		Map<String, Object> requestBody = new HashMap<>();
//		requestBody.put("applicationNumber", "112405250417235");
		requestBody.put("applicationNumber", data.get("applicationNumber").toString());
		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.setBearerAuth(data.get("swsAuthToken").toString());
		headers.set("swskey", "5227439299922");

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		log.info("requestEntity : " + requestEntity.toString());
		String apiUrl = "https://industries.cg.gov.in/swschhattisgarhserviceapi/api/UnitRegistration/getapplicationdetail";

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
}


@Configuration
class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
