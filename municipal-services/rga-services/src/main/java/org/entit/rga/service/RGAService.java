package org.entit.rga.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.RGARepository;
import org.entit.rga.util.NotificationUtil;
import org.entit.rga.util.RGAConstants;
import org.entit.rga.util.RGAErrorConstants;
import org.entit.rga.util.RGAUtil;
import org.entit.rga.validator.RGAValidator;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGAPenaltyRequest;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.RGASlabMasterRequest;
import org.entit.rga.web.model.landInfo.LandInfo;
import org.entit.rga.web.model.landInfo.LandSearchCriteria;
import org.entit.rga.web.model.user.UserDetailResponse;
import org.entit.rga.web.model.user.UserSearchRequest;
import org.entit.rga.web.model.workflow.BusinessService;
import org.entit.rga.workflow.ActionValidator;
import org.entit.rga.workflow.WorkflowIntegrator;
import org.entit.rga.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Slf4j
@Service
public class RGAService {

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private RGARepository repository;

	@Autowired
	private ActionValidator actionValidator;

	@Autowired
	private RGAValidator rgaValidator;

	@Autowired
	private RGAUtil util;

	@Autowired
	private CalculationService calculationService;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private RGALandService landService;

	@Autowired
	private UserService userService;

	@Autowired
	private RGAConfiguration config;

	public RGA create(RGARequest rgaRequest) {

		RequestInfo requestInfo = rgaRequest.getRequestInfo();
		String tenantId = rgaRequest.getRegularisation().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		if (rgaRequest.getRegularisation().getTenantId().split("\\.").length == 1) {
			throw new CustomException(RGAErrorConstants.INVALID_TENANT, " Application cannot be create at StateLevel");
		}

//		log.info("regularisationRequest.getRegularisation().getLandInfo().getAddress(): "
//				+ regularisationRequest.getRegularisation().getLandInfo().getAddress().toString());
		// Since approval number should be generated at approve stage
		if (!StringUtils.isEmpty(rgaRequest.getRegularisation().getApprovalNo())) {
			rgaRequest.getRegularisation().setApprovalNo(null);
		}

		Map<String, String> values = edcrService.validateEdcrPlan(rgaRequest, mdmsData);
		String applicationType = values.get(RGAConstants.APPLICATIONTYPE);
//		this.validateCreateOC(applicationType, values, requestInfo, regularisationRequest);
		rgaValidator.validateCreate(rgaRequest, mdmsData, values);
//		if (!applicationType.equalsIgnoreCase(RGAConstants.BUILDING_PLAN_OC)) {
		landService.addLandInfoToBPA(rgaRequest);
//		}
		enrichmentService.enrichRegularisationCreateRequest(rgaRequest, mdmsData, values);
		wfIntegrator.callWorkFlow(rgaRequest);
//		nocService.createNocRequest(regularisationRequest, mdmsData);
//		this.addCalculation(applicationType, regularisationRequest);
		repository.save(rgaRequest);
		return rgaRequest.getRegularisation();
	}

	/**
	 * calls calculation service calculate and generte demand accordingly
	 * 
	 * @param applicationType
	 * @param bpaRequest
	 */
	private void addCalculation(String applicationType, RGARequest bpaRequest) {

		log.info("inside BPAService.addCalculation()........");
		log.info("applicationType: " + applicationType);
//		if (bpaRequest.getRegularisation().getRiskType().equals(RegularisationConstants.LOW_RISKTYPE)
//				&& !applicationType.equalsIgnoreCase(RegularisationConstants.BUILDING_PLAN_OC)) {
////			calculationService.addCalculation(bpaRequest, RegularisationConstants.LOW_RISK_PERMIT_FEE_KEY);
////			calculationService.addCalculation(bpaRequest, RegularisationConstants.LOW_APPLICATION_FEE_KEY);
//			calculationService.addCalculation(bpaRequest, RegularisationConstants.APPLICATION_FEE_KEY);
//		} else {
		calculationService.addCalculation(bpaRequest, RGAConstants.APPLICATION_FEE_KEY);
//		}
	}

