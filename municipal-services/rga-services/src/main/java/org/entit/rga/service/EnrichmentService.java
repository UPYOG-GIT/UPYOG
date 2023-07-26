package org.entit.rga.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.IdGenRepository;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.util.RGAConstants;
import org.entit.rga.util.RGAErrorConstants;
import org.entit.rga.util.RGAUtil;
import org.entit.rga.validator.MDMSValidator;
import org.entit.rga.web.model.AuditDetails;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.Workflow;
import org.entit.rga.web.model.edcr.RequestInfoWrapper;
import org.entit.rga.web.model.idgen.IdResponse;
import org.entit.rga.web.model.workflow.BusinessService;
import org.entit.rga.workflow.WorkflowIntegrator;
import org.entit.rga.workflow.WorkflowService;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Service
@Slf4j
public class EnrichmentService {

	@Autowired
	private RGAConfiguration config;

	@Autowired
	private RGAUtil rGAUtil;

	@Autowired
	private IdGenRepository idGenRepository;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

//	@Autowired
//	private NocService nocService;

	@Autowired
	private RGAUtil util;

	@Autowired
	private UserService userService;

	@Autowired
	private MDMSValidator mdmsValidator;
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	/**
	 * encrich create RGA Reqeust by adding audidetails and uuids
	 * 
	 * @param rGARequest
	 * @param mdmsData
	 * @param values
	 */
	public void enrichRegularisationCreateRequest(RGARequest rGARequest, Object mdmsData,
			Map<String, String> values) {
		RequestInfo requestInfo = rGARequest.getRequestInfo();
		AuditDetails auditDetails = rGAUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		rGARequest.getRegularisation().setAuditDetails(auditDetails);
		rGARequest.getRegularisation().setId(UUID.randomUUID().toString());

		rGARequest.getRegularisation()
				.setAccountId(rGARequest.getRegularisation().getAuditDetails().getCreatedBy());
		String applicationType = values.get(RGAConstants.APPLICATIONTYPE);
		if (applicationType.equalsIgnoreCase(RGAConstants.REGULARISATION)) {
//			if (regularisationRequest.getRegularisation().getRiskType().equalsIgnoreCase(RegularisationConstants.LOW_RISKTYPE)
//				    || regularisationRequest.getRegularisation().getRiskType().equalsIgnoreCase(RegularisationConstants.VLOW_RISKTYPE)){
//				regularisationRequest.getRegularisation().setBusinessService(RegularisationConstants.BPA_LOW_MODULE_CODE);
//			} else {
			rGARequest.getRegularisation().setBusinessService(RGAConstants.RGA_MODULE_CODE);
//			}
		}
//				else {
//			regularisationRequest.getRegularisation().setBusinessService(RegularisationConstants.BPA_OC_MODULE_CODE);
//			regularisationRequest.getRegularisation().setLandId(values.get("landId"));
//		}
		if (rGARequest.getRegularisation().getLandInfo() != null) {
			rGARequest.getRegularisation()
					.setLandId(rGARequest.getRegularisation().getLandInfo().getId());
		}
		// RGA Documents
		if (!CollectionUtils.isEmpty(rGARequest.getRegularisation().getDocuments()))
			rGARequest.getRegularisation().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		setIdgenIds(rGARequest);
	}

	/**
	 * Sets the ApplicationNumber for given regularisationRequest
	 *
	 * @param request regularisationRequest which is to be created
	 */
	private void setIdgenIds(RGARequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getRegularisation().getTenantId();
		RGA rGA = request.getRegularisation();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNoIdgenName(),
				config.getApplicationNoIdgenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		rGA.setApplicationNo(itr.next());
	}

