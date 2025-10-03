package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.validator.MDMSValidator;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.edcr.RequestInfo;
import org.egov.bpa.web.model.edcr.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
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

	private BPAConfiguration config;

	@Autowired
	private MDMSValidator mdmsValidator;

	@Autowired
	BPARepository bpaRepository;

	@Autowired
	public EDCRService(ServiceRequestRepository serviceRequestRepository, BPAConfiguration config) {
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
	public Map<String, String> validateEdcrPlan(BPARequest request, Object mdmsData) {

		String edcrNo = request.getBPA().getEdcrNumber();
		String riskType = request.getBPA().getRiskType();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());
		BPA bpa = request.getBPA();

		BPASearchCriteria criteria = new BPASearchCriteria();
		criteria.setEdcrNumber(bpa.getEdcrNumber());

		Map<String, String> additionalDetails = bpa.getAdditionalDetails() != null
				? (Map<String, String>) bpa.getAdditionalDetails()
				: new HashMap<String, String>();

		String bpaApplicationType = additionalDetails.get(BPAConstants.APPLICATIONTYPE);
		if (bpaApplicationType.equals("BUILDING_OC_PLAN_SCRUTINY")) {
			criteria.setApplicationType("BUILDING_OC_PLAN_SCRUTINY");
		}

		List<BPA> bpas = bpaRepository.getBPAData(criteria, null);
		if (bpas.size() > 0) {
			for (int i = 0; i < bpas.size(); i++) {
				if (!bpas.get(i).getStatus().equalsIgnoreCase(BPAConstants.STATUS_REJECTED)
						&& !bpas.get(i).getStatus().equalsIgnoreCase(BPAConstants.STATUS_REVOCATED)) {
					throw new CustomException(BPAErrorConstants.DUPLICATE_EDCR,
							" Application already exists with EDCR Number " + bpa.getEdcrNumber());
				}
			}
		}

		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(bpa.getTenantId());
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(request.getRequestInfo(), edcrRequestInfo);
		Map<String, List<String>> masterData = mdmsValidator.getAttributeValues(mdmsData);

//		log.info("masterData: " + masterData);
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
//			log.info("responseMap: " + responseMap);
		} catch (ServiceCallException se) {
			throw new CustomException(BPAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
		}

		if (CollectionUtils.isEmpty(responseMap))
			throw new CustomException(BPAErrorConstants.EDCR_ERROR, "The response from EDCR service is empty or null");

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		List<String> edcrStatus = context.read("edcrDetail.*.status");
		List<String> OccupancyTypes = context
				.read("edcrDetail.*.planDetail.virtualBuilding.occupancyTypes.*.type.code");
		TypeRef<List<Double>> typeRef = new TypeRef<List<Double>>() {
		};
//		Map<String, String> additionalDetails = bpa.getAdditionalDetails() != null ? (Map) bpa.getAdditionalDetails()
//				: new HashMap<String, String>();
		LinkedList<String> serviceType = context.read("edcrDetail.*.applicationSubType");
		if (serviceType != null && !serviceType.isEmpty() && additionalDetails.get(BPAConstants.SERVICETYPE) != null
				&& !serviceType.get(0).equalsIgnoreCase(additionalDetails.get(BPAConstants.SERVICETYPE))) {
			throw new CustomException(BPAErrorConstants.INVALID_SERVICE_TYPE,
					"The service type is invalid, it is not matching with scrutinized plan service type "
							+ serviceType.get(0));
		}
		if (serviceType == null || serviceType.size() == 0) {
			serviceType.add("NEW_CONSTRUCTION");
		}
		LinkedList<String> applicationType = context.read("edcrDetail.*.appliactionType");
		if (applicationType != null && !applicationType.isEmpty()
				&& additionalDetails.get(BPAConstants.APPLICATIONTYPE) != null
				&& !applicationType.get(0).equalsIgnoreCase(additionalDetails.get(BPAConstants.APPLICATIONTYPE))
				&& !bpaApplicationType.equals("BUILDING_OC_PLAN_SCRUTINY")) {
			throw new CustomException(BPAErrorConstants.INVALID_APPLN_TYPE,
					"The application type is invalid, it is not matching with scrutinized plan application type "
							+ applicationType.get(0));
		}

		if (applicationType == null || applicationType.size() == 0) {
			applicationType.add("permit");
		}
		LinkedList<String> permitNumber = context.read("edcrDetail.*.permitNumber");

		if (bpaApplicationType.equals("BUILDING_OC_PLAN_SCRUTINY")) {
			List<String> far = context.read("edcrDetail.*.planDetail.farDetails.providedFar");
			List<String> coverage = context.read("edcrDetail.*.planDetail.coverage");
			List<String> buildingHeight = context.read("edcrDetail.*.planDetail.blocks.*.building.buildingHeight");
			List<String> plotArea = context.read("edcrDetail.*.planDetail.plot.plotBndryArea");
			List<String> totalBuitUpArea = context.read("edcrDetail.*.planDetail.virtualBuilding.totalBuitUpArea");
			// List<String> distanceToRoad =
			// context.read("edcrDetail.*.planDetail.roadReserves[].shortestDistanceToRoad");
			List<String> frontSetback = context.read("$.edcrDetail[*].planDetail.blocks[*].setBacks[*].frontYard.mean");
			List<String> rearSetback = context.read("$.edcrDetail[*].planDetail.blocks[*].setBacks[*].rearYard.mean");
			List<String> leftSetback = context.read("$.edcrDetail[*].planDetail.blocks[*].setBacks[*].sideYard1.mean");
			List<String> rightSetback = context.read("$.edcrDetail[*].planDetail.blocks[*].setBacks[*].sideYard2.mean");
			List<String> parkingProvided = context.read(
					"$.edcrDetail[*].planDetail.reportOutput.scrutinyDetails[?(@.key == 'Common_Parking')].detail[*].Provided");
			Map<String, Object> edcrDetails = new HashMap<>();

			edcrDetails.put("far", (far == null || far.size() == 0) ? 0 : far.get(0));
			edcrDetails.put("coverage", (coverage == null || coverage.size() == 0) ? 0 : coverage.get(0));
			edcrDetails.put("buildingHeight",
					(buildingHeight == null || buildingHeight.size() == 0) ? 0 : buildingHeight.get(0));
			edcrDetails.put("plotArea", (plotArea == null || plotArea.size() == 0) ? 0 : plotArea.get(0));
			edcrDetails.put("totalBuitUpArea",
					(totalBuitUpArea == null || totalBuitUpArea.size() == 0) ? 0 : totalBuitUpArea.get(0));
			edcrDetails.put("parking",
					(parkingProvided == null || parkingProvided.size() == 0) ? 0 : parkingProvided.get(0));
			edcrDetails.put("frontSetback",
					(frontSetback == null || frontSetback.size() == 0) ? 0 : frontSetback.get(0));
			edcrDetails.put("rearSetback", (rearSetback == null || rearSetback.size() == 0) ? 0 : rearSetback.get(0));
			edcrDetails.put("leftSetback", (leftSetback == null || leftSetback.size() == 0) ? 0 : leftSetback.get(0));
			edcrDetails.put("rightSetback",
					(rightSetback == null || rightSetback.size() == 0) ? 0 : rightSetback.get(0));

			additionalDetails.put("edcrDetails", edcrDetails.toString());
		}

//		additionalDetails.put("far",far.get(0));
//		additionalDetails.put("coverage",coverage.get(0));
//		additionalDetails.put("buildingHeight",buildingHeight.get(0));
//		additionalDetails.put("plotArea",plotArea.get(0));
//		additionalDetails.put("totalBuitUpArea",totalBuitUpArea.get(0));

		additionalDetails.put(BPAConstants.SERVICETYPE, serviceType.get(0));
		if (bpaApplicationType.equals("BUILDING_OC_PLAN_SCRUTINY")) {
			additionalDetails.put(BPAConstants.APPLICATIONTYPE, bpaApplicationType);
		} else {
			additionalDetails.put(BPAConstants.APPLICATIONTYPE, applicationType.get(0));
		}

		/*
		 * Validating OC application, with submitted permit number is any OC submitted
		 * without rejection. Using a permit number only one OC application submission
		 * should allowed otherwise needs to throw validation message for more one
		 * submission. If the OC application is rejected for a permit then we need
		 * allow.
		 */
		/*
		 * if (!permitNumber.isEmpty()) {
		 * 
		 * BPASearchCriteria ocCriteria = new BPASearchCriteria();
		 * ocCriteria.setPermitNumber(permitNumber.get(0));
		 * ocCriteria.setTenantId(bpa.getTenantId()); List<BPA> ocApplns =
		 * bpaRepository.getBPAData(ocCriteria, null); if (!ocApplns.isEmpty()) { for
		 * (int i = 0; i < ocApplns.size(); i++) { if
		 * (!ocApplns.get(i).getStatus().equalsIgnoreCase(BPAConstants.STATUS_REJECTED))
		 * { throw new CustomException(BPAErrorConstants.DUPLICATE_OC,
		 * "Occupancy certificate application is already exists with permit approval Number "
		 * + permitNumber.get(0)); } } } additionalDetails.put(BPAConstants.PERMIT_NO,
		 * permitNumber.get(0)); }
		 */
		List<Double> plotAreas = context.read("edcrDetail.*.planDetail.plot.area", typeRef);
		List<Double> buildingHeights = context.read("edcrDetail.*.planDetail.blocks.*.building.buildingHeight",
				typeRef);

		if (CollectionUtils.isEmpty(edcrStatus) || !edcrStatus.get(0).equalsIgnoreCase("Accepted")) {
			throw new CustomException(BPAErrorConstants.INVALID_EDCR_NUMBER,
					"The EDCR Number is not Accepted " + edcrNo);
		}
		this.validateOCEdcr(OccupancyTypes, plotAreas, buildingHeights, applicationType, masterData, riskType);

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
				&& !applicationType.get(0).equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			Double buildingHeight = Collections.max(buildingHeights);
			String OccupancyType = OccupancyTypes.get(0); // Assuming
															// OccupancyType
															// would be same in
															// the list
			Double plotArea = plotAreas.get(0);
//			log.info("masterData: " + masterData);
			List jsonOutput = JsonPath.read(masterData, BPAConstants.RISKTYPE_COMPUTATION);
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
			// && OccupancyType.equals(BPAConstants.RESIDENTIAL_OCCUPANCY)
			) {
				String expectedRiskType = riskTypes.get(0);

				log.info("expectedRiskType: " + expectedRiskType);

				if (expectedRiskType == null || !expectedRiskType.equals(riskType)) {
					throw new CustomException(BPAErrorConstants.INVALID_RISK_TYPE,
							"The Risk Type is not valid " + riskType);
				}
			} else {
				throw new CustomException(BPAErrorConstants.INVALID_OCCUPANCY,
						"The OccupancyType " + OccupancyType + " is not supported! ");
			}
		}
	}

	/**
	 * fetch the edcrPdfUrl fron the bpa data
	 * 
	 * @param bpaRequest
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getEDCRPdfUrl(BPARequest bpaRequest) {

		BPA bpa = bpaRequest.getBPA();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());
		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(bpa.getTenantId());
		uri.append("&").append("edcrNumber=").append(bpaRequest.getBPA().getEdcrNumber());
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(bpaRequest.getRequestInfo(), edcrRequestInfo);
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException(BPAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
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
	 * @param bpa
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, String> getEDCRDetails(org.egov.common.contract.request.RequestInfo requestInfo, BPA bpa) {

		String edcrNo = bpa.getEdcrNumber();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());

		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(bpa.getTenantId());
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(requestInfo, edcrRequestInfo);
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException(BPAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
		}

		if (CollectionUtils.isEmpty(responseMap))
			throw new CustomException(BPAErrorConstants.EDCR_ERROR, "The response from EDCR service is empty or null");

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		Map<String, String> edcrDetails = new HashMap<String, String>();
		List<String> serviceType = context.read("edcrDetail.*.planDetail.planInformation.serviceType");
		List<String> occupancy = context.read("edcrDetail.*.planDetail.planInformation.occupancy");
		if (CollectionUtils.isEmpty(serviceType)) {
			serviceType.add("NEW_CONSTRUCTION");
		}
		List<String> applicationType = context.read("edcrDetail.*.appliactionType");
		if (CollectionUtils.isEmpty(applicationType)) {
			applicationType.add("permit");
		}
		List<String> approvalNo = context.read("edcrDetail.*.permitNumber");
		edcrDetails.put(BPAConstants.SERVICETYPE, serviceType.get(0).toString());
		edcrDetails.put("Occupancy", occupancy.get(0).toString());
		edcrDetails.put(BPAConstants.APPLICATIONTYPE, applicationType.get(0).toString());
		if (approvalNo.size() > 0 && approvalNo != null) {
			edcrDetails.put(BPAConstants.PERMIT_NO, approvalNo.get(0).toString());
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
	public List<String> getEDCRNos(BPASearchCriteria searchCriteria,
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
			throw new CustomException(BPAErrorConstants.EDCR_ERROR, " Invalid search criteria");
		}

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		List<String> edcrNos = context.read("edcrDetail.*.edcrNumber");

		return CollectionUtils.isEmpty(edcrNos) ? null : edcrNos;
	}

	public Map<String, Object> getRiskTypeTest(BPARequest request, Object mdmsData) {

		log.info("inside EdcrService get Risk Type");
		Map<String, Object> returnMap = new HashMap<>();
		try {
			String edcrNo = request.getBPA().getEdcrNumber();
			StringBuilder uri = new StringBuilder(config.getEdcrHost());
			BPA bpa = request.getBPA();

			uri.append(config.getGetPlanEndPoint());
			uri.append("?").append("tenantId=").append(bpa.getTenantId());
			uri.append("&").append("edcrNumber=").append(edcrNo);
			RequestInfo edcrRequestInfo = new RequestInfo();
			BeanUtils.copyProperties(request.getRequestInfo(), edcrRequestInfo);
			Map<String, List<String>> masterData = mdmsValidator.getAttributeValues(mdmsData);

//		log.info("masterData: " + masterData);
			LinkedHashMap responseMap = null;
			try {
				responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
						new RequestInfoWrapper(edcrRequestInfo));
//			log.info("responseMap: " + responseMap);
			} catch (ServiceCallException se) {
				throw new CustomException(BPAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
			}

			if (CollectionUtils.isEmpty(responseMap))
				throw new CustomException(BPAErrorConstants.EDCR_ERROR,
						"The response from EDCR service is empty or null");

			String jsonString = new JSONObject(responseMap).toString();
			DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
//			TypeRef<List<Double>> typeRef = new TypeRef<List<Double>>() {
//			};
//
//			List<Double> plotAreas = context.read("edcrDetail.*.planDetail.plot.area", typeRef);
//			List<Double> buildingHeights = context.read("edcrDetail.*.planDetail.blocks.*.building.buildingHeight",
//					typeRef);

			Double plotArea = Double.valueOf(context.read("edcrDetail[0].planDetail.plot.area").toString());

			Double buildingHeight = Double
					.valueOf(context.read("edcrDetail[0].planDetail.blocks[0].building.buildingHeight").toString());

//			Double plotArea = plotAreas.get(0);
//		log.info("masterData: " + masterData);
			List jsonOutput = JsonPath.read(masterData, BPAConstants.RISKTYPE_COMPUTATION);
//		log.info("jsonOutput: " + jsonOutput);
			String filterExp = "";
			List<String> riskTypes = new ArrayList<String>();
//			Double buildingHeight = Collections.max(buildingHeights);
			if (plotArea > 1000 || buildingHeight >= 15) {
//			filterExp = "$.[?((@.fromPlotArea < " + plotArea + " ) || ( @.fromBuildingHeight < " + buildingHeight
//					+ "  ))].riskType";
//			log.info("filterExp: " + filterExp);
//
//			riskTypes = JsonPath.read(jsonOutput, filterExp);
				riskTypes.add("HIGH");
			} else {
				filterExp = "$.[?((@.fromPlotArea < " + plotArea + " && @.toPlotArea >= " + plotArea
						+ ") && ( @.fromBuildingHeight < " + buildingHeight + "  &&  @.toBuildingHeight >= "
						+ buildingHeight + "  ))].riskType";
//				log.info("filterExp: " + filterExp);

				riskTypes = JsonPath.read(jsonOutput, filterExp);
			}
			returnMap.put("plotArea", plotArea);
			returnMap.put("buildingHeight", buildingHeight);
			returnMap.put("riskType", riskTypes.get(0));
			log.info("riskTypes.get(0): " + riskTypes.get(0));
		} catch (Exception ex) {
			log.error("EdcerService.getRiskTypeTest Exception : " + ex.toString());
		}
		return returnMap;
//		this.validateOCEdcr(OccupancyTypes, plotAreas, buildingHeights, applicationType, masterData, riskType);

//		return additionalDetails;
	}

	public List<Map<String, Object>> getEDCRDetailsPtis(String edcrNo, String tenantId) {

		org.egov.common.contract.request.RequestInfo requestInfo = new org.egov.common.contract.request.RequestInfo();
//		String edcrNo = bpa.getEdcrNumber();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());

		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(tenantId);
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(requestInfo, edcrRequestInfo);
//		LinkedHashMap responseMap = null;
		Map<String, Object> response = new HashMap<>();
		try {
			response = (Map<String, Object>) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException(BPAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
		}

		List<Map<String, Object>> blocks = Optional.ofNullable((List<Map<String, Object>>) response.get("edcrDetail"))
				.filter(list -> !list.isEmpty()).map(list -> list.get(0))
				.map(edcrDetail -> (Map<String, Object>) edcrDetail.get("planDetail"))
				.map(planDetail -> (List<Map<String, Object>>) planDetail.get("blocks"))
				.orElse(Collections.emptyList());

		List<Map<String, Object>> floors = blocks.stream().map(block -> (Map<String, Object>) block.get("building"))
				.filter(Objects::nonNull).map(building -> (List<Map<String, Object>>) building.get("floors"))
				.filter(Objects::nonNull).flatMap(List::stream) // flatten all floors
				.map(floor -> {
					Map<String, Object> floorMap = new HashMap<>();
					int floorNo = Integer.parseInt(String.valueOf(floor.get("number")));

					List<Map<String, Object>> occupancies = (List<Map<String, Object>>) floor.get("occupancies");
					double totalFloorArea = occupancies == null ? 0.0
							: occupancies.stream().map(occ -> occ.get("floorArea")).filter(Objects::nonNull)
									.mapToDouble(obj -> Double.parseDouble(obj.toString())).sum();

					floorMap.put("Floor_No", String.valueOf(floorNo));
					floorMap.put("Floor_Area", String.valueOf(totalFloorArea));
					floorMap.put("Measurement_Unit", "SqM");
					floorMap.put("Floor_Name", getFloorName(floorNo));

					return floorMap;
				}).collect(Collectors.toList());

		return floors;
	}

	private static String getFloorName(int number) {
		if (number < 0) {
			return "Basement";
		} else if (number == 0) {
			return "Ground Floor";
		} else if (number == 1) {
			return "First Floor";
		} else if (number == 2) {
			return "Second Floor";
		} else if (number == 3) {
			return "Third Floor";
		} else if (number == 4) {
			return "Fourth Floor";
		} else if (number == 5) {
			return "Fifth Floor";
		} else if (number == 6) {
			return "Sixth Floor";
		} else if (number == 7) {
			return "Seventh Floor";
		} else if (number == 8) {
			return "Eighth Floor";
		} else if (number == 9) {
			return "Nineth Floor";
		} else if (number == 10) {
			return "Tenth Floor";
		} else {
			return "Floor";
		}

//		switch (number) {
//		case 0:
//			return "GF";
//		case 1:
//			return "FF";
//		case 2:
//			return "SF";
//		case 3:
//			return "TF";
//		case 4:
//			return "FF";
//		default:
//			return number + "F"; 
//		}
	}
}
