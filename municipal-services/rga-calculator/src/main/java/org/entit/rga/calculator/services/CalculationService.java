package org.entit.rga.calculator.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.entit.rga.calculator.config.RGACalculatorConfig;
import org.entit.rga.calculator.kafka.broker.RGACalculatorProducer;
import org.entit.rga.calculator.utils.RGACalculatorConstants;
import org.entit.rga.calculator.utils.CalculationUtils;
import org.entit.rga.calculator.web.models.BillingSlabSearchCriteria;
import org.entit.rga.calculator.web.models.Calculation;
import org.entit.rga.calculator.web.models.CalculationReq;
import org.entit.rga.calculator.web.models.CalculationRes;
import org.entit.rga.calculator.web.models.CalulationCriteria;
import org.entit.rga.calculator.web.models.demand.Category;
import org.entit.rga.calculator.web.models.demand.TaxHeadEstimate;
import org.entit.rga.calculator.web.models.rga.RGA;
import org.entit.rga.calculator.web.models.rga.EstimatesAndSlabs;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class CalculationService {

	@Autowired
	private FeeCalculationService feeCalculationService;

	@Autowired
	private DemandService demandService;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private RGACalculatorConfig config;

	@Autowired
	private CalculationUtils utils;

	@Autowired
	private RGACalculatorProducer producer;

	@Autowired
	private RGAService rgaService;

	/**
	 * Calculates tax estimates and creates demand
	 * 
	 * @param calculationReq The calculationCriteria request
	 * @return List of calculations for all applicationNumbers or tradeLicenses in
	 *         calculationReq
	 */
	public List<Calculation> calculate(CalculationReq calculationReq) {
		String tenantId = calculationReq.getCalulationCriteria().get(0).getTenantId();
		Object mdmsData = feeCalculationService.mDMSCall(calculationReq, tenantId);
		List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),
				calculationReq.getCalulationCriteria(), mdmsData);
		demandService.generateDemand(calculationReq.getRequestInfo(), calculations, mdmsData);
		CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
		producer.push(config.getSaveTopic(), calculationRes);
		return calculations;
	}

	/***
	 * Calculates tax estimates
	 * 
	 * @param requestInfo The requestInfo of the calculation request
	 * @param criterias   list of CalculationCriteria containing the tradeLicense or
	 *                    applicationNumber
	 * @return List of calculations for all applicationNumbers or tradeLicenses in
	 *         criterias
	 */
	public List<Calculation> getCalculation(RequestInfo requestInfo, List<CalulationCriteria> criterias,
			Object mdmsData) {
		log.info("CalculationService.getCalculation()");
		List<Calculation> calculations = new LinkedList<>();
		for (CalulationCriteria criteria : criterias) {
			RGA rga;
			if (criteria.getRga() == null && criteria.getApplicationNo() != null) {
				rga = rgaService.getBuildingPlan(requestInfo, criteria.getTenantId(), criteria.getApplicationNo(),
						null);
				criteria.setRga(rga);
			}

			EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria, requestInfo, mdmsData);
			List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs.getEstimates();

			log.info("taxHeadEstimates: " + taxHeadEstimates.get(0).getEstimateAmount().toString());
			Calculation calculation = new Calculation();
			calculation.setRga(criteria.getRga());
			calculation.setTenantId(criteria.getTenantId());
			calculation.setTaxHeadEstimates(taxHeadEstimates);
			calculation.setFeeType(criteria.getFeeType());
			calculations.add(calculation);

		}
		return calculations;
	}

	/**
	 * Creates TacHeadEstimates
	 * 
	 * @param calulationCriteria CalculationCriteria containing the tradeLicense or
	 *                           applicationNumber
	 * @param requestInfo        The requestInfo of the calculation request
	 * @return TaxHeadEstimates and the billingSlabs used to calculate it
	 */
	private EstimatesAndSlabs getTaxHeadEstimates(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		log.info("inside CalculationService.getTaxHeadEstimates()");
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		EstimatesAndSlabs estimatesAndSlabs;
//		if (calulationCriteria.getFeeType().equalsIgnoreCase(RGACalculatorConstants.MDMS_CALCULATIONTYPE_LOW_APL_FEETYPE)) {
//
////			 stopping Application fee for lowrisk applicaiton according to BBI-391
//			calulationCriteria.setFeeType(RGACalculatorConstants.MDMS_CALCULATIONTYPE_APL_FEETYPE);
////			calulationCriteria.setFeeType(RGACalculatorConstants.MDMS_CALCULATIONTYPE_LOW_APL_FEETYPE);
//			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);
//
//			estimates.addAll(estimatesAndSlabs.getEstimates());
//
////			calulationCriteria.setFeeType(RGACalculatorConstants.MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE);
////			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);
////
////			estimates.addAll(estimatesAndSlabs.getEstimates());
//
////			calulationCriteria.setFeeType(RGACalculatorConstants.LOW_RISK_PERMIT_FEE_TYPE);
//
//		} else {
		estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);

		estimates.addAll(estimatesAndSlabs.getEstimates());