	/**
	 * Returns a list of numbers generated from idgen
	 *
	 * @param requestInfo RequestInfo from the request
	 * @param tenantId    tenantId of the city
	 * @param idKey       code of the field defined in application properties for
	 *                    which ids are generated for
	 * @param idformat    format in which ids are to be generated
	 * @param count       Number of ids to be generated
	 * @return List of ids generated using idGen service
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException(RGAErrorConstants.IDGEN_ERROR, "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * enchrich the updateRequest
	 * 
	 * @param rGARequest
	 * @param businessService
	 */
	public void enrichBPAUpdateRequest(RGARequest rGARequest, BusinessService businessService) {

		RequestInfo requestInfo = rGARequest.getRequestInfo();
		AuditDetails auditDetails = rGAUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		auditDetails.setCreatedBy(rGARequest.getRegularisation().getAuditDetails().getCreatedBy());
		auditDetails.setCreatedTime(rGARequest.getRegularisation().getAuditDetails().getCreatedTime());
		rGARequest.getRegularisation().getAuditDetails()
				.setLastModifiedTime(auditDetails.getLastModifiedTime());
		enrichAssignes(rGARequest.getRegularisation());
		// RGA Documents
		if (!CollectionUtils.isEmpty(rGARequest.getRegularisation().getDocuments()))
			rGARequest.getRegularisation().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		// RGA WfDocuments
		if (!CollectionUtils
				.isEmpty(rGARequest.getRegularisation().getWorkflow().getVarificationDocuments())) {
			rGARequest.getRegularisation().getWorkflow().getVarificationDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		}

	}

