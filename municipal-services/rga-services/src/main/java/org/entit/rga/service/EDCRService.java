package org.entit.rga.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.RGARepository;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.util.RGAConstants;
import org.entit.rga.util.RGAErrorConstants;
import org.entit.rga.validator.MDMSValidator;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.RequestInfoWrapper;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EDCRService {

	private ServiceRequestRepository serviceRequestRepository;

	private RGAConfiguration config;

	@Autowired
	private MDMSValidator mdmsValidator;

	@Autowired
	RGARepository rGARepository;

	@Autowired
	public EDCRService(ServiceRequestRepository serviceRequestRepository, RGAConfiguration config) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.config = config;
	}

	/**
	 * Validates the EDCR Plan based on the edcr Number and the RiskType
	 * 
	 * @param request BPARequest for create
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> validateEdcrPlan(RGARequest request, Object mdmsData) {

		String edcrNo = request.getRegularisation().getEdcrNumber();
//		String riskType = request.getRegularisation().getRiskType();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());
		RGA rGA = request.getRegularisation();

		RGASearchCriteria criteria = new RGASearchCriteria();
		criteria.setEdcrNumber(rGA.getEdcrNumber());
		List<RGA> rGAs = rGARepository.getRegularisationData(criteria, null);
		if (rGAs.size() > 0) {
			for (int i = 0; i < rGAs.size(); i++) {
				if (!rGAs.get(i).getStatus().equalsIgnoreCase(RGAConstants.STATUS_REJECTED)
						&& !rGAs.get(i).getStatus().equalsIgnoreCase(RGAConstants.STATUS_REVOCATED)) {
					throw new CustomException(RGAErrorConstants.DUPLICATE_EDCR,
							" Application already exists with EDCR Number " + rGA.getEdcrNumber());
				}
			}
		}

		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(rGA.getTenantId());
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(request.getRequestInfo(), edcrRequestInfo);
//		Map<String, List<String>> masterData = mdmsValidator.getAttributeValues(mdmsData);

//		log.info("masterData: " + masterData);
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
//			log.info("responseMap: " + responseMap);
		} catch (ServiceCallException se) {
			throw new CustomException(RGAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
		}

		if (CollectionUtils.isEmpty(responseMap))
			throw new CustomException(RGAErrorConstants.EDCR_ERROR, "The response from EDCR service is empty or null");

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		List<String> edcrStatus = context.read("edcrDetail.*.status");
//		List<String> OccupancyTypes = context
//				.read("edcrDetail.*.planDetail.virtualBuilding.occupancyTypes.*.type.code");
//		TypeRef<List<Double>> typeRef = new TypeRef<List<Double>>() {
//		};
		Map<String, String> additionalDetails = rGA.getAdditionalDetails() != null ? (Map) rGA.getAdditionalDetails()
				: new HashMap<String, String>();
		LinkedList<String> serviceType = context.read("edcrDetail.*.applicationSubType");
		if (serviceType != null && !serviceType.isEmpty() && additionalDetails.get(RGAConstants.SERVICETYPE) != null
				&& !serviceType.get(0).equalsIgnoreCase(additionalDetails.get(RGAConstants.SERVICETYPE))) {
			throw new CustomException(RGAErrorConstants.INVALID_SERVICE_TYPE,
					"The service type is invalid, it is not matching with scrutinized plan service type "
							+ serviceType.get(0));
		}
		if (serviceType == null || serviceType.size() == 0) {
			serviceType.add("REGULARISATION");
		}
		LinkedList<String> applicationType = context.read("edcrDetail.*.appliactionType");
		if (applicationType != null && !applicationType.isEmpty()
				&& additionalDetails.get(RGAConstants.APPLICATIONTYPE) != null
				&& !applicationType.get(0).equalsIgnoreCase(additionalDetails.get(RGAConstants.APPLICATIONTYPE))) {
			throw new CustomException(RGAErrorConstants.INVALID_APPLN_TYPE,
					"The application type is invalid, it is not matching with scrutinized plan application type "
							+ applicationType.get(0));
		}

		if (applicationType == null || applicationType.size() == 0) {
			applicationType.add("permit");
		}
		LinkedList<String> permitNumber = context.read("edcrDetail.*.permitNumber");
		additionalDetails.put(RGAConstants.SERVICETYPE, serviceType.get(0));
		additionalDetails.put(RGAConstants.APPLICATIONTYPE, applicationType.get(0));
		if (!permitNumber.isEmpty()) {
			/*
			 * Validating OC application, with submitted permit number is any OC submitted
			 * without rejection. Using a permit number only one OC application submission
			 * should allowed otherwise needs to throw validation message for more one
			 * submission. If the OC application is rejected for a permit then we need
			 * allow.
			 */
