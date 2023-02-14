package org.egov.bpa.web.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.bpa.service.BPAService;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPAResponse;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.PayTypeFeeDetailRequest;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bpa")
public class BPAController {

	@Autowired
	private BPAService bpaService;

	@Autowired
	private BPAUtil bpaUtil;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@PostMapping(value = "/_create")
	public ResponseEntity<BPAResponse> create(@Valid @RequestBody BPARequest bpaRequest) {
		bpaUtil.defaultJsonPathConfig();
		BPA bpa = bpaService.create(bpaRequest);
		List<BPA> bpas = new ArrayList<BPA>();
		bpas.add(bpa);
		BPAResponse response = BPAResponse.builder().BPA(bpas)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(bpaRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_update")
	public ResponseEntity<BPAResponse> update(@Valid @RequestBody BPARequest bpaRequest) {
		BPA bpa = bpaService.update(bpaRequest);
		List<BPA> bpas = new ArrayList<BPA>();
		bpas.add(bpa);
		BPAResponse response = BPAResponse.builder().BPA(bpas)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(bpaRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping(value = "/_search")
	public ResponseEntity<BPAResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute BPASearchCriteria criteria) {

		List<BPA> bpas = bpaService.search(criteria, requestInfoWrapper.getRequestInfo());
		int count = bpaService.getBPACount(criteria, requestInfoWrapper.getRequestInfo());
		BPAResponse response = BPAResponse
				.builder().BPA(bpas).responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.count(count).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_permitorderedcr")
	public ResponseEntity<Resource> getPdf(@Valid @RequestBody BPARequest bpaRequest) {

		Path path = Paths.get(BPAConstants.EDCR_PDF);
		Resource resource = null;

		bpaService.getEdcrPdf(bpaRequest);
		try {
			resource = new UrlResource(path.toUri());
		} catch (Exception ex) {
			throw new CustomException(BPAErrorConstants.UNABLE_TO_DOWNLOAD, "Unable to download the file");
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + BPAConstants.EDCR_PDF + "\"")
				.body(resource);
	}

	@PostMapping(value = "/_plainsearch")
	public ResponseEntity<BPAResponse> plainSearch(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute BPASearchCriteria criteria) {

		List<BPA> bpas = bpaService.plainSearch(criteria, requestInfoWrapper.getRequestInfo());
		BPAResponse response = BPAResponse.builder().BPA(bpas).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_paytype")
	public ResponseEntity<List<Map<String,Object>>> getPayTypeByTenantId(@RequestParam String tenantId) {
//		@RequestBody RequestInfo requestInfo,
		List<Map<String,Object>> sqlResponseList = bpaService.getPayTypeByTenantId(tenantId);
		List<Map<String,Object>> responseList=new ArrayList<>();
		
		for(Map<String,Object> response:sqlResponseList) {
			Map<String,Object> responseMap=new HashMap<String,Object>();
			responseMap.put("code", response.get("charges_type_name"));
			responseMap.put("value", response.get("id"));
			responseList.add(responseMap);
		}
		return new ResponseEntity<>(responseList, HttpStatus.OK);
//		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_createfeedetail")
	public ResponseEntity<List<Map<String,Object>>> createFeeDetails(@RequestBody List<PayTypeFeeDetailRequest> payTypeFeeDetailRequest) {
//		@RequestBody RequestInfo requestInfo,
//		List<Map<String,Object>> responseList = bpaService.getPayTypeByTenantId(tenantId);
//		return new ResponseEntity<>(responseList, HttpStatus.OK);
		bpaService.createFeeDetail(payTypeFeeDetailRequest);
		return null;
	}
	
	@PostMapping(value = "/_updatefeedetail")
	public ResponseEntity<List<Map<String,Object>>> updateFeeDetails(@RequestParam String tenantId) {
//		@RequestBody RequestInfo requestInfo,
//		List<Map<String,Object>> responseList = bpaService.getPayTypeByTenantId(tenantId);
//		return new ResponseEntity<>(responseList, HttpStatus.OK);
		return null;
	}
	
	@PostMapping(value = "/_deletefeedetail")
	public ResponseEntity<List<Map<String,Object>>> deleteFeeDetails(@RequestParam String tenantId) {
//		@RequestBody RequestInfo requestInfo,
//		List<Map<String,Object>> responseList = bpaService.getPayTypeByTenantId(tenantId);
//		return new ResponseEntity<>(responseList, HttpStatus.OK);
		return null;
	}

}
