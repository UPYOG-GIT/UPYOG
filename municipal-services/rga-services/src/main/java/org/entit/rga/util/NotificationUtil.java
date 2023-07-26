package org.entit.rga.util;

import static org.entit.rga.util.RGAConstants.BILL_AMOUNT;
import static org.springframework.util.StringUtils.capitalize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.producer.Producer;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.service.EDCRService;
import org.entit.rga.service.UserService;
import org.entit.rga.web.model.Email;
import org.entit.rga.web.model.EmailRequest;
import org.entit.rga.web.model.EventRequest;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.RequestInfoWrapper;
import org.entit.rga.web.model.SMSRequest;
import org.entit.rga.web.model.collection.PaymentResponse;
import org.entit.rga.web.model.user.UserDetailResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationUtil {

	private static final String STAKEHOLDER_TYPE = "{STAKEHOLDER_TYPE}";

    private static final String STAKEHOLDER_NAME = "{STAKEHOLDER_NAME}";

    private static final String AMOUNT_TO_BE_PAID = "{AMOUNT_TO_BE_PAID}";

    private RGAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private Producer producer;
	
	private EDCRService edcrService;
	
	private RGAUtil bpaUtil;

	private RestTemplate restTemplate;

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper mapper;


	@Autowired
	public NotificationUtil(RGAConfiguration config, ServiceRequestRepository serviceRequestRepository,
			Producer producer, EDCRService edcrService, RGAUtil bpaUtil) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.producer = producer;
		this.edcrService = edcrService;
		this.bpaUtil = bpaUtil;
		this.restTemplate = restTemplate;
	}

	final String receiptNumberKey = "receiptNumber";

	final String amountPaidKey = "amountPaid";
	private String URL = "url";


	/**
	 * Creates customized message based on bpa
	 * 
	 * @param rGA
	 *            The bpa for which message is to be sent
	 * @param localizationMessage
	 *            The messages from localization
	 * @return customized message based on bpa
	 */
	@SuppressWarnings("unchecked")
	public String getCustomizedMsg(RequestInfo requestInfo, RGA rGA, String localizationMessage) {
		String message = null, messageTemplate;
		Map<String, String> edcrResponse = edcrService.getEDCRDetails(requestInfo, rGA);
		
		String applicationType = edcrResponse.get(RGAConstants.APPLICATIONTYPE);
		String serviceType = edcrResponse.get(RGAConstants.SERVICETYPE);

		if (rGA.getStatus().toString().toUpperCase().equals(RGAConstants.STATUS_REJECTED)) {
			messageTemplate = getMessageTemplate(
					applicationType + "_" + serviceType + "_" + RGAConstants.STATUS_REJECTED, localizationMessage);
			message = getInitiatedMsg(rGA, messageTemplate, serviceType);
		} else {

			String messageCode = applicationType + "_" + serviceType + "_" + rGA.getWorkflow().getAction() + "_"
					+ rGA.getStatus();

			messageTemplate = getMessageTemplate(messageCode, localizationMessage);
			if (!StringUtils.isEmpty(messageTemplate)) {
				message = getInitiatedMsg(rGA, messageTemplate, serviceType);

				if (message.contains(AMOUNT_TO_BE_PAID)) {
					BigDecimal amount = getAmountToBePaid(requestInfo, rGA);
					message = message.replace(AMOUNT_TO_BE_PAID, amount.toString());
				}
			}
		}
		return message;
	}

	@SuppressWarnings("unchecked")
	// As per OAP-304, keeping the same messages for Events and SMS, so removed
	// "M_" prefix for the localization codes.
	// so it will be same as the getCustomizedMsg
	public String getEventsCustomizedMsg(RequestInfo requestInfo, RGA rGA, String localizationMessage) {
		String message = null, messageTemplate;
		Map<String, String> edcrResponse = edcrService.getEDCRDetails(requestInfo, rGA);		
		String applicationType = edcrResponse.get(RGAConstants.APPLICATIONTYPE);
		String serviceType = edcrResponse.get(RGAConstants.SERVICETYPE);
		
		if (rGA.getStatus().toString().toUpperCase().equals(RGAConstants.STATUS_REJECTED)) {
			messageTemplate = getMessageTemplate(RGAConstants.M_APP_REJECTED, localizationMessage);
			message = getInitiatedMsg(rGA, messageTemplate, serviceType);
		} else {
			String messageCode = applicationType + "_" + serviceType + "_" + rGA.getWorkflow().getAction()
					+ "_" + rGA.getStatus();
			messageTemplate = getMessageTemplate(messageCode, localizationMessage);
			if (!StringUtils.isEmpty(messageTemplate)) {
				message = getInitiatedMsg(rGA, messageTemplate, serviceType);
				if (message.contains(AMOUNT_TO_BE_PAID)) {
					BigDecimal amount = getAmountToBePaid(requestInfo, rGA);
					message = message.replace(AMOUNT_TO_BE_PAID, amount.toString());
				}
			}
		}
		return message;

	}

	/**
	 * Extracts message for the specific code
	 * 
	 * @param notificationCode
	 *            The code for which message is required
	 * @param localizationMessage
	 *            The localization messages
	 * @return message for the specific code
	 */
	@SuppressWarnings("rawtypes")
	public String getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		String message = null;
		try {
			List data = JsonPath.parse(localizationMessage).read(path);
			if (!CollectionUtils.isEmpty(data))
				message = data.get(0).toString();
			else
				log.error("Fetching from localization failed with code " + notificationCode);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		return message;
	}

	/**
	 * Fetches the amount to be paid from getBill API
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the request
	 * @param license
	 *            The TradeLicense object for which
	 * @return
	 */
	private BigDecimal getAmountToBePaid(RequestInfo requestInfo, RGA rGA) {

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(bpaUtil.getBillUri(rGA),
				new RequestInfoWrapper(requestInfo));
		JSONObject jsonObject = new JSONObject(responseMap);
		BigDecimal amountToBePaid;
		double amount = 0.0;
		try {
			JSONArray demandArray = (JSONArray) jsonObject.get("Demands");
			if (demandArray != null) {
				JSONObject firstElement = (JSONObject) demandArray.get(0);
				if (firstElement != null) {
					JSONArray demandDetails = (JSONArray) firstElement.get("demandDetails");
					if (demandDetails != null) {
						for (int i = 0; i < demandDetails.length(); i++) {
							JSONObject object = (JSONObject) demandDetails.get(i);
							Double taxAmt = Double.valueOf((object.get("taxAmount").toString()));
							amount = amount + taxAmt;
						}
					}
				}
			}
			amountToBePaid = BigDecimal.valueOf(amount);
		} catch (Exception e) {
			throw new CustomException("PARSING ERROR",
					"Failed to parse the response using jsonPath: "
							+ BILL_AMOUNT);
		}
		return amountToBePaid;
	}

	

	/**
	 * Returns the uri for the localization call
	 * 
	 * @param tenantId
	 *            TenantId of the propertyRequest
	 * @return The uri for localization search call
	 */
	public StringBuilder getUri(String tenantId, RequestInfo requestInfo) {

		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];

		String locale = "en_IN";
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(RGAConstants.SEARCH_MODULE);
		return uri;
	}

	/**
	 * Fetches messages from localization service
	 * 
	 * @param tenantId
	 *            tenantId of the BPA
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return Localization messages for the module
	 */
	@SuppressWarnings("rawtypes")
	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo),
				requestInfo);
		String jsonString = new JSONObject(responseMap).toString();
		return jsonString;
	}

	/**
	 * Creates customized message for initiate
	 * 
	 * @param rGA
	 *            tenantId of the bpa
	 * @param message
	 *            Message from localization for initiate
	 * @return customized message for initiate
	 */
	private String getInitiatedMsg(RGA rGA, String message, String serviceType) {
		if("REGULARISATION".equals(serviceType))
			message = message.replace("{2}", "New Construction");
		else
			message = message.replace("{2}", serviceType);

		message = message.replace("{3}", rGA.getApplicationNo());
		return message;
	}


	/**
	 * Send the SMSRequest on the SMSNotification kafka topic
	 * 
	 * @param smsRequestList
	 *            The list of SMSRequest to be sent
	 */
	public void sendSMS(List<org.entit.rga.web.model.SMSRequest> smsRequestList, boolean isSMSEnabled) {
		if (isSMSEnabled) {
			if (CollectionUtils.isEmpty(smsRequestList))
				log.debug("Messages from localization couldn't be fetched!");
			for (SMSRequest smsRequest : smsRequestList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				log.debug("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
			}
		}
	}

	/**
	 * Creates sms request for the each owners
	 * 
	 * @param message
	 *            The message for the specific bpa
	 * @param mobileNumberToOwnerName
	 *            Map of mobileNumber to OwnerName
	 * @return List of SMSRequest
	 */
	public List<SMSRequest> createSMSRequest(RGARequest rGARequest,String message, Map<String, String> mobileNumberToOwner) {
		List<SMSRequest> smsRequest = new LinkedList<>();

		for (Map.Entry<String, String> entryset : mobileNumberToOwner.entrySet()) {
			String customizedMsg = message.replace("{1}", entryset.getValue());
			if (customizedMsg.contains("{RECEIPT_LINK}")) {
				String linkToReplace = getRecepitDownloadLink(rGARequest, entryset.getKey());
//				log.info("Link to replace - "+linkToReplace);
				customizedMsg = customizedMsg.replace("{RECEIPT_LINK}",linkToReplace);
			}
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg));
		}
		return smsRequest;
	}
	
	
	/**
	 * Pushes the event request to Kafka Queue.
	 * 
	 * @param request
	 */
	public void sendEventNotification(EventRequest request) {
		producer.push(config.getSaveUserEventsTopic(), request);

		log.debug("STAKEHOLDER:: " + request.getEvents().get(0).getDescription());
	}

	public String getEmailCustomizedMsg(RequestInfo requestInfo, RGA rGA, String localizationMessage) {
		String message = null, messageTemplate;
		Map<String, String> edcrResponse = edcrService.getEDCRDetails(requestInfo, rGA);

		String applicationType = edcrResponse.get(RGAConstants.APPLICATIONTYPE);
		String serviceType = edcrResponse.get(RGAConstants.SERVICETYPE);

		if (rGA.getStatus().toString().toUpperCase().equals(RGAConstants.STATUS_REJECTED)) {
			messageTemplate = getMessageTemplate(
					applicationType + "_" + serviceType + "_" + RGAConstants.STATUS_REJECTED + "_" + "EMAIL", localizationMessage);
			message = getReplacedMessage(rGA, messageTemplate,serviceType);
		} else {
			String messageCode = applicationType + "_" + serviceType + "_" + rGA.getWorkflow().getAction() + "_"
					+ rGA.getStatus() + "_" + "EMAIL";

			messageTemplate = getMessageTemplate(messageCode, localizationMessage);

			if (!StringUtils.isEmpty(messageTemplate)) {
				message = getReplacedMessage(rGA, messageTemplate,serviceType);

				if (message.contains(AMOUNT_TO_BE_PAID)) {
					BigDecimal amount = getAmountToBePaid(requestInfo, rGA);
					message = message.replace(AMOUNT_TO_BE_PAID, amount.toString());
				}
				if(message.contains(STAKEHOLDER_NAME) || message.contains(STAKEHOLDER_TYPE))
				{
					message  = getStakeHolderDetailsReplaced(requestInfo,rGA, message);
				}
		 }
		}
		return message;
	}
			public String getStakeHolderDetailsReplaced(RequestInfo requestInfo, RGA rGA, String message)
		{
				String stakeUUID = rGA.getAuditDetails().getCreatedBy();
				List<String> ownerId = new ArrayList<String>();
				ownerId.add(stakeUUID);
				RGASearchCriteria bpaSearchCriteria = new RGASearchCriteria();
				bpaSearchCriteria.setOwnerIds(ownerId);
				bpaSearchCriteria.setTenantId(rGA.getTenantId());
				UserDetailResponse userDetailResponse = userService.getUser(bpaSearchCriteria,requestInfo);
				if(message.contains(STAKEHOLDER_TYPE))
				{message = message.replace(STAKEHOLDER_TYPE, userDetailResponse.getUser().get(0).getType());}
			if(message.contains(STAKEHOLDER_NAME))
			{message = message.replace(STAKEHOLDER_NAME, userDetailResponse.getUser().get(0).getName());}

			 return message;
		}

		private String getReplacedMessage(RGA bpa, String message,String serviceType) {

		if("REGULARISATION".equals(serviceType))
			message = message.replace("{2}", "New Construction");
		else
			message = message.replace("{2}", serviceType);

			message = message.replace("{3}", bpa.getApplicationNo());
		message = message.replace("{1}", capitalize(bpa.getTenantId().split("\\.")[1]));
		message = message.replace("{PORTAL_LINK}",config.getUiAppHost());
		//CCC - Designaion configurable according to ULB
		// message = message.replace("CCC","");
		return message;
	}

	public List<EmailRequest> createEmailRequest(RGARequest rGARequest,String message, Map<String, String> mobileNumberToEmailId) {

		List<EmailRequest> emailRequest = new LinkedList<>();
		for (Map.Entry<String, String> entryset : mobileNumberToEmailId.entrySet()) {
			String customizedMsg = message.replace("{1}",entryset.getValue());
			customizedMsg = customizedMsg.replace("{MOBILE_NUMBER}",entryset.getKey());
			if (customizedMsg.contains("{RECEIPT_LINK}")) {
				String linkToReplace = getRecepitDownloadLink(rGARequest, entryset.getKey());
//				log.info("Link to replace - "+linkToReplace);
				customizedMsg = customizedMsg.replace("{RECEIPT_LINK}",linkToReplace);
			}
			String subject = customizedMsg.substring(customizedMsg.indexOf("<h2>")+4,customizedMsg.indexOf("</h2>"));
			String body = customizedMsg.substring(customizedMsg.indexOf("</h2>")+4);
			Email emailobj = Email.builder().emailTo(Collections.singleton(entryset.getValue())).isHTML(true).body(body).subject(subject).build();
			EmailRequest email = new EmailRequest(rGARequest.getRequestInfo(),emailobj);
			emailRequest.add(email);
		}
		return emailRequest;
	}

	/**
	 * Send the EmailRequest on the EmailNotification kafka topic
	 *
	 * @param emailRequestList
	 *            The list of EmailRequest to be sent
	 */
	public void sendEmail(List<EmailRequest> emailRequestList) {

		if (config.getIsEmailNotificationEnabled()) {
			if (CollectionUtils.isEmpty(emailRequestList))
				log.info("Messages from localization couldn't be fetched!");
			for (EmailRequest emailRequest : emailRequestList) {
				producer.push(config.getEmailNotifTopic(), emailRequest);
				log.info("Email Request -> "+emailRequest.toString());
				log.info("EMAIL notification sent!");
			}
		}
	}

	/**
	 * Fetches email ids of CITIZENs based on the phone number.
	 *
	 * @param mobileNumbers
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */

	public Map<String, String> fetchUserEmailIds(Set<String> mobileNumbers, RequestInfo requestInfo, String tenantId) {
		Map<String, String> mapOfPhnoAndEmailIds = new HashMap<>();
		StringBuilder uri = new StringBuilder();
		uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
		Map<String, Object> userSearchRequest = new HashMap<>();
		userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("tenantId", tenantId);
		userSearchRequest.put("userType", "CITIZEN");
		for(String mobileNo: mobileNumbers) {
			userSearchRequest.put("userName", mobileNo);
			try {
				Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
				if(null != user) {
					if(JsonPath.read(user, "$.user[0].emailId")!=null) {
						String email = JsonPath.read(user, "$.user[0].emailId");
						mapOfPhnoAndEmailIds.put(mobileNo, email);
					}
				}else {
					log.error("Service returned null while fetching user for username - "+mobileNo);
				}
			}catch(Exception e) {
				log.error("Exception while fetching user for username - "+mobileNo);
				log.error("Exception trace: ",e);
				continue;
			}
		}
		return mapOfPhnoAndEmailIds;
	}

	public String getRecepitDownloadLink(RGARequest rGARequest, String mobileno) {

		String receiptNumber = getReceiptNumber(rGARequest);
		String consumerCode;
		consumerCode = rGARequest.getRegularisation().getApplicationNo();
			String link = config.getUiAppHost() + config.getReceiptDownloadLink();
			link = link.replace("$consumerCode", consumerCode);
			link = link.replace("$tenantId", rGARequest.getRegularisation().getTenantId());
			link = link.replace("$businessService", rGARequest.getRegularisation().getBusinessService());
			link = link.replace("$receiptNumber", receiptNumber);
			link = link.replace("$mobile", mobileno);
			link = getShortnerURL(link);
        log.info(link);
		return link;
	}

	public String getReceiptNumber(RGARequest rGARequest){
		String consumerCode,service;

		consumerCode = rGARequest.getRegularisation().getApplicationNo();
		service = "BPA";

		StringBuilder URL = getcollectionURL();
		URL.append(service).append("/_search").append("?").append("consumerCodes=").append(consumerCode)
				.append("&").append("tenantId=").append(rGARequest.getRegularisation().getTenantId());
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(rGARequest.getRequestInfo()).build();
		Object response = serviceRequestRepository.fetchResult(URL,requestInfoWrapper);
		PaymentResponse paymentResponse = mapper.convertValue(response, PaymentResponse.class);
		return paymentResponse.getPayments().get(0).getPaymentDetails().get(0).getReceiptNumber();
	}

	public StringBuilder getcollectionURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCollectionHost()).append(config.getPaymentSearch());
	}

	public String getShortnerURL(String actualURL) {
		net.minidev.json.JSONObject obj = new net.minidev.json.JSONObject();
		obj.put(URL, actualURL);
		String url = config.getUrlShortnerHost() + config.getShortenerURL();

		Object response = serviceRequestRepository.getShorteningURL(new StringBuilder(url), obj);
		return response.toString();
	}
}
