package org.egov.bpa.calculator.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.repository.BPARepository;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.web.models.CalculationReq;
import org.egov.bpa.calculator.web.models.PayTypeFeeDetailRequest;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
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
public class MDMSService {

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private BPACalculatorConfig config;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private BPARepository bpaRepository;

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
		List<MasterDetail> bpaMasterDetails = new ArrayList<>();

		bpaMasterDetails.add(MasterDetail.builder().name(BPACalculatorConstants.MDMS_CALCULATIONTYPE).build());
		ModuleDetail bpaModuleDtls = ModuleDetail.builder().masterDetails(bpaMasterDetails)
				.moduleName(BPACalculatorConstants.MDMS_BPA).build();

		List<ModuleDetail> moduleDetails = new ArrayList<>();

		moduleDetails.add(bpaModuleDtls);

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
	public Map getCalculationType(RequestInfo requestInfo, BPA bpa, Object mdmsData, String feeType) {
		HashMap<String, Object> calculationType = new HashMap<>();
		try {
//			if (feeType.equalsIgnoreCase(BPACalculatorConstants.MDMS_CALCULATIONTYPE_SANC_FEETYPE)) {
//				log.info("inside sancFee if condition......");
//				String consumerCode = bpa.getApplicationNo();
//				log.info("consumerCode: " + consumerCode);
//				Map sancFeeMap = new HashMap();
//				try {
//					String[] SancFee = bpaRepository.getSanctionFeeAmount(consumerCode);
//					log.info("SancFee from DB: " + SancFee.toString());
//					if (SancFee.length != 0) {
//						Double totalSancFeeAmount = Double.valueOf(SancFee[SancFee.length - 1]);
//						log.info("totalSancFeeAmount: " + totalSancFeeAmount);
//						sancFeeMap.put(BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT, totalSancFeeAmount);
//						return sancFeeMap;
//					} else {
//						log.error("Sanction Fee not found in DB");
//						throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR, "Sanction Fee not found");
//					}
//				} catch (Exception ex) {
//					log.error("Exception in SancFee condition: " + ex);
//					throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR, "Sanction Fee not found");
//				}
//			}

			List jsonOutput = JsonPath.read(mdmsData, BPACalculatorConstants.MDMS_CALCULATIONTYPE_PATH);
			LinkedHashMap responseMap = edcrService.getEDCRDetails(requestInfo, bpa);
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
			String appNum = bpa.getApplicationNo();
			log.info("appNum:----- " + appNum);
			String tenantid = bpa.getTenantId();
			log.info("tenantid:----- " + tenantid);
			String bCate = context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].type.name");
			log.info("bcate:----- " + bCate);

			Integer bcategory = bpaRepository.getBcategoryId(bCate.toString(), tenantid);
			log.info("bcategory: " + bcategory + "");

			// log.info("isnullsu======="+context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].subtype"));

			// String isnullsubCate =
			// context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].subtype");
//			isnullsubCate.isNull("sfvs");

			// log.info("isnullsubCate---"+isnullsubCate);
			Integer scategory = 0;
			// if(!isnullsubCate.equals(null)) {
			// log.info("T/F===="+(!isnullsubCate.equals(null)));
			String subCate = context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].subtype.name");
			log.info("subcate:----- " + subCate);
			scategory = bpaRepository.getScategoryId(subCate.toString(), bcategory, tenantid);
			log.info("scategory: " + scategory + "");
			// }
//			else {
//				scategory=null;
//			}

//			Map parkDetails = context.read("edcrDetail[0].planDetail.reportOutput.scrutinyDetails[6]");
//			log.info("totalparkarea:----- " +parkDetails.toString());
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
							throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR,
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
			calculationType.put(BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT, obj);

//			added end----- auto calculation--------------------------------------------			

//			if (((plotArea <= 500.00) && (additionalDetails.get("occupancyType").equals("Residential")))) {
//	     		   String filterExp = "$.[?(@.amount==1)]";
//				String filterExp = "$.[?(@.feeType=='" + feeType + "')]";
//				log.info("filterExp--------" + filterExp);
//				List<Object> calTypes = JsonPath.read(jsonOutput, filterExp);
//				List<Object> calTypes = new ArrayList<>();
//				calTypes.add("");
//				log.info("calTypes plotArea ----  " + calTypes);
//				log.info("calTypes.size(): " + calTypes.size());
//				if (calTypes.size() == 0) {
//					log.info("================should not enter==========");
//					return defaultMap(feeType);
//				}
//				Object obj = calTypes.get(0);
//				calculationType = (HashMap<String, Object>) obj;
//			}
//			else {
//
//				JSONArray serviceType = context.read("edcrDetail.*.applicationSubType");
//				if (CollectionUtils.isEmpty(serviceType)) {
//					serviceType.add("NEW_CONSTRUCTION");
//				}
//				JSONArray applicationType = context.read("edcrDetail.*.appliactionType");
//				if (StringUtils.isEmpty(applicationType)) {
//					applicationType.add("permit");
//				}
//				additionalDetails.put("applicationType", applicationType.get(0).toString());
//				additionalDetails.put("serviceType", serviceType.get(0).toString());
//
//				log.info("context totalBuiltupare"
//						+ context.read("edcrDetail[0].planDetail.virtualBuilding.totalBuitUpArea"));
//				Double totalBuitUpArea = context.read("edcrDetail[0].planDetail.virtualBuilding.totalBuitUpArea");
//				log.info("totalBuitUpArea: " + totalBuitUpArea);
//
//				additionalDetails.put("totalBuitUpArea", totalBuitUpArea.toString());
//
//				log.info("JSONArray occupancyType ki value :--------- " + occupancyType.get(0).toString()
//						+ "=========JSONArray totalBuitUpArea ki value:----" + totalBuitUpArea.toString());
//				log.info("occupancyType form additional=====:  " + additionalDetails.get("occupancyType"));
//				log.info("totalBuitUpArea form additional=====:  " + additionalDetails.get("totalBuitUpArea"));
////             log.debug("applicationType is " + additionalDetails.get("applicationType"));
////             log.debug("serviceType is " + additionalDetails.get("serviceType"));
//				log.info("applicationType: " + additionalDetails.get("applicationType"));
//				String filterExp = "$.[?((@.applicationType == '" + additionalDetails.get("applicationType")
//						+ "' || @.applicationType === 'ALL' ) &&  @.feeType == '" + feeType + "')]";
//				List<Object> calTypes = JsonPath.read(jsonOutput, filterExp);
//				log.info("calTypes10: " + calTypes);
//
//				log.info("serviceType: " + additionalDetails.get("serviceType"));
//				filterExp = "$.[?(@.serviceType == '" + additionalDetails.get("serviceType")
//						+ "' || @.serviceType === 'ALL' )]";
//				calTypes = JsonPath.read(calTypes, filterExp);
//				log.info("calTypes11: " + calTypes);
////            
////             filterExp = "$.[?(@.riskType == '"+bpa.getRiskType()+"' || @.riskType === 'ALL' )]";
////             calTypes = JsonPath.read(calTypes, filterExp);
//
////             ----added by manisha for filter amount-------
//				filterExp = "$.[?(@.occupancyType == '" + additionalDetails.get("occupancyType") + "')]";
//				log.info("filterExp:------ " + filterExp);
//				calTypes = JsonPath.read(calTypes, filterExp);
//				log.info("calTypes12: " + calTypes);
////			log.info("calTypes(JsonPath.read(jsonOutput, filterExp) : " + calTypes);
//
//				filterExp = "$.[?(@.builtupAreaFrom <=" + additionalDetails.get("totalBuitUpArea")
//						+ "&& @.builtupAreaTo >=" + additionalDetails.get("totalBuitUpArea") + ")]";
//				log.info("filterExp:------ " + filterExp);
//				calTypes = JsonPath.read(calTypes, filterExp);
//				log.info("calTypes14: " + calTypes);
//
////			log.info("calTypes = JsonPath.read(calTypes, filterExp): " + calTypes);
//
////             ----added by manisha for filter amount-------
//
//				if (calTypes.size() > 1) {
//					filterExp = "$.[?(@.riskType == '" + bpa.getRiskType() + "' )]";
//					calTypes = JsonPath.read(calTypes, filterExp);
//					log.info("calTypes15: " + calTypes);
//				}
//
//				if (calTypes.size() == 0) {
//					log.info("================should not enter==========");
//					return defaultMap(feeType);
//				}
//
//				log.info("calTypes--------------" + calTypes + "==========calTypes.get(0)==========" + calTypes.get(0));
//				Object obj = calTypes.get(0);
//
//				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
//
//				String financialYear = currentYear + "-" + (currentYear + 1);
////			System.out.println(financialYear);
//
//				calculationType = (HashMap<String, Object>) obj;
//			}
		} catch (Exception e) {
			log.error("Exception :" + e);
			throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR, "Failed to get calculationType");
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
		String feeAmount = (feeType.equalsIgnoreCase(BPACalculatorConstants.MDMS_CALCULATIONTYPE_APL_FEETYPE))
				? config.getApplFeeDefaultAmount()
				: config.getSancFeeDefaultAmount();
		defaultMap.put(BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT, feeAmount);
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
		String zonedesc = data.get("zonedesc").toString();
		log.info("zonedesc----" + zonedesc);
		Double res_area = Double.valueOf(data.get("ResArea").toString());
		log.info("res_area----" + res_area);
		Double com_area = Double.valueOf(data.get("CommArea").toString());
		log.info("com_area----" + com_area);
		Double ind_area = Double.valueOf(data.get("IndArea").toString());
		log.info("ind_area----" + ind_area);
		boolean isHighRise = Boolean.parseBoolean(data.get("isHighRisetf").toString());
		log.info("isHighRise----" + isHighRise);
	

		String heightcat = "";

		if (isHighRise) {
			heightcat = "HR";
		} else {
			heightcat = "NH";
		}

		String feety = "";
		String brkflg = "";