	/**
	 * postStatus encrichment to update the status of the workflow to the
	 * application and generating permit and oc number when applicable
	 * 
	 * @param rgaRequest
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void postStatusEnrichment(RGARequest rgaRequest) {
//		log.info("hyyyyyyyyyyyyyyy");
		RGA rga = rgaRequest.getRegularisation();
		String tenantId = rgaRequest.getRegularisation().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(rgaRequest.getRequestInfo(), tenantId);

		BusinessService businessService = workflowService.getBusinessService(rga,
				rgaRequest.getRequestInfo(), rga.getApplicationNo());
//		log.info("Application status is : " + regularisation.getStatus());
		String state = workflowService.getCurrentState(rga.getStatus(), businessService);

		if (state.equalsIgnoreCase(RGAConstants.DOCVERIFICATION_STATE)) {
			rga.setApplicationDate(Calendar.getInstance().getTimeInMillis());
		}
//		log.info("state status is : " + state);

		if (StringUtils.isEmpty(rga.getRiskType())) {
//			if (bpa.getBusinessService().equals(RegularisationConstants.BPA_LOW_MODULE_CODE)) {
//				bpa.setRiskType(RegularisationConstants.LOW_RISKTYPE);
//			} else {
			Map<String, List<String>> masterData = mdmsValidator.getAttributeValues(mdmsData);
			StringBuilder uri = new StringBuilder(config.getEdcrHost());
			uri.append(config.getGetPlanEndPoint());
			uri.append("?").append("tenantId=").append(rga.getTenantId().split("\\.")[0]);
			uri.append("&").append("edcrNumber=").append(rga.getEdcrNumber());
			org.entit.rga.web.model.edcr.RequestInfo edcrRequestInfo = new org.entit.rga.web.model.edcr.RequestInfo();

			BeanUtils.copyProperties(rgaRequest.getRequestInfo(), edcrRequestInfo);

			LinkedHashMap responseMap = null;

			try {
				responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
						new RequestInfoWrapper(edcrRequestInfo));
			} catch (ServiceCallException se) {
				throw new CustomException(RGAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
			}

			if (CollectionUtils.isEmpty(responseMap))
				throw new CustomException(RGAErrorConstants.EDCR_ERROR,
						"The response from EDCR service is empty or null");
			String jsonString = new JSONObject(responseMap).toString();

			DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);

			String plotAreaStr = "" + context.read("edcrDetail[0].planDetail.planInformation.plotArea");
			String buildingHeightStr = "" + context.read("edcrDetail[0].planDetail.blocks[0].building.buildingHeight");
			Double plotArea = Double.valueOf(plotAreaStr);
//				Double plotArea = context.read("edcrDetail[0].planDetail.planInformation.plotArea");
			Double buildingHeight = Double.valueOf(buildingHeightStr);
//				Double buildingHeight = context.read("edcrDetail[0].planDetail.blocks[0].building.buildingHeight");

			List jsonOutput = JsonPath.read(masterData, RGAConstants.RISKTYPE_COMPUTATION);
			String filterExp = "";
			List<String> riskTypes = new ArrayList<String>();
			if (plotArea > 1000 || buildingHeight >= 15) {
//				filterExp = "$.[?((@.fromPlotArea < " + plotArea + " ) || ( @.fromBuildingHeight < " + buildingHeight
//						+ "  ))].riskType";
				riskTypes.add("HIGH");
			} else {
				filterExp = "$.[?((@.fromPlotArea < " + plotArea + " && @.toPlotArea >= " + plotArea
						+ ") && ( @.fromBuildingHeight < " + buildingHeight + "  &&  @.toBuildingHeight >= "
						+ buildingHeight + "  ))].riskType";
				log.info("filterExp: " + filterExp);

				riskTypes = JsonPath.read(jsonOutput, filterExp);
			}

//			log.info("filterExp: " + filterExp);
//
//			 riskTypes = JsonPath.read(jsonOutput, filterExp);

			log.info("riskTypes: " + riskTypes.toString());

			if (!CollectionUtils.isEmpty(riskTypes)) {
				String expectedRiskType = riskTypes.get(0);
				log.info("expectedRiskType: " + expectedRiskType);

				rga.setRiskType(expectedRiskType);
			} else {
				throw new CustomException(RGAErrorConstants.INVALID_RISK_TYPE,
						"The Risk Type is not valid ");
			}

//			}
		}

//		log.info("Application state is : " + state);
		this.generateApprovalNo(rgaRequest, state);
//		nocService.initiateNocWorkflow(regularisationRequest, mdmsData);

	}

	/**
	 * generate the permit and oc number on approval status of the BPA and BPAOC
	 * respectively
	 * 
	 * @param rGARequest
	 * @param state
	 */
	private void generateApprovalNo(RGARequest rGARequest, String state) {
//		log.info("hii");
		RGA rGA = rGARequest.getRegularisation();
//		if ((regularisation.getBusinessService().equalsIgnoreCase(RegularisationConstants.BPA_OC_MODULE_CODE)
//				&& regularisation.getStatus().equalsIgnoreCase(RegularisationConstants.APPROVED_STATE))
//				|| (!regularisation.getBusinessService().equalsIgnoreCase(RegularisationConstants.BPA_OC_MODULE_CODE)
//						&& ((!regularisation.getRiskType().toString().equalsIgnoreCase(RegularisationConstants.LOW_RISKTYPE)
//								&& state.equalsIgnoreCase(RegularisationConstants.APPROVED_STATE))
//								|| (state.equalsIgnoreCase(RegularisationConstants.APPROVED_STATE) && regularisation.getRiskType()
//										.toString().equalsIgnoreCase(RegularisationConstants.LOW_RISKTYPE))))) {
		if (state.equalsIgnoreCase(RGAConstants.APPROVED_STATE)) {
			int vailidityInMonths = config.getValidityInMonths();
			Calendar calendar = Calendar.getInstance();
			rGA.setApprovalDate(Calendar.getInstance().getTimeInMillis());

			// Adding 3years (36 months) to Current Date
			calendar.add(Calendar.MONTH, vailidityInMonths);
			Map<String, Object> additionalDetail = null;
			if (rGA.getAdditionalDetails() != null) {
				additionalDetail = (Map) rGA.getAdditionalDetails();
			} else {
				additionalDetail = new HashMap<String, Object>();
				rGA.setAdditionalDetails(additionalDetail);
			}

			additionalDetail.put("validityDate", calendar.getTimeInMillis());
			List<IdResponse> idResponses = idGenRepository.getId(rGARequest.getRequestInfo(),
					rGA.getTenantId(), config.getPermitNoIdgenName(), config.getPermitNoIdgenFormat(), 1)
					.getIdResponses();
			rGA.setApprovalNo(idResponses.get(0).getId());
			log.debug("Approval number generated: " + rGA.getApprovalNo());
			log.info("Approval number generated:----- " + rGA.getApprovalNo());
			if (state.equalsIgnoreCase(RGAConstants.DOCVERIFICATION_STATE)
					&& rGA.getRiskType().toString().equalsIgnoreCase(RGAConstants.LOW_RISKTYPE)) {

				Object mdmsData = rGAUtil.mDMSCall(rGARequest.getRequestInfo(),
						rGARequest.getRegularisation().getTenantId());
				Map<String, String> edcrResponse = edcrService.getEDCRDetails(rGARequest.getRequestInfo(),
						rGARequest.getRegularisation());
//				log.debug("applicationType is " + edcrResponse.get(RegularisationConstants.APPLICATIONTYPE));
//				log.debug("serviceType is " + edcrResponse.get(RegularisationConstants.SERVICETYPE));

				String conditionsPath = RGAConstants.CONDITIONS_MAP
						.replace("{1}", RGAConstants.PENDING_APPROVAL_STATE)
						.replace("{2}", rGA.getRiskType().toString())
						.replace("{3}", edcrResponse.get(RGAConstants.SERVICETYPE))
						.replace("{4}", edcrResponse.get(RGAConstants.APPLICATIONTYPE));
				log.debug(conditionsPath);

				try {
					List<String> conditions = (List<String>) JsonPath.read(mdmsData, conditionsPath);
					log.debug(conditions.toString());
					if (rGA.getAdditionalDetails() == null) {
						rGA.setAdditionalDetails(new HashMap());
					}
					Map additionalDetails = (Map) rGA.getAdditionalDetails();
					additionalDetails.put(RGAConstants.PENDING_APPROVAL_STATE.toLowerCase(),
							conditions.get(0));

				} catch (Exception e) {
					log.warn("No approval conditions found for the application " + rGA.getApplicationNo());
				}
			}
		}
	}