	/**
	 * Updates the bpa
	 * 
	 * @param rgaRequest The update Request
	 * @return Updated bpa
	 */
	@SuppressWarnings("unchecked")
	public RGA update(RGARequest rgaRequest) {
		log.info("inside RegularisationService.update().......");
		RequestInfo requestInfo = rgaRequest.getRequestInfo();
		String tenantId = rgaRequest.getRegularisation().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
//		log.info("mdmsData: " + mdmsData);
		RGA rga = rgaRequest.getRegularisation();
//		log.info("bpa: " + bpa);

		if (rga.getId() == null) {
			throw new CustomException(RGAErrorConstants.UPDATE_ERROR, "Application Not found in the System" + rga);
		}

		Map<String, String> edcrResponse = edcrService.getEDCRDetails(rgaRequest.getRequestInfo(),
				rgaRequest.getRegularisation());
		String applicationType = edcrResponse.get(RGAConstants.APPLICATIONTYPE);
//		log.info("applicationType is " + applicationType);

		BusinessService businessService = workflowService.getBusinessService(rga, rgaRequest.getRequestInfo(),
				rga.getApplicationNo());

		List<RGA> searchResult = getRegularisationWithRegularisationId(rgaRequest);
		if (CollectionUtils.isEmpty(searchResult) || searchResult.size() > 1) {
			throw new CustomException(RGAErrorConstants.UPDATE_ERROR,
					"Failed to Update the Application, Found None or multiple applications!");
		}

		Map<String, String> additionalDetails = rga.getAdditionalDetails() != null
				? (Map<String, String>) rga.getAdditionalDetails()
				: new HashMap<String, String>();

		if (rga.getStatus().equalsIgnoreCase(RGAConstants.FI_STATUS)
				&& rga.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_SENDBACKTOCITIZEN)) {
			if (additionalDetails.get(RGAConstants.FI_ADDITIONALDETAILS) != null)
				additionalDetails.remove(RGAConstants.FI_ADDITIONALDETAILS);
		}

//		this.processOcUpdate(applicationType, edcrResponse.get(RegularisationConstants.PERMIT_NO), regularisationRequest, requestInfo,
//				additionalDetails);

		rgaRequest.getRegularisation().setAuditDetails(searchResult.get(0).getAuditDetails());

//		nocService.manageOfflineNocs(regularisationRequest, mdmsData);
		rgaValidator.validatePreEnrichData(rgaRequest, mdmsData);
		enrichmentService.enrichBPAUpdateRequest(rgaRequest, businessService);

		this.handleRejectSendBackActions(applicationType, rgaRequest, businessService, searchResult, mdmsData,
				edcrResponse);
		String state = workflowService.getCurrentState(rga.getStatus(), businessService);
		String businessSrvc = businessService.getBusinessService();

//		log.info("businessSrvc :" + businessSrvc);

		/*
		 * Before approving the application we need to check sanction fee is applicable
		 * or not for that purpose on PENDING_APPROVAL_STATE the demand is generating.
		 */
		// Generate the sanction Demand
//		if ((businessSrvc.equalsIgnoreCase(RGAConstants.BPA_OC_MODULE_CODE)
//				|| businessSrvc.equalsIgnoreCase(RGAConstants.BPA_BUSINESSSERVICE)
//				|| businessSrvc.equalsIgnoreCase(RGAConstants.BPA_LOW_BUSINESSSERVICE))
//				&& state.equalsIgnoreCase(RGAConstants.PENDING_APPROVAL_STATE)) {
//			calculationService.addCalculation(rgaRequest, RGAConstants.SANCTION_FEE_KEY);
//		}

		/*
		 * For Permit medium/high and OC on approval stage, we need to check whether for
		 * a application sanction fee is applicable or not. If sanction fee is not
		 * applicable then we need to skip the payment on APPROVE and need to make it
		 * APPROVED instead of SANCTION FEE PAYMENT PEDNING.
		 */

		/*
		 * if ((businessSrvc.equalsIgnoreCase(RGAConstants.BPA_OC_MODULE_CODE) ||
		 * businessSrvc.equalsIgnoreCase(RGAConstants.BPA_BUSINESSSERVICE)) &&
		 * state.equalsIgnoreCase(RGAConstants.PENDING_APPROVAL_STATE) &&
		 * rga.getWorkflow() != null &&
		 * rga.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_APPROVE)
		 * && util.getDemandAmount(rgaRequest).compareTo(BigDecimal.ZERO) <= 0) {
		 * Workflow workflow =
		 * Workflow.builder().action(RGAConstants.ACTION_SKIP_PAY).build();
		 * rga.setWorkflow(workflow); }
		 */

		wfIntegrator.callWorkFlow(rgaRequest);
//		log.info("===> workflow done =>" + regularisationRequest.getRegularisation().getStatus());
		enrichmentService.postStatusEnrichment(rgaRequest);

//		log.info("Bpa status is--- : " + bpa.getStatus());
//		log.info("Bpa Occupancy is---- : " + edcrResponse.get("Occupancy"));
//		log.info("Bpa getBusinessService is---- : " + bpa.getBusinessService());
//		log.info("Bpa state is---- : " + state);

