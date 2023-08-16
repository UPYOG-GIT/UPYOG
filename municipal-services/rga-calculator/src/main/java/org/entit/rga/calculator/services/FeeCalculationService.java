package org.entit.rga.calculator.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.entit.rga.calculator.config.RGACalculatorConfig;
import org.entit.rga.calculator.repository.RGARepository;
import org.entit.rga.calculator.repository.ServiceRequestRepository;
import org.entit.rga.calculator.utils.RGACalculatorConstants;
import org.entit.rga.calculator.web.models.CalculationReq;
import org.entit.rga.calculator.web.models.PayTypeFeeDetailRequest;
import org.entit.rga.calculator.web.models.rga.RGA;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class FeeCalculationService {

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private RGACalculatorConfig config;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private RGARepository rgaRepository;

	public Object mDMSCall(CalculationReq calculationReq, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(calculationReq, tenantId);
		StringBuilder url = getMdmsSearchUrl();
		Object result = serviceRequestRepository.fetchResult(url, mdmsCriteriaReq);
		return result;
	}

	/**
	 * Creates and returns the url for mdms search endpoint
	 *
	 * @return MDMS Search URL
	 */
	private StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsSearchEndpoint());
	}

	/**
	 * Creates MDMS request
	 * 
	 * @param requestInfo The RequestInfo of the calculationRequest
	 * @param tenantId    The tenantId of the tradeLicense
	 * @return MDMSCriteria Request
	 */
	private MdmsCriteriaReq getMDMSRequest(CalculationReq calculationReq, String tenantId) {
		RequestInfo requestInfo = calculationReq.getRequestInfo();
		List<MasterDetail> rgaMasterDetails = new ArrayList<>();

		rgaMasterDetails.add(MasterDetail.builder().name(RGACalculatorConstants.MDMS_CALCULATIONTYPE).build());
		ModuleDetail rgaModuleDtls = ModuleDetail.builder().masterDetails(rgaMasterDetails)
				.moduleName(RGACalculatorConstants.MDMS_RGA).build();

		List<ModuleDetail> moduleDetails = new ArrayList<>();

		moduleDetails.add(rgaModuleDtls);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * Gets the calculationType for the city for a particular financialYear If for
	 * particular financialYear entry is not there previous year is taken If MDMS
	 * data is not available default values are returned
	 * 
	 * @param requestInfo The RequestInfo of the calculationRequest
	 * @param license     The tradeLicense for which calculation is done
	 * @return Map contianing the calculationType for TradeUnit and accessory
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getCalculationType(RequestInfo requestInfo, RGA rga, Object mdmsData, String feeType) {
		HashMap<String, Object> calculationType = new HashMap<>();
		try {
//			if (feeType.equalsIgnoreCase(RGACalculatorConstants.MDMS_CALCULATIONTYPE_SANC_FEETYPE)) {
//				log.info("inside sancFee if condition......");
//				String consumerCode = rga.getApplicationNo();
//				log.info("consumerCode: " + consumerCode);
//				Map sancFeeMap = new HashMap();
//				try {
//					String[] SancFee = rgaRepository.getSanctionFeeAmount(consumerCode);
//					log.info("SancFee from DB: " + SancFee.toString());
//					if (SancFee.length != 0) {
//						Double totalSancFeeAmount = Double.valueOf(SancFee[SancFee.length - 1]);
//						log.info("totalSancFeeAmount: " + totalSancFeeAmount);
//						sancFeeMap.put(RGACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT, totalSancFeeAmount);
//						return sancFeeMap;
//					} else {
//						log.error("Sanction Fee not found in DB");
//						throw new CustomException(RGACalculatorConstants.CALCULATION_ERROR, "Sanction Fee not found");
//					}
//				} catch (Exception ex) {
//					log.error("Exception in SancFee condition: " + ex);
//					throw new CustomException(RGACalculatorConstants.CALCULATION_ERROR, "Sanction Fee not found");
//				}
//			}

			List jsonOutput = JsonPath.read(mdmsData, RGACalculatorConstants.MDMS_CALCULATIONTYPE_PATH);
			LinkedHashMap responseMap = edcrService.getEDCRDetails(requestInfo, rga);
//			Map responseMap1 = feeCalculation(responseMap);
			log.info("jsonOutput logg :======= " + jsonOutput);

			log.info("feeType: " + feeType);
//			log.info("responseMap: " + responseMap);

			String jsonString = new JSONObject(responseMap).toString();
			DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
			Map<String, String> additionalDetails = new HashMap<String, String>();

//			log.info("context logg:======= " + context.jsonString());

			Double plotArea = Double
					.valueOf(context.read("edcrDetail[0].planDetail.planInformation.plotArea").toString());
			log.info("plotArea context.read -----" + context.read("edcrDetail[0].planDetail.planInformation.plotArea"));
			log.info("plotArea  ---  " + plotArea);
//			JSONArray occupancyType = context.read("edcrDetail[0].planDetail.planInformation.occupancy");
			log.info("context occupancy: " + context.read("edcrDetail.*.planDetail.planInformation.occupancy"));
			JSONArray occupancyType = context.read("edcrDetail.*.planDetail.planInformation.occupancy");
			log.info("occupancyType: " + occupancyType);
			additionalDetails.put("occupancyType", occupancyType.get(0).toString());

			log.info("occupancy Condition: " + (additionalDetails.get("occupancyType") == "Residential"));
			log.info("plotArea Condition: " + (plotArea <= 500.00));
			log.info("occupancy Condition:(equals) " + additionalDetails.get("occupancyType").equals("Residential"));

//			added ----- auto calculation----------------------------------------------
			String appDate = context.read("edcrDetail[0].applicationDate");
			log.info("appDate:-----" + appDate);
			
			String appNum = rga.getApplicationNo();
			log.info("appNum:----- " + appNum);
			String tenantid = rga.getTenantId();
			log.info("tenantid:----- " + tenantid);
			String bCate = context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].type.name");
			log.info("bcate:----- " + bCate);

			Integer bcategory = rgaRepository.getBcategoryId(bCate.toString(), tenantid);
			log.info("bcategory: " + bcategory + "");

			String status = context.read("edcrDetail[0].status");
			log.info("status:-----" + status);
			
			Integer scategory = 0;
		
			
			String subCate = context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].subtype.name");
			log.info("subcate:----- " + subCate);
			scategory = rgaRepository.getScategoryId(subCate.toString(), bcategory, tenantid);
			log.info("scategory: " + scategory + "");
	
			JSONArray parkDetails11 = context.read(
					"edcrDetail[0].planDetail.reportOutput.scrutinyDetails[?(@.key==\"Common_Parking\")].detail[0].Provided");
			JSONArray parkDetails12 = context.read(
					"edcrDetail[0].planDetail.reportOutput.scrutinyDetails[?(@.key==\"Common_Parking Details\")].detail[0].['Open Parking']");
			log.info("parkDetails11====:----- " + parkDetails11.toString());
			log.info("parkDetails12====:----- " + parkDetails12.toString());
			String totalParkArea = parkDetails11.get(0).toString();
			String totalParkArea1 = parkDetails12.get(0).toString();

			String zonedesc = context.read("edcrDetail[0].planDetail.planInfoProperties.DEVELOPMENT_ZONE");

			ArrayList<LinkedHashMap> block = context.read("edcrDetail[0].planDetail.blocks");
//			log.info("block LinkedHashMap===="+block);

			Double ResArea = 0d;
			Double CommArea = 0d;
			Double IndArea = 0d;
			boolean isHighRisetf = false;

			for (LinkedHashMap blockMap : block) {
				HashMap building = (HashMap) blockMap.get("building");

//				 log.info("blockMap======"+building);
				ArrayList<LinkedHashMap> floor = (ArrayList<LinkedHashMap>) building.get("floors");
//				 log.info("floor======"+floor);

				isHighRisetf = (boolean) building.get("isHighRise");
				log.info("isHighRisetf-------" + isHighRisetf);

				for (LinkedHashMap floorMap : floor) {
					log.info("floorMap-------" + floorMap);

					ArrayList<LinkedHashMap> getOccupancies = (ArrayList<LinkedHashMap>) floorMap.get("occupancies");
					log.info("getOccupancies-------" + getOccupancies);

					for (LinkedHashMap getOccupanciesMap : getOccupancies) {
						log.info("getOccupanciesMap-------" + getOccupanciesMap);

						HashMap typeHelper = (HashMap) getOccupanciesMap.get("typeHelper");
						log.info("typeHelper=====" + typeHelper);

						log.info("typeHelper size----- " + typeHelper.size());

						if (typeHelper.size() == 0) {
							continue;
						}

						HashMap typeOcc = (HashMap) typeHelper.get("type");
						log.info("typeOcc=====" + typeOcc);

						String nameOcc = (String) typeOcc.get("name");
						log.info("nameOcc=====" + nameOcc);

						if (nameOcc.equals("Residential")) {
							ResArea += (Double) getOccupanciesMap.get("floorArea");
							log.info("ResArea=====" + ResArea);
						} else if (nameOcc.equals("Mercantile / Commercial")) {
							CommArea += (Double) getOccupanciesMap.get("floorArea");
							log.info("CommArea=====" + CommArea);
						} else if (nameOcc.equals("Industrial")) {
							IndArea += (Double) getOccupanciesMap.get("floorArea");
							log.info("IndArea=====" + IndArea);
						} else {
							throw new CustomException(RGACalculatorConstants.CALCULATION_ERROR,
									"Not a valid occupency");
						}

					}

				}
			}

			additionalDetails.put("appDate", appDate.toString());
			additionalDetails.put("appNum", appNum.toString());
			additionalDetails.put("plotares", plotArea.toString());
			additionalDetails.put("feeType", feeType.toString());
			additionalDetails.put("tenantid", tenantid.toString());
			additionalDetails.put("bcate", bcategory + "");
			additionalDetails.put("subcate", scategory + "");
			additionalDetails.put("totalParkArea", totalParkArea);
			additionalDetails.put("openParkArea", totalParkArea1);
			additionalDetails.put("zonedesc", zonedesc.toString());
			additionalDetails.put("ResArea", ResArea.toString());
			additionalDetails.put("CommArea", CommArea.toString());
			additionalDetails.put("IndArea", IndArea.toString());
			additionalDetails.put("isHighRisetf", isHighRisetf + "");
			additionalDetails.put("status", status.toString());

			log.info("additionalDetails---------" + additionalDetails);
			Double responseMap1 = feeCalculation(additionalDetails);

			log.info("responseMap1----------" + responseMap1);

			List<Object> calTypes = new ArrayList<>();
			calTypes.add(responseMap1);
			log.info("calTypes plotArea ----  " + calTypes);
			log.info("calTypes.size(): " + calTypes.size());

			if (calTypes.size() == 0) {
				log.info("================should not enter==========");
				return defaultMap(feeType);
			}
			Object obj = calTypes.get(0);
			log.info("obj-----" + obj);
//			calculationType = (HashMap<String, Object>) obj;
			calculationType.put(RGACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT, obj);


		} catch (Exception e) {
			log.error("Exception :" + e);
			throw new CustomException(RGACalculatorConstants.CALCULATION_ERROR, "Failed to get calculationType");
		}

		log.info("calculationType-----------" + calculationType);
		return calculationType;
	}

	/**
	 * Creates and return default calculationType values as map
	 * 
	 * @return default calculationType Map
	 */
	private Map defaultMap(String feeType) {
		Map defaultMap = new HashMap();
		String feeAmount = (feeType.equalsIgnoreCase(RGACalculatorConstants.MDMS_CALCULATIONTYPE_APL_FEETYPE))
				? config.getApplFeeDefaultAmount()
				: config.getSancFeeDefaultAmount();
		defaultMap.put(RGACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT, feeAmount);
		return defaultMap;
	}

