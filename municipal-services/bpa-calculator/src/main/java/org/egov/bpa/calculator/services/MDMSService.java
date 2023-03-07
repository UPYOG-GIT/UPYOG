package org.egov.bpa.calculator.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.repository.BPARepository;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.web.models.CalculationReq;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
			if (feeType.equalsIgnoreCase(BPACalculatorConstants.MDMS_CALCULATIONTYPE_SANC_FEETYPE)) {
				log.info("inside sancFee if condition......");
				String consumerCode = bpa.getApplicationNo();
				log.info("consumerCode: " + consumerCode);
				Map sancFeeMap = new HashMap();
				try {
					String[] SancFee = bpaRepository.getSanctionFeeAmount(consumerCode);
					log.info("SancFee from DB: " + SancFee.toString());
					if (SancFee.length != 0) {
						Double totalSancFeeAmount = Double.valueOf(SancFee[SancFee.length - 1]);
						log.info("totalSancFeeAmount: " + totalSancFeeAmount);
						sancFeeMap.put(BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT, totalSancFeeAmount);
						return sancFeeMap;
					} else {
						log.error("Sanction Fee not found in DB");
						throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR, "Sanction Fee not found");
					}
				} catch (Exception ex) {
					log.error("Exception in SancFee condition: " + ex);
					throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR, "Sanction Fee not found");
				}
			}

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

			Double plotArea = context.read("edcrDetail[0].planDetail.planInformation.plotArea");
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
			log.info("appNum:----- " +appNum);
			String tenantid = bpa.getTenantId();
			log.info("tenantid:----- " +tenantid);
			String bCate = context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].type.name");
			log.info("bcate:----- " +bCate);
			String subCate = context.read("edcrDetail[0].planDetail.virtualBuilding.occupancyTypes[0].subtype.name");
			log.info("subcate:----- " +subCate);
//			Map parkDetails = context.read("edcrDetail[0].planDetail.reportOutput.scrutinyDetails[6]");
//			log.info("totalparkarea:----- " +parkDetails.toString());
			JSONArray parkDetails11 = context.read("edcrDetail[0].planDetail.reportOutput.scrutinyDetails[?(@.key==\"Common_Parking\")].detail[0].Provided");
			log.info("parkDetails11====:----- " +parkDetails11.toString());
			String totalParkArea = parkDetails11.get(0).toString();
			
			additionalDetails.put("appDate", appDate.toString());
			additionalDetails.put("appNum", appNum.toString());
			additionalDetails.put("plotares", plotArea.toString());
			additionalDetails.put("feeType", feeType.toString());
			additionalDetails.put("tenantid", tenantid.toString());
			additionalDetails.put("bcate", bCate.toString());
			additionalDetails.put("subcate", subCate.toString());
			additionalDetails.put("totalParkArea", totalParkArea);
			
			
			log.info("additionalDetails---------"+additionalDetails);
			List<Map<String, Object>> responseMap1 = feeCalculation(additionalDetails);
			
			log.info("responseMap1----------"+responseMap1);
