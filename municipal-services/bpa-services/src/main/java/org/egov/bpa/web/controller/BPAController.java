package org.egov.bpa.web.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.service.BPAService;
import org.egov.bpa.service.NationalDashboardService;
import org.egov.bpa.service.SwsService;
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
import org.egov.bpa.web.model.IngestRequest;
import org.egov.bpa.web.model.NdbResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
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
	private NationalDashboardService nationalDashboardService;

	@Autowired
	private BPAUtil bpaUtil;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	BPAConfiguration bpaConfig;

	@Autowired
	SwsService swsService;

	LocalDate startDate = LocalDate.of(2023, 5, 28);
	LocalDate endDate = LocalDate.of(2024, 2, 15);
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
		log.info("bpa---" + bpa.getApprovalNo());
		List<BPA> bpas = new ArrayList<BPA>();
		bpas.add(bpa);
		BPAResponse response = BPAResponse.builder().BPA(bpas)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(bpaRequest.getRequestInfo(), true))
				.build();
		log.info("Response Approval number----" + response.getBPA().get(0).getApprovalNo());
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
		try {
			PayTypeRequest payTypeRequest = payTypeRequestWrapper.getPayTypeRequest();
			int updateResult = bpaService.updatePayType(payTypeRequest);
			if (updateResult > 0) {
				return new ResponseEntity<>(updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(updateResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in updatePayType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/_createfeedetail")
	public ResponseEntity<Object> createFeeDetails(
			@RequestBody PayTypeFeeDetailRequestWrapper payTypeFeeDetailRequestWrapper) {
		try {
			PayTypeFeeDetailRequest payTypeFeeDetailRequest = payTypeFeeDetailRequestWrapper
					.getPayTypeFeeDetailRequest();

			int insertResult = bpaService.createFeeDetail(payTypeFeeDetailRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createFeeDetails: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_updatefeedetail")
	public ResponseEntity<Object> updateFeeDetails(
			@RequestBody PayTypeFeeDetailRequestWrapper payTypeFeeDetailRequestWrapper) {
		try {
			PayTypeFeeDetailRequest payTypeFeeDetailRequest = payTypeFeeDetailRequestWrapper
					.getPayTypeFeeDetailRequest();
			int updateResult = bpaService.updateFeeDetails(payTypeFeeDetailRequest);
			if (updateResult > 0) {
				return new ResponseEntity<>(updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(updateResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in updateFeeDetails: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/_searchfeedetails")
	public ResponseEntity<List<Map<String, Object>>> getFeeDetails(@RequestParam String applicationNo) {
//		try {
		List<Map<String, Object>> sqlResponseList = bpaService.getFeeDetails(applicationNo);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
//		} catch (Exception ex) {
//			log.error("Exception in getFeeDetails: " + ex);
//			List errorList = new ArrayList<Map<String, Object>>();
//			errorList.add(new HashMap<String, Object>().put("Error", ex.toString()));
//			return new ResponseEntity<>(errorList, HttpStatus.BAD_REQUEST);
//		}
	}

	@PostMapping(value = "/_deletefeedetail")
	public ResponseEntity<Object> deleteFeeDetails(
			@RequestBody PayTypeFeeDetailRequestWrapper payTypeFeeDetailRequestWrapper) {
		try {
			PayTypeFeeDetailRequest payTypeFeeDetailRequest = payTypeFeeDetailRequestWrapper
					.getPayTypeFeeDetailRequest();
			int deleteResult = bpaService.deleteFeeDetailsById(payTypeFeeDetailRequest.getIds(),
					payTypeFeeDetailRequest.getApplicationNo(), payTypeFeeDetailRequest.getFeeType());
			if (deleteResult > 0) {
				return new ResponseEntity<>(deleteResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(deleteResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in deleteFeeDetails: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/_verifyfeedetail")
	public ResponseEntity<Object> verifyFeeDetailsByApplicationNo(@RequestParam String applicationNo,
			@RequestParam String isVerified, @RequestParam String verifiedBy, @RequestParam String feeType) {
		try {
			int updateResult = bpaService.verifyFeeDetailsByApplicationNo(applicationNo, isVerified, verifiedBy,
					feeType);
			if (updateResult > 0) {
				return new ResponseEntity<>(updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(updateResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in verifyFeeDetailsByApplicationNo: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
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
			log.error("Exception in createProposalType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_searchproposaltype")
	public ResponseEntity<List<Map<String, Object>>> getProposalTypeByTenantId(@RequestParam String tenantId) {
//		try {
		List<Map<String, Object>> sqlResponseList = bpaService.getProposalTypeByTenantId(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
//		} catch (Exception ex) {
//			log.error("Exception in getProposalTypeByTenantId: " + ex);
//			List errorList = new ArrayList<Map<String, Object>>();
//			errorList.add(new HashMap<String, Object>().put("Error", ex.toString()));
//			return new ResponseEntity<>(errorList, HttpStatus.BAD_REQUEST);
//		}
	}

	@PostMapping(value = "/_updateproposaltype")
	public ResponseEntity<Object> updateProposalType(
			@RequestBody ProposalTypeRequestWrapper proposalTypeRequestWrapper) {
		try {
			ProposalTypeRequest proposalTypeRequest = proposalTypeRequestWrapper.getProposalTypeRequest();
			int updateResult = bpaService.updateProposalType(proposalTypeRequest);
			if (updateResult > 0) {
				return new ResponseEntity<>(updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(updateResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in updateProposalType: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
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
			log.error("Exception in createBCategory: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
//		return null;
	}

	@PostMapping(value = "/_searchbcategory")
	public ResponseEntity<List<Map<String, Object>>> getBCategoryByTenantId(@RequestParam String tenantId) {
//		try {
		List<Map<String, Object>> sqlResponseList = bpaService.getBCategoryByTenantId(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
//		} catch (Exception ex) {
//			log.error("Exception in getBCategoryByTenantId: " + ex);
//			List errorList = new ArrayList<Map<String, Object>>();
//			errorList.add(new HashMap<String, Object>().put("Error", ex.toString()));
//			return new ResponseEntity<>(errorList, HttpStatus.BAD_REQUEST);
//		}
	}

	@PostMapping(value = "/_updatebcategory")
	public ResponseEntity<Object> updateBCategory(@RequestBody BCategoryRequestWrapper bCategoryRequestWrapper) {
		try {
			BCategoryRequest bCategoryRequest = bCategoryRequestWrapper.getBCategoryRequest();
			int updateResult = bpaService.updateBCategory(bCategoryRequest);
			if (updateResult > 0) {
				return new ResponseEntity<>(updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(updateResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in updateBCategory: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
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
			log.error("Exception in createBSCategory: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/_searchbscategory")
	public ResponseEntity<List<Map<String, Object>>> getBSCategoryByTenantId(@RequestParam String tenantId,
			@RequestParam int catId) {
		List<Map<String, Object>> sqlResponseList = bpaService.getBSCategoryByTenantId(tenantId, catId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}

	@PostMapping(value = "/_updatebscategory")
	public ResponseEntity<Object> updateBSCategory(@RequestBody BSCategoryRequestWrapper bSCategoryRequestWrapper) {
		try {
			BSCategoryRequest bSCategoryRequest = bSCategoryRequestWrapper.getBSCategoryRequest();
			int updateResult = bpaService.updateBSCategory(bSCategoryRequest);
			if (updateResult > 0) {
				return new ResponseEntity<>(updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(updateResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in updateBSCategory: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/_createpaytprate")
	public ResponseEntity<Object> createPayTpRate(@RequestBody @Valid PayTpRateRequestWrapper payTpRateRequestWrapper) {
		try {
			PayTpRateRequest payTpRateRequest = payTpRateRequestWrapper.getPayTpRateRequest();
			int insertResult = bpaService.createPayTpRate(payTpRateRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createPayTpRate: " + ex);
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
	public ResponseEntity<Object> deletePayTpRateById(
			@RequestBody @Valid PayTpRateRequestWrapper payTpRateRequestWrapper) {
		try {
			PayTpRateRequest payTpRateRequest = payTpRateRequestWrapper.getPayTpRateRequest();
			int deleteResult = bpaService.deletePayTpRateById(payTpRateRequest.getIds());
			if (deleteResult > 0) {
				return new ResponseEntity<>(deleteResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(deleteResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in deletePayTpRateById: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
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
			log.error("Exception in createSlabMaster: " + ex);
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
	public ResponseEntity<Object> deleteSlabMasterById(@RequestBody SlabMasterRequestWrapper slabMasterRequestWrapper) {
		try {
			SlabMasterRequest slabMasterRequest = slabMasterRequestWrapper.getSlabMasterRequest();
			int deleteResult = bpaService.deleteSlabMasterById(slabMasterRequest.getIds());
			if (deleteResult > 0) {
				return new ResponseEntity<>(deleteResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(deleteResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in deleteSlabMasterById: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/_updatebillamount")
	public ResponseEntity<Object> updateBillAmount(@RequestParam String applicationNo,
			@RequestParam String businessService, @RequestParam String feeType) {
		try {
			int updateResult = bpaService.updateBillAmount(applicationNo, businessService, feeType);
			if (updateResult > 0) {
				return new ResponseEntity<>("Updated Successfully " + updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Total Bill Amount is already updated", HttpStatus.OK);
			}
		} catch (Exception ex) {
			log.error("Exception in updateBillAmount: " + ex);
			return new ResponseEntity<>("Exception while update bill amount", HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping(value = "/_deleteapplication")
	public ResponseEntity<Object> deleteApplication(@RequestParam String applicationNo) {
		try {
			int deleteResult = bpaService.deleteApplication(applicationNo);
			if (deleteResult > 0) {
				return new ResponseEntity<>(
						"Applicatio No : " + applicationNo + " is deleted, Result : " + deleteResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Applicatio No : " + applicationNo + " not found ", HttpStatus.OK);
			}
		} catch (Exception ex) {
			log.error("Exception in deleteApplication: " + ex);
			return new ResponseEntity<>("Exception while delete the Application : " + applicationNo,
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping(value = "/_applicationstepback")
	public ResponseEntity<Object> applicationStepBack(@RequestParam String applicationNo,
			@RequestParam String applicationStatus, @RequestParam int stepsBack) {
		try {
			int deleteResult = bpaService.applicationStepBack(applicationNo, applicationStatus, stepsBack);
			if (deleteResult > 0) {
				return new ResponseEntity<>("Application No : " + applicationNo + ", " + stepsBack + " step back",
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Applicatio No : " + applicationNo + " not found ", HttpStatus.OK);
			}
		} catch (Exception ex) {
			log.error("Exception in updateBillAmount: " + ex);
			return new ResponseEntity<>("Exception while step back the Application : " + applicationNo,
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping(value = "/dashboard/count")
	public ResponseEntity<List<Map<String, Object>>> getDataCountsForDashboard(String tenantId) {

		List<Map<String, Object>> sqlResponseList = bpaService.getDataCountsForDashboard(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);

	}

	@PostMapping(value = "/application/_search")
	public ResponseEntity<BPAResponse> searchByApplication(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute BPASearchCriteria criteria) {

		List<BPA> bpas = bpaService.applicationSearch(criteria, requestInfoWrapper.getRequestInfo());
		int count = bpaService.getBPACount(criteria, requestInfoWrapper.getRequestInfo());
		BPAResponse response = BPAResponse
				.builder().BPA(bpas).responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.count(count).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/applicationData")
	public ResponseEntity<BPAResponse> getApplicationDataInDasboardForUlb(
			@RequestBody RequestInfoWrapper requestInfoWrapper, @ModelAttribute BPASearchCriteria criteria) {
//	    List<Map<String, Object>> sqlResponseList = bpaService.getApplicationDataInDasboardForUlb(criteria);
//	    return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);

//		List<BPA> bpas = bpaService.getApplicationDataInDasboardForUlb(criteria);applicationDataSearch
		List<BPA> bpas = bpaService.applicationDataSearch(criteria, requestInfoWrapper.getRequestInfo());
//		int count = bpaService.getBPACount(criteria, requestInfoWrapper.getRequestInfo());
		BPAResponse response = BPAResponse.builder().BPA(bpas).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping(value = "/applicationData1")
	public ResponseEntity<List<Map<String, Object>>> getListOfApplications(String tenantId) {
		List<Map<String, Object>> sqlResponseList = bpaService.getListOfApplications(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);

	}

	@GetMapping(value = "/ingestData")
	public ResponseEntity<IngestRequest> getListOfIngestData(String date) {

//		List<IngestRequest> resultList = new ArrayList<>();

//		int loopCount = 0;

//		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//			String formattedDate = date.format(dateFormatter);
		IngestRequest request = nationalDashboardService.getIngestData(date);

//			System.out.println("requestt(((" + request.getIngestData().toString());

		// Create a new instance of IngestRequest for each iteration
//		IngestRequest newRequest = new IngestRequest();
//		newRequest.setIngestData(request.getIngestData()); // Assuming you need to copy the data
//
//		resultList.add(newRequest);
		int totalApplicationsSubmitted = request.getIngestData().stream()
				.mapToInt(item -> ((Number) item.getMetrics().get("applicationsSubmitted")).intValue()).sum();

		int totalApprovedApplications = request.getIngestData().stream()
				.mapToInt(item -> ((Number) item.getMetrics().get("todaysApprovedApplications")).intValue()).sum();

		log.info("totalApplicationsSubmitted: " + totalApplicationsSubmitted);
		log.info("totalApprovedApplications: " + totalApprovedApplications);
//			loopCount++;

//			System.out.println("Loop ran " + loopCount + " times.");
//		}

//		return new ResponseEntity<>(resultList, HttpStatus.OK);
		return new ResponseEntity<>(request, HttpStatus.OK);
	}

	@PostMapping(value = "/pushData")
	public ResponseEntity<NdbResponse> pushDataToApi(String apiUrl, String date) {

//		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//			String formattedDate1 = date.format(dateFormatter);

		// System.out.println("request----" + requestInfoWrapper.getRequestInfo());
//			String apiUrl = "https://upyog-test.niua.org/national-dashboard/metric/_ingest";
//	    try {
		NdbResponse response = nationalDashboardService.pushDataToApiManually(apiUrl, date);
		return new ResponseEntity<>(response, HttpStatus.OK);
//	    } catch (Exception e) {
//	       
//	        e.printStackTrace();
//	    
//	        return new ResponseEntity<>(Collections.singletonMap("error", "Failed to push data: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//
//	    }
//		}
//	        return null;
	}

	@PostMapping(value = "/pushdatatoswsmanual")
	public ResponseEntity<String> pushDatatoSwsManual(@RequestBody String dataRequest) {
		return swsService.pushDataToSwsDirectly(dataRequest);
//		return null;
	}

	@PostMapping(value = "/_pushdatatosws")
	public ResponseEntity<String> pushDatatoSws(@Valid @RequestBody BPARequest bpaRequestt) {
		return swsService.pushDatatoSws(bpaRequestt);
//		return null;
	}

	@PostMapping(value = "/_getrisktypetest")
	public List<Map<String, Object>> getRiskTypeTest(@Valid @RequestBody BPARequest bpaRequest) {

		try {
			return bpaService.getRiskTypeTest(bpaRequest);
		} catch (Exception ex) {
			List<Map<String, Object>> returnBpaList = new ArrayList<>();
			Map<String, Object> errorMap = new HashMap<>();
			errorMap.put("Error", ex.toString());
			returnBpaList.add(errorMap);

			return returnBpaList;
		}
	}

	@PostMapping(value = "/_getbuildingdetails")
	public Map<String, Object> getBuildingDetails(@RequestParam(required = false) String locid,
			@RequestParam(required = false) String fromDate, @RequestParam(required = false) String toDate) {
		Map<String, Object> response = new HashMap<>();

		if (locid == null || locid.trim().isEmpty() || fromDate == null || fromDate.trim().isEmpty() || toDate == null
				|| toDate.trim().isEmpty()) {

			response.put("status", false);
			response.put("message", "locid, fromDate, and toDate fields are mandatory");
			return response;
		}

		try {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate from = LocalDate.parse(fromDate, formatter);
			LocalDate to = LocalDate.parse(toDate, formatter);

			// Validate range: not more than 1 month
			Period diff = Period.between(from, to);
			if (diff.getMonths() > 1 || diff.getYears() > 0 || (diff.getMonths() == 1 && diff.getDays() > 0)) {
				response.put("status", false);
				response.put("message", "Date range should not exceed one month");
				return response;
			}

			Map<String, Object> bpaList = bpaService.getBuildingDetails(locid, fromDate, toDate);
			return bpaList;
		} catch (Exception ex) {
			log.info("Exception : " + ex.toString());
			Map<String, Object> returnStatement = new HashMap<>();
			returnStatement.put("Exception", "Exception While fetching data");
			returnStatement.put("status", false);

			return returnStatement;
		}
	}
	
	@PostMapping(value = "/_getLabourCessFeeDetails")
	public ResponseEntity<Object> getLabourCessFee(
		        @RequestParam(required = false) String locid,
		        @RequestParam(required = false) String fromDate,
		        @RequestParam(required = false) String toDate) {
	
		    // 1. Mandatory Check (Global Exception Handling could also handle this) [cite: 10, 43]
		if (locid == null || locid.trim().isEmpty() || fromDate == null || fromDate.trim().isEmpty() || toDate == null
				|| toDate.trim().isEmpty()) {
		        Map<String, Object> response = new HashMap<>();
		        response.put("status", false);
		        response.put("message", "locid, fromDate, and toDate fields are mandatory");
		        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 
		    }
	
		    try {
		        // 2. Correct Date Parsing [cite: 11]
		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		        LocalDate from = LocalDate.parse(fromDate, formatter);
		        LocalDate to = LocalDate.parse(toDate, formatter);
	
		        // 3. Service Call (Ensure service accepts String or LocalDate as defined) [cite: 15, 59]
		        Map<String, Object> bpaList = bpaService.getLabourCessFee(locid, fromDate, toDate);
	
		        return ResponseEntity.ok(bpaList); // [cite: 41]
	
		   
		    } catch (Exception ex) {
		        log.error("Exception While fetching data: ", ex);
		        Map<String, Object> returnStatement = new HashMap<>();
		        returnStatement.put("status", false);
		        returnStatement.put("Exception", "An internal error occurred while fetching data");
	
		        // Use this if .internalServerError() is undefined in your version
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnStatement); // [cite: 42]
		    }
		}



	@PostMapping(value = "/_updatepropertyid")
	public ResponseEntity<Object> updatePropertyId(String applicationNo, String propertyId) {
		try {
			int updateResult = bpaService.updatePropertyId(applicationNo, propertyId);
			if (updateResult > 0) {
				return new ResponseEntity<>(updateResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(updateResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in updatePropertyId: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

}
