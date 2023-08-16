package org.entit.rga.calculator.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.entit.rga.calculator.config.RGACalculatorConfig;
import org.entit.rga.calculator.repository.ServiceRequestRepository;
import org.entit.rga.calculator.utils.RGACalculatorConstants;
import org.entit.rga.calculator.web.models.RequestInfoWrapper;
import org.entit.rga.calculator.web.models.rga.RGA;
import org.entit.rga.calculator.web.models.rga.RGAResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
public class RGAService {

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RGACalculatorConfig config;

	public RGA getBuildingPlan(RequestInfo requestInfo, String tenantId, String applicationNo, String approvalNo) {
		StringBuilder url = getRGASearchURL();
		url.append("tenantId=");
		url.append(tenantId);
		if (approvalNo != null) {
			url.append("&");
			url.append("approvalNo=");
		} else {
			url.append("&");
			url.append("applicationNo=");
		}
		url.append(approvalNo);
		LinkedHashMap responseMap = null;
		responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(url, new RequestInfoWrapper(requestInfo));

		RGAResponse rgaResponse = null;

		try {
			rgaResponse = mapper.convertValue(responseMap, RGAResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(RGACalculatorConstants.PARSING_ERROR, "Error while parsing response of TradeLicense Search");
		}

		return rgaResponse.getRGA().get(0);
	}

	private StringBuilder getRGASearchURL() {
		// TODO Auto-generated method stub
		StringBuilder url = new StringBuilder(config.getRgaHost());
		url.append(config.getRgaContextPath());
		url.append(config.getRgaSearchEndpoint());
		url.append("?");
		return url;
	}
}