//			RegularisationSearchCriteria ocCriteria = new RegularisationSearchCriteria();
//			ocCriteria.setPermitNumber(permitNumber.get(0));
//			ocCriteria.setTenantId(regularisation.getTenantId());
//			List<RGA> ocApplns = regularisationRepository.getRegularisationData(ocCriteria, null);
//			if (!ocApplns.isEmpty()) {
//				for (int i = 0; i < ocApplns.size(); i++) {
//					if (!ocApplns.get(i).getStatus().equalsIgnoreCase(RegularisationConstants.STATUS_REJECTED)) {
//						throw new CustomException(RegularisationErrorConstants.DUPLICATE_OC,
//								"Occupancy certificate application is already exists with permit approval Number "
//										+ permitNumber.get(0));
//					}
//				}
//			}
			additionalDetails.put(RGAConstants.PERMIT_NO, permitNumber.get(0));
		}
//		List<Double> plotAreas = context.read("edcrDetail.*.planDetail.plot.area", typeRef);
//		List<Double> buildingHeights = context.read("edcrDetail.*.planDetail.blocks.*.building.buildingHeight",
//				typeRef);

		if (CollectionUtils.isEmpty(edcrStatus) || !edcrStatus.get(0).equalsIgnoreCase("Accepted")) {
			throw new CustomException(RGAErrorConstants.INVALID_EDCR_NUMBER,
					"The EDCR Number is not Accepted " + edcrNo);
		}