//			added end----- auto calculation--------------------------------------------			
			
			
			
			
			if (((plotArea <= 500.00) && (additionalDetails.get("occupancyType").equals("Residential")))) {
//	     		   String filterExp = "$.[?(@.amount==1)]";
				String filterExp = "$.[?(@.feeType=='" + feeType + "')]";
				log.info("filterExp--------" + filterExp);
				List<Object> calTypes = JsonPath.read(jsonOutput, filterExp);
				log.info("calTypes plotArea ----  " + calTypes);
				log.info("calTypes.size(): " + calTypes.size());
				if (calTypes.size() == 0) {
					log.info("================should not enter==========");
					return defaultMap(feeType);
				}
				Object obj = calTypes.get(0);
				calculationType = (HashMap<String, Object>) obj;
			} else {

				JSONArray serviceType = context.read("edcrDetail.*.applicationSubType");
				if (CollectionUtils.isEmpty(serviceType)) {
					serviceType.add("NEW_CONSTRUCTION");
				}
				JSONArray applicationType = context.read("edcrDetail.*.appliactionType");
				if (StringUtils.isEmpty(applicationType)) {
					applicationType.add("permit");
				}
				additionalDetails.put("applicationType", applicationType.get(0).toString());
				additionalDetails.put("serviceType", serviceType.get(0).toString());

				log.info("context totalBuiltupare"
						+ context.read("edcrDetail[0].planDetail.virtualBuilding.totalBuitUpArea"));
				Double totalBuitUpArea = context.read("edcrDetail[0].planDetail.virtualBuilding.totalBuitUpArea");
				log.info("totalBuitUpArea: " + totalBuitUpArea);

				additionalDetails.put("totalBuitUpArea", totalBuitUpArea.toString());

				log.info("JSONArray occupancyType ki value :--------- " + occupancyType.get(0).toString()
						+ "=========JSONArray totalBuitUpArea ki value:----" + totalBuitUpArea.toString());
				log.info("occupancyType form additional=====:  " + additionalDetails.get("occupancyType"));
				log.info("totalBuitUpArea form additional=====:  " + additionalDetails.get("totalBuitUpArea"));
//             log.debug("applicationType is " + additionalDetails.get("applicationType"));
//             log.debug("serviceType is " + additionalDetails.get("serviceType"));
				log.info("applicationType: " + additionalDetails.get("applicationType"));
				String filterExp = "$.[?((@.applicationType == '" + additionalDetails.get("applicationType")
						+ "' || @.applicationType === 'ALL' ) &&  @.feeType == '" + feeType + "')]";
				List<Object> calTypes = JsonPath.read(jsonOutput, filterExp);
				log.info("calTypes10: " + calTypes);

				log.info("serviceType: " + additionalDetails.get("serviceType"));
				filterExp = "$.[?(@.serviceType == '" + additionalDetails.get("serviceType")
						+ "' || @.serviceType === 'ALL' )]";
				calTypes = JsonPath.read(calTypes, filterExp);
				log.info("calTypes11: " + calTypes);
//            
//             filterExp = "$.[?(@.riskType == '"+bpa.getRiskType()+"' || @.riskType === 'ALL' )]";
//             calTypes = JsonPath.read(calTypes, filterExp);

//             ----added by manisha for filter amount-------
				filterExp = "$.[?(@.occupancyType == '" + additionalDetails.get("occupancyType") + "')]";
				log.info("filterExp:------ " + filterExp);
				calTypes = JsonPath.read(calTypes, filterExp);
				log.info("calTypes12: " + calTypes);
//			log.info("calTypes(JsonPath.read(jsonOutput, filterExp) : " + calTypes);

				filterExp = "$.[?(@.builtupAreaFrom <=" + additionalDetails.get("totalBuitUpArea")
						+ "&& @.builtupAreaTo >=" + additionalDetails.get("totalBuitUpArea") + ")]";
				log.info("filterExp:------ " + filterExp);
				calTypes = JsonPath.read(calTypes, filterExp);
				log.info("calTypes14: " + calTypes);

//			log.info("calTypes = JsonPath.read(calTypes, filterExp): " + calTypes);

//             ----added by manisha for filter amount-------

				if (calTypes.size() > 1) {
					filterExp = "$.[?(@.riskType == '" + bpa.getRiskType() + "' )]";
					calTypes = JsonPath.read(calTypes, filterExp);
					log.info("calTypes15: " + calTypes);
				}

				if (calTypes.size() == 0) {
					log.info("================should not enter==========");
					return defaultMap(feeType);
				}

				log.info("calTypes--------------" + calTypes + "==========calTypes.get(0)==========" + calTypes.get(0));
				Object obj = calTypes.get(0);

				int currentYear = Calendar.getInstance().get(Calendar.YEAR);

				String financialYear = currentYear + "-" + (currentYear + 1);
//			System.out.println(financialYear);

				calculationType = (HashMap<String, Object>) obj;
			}
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
	

	private List<Map<String, Object>> feeCalculation(Map data) {	
		log.info("Data  "+data);
		String feetype = data.get("feeType").toString();
		log.info("feetype----"+feetype);
		String tenantid = data.get("tenantid")+"";
		log.info("tenantid----"+tenantid);
		String occupancyType = data.get("occupancyType")+"";
		log.info("occupancyType----"+occupancyType);
		Double plotares = Double.valueOf(data.get("plotares").toString());
		log.info("plotares----"+plotares);
		String applicationNo = data.get("appNum").toString();
		log.info("applicationNo----"+applicationNo);
		String bcategory = data.get("bcate").toString();
		log.info("bcategory----"+bcategory);
		Double totalParkArea = Double.valueOf(data.get("totalParkArea").toString());
		log.info("totalParkArea----"+totalParkArea);
		String subcate = data.get("subcate").toString();
		log.info("subcate----"+subcate);
		String appDate = data.get("appDate").toString();
		log.info("appDate----"+appDate);
		 
		
		
//		Object feetype = data.get("feeType");
		String feety ="";
		String brkflg="";
		String heightcat = "NH";
		String newrevise = "NEW";
		int pCategory = 0;
		
		if(feetype.equals("ApplicationFee")) {
			feety = "Pre";
		}
		else if(feetype.equals("SanctionFee")) {
			feety = "Post";
		} 
		
		if(occupancyType.equals("Residential")) {
			pCategory =1;
		}
		else if(occupancyType.equals("Commercial")) {
			pCategory =2;
		}
		else if(occupancyType.equals("INDUSTRIAL")) {
			pCategory =3;
		}
		else if(occupancyType.equals("MIX")) {
			pCategory =4;
		}
		
		
		if (feety.equals("Pre"))	
		{
			//for hight rise----------
			log.info("-------------inside hight rise-----------");
		}	
		
		
		
		log.info("tenantid--"+tenantid+"---feety---"+feety);
		
		List<Map<String,Object>> result  = bpaRepository.getPaytyDate(tenantid,feety,occupancyType,plotares,heightcat,newrevise);
		
		log.info("result--0-----"+result.toString());
		
		for(Map<String,Object> item : result) {
			if (feety.equals("Pre"))	
			{
				if(heightcat.equals("NH") && newrevise.equals("REVISED")) {
					log.info("------------REVISED----------");
				}
			}
			if(brkflg.equals("")) {
				if(item.get("zdaflg").equals("N")) {
					int id = Integer.parseInt(item.get("id").toString());
					Integer countPayTyrate = bpaRepository.getCountOfPaytyrate(tenantid,id,pCategory);
					log.info("countPayTyrate: "+countPayTyrate);
					
					if(countPayTyrate.equals(0)) {
						throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR, "No pay type rate entry found for  this id");
					}
					
					String[] detailPayTyrate = bpaRepository.getDetailOfPaytyrate(tenantid,id,pCategory);
					log.info("detailPayTyrate : "+detailPayTyrate.toString());
					
					
					log.info("End-------End---------End---------End-------End");
				}
				
			}
		}
		
		

		return result;
	}

}