//	private Map<String, String> feeCalculation(Map data) {
	private Double feeCalculation(Map data) {

		log.info("Data  " + data);
		String feetype = data.get("feeType").toString();
		log.info("feetype----" + feetype);
		String tenantid = data.get("tenantid") + "";
		log.info("tenantid----" + tenantid);
		String occupancyType = data.get("occupancyType") + "";
		log.info("occupancyType----" + occupancyType);
		Double plotArea = Double.valueOf(data.get("plotares").toString());
		log.info("plotares----" + plotArea);
		String applicationNo = data.get("appNum").toString();
		log.info("applicationNo----" + applicationNo);
		Integer bcategory = Integer.parseInt(data.get("bcate").toString());
		log.info("bcategory----" + bcategory);
		String tolpark = (data.get("totalParkArea").toString()).replace("m2", "");
		String openpark = (data.get("openParkArea").toString());
		log.info("tolpark----" + tolpark);
		log.info("tolpark1----" + openpark);
//		().replace('m2','');
		Double totalParkArea = Double.valueOf(tolpark);
		Double openParkArea = Double.valueOf(openpark);
		log.info("totalParkArea----" + totalParkArea);
		log.info("openParkArea----" + openParkArea);
		Integer subcate = Integer.parseInt(data.get("subcate").toString());
		log.info("subcate----" + subcate);
		String appDate = data.get("appDate").toString();
		log.info("appDate----" + appDate);
		

		Double sFromVal = 0.0;
		Double sToVal = 0d;
		Double bpfAmount = 0.0;
		Double amount = 0.0;
		Double totalAmount = 0.0;
		boolean isPenalty = false;
		int count = 1;
	

		List<HashMap<String, Object>> rgaFeeDetailMap = new ArrayList<HashMap<String, Object>>();
		PayTypeFeeDetailRequest payTypeFeeDetailRequest = new PayTypeFeeDetailRequest();

	


		log.info("Inside SlabWise Condition***************");

		List<Map<String, Object>> slabResult = rgaRepository.getDetailOfRgaSlabMaster(tenantid, plotArea,
				occupancyType);

		if (slabResult.isEmpty()) {
			throw new CustomException(RGACalculatorConstants.CALCULATION_ERROR,
					"No slab entry found for this category and area range");

		}

		log.info("slabResult------" + slabResult.toString());

		

		for (Map<String, Object> sResult : slabResult) {

			log.info("sResult-----" + sResult.toString());
			log.info("to val-------" + sResult.get("to_val"));

			sFromVal = Double.valueOf(sResult.get("from_val").toString());
			sToVal = Double.valueOf(sResult.get("to_val").toString());
			bpfAmount = Double.valueOf(sResult.get("rate").toString());


			amount = bpfAmount;
		    totalAmount = bpfAmount;
			
		  
		   Double penalty = 0.0;
			if (data.get("status").equals("Partial Accepted")) {
				
				 penalty = rgaRepository.getPenalty(occupancyType, tenantid, plotArea);

				if (penalty == 0.0) {
					throw new CustomException(RGACalculatorConstants.CALCULATION_ERROR,
							"No Penalty found for this category and area range");

				}
				isPenalty = true;

				totalAmount = amount * penalty;

				log.info("slabResult------" + slabResult.toString());
				log.info( "sFromVal==" + sFromVal + "sToVal==" + sToVal + "rate==" + bpfAmount
						);

			}
			    payTypeFeeDetailRequest.setApplicationNo(applicationNo);
			    payTypeFeeDetailRequest.setTenantId(tenantid);
			    payTypeFeeDetailRequest.setBillId("");
				payTypeFeeDetailRequest.setChargesTypeName("Building Permission Fee");
				payTypeFeeDetailRequest.setRate(bpfAmount);
				payTypeFeeDetailRequest.setAmount(amount);
				payTypeFeeDetailRequest.setBillId("");
				payTypeFeeDetailRequest.setSrNo(count);
				payTypeFeeDetailRequest.setPenalty(isPenalty);
				payTypeFeeDetailRequest.setPenaltyMultiplication(penalty);
				
				rgaRepository.createRgaFeeDetail(payTypeFeeDetailRequest);
			
		}

		return totalAmount;
	}

}