//		String heightcat = "NH";
		String newrevise = "NEW";
		Integer pCategory = 0;
		Double totalAmount = 0.0;
		boolean chkflg = false;
		Double calcval = 0.0;
		int lackshect = (100000 / 10000); // 1Lacks/Hector //190413 -use to calcualte Postapproal-RainHarvesting Charges
											// 1lacks/hector-this will be fetch from table either loc or paytype for
											// rainharvesting
		Double netplot_area = 0.0;// have to add actual ploat area from dxf file
		String calcact = "";
//		Double res_area = 171.03;
//		Double com_area = 0.0;
//		Double ind_area = 0.0;
		Double res_unit = 5.0;

		List<HashMap<String, Object>> feeDetailMap = new ArrayList<HashMap<String, Object>>();
		List<PayTypeFeeDetailRequest> feeDetailList = new ArrayList<PayTypeFeeDetailRequest>();

		if (feetype.equals("ApplicationFee")) {
			feety = "Pre";
		} else if (feetype.equals("SanctionFee")) {
			feety = "Post";
		}

//		 if((occupancyType.split(",")).length>1) {
//			 log.info("index-----");
//		 }

		if (occupancyType.equals("Residential")) {
			pCategory = 1; // this is srno. of proposal type master based on occupency
			totalParkArea -= openParkArea;
		} else if (occupancyType.equals("Mercantile / Commercial")) {
			pCategory = 2; // this is srno. of proposal type master based on occupency
		} else if (occupancyType.equals("INDUSTRIAL")) {
			pCategory = 3; // this is srno. of proposal type master based on occupency
		}
