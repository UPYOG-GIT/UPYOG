package org.entit.rga.calculator.web.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.entit.rga.calculator.services.CalculationService;
import org.entit.rga.calculator.services.DemandService;
import org.entit.rga.calculator.web.models.Calculation;
import org.entit.rga.calculator.web.models.CalculationReq;
import org.entit.rga.calculator.web.models.CalculationRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
//@RequestMapping("/v1")
@Slf4j
public class RGACalculatorController {

	private ObjectMapper objectMapper;

	private HttpServletRequest request;

	private CalculationService calculationService;

	private DemandService demandService;

	@Autowired
	public RGACalculatorController(ObjectMapper objectMapper, HttpServletRequest request,
			CalculationService calculationService, DemandService demandService) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.calculationService = calculationService;
		this.demandService = demandService;
	}

	/**
	 * Calulates the tradeLicense fee and creates Demand
	 * 
	 * @param calculationReq The calculation Request
	 * @return Calculation Response
	 */
	@RequestMapping(value = "/rga/_calculate", method = RequestMethod.POST)
	public ResponseEntity<CalculationRes> calculate(@Valid @RequestBody CalculationReq calculationReq) {
		log.debug("CalculationReaquest:: " + calculationReq);
//		log.info("calculationReq : " + calculationReq);
		log.info("calculationReq.getCalulationCriteria().get(0).getFeeType() : "
				+ calculationReq.getCalulationCriteria().get(0).getFeeType());
		List<Calculation> calculations = calculationService.calculate(calculationReq);
		log.info("Calculations Amount : " + calculations.get(0).getTaxHeadEstimates().get(0).getEstimateAmount());
		CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
		return new ResponseEntity<CalculationRes>(calculationRes, HttpStatus.OK);
	}

}