	/**
	 * handles the skippayment of the BPA when demand is zero
	 * 
	 * @param rGARequest
	 */
	public void skipPayment(RGARequest rGARequest) {
		RGA rGA = rGARequest.getRegularisation();
		BigDecimal demandAmount = rGAUtil.getDemandAmount(rGARequest);
		if (!(demandAmount.compareTo(BigDecimal.ZERO) > 0)) {
			Workflow workflow = Workflow.builder().action(RGAConstants.ACTION_SKIP_PAY).build();
			rGA.setWorkflow(workflow);
			wfIntegrator.callWorkFlow(rGARequest);
		}
	}

	/**
	 * In case of SENDBACKTOCITIZEN enrich the assignee with the owners and creator
	 * of BPA
	 * 
	 * @param rGA RGA to be enriched
	 */
	public void enrichAssignes(RGA rGA) {
		Workflow wf = rGA.getWorkflow();
		Map<String, String> mobilenumberToUUIDs = new HashMap<>();
		Set<String> assignes = new HashSet<>();
		if (wf != null && wf.getAssignes() != null)
			assignes.addAll(wf.getAssignes());
		if (wf != null && wf.getAction().equalsIgnoreCase(RGAConstants.ACTION_SENDBACKTOCITIZEN)
				|| wf.getAction().equalsIgnoreCase(RGAConstants.ACTION_SEND_TO_CITIZEN)) {

			// Adding owners to assignes list
			rGA.getLandInfo().getOwners().forEach(ownerInfo -> {
				if (ownerInfo.getUuid() != null && ownerInfo.getActive()) {
					mobilenumberToUUIDs.put(ownerInfo.getMobileNumber(), ownerInfo.getUuid());
				}
			});

			Set<String> registeredUUIDS = userService.getUUidFromUserName(rGA, mobilenumberToUUIDs);

			if (!CollectionUtils.isEmpty(registeredUUIDS))
				assignes.addAll(registeredUUIDS);

		} else if (wf != null
				&& (wf.getAction().equalsIgnoreCase(RGAConstants.ACTION_SEND_TO_ARCHITECT) || (rGA
						.getStatus().equalsIgnoreCase(RGAConstants.STATUS_CITIZEN_APPROVAL_INPROCESS)
						&& wf.getAction().equalsIgnoreCase(RGAConstants.ACTION_APPROVE)))) {
			// Adding creator of BPA(Licensee)
			if (rGA.getAccountId() != null)
				assignes.add(rGA.getAccountId());
		}
		if (rGA.getWorkflow() == null) {
			Workflow wfNew = new Workflow();
			wfNew.setAssignes(new LinkedList<>(assignes));
			rGA.setWorkflow(wfNew);
		} else {
			rGA.getWorkflow().setAssignes(new LinkedList<>(assignes));
		}
	}
}
