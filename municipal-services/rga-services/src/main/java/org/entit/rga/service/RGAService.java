package org.entit.rga.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
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
	private RGAValidator rGAValidator;

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
		rGAValidator.validateCreate(rgaRequest, mdmsData, values);
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
		rGAValidator.validatePreEnrichData(rgaRequest, mdmsData);
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

	public List<RGA> getRegularisationWithRegularisationId(RGARequest request) {
		RGASearchCriteria criteria = new RGASearchCriteria();
		List<String> ids = new LinkedList<>();
		ids.add(request.getRegularisation().getId());
		criteria.setTenantId(request.getRegularisation().getTenantId());
		criteria.setIds(ids);
		List<RGA> bpa = repository.getRegularisationData(criteria, null);
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
				rGAValidator.validateUpdate(rGARequest, searchResult, mdmsData,
						workflowService.getCurrentState(rGA.getStatus(), businessService), edcrResponse);
//				if (!applicationType.equalsIgnoreCase(RegularisationConstants.BUILDING_PLAN_OC)) {
//					landService.updateLandInfo(regularisationRequest);
//				}
				rGAValidator.validateCheckList(mdmsData, rGARequest,
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