		// Validity Date for Direct Bhawan Anugya

		/*
		 * if (edcrResponse.get("Occupancy").equalsIgnoreCase("Residential") &&
		 * rGA.getStatus().equalsIgnoreCase(RGAConstants.APPL_FEE_STATE) &&
		 * (rGA.getBusinessService().equalsIgnoreCase(RGAConstants.
		 * BPA_LOW_BUSINESSSERVICE))) { // log.info("inside if condition--by nehaaaa");
		 * // log.info("edcrResponse.get Occupancy" + edcrResponse.get("Occupancy"));
		 * 
		 * int validityInMonthsForPre = config.getValidityInMonthsForPre(); Calendar
		 * calendar = Calendar.getInstance();
		 * 
		 * // Adding 1 month to current date calendar.add(Calendar.MONTH,
		 * validityInMonthsForPre); Map<String, Object> additionalDetail = null;
		 * additionalDetail = (Map) rGA.getAdditionalDetails();
		 * 
		 * additionalDetail.put("validityDateForPre", calendar.getTimeInMillis());
		 * rGA.setAdditionalDetails(additionalDetail);
		 * 
		 * }
		 */

		/*
		 * if (Arrays.asList(config.getSkipPaymentStatuses().split(",")).contains(bpa.
		 * getStatus())) { enrichmentService.skipPayment(bpaRequest);
		 * enrichmentService.postStatusEnrichment(bpaRequest); }
		 */

