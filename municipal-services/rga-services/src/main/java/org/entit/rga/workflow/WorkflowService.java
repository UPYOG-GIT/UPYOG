package org.entit.rga.workflow;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.util.RGAErrorConstants;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RequestInfoWrapper;
import org.entit.rga.web.model.workflow.BusinessService;
import org.entit.rga.web.model.workflow.BusinessServiceResponse;
import org.entit.rga.web.model.workflow.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowService {

	private RGAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	@Autowired
	public WorkflowService(RGAConfiguration config, ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	/**
	 * Get the workflow config for the given tenant
	 * 
	 * @param tenantId    The tenantId for which businessService is requested
	 * @param requestInfo The RequestInfo object of the request
	 * @return BusinessService for the the given tenantId
	 */
	public BusinessService getBusinessService(RGA rGA, RequestInfo requestInfo, String applicationNo) {
		StringBuilder url = getSearchURLWithParams(rGA, true, null);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		BusinessServiceResponse response = null;
		try {
			response = mapper.convertValue(result, BusinessServiceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(RGAErrorConstants.PARSING_ERROR, "Failed to parse response of calculate");
		}
		return response.getBusinessServices().get(0);
	}

	/**
	 * Creates url for search based on given tenantId
	 *
	 * @param tenantId The tenantId for which url is generated
	 * @return The search url
	 */
	private StringBuilder getSearchURLWithParams(RGA rGA, boolean businessService, String applicationNo) {
		StringBuilder url = new StringBuilder(config.getWfHost());
		if (businessService) {
			url.append(config.getWfBusinessServiceSearchPath());
		} else {
			url.append(config.getWfProcessPath());
		}
		url.append("?tenantId=");
		url.append(rGA.getTenantId());
		if (businessService) {
			url.append("&businessServices=");
			url.append(rGA.getBusinessService());
		} else {
			url.append("&businessIds=");
			url.append(applicationNo);
		}
		return url;
	}

	/**
	 * Returns boolean value to specifying if the state is updatable
	 * 
	 * @param statusEnum      The stateCode of the bpa
	 * @param businessService The BusinessService of the application flow
	 * @return State object to be fetched
	 */
	public Boolean isStateUpdatable(String status, BusinessService businessService) {
		log.info("status: " + status);
		for (org.entit.rga.web.model.workflow.State state : businessService.getStates()) {
			log.info("state.getApplicationStatus(): " + state.getApplicationStatus());
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state.getIsStateUpdatable();
		}
		return Boolean.FALSE;
	}

	/**
	 * Returns State name fo the current state of the document
	 * 
	 * @param statusEnum      The stateCode of the bpa
	 * @param businessService The BusinessService of the application flow
	 * @return State String to be fetched
	 */
	public String getCurrentState(String status, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state.getState();
		}
		return null;
	}

	/**
	 * Returns State Obj fo the current state of the document
	 * 
	 * @param statusEnum      The stateCode of the bpa
	 * @param businessService The BusinessService of the application flow
	 * @return State object to be fetched
	 */
	public State getCurrentStateObj(String status, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state;
		}
		return null;
	}
}
