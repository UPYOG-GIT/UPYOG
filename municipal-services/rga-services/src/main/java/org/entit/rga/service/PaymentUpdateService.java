package org.entit.rga.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.RGARepository;
import org.entit.rga.util.RGAErrorConstants;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.Workflow;
import org.entit.rga.web.model.collection.PaymentDetail;
import org.entit.rga.web.model.collection.PaymentRequest;
import org.entit.rga.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentUpdateService {

	private RGAConfiguration config;

	private RGARepository repository;

	private WorkflowIntegrator wfIntegrator;

	private EnrichmentService enrichmentService;

	private ObjectMapper mapper;

	@Autowired
	public PaymentUpdateService(RGAConfiguration config, RGARepository repository,
			WorkflowIntegrator wfIntegrator, EnrichmentService enrichmentService, ObjectMapper mapper) {
		this.config = config;
		this.repository = repository;
		this.wfIntegrator = wfIntegrator;
		this.enrichmentService = enrichmentService;
		this.mapper = mapper;

	}

	final String tenantId = "tenantId";

	final String businessService = "businessService";

	final String consumerCode = "consumerCode";

	/**
	 * Process the message from kafka and updates the status to paid
	 * 
	 * @param record
	 *            The incoming message from receipt create consumer
	 */
	public void process(HashMap<String, Object> record) {

		try {
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();
			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			String tenantId = paymentRequest.getPayment().getTenantId();

			for (PaymentDetail paymentDetail : paymentDetails) {

				List<String> businessServices = new ArrayList<String>(
						Arrays.asList(config.getBusinessService().split(",")));
				if (businessServices.contains(paymentDetail.getBusinessService())) {
					RGASearchCriteria searchCriteria = new RGASearchCriteria();
					searchCriteria.setTenantId(tenantId);
//					List<String> codes = Arrays.asList(paymentDetail.getBill().getConsumerCode());
					searchCriteria.setApplicationNo(paymentDetail.getBill().getConsumerCode());
					List<RGA> rGAs = repository.getRegularisationData(searchCriteria, null);
					if (CollectionUtils.isEmpty(rGAs)) {
						throw new CustomException(RGAErrorConstants.INVALID_RECEIPT,
								"No Building Plan Application found for the comsumerCode "
										+ searchCriteria.getApplicationNo());
					}
					Workflow workflow = Workflow.builder().action("PAY").build();
					rGAs.forEach(bpa -> bpa.setWorkflow(workflow));
					
					// FIXME check if the update call to repository can be avoided
					// FIXME check why aniket is not using request info from consumer
					// REMOVE SYSTEM HARDCODING AFTER ALTERING THE CONFIG IN WF FOR TL

					Role role = Role.builder().code("SYSTEM_PAYMENT").tenantId(rGAs.get(0).getTenantId()).build();
					requestInfo.getUserInfo().getRoles().add(role);
					role = Role.builder().code("CITIZEN").tenantId(rGAs.get(0).getTenantId()).build();
					requestInfo.getUserInfo().getRoles().add(role);
					RGARequest updateRequest = RGARequest.builder().requestInfo(requestInfo).RGA(rGAs.get(0)).build();

					/*
					 * calling workflow to update status
					 */
					wfIntegrator.callWorkFlow(updateRequest);

					log.info(" the status of the application is : " + updateRequest.getRegularisation().getStatus());

					/*
					 * calling repository to update the object in eg_bpa_buildingpaln tables
					 */
					enrichmentService.postStatusEnrichment(updateRequest);

					repository.update(updateRequest, false);

				}
			}
		} catch (Exception e) {
			log.error("KAFKA_PROCESS_ERROR:", e);
		}
	}
}
