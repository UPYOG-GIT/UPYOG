package org.entit.rga.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.util.RGAConstants;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.landInfo.OwnerInfo;
import org.entit.rga.web.model.user.UserDetailResponse;
import org.entit.rga.web.model.user.UserSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

	@Autowired
	private RGAConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public UserDetailResponse getUsersForBpas(List<RGA> rGAs) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		List<String> ids = new ArrayList<String>();
		List<String> uuids = new ArrayList<String>();
		rGAs.forEach(bpa -> {
			if (bpa.getLandInfo() != null) {
				bpa.getLandInfo().getOwners().forEach(owner -> {
					if (owner.getUuid() != null)
						ids.add(owner.getUuid().toString());

					if (owner.getUuid() != null)
						uuids.add(owner.getUuid().toString());
				});
			}
		});

		userSearchRequest.setId(ids);
		userSearchRequest.setUuid(uuids);
		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		return userCall(userSearchRequest, uri);
	}

	/**
	 * Returns UserDetailResponse by calling user service with given uri and object
	 * 
	 * @param userRequest Request object for user service
	 * @param uri         The address of the end point
	 * @return Response from user service as parsed as userDetailResponse
	 */
	@SuppressWarnings("rawtypes")
	UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
		String dobFormat = null;
		log.info("uri++++++: " + uri.toString());
		if (uri.toString().contains(config.getUserSearchEndpoint())
				|| uri.toString().contains(config.getUserUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(config.getUserCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		try {
			log.info("dobFormat++++++++: " + dobFormat);
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
			UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
			return userDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		}
	}

	/**
	 * Parses date formats to long for all users in responseMap
	 * 
	 * @param responeMap LinkedHashMap got from user api response
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
		List<LinkedHashMap> users = (List<LinkedHashMap>) responeMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (users != null) {
			users.forEach(map -> {
				map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get("lastModifiedDate") != null)
					map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), format1));
				if ((String) map.get("dob") != null)
					map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get("pwdExpiryDate") != null)
					map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), format1));
			});
		}
	}

	/**
	 * Converts date to long
	 * 
	 * @param date   date to be parsed
	 * @param format Format of the date
	 * @return Long value of date
	 */
	private Long dateTolong(String date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}

	/**
	 * Call search in user service based on ownerids from criteria
	 * 
	 * @param criteria    The search criteria containing the ownerids
	 * @param requestInfo The requestInfo of the request
	 * @return Search response from user service based on ownerIds
	 */
	public UserDetailResponse getUser(RGASearchCriteria criteria, RequestInfo requestInfo) {
		log.info("criteria.getTenantId()+++++++ " + criteria.getTenantId());
		UserSearchRequest userSearchRequest = getUserSearchRequest(criteria, requestInfo);
		log.info("userSearchRequest.getTenantId(): " + userSearchRequest.getTenantId());
		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		UserDetailResponse userDetailResponse = userCall(userSearchRequest, uri);
		return userDetailResponse;
	}

	/**
	 * Creates userSearchRequest from bpaSearchCriteria
	 * 
	 * @param criteria    The bpaSearch criteria
	 * @param requestInfo The requestInfo of the request
	 * @return The UserSearchRequest based on ownerIds
	 */
	private UserSearchRequest getUserSearchRequest(RGASearchCriteria criteria, RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setRequestInfo(requestInfo);
//		userSearchRequest.setTenantId(criteria.getTenantId().split("\\.")[0]);
		userSearchRequest.setTenantId(criteria.getTenantId());
		userSearchRequest.setMobileNumber(criteria.getMobileNumber());
		userSearchRequest.setActive(true);
		userSearchRequest.setUserType(RGAConstants.CITIZEN);
		if (!CollectionUtils.isEmpty(criteria.getOwnerIds()))
			userSearchRequest.setUuid(criteria.getOwnerIds());
		return userSearchRequest;
	}

	private UserDetailResponse searchByUserName(String userName, String tenantId) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setUserType("CITIZEN");
		userSearchRequest.setUserName(userName);
		userSearchRequest.setTenantId(tenantId);
		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		return userCall(userSearchRequest, uri);

	}

	private String getStateLevelTenant(String tenantId) {
		return tenantId.split("\\.")[0];
	}

	/**
	 * Searches registered user for mobileNumbers in the given BPA
	 * 
	 * @param rGA
	 * @return uuids of the users
	 */
	public Set<String> getUUidFromUserName(RGA rGA, Map<String, String> mobilenumberToUUIDs) {

		String tenantId = rGA.getTenantId();
		List<OwnerInfo> ownerInfos = rGA.getLandInfo().getOwners();
		// List<OwnerInfo> ownerInfos = bpa.getLandInfo().getOwners().stream().filter(ow
		// -> ow.getActive()).collect(Collectors.toList());
		Set<String> mobileNumbers = new HashSet<>();

		// Get all unique mobileNumbers in the license
		ownerInfos.forEach(owner -> {
			if (owner.getActive()) {
				mobileNumbers.add(owner.getMobileNumber());
			}
		});

		Set<String> uuids = new HashSet<>();

		// For every unique mobilenumber search the use with mobilenumber as username
		// and get uuid
		mobileNumbers.forEach(mobileNumber -> {
//            UserDetailResponse userDetailResponse = searchByUserName(mobileNumber, getStateLevelTenant(tenantId));
			UserDetailResponse userDetailResponse = searchByUserName(mobileNumber, tenantId);
			if (!CollectionUtils.isEmpty(userDetailResponse.getUser())) {
				mobilenumberToUUIDs.put(mobileNumber, userDetailResponse.getUser().get(0).getUuid());
			}
		});

		for (String value : mobilenumberToUUIDs.values()) {
			uuids.add(value);
		}

		return uuids;
	}

}