//		}

		estimatesAndSlabs.setEstimates(estimates);
		log.info("getEstimateAmount(): " + estimatesAndSlabs.getEstimates().get(0).getEstimateAmount().toString());

		return estimatesAndSlabs;
	}

	/**
	 * Calculates base tax and cretaes its taxHeadEstimate
	 * 
	 * @param calulationCriteria CalculationCriteria containing the tradeLicense or
	 *                           applicationNumber
	 * @param requestInfo        The requestInfo of the calculation request
	 * @return BaseTax taxHeadEstimate and billingSlabs used to calculate it
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private EstimatesAndSlabs getBaseTax(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		RGA rga = calulationCriteria.getRga();
		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		BillingSlabSearchCriteria searchCriteria = new BillingSlabSearchCriteria();
		searchCriteria.setTenantId(rga.getTenantId());

		Map calculationTypeMap = feeCalculationService.getCalculationType(requestInfo, rga, mdmsData,
				calulationCriteria.getFeeType());
//		int calculatedAmout = 0;
		Double calculatedAmout = 0d;
		ArrayList<TaxHeadEstimate> estimates = new ArrayList<TaxHeadEstimate>();
		if (calculationTypeMap.containsKey("calsiLogic")) {
			log.info("inside if condition");
			LinkedHashMap ocEdcr = edcrService.getEDCRDetails(requestInfo, rga);
			String jsonString = new JSONObject(ocEdcr).toString();
			DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
			JSONArray permitNumber = context.read("edcrDetail.*.permitNumber");
			String jsonData = new JSONObject(calculationTypeMap).toString();
			DocumentContext calcContext = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonData);
			JSONArray parameterPaths = calcContext.read("calsiLogic.*.paramPath");
			JSONArray tLimit = calcContext.read("calsiLogic.*.tolerancelimit");
			log.info("tolerance limit in: " + tLimit.get(0));
			DocumentContext edcrContext = null;
			if (!CollectionUtils.isEmpty(permitNumber)) {
				RGA permitRga = rgaService.getBuildingPlan(requestInfo, rga.getTenantId(), null,
						permitNumber.get(0).toString());
				if (permitRga.getEdcrNumber() != null) {
					LinkedHashMap edcr = edcrService.getEDCRDetails(requestInfo, permitRga);
					String edcrData = new JSONObject(edcr).toString();
					edcrContext = JsonPath.using(Configuration.defaultConfiguration()).parse(edcrData);
				}
			}

			for (int i = 0; i < parameterPaths.size(); i++) {
				Double ocTotalBuitUpArea = context.read(parameterPaths.get(i).toString());
				Double rgaTotalBuitUpArea = edcrContext.read(parameterPaths.get(i).toString());
				Double diffInBuildArea = ocTotalBuitUpArea - rgaTotalBuitUpArea;
				log.info("difference in area: " + diffInBuildArea);
				Double limit = Double.valueOf(tLimit.get(i).toString());
				if (diffInBuildArea > limit) {
					JSONArray data = calcContext.read("calsiLogic.*.deviation");
					log.info(data.get(0).toString());
					JSONArray data1 = (JSONArray) data.get(0);
					for (int j = 0; j < data1.size(); j++) {
						LinkedHashMap diff = (LinkedHashMap) data1.get(j);
						Integer from = (Integer) diff.get("from");
						Integer to = (Integer) diff.get("to");
						Integer uom = (Integer) diff.get("uom");
						Integer mf = (Integer) diff.get("MF");
						if (diffInBuildArea >= from && diffInBuildArea <= to) {
							calculatedAmout = (Double) (diffInBuildArea * mf * uom);
							break;
						}
					}
				} else {
					calculatedAmout = 0d;
				}
				TaxHeadEstimate estimate = new TaxHeadEstimate();
				BigDecimal totalTax = BigDecimal.valueOf(calculatedAmout);
				if (totalTax.compareTo(BigDecimal.ZERO) == -1)
					throw new CustomException(RGACalculatorConstants.INVALID_AMOUNT, "Tax amount is negative");

				estimate.setEstimateAmount(totalTax);
				estimate.setCategory(Category.FEE);

				String taxHeadCode = utils.getTaxHeadCode(rga.getBusinessService(), calulationCriteria.getFeeType());
				estimate.setTaxHeadCode(taxHeadCode);
				estimates.add(estimate);
			}
		} else {
			log.info("inside else condition");
			TaxHeadEstimate estimate = new TaxHeadEstimate();

//			calculatedAmout = Integer
//					.parseInt(calculationTypeMap.get(RGACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT).toString());
			calculatedAmout = Double
					.parseDouble(calculationTypeMap.get(RGACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT).toString());
			log.info("calculatedAmout: " + calculatedAmout);
			BigDecimal totalTax = BigDecimal.valueOf(calculatedAmout);
			if (totalTax.compareTo(BigDecimal.ZERO) == -1)
				throw new CustomException(RGACalculatorConstants.INVALID_AMOUNT, "Tax amount is negative");

			estimate.setEstimateAmount(totalTax);
			estimate.setCategory(Category.FEE);

			String taxHeadCode = utils.getTaxHeadCode(rga.getBusinessService(), calulationCriteria.getFeeType());
			estimate.setTaxHeadCode(taxHeadCode);
			estimates.add(estimate);
		}
		estimatesAndSlabs.setEstimates(estimates);
		return estimatesAndSlabs;
	}

}
