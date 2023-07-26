package org.entit.rga.validator;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.service.EDCRService;
import org.entit.rga.util.RGAConstants;
import org.entit.rga.util.RGAErrorConstants;
import org.entit.rga.util.RGAUtil;
import org.entit.rga.web.model.Document;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGASearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RGAValidator {

	@Autowired
	private MDMSValidator mdmsValidator;

	@Autowired
	private RGAConfiguration config;
	
	@Autowired
	private EDCRService edcrService;

	@Autowired
	private RGAUtil bpaUtil;
	
//	@Autowired
//	private NocService nocService;
	
	public void validateCreate(RGARequest rGARequest, Object mdmsData, Map<String, String> values) {
		mdmsValidator.validateMdmsData(rGARequest, mdmsData);
		validateApplicationDocuments(rGARequest, mdmsData, null, values);
	}


	/**
	 * Validates the application documents of the BPA comparing the document types configured in the mdms
	 * @param request
	 * @param mdmsData
	 * @param currentState
	 * @param values
	 */
	private void validateApplicationDocuments(RGARequest request, Object mdmsData, String currentState, Map<String, String> values) {
		Map<String, List<String>> masterData = mdmsValidator.getAttributeValues(mdmsData);
		RGA rGA = request.getRegularisation();

		if (!rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_REJECT)
				&& !rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_ADHOC)
				&& !rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_PAY)) {

			String applicationType = values.get(RGAConstants.APPLICATIONTYPE);
			String serviceType = values.get(RGAConstants.SERVICETYPE);
			
			String filterExp = "$.[?(@.applicationType=='" + applicationType + "' && @.ServiceType=='"
					+ serviceType + "' && @.RiskType=='" + rGA.getRiskType() + "' && @.WFState=='"
					+ currentState + "')].docTypes";
			
			List<Object> docTypeMappings = JsonPath.read(masterData.get(RGAConstants.DOCUMENT_TYPE_MAPPING), filterExp);

			List<Document> allDocuments = new ArrayList<Document>();
			if (rGA.getDocuments() != null) {
				allDocuments.addAll(rGA.getDocuments());
			}

			if (CollectionUtils.isEmpty(docTypeMappings)) {
				return;
			}

			filterExp = "$.[?(@.required==true)].code";
			List<String> requiredDocTypes = JsonPath.read(docTypeMappings.get(0), filterExp);

			List<String> validDocumentTypes = masterData.get(RGAConstants.DOCUMENT_TYPE);

			if (!CollectionUtils.isEmpty(allDocuments)) {

				allDocuments.forEach(document -> {

					if (!validDocumentTypes.contains(document.getDocumentType())) {
						throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DOCUMENTTYPE,
								document.getDocumentType() + " is Unkown");
					}
				});

				if (requiredDocTypes.size() > 0 && allDocuments.size() < requiredDocTypes.size()) {

					throw new CustomException(RGAErrorConstants.BPA_MDNADATORY_DOCUMENTPYE_MISSING,
							RGAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
				} else if (requiredDocTypes.size() > 0) {

					List<String> addedDocTypes = new ArrayList<String>();
					allDocuments.forEach(document -> {

						String docType = document.getDocumentType();
						int lastIndex = docType.lastIndexOf(".");
						String documentNs = "";
						if (lastIndex > 1) {
							documentNs = docType.substring(0, lastIndex);
						} else if (lastIndex == 1) {
							throw new CustomException(RGAErrorConstants.BPA_INVALID_DOCUMENTTYPE,
									document.getDocumentType() + " is Invalid");
						} else {
							documentNs = docType;
						}

						addedDocTypes.add(documentNs);
					});
					requiredDocTypes.forEach(docType -> {
						String docType1 = docType.toString();
						if (!addedDocTypes.contains(docType1)) {
							throw new CustomException(RGAErrorConstants.BPA_MDNADATORY_DOCUMENTPYE_MISSING,
									"Document Type " + docType1 + " is Missing");
						}
					});
				}
			} else if (requiredDocTypes.size() > 0) {
				throw new CustomException(RGAErrorConstants.BPA_MDNADATORY_DOCUMENTPYE_MISSING,
						"Atleast " + requiredDocTypes.size() + " Documents are requied ");
			}
			rGA.setDocuments(allDocuments);
		}

	}

	/** 
	 * validate duplicates documents in the bpa request
	 * @param request
	 */
	private void validateDuplicateDocuments(RGARequest request) {
		if (request.getRegularisation().getDocuments() != null) {
			List<String> documentFileStoreIds = new LinkedList<String>();
			request.getRegularisation().getDocuments().forEach(document -> {
				if (documentFileStoreIds.contains(document.getFileStoreId()))
					throw new CustomException(RGAErrorConstants.BPA_DUPLICATE_DOCUMENT, "Same document cannot be used multiple times");
				else
					documentFileStoreIds.add(document.getFileStoreId());
			});
		}
	}

	/**
	 * Validates if the search parameters are valid
	 * 
	 * @param requestInfo
	 *            The requestInfo of the incoming request
	 * @param criteria
	 *            The BPASearch Criteria
	 */