		repository.update(rgaRequest, workflowService.isStateUpdatable(rga.getStatus(), businessService));
		return rgaRequest.getRegularisation();

	}
	
	public List<RGA> search(RGASearchCriteria criteria, RequestInfo requestInfo) {
		List<RGA> rgas = new LinkedList<>();
		log.info("criteria.getTenantId():______====" + criteria.getTenantId());
		rgaValidator.validateSearch(requestInfo, criteria);
		LandSearchCriteria landcriteria = new LandSearchCriteria();
		landcriteria.setTenantId(criteria.getTenantId());
		landcriteria.setLocality(criteria.getLocality());
		List<String> edcrNos = null;
		if (criteria.getMobileNumber() != null) {
			rgas = this.getBPAFromMobileNumber(criteria, landcriteria, requestInfo);
		} else {
			List<String> roles = new ArrayList<>();
			for (Role role : requestInfo.getUserInfo().getRoles()) {
				roles.add(role.getCode());
			}
			if ((criteria.tenantIdOnly() || criteria.isEmpty()) && roles.contains(RGAConstants.CITIZEN)) {
				log.info("loading data of created and by me");
				rgas = this.getBPACreatedForByMe(criteria, requestInfo, landcriteria, edcrNos);
				log.info("no of bpas retuning by the search query" + rgas.size());
			} else {
				rgas = getBPAFromCriteria(criteria, requestInfo, edcrNos);
				ArrayList<String> landIds = new ArrayList<>();
				if (!rgas.isEmpty()) {
					for (int i = 0; i < rgas.size(); i++) {
						landIds.add(rgas.get(i).getLandId());
					}
					landcriteria.setIds(landIds);
					landcriteria.setTenantId(rgas.get(0).getTenantId());
					log.info("Call with tenantId to Land::" + landcriteria.getTenantId());
					ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);

					this.populateLandToBPA(rgas, landInfos, requestInfo);
				}
			}
		}
		return rgas;
	}
	
	private List<RGA> getBPACreatedForByMe(RGASearchCriteria criteria, RequestInfo requestInfo,
			LandSearchCriteria landcriteria, List<String> edcrNos) {
		List<RGA> rgas = null;
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		if (criteria.getTenantId() != null) {
			userSearchRequest.setTenantId(criteria.getTenantId());
		}
		List<String> uuids = new ArrayList<>();
		if (requestInfo.getUserInfo() != null && !StringUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
			uuids.add(requestInfo.getUserInfo().getUuid());
			criteria.setOwnerIds(uuids);
			criteria.setCreatedBy(uuids);
		}
		log.info("loading data of created and by me" + uuids.toString());
		UserDetailResponse userInfo = userService.getUser(criteria, requestInfo);
		log.info("userInfo.getUser().size(): " + userInfo.getUser().size());
		log.info("userInfo: " + userInfo.getUser().toString());
		if (userInfo != null) {
			landcriteria.setMobileNumber(userInfo.getUser().get(0).getMobileNumber());
		}
		log.info("Call with multiple to Land::" + landcriteria.getTenantId() + landcriteria.getMobileNumber());
		ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);
		ArrayList<String> landIds = new ArrayList<>();
		if (!landInfos.isEmpty()) {
			landInfos.forEach(land -> landIds.add(land.getId()));
			criteria.setLandId(landIds);
		}

		rgas = getBPAFromCriteria(criteria, requestInfo, edcrNos);
		log.info("no of bpas queried" + rgas.size());
		this.populateLandToBPA(rgas, landInfos, requestInfo);
		return rgas;
	}
	
	private void populateLandToBPA(List<RGA> rgas, List<LandInfo> landInfos, RequestInfo requestInfo) {
		for (int i = 0; i < rgas.size(); i++) {
			for (int j = 0; j < landInfos.size(); j++) {
				if (landInfos.get(j).getId().equalsIgnoreCase(rgas.get(i).getLandId())) {
					rgas.get(i).setLandInfo(landInfos.get(j));
				}
			}
			if (rgas.get(i).getLandId() != null && rgas.get(i).getLandInfo() == null) {
				LandSearchCriteria missingLandcriteria = new LandSearchCriteria();
				List<String> missingLandIds = new ArrayList<>();
				missingLandIds.add(rgas.get(i).getLandId());
				missingLandcriteria.setTenantId(rgas.get(0).getTenantId());
				missingLandcriteria.setIds(missingLandIds);
				log.info("Call with land ids to Land::" + missingLandcriteria.getTenantId()
						+ missingLandcriteria.getIds());
				List<LandInfo> newLandInfo = landService.searchLandInfoToBPA(requestInfo, missingLandcriteria);
				for (int j = 0; j < newLandInfo.size(); j++) {
					if (newLandInfo.get(j).getId().equalsIgnoreCase(rgas.get(i).getLandId())) {
						rgas.get(i).setLandInfo(newLandInfo.get(j));
					}
				}
			}
		}
	}
	
	private List<RGA> getBPAFromMobileNumber(RGASearchCriteria criteria, LandSearchCriteria landcriteria,
			RequestInfo requestInfo) {
		List<RGA> rgas = null;
		log.info("Call with mobile number to Land::" + criteria.getMobileNumber());
		landcriteria.setMobileNumber(criteria.getMobileNumber());
		ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
		ArrayList<String> landId = new ArrayList<>();
		if (!landInfo.isEmpty()) {
			landInfo.forEach(land -> landId.add(land.getId()));
			criteria.setLandId(landId);
		}
		List<String> uuids = new ArrayList<>();
		if (requestInfo.getUserInfo() != null && !StringUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
			uuids.add(requestInfo.getUserInfo().getUuid());
			criteria.setCreatedBy(uuids);
		}
		rgas = getBPAFromLandId(criteria, requestInfo, null);
		if (!landInfo.isEmpty()) {
			for (int i = 0; i < rgas.size(); i++) {
				for (int j = 0; j < landInfo.size(); j++) {
					if (landInfo.get(j).getId().equalsIgnoreCase(rgas.get(i).getLandId())) {
						rgas.get(i).setLandInfo(landInfo.get(j));
					}
				}
			}
		}
		return rgas;
	}
	
	private List<RGA> getBPAFromLandId(RGASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
		List<RGA> bpa = new LinkedList<>();
		bpa = repository.getRGAData(criteria, edcrNos);
		if (bpa.size() == 0) {
			return Collections.emptyList();
		}
		return bpa;
	}
	
	public List<RGA> getBPAFromCriteria(RGASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
		List<RGA> bpa = repository.getRGAData(criteria, edcrNos);
		if (bpa.isEmpty())
			return Collections.emptyList();
		return bpa;
	}
	
	public int getBPACount(RGASearchCriteria criteria, RequestInfo requestInfo) {

		LandSearchCriteria landcriteria = new LandSearchCriteria();
		landcriteria.setTenantId(criteria.getTenantId());
		landcriteria.setLocality(criteria.getLocality());
		List<String> edcrNos = null;
		if (criteria.getMobileNumber() != null) {
			landcriteria.setMobileNumber(criteria.getMobileNumber());
			ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
			ArrayList<String> landId = new ArrayList<>();
			if (!landInfo.isEmpty()) {
				landInfo.forEach(land -> landId.add(land.getId()));
				criteria.setLandId(landId);
			}
		} else {
			List<String> roles = new ArrayList<>();
			for (Role role : requestInfo.getUserInfo().getRoles()) {
				roles.add(role.getCode());
			}
			if ((criteria.tenantIdOnly() || criteria.isEmpty()) && roles.contains(RGAConstants.CITIZEN)) {
				UserSearchRequest userSearchRequest = new UserSearchRequest();
				if (criteria.getTenantId() != null) {
					userSearchRequest.setTenantId(criteria.getTenantId());
				}
				List<String> uuids = new ArrayList<>();
				if (requestInfo.getUserInfo() != null && !StringUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
					uuids.add(requestInfo.getUserInfo().getUuid());
					criteria.setOwnerIds(uuids);
					criteria.setCreatedBy(uuids);
				}
				UserDetailResponse userInfo = userService.getUser(criteria, requestInfo);
				if (userInfo != null) {
					landcriteria.setMobileNumber(userInfo.getUser().get(0).getMobileNumber());
				}
				ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);
				ArrayList<String> landIds = new ArrayList<>();
				if (!landInfos.isEmpty()) {
					landInfos.forEach(land -> landIds.add(land.getId()));
					criteria.setLandId(landIds);
				}
			}
		}
		return repository.getBPACount(criteria, edcrNos);

	}
	
