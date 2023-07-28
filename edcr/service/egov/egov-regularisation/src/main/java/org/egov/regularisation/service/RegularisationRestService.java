package org.egov.regularisation.service;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.dcr.helper.ErrorDetail;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.PlanInformation;
import org.egov.edcr.config.properties.EdcrApplicationSettings;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.admin.master.entity.City;
import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.config.core.EnvironmentSettings;
import org.egov.infra.custom.CustomImplProvider;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.microservice.contract.RequestInfoWrapper;
import org.egov.infra.microservice.contract.ResponseInfo;
import org.egov.infra.microservice.models.RequestInfo;
import org.egov.infra.microservice.models.Role;
import org.egov.infra.microservice.models.UserInfo;
import org.egov.infra.utils.TenantUtils;
import org.egov.regularisation.constants.DxfFileConstants;
import org.egov.regularisation.contract.RegEdcrDetail;
import org.egov.regularisation.contract.RegEdcrRequest;
import org.egov.regularisation.entity.RegApplicationType;
import org.egov.regularisation.entity.RegEdcrApplication;
import org.egov.regularisation.entity.RegEdcrApplicationDetail;
import org.egov.regularisation.entity.RegEdcrIndexData;
import org.egov.regularisation.entity.RegEdcrPdfDetail;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class RegularisationRestService {
	
	private final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

	private static final String MSG_UNQ_TRANSACTION_NUMBER = "Transaction Number should be unique";

	private static final String REQ_BODY_REQUIRED = "Required request body is missing";

	private static final String USER_ID_IS_MANDATORY = "User id is mandatory";

	private static final String BPA_01 = "BPA-01";

	private static final String BPA_07 = "BPA-07";

	private static final String BPA_05 = "BPA-05";
//	 @Autowired
//	    private PlanFeatureService featureService;
	@Autowired
	private FileStoreService fileStoreService;
	@Autowired
	private CustomImplProvider specificRuleService;
	@PersistenceContext
	private EntityManager entityManager;
//	    @Autowired
//	    private EdcrApplicationDetailService edcrApplicationDetailService;
//	    @Autowired
//	    private EdcrPdfDetailService edcrPdfDetailService;
	@Autowired
	private RegularisationExtractService regularisationExtractService;
	@Autowired
	private RegEdcrApplicationService edcrApplicationService;
	@Autowired
	private EnvironmentSettings environmentSettings;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private EdcrApplicationSettings edcrApplicationSettings;
	@Autowired
	private CityService cityService;
	@Autowired
	private TenantUtils tenantUtils;
	
	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	private static Logger LOG = LogManager.getLogger(RegularisationRestService.class);

	@Value("${egov.services.egov-indexer.url}")
	private String egovIndexerUrl;

	@Value("${indexer.host}")
	private String indexerHost;

	public static final String FILE_DOWNLOAD_URL = "%s/edcr/rest/dcr/downloadfile";

//	    @Autowired
//	    private OcComparisonService ocComparisonService;
//	    @Autowired
//	    private OcComparisonDetailService ocComparisonDetailService;
	@Transactional
	public RegEdcrDetail createRegularisationEdcr(final RegEdcrRequest regEdcrRequest, final MultipartFile file,
			Map<String, List<Object>> masterData) {
		RegEdcrApplication regEdcrApplication = new RegEdcrApplication();
		regEdcrApplication.setMdmsMasterData(masterData);
		RegEdcrApplicationDetail regEdcrApplicationDetail = new RegEdcrApplicationDetail();
//			if (ApplicationType.OCCUPANCY_CERTIFICATE.toString().equalsIgnoreCase(edcrRequest.getAppliactionType())) {
//				edcrApplicationDetail.setComparisonDcrNumber(edcrRequest.getComparisonEdcrNumber());
//			}

//	        ///add
//	        if (edcrRequest.getAreaCategory() != null) {
//	        	edcrApplicationDetail.setAreaCategory(edcrRequest.getAreaCategory());
//	        }

		List<RegEdcrApplicationDetail> regEdcrApplicationDetails = new ArrayList<>();
		regEdcrApplicationDetails.add(regEdcrApplicationDetail);
		regEdcrApplication.setTransactionNumber(regEdcrRequest.getTransactionNumber());
		if (isNotBlank(regEdcrRequest.getApplicantName()))
			regEdcrApplication.setApplicantName(regEdcrRequest.getApplicantName());
		else
			regEdcrApplication.setApplicantName(DxfFileConstants.ANONYMOUS_APPLICANT);
		regEdcrApplication.setArchitectInformation(DxfFileConstants.ANONYMOUS_APPLICANT);
		regEdcrApplication.setServiceType(regEdcrRequest.getApplicationSubType());
		if (regEdcrRequest.getAppliactionType() == null)
			regEdcrApplication.setApplicationType(RegApplicationType.REGULARISATION);
		else
			regEdcrApplication.setApplicationType(RegApplicationType.valueOf(regEdcrRequest.getAppliactionType()));
		if (regEdcrRequest.getPermitNumber() != null)
			regEdcrApplication.setPlanPermitNumber(regEdcrRequest.getPermitNumber());

		if (regEdcrRequest.getPermitDate() != null) {
			regEdcrApplication.setPermitApplicationDate(regEdcrRequest.getPermitDate());
		}

		// add for areaCategory
//			edcrApplication.setAreaCategory(edcrRequest.getAreaCategory());
//			edcrApplication.setKhataNo();
//			String kh=planInformation.getKhataNo();
		regEdcrApplication.setEdcrApplicationDetails(regEdcrApplicationDetails);
		regEdcrApplication.setDxfFile(file);

		if (regEdcrRequest.getRequestInfo() != null && regEdcrRequest.getRequestInfo().getUserInfo() != null) {
			regEdcrApplication.setThirdPartyUserCode(isNotBlank(regEdcrRequest.getRequestInfo().getUserInfo().getUuid())
					? regEdcrRequest.getRequestInfo().getUserInfo().getUuid()
					: regEdcrRequest.getRequestInfo().getUserInfo().getId());
			String tenantId = "";
			if (StringUtils.isNotBlank(regEdcrRequest.getTenantId())) {
				String[] tenantArr = regEdcrRequest.getTenantId().split("\\.");
				String tenantFromReq;
				if (tenantArr.length == 1)
					tenantFromReq = tenantArr[0];
				else
					tenantFromReq = tenantArr[1];
				if (tenantFromReq.equalsIgnoreCase(ApplicationThreadLocals.getTenantID()))
					tenantId = regEdcrRequest.getTenantId();
			}

			if (StringUtils.isBlank(tenantId) && regEdcrRequest.getRequestInfo() != null
					&& regEdcrRequest.getRequestInfo().getUserInfo() != null
					&& StringUtils.isNotBlank(regEdcrRequest.getRequestInfo().getUserInfo().getTenantId())) {
				tenantId = regEdcrRequest.getRequestInfo().getUserInfo().getTenantId();
			} else if (StringUtils.isBlank(tenantId)) {
				tenantId = ApplicationThreadLocals.getTenantID();
			}
			regEdcrApplication.setThirdPartyUserTenant(tenantId);
		}

		regEdcrApplication = edcrApplicationService.createRestEdcr(regEdcrApplication);

		// Code to push the data of edcr application to kafka index
		RegEdcrIndexData regEdcrIndexData = new RegEdcrIndexData();
		if (environmentSettings.getDataPush()) {
			// Building object to be pushed
			regEdcrIndexData = setEdcrIndexData(regEdcrApplication,
					regEdcrApplication.getEdcrApplicationDetails().get(0));
			// call kafka topic
			pushDataToIndexer(regEdcrIndexData, "edcr-create-application");
		}

		return setEdcrResponse(regEdcrApplication.getEdcrApplicationDetails().get(0), regEdcrRequest);
	}

	public void pushDataToIndexer(Object data, String topicName) {
		try {
			restTemplate = new RestTemplate();
			StringBuilder uri = new StringBuilder(indexerHost).append(egovIndexerUrl);
			LOG.info("URL created: " + uri.toString());
			restTemplate.postForObject(uri.toString(), data, Object.class, topicName);
			LOG.info("Data pushed in topic->edcr-create-application.\n Data pushed=> \n" + data);
		} catch (RestClientException e) {
			LOG.error("ERROR occurred while trying to push the data to indexer : ", e);
		}
	}

	public RegEdcrIndexData setEdcrIndexData(RegEdcrApplication regEdcrApplication,
			RegEdcrApplicationDetail edcrApplnDtl) {

		RegEdcrIndexData regEdcrIndexData = new RegEdcrIndexData();
		if (regEdcrApplication.getApplicantName() != null) {
			regEdcrIndexData.setApplicantName(regEdcrApplication.getApplicantName());
		}
		if (regEdcrApplication.getApplicationNumber() != null) {
			regEdcrIndexData.setApplicationNumber(regEdcrApplication.getApplicationNumber());
		}
		if (regEdcrApplication.getApplicationType() != null) {
			regEdcrIndexData.setApplicationType(regEdcrApplication.getApplicationType());
		}
		if (regEdcrApplication.getApplicationDate() != null) {
			regEdcrIndexData.setApplicationDate(regEdcrApplication.getApplicationDate());
		}
		if (regEdcrApplication.getStatus() != null) {
			regEdcrIndexData.setStatus(regEdcrApplication.getStatus());
		}
		if (regEdcrApplication.getPlanPermitNumber() != null) {
			regEdcrIndexData.setPlanPermitNumber(regEdcrApplication.getPlanPermitNumber());
		}
		if (regEdcrApplication.getPermitApplicationDate() != null) {
			regEdcrIndexData.setPermitApplicationDate(regEdcrApplication.getPermitApplicationDate());
		}
		if (regEdcrApplication.getTransactionNumber() != null) {
			regEdcrIndexData.setTransactionNumber(regEdcrApplication.getTransactionNumber());
		}
		if (regEdcrApplication.getThirdPartyUserTenant() != null) {
			regEdcrIndexData.setThirdPartyUserTenant(regEdcrApplication.getThirdPartyUserTenant());
		}
		if (regEdcrApplication.getServiceType() != null) {
			regEdcrIndexData.setServiceType(regEdcrApplication.getServiceType());
		}
		if (regEdcrApplication.getArchitectInformation() != null) {
			regEdcrIndexData.setArchitectInformation(regEdcrApplication.getArchitectInformation());
		}
		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber() != null) {
			regEdcrIndexData.setDcrNumber(regEdcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber());
		}
		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getComparisonDcrNumber() != null) {
			regEdcrIndexData.setComparisonDcrNumber(
					regEdcrApplication.getEdcrApplicationDetails().get(0).getComparisonDcrNumber());
		}
		if (edcrApplnDtl.getPlan() != null && edcrApplnDtl.getPlan().getPlot() != null
				&& edcrApplnDtl.getPlan().getPlot().getPlotBndryArea() != null) {
			regEdcrIndexData.setPlotBndryArea(edcrApplnDtl.getPlan().getPlot().getPlotBndryArea());
		}

		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding()
						.getBuildingHeight() != null) {
			regEdcrIndexData.setBuildingHeight(regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan()
					.getVirtualBuilding().getBuildingHeight());
		}
		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding()
						.getOccupancyTypes() != null) {
			regEdcrIndexData.setOccupancyTypes(regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan()
					.getVirtualBuilding().getOccupancyTypes());
		}
		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding()
						.getTotalBuitUpArea() != null) {
			regEdcrIndexData.setTotalBuitUpArea(regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan()
					.getVirtualBuilding().getTotalBuitUpArea());
		}
		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding()
						.getTotalFloorArea() != null) {
			regEdcrIndexData.setTotalFloorArea(regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan()
					.getVirtualBuilding().getTotalFloorArea());
		}
		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding()
						.getTotalCarpetArea() != null) {
			regEdcrIndexData.setTotalCarpetArea(regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan()
					.getVirtualBuilding().getTotalCarpetArea());
		}
		if (regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding() != null
				&& regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan().getVirtualBuilding()
						.getFloorsAboveGround() != null) {
			regEdcrIndexData.setFloorsAboveGround(regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan()
					.getVirtualBuilding().getFloorsAboveGround());
		}
		return regEdcrIndexData;
	}

	public RegEdcrDetail setEdcrResponse(RegEdcrApplicationDetail edcrApplnDtl, RegEdcrRequest regEdcrRequest) {
		RegEdcrDetail regEdcrDetail = new RegEdcrDetail();
		List<String> planPdfs = new ArrayList<>();
		regEdcrDetail.setTransactionNumber(edcrApplnDtl.getApplication().getTransactionNumber());
		LOG.info("edcr number == " + edcrApplnDtl.getDcrNumber());
		regEdcrDetail.setEdcrNumber(edcrApplnDtl.getDcrNumber());
		regEdcrDetail.setStatus(edcrApplnDtl.getStatus());
		LOG.info("application number ==" + edcrApplnDtl.getApplication().getApplicationNumber());
		regEdcrDetail.setApplicationNumber(edcrApplnDtl.getApplication().getApplicationNumber());
		regEdcrDetail.setApplicationDate(edcrApplnDtl.getApplication().getApplicationDate());

		if (edcrApplnDtl.getApplication().getPlanPermitNumber() != null) {
			regEdcrDetail.setPermitNumber(edcrApplnDtl.getApplication().getPlanPermitNumber());
		}

//	        if (edcrApplnDtl.getAreaCategory() != null) {
//	            edcrDetail.setAreaCategory(edcrApplnDtl.getAreaCategory());
//	        }
//	        edcrDetail.setAreaCategory("BR");

		if (edcrApplnDtl.getApplication().getPermitApplicationDate() != null) {
			regEdcrDetail.setPermitDate(edcrApplnDtl.getApplication().getPermitApplicationDate());
		}
		RegApplicationType regApplicationType = edcrApplnDtl.getApplication().getApplicationType();
		if (regApplicationType != null) {
			if (RegApplicationType.PERMIT.getApplicationTypeVal()
					.equalsIgnoreCase(edcrApplnDtl.getApplication().getApplicationType().getApplicationTypeVal())) {
				regEdcrDetail.setAppliactionType("REGULARISATION");
			} else if (RegApplicationType.OCCUPANCY_CERTIFICATE.getApplicationTypeVal()
					.equalsIgnoreCase(edcrApplnDtl.getApplication().getApplicationType().getApplicationTypeVal())) {
				regEdcrDetail.setAppliactionType("BUILDING_OC_PLAN_SCRUTINY");
			} else {
				regEdcrDetail.setAppliactionType(regApplicationType.getApplicationTypeVal());
			}

		}
		if (edcrApplnDtl.getApplication().getServiceType() != null)
			regEdcrDetail.setApplicationSubType(edcrApplnDtl.getApplication().getServiceType());
		String tenantId;
		String[] tenantArr = edcrApplnDtl.getApplication().getThirdPartyUserTenant().split("\\.");
		if (tenantArr.length == 1)
			tenantId = tenantArr[0];
		else
			tenantId = tenantArr[1];
		if (edcrApplnDtl.getDxfFileId() != null)
			regEdcrDetail
					.setDxfFile(format(getFileDownloadUrl(edcrApplnDtl.getDxfFileId().getFileStoreId(), tenantId)));

		if (edcrApplnDtl.getScrutinizedDxfFileId() != null)
			regEdcrDetail.setUpdatedDxfFile(
					format(getFileDownloadUrl(edcrApplnDtl.getScrutinizedDxfFileId().getFileStoreId(), tenantId)));

		if (edcrApplnDtl.getReportOutputId() != null)
			regEdcrDetail.setPlanReport(
					format(getFileDownloadUrl(edcrApplnDtl.getReportOutputId().getFileStoreId(), tenantId)));

		File file = edcrApplnDtl.getPlanDetailFileStore() != null
				? fileStoreService.fetch(edcrApplnDtl.getPlanDetailFileStore().getFileStoreId(),
						DcrConstants.APPLICATION_MODULE_TYPE, tenantId)
				: null;

		if (LOG.isInfoEnabled())
			LOG.info("**************** End - Reading Plan detail file **************" + file);
		try {
			if (file == null) {
				Plan pl1 = new Plan();
				PlanInformation pi = new PlanInformation();
				pi.setApplicantName(edcrApplnDtl.getApplication().getApplicantName());
				pl1.setPlanInformation(pi);
				regEdcrDetail.setPlanDetail(pl1);
			} else {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				Plan pl1 = mapper.readValue(file, Plan.class);
				pl1.getPlanInformation().setApplicantName(edcrApplnDtl.getApplication().getApplicantName());
				if (LOG.isInfoEnabled())
					LOG.info("**************** Plan detail object **************" + pl1);
				regEdcrDetail.setPlanDetail(pl1);
			}
		} catch (IOException e) {
			LOG.log(Level.ERROR, e);
		}

		for (RegEdcrPdfDetail planPdf : edcrApplnDtl.getEdcrPdfDetails()) {
			if (planPdf.getConvertedPdf() != null) {
				String downloadURL = format(getFileDownloadUrl(planPdf.getConvertedPdf().getFileStoreId(),
						ApplicationThreadLocals.getTenantID()));
				planPdfs.add(planPdf.getLayer().concat(" - ").concat(downloadURL));
				for (org.egov.common.entity.edcr.EdcrPdfDetail pdf : regEdcrDetail.getPlanDetail()
						.getEdcrPdfDetails()) {
					if (planPdf.getLayer().equalsIgnoreCase(pdf.getLayer()))
						pdf.setDownloadURL(downloadURL);
				}
			}
		}

		regEdcrDetail.setPlanPdfs(planPdfs);
		regEdcrDetail.setTenantId(regEdcrRequest.getTenantId());
//			edcrDetail.setAreaCategory(edcrRequest.getAreaCategory());// add for areaCategory in response

		if (StringUtils.isNotBlank(regEdcrRequest.getComparisonEdcrNumber()))
			regEdcrDetail.setComparisonEdcrNumber(regEdcrRequest.getComparisonEdcrNumber());

		if (!edcrApplnDtl.getStatus().equalsIgnoreCase("Accepted"))
			regEdcrDetail.setStatus(edcrApplnDtl.getStatus());

		return regEdcrDetail;
	}

	public String getFileDownloadUrl(final String fileStoreId, final String tenantId) {
		return String.format(FILE_DOWNLOAD_URL, ApplicationThreadLocals.getDomainURL()) + "?tenantId=" + tenantId
				+ "&fileStoreId=" + fileStoreId;
	}

	public ResponseInfo createResponseInfoFromRequestInfo(RequestInfo requestInfo, Boolean success) {
		String apiId = null;
		String ver = null;
		String ts = null;
		String resMsgId = "";
		String msgId = null;
		if (requestInfo != null) {
			apiId = requestInfo.getApiId();
			ver = requestInfo.getVer();
			if (requestInfo.getTs() != null)
				ts = requestInfo.getTs().toString();
			msgId = requestInfo.getMsgId();
		}
		String responseStatus = success ? "successful" : "failed";

		return new ResponseInfo(apiId, ver, ts, resMsgId, msgId, responseStatus);
	}

	public List<ErrorDetail> validateEdcrMandatoryFields(final RegEdcrRequest regEdcrRequest) {
		List<ErrorDetail> errors = new ArrayList<>();
		if (StringUtils.isBlank(regEdcrRequest.getAppliactionType())) {
			errors.add(new ErrorDetail("BPA-10", "Application type is missing"));
		}

		if (StringUtils.isBlank(regEdcrRequest.getApplicationSubType())) {
			errors.add(new ErrorDetail("BPA-11", "Service type is missing"));
		}

		return errors;
	}

	public ErrorDetail validateEdcrRequest(final RegEdcrRequest regEdcrRequest, final MultipartFile planFile) {
		if (regEdcrRequest.getRequestInfo() == null)
			return new ErrorDetail(BPA_07, REQ_BODY_REQUIRED);
		else if (regEdcrRequest.getRequestInfo().getUserInfo() == null
				|| (regEdcrRequest.getRequestInfo().getUserInfo() != null
						&& isBlank(regEdcrRequest.getRequestInfo().getUserInfo().getUuid())))
			return new ErrorDetail(BPA_07, USER_ID_IS_MANDATORY);

		if (isBlank(regEdcrRequest.getTransactionNumber()))
			return new ErrorDetail(BPA_07, "Please enter transaction number");
		if (isNotBlank(regEdcrRequest.getTransactionNumber())
				&& edcrApplicationService.findByTransactionNumber(regEdcrRequest.getTransactionNumber()) != null) {
			return new ErrorDetail(BPA_01, MSG_UNQ_TRANSACTION_NUMBER);
		}

		return validatePlanFile(planFile);
	}

	public ErrorDetail validatePlanFile(final MultipartFile file) {
		List<String> dcrAllowedExtenstions = new ArrayList<>(
				Arrays.asList(edcrApplicationSettings.getValue("dcr.dxf.allowed.extenstions").split(",")));

		String fileSize = edcrApplicationSettings.getValue("dcr.dxf.max.size");
		final String maxAllowSizeInMB = fileSize;
		String extension;
		if (file != null && !file.isEmpty()) {
			extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
			if (extension != null && !extension.isEmpty()) {

				if (!dcrAllowedExtenstions.contains(extension.toLowerCase())) {
					return new ErrorDetail("BPA-02", "Please upload " + dcrAllowedExtenstions + " format file only");
				} else if (file.getSize() > (Long.valueOf(maxAllowSizeInMB) * 1024 * 1024)) {
					return new ErrorDetail("BPA-04", "File size should not exceed 30 MB");
				} /*
					 * else if (allowedExtenstions.contains(extension.toLowerCase()) &&
					 * (!mimeTypes.contains(mimeType) ||
					 * StringUtils.countMatches(file.getOriginalFilename(), ".") > 1 ||
					 * file.getOriginalFilename().contains("%00"))) { return new
					 * ErrorDetail("BPA-03", "Malicious file upload"); }
					 */
			}
		} else {
			return new ErrorDetail(BPA_05, "Please upload plan file, It is mandatory");
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public List<RegEdcrDetail> fetchEdcr(final RegEdcrRequest regEdcrRequest, final RequestInfoWrapper reqInfoWrapper) {
		List<RegEdcrApplicationDetail> regEdcrApplications = new ArrayList<>();
		UserInfo userInfo = reqInfoWrapper.getRequestInfo() == null ? null
				: reqInfoWrapper.getRequestInfo().getUserInfo();
		String userId = "";
		if (userInfo != null && StringUtils.isNoneBlank(userInfo.getUuid()))
			userId = userInfo.getUuid();
		else if (userInfo != null && StringUtils.isNoneBlank(userInfo.getId()))
			userId = userInfo.getId();
		// When the user is ANONYMOUS, then search application by edcrno or transaction
		// number
		if (userInfo != null && StringUtils.isNoneBlank(userId) && userInfo.getPrimaryrole() != null
				&& !userInfo.getPrimaryrole().isEmpty()) {
			List<String> roles = userInfo.getPrimaryrole().stream().map(Role::getCode).collect(Collectors.toList());
			LOG.info("****Roles***" + roles);
			if (roles.contains("ANONYMOUS"))
				userId = "";
		}
		if (regEdcrRequest.getLimit() == null)
			regEdcrRequest.setLimit(-1);
		if (regEdcrRequest.getOffset() == null)
			regEdcrRequest.setOffset(0);
		boolean onlyTenantId = regEdcrRequest != null && isBlank(regEdcrRequest.getEdcrNumber())
				&& isBlank(regEdcrRequest.getTransactionNumber()) && isBlank(regEdcrRequest.getAppliactionType())
				&& isBlank(regEdcrRequest.getApplicationSubType()) && isBlank(regEdcrRequest.getStatus())
				&& regEdcrRequest.getFromDate() == null && regEdcrRequest.getToDate() == null
				&& isBlank(regEdcrRequest.getApplicationNumber()) && isNotBlank(regEdcrRequest.getTenantId());

		boolean isStakeholder = regEdcrRequest != null && (isNotBlank(regEdcrRequest.getAppliactionType())
				|| isNotBlank(regEdcrRequest.getApplicationSubType()) || isNotBlank(regEdcrRequest.getStatus())
				|| regEdcrRequest.getFromDate() != null || regEdcrRequest.getToDate() != null);

		City stateCity = cityService.fetchStateCityDetails();

		int limit = Integer.parseInt(environmentSettings.getProperty("egov.edcr.default.limit"));
		int offset = Integer.parseInt(environmentSettings.getProperty("egov.edcr.default.offset"));
		int maxLimit = Integer.parseInt(environmentSettings.getProperty("egov.edcr.max.limit"));
		if (regEdcrRequest.getLimit() != null && regEdcrRequest.getLimit() <= maxLimit)
			limit = regEdcrRequest.getLimit();
		if (regEdcrRequest.getLimit() != null && (regEdcrRequest.getLimit() > maxLimit || regEdcrRequest.getLimit() == -1)) {
			limit = maxLimit;
		}
		if (regEdcrRequest.getLimit() != null)
			offset = regEdcrRequest.getOffset();

		if (regEdcrRequest != null && regEdcrRequest.getTenantId().equalsIgnoreCase(stateCity.getCode())) {
			final Map<String, String> params = new ConcurrentHashMap<>();

			String queryString = searchAtStateTenantLevel(regEdcrRequest, userInfo, userId, onlyTenantId, params,
					isStakeholder);
			LOG.info(queryString);
			final Query query = getCurrentSession().createSQLQuery(queryString).setFirstResult(offset)
					.setMaxResults(limit);
			for (final Map.Entry<String, String> param : params.entrySet())
				query.setParameter(param.getKey(), param.getValue());
			List<Object[]> applns = query.list();
			if (applns.isEmpty()) {
				RegEdcrDetail edcrDetail = new RegEdcrDetail();
				edcrDetail.setErrors("No Record Found");
				return Arrays.asList(edcrDetail);
			} else {
				List<RegEdcrDetail> edcrDetails2 = new ArrayList<>();
				for (Object[] appln : applns)
					edcrDetails2.add(setEdcrResponseForAcrossTenants(appln, stateCity.getCode()));
				List<RegEdcrDetail> sortedList = new ArrayList<>();
				String orderBy = "desc";
				if (isNotBlank(regEdcrRequest.getOrderBy()))
					orderBy = regEdcrRequest.getOrderBy();
				if (orderBy.equalsIgnoreCase("asc"))
					sortedList = edcrDetails2.stream().sorted(Comparator.comparing(RegEdcrDetail::getApplicationDate))
							.collect(Collectors.toList());
				else
					sortedList = edcrDetails2.stream()
							.sorted(Comparator.comparing(RegEdcrDetail::getApplicationDate).reversed())
							.collect(Collectors.toList());

				LOG.info("The number of records = " + edcrDetails2.size());
				return sortedList;
			}
		} else {
			final Criteria criteria = getCriteriaofSingleTenant(regEdcrRequest, userInfo, userId, onlyTenantId,
					isStakeholder);

			LOG.info(criteria.toString());
			criteria.setFirstResult(offset);
			criteria.setMaxResults(limit);
			regEdcrApplications = criteria.list();
		}

		LOG.info("The number of records = " + regEdcrApplications.size());
		if (regEdcrApplications.isEmpty()) {
			RegEdcrDetail edcrDetail = new RegEdcrDetail();
			edcrDetail.setErrors("No Record Found");
			return Arrays.asList(edcrDetail);
		} else {
			return edcrDetailsResponse(regEdcrApplications, regEdcrRequest);
		}
	}

	public Integer fetchCount(final RegEdcrRequest edcrRequest, final RequestInfoWrapper reqInfoWrapper) {
		UserInfo userInfo = reqInfoWrapper.getRequestInfo() == null ? null
				: reqInfoWrapper.getRequestInfo().getUserInfo();
		String userId = "";
		if (userInfo != null && StringUtils.isNoneBlank(userInfo.getUuid()))
			userId = userInfo.getUuid();
		else if (userInfo != null && StringUtils.isNoneBlank(userInfo.getId()))
			userId = userInfo.getId();

		// When the user is ANONYMOUS, then search application by edcrno or transaction
		// number
		if (userInfo != null && StringUtils.isNoneBlank(userId) && userInfo.getPrimaryrole() != null
				&& !userInfo.getPrimaryrole().isEmpty()) {
			List<String> roles = userInfo.getPrimaryrole().stream().map(Role::getCode).collect(Collectors.toList());
			LOG.info("****Roles***" + roles);
			if (roles.contains("ANONYMOUS"))
				userId = "";
		}
		boolean onlyTenantId = edcrRequest != null && isBlank(edcrRequest.getEdcrNumber())
				&& isBlank(edcrRequest.getTransactionNumber()) && isBlank(edcrRequest.getAppliactionType())
				&& isBlank(edcrRequest.getApplicationSubType()) && isBlank(edcrRequest.getStatus())
				&& edcrRequest.getFromDate() == null && edcrRequest.getToDate() == null
				&& isBlank(edcrRequest.getApplicationNumber()) && isNotBlank(edcrRequest.getTenantId());

		boolean isStakeholder = edcrRequest != null && (isNotBlank(edcrRequest.getAppliactionType())
				|| isNotBlank(edcrRequest.getApplicationSubType()) || isNotBlank(edcrRequest.getStatus())
				|| edcrRequest.getFromDate() != null || edcrRequest.getToDate() != null);

		City stateCity = cityService.fetchStateCityDetails();
		if (edcrRequest != null && edcrRequest.getTenantId().equalsIgnoreCase(stateCity.getCode())) {
			final Map<String, String> params = new ConcurrentHashMap<>();

			String queryString = searchAtStateTenantLevel(edcrRequest, userInfo, userId, onlyTenantId, params,
					isStakeholder);

			final Query query = getCurrentSession().createSQLQuery(queryString);
			for (final Map.Entry<String, String> param : params.entrySet())
				query.setParameter(param.getKey(), param.getValue());
			return query.list().size();
		} else {
			final Criteria criteria = getCriteriaofSingleTenant(edcrRequest, userInfo, userId, onlyTenantId,
					isStakeholder);
			return criteria.list().size();
		}

	}
	
	private String searchAtStateTenantLevel(final RegEdcrRequest edcrRequest, UserInfo userInfo, String userId,
			boolean onlyTenantId, final Map<String, String> params, boolean isStakeholder) {
		StringBuilder queryStr = new StringBuilder();
		Map<String, String> tenants = tenantUtils.tenantsMap();
		Iterator<Map.Entry<String, String>> tenantItr = tenants.entrySet().iterator();
		String orderByWrapperDesc = "select * from ({}) as result order by result.applicationDate desc";
		String orderByWrapperAsc = "select * from ({}) as result order by result.applicationDate asc";
		while (tenantItr.hasNext()) {
			Map.Entry<String, String> value = tenantItr.next();
			queryStr.append("(select '").append(value.getKey()).append(
					"' as tenantId,appln.transactionNumber,dtl.dcrNumber,dtl.status,appln.applicantName,dxf.fileStoreId as dxfFileId,scrudxf.fileStoreId as scrutinizedDxfFileId,rofile.fileStoreId as reportOutputId,pdfile.fileStoreId as planDetailFileStore,appln.applicationDate,appln.applicationNumber,appln.applicationType,appln.serviceType,appln.planPermitNumber,appln.permitApplicationDate from ")
					.append(value.getKey()).append(".edcr_reg_application appln, ").append(value.getKey())
					.append(".edcr_reg_application_detail dtl, ").append(value.getKey()).append(".eg_filestoremap dxf, ")
					.append(value.getKey()).append(".eg_filestoremap scrudxf, ").append(value.getKey())
					.append(".eg_filestoremap rofile, ").append(value.getKey()).append(".eg_filestoremap pdfile ")
					.append("where appln.id = dtl.application and dtl.dxfFileId=dxf.id and dtl.scrutinizedDxfFileId=scrudxf.id and dtl.reportOutputId=rofile.id and dtl.planDetailFileStore=pdfile.id ");

			if (isNotBlank(edcrRequest.getEdcrNumber())) {
				queryStr.append("and dtl.dcrNumber=:dcrNumber ");
				params.put("dcrNumber", edcrRequest.getEdcrNumber());
			}

			if (isNotBlank(edcrRequest.getTransactionNumber())) {
				queryStr.append("and appln.transactionNumber=:transactionNumber ");
				params.put("transactionNumber", edcrRequest.getTransactionNumber());
			}

			if (isNotBlank(edcrRequest.getApplicationNumber())) {
				queryStr.append("and appln.applicationNumber=:applicationNumber ");
				params.put("applicationNumber", edcrRequest.getApplicationNumber());
			}

			if ((onlyTenantId || isStakeholder) && userInfo != null && isNotBlank(userId)) {
				queryStr.append("and appln.thirdPartyUserCode=:thirdPartyUserCode ");
				params.put("thirdPartyUserCode", userId);
			}

			String appliactionType = edcrRequest.getAppliactionType();
			if (isNotBlank(appliactionType)) {
				RegApplicationType applicationType = null;
				if ("REGULARISATION".equalsIgnoreCase(appliactionType)) {
					applicationType = RegApplicationType.PERMIT;
				} else if ("BUILDING_OC_PLAN_SCRUTINY".equalsIgnoreCase(appliactionType)) {
					applicationType = RegApplicationType.OCCUPANCY_CERTIFICATE;
				} else if ("Occupancy certificate".equalsIgnoreCase(appliactionType)) {
					applicationType = RegApplicationType.OCCUPANCY_CERTIFICATE;
				} else {
					applicationType = RegApplicationType.PERMIT;
				}
				queryStr.append("and appln.applicationType=:applicationtype ");
				params.put("applicationtype", applicationType.toString());
			}

			if (isNotBlank(edcrRequest.getApplicationSubType())) {
				queryStr.append("and appln.serviceType=:servicetype ");
				params.put("servicetype", edcrRequest.getApplicationSubType());
			}

			if (isNotBlank(edcrRequest.getStatus())) {
				queryStr.append("and dtl.status=:status ");
				params.put("status", edcrRequest.getStatus());
			}

			if (edcrRequest.getFromDate() != null) {
				queryStr.append("and appln.applicationDate>=to_timestamp(:fromDate, 'yyyy-MM-dd')");
				params.put("fromDate", sf.format(resetFromDateTimeStamp(edcrRequest.getFromDate())));
			}

			if (edcrRequest.getToDate() != null) {
				queryStr.append("and appln.applicationDate<=to_timestamp(:toDate ,'yyyy-MM-dd')");
				params.put("toDate", sf.format(resetToDateTimeStamp(edcrRequest.getToDate())));
			}
			String orderBy = "desc";
			if (isNotBlank(edcrRequest.getOrderBy()))
				orderBy = edcrRequest.getOrderBy();
			if (orderBy.equalsIgnoreCase("asc"))
				queryStr.append(" order by appln.createddate asc)");
			else
				queryStr.append(" order by appln.createddate desc)");
			if (tenantItr.hasNext()) {
				queryStr.append(" union ");
			}
		}
		String query;
		String orderBy = "desc";
		if (isNotBlank(edcrRequest.getOrderBy()))
			orderBy = edcrRequest.getOrderBy();
		if (orderBy.equalsIgnoreCase("asc"))
			query = orderByWrapperAsc.replace("{}", queryStr);
		else
			query = orderByWrapperDesc.replace("{}", queryStr);
		return query;
	}
	
	
	private Criteria getCriteriaofSingleTenant(final RegEdcrRequest edcrRequest, UserInfo userInfo, String userId,
			boolean onlyTenantId, boolean isStakeholder) {
		final Criteria criteria = getCurrentSession().createCriteria(RegEdcrApplicationDetail.class,
				"edcrApplicationDetail");
		criteria.createAlias("edcrApplicationDetail.application", "application");
		if (edcrRequest != null && isNotBlank(edcrRequest.getEdcrNumber())) {
			criteria.add(Restrictions.eq("edcrApplicationDetail.dcrNumber", edcrRequest.getEdcrNumber()));
		}
		if (edcrRequest != null && isNotBlank(edcrRequest.getTransactionNumber())) {
			criteria.add(Restrictions.eq("application.transactionNumber", edcrRequest.getTransactionNumber()));
		}
		if (edcrRequest != null && isNotBlank(edcrRequest.getApplicationNumber())) {
			criteria.add(Restrictions.eq("application.applicationNumber", edcrRequest.getApplicationNumber()));
		}

		String appliactionType = edcrRequest.getAppliactionType();

		if (edcrRequest != null && isNotBlank(appliactionType)) {
			RegApplicationType applicationType = null;
			if ("REGULARISATION".equalsIgnoreCase(appliactionType)) {
				applicationType = RegApplicationType.PERMIT;
			} else if ("BUILDING_OC_PLAN_SCRUTINY".equalsIgnoreCase(appliactionType)) {
				applicationType = RegApplicationType.OCCUPANCY_CERTIFICATE;
			}
			if ("Permit".equalsIgnoreCase(appliactionType)) {
				applicationType = RegApplicationType.PERMIT;
			} else if ("Occupancy certificate".equalsIgnoreCase(appliactionType)) {
				applicationType = RegApplicationType.OCCUPANCY_CERTIFICATE;
			}
			criteria.add(Restrictions.eq("application.applicationType", applicationType));
		}

		if (edcrRequest != null && isNotBlank(edcrRequest.getApplicationSubType())) {
			criteria.add(Restrictions.eq("application.serviceType", edcrRequest.getApplicationSubType()));
		}

		if ((onlyTenantId || isStakeholder) && userInfo != null && isNotBlank(userId)) {
			criteria.add(Restrictions.eq("application.thirdPartyUserCode", userId));
		}

		if (isNotBlank(edcrRequest.getStatus()))
			criteria.add(Restrictions.eq("edcrApplicationDetail.status", edcrRequest.getStatus()));
		if (edcrRequest.getFromDate() != null)
			criteria.add(Restrictions.ge("application.applicationDate", edcrRequest.getFromDate()));
		if (edcrRequest.getToDate() != null)
			criteria.add(Restrictions.le("application.applicationDate", edcrRequest.getToDate()));
		String orderBy = "desc";
		if (isNotBlank(edcrRequest.getOrderBy()))
			orderBy = edcrRequest.getOrderBy();
		if (orderBy.equalsIgnoreCase("asc"))
			criteria.addOrder(Order.asc("edcrApplicationDetail.createdDate"));
		else
			criteria.addOrder(Order.desc("edcrApplicationDetail.createdDate"));

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		return criteria;
	}
	
	public RegEdcrDetail setEdcrResponseForAcrossTenants(Object[] applnDtls, String stateCityCode) {
		RegEdcrDetail edcrDetail = new RegEdcrDetail();
		edcrDetail.setTransactionNumber(String.valueOf(applnDtls[1]));
		edcrDetail.setEdcrNumber(String.valueOf(applnDtls[2]));
		edcrDetail.setStatus(String.valueOf(applnDtls[3]));
		edcrDetail.setApplicationDate(new LocalDate(String.valueOf(applnDtls[9])).toDate());
		edcrDetail.setApplicationNumber(String.valueOf(applnDtls[10]));
		String applicationType = String.valueOf(applnDtls[11]);
		if (applicationType != null) {
			if (RegApplicationType.PERMIT.getApplicationTypeVal()
					.equalsIgnoreCase(RegApplicationType.valueOf(applicationType).getApplicationTypeVal())) {
				edcrDetail.setAppliactionType("REGULARISATION");
			} else if (RegApplicationType.OCCUPANCY_CERTIFICATE.getApplicationTypeVal()
					.equalsIgnoreCase(RegApplicationType.valueOf(applicationType).getApplicationTypeVal())) {
				edcrDetail.setAppliactionType("BUILDING_OC_PLAN_SCRUTINY");
			} else {
				edcrDetail.setAppliactionType(RegApplicationType.valueOf(applicationType).getApplicationTypeVal());
			}

		}
		edcrDetail.setApplicationSubType(String.valueOf(applnDtls[12]));
		edcrDetail.setPermitNumber(String.valueOf(applnDtls[13]));
		String tenantId = String.valueOf(applnDtls[0]);
		if (applnDtls[14] != null)
			edcrDetail.setPermitDate(new LocalDate(String.valueOf(applnDtls[14])).toDate());

		if (String.valueOf(applnDtls[5]) != null)
			edcrDetail.setDxfFile(format(getFileDownloadUrl(String.valueOf(applnDtls[5]), tenantId)));

		if (String.valueOf(applnDtls[6]) != null)
			edcrDetail.setUpdatedDxfFile(format(getFileDownloadUrl(String.valueOf(applnDtls[6]), tenantId)));

		if (String.valueOf(applnDtls[7]) != null)
			edcrDetail.setPlanReport(format(getFileDownloadUrl(String.valueOf(applnDtls[7]), tenantId)));
		File file = null;
		try {
			file = String.valueOf(applnDtls[8]) != null
					? fileStoreService.fetch(String.valueOf(applnDtls[8]), DcrConstants.APPLICATION_MODULE_TYPE,
							tenantId)
					: null;
		} catch (ApplicationRuntimeException e) {
			LOG.error("Error occurred, while fetching plan details!!!", e);
		}

		if (LOG.isInfoEnabled())
			LOG.info("**************** End - Reading Plan detail file **************" + file);
		try {
			if (file == null) {
				Plan pl1 = new Plan();
				PlanInformation pi = new PlanInformation();
				pi.setApplicantName(String.valueOf(applnDtls[4]));
				pl1.setPlanInformation(pi);
				edcrDetail.setPlanDetail(pl1);
			} else {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				Plan pl1 = mapper.readValue(file, Plan.class);
				pl1.getPlanInformation().setApplicantName(String.valueOf(applnDtls[4]));
				if (LOG.isInfoEnabled())
					LOG.info("**************** Plan detail object **************" + pl1);
				edcrDetail.setPlanDetail(pl1);
			}
		} catch (IOException e) {
			LOG.log(Level.ERROR, e);
		}

		edcrDetail.setTenantId(stateCityCode.concat(".").concat(tenantId));

		if (!String.valueOf(applnDtls[3]).equalsIgnoreCase("Accepted"))
			edcrDetail.setStatus(String.valueOf(applnDtls[3]));

		return edcrDetail;
	}
	
	@Transactional
	public List<RegEdcrDetail> edcrDetailsResponse(List<RegEdcrApplicationDetail> edcrApplications, RegEdcrRequest edcrRequest) {
		List<RegEdcrDetail> edcrDetails = new ArrayList<>();
		for (RegEdcrApplicationDetail edcrApp : edcrApplications)
			edcrDetails.add(setEdcrResponse(edcrApp, edcrRequest));

		return edcrDetails;
	}
	
	public Date resetFromDateTimeStamp(final Date date) {
		final Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		return cal1.getTime();
	}

	public Date resetToDateTimeStamp(final Date date) {
		final Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		cal1.set(Calendar.HOUR_OF_DAY, 23);
		cal1.set(Calendar.MINUTE, 59);
		cal1.set(Calendar.SECOND, 59);
		cal1.set(Calendar.MILLISECOND, 999);
		return cal1.getTime();
	}

}
