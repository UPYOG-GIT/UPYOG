package org.entit.rga.workflow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.entit.rga.util.RGAConstants;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.workflow.Action;
import org.entit.rga.web.model.workflow.BusinessService;
import org.entit.rga.web.model.workflow.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActionValidator {

	private WorkflowService workflowService;

	@Autowired
	public ActionValidator(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * Validates create request
	 * 
	 * @param request The BPA Create request
	 */
	public void validateCreateRequest(RGARequest request) {
		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Validates the update request
	 * 
	 * @param request The BPA update request
	 */
	public void validateUpdateRequest(RGARequest request, BusinessService businessService) {
		validateRoleAction(request, businessService);
//		validateAction(request);
		validateIds(request, businessService);
	}

	/**
	 * Validates if the role of the logged in user can perform the given action
	 * 
	 * @param request The bpa create or update request
	 */
	private void validateRoleAction(RGARequest request, BusinessService businessService) {
		RGA rGA = request.getRegularisation();
		Map<String, String> errorMap = new HashMap<>();
		RequestInfo requestInfo = request.getRequestInfo();
//		}
		State state = workflowService.getCurrentStateObj(rGA.getStatus(), businessService);
		log.info("current state: " + state);

		if (state != null) {
			List<Action> actions = state.getActions();
			List<Role> roles = requestInfo.getUserInfo().getRoles();
			List<String> validActions = new LinkedList<>();

			log.info("requestInfo.getUserInfo().getRoles().size(): " + requestInfo.getUserInfo().getRoles().size());

			roles.forEach(role -> {
				actions.forEach(action -> {
					log.info("action.getRoles().contains(role.getCode()): "
							+ action.getRoles().contains(role.getCode()));
					if (action.getRoles().contains(role.getCode())) {
						log.info("action.getAction(): " + action.getAction());
						validActions.add(action.getAction());
					}
				});
			});

			if (!validActions.contains(rGA.getWorkflow().getAction())) {
				errorMap.put("UNAUTHORIZED UPDATE", "The action cannot be performed by this user");
			}
		} else {
			errorMap.put("UNAUTHORIZED UPDATE",
					"No workflow state configured for the current status of the application");
		}

		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}

	}

	/**
	 * Validates if the any new object is added in the request
	 * 
	 * @param request The bpa update request
	 */
	private void validateIds(RGARequest request, BusinessService businessService) {
		Map<String, String> errorMap = new HashMap<>();
		RGA rGA = request.getRegularisation();

		if (!workflowService.isStateUpdatable(rGA.getStatus(), businessService)) {
			if (rGA.getId() == null) {
				errorMap.put(RGAConstants.INVALID_UPDATE, "Id of Application cannot be null");
			}

			if (!CollectionUtils.isEmpty(rGA.getDocuments())) {
				rGA.getDocuments().forEach(document -> {
					if (document.getId() == null)
						errorMap.put(RGAConstants.INVALID_UPDATE, "Id of applicationDocument cannot be null");
				});
			}

		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

}
