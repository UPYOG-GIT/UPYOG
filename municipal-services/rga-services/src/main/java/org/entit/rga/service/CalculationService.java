package org.entit.rga.service;

import java.util.Arrays;
import java.util.List;

import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.web.model.CalculationCriteria;
import org.entit.rga.web.model.CalculationReq;
import org.entit.rga.web.model.RGARequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculationService {

	private ServiceRequestRepository serviceRequestRepository;

	private RGAConfiguration config;

	@Autowired
	public CalculationService(ServiceRequestRepository serviceRequestRepository, RGAConfiguration config) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.config = config;
	}

	/**
	 * add calculation for the bpa object based on the FeeType
	 * @param rGARequest
	 * @param feeType
	 */
	public void addCalculation(RGARequest rGARequest, String feeType) {

		CalculationReq calulcationRequest = new CalculationReq();
		calulcationRequest.setRequestInfo(rGARequest.getRequestInfo());
		CalculationCriteria calculationCriteria = new CalculationCriteria();
		calculationCriteria.setApplicationNo(rGARequest.getRegularisation().getApplicationNo());
		calculationCriteria.setRGA(rGARequest.getRegularisation());
		calculationCriteria.setFeeType(feeType);
		calculationCriteria.setTenantId(rGARequest.getRegularisation().getTenantId());
		List<CalculationCriteria> criterias = Arrays.asList(calculationCriteria);
		calulcationRequest.setCalculationCriteria(criterias);
		StringBuilder url = new StringBuilder();
		url.append(this.config.getCalculatorHost());
		url.append(this.config.getCalulatorEndPoint());

		this.serviceRequestRepository.fetchResult(url, calulcationRequest);
	}

}