//		this.validateOCEdcr(OccupancyTypes, plotAreas, buildingHeights, applicationType, masterData, riskType);

		return additionalDetails;
	}

	/**
	 * validate the ocEDCR values
	 * 
	 * @param OccupancyTypes
	 * @param plotAreas
	 * @param buildingHeights
	 * @param applicationType
	 * @param masterData
	 * @param riskType
	 */
	private void validateOCEdcr(List<String> OccupancyTypes, List<Double> plotAreas, List<Double> buildingHeights,
			LinkedList<String> applicationType, Map<String, List<String>> masterData, String riskType) {
		if (!CollectionUtils.isEmpty(OccupancyTypes) && !CollectionUtils.isEmpty(plotAreas)
				&& !CollectionUtils.isEmpty(buildingHeights)
				&& !applicationType.get(0).equalsIgnoreCase(RGAConstants.BUILDING_PLAN_OC)) {
			Double buildingHeight = Collections.max(buildingHeights);
			String OccupancyType = OccupancyTypes.get(0); // Assuming
															// OccupancyType
															// would be same in
															// the list
			Double plotArea = plotAreas.get(0);
//			log.info("masterData: " + masterData);
			List jsonOutput = JsonPath.read(masterData, RGAConstants.RISKTYPE_COMPUTATION);
			log.info("jsonOutput: " + jsonOutput);
			String filterExp = "";
			List<String> riskTypes = new ArrayList<String>();
			if (plotArea > 1000 || buildingHeight >= 15) {
//				filterExp = "$.[?((@.fromPlotArea < " + plotArea + " ) || ( @.fromBuildingHeight < " + buildingHeight
//						+ "  ))].riskType";
//				log.info("filterExp: " + filterExp);
//
//				riskTypes = JsonPath.read(jsonOutput, filterExp);
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
//			riskTypes = JsonPath.read(jsonOutput, filterExp);
			log.info("riskTypes: " + riskTypes);

			if (!CollectionUtils.isEmpty(riskTypes)
			// && OccupancyType.equals(RegularisationConstants.RESIDENTIAL_OCCUPANCY)
			) {
				String expectedRiskType = riskTypes.get(0);

				log.info("expectedRiskType: " + expectedRiskType);

				if (expectedRiskType == null || !expectedRiskType.equals(riskType)) {
					throw new CustomException(RGAErrorConstants.INVALID_RISK_TYPE,
							"The Risk Type is not valid " + riskType);
				}
			} else {
				throw new CustomException(RGAErrorConstants.INVALID_OCCUPANCY,
						"The OccupancyType " + OccupancyType + " is not supported! ");
			}
		}
	}

	/**
	 * fetch the edcrPdfUrl fron the bpa data
	 * 
	 * @param rGARequest
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getEDCRPdfUrl(RGARequest rGARequest) {

		RGA rGA = rGARequest.getRegularisation();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());
		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(rGA.getTenantId());
		uri.append("&").append("edcrNumber=").append(rGARequest.getRegularisation().getEdcrNumber());
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(rGARequest.getRequestInfo(), edcrRequestInfo);
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException(RGAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
		}

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		List<String> planReports = context.read("edcrDetail.*.planReport");

		return CollectionUtils.isEmpty(planReports) ? null : planReports.get(0);
	}

	/**
	 * fetch the edcr details from the bpa
	 * 
	 * @param requestInfo
	 * @param rGA
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, String> getEDCRDetails(org.egov.common.contract.request.RequestInfo requestInfo, RGA rGA) {

		String edcrNo = rGA.getEdcrNumber();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());

		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(rGA.getTenantId());
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(requestInfo, edcrRequestInfo);
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException(RGAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
		}

		if (CollectionUtils.isEmpty(responseMap))
			throw new CustomException(RGAErrorConstants.EDCR_ERROR, "The response from EDCR service is empty or null");

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		Map<String, String> edcrDetails = new HashMap<String, String>();
		List<String> serviceType = context.read("edcrDetail.*.planDetail.planInformation.serviceType");
		List<String> occupancy = context.read("edcrDetail.*.planDetail.planInformation.occupancy");
		if (CollectionUtils.isEmpty(serviceType)) {
			serviceType.add("REGULARISATION");
		}
		List<String> applicationType = context.read("edcrDetail.*.appliactionType");
		if (CollectionUtils.isEmpty(applicationType)) {
			applicationType.add("permit");
		}
		List<String> approvalNo = context.read("edcrDetail.*.permitNumber");
		edcrDetails.put(RGAConstants.SERVICETYPE, serviceType.get(0).toString());
		edcrDetails.put("Occupancy", occupancy.get(0).toString());
		edcrDetails.put(RGAConstants.APPLICATIONTYPE, applicationType.get(0).toString());
		if (approvalNo.size() > 0 && approvalNo != null) {
			edcrDetails.put(RGAConstants.PERMIT_NO, approvalNo.get(0).toString());
		}
		return edcrDetails;
	}

	/**
	 * get edcrNumbers from the bpa search criteria
	 * 
	 * @param searchCriteria
	 * @param requestInfo
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<String> getEDCRNos(RGASearchCriteria searchCriteria,
			org.egov.common.contract.request.RequestInfo requestInfo) {

		StringBuilder uri = new StringBuilder(config.getEdcrHost());
		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(searchCriteria.getTenantId());
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(requestInfo, edcrRequestInfo);
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException(RGAErrorConstants.EDCR_ERROR, " Invalid search criteria");
		}

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		List<String> edcrNos = context.read("edcrDetail.*.edcrNumber");

		return CollectionUtils.isEmpty(edcrNos) ? null : edcrNos;
	}

}