//		else if(occupancyType.equals("MIX")) {
		else if ((occupancyType.split(",")).length > 1) {
			pCategory = 4; // this is srno. of proposal type master based on occupency
		}

		if (feety.equalsIgnoreCase("Pre")) {
			// for hight rise----------
			log.info("-------------inside hight rise-----------");
		}

		log.info("tenantid--" + tenantid + "---feety---" + feety);

		List<Map<String, Object>> result = bpaRepository.getPaytyData(tenantid, feety, occupancyType, plotArea,
				heightcat, newrevise);

//		String  unitid ="";
		log.info("result--0-----" + result.toString());
		int count = 1;
		for (Map<String, Object> item : result) {
			Double calculationArea = 0.0; // Area used for Calculation
			Double calculationRate = 0.0; // Rate used for Calculation
			Double amount = 0.0; // Calculated Value
			Double trate = 0.0;
			Double ptarea = 0.0;
			Double parkArea = 0.0;
			HashMap<String, Object> feeMap = new HashMap<String, Object>();
			PayTypeFeeDetailRequest payTypeFeeDetailRequest = new PayTypeFeeDetailRequest();

//			String pId = item.get("id").toString();
			String chargesTy = item.get("charges_type_name").toString();

			if (feety.equalsIgnoreCase("Pre")) {
				if (heightcat.equals("NH") && newrevise.equals("REVISED")) {
					log.info("------------REVISED----------");
				}
			}
			if (brkflg.equals("")) {
				if (item.get("zdaflg").equals("N")) {

//					log.info("blockDetail==="+blockDetail);

//					log.info("blockDetail size==="+blockDetail.size());

//					for(Map<String,Object>  eachBlockDetail:blockDetail) {
//						log.info("eachBlockDetail===="+eachBlockDetail);
//					}

					int paytyid = Integer.parseInt(item.get("id").toString());

					if (totalParkArea > 0) {
						parkArea = totalParkArea;
					}

					Integer countPayTyrate = bpaRepository.getCountOfPaytyrate(tenantid, paytyid, pCategory);
					log.info("paytyid-------" + paytyid + "  pCategory--------" + pCategory + "  countPayTyrate: "
							+ countPayTyrate);

					if (countPayTyrate.equals(0)) {
						throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR,
								"No pay type rate entry found for  this id");

					}

					log.info("bcategory--" + bcategory + "subcate----" + subcate);
					Map<String, Object> detailPayTyrate = bpaRepository.getDetailOfPaytyrate(tenantid, paytyid,
							pCategory, countPayTyrate, bcategory, subcate);
					log.info("detailPayTyrate : " + detailPayTyrate.toString());

					Integer p_category = Integer.parseInt(detailPayTyrate.get("p_category").toString());
					Integer b_category = Integer.parseInt(detailPayTyrate.get("b_category").toString());
					Integer s_category = Integer.parseInt(detailPayTyrate.get("s_category").toString());
					String unitid = detailPayTyrate.get("unitid").toString();
					String calcon = detailPayTyrate.get("calcon").toString();
					calcact = detailPayTyrate.get("calcact").toString();
					Double rate_res = Double.valueOf(detailPayTyrate.get("rate_res").toString());
					Double rate_comm = Double.valueOf(detailPayTyrate.get("rate_comm").toString());
					Double rate_ind = Double.valueOf(detailPayTyrate.get("rate_ind").toString());
					Double perval = Double.valueOf(detailPayTyrate.get("perval").toString());

					log.info("p_category==" + p_category + "b_category==" + b_category + "s_category==" + s_category
							+ "unitid==" + unitid + "calcon==" + calcon + "calcact==" + calcact + "rate_res=="
							+ rate_res + "rate_comm==" + rate_comm + "rate_ind==" + rate_ind + "perval==" + perval);

					if (b_category.equals(null) || b_category.equals(0))
						bcategory = 0;
					if (s_category.equals(null) || s_category.equals(0))
						s_category = 0;

//					if (!calcon.equals("Buildup Area")) 
					if (!calcon.equals("Buildup Area") || s_category.equals(1))
					// Parking area is applicable only for calculation based on
					// Build up Area
					{
						parkArea = 0.0;
					}

					// SET Default Area & Rate Category wise For Rate Master

					if (pCategory.equals(1)) { // RESIDENTIAL
						calculationArea = res_area + parkArea;
						calculationRate = rate_res;
					} else if (pCategory.equals(2) || pCategory.equals(5)) { // COMMERCIAL/EDUCATIONAL
						calculationArea = com_area + parkArea;
						calculationRate = rate_comm;
					} else if (pCategory.equals(3)) { // INDUSTRIAL
						calculationArea = ind_area + parkArea;
						calculationRate = rate_comm;
					} else if (pCategory.equals(4)) { // MIX
						if (calcon.equals("Buildup Area")) {
							calculationArea = res_area + com_area;
							calculationRate = rate_res + rate_comm;
						} else {
							calculationArea = res_area + com_area;
							calculationRate = rate_res + rate_comm;
						}
					}

					if (calcon.equals("Plot Area")) {
						calculationArea = plotArea; // change above pcat area but rate will be same
					}

					// FOR SLAB AND NON-SLAB
					if (!calcact.equals("Slabwise")) // FOR NO SLAB !'S' like post-'Development Charges,SubTax',
														// 'Pre-'Scrutiny Charges (HR-NEW)'
					{
						log.info("Inside Non-SlabWise Condition***************");
						log.info("Area TPRate1: " + calculationArea);
						log.info("Rate TPRate1: " + calculationRate);
						if (calcon.equals("Buildup Area")) {
							if (calcact.equals("Multiple With Rate")) {
								log.info("######Inside Buildup Area: Multiple With Rate Condition ");
								amount = (calculationArea * calculationRate);
								if (pCategory.equals(4)) { // MIX
									amount = (res_area * rate_res) + (com_area * rate_comm); // if parking='N'
									if (parkArea > 0) { // means parking=Y
										// for Mix if res_area>com_area then add parking area in res_area else in
										// com_area which one is greater.
										if (res_area > com_area) {
											calculationArea = res_area + parkArea; // Resi greater
											calculationRate = rate_res;
										} else {
											calculationArea = com_area + parkArea; // comm greater
											calculationRate = rate_comm;
										}
										log.info("Area TPRate2: " + calculationArea);
										log.info("Rate TPRate2: " + calculationRate);
										amount = (calculationArea * calculationRate);

									}
								} // end mix
							} else if (calcact.equals("Multiple With Percent")) {
								log.info("######Inside Buildup Area: Plot Area Condition ");
								// calculated on BA and Multiple With Percent
								amount = (calculationArea * (calculationRate / 100));
								if (pCategory.equals(4)) { // MIX
									amount = (res_area * (rate_res / 100)) + (com_area * (rate_comm / 100)); // if
																											// parking='N'
									if (parkArea > 0) { // means parking=Y
										// for Mix if res_area>com_area then add parking area in res_area else in
										// com_area which one is greater.
										if (res_area > com_area) {
											calculationArea = res_area + parkArea; // Resi greater
											calculationRate = rate_res;
										} else {
											calculationArea = com_area + parkArea; // comm greater
											calculationRate = rate_comm;
										}
										log.info("Area TPRate3: " + calculationArea);
										log.info("Rate TPRate3: " + calculationRate);
										amount = (calculationArea * (calculationRate / 100));
									}
								} // end mix
							} else if (calcact.equals("Multiple With Rate & Percent")) {
								log.info("######Inside Buildup Area: Multiple With Rate & Percent Condition ");
								// calculated on BA and Multiple With Rate & Percent
								amount = (calculationArea * calculationRate * (perval / 100));
								if (pCategory.equals(4)) { // MIX
									amount = (res_area * rate_res * (perval / 100))
											+ (com_area * rate_comm * (perval / 100)); // if parking='N'
									if (parkArea > 0) { // means parking=Y
										// for Mix if res_area>com_area then add parking area in res_area else in
										// com_area which one is greater.
										if (res_area > com_area) {
											calculationArea = res_area + parkArea; // Resi greater
											calculationRate = rate_res;
										} else {
											calculationArea = com_area + parkArea; // comm greater
											calculationRate = rate_comm;
										}
										log.info("Area TPRate4: " + calculationArea);
										log.info("Rate TPRate4: " + calculationRate);
										amount = (calculationArea * calculationRate * (perval / 100));
									}
								} // end mix
							} else if (calcact.equals("Fix")) {
								log.info("######Inside Buildup Area: Fix Condition ");
								// calculated on BA and Fix Pre-'Scrutiny Charges NH-REVISED'

								amount = calculationRate;
								if (pCategory.equals(4)) { // MIX
									amount = (rate_res + rate_comm); // if parking='N'
									if (parkArea > 0) { // means parking=Y
										// for Mix if res_area>com_area then add parking area in res_area else in
										// com_area which one is greater.
										if (res_area > com_area) {
											calculationArea = res_area + parkArea; // Resi greater
											calculationRate = rate_res;
										} else {
											calculationArea = com_area + parkArea; // comm greater
											calculationRate = rate_comm;
										}
										log.info("Area TPRate5: " + calculationArea);
										log.info("Rate TPRate5: " + calculationRate);
										amount = calculationRate;
									}
								} // end mix
							}
//							log.info("++++Amount =" + Val);
							log.info("Charges Type : " + chargesTy + ", Amount: " + amount);
						} else if (calcon.equals("Plot Area")) {

							// calculated on PA
							// Note: There is a Issues in calculation for MIX category and, No I/P from
							// client that how calculation will be done.
							calculationArea = plotArea; // change above pcat area but rate will be same
							// $lRate=$lres_rate+$lcom_rate+$lind_rate;
							log.info("Area TPRate6: " + calculationArea);
							log.info("Rate TPRate6: " + calculationRate);
							if (calcact.equals("Multiple With Rate")) {
								log.info("######Inside Plot Area: Multiple With Rate Condition ");
								// calculated on PA and Multiple With Rate
								amount = (calculationArea * calculationRate);
							} else if (calcact.equals("Multiple With Percent")) {
								log.info("######Inside Plot Area: Multiple With Percent Condition ");
								// calculated on PA and Multiple With Percent
								amount = (calculationArea * (calculationRate / 100));
							} else if (calcact.equals("Multiple With Rate & Percent")) {
								log.info("######Inside Plot Area: Multiple With Rate & Percent Condition ");
								// calculated on PA and Multiple With Rate & Percent
								amount = (calculationArea * calculationRate * (perval / 100));
							} else if (calcact.equals("Fix")) {
								log.info("######Inside Plot Area: Fix Condition ");
								// calculated on PA and Fix
								amount = calculationRate;
							}
							log.info("Charges Type : " + chargesTy + ", Amount: " + amount);
						} else if (calcon.equals("Buildup Area and Plot Area")) {
							log.info("######Inside Buildup Area and Plot Area");
							// calculated on BA&PA.
							// Note: Yet No Such Fees based for non-slab(payrate master), but it is in
							// slab-master for BPMS. //So here no calculation.
							if (calcact.equals("Multiple With Rate")) {
								// calculated on BA&PA and Multiple With Rate
							} else if (calcon.equals("Buildup Area and Plot Area")
									&& calcact.equals("Multiple With Percent")) {
								// calculated on BA&PA and Multiple With Percent
							} else if (calcon.equals("Buildup Area and Plot Area")
									&& calcact.equals("Multiple With Rate & Percent")) {
								// calculated on BA&PA and Multiple With Rate & Percent
							} else if (calcon.equals("Buildup Area and Plot Area") && calcact.equals("Fix")) {
								// calculated on BA&PA and Fix
							}
							log.info("Charges Type : " + chargesTy + ", Amount: " + amount);
						} else if (calcon.equals("Plot Area and No of Unit") && calcact.equals("Multiple With Rate")) {
							log.info("######Inside Plot Area and No of Unit");
							// calculated on PA & No.of Unit and Multiple With Rate
						} else if (calcon.equals("Plot Area and No of Unit")
								&& calcact.equals("Multiple With Percent")) {
							log.info("######Inside Plot Area and No of Unit && Multiple With Percent");
							// calculated on PA & No.of Unit.
							// Note: Yet No Such Fees based for non-slab(payrate master), but it is in
							// slab-master for BPMS
							// So here no calculation.
							// calculated on PA & No.of Unit and Multiple With Percent
						} else if (calcon.equals("Plot Area and No of Unit")
								&& calcact.equals("Multiple With Rate & Percent")) {
							log.info("######Inside Plot Area and No of Unit && Multiple With Rate & Percent");
							// calculated on PA & No.of Unit and Multiple With Rate & Percent
						} else if (calcon.equals("Plot Area and No of Unit") && calcact.equals("Fix")) {
							log.info("######Inside Plot Area and No of Unit && Fix");
							// calculated on PA & No.of Unit and Fix
						}

						// calculated on Net Plot Area
						// Note: Yet No Such Fees based for non-slab(payrate master), but it is in
						// slab-master for BPMS
						// So here no calculation.
						else if (calcon.equals("Net Plot Area") && calcact.equals("Multiple With Rate")) {
							// calculated on Net Plot Area and Multiple With Rate
						} else if (calcon.equals("Net Plot Area") && calcact.equals("Multiple With Percent")) {
							// calculated on Net Plot Area and Multiple With Percent
						} else if (calcon.equals("Net Plot Area") && calcact.equals("Multiple With Rate & Percent")) {
							// calculated on Net Plot Area and Multiple With Rate & Percent
						} else if (calcon.equals("Net Plot Area") && calcact.equals("Fix")) {
							// calculated on Net Plot Area and Fix
						}

					} else {
						// FOR SLAB 'S'
						// check for slab.
//						Math.ceil(Area);
						log.info("Inside SlabWise Condition***************");
						log.info("b_category==" + b_category + "--s_category--" + s_category + "-- Math.ceil(Area)--"
								+ Math.ceil(calculationArea));

						List<Map<String, Object>> slabResult = bpaRepository.getDetailOfSlabMaster(b_category,
								s_category, tenantid, paytyid, pCategory, Math.ceil(calculationArea));

						if (slabResult.isEmpty()) {
							throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR,
									"No slab entry found for this category and area range");

						}

						log.info("slabResult------" + slabResult.toString());

						String s_oper = "";
						Double sFromVal = 0.0;
						Double sToVal = 0d;
						Double sres_rate = 0.0;
						Double scom_rate = 0.0;
						Double sind_rate = 0.0;
						Double multpval = 0.0;
						Double maxlimit = 0.0;

						for (Map<String, Object> sResulte : slabResult) {
//							Integer sFromVal=0;
//							Integer sToVal=0;

							log.info("sResulte-----" + sResulte.toString());
							log.info("to val-------" + sResulte.get("to_val"));
//							if(Double.valueOf(sResulte.get("to_val").toString())!=0 && !sResulte.get("to_val").equals(null)) {
							if (Double.valueOf(sResulte.get("to_val").toString()) != 0) {
								log.info("not equal to 0");
								if (Math.ceil(calculationArea) <= Double.valueOf(sResulte.get("to_val").toString())) {
									log.info("<=Math ceil(Area)");
									// slab found
									// get value
									s_oper = sResulte.get("operation").toString();
									sFromVal = Double.valueOf(sResulte.get("to_val").toString());
									sToVal = Double.valueOf(sResulte.get("from_val").toString());
									sres_rate = Double.valueOf(sResulte.get("rate_res").toString());
									scom_rate = Double.valueOf(sResulte.get("rate_comm").toString());
									sind_rate = Double.valueOf(sResulte.get("rate_ind").toString());
									multpval = Double.valueOf(sResulte.get("multp_val").toString()); // 250113 multiply
																										// value
									maxlimit = Double.valueOf(sResulte.get("max_limit").toString()); // 150413 Max Limit
									chkflg = true;
									String sflg = "Found";
									break;
								} else {
									String sflg = "";
								}
							} else {
								if (!sResulte.get("from_val").equals(0)) { // but TOVAL=0/null
									log.info("elas of not equal to 0");
									s_oper = sResulte.get("operation").toString();
									sFromVal = Double.valueOf(sResulte.get("to_val").toString());
									sToVal = Double.valueOf(sResulte.get("from_val").toString());
									sres_rate = Double.valueOf(sResulte.get("rate_res").toString());
									scom_rate = Double.valueOf(sResulte.get("rate_comm").toString());
									sind_rate = Double.valueOf(sResulte.get("rate_ind").toString());
									multpval = Double.valueOf(sResulte.get("multp_val").toString()); // 250113 multiply
																										// value
									maxlimit = Double.valueOf(sResulte.get("max_limit").toString()); // 150413 Max Limit
									chkflg = true; // tovalue=0 >2500
									String sflg = "Found>";
									break;
								}
							}
							log.info("s_oper==" + s_oper + "sFromVal==" + sFromVal + "sToVal==" + sToVal + "sres_rate=="
									+ sres_rate + "scom_rate==" + scom_rate + "sind_rate==" + sind_rate + "multpval=="
									+ multpval + "maxlimit==" + maxlimit);
						} // end of each slab
						log.info("s_oper==" + s_oper + "sFromVal==" + sFromVal + "sToVal==" + sToVal + "sres_rate=="
								+ sres_rate + "scom_rate==" + scom_rate + "sind_rate==" + sind_rate + "multpval=="
								+ multpval + "maxlimit==" + maxlimit);

//						START FROM HERE				
						// SET Default Area & Rate Category wise For Rate & Slab Master
						if (pCategory.equals(1)) { // RESIDENTIAL
							calculationArea = res_area + parkArea;
							calculationRate = sres_rate;
							// showerr($lArea.','.$lsres_rate);
						}
						if (pCategory.equals(2) || pCategory.equals(5)) { // COMMERCIAL/EDUCATIONAL
							calculationArea = com_area + parkArea;
							calculationRate = scom_rate;
						}
						if (pCategory.equals(3)) { // INDUSTRIAL
							calculationArea = ind_area + parkArea;
							calculationRate = sind_rate;
						}
						if (pCategory.equals(4)) { // MIX
							if (calcon.equals("Buildup Area")) {
								calculationArea = res_area + com_area;
								calculationRate = sres_rate + scom_rate;
							} else {
								calculationArea = res_area + com_area;
								calculationRate = sres_rate + scom_rate;
							}
						}
						log.info("Area1: " + calculationArea);
						log.info("Rate1: " + calculationRate);

						// slab calculated on BA -ToDO
						// echo ceil($lArea).','.$lRate.'<br>';
						if (calcon.equals("Buildup Area")) {
							if (s_oper.equals("Multiply")) {
								log.info("######Inside Buildup Area: Multiple With Rate Condition ");
								// calculated on BA and Multiply
								amount = (calculationArea * calculationRate);
								if (pCategory.equals(4)) { // MIX
									amount = (res_area * sres_rate) + (com_area * scom_rate); // if parking='N'
									if (parkArea > 0) { // means parking=Y
										// for Mix if res_area>com_area then add parking area in res_area else in
										// com_area which one is greater.
										if (res_area > com_area) {
											calculationArea = res_area + parkArea; // Resi greater
											calculationRate = sres_rate;
										} else {
											calculationArea = com_area + parkArea; // comm greater
											calculationRate = scom_rate;
										}
										log.info("Area2: " + calculationArea);
										log.info("Rate2: " + calculationRate);
										amount = (calculationArea * calculationRate);
									}
								} // end mix
							} else if (s_oper.equals("Fix")) {
								log.info("######Inside Buildup Area: Fix Condition ");
								// calculated on BA and Fix
								amount = calculationRate;

								if (pCategory.equals(4)) { // MIX
									amount = (sres_rate + scom_rate); // if parking='N'
									if (parkArea > 0) { // means parking=Y
										// for Mix if res_area>com_area then add parking area in res_area else in
										// com_area which one is greater.
										if (res_area > com_area) {
											calculationArea = res_area + parkArea; // Resi greater
											calculationRate = sres_rate;
										} else {
											calculationArea = com_area + parkArea; // comm greater
											calculationRate = scom_rate;
										}
										log.info("Area3: " + calculationArea);
										log.info("Rate3: " + calculationRate);
										amount = calculationRate;
									}
								} // end mix
							} else if (s_oper.equals("Fix and Multiply")) { // TODO
								log.info("######Inside Buildup Area: Fix and Multiply Condition ");
								// calculated on BA and Fix & Multiply -- For all category
								Double LowerRate = 0.0;
								amount = calculationRate + (((double) calculationArea - ((double) sFromVal - 1)) * (double) multpval);
								if (pCategory.equals(4) || pCategory.equals(5)) { // Mix
									calculationArea = res_area + com_area; // default if parking='N' && $l_feetype!='P' ie.Not
																// Scrutiny
									calculationRate = sres_rate + scom_rate;
									amount = (double) calculationRate
											+ (((double) calculationArea - ((double) sFromVal - 1)) * (double) multpval);

									if (parkArea > 0 && feety.equalsIgnoreCase("Pre")) { // means parking=Y & Scrutiny
										// for Mix if res_area>com_area then add parking area in res_area else in
										// com_area which one is greater.
										if ((double) res_area > (double) com_area) {
											calculationArea = (double) res_area + parkArea; // Resi greater
											calculationRate = sres_rate;
											LowerRate = scom_rate;

										} else {
											calculationArea = (double) com_area + parkArea; // comm greater
											calculationRate = scom_rate;
											LowerRate = sres_rate;
										}
										log.info("Area4: " + calculationArea);
										log.info("Rate4: " + calculationRate);
										amount = (double) calculationRate
												+ (((double) calculationArea - ((double) sFromVal - 1)) * (double) multpval)
												+ LowerRate;
									}
									if (pCategory.equals(5)) { // Educational
										log.info("Area4: " + calculationArea);
										log.info("Rate4: " + calculationRate);
										amount = ((double) amount * (50 / 100));
									}
								}
							} else if (s_oper.equals("Multiply & Check Limit")
									|| s_oper.equals("Multiply and Check Limit")) {// -----------have to add C value
								log.info("######Inside Buildup Area: Multiply & Check Limit Condition ");
								// calculated on BA and Multiply & Check Limit
								calcval = (calculationArea * calculationRate);
								if (calcval >= maxlimit)
									amount = maxlimit;
								if (calcval <= maxlimit)
									amount = calcval;
							}
							log.info("Charges Type : " + chargesTy + ", Amount: " + amount);
						} else if (calcon.equals("Plot Area")) {
							// slab calculated on PA
							calculationArea = plotArea; // change above $lpcat area but rate will be same as slab rate
							if (s_oper.equals("Multiply")) {
								log.info("######Inside Plot Area: Multiple With Rate Condition ");
								// calculated on PA and Multiply
								amount = (calculationArea * calculationRate);
							} else if (s_oper.equals("Fix")) {
								log.info("######Inside Plot Area: Fix Condition ");
								// calculated on PA and Fix
								amount = calculationRate;
							} else if (s_oper.equals("Fix and Multiply")) { // Yet No such type of Fees define so write
																			// default
																			// formula
								// calculated on PA and Fix & Multiply -
								log.info("######Inside Plot Area: Fix and Multiply Condition ");
								amount = (double) calculationRate + (((double) calculationArea - ((double) sFromVal - 1)) * (double) multpval);
							} else if (s_oper.equals("Multiply & Check Limit")
									|| s_oper.equals("Multiply and Check Limit")) {
								// calculated on PA and Multiply & Check Limit,residential-single-unit-Rain
								// Harvesting Charges.
								log.info("######Inside Plot Area: Multiply & Check Limit Condition ");
								calcval = (calculationArea * calculationRate);
								if (calcval >= maxlimit)
									amount = maxlimit;
								if (calcval <= maxlimit)
									amount = calcval;
							}
							log.info("Charges Type : " + chargesTy + ", Amount: " + Val);
						} else if (calcon.equals("Buildup Area and Plot Area")) {
							// slab calculated on BA&PA
							Double PlotArea = plotArea; // $lArea - already have build up area. (res,com,ind,mix,edu )

							if (s_oper.equals("Multiply")) {
								log.info("######Inside Buildup Area and Plot Area: Multiple With Rate Condition ");
								// calculated on BA&PA and Multiply
								amount = (calculationArea * calculationRate);
							} else if (s_oper.equals("Fix")) {
								log.info("######Inside Buildup Area and Plot Area: Fix Condition ");
								// calculated on BA&PA and Fix
								amount = calculationRate;
							} else if (s_oper.equals("Fix and Multiply")) {
								// calculated on BA&PA and Fix & Multiply
							} else if (s_oper.equals("Multiply & Check Limit")
									|| s_oper.equals("Multiply and Check Limit")) { // RAI-rain harvesting MUltiply and
																					// Check
								// Limit
								// calculated on BA&PA and Multiply & Check Limit
								// $lackshect=1Lacs/Hector=1000000/10000=10) - define on top-1Lacs value will be
								// fetch from either location/paytype master
								log.info("######Inside Buildup Area and Plot Area: Multiply & Check Limit Condition ");
								calcval = (calculationArea * calculationRate) + (PlotArea * lackshect);
								if (calcval >= maxlimit)
									amount = maxlimit;
								if (calcval <= maxlimit)
									amount = calcval;
							}
							log.info("Charges Type : " + chargesTy + ", Amount: " + amount);
						} else if (calcon.equals("Plot Area and No of Unit")) {
							// slab calculated on PA & No.of Unit
							Double PlotArea = plotArea; // $lArea - already have build up area. (res,com,ind,mix,edu )
							calculationArea = res_unit;
							if (s_oper.equals("Multiply")) {
								// calculated on PA & No.of Unit and Multiply

							} else if (s_oper.equals("Fix")) {
								// calculated on PA & No.of Unit and Fix
							} else if (s_oper.equals("Fix and Multiply")) {
								// calculated on PA & No.of Unit and Fix & Multiply
							} else if (s_oper.equals("Multiply & Check Limit")
									|| s_oper.equals("Multiply and Check Limit")) { // RAI-rain harvesting MUltiply and
																					// Check
								// Limit
								// calculated on PA & No.of Unit and Multiply & Check Limit
								log.info("######Inside Plot Area and No of Unit: Multiply & Check Limit Condition ");
								calcval = (PlotArea * calculationRate);
								if (calcval >= (res_unit * maxlimit))
									amount = (res_unit * maxlimit);
								if (calcval <= (res_unit * maxlimit))
									amount = calcval;
							}
							log.info("Charges Type : " + chargesTy + ", Amount: " + amount);
						} else if (calcon.equals("Net Plot Area")) {
							// slab calculated on Net Plot Area fee:development of colony
							// Calcon='NetPlotArea' -(NPA) = PA-(Road Area+ActualSubPA+OpenSpaceArea)
							// eg if PA=100,RoadArea=50,ActualSubPA=10,OpenSpaceArea=0 then
							// 100-(50+10+0)=(100-60)=40 - this calculated value will be in XML-TAG
							// Rate= 1Lks/Hector ie (100000/10000(in sqmt))=10 - This caluclated value as
							// Rate
							calculationArea = netplot_area; // Net Plot area New from XML

							if (s_oper.equals("Multiply")) {
								log.info("######Inside Net Plot Area: Multiple With Rate Condition ");
								// calculated on Net Plot Area and Multiply
								amount = (calculationArea * calculationRate);
							} else if (s_oper.equals("Fix")) {
								log.info("######Inside Net Plot Area: Fix Condition ");
								// calculated on Net Plot Area and Fix
								amount = calculationRate;
							} else if (s_oper.equals("Fix and Multiply")) {
								// calculated on Net Plot Area and Fix & Multiply
							} else if (s_oper.equals("Multiply & Check Limit")
									|| s_oper.equals("Multiply and Check Limit")) {
								log.info("######Inside Net Plot Area: Multiply & Check Limit Condition ");
								// calculated on Net Plot Area and Multiply & Check Limit
								// 10=1Lacs/Hector=1000000/10000=10) - 1Lacs value will be fetch from either
								// location/paytype master
								calcval = (calculationArea * calculationRate);
								if (calcval >= maxlimit)
									amount = maxlimit;
								if (calcval <= maxlimit)
									amount = calcval;
							}
							log.info("Charges Type : " + chargesTy + ", Amount: " + amount);
						}

					} // end of SLAB

					totalAmount += (double) amount; // each building total calulation/charges for the fee
					trate += (double) calculationRate; // each building total rate for the fee
					ptarea += (double) calculationArea; // each building total area for the fee

//					log.info("Charges Type : " + chargesTy + ", Amount: " + Val);
					log.info("End--Value--" + totalAmount + "-----trate---" + trate + "----End--ptarea--" + ptarea
							+ "---unitid--" + unitid + "--End----paytyid--" + paytyid + "----chargesTy--" + chargesTy
							+ "----calcact--" + calcact + "----tenantid--" + tenantid + "----feety--" + feety
							+ "----applicationNo--" + applicationNo + "---End");

					payTypeFeeDetailRequest.setApplicationNo(applicationNo);
					payTypeFeeDetailRequest.setFeeType(feety);
					payTypeFeeDetailRequest.setTenantId(tenantid);
					payTypeFeeDetailRequest.setType(calcact);
					payTypeFeeDetailRequest.setChargesTypeName(chargesTy);
					payTypeFeeDetailRequest.setPayTypeId(paytyid);
					payTypeFeeDetailRequest.setUnit(unitid);
					payTypeFeeDetailRequest.setPropPlotArea(calculationArea);
					payTypeFeeDetailRequest.setRate(trate);
					payTypeFeeDetailRequest.setAmount(amount);
					payTypeFeeDetailRequest.setBillId("");
					payTypeFeeDetailRequest.setSrNo(count);

					feeMap.put("ApplicationNo", applicationNo);
					feeMap.put("FeeType", feety);
					feeMap.put("Tenantid", tenantid);
					feeMap.put("Operation", calcact);
					feeMap.put("ChargesType", chargesTy);
					feeMap.put("PayTypeId", paytyid);
					feeMap.put("UnitId", unitid);
					feeMap.put("PropValue", calculationArea);
					feeMap.put("Rate", trate);
					feeMap.put("Amount", amount);
					feeMap.put("bill_id", "");
					feeMap.put("createdby", "");
					feeMap.put("updatedby", "");
					feeMap.put("updateddate", "");
					// }//end of for each building loop

				} // end of tpd_zdaflg='N'
				else if (item.get("zdaflg").equals("Y")) {
					log.info("Inside zdaflg is Y");
					// have to verify this logic of calculation ----this calculation is not require
					// for CG told by ved sir-------------------------
					if (!zonedesc.equals(null) && plotArea > 0) {
						calculationArea = plotArea;
						if (zonedesc.equals("DA-01"))
							calculationRate = 7.29;
						if (zonedesc.equals("CA"))
							calculationRate = 9.72;
						if (zonedesc.equals("DA-02"))
							calculationRate = 12.25;
						if (zonedesc.equals("DA-03"))
							calculationRate = 12.25;
						amount = (double) calculationArea * (double) calculationRate;

						totalAmount = (double) amount; // each building total calulation/charges for the fee
						trate = (double) calculationRate; // each building total rate for the fee
						ptarea = (double) calculationArea; // each building total area for the fee
						calcact = "Multiple With Rate";
					}
				}

			} // end of if $lbrkflg
				// insert data in data base-------------
//			feeDetailMap.add(feeMap);
			if (payTypeFeeDetailRequest.getAmount() > 0) {
				feeDetailList.add(payTypeFeeDetailRequest);
			}
			log.info("feeDetailMap-List-------" + feeDetailList.toString());
//			log.info("feeDetailMap-List-------"+feeDetailMap);
//			bpaRepository.createFeeDetail(feeDetailMap);
//			bpaRepository.createFeeDetail(feeDetailList);
			count += 1;
		} // End of for each fee type
		bpaRepository.createFeeDetail(feeDetailList);

		log.info("feeDetailMap-List-------" + feeDetailMap);
//		Map<String, String> list = new HashMap<String, String>();
//		list.put("Value",Value.toString());
//		list.put("trate",trate.toString());
//		list.put("calcact",calcact.toString());

		return totalAmount;
	}

}