//TODO need to make the changes in the data
	public void validateSearch(RequestInfo requestInfo, RGASearchCriteria criteria) {
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(RGAConstants.CITIZEN) && criteria.isEmpty())
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search without any paramters is not allowed");

		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(RGAConstants.CITIZEN) && !criteria.tenantIdOnly()
				&& criteria.getTenantId() == null)
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(RGAConstants.CITIZEN) && !criteria.isEmpty()
				&& !criteria.tenantIdOnly() && criteria.getTenantId() == null)
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		String allowedParamStr = null;

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(RGAConstants.CITIZEN))
			allowedParamStr = config.getAllowedCitizenSearchParameters();
		else if (requestInfo.getUserInfo().getType().equalsIgnoreCase(RGAConstants.EMPLOYEE))
			allowedParamStr = config.getAllowedEmployeeSearchParameters();
		else
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH,
					"The userType: " + requestInfo.getUserInfo().getType() + " does not have any search config");

		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
			validateSearchParams(criteria, allowedParams);
		}
	}

	/**
	 * Validates if the paramters coming in search are allowed
	 * 
	 * @param criteria
	 *            BPA search criteria
	 * @param allowedParams
	 *            Allowed Params for search
	 */
	private void validateSearchParams(RGASearchCriteria criteria, List<String> allowedParams) {

		if (criteria.getApplicationNo() != null && !allowedParams.contains("applicationNo"))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search on applicationNo is not allowed");

		if (criteria.getEdcrNumber() != null && !allowedParams.contains("edcrNumber"))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search on edcrNumber is not allowed");

		if (criteria.getStatus() != null && !allowedParams.contains("status"))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search on Status is not allowed");

		if (criteria.getIds() != null && !allowedParams.contains("ids"))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search on ids is not allowed");

		if (criteria.getMobileNumber() != null && !allowedParams.contains("mobileNumber"))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search on mobileNumber is not allowed");

		if (criteria.getOffset() != null && !allowedParams.contains("offset"))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search on offset is not allowed");

		if (criteria.getLimit() != null && !allowedParams.contains("limit"))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Search on limit is not allowed");
		
		if (criteria.getApprovalDate() != null && (criteria.getApprovalDate() > new Date().getTime()))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "Permit Order Genarated date cannot be a future date");
		
		if (criteria.getFromDate() != null && (criteria.getFromDate() > new Date().getTime()))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "From date cannot be a future date");

		if (criteria.getToDate() != null && criteria.getFromDate() != null
				&& (criteria.getFromDate() > criteria.getToDate()))
			throw new CustomException(RGAErrorConstants.INVALID_SEARCH, "To date cannot be prior to from date");
	}

	/**
	 * valide the update BPARequest
	 * @param rGARequest
	 * @param searchResult
	 * @param mdmsData
	 * @param currentState
	 * @param edcrResponse
	 */
	public void validateUpdate(RGARequest rGARequest, List<RGA> searchResult, Object mdmsData, String currentState, Map<String, String> edcrResponse) {

		RGA rGA = rGARequest.getRegularisation();
		validateApplicationDocuments(rGARequest, mdmsData, currentState, edcrResponse);
		validateAllIds(searchResult, rGA);
		mdmsValidator.validateMdmsData(rGARequest, mdmsData);
		validateDuplicateDocuments(rGARequest);
		setFieldsFromSearch(rGARequest, searchResult, mdmsData);

	}

	/**
	 * set the fields from search response to the bpaRequest for furhter processing
	 * @param rGARequest
	 * @param searchResult
	 * @param mdmsData
	 */
	private void setFieldsFromSearch(RGARequest rGARequest, List<RGA> searchResult, Object mdmsData) {
		Map<String, RGA> idToBPAFromSearch = new HashMap<>();

		searchResult.forEach(bpa -> {
			idToBPAFromSearch.put(bpa.getId(), bpa);
		});

		rGARequest.getRegularisation().getAuditDetails()
				.setCreatedBy(idToBPAFromSearch.get(rGARequest.getRegularisation().getId()).getAuditDetails().getCreatedBy());
		rGARequest.getRegularisation().getAuditDetails()
				.setCreatedTime(idToBPAFromSearch.get(rGARequest.getRegularisation().getId()).getAuditDetails().getCreatedTime());
		rGARequest.getRegularisation().setStatus(idToBPAFromSearch.get(rGARequest.getRegularisation().getId()).getStatus());
	}



	/**
	 * Validate the ids of the search results
	 * @param searchResult
	 * @param rGA
	 */
	private void validateAllIds(List<RGA> searchResult, RGA rGA) {

		Map<String, RGA> idToBPAFromSearch = new HashMap<>();
		searchResult.forEach(bpas -> {
			idToBPAFromSearch.put(bpas.getId(), bpas);
		});

		Map<String, String> errorMap = new HashMap<>();
		RGA searchedRegularisation = idToBPAFromSearch.get(rGA.getId());

		if (!searchedRegularisation.getApplicationNo().equalsIgnoreCase(rGA.getApplicationNo()))
			errorMap.put("INVALID UPDATE", "The application number from search: " + searchedRegularisation.getApplicationNo()
					+ " and from update: " + rGA.getApplicationNo() + " does not match");

		if (!searchedRegularisation.getId().equalsIgnoreCase(rGA.getId()))
			errorMap.put("INVALID UPDATE", "The id " + rGA.getId() + " does not exist");




		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}




	/**
	 * validate the fields inspection checlist data populated by the user against the mdms
	 * @param mdmsData
	 * @param rGARequest
	 * @param wfState
	 */
	public void validateCheckList(Object mdmsData, RGARequest rGARequest, String wfState) {
		RGA rGA = rGARequest.getRegularisation();
		Map<String, String> edcrResponse = edcrService.getEDCRDetails(rGARequest.getRequestInfo(), rGARequest.getRegularisation());
		log.debug("applicationType is " + edcrResponse.get(RGAConstants.APPLICATIONTYPE));
        log.debug("serviceType is " + edcrResponse.get(RGAConstants.SERVICETYPE));
        
		validateQuestions(mdmsData, rGA, wfState, edcrResponse);
		validateFIDocTypes(mdmsData, rGA, wfState, edcrResponse);
	}

	/**
	 * validate the fields insepction report questions agains the MDMS
	 * @param mdmsData
	 * @param rGA
	 * @param wfState
	 * @param edcrResponse
	 */
	@SuppressWarnings(value = { "unchecked", "rawtypes" })
	private void validateQuestions(Object mdmsData, RGA rGA, String wfState, Map<String, String> edcrResponse) {
		List<String> mdmsQns = null;

		log.debug("Fetching MDMS result for the state " + wfState);

		try {
			String questionsPath = RGAConstants.QUESTIONS_MAP.replace("{1}", wfState)
					.replace("{2}", rGA.getRiskType().toString()).replace("{3}", edcrResponse.get(RGAConstants.SERVICETYPE))
					.replace("{4}", edcrResponse.get(RGAConstants.APPLICATIONTYPE));

			List<Object> mdmsQuestionsArray = (List<Object>) JsonPath.read(mdmsData, questionsPath);

			if (!CollectionUtils.isEmpty(mdmsQuestionsArray))
				mdmsQns = JsonPath.read(mdmsQuestionsArray.get(0), RGAConstants.QUESTIONS_PATH);

			log.debug("MDMS questions " + mdmsQns);
			if (!CollectionUtils.isEmpty(mdmsQns)) {
				if (rGA.getAdditionalDetails() != null) {
					List checkListFromReq = (List) ((Map) rGA.getAdditionalDetails()).get(wfState.toLowerCase());
					if (!CollectionUtils.isEmpty(checkListFromReq)) {
						for (int i = 0; i < checkListFromReq.size(); i++) {
							// MultiItem framework adding isDeleted object to
							// additionDetails object whenever report is being
							// removed.
							// So skipping that object validation.
							if (((Map) checkListFromReq.get(i)).containsKey("isDeleted")) {
								checkListFromReq.remove(i);
								i--;
								continue;
							}
							List<Map> requestCheckList = new ArrayList<Map>();
							List<String> requestQns = new ArrayList<String>();
							validateDateTime((Map)checkListFromReq.get(i));
							List<Map> questions = ((Map) checkListFromReq.get(i))
									.get(RGAConstants.QUESTIONS_TYPE) != null
											? (List<Map>) ((Map) checkListFromReq.get(i))
													.get(RGAConstants.QUESTIONS_TYPE)
											: null;
							if (questions != null)
								requestCheckList.addAll(questions);
							if (!CollectionUtils.isEmpty(requestCheckList)) {
								for (Map reqQn : requestCheckList) {
									requestQns.add((String) reqQn.get(RGAConstants.QUESTION_TYPE));
								}
							}

							log.debug("Request questions " + requestQns);

							if (!CollectionUtils.isEmpty(requestQns)) {
								if (requestQns.size() < mdmsQns.size())
									throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_QUESTIONS,
											RGAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
								else {
									List<String> pendingQns = new ArrayList<String>();
									for (String qn : mdmsQns) {
										if (!requestQns.contains(qn)) {
											pendingQns.add(qn);
										}
									}
									if (pendingQns.size() > 0) {
										throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_QUESTIONS,
												RGAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
									}
								}
							} else {
								throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_QUESTIONS,
										RGAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
							}
						}
					} else {
						throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_QUESTIONS, RGAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
					}
				} else {
					throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_QUESTIONS, RGAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
				}
			}
		} catch (PathNotFoundException ex) {
			log.error("Exception occured while validating the Checklist Questions" + ex.getMessage());
		}
	}

	/**
	 * Validate fieldinspection documents and their documentTypes
	 * @param mdmsData
	 * @param rGA
	 * @param wfState
	 * @param edcrResponse
	 */
	@SuppressWarnings(value = { "unchecked", "rawtypes" })
	private void validateFIDocTypes(Object mdmsData, RGA rGA, String wfState, Map<String, String> edcrResponse) {
		List<String> mdmsDocs = null;

		log.debug("Fetching MDMS result for the state " + wfState);

		try {
			String docTypesPath = RGAConstants.DOCTYPES_MAP.replace("{1}", wfState)
					.replace("{2}", rGA.getRiskType().toString()).replace("{3}", edcrResponse.get(RGAConstants.SERVICETYPE))
					.replace("{4}", edcrResponse.get(RGAConstants.APPLICATIONTYPE));;

			List<Object> docTypesArray = (List<Object>) JsonPath.read(mdmsData, docTypesPath);

			if (!CollectionUtils.isEmpty(docTypesArray))
				mdmsDocs = JsonPath.read(docTypesArray.get(0), RGAConstants.DOCTYPESS_PATH);

			log.debug("MDMS DocTypes " + mdmsDocs);
			if (!CollectionUtils.isEmpty(mdmsDocs)) {
				if (rGA.getAdditionalDetails() != null) {
					List checkListFromReq = (List) ((Map) rGA.getAdditionalDetails()).get(wfState.toLowerCase());
					if (!CollectionUtils.isEmpty(checkListFromReq)) {
						for (int i = 0; i < checkListFromReq.size(); i++) {
							List<Map> requestCheckList = new ArrayList<Map>();
							List<String> requestDocs = new ArrayList<String>();
							List<Map> docs = ((Map) checkListFromReq.get(i)).get(RGAConstants.DOCS) != null
									? (List<Map>) ((Map) checkListFromReq.get(i)).get(RGAConstants.DOCS) : null;
							if (docs != null)
								requestCheckList.addAll(docs);
							
							if (!CollectionUtils.isEmpty(requestCheckList)) {
								for (Map reqDoc : requestCheckList) {
									String fileStoreId = ((String) reqDoc.get(RGAConstants.FILESTOREID));
									if (!StringUtils.isEmpty(fileStoreId)) {
										String docType = (String) reqDoc.get(RGAConstants.CODE);
										int lastIndex = docType.lastIndexOf(".");
										String documentNs = "";
										if (lastIndex > 1) {
											documentNs = docType.substring(0, lastIndex);
										} else if (lastIndex == 1) {
											throw new CustomException(RGAErrorConstants.BPA_INVALID_DOCUMENTTYPE,
													(String) reqDoc.get(RGAConstants.CODE) + " is Invalid");
										} else {
											documentNs = docType;
										}
										requestDocs.add(documentNs);
									} else {
										throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DOCS,
												RGAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
									}
								}
							}

							log.debug("Request Docs " + requestDocs);

							if (!CollectionUtils.isEmpty(requestDocs)) {
								if (requestDocs.size() < mdmsDocs.size())
									throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DOCS,
											RGAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
								else {
									List<String> pendingDocs = new ArrayList<String>();
									for (String doc : mdmsDocs) {
										if (!requestDocs.contains(doc)) {
											pendingDocs.add(doc);
										}
									}
									if (pendingDocs.size() > 0) {
										throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DOCS,
												RGAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
									}
								}
							} else {
								throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DOCS, RGAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
							}
						}
					} else {
						throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DOCS, RGAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
					}
				} else {
					throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DOCS, RGAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
				}
			}
		} catch (PathNotFoundException ex) {
			log.error("Exception occured while validating the Checklist Documents" + ex.getMessage());
		}
	}
	
	/**
	 * Validate FieldINpsection report date and time
	 * @param checkListFromRequest
	 */
	private void validateDateTime(@SuppressWarnings("rawtypes") Map checkListFromRequest) {

		if (checkListFromRequest.get(RGAConstants.INSPECTION_DATE) == null
				|| StringUtils.isEmpty(checkListFromRequest.get(RGAConstants.INSPECTION_DATE).toString())) {
			throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DATE, "Please mention the inspection date");
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dt;
			try {
				dt = sdf.parse(checkListFromRequest.get(RGAConstants.INSPECTION_DATE).toString());
				long inspectionEpoch = dt.getTime();
				if (inspectionEpoch > new Date().getTime()) {
					throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DATE, "Inspection date cannot be a future date");
				} else if (inspectionEpoch < 0) {
					throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DATE, "Provide the date in specified format 'yyyy-MM-dd'");
				}
			} catch (ParseException e) {
				throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_DATE, "Unable to parase the inspection date");
			}
		}
		if (checkListFromRequest.get(RGAConstants.INSPECTION_TIME) == null
				|| StringUtils.isEmpty(checkListFromRequest.get(RGAConstants.INSPECTION_TIME).toString())) {
			throw new CustomException(RGAErrorConstants.BPA_UNKNOWN_TIME, "Please mention the inspection time");
		}
	}

	/**
	 * validate the workflow and the nocapproval stages to move forward
	 * @param rGARequest
	 * @param mdmsRes
	 */
	public void validatePreEnrichData(RGARequest rGARequest, Object mdmsRes) {		
		validateSkipPaymentAction(rGARequest);
		validateNocApprove(rGARequest, mdmsRes);
	}
	/**
	 * Validate workflowActions against the skipPayment 
	 * @param rGARequest
	 */
	private void validateSkipPaymentAction(RGARequest rGARequest) {
		RGA rGA = rGARequest.getRegularisation();
		if (rGA.getWorkflow().getAction() != null && (rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_SKIP_PAY))) {
			BigDecimal demandAmount = bpaUtil.getDemandAmount(rGARequest);
			if ((demandAmount.compareTo(BigDecimal.ZERO) > 0)) {
				throw new CustomException(RGAErrorConstants.BPA_INVALID_ACTION, "Payment can't be skipped once demand is generated.");
			}
		}
	}
	
	/**
	 * Validates the NOC approval state to move forward the bpa applicaiton
	 * @param rGARequest
	 * @param mdmsRes
	 */
	@SuppressWarnings("unchecked")
	private void validateNocApprove(RGARequest rGARequest, Object mdmsRes) {
		RGA rGA = rGARequest.getRegularisation();
		log.debug("===========> valdiateNocApprove method called");
		if (config.getValidateRequiredNoc()) {
			if (rGA.getStatus().equalsIgnoreCase(RGAConstants.NOCVERIFICATION_STATUS)
					&& rGA.getWorkflow().getAction().equalsIgnoreCase(RGAConstants.ACTION_FORWORD)) {
				Map<String, String> edcrResponse = edcrService.getEDCRDetails(rGARequest.getRequestInfo(),
						rGARequest.getRegularisation());
				log.debug("===========> valdiateNocApprove method called, application is in noc verification pending");
				String riskType = "ALL";
				if (StringUtils.isEmpty(rGA.getRiskType()) || rGA.getRiskType().equalsIgnoreCase("LOW")) {
					riskType = rGA.getRiskType();
				}
				log.debug("fetching NocTypeMapping record having riskType : " + riskType);

//				String nocPath = RegularisationConstants.NOCTYPE_REQUIRED_MAP
//						.replace("{1}", edcrResponse.get(RegularisationConstants.APPLICATIONTYPE))
//						.replace("{2}", edcrResponse.get(RegularisationConstants.SERVICETYPE)).replace("{3}", riskType);

//				List<Object> nocMappingResponse = (List<Object>) JsonPath.read(mdmsRes, nocPath);
//				List<String> nocTypes = JsonPath.read(nocMappingResponse, "$..type");

//				log.debug("===========> valdiateNocApprove method called, noctypes====",nocTypes);
				/*
				 * List<Noc> nocs = nocService.fetchNocRecords(regularisationRequest); if
				 * (!CollectionUtils.isEmpty(nocs)) { for (Noc noc : nocs) { if
				 * (!nocTypes.isEmpty() && nocTypes.contains(noc.getNocType())) { List<String>
				 * statuses = Arrays.asList(config.getNocValidationCheckStatuses().split(","));
				 * if(!statuses.contains(noc.getApplicationStatus())) {
				 * log.error("Noc is not approved having applicationNo :" +
				 * noc.getApplicationNo()); throw new
				 * CustomException(RegularisationErrorConstants.NOC_SERVICE_EXCEPTION,
				 * " Application can't be forwarded without NOC " + StringUtils.join(statuses,
				 * " or ")); } } } } else {
				 * log.debug("No NOC record found to validate with sourceRefId " +
				 * regularisation.getApplicationNo()); }
				 */
			}
		}
	}
}
