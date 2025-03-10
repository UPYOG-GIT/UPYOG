package org.entit.rga.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.repository.ServiceRequestRepository;
import org.entit.rga.util.RGAErrorConstants;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RequestInfoWrapper;
import org.entit.rga.web.model.landInfo.LandInfo;
import org.entit.rga.web.model.landInfo.LandInfoRequest;
import org.entit.rga.web.model.landInfo.LandSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RGALandService {

	@Autowired
	private RGAConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * create landInfo calling land-services/_create api and update the landid to
	 * the BPA
	 * 
	 * @param rGARequest
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addLandInfoToBPA(RGARequest rGARequest) {
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoCreate());

		LandInfoRequest landRequest = new LandInfoRequest();
		landRequest.setRequestInfo(rGARequest.getRequestInfo());
		landRequest.setLandInfo(rGARequest.getRegularisation().getLandInfo());
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, landRequest);
		} catch (Exception se) {
			throw new CustomException(RGAErrorConstants.LANDINFO_EXCEPTION, " LandInfo service call failed.");
		}
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();

		landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		LandInfo landData = mapper.convertValue(landInfo.get(0), LandInfo.class);
		rGARequest.getRegularisation().setLandInfo(landData);
		rGARequest.getRegularisation().setLandId(landData.getId());
	}

	/**
	 * updates the landInfo data of the current BPA record
	 * 
	 * @param rGARequest
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateLandInfo(RGARequest rGARequest) {
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoUpdate());

		LandInfoRequest landRequest = new LandInfoRequest();
		landRequest.setRequestInfo(rGARequest.getRequestInfo());
		landRequest.setLandInfo(rGARequest.getRegularisation().getLandInfo());
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, landRequest);
		} catch (Exception se) {
			throw new CustomException(RGAErrorConstants.LANDINFO_EXCEPTION, " LandInfo service call failed.");
		}
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();

		landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		LandInfo landData = mapper.convertValue(landInfo.get(0), LandInfo.class);
		rGARequest.getRegularisation().setLandInfo(landData);
		rGARequest.getRegularisation().setLandId(landData.getId());
	}

	/**
	 * fetch the land info records for the current BPA
	 * 
	 * @param requestInfo
	 * @param landcriteria
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<LandInfo> searchLandInfoToBPA(RequestInfo requestInfo, LandSearchCriteria landcriteria) {

		log.debug(
				"Searching with the params::" + landcriteria.getIds() + "with mobileNo" + landcriteria.getMobileNumber()
						+ "with landUid" + landcriteria.getLandUId() + "with Ids" + landcriteria.getIds()+ "with localities" + landcriteria.getLocality());
		StringBuilder url = getLandSerchURLWithParams(requestInfo, landcriteria);

		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		LinkedHashMap responseMap = null;
		responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		if (responseMap != null && responseMap.get("LandInfo") != null)
			landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		ArrayList<LandInfo> landData = new ArrayList<LandInfo>();
		if (landInfo.size() > 0) {
			for (int i = 0; i < landInfo.size(); i++) {
				landData.add(mapper.convertValue(landInfo.get(i), LandInfo.class));
			}
		}
		return landData;
	}

	/**
	 * parepre the land-info search url with the parameters for land-services
	 * 
	 * @param requestInfo
	 * @param landcriteria
	 * @return
	 */
	private StringBuilder getLandSerchURLWithParams(RequestInfo requestInfo, LandSearchCriteria landcriteria) {
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoSearch());
		uri.append("?tenantId=");
		uri.append(landcriteria.getTenantId());
		LandSearchCriteria landSearchCriteria = new LandSearchCriteria();
		LandInfoRequest landRequest = new LandInfoRequest();
		landRequest.setRequestInfo(requestInfo);
		if (landcriteria.getIds() != null) {
			landSearchCriteria.setIds(landcriteria.getIds());
			uri.append("&").append("ids=");
			for (int i = 0; i < landcriteria.getIds().size(); i++) {
				if (i != 0) {
					uri.append(",");
				}
				uri.append(landcriteria.getIds().get(i));
			}
		} else if (landcriteria.getMobileNumber() != null) {
			landSearchCriteria.setMobileNumber(landcriteria.getMobileNumber());
			uri.append("&").append("&mobileNumber=");
			uri.append(landcriteria.getMobileNumber());
		}
		if(landcriteria.getLocality() != null) {
		    landSearchCriteria.setLocality(landcriteria.getLocality());
                    uri.append("&").append("&locality=");
                    uri.append(landcriteria.getLocality());
		}
		return uri;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<LandInfo> searchLandInfoToBPAForPlaneSearch(RequestInfo requestInfo, LandSearchCriteria landcriteria) {

		log.debug("Searching with the params::" + landcriteria.getIds());
		StringBuilder url = getLandSerchURLWithParamsForPlaneSearch(requestInfo, landcriteria);

		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		LinkedHashMap responseMap = null;
		responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		if (responseMap != null && responseMap.get("LandInfo") != null)
			landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		ArrayList<LandInfo> landData = new ArrayList<LandInfo>();
		if (landInfo.size() > 0) {
			for (int i = 0; i < landInfo.size(); i++) {
				landData.add(mapper.convertValue(landInfo.get(i), LandInfo.class));
			}
		}
		return landData;
	}
	
	private StringBuilder getLandSerchURLWithParamsForPlaneSearch(RequestInfo requestInfo, LandSearchCriteria landcriteria) {
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoSearch());
		uri.append("?tenantId=");
		uri.append(landcriteria.getTenantId());
		LandSearchCriteria landSearchCriteria = new LandSearchCriteria();
		LandInfoRequest landRequest = new LandInfoRequest();
		landRequest.setRequestInfo(requestInfo);
		if (landcriteria.getIds() != null) {
			landSearchCriteria.setIds(landcriteria.getIds());
			uri.append("&").append("ids=");
			for (int i = 0; i < landcriteria.getIds().size(); i++) {
				if (i != 0) {
					uri.append(",");
				}
				uri.append(landcriteria.getIds().get(i));
			}
		}
		return uri;
	}
}
