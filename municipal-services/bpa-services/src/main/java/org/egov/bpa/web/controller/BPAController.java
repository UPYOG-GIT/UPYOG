package org.egov.bpa.web.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.bpa.service.BPAService;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.model.BCategoryRequest;
import org.egov.bpa.web.model.BCategoryRequestWrapper;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPAResponse;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.BSCategoryRequest;
import org.egov.bpa.web.model.BSCategoryRequestWrapper;
import org.egov.bpa.web.model.PayTpRateRequest;
import org.egov.bpa.web.model.PayTpRateRequestWrapper;
import org.egov.bpa.web.model.PayTypeFeeDetailRequest;
import org.egov.bpa.web.model.PayTypeFeeDetailRequestWrapper;
import org.egov.bpa.web.model.PayTypeRequest;
import org.egov.bpa.web.model.PayTypeRequestWrapper;
import org.egov.bpa.web.model.ProposalTypeRequest;
import org.egov.bpa.web.model.ProposalTypeRequestWrapper;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.bpa.web.model.SlabMasterRequest;
import org.egov.bpa.web.model.SlabMasterRequestWrapper;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	

	@PostMapping(value = "/_createpaytype")
	public ResponseEntity<Object> createPayType(@RequestBody PayTypeRequestWrapper payTypeRequestWrapper) {
//		List<Map<String,Object>> responseList = bpaService.getPayTypeByTenantId(tenantId);
//		return new ResponseEntity<>(responseList, HttpStatus.OK);
		try {
			PayTypeRequest payTypeRequest = payTypeRequestWrapper.getPayTypeRequest();
			int insertResult = bpaService.createPayType(payTypeRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}
	
	@PostMapping(value = "/_paytype")
	public ResponseEntity<List<Map<String, Object>>> getPayTypeByTenantId(@RequestParam String tenantId) {
//		@RequestBody RequestInfo requestInfo,
		List<Map<String, Object>> sqlResponseList = bpaService.getPayTypeByTenantId(tenantId);
//		List<Map<String,Object>> responseList=new ArrayList<>();
//		
//		for(Map<String,Object> response:sqlResponseList) {
//			Map<String,Object> responseMap=new HashMap<String,Object>();
//			responseMap.put("code", response.get("charges_type_name"));
//			responseMap.put("value", response.get("id"));
//			responseList.add(responseMap);
//		}
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
//		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_updatepaytype")
	public ResponseEntity<Object> updatePayType(@RequestBody PayTypeRequestWrapper payTypeRequestWrapper) {
		PayTypeRequest payTypeRequest = payTypeRequestWrapper.getPayTypeRequest();
		int updateResult = bpaService.updatePayType(payTypeRequest);
		return new ResponseEntity<>(updateResult, HttpStatus.OK);
	}

	@PostMapping(value = "/_createfeedetail")
	public ResponseEntity<Object> createFeeDetails(
			@RequestBody PayTypeFeeDetailRequestWrapper payTypeFeeDetailRequestWrapper) {
		try {
			PayTypeFeeDetailRequest payTypeFeeDetailRequest = payTypeFeeDetailRequestWrapper.getPayTypeFeeDetailRequest();
			
			int[] insertResult = bpaService.createFeeDetail(payTypeFeeDetailRequest);
			if (insertResult.length > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_updatefeedetail")
	public ResponseEntity<Object> updateFeeDetails(@RequestBody PayTypeFeeDetailRequestWrapper payTypeFeeDetailRequestWrapper) {
		PayTypeFeeDetailRequest payTypeFeeDetailRequest = payTypeFeeDetailRequestWrapper.getPayTypeFeeDetailRequest();
		int updateResult = bpaService.updateFeeDetails(payTypeFeeDetailRequest);
		return new ResponseEntity<>(updateResult, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_searchfeedetails")
	public ResponseEntity<List<Map<String, Object>>> getFeeDetails(@RequestParam String tenantId, @RequestParam String applicationNo,@RequestParam int id) {
		List<Map<String, Object>> sqlResponseList = bpaService.getFeeDetails(tenantId, applicationNo, id);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}
	@PostMapping(value = "/_deletefeedetail")
	public ResponseEntity<List<Map<String, Object>>> deleteFeeDetails(@RequestParam String tenantId) {
//		@RequestBody RequestInfo requestInfo,
//		List<Map<String,Object>> responseList = bpaService.getPayTypeByTenantId(tenantId);
//		return new ResponseEntity<>(responseList, HttpStatus.OK);
		return null;
	}

	@PostMapping(value = "/_createproposaltype")
	public ResponseEntity<Object> createProposalType(
			@RequestBody ProposalTypeRequestWrapper proposalTypeRequestWrapper) {
//		@RequestBody RequestInfo requestInfo,
//		List<Map<String,Object>> responseList = bpaService.getPayTypeByTenantId(tenantId);
//		return new ResponseEntity<>(responseList, HttpStatus.OK);
		try {
			ProposalTypeRequest proposalTypeRequest = proposalTypeRequestWrapper.getProposalTypeRequest();
			int insertResult = bpaService.createProposalType(proposalTypeRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_searchproposaltype")
	public ResponseEntity<List<Map<String, Object>>> getProposalTypeByTenantId(@RequestParam String tenantId) {
		List<Map<String, Object>> sqlResponseList = bpaService.getProposalTypeByTenantId(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}

	@PostMapping(value = "/_updateproposaltype")
	public ResponseEntity<Object> updateProposalType(@RequestBody ProposalTypeRequestWrapper proposalTypeRequestWrapper) {
		ProposalTypeRequest proposalTypeRequest = proposalTypeRequestWrapper.getProposalTypeRequest();
		int updateResult = bpaService.updateProposalType(proposalTypeRequest);
		return new ResponseEntity<>(updateResult, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_createbcategory")
	public ResponseEntity<Object> createBCategory(@RequestBody BCategoryRequestWrapper bCategoryRequestWrapper) {
		try {
			BCategoryRequest bCategoryRequest = bCategoryRequestWrapper.getBCategoryRequest();
			int insertResult = bpaService.createBCategory(bCategoryRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_searchbcategory")
	public ResponseEntity<List<Map<String, Object>>> getBCategoryByTenantId(@RequestParam String tenantId) {
		List<Map<String, Object>> sqlResponseList = bpaService.getBCategoryByTenantId(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_updatebcategory")
	public ResponseEntity<Object> updateBCategory(@RequestBody BCategoryRequestWrapper bCategoryRequestWrapper) {
		BCategoryRequest bCategoryRequest = bCategoryRequestWrapper.getBCategoryRequest();
		int updateResult = bpaService.updateBCategory(bCategoryRequest);
		return new ResponseEntity<>(updateResult, HttpStatus.OK);
	}

	@PostMapping(value = "/_createbscategory")
	public ResponseEntity<Object> createBSCategory(@RequestBody BSCategoryRequestWrapper bSCategoryRequestWrapper) {
		try {
			BSCategoryRequest bSCategoryRequest = bSCategoryRequestWrapper.getBSCategoryRequest();
			int insertResult = bpaService.createBSCategory(bSCategoryRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_searchbscategory")
	public ResponseEntity<List<Map<String, Object>>> getBSCategoryByTenantId(@RequestParam String tenantId,
			@RequestParam int catId) {
		List<Map<String, Object>> sqlResponseList = bpaService.getBSCategoryByTenantId(tenantId, catId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_updatebscategory")
	public ResponseEntity<Object> updateBSCategory(@RequestBody BSCategoryRequestWrapper bSCategoryRequestWrapper) {
		BSCategoryRequest bSCategoryRequest = bSCategoryRequestWrapper.getBSCategoryRequest();
		int updateResult = bpaService.updateBSCategory(bSCategoryRequest);
		return new ResponseEntity<>(updateResult, HttpStatus.OK);
	}

	@PostMapping(value = "/_createpaytprate")
	public ResponseEntity<Object> createPayTpRate(@RequestBody PayTpRateRequestWrapper payTpRateRequestWrapper) {
		try {
			PayTpRateRequest payTpRateRequest = payTpRateRequestWrapper.getPayTpRateRequest();
			int insertResult = bpaService.createPayTpRate(payTpRateRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_searchpaytprate")
	public ResponseEntity<List<Map<String, Object>>> getPayTpRateByTenantIdAndTypeId(@RequestParam String tenantId,
			@RequestParam int typeId) {
		List<Map<String, Object>> sqlResponseList = bpaService.getPayTpRateByTenantIdAndTypeId(tenantId, typeId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}

	@PostMapping(value = "/_deletepaytprate")
	public ResponseEntity<Object> deletePayTpRateById(@RequestParam int id) {
		int deleteResult = bpaService.deletePayTpRateById(id);
		return new ResponseEntity<>(deleteResult, HttpStatus.OK);
	}

	@PostMapping(value = "/_createslab")
	public ResponseEntity<Object> createSlabMaster(@RequestBody SlabMasterRequestWrapper slabMasterRequestWrapper) {
		try {
			SlabMasterRequest slabMasterRequest = slabMasterRequestWrapper.getSlabMasterRequest();
			int insertResult = bpaService.createSlabMaster(slabMasterRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_searchslab")
	public ResponseEntity<List<Map<String, Object>>> getSlabMasterByTenantIdAndTypeId(@RequestParam String tenantId,
			@RequestParam int typeId) {
		List<Map<String, Object>> sqlResponseList = bpaService.getSlabMasterByTenantIdAndTypeId(tenantId, typeId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_deleteslab")
	public ResponseEntity<Object> deleteSlabMasterById(@RequestParam int id) {
		int deleteResult = bpaService.deleteSlabMasterById(id);
		return new ResponseEntity<>(deleteResult, HttpStatus.OK);
	}
}
