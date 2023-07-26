package org.entit.rga.service.notification;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static org.entit.rga.util.RGAConstants.ACTION;
import static org.entit.rga.util.RGAConstants.BPA_BUSINESSSERVICE;
import static org.entit.rga.util.RGAConstants.CHANNEL;
import static org.entit.rga.util.RGAConstants.CHANNEL_LIST;
import static org.entit.rga.util.RGAConstants.CHANNEL_NAME_EMAIL;
import static org.entit.rga.util.RGAConstants.CHANNEL_NAME_EVENT;
import static org.entit.rga.util.RGAConstants.CHANNEL_NAME_SMS;
import static org.entit.rga.util.RGAConstants.MODULE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.service.RGALandService;
import org.entit.rga.service.UserService;
import org.entit.rga.util.NotificationUtil;
import org.entit.rga.util.RGAConstants;
import org.entit.rga.util.RGAUtil;
import org.entit.rga.web.model.Action;
import org.entit.rga.web.model.ActionItem;
import org.entit.rga.web.model.EmailRequest;
import org.entit.rga.web.model.Event;
import org.entit.rga.web.model.EventRequest;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.Recepient;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.SMSRequest;
import org.entit.rga.web.model.landInfo.LandInfo;
import org.entit.rga.web.model.landInfo.LandSearchCriteria;
import org.entit.rga.web.model.landInfo.Source;
import org.entit.rga.web.model.user.UserDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RGANotificationService {

	private RGAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private NotificationUtil util;
	
	private RGAUtil bpaUtil;

	@Autowired
	private UserService userService;
	
	@Autowired
	private RGALandService bpalandService;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsUrl;

	@Autowired
	public RGANotificationService(RGAConfiguration config, ServiceRequestRepository serviceRequestRepository,
			NotificationUtil util, RGAUtil bpaUtil) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.util = util;
		this.bpaUtil = bpaUtil;
	}

	/**
	 * Creates and send the sms based on the regularisationRequest
	 * 
	 * @param request
	 *            The regularisationRequest listenend on the kafka topic
	 */
	public void process(RGARequest rGARequest) {
		RequestInfo requestInfo = rGARequest.getRequestInfo();
		Map<String, String> mobileNumberToOwner = new HashMap<>();
		String tenantId = rGARequest.getRegularisation().getTenantId();
		String action = rGARequest.getRegularisation().getWorkflow().getAction();
		List<String> configuredChannelNames =  fetchChannelList(new RequestInfo(), tenantId, BPA_BUSINESSSERVICE, action);
		Set<String> mobileNumbers = new HashSet<>();
		mobileNumberToOwner = getUserList(rGARequest);

		for (Map.Entry<String, String> entryset : mobileNumberToOwner.entrySet()) {
			mobileNumbers.add(entryset.getKey());
		}

			if(configuredChannelNames.contains(CHANNEL_NAME_SMS)){
		List<SMSRequest> smsRequests = new LinkedList<>();
		if (null != config.getIsSMSEnabled()) {
			if (config.getIsSMSEnabled()) {
				enrichSMSRequest(rGARequest, smsRequests);
				if (!CollectionUtils.isEmpty(smsRequests))
					util.sendSMS(smsRequests, config.getIsSMSEnabled());
			}
		}
		}

		if(configuredChannelNames.contains(CHANNEL_NAME_EVENT)){
			if (null != config.getIsUserEventsNotificationEnabled()) {
				if (config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEvents(rGARequest);
				if (null != eventRequest)
					util.sendEventNotification(eventRequest);
			    }
		    }
		}

		if(configuredChannelNames.contains(CHANNEL_NAME_EMAIL)){
//			EMAIL block TBD
			if (null != config.getIsEmailNotificationEnabled()) {
				if (config.getIsEmailNotificationEnabled()) {
					Map<String, String> mapOfPhnoAndEmail = util.fetchUserEmailIds(mobileNumbers, requestInfo, tenantId);
					String localizationMessages = util.getLocalizationMessages(tenantId, rGARequest.getRequestInfo());
					String message = util.getEmailCustomizedMsg(rGARequest.getRequestInfo(), rGARequest.getRegularisation(), localizationMessages);
					List<EmailRequest> emailRequests = util.createEmailRequest(rGARequest, message, mapOfPhnoAndEmail);
					util.sendEmail(emailRequests);
				}
			}
		}
	}

	/**
	 * Creates and registers an event at the egov-user-event service at defined
	 * trigger points as that of sms notifs.
	 * 
	 * Assumption - The regularisationRequest received will always contain only one BPA.
	 * 
	 * @param request
	 * @return
	 */
	public EventRequest getEvents(RGARequest rGARequest) {

		List<Event> events = new ArrayList<>();
		String tenantId = rGARequest.getRegularisation().getTenantId();
		String localizationMessages = util.getLocalizationMessages(tenantId, rGARequest.getRequestInfo()); // --need
																											// localization
																											// service
																											// changes.
		String message = util.getEventsCustomizedMsg(rGARequest.getRequestInfo(), rGARequest.getRegularisation(),
				localizationMessages); // --need localization service changes.
		RGA regularisationApplication = rGARequest.getRegularisation();
		Map<String, String> mobileNumberToOwner = getUserList(rGARequest);

		List<SMSRequest> smsRequests = util.createSMSRequest(rGARequest,message, mobileNumberToOwner);
		Set<String> mobileNumbers = smsRequests.stream().map(SMSRequest::getMobileNumber).collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, rGARequest.getRequestInfo(),
				rGARequest.getRegularisation().getTenantId());

		Map<String, String> mobileNumberToMsg = smsRequests.stream()
				.collect(Collectors.toMap(SMSRequest::getMobileNumber, SMSRequest::getMessage));
		for (String mobile : mobileNumbers) {
			if (null == mapOfPhnoAndUUIDs.get(mobile) || null == mobileNumberToMsg.get(mobile)) {
				log.error("No UUID/SMS for mobile {} skipping event", mobile);
				continue;
			}
			List<String> toUsers = new ArrayList<>();
			toUsers.add(mapOfPhnoAndUUIDs.get(mobile));
			Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
			List<String> payTriggerList = Arrays.asList(config.getPayTriggers().split("[,]"));
			Action action = null;
			if (payTriggerList.contains(regularisationApplication.getStatus())) {
				List<ActionItem> items = new ArrayList<>();
				String busineService = bpaUtil.getFeeBusinessSrvCode(regularisationApplication);
				String actionLink = config.getPayLink().replace("$mobile", mobile)
						.replace("$applicationNo", regularisationApplication.getApplicationNo())
						.replace("$tenantId", regularisationApplication.getTenantId()).replace("$businessService", busineService);
				actionLink = config.getUiAppHost() + actionLink;
				ActionItem item = ActionItem.builder().actionUrl(actionLink).code(config.getPayCode()).build();
				items.add(item);
				action = Action.builder().actionUrls(items).build();
			}

			events.add(Event.builder().tenantId(regularisationApplication.getTenantId()).description(mobileNumberToMsg.get(mobile))
					.eventType(RGAConstants.USREVENTS_EVENT_TYPE).name(RGAConstants.USREVENTS_EVENT_NAME)
					.postedBy(RGAConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP)
					.recepient(recepient)
					.eventDetails(null).actions(action).build());
		}

		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(rGARequest.getRequestInfo()).events(events).build();
		} else {
			return null;
		}

	}

	/**
	 * Fetches UUIDs of CITIZENs based on the phone number.
	 * 
	 * @param mobileNumbers
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	private Map<String, String> fetchUserUUIDs(Set<String> mobileNumbers, RequestInfo requestInfo, String tenantId) {

		Map<String, String> mapOfPhnoAndUUIDs = new HashMap<>();
		StringBuilder uri = new StringBuilder();
		uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
		Map<String, Object> userSearchRequest = new HashMap<>();
		userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("tenantId", tenantId);
		userSearchRequest.put("userType", "CITIZEN");
		for (String mobileNo : mobileNumbers) {
			userSearchRequest.put("userName", mobileNo);
			try {
				Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
				if (null != user) {
					String uuid = JsonPath.read(user, "$.user[0].uuid");
					mapOfPhnoAndUUIDs.put(mobileNo, uuid);
				} else {
					log.error("Service returned null while fetching user for username - " + mobileNo);
				}
			} catch (Exception e) {
				log.error("Exception while fetching user for username - " + mobileNo);
				log.error("Exception trace: ", e);
				continue;
			}
		}
		return mapOfPhnoAndUUIDs;
	}

	/**
	 * Enriches the smsRequest with the customized messages
	 * 
	 * @param request
	 *            The regularisationRequest from kafka topic
	 * @param smsRequests
	 *            List of SMSRequets
	 */
	private void enrichSMSRequest(RGARequest rGARequest, List<SMSRequest> smsRequests) {
		String tenantId = rGARequest.getRegularisation().getTenantId();
		String localizationMessages = util.getLocalizationMessages(tenantId, rGARequest.getRequestInfo());
		String message = util.getCustomizedMsg(rGARequest.getRequestInfo(), rGARequest.getRegularisation(), localizationMessages);
		Map<String, String> mobileNumberToOwner = getUserList(rGARequest);
		smsRequests.addAll(util.createSMSRequest(rGARequest,message, mobileNumberToOwner));

	}

	/**
	 * To get the Users to whom we need to send the sms notifications or event
	 * notifications.
	 * 
	 * @param rGARequest
	 * @return
	 */
	private Map<String, String> getUserList(RGARequest rGARequest) {
		Map<String, String> mobileNumberToOwner = new HashMap<>();
		String tenantId = rGARequest.getRegularisation().getTenantId();
		String stakeUUID = rGARequest.getRegularisation().getAuditDetails().getCreatedBy();
		List<String> ownerId = new ArrayList<String>();
		ownerId.add(stakeUUID);
		RGASearchCriteria bpaSearchCriteria = new RGASearchCriteria();
		bpaSearchCriteria.setOwnerIds(ownerId);
		bpaSearchCriteria.setTenantId(tenantId);
		UserDetailResponse userDetailResponse = userService.getUser(bpaSearchCriteria, rGARequest.getRequestInfo());

		LandSearchCriteria landcriteria = new LandSearchCriteria();
		landcriteria.setTenantId(bpaSearchCriteria.getTenantId());
		landcriteria.setIds(Arrays.asList(rGARequest.getRegularisation().getLandId()));
		List<LandInfo> landInfo = bpalandService.searchLandInfoToBPA(rGARequest.getRequestInfo(), landcriteria);

		mobileNumberToOwner.put(userDetailResponse.getUser().get(0).getUserName(),
				userDetailResponse.getUser().get(0).getName());
		

		if (rGARequest.getRegularisation().getLandInfo() == null) {
			for (int j = 0; j < landInfo.size(); j++)
				rGARequest.getRegularisation().setLandInfo(landInfo.get(j));
		}
		
		if (!(rGARequest.getRegularisation().getWorkflow().getAction().equals(config.getActionsendtocitizen())
				&& rGARequest.getRegularisation().getStatus().equals("INITIATED"))
				&& !(rGARequest.getRegularisation().getWorkflow().getAction().equals(config.getActionapprove())
						&& rGARequest.getRegularisation().getStatus().equals("INPROGRESS"))) {
			
			rGARequest.getRegularisation().getLandInfo().getOwners().forEach(owner -> {
					if (owner.getMobileNumber() != null) {
						mobileNumberToOwner.put(owner.getMobileNumber(), owner.getName());
					}
			});
			
		}
		return mobileNumberToOwner;
	}

	public List<String> fetchChannelList(RequestInfo requestInfo, String tenantId, String moduleName, String action){
		List<String> masterData = new ArrayList<>();
		StringBuilder uri = new StringBuilder();
		uri.append(mdmsHost).append(mdmsUrl);
		if(StringUtils.isEmpty(tenantId))
			return masterData;
		MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequestForChannelList(requestInfo, tenantId.split("\\.")[0]);

		Filter masterDataFilter = filter(
				where(MODULE).is(moduleName).and(ACTION).is(action)
		);

		try {
			Object response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
			masterData = JsonPath.parse(response).read("$.MdmsRes.Channel.channelList[?].channelNames[*]", masterDataFilter);
		}catch(Exception e) {
			log.error("Exception while fetching workflow states to ignore: ",e);
		}
		return masterData;
	}

	private MdmsCriteriaReq getMdmsRequestForChannelList(RequestInfo requestInfo, String tenantId){
		MasterDetail masterDetail = new MasterDetail();
		masterDetail.setName(CHANNEL_LIST);
		List<MasterDetail> masterDetailList = new ArrayList<>();
		masterDetailList.add(masterDetail);

		ModuleDetail moduleDetail = new ModuleDetail();
		moduleDetail.setMasterDetails(masterDetailList);
		moduleDetail.setModuleName(CHANNEL);
		List<ModuleDetail> moduleDetailList = new ArrayList<>();
		moduleDetailList.add(moduleDetail);

		MdmsCriteria mdmsCriteria = new MdmsCriteria();
		mdmsCriteria.setTenantId(tenantId);
		mdmsCriteria.setModuleDetails(moduleDetailList);

		MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
		mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
		mdmsCriteriaReq.setRequestInfo(requestInfo);

		return mdmsCriteriaReq;
	}

}
