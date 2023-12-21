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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
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
			metrics.put("permitIssued", List.of(occupancy, permits));

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
		    
		    
		

		    public Map<String, Object> pushDataToApi(String apiUrl, RequestInfo requestInfo){
		                    
		    	IngestRequest body = getIngestData();
		    	System.out.println("bodyy---====" + body);
		    	ingestRequest.setRequestInfo(requestInfo);
		    	
		    	 HttpHeaders headers = new HttpHeaders();
		    	    headers.setContentType(MediaType.APPLICATION_JSON);
		    	    
		    	    HttpEntity<IngestRequest> requestEntity = new HttpEntity<IngestRequest>(body, headers);
		    	    
		    	    ResponseEntity<Map> responseEntity = this.restTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);
		    	    
		    	    Map<String, Object> responseBody = responseEntity.getBody();
		    	    return responseBody;
		        
		       
		    	
		    	
		    }
}
