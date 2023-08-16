package org.entit.rga.calculator.services;

import java.util.LinkedHashMap;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.entit.rga.calculator.config.RGACalculatorConfig;
import org.entit.rga.calculator.repository.ServiceRequestRepository;
import org.entit.rga.calculator.utils.RGACalculatorConstants;
import org.entit.rga.calculator.web.models.RequestInfoWrapper;
import org.entit.rga.calculator.web.models.rga.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EDCRService {

	@Autowired
	 private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private RGACalculatorConfig config;
	
	@SuppressWarnings("rawtypes")
	public LinkedHashMap getEDCRDetails(RequestInfo requestInfo, RGA rga) {

		String edcrNo = rga.getEdcrNumber();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());

		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(rga.getTenantId());
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(requestInfo, edcrRequestInfo);
		edcrRequestInfo.setUserInfo(null); // since EDCR service is not
											// accepting userInfo
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException(RGACalculatorConstants.EDCR_ERROR, " EDCR Number is Invalid");
		}

		if (CollectionUtils.isEmpty(responseMap))
			throw new CustomException(RGACalculatorConstants.EDCR_ERROR, "The response from EDCR service is empty or null");

		return responseMap;
	}
	
}