//	public List<RGA> getBPADataFromCriteria(RGASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
//		List<RGA> bpa = repository.getApplicationData(criteria);
//		if (bpa.isEmpty())
//			return Collections.emptyList();
//		return bpa;
//	}

	public List<RGA> getRegularisationWithRegularisationId(RGARequest request) {
		RGASearchCriteria criteria = new RGASearchCriteria();
		List<String> ids = new LinkedList<>();
		ids.add(request.getRegularisation().getId());
		criteria.setTenantId(request.getRegularisation().getTenantId());
		criteria.setIds(ids);
		List<RGA> bpa = repository.getRGAData(criteria, null);
		return bpa;
	}

	private void handleRejectSendBackActions(String applicationType, RGARequest rGARequest,
			BusinessService businessService, List<RGA> searchResult, Object mdmsData,
			Map<String, String> edcrResponse) {
		RGA rGA = rGARequest.getRegularisation();
		if (rGA.getWorkflow().getAction() != null
				&& (rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_REJECT)
						|| rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_REVOCATE))) {

			if (rGA.getWorkflow().getComments() == null || rGA.getWorkflow().getComments().isEmpty()) {
				throw new CustomException(RGAErrorConstants.BPA_UPDATE_ERROR_COMMENT_REQUIRED,
						"Comment is mandaotory, please provide the comments ");
			}
//			nocService.handleBPARejectedStateForNoc(regularisationRequest);

		} else {

			if (!rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_SENDBACKTOCITIZEN)) {
				actionValidator.validateUpdateRequest(rGARequest, businessService);
				rgaValidator.validateUpdate(rGARequest, searchResult, mdmsData,
						workflowService.getCurrentState(rGA.getStatus(), businessService), edcrResponse);
//				if (!applicationType.equalsIgnoreCase(RegularisationConstants.BUILDING_PLAN_OC)) {
//					landService.updateLandInfo(regularisationRequest);
//				}
				rgaValidator.validateCheckList(mdmsData, rGARequest,
						workflowService.getCurrentState(rGA.getStatus(), businessService));
			}
		}
	}

	/*

	*/
	
	public int createRGASlabMaster(RGASlabMasterRequest rgaSlabMasterRequest) {
		return repository.createRGASlabMaster(rgaSlabMasterRequest);
	}

	public List<Map<String, Object>> getRGASlabMasterByTenantId(String tenantId) {
		List<Map<String, Object>> resultList = repository.getRGASlabMasterByTenantId(tenantId);
		log.info("getRGASlabMasterByTenantId: " + resultList.toString());
		return resultList;
	}
	
	public int deleteRGASlabMasterById(List<Integer> ids) {
		return repository.deleteRGASlabMasterById(ids);
	}
	
	public int createRGAPenalty(RGAPenaltyRequest rgaPenaltyRequest) {
		return repository.createRGAPenalty(rgaPenaltyRequest);
	}

	public List<Map<String, Object>> getRGAPenaltyByTenantId(String tenantId) {
		List<Map<String, Object>> resultList = repository.getRGAPenaltyByTenantId(tenantId);
		log.info("getRGAPenaltyByTenantId: " + resultList.toString());
		return resultList;
	}
	
	public int deleteRGAPenaltyById(List<Integer> ids) {
		return repository.deleteRGAPenaltyById(ids);
	}
}
