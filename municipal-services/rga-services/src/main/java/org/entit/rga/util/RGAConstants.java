package org.entit.rga.util;

import org.springframework.stereotype.Component;

@Component
public class RGAConstants {

	// MDMS

	public static final String RGA_MODULE = "RGA";

	public static final String RGA_BusinessService = "RGA";

	public static final String RGA_MODULE_CODE = "RGA";

	public static final String BPA_LOW_MODULE_CODE = "BPA_LOW";

	public static final String COMMON_MASTERS_MODULE = "common-masters";

	public static final String NOTIFICATION_LOCALE = "en_IN";

	public static final String NOTIFICATION_INITIATED = "tl.en.counter.initiate";

	public static final String NOTIFICATION_PANDING_APPL_FEE = "tl.en.counter.appl.fee";

	public static final String NOTIFICATION_APPLIED = "tl.en.counter.submit";

	public static final String NOTIFICATION_DOCUMENT_VERIFICATION = "rga.en.document";

	public static final String NOTIFICATION_FIELD_INSPECTION = "rga.en.field.inspection";

	public static final String NOTIFICATION_NOC_UPDATION = "rga.en.field.inspection";

	public static final String NOTIFICATION_PAYMENT_OWNER = "tl.en.counter.payment.successful.owner";

	public static final String NOTIFICATION_PAYMENT_PAYER = "rga.en.counter.payment.successful.payer";

	public static final String NOTIFICATION_PAID = "rga.en.counter.pending.approval";

	public static final String NOTIFICATION_APPROVED = "rga.en.counter.approved";

	public static final String NOTIFICATION_REJECTED = "rga.en.counter.rejected";

	public static final String NOTIFICATION_CANCELLED = "rga.en.counter.cancelled";

	public static final String NOTIFICATION_FIELD_CHANGED = "rga.en.edit.field.change";

	public static final String NOTIFICATION_OBJECT_ADDED = "rga.en.edit.object.added";

	public static final String NOTIFICATION_OBJECT_REMOVED = "rga.en.edit.object.removed";

	public static final String NOTIFICATION_OBJECT_MODIFIED = "rga.en.edit.object.modified";

	public static final String DEFAULT_OBJECT_MODIFIED_MSG = "Dear <1>,Your Building Plan with application number <APPLICATION_NUMBER> was modified.";

	// mdms path codes

	public static final String RGA_JSONPATH_CODE = "$.MdmsRes.RGA";

	public static final String COMMON_MASTER_JSONPATH_CODE = "$.MdmsRes.common-masters";

	// error constants

//	public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";
//
//	public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";

	// mdms master names

	public static final String SERVICE_TYPE = "ServiceType";

	public static final String APPLICATION_TYPE = "ApplicationType";

	public static final String OCCUPANCY_TYPE = "OccupancyType";

	public static final String SUB_OCCUPANCY_TYPE = "SubOccupancyType";

	public static final String USAGES = "Usages";

	public static final String CalculationType = "CalculationType";

	public static final String DOCUMENT_TYPE_MAPPING = "DocTypeMapping";

	public static final String RISKTYPE_COMPUTATION = "RiskTypeComputation";

	public static final String DOCUMENT_TYPE = "DocumentType";

	public static final String OWNER_TYPE = "OwnerType";

	public static final String OWNERSHIP_CATEGORY = "OwnerShipCategory";

	public static final String CHECKLIST_NAME = "CheckList";

	public static final String NOC_TYPE_MAPPING = "NocTypeMapping";

	// FINANCIAL YEAR

	public static final String MDMS_EGF_MASTER = "egf-master";

	public static final String MDMS_FINANCIALYEAR = "FinancialYear";

	public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

	public static final String MDMS_STARTDATE = "startingDate";

	public static final String MDMS_ENDDATE = "endingDate";

	// BPA actions

	public static final String ACTION_INITIATE = "INITIATE";

	public static final String ACTION_APPLY = "APPLY";

	public static final String ACTION_APPROVE = "APPROVE";

	public static final String ACTION_FORWORD = "FORWARD";

	public static final String ACTION_MARK = "MARK";

	public static final String ACTION_SENDBACK = "SENDBACK";

	public static final String ACTION_DOC_VERIFICATION_FORWARD = "DOC_VERIFICATION_FORWARD";

	public static final String ACTION_FIELDINSPECTION_FORWARD = "FIELDINSPECTION_FORWARD";

	public static final String ACTION_NOC_FORWARD = "NOC_FORWARD";

	public static final String ACTION_PENDINGAPPROVAL = "PENDINGAPPROVAL";

	public static final String ACTION_REJECT = "REJECT";
	public static final String ACTION_REVOCATE = "REVOCATE";

	public static final String ACTION_CANCEL = "CANCEL";

	public static final String ACTION_PAY = "PAY";
	
	public static final String ACTION_SKIP_PAY = "SKIP_PAYMENT";

	public static final String ACTION_ADHOC = "ADHOC";
	
	public static final String ACTION_SEND_TO_ARCHITECT = "SEND_TO_ARCHITECT";
	
	public static final String ACTION_SEND_TO_CITIZEN = "SEND_TO_CITIZEN";

	// BPA Status

	public static final String STATUS_INITIATED = "INPROGRESS";

	public static final String STATUS_APPLIED = "INPROGRESS";

	public static final String STATUS_APPROVED = "APPROVED";

	public static final String STATUS_REJECTED = "REJECTED";

	public static final String STATUS_REVOCATED = "PERMIT REVOCATION";

	public static final String STATUS_DOCUMENTVERIFICATION = "INPROGRESS";

	public static final String STATUS_FIELDINSPECTION = "INPROGRESS";

	public static final String STATUS_NOCUPDATION = "INPROGRESS";

	public static final String STATUS_PENDINGAPPROVAL = "INPROGRESS";

	public static final String STATUS_CANCELLED = "CANCELLED";

	public static final String STATUS_PAID = "INPROGRESS";

	public static final String BILL_AMOUNT = "$.Demands[0].demandDetails[0].taxAmount";

	// ACTION_STATUS combinations for notification

	public static final String ACTION_STATUS_INITIATED = "INITIATE_INITIATED";

	public static final String ACTION_STATUS_SEND_TO_CITIZEN = "SEND_TO_CITIZEN_CITIZEN_APPROVAL_INPROCESS";

	public static final String ACTION_STATUS_SEND_TO_ARCHITECT = "SEND_TO_ARCHITECT_INITIATED";

	public static final String ACTION_STATUS_CITIZEN_APPROVE = "APPROVE_INPROGRESS";

	public static final String ACTION_STATUS_PENDING_APPL_FEE = "APPLY_PENDING_APPL_FEE";

	public static final String ACTION_STATUS_DOC_VERIFICATION = "PAY_DOC_VERIFICATION_INPROGRESS";

	public static final String ACTION_STATUS_FI_VERIFICATION = "FORWARD_FIELDINSPECTION_INPROGRESS";

	public static final String ACTION_STATUS_NOC_VERIFICATION = "FORWARD_NOC_VERIFICATION_INPROGRESS";

	public static final String ACTION_STATUS_PENDING_APPROVAL = "FORWARD_APPROVAL_INPROGRESS";

	public static final String ACTION_STATUS_PENDING_SANC_FEE = "APPROVE_PENDING_SANC_FEE_PAYMENT";

	public static final String ACTION_STATUS_APPROVED = "PAY_APPROVED";

	public static final String ACTION_STATUS_APPLIED = "APPLIED";

	public static final String ACTION_STATUS_REJECTED = "REJECT_REJECTED";

	public static final String ACTION_STATUS_DOCUMENTVERIFICATION = "FORWARD_DOCUMENTVERIFICATION";

	// public static final String ACTION_CANCEL_CANCELLED = "CANCEL_CANCELLED";

	public static final String ACTION_STATUS_PAID = "PAID";

	public static final String ACTION_STATUS_FIELDINSPECTION = "FORWARD_FIELDINSPECTION";

	public static final String ACTION_CANCEL_CANCELLED = "CANCEL_CANCELLED";

	public static final String ACTION_STATUS_NOCUPDATION = "FORWARD_NOCUPDATION";

	public static final String USREVENTS_EVENT_TYPE = "SYSTEMGENERATED";
	public static final String USREVENTS_EVENT_NAME = "Building Plan";
	public static final String USREVENTS_EVENT_POSTEDBY = "SYSTEM-RGA";
	
	public static final String FI_STATUS = "FIELDINSPECTION_INPROGRESS";
	public static final String FI_ADDITIONALDETAILS = "fieldinspection_pending";
	
	public static final String  STATUS_CITIZEN_APPROVAL_INPROCESS = "CITIZEN_APPROVAL_INPROCESS";

	// OCCUPANCY TYPE

	public static final String RESIDENTIAL_OCCUPANCY = "A";

	// CALCULATION FEEe
	public static final String APPLICATION_FEE_KEY = "ApplicationFee";
	public static final String SANCTION_FEE_KEY = "SanctionFee";
	public static final String LOW_RISK_PERMIT_FEE_KEY = "LOW_RISK_PERMIT_FEE";
	public static final String LOW_APPLICATION_FEE_KEY = "Low_ApplicationFee";

	public static final String SANC_FEE_STATE = "PENDING_SANC_FEE_PAYMENT";
	public static final String APPL_FEE_STATE = "PENDING_APPL_FEE";
	public static final String BPA_LOW_APPL_FEE_STATE = "PENDING_FEE";
	public static final String APPROVED_STATE = "APPROVED";
	public static final String DOCVERIFICATION_STATE = "DOC_VERIFICATION_PENDING";
	public static final String NOCVERIFICATION_STATUS = "NOC_VERIFICATION_INPROGRESS";

	public static final String PENDING_APPROVAL_STATE = "PENDINGAPPROVAL";

	public static final String APPL_FEE = "RGA.NC_APP_FEE";

	public static final String SANC_FEE = "RGA.NC_SAN_FEE";
	public static final String INPROGRESS_STATUS = "INPROGRESS";

	// CheckList
	public static final String QUESTIONS_MAP = "$.MdmsRes.RGA.CheckList[?(@.WFState==\"{1}\" && @.RiskType==\"{2}\" && @.ServiceType==\"{3}\" && @.applicationType==\"{4}\")].questions";

	public static final String DOCTYPES_MAP = "$.MdmsRes.RGA.CheckList[?(@.WFState==\"{1}\" && @.RiskType==\"{2}\" && @.ServiceType==\"{3}\" && @.applicationType==\"{4}\")].docTypes";
	public static final String CONDITIONS_MAP = "$.MdmsRes.RGA.CheckList[?(@.WFState==\"{1}\" && @.RiskType==\"{2}\" && @.ServiceType==\"{3}\" && @.applicationType==\"{4}\")].conditions";
	public static final String CHECKLISTFILTER = "$.[?(@.WFState==\"{}\")]";

	public static final String CHECKLIST_TYPE = "checkList";
	public static final String DOCTYPES_TYPE = "docTypes";
	public static final String QUESTIONS_TYPE = "questions";
	public static final String QUESTION_TYPE = "question";
	public static final String INSPECTION_DATE= "date";
	public static final String INSPECTION_TIME= "time";
	public static final String DOCS = "docs";
	public static final String CODE = "documentType";
	public static final String QUESTIONS_PATH = "$.[?(@.active==true)].question";
	public static final String DOCTYPESS_PATH = "$.[?(@.required==true)].code";
//	public static final String NOCTYPE_MAP = "$.MdmsRes.BPA.NocTypeMapping[?(@.applicationType==\"{1}\" && @.serviceType==\"{2}\" && @.riskType==\"{3}\" && @.nocTriggerState==\"{4}\")].nocTypes";
//	public static final String NOCTYPE_REQUIRED_MAP = "$.MdmsRes.BPA.NocTypeMapping[?(@.applicationType==\"{1}\" && @.serviceType==\"{2}\" && @.riskType==\"{3}\")].nocTypes";
//	public static final String NOCTYPE_OFFLINE_MAP = "$.MdmsRes.NOC.NocType[?(@.mode==\"offline\")].code";	
//	public static final String NOC_TRIGGER_STATE_MAP = "$.MdmsRes.BPA.NocTypeMapping[?(@.applicationType==\"{1}\" && @.serviceType==\"{2}\" && @.riskType==\"{3}\")].nocTriggerState";

	// SMS Notification messages
	public static final String APP_CREATE = "APPLICATION_CREATE_MSG";

	public static final String SEND_TO_CITIZEN = "SEND_TO_CITIZEN_MSG";

	public static final String CITIZEN_APPROVED = "CITIZEN_APPROVED_MSG";

	public static final String SEND_TO_ARCHITECT = "SEND_TO_ARCHITECT_MSG";

	public static final String APP_CLOSED = "APP_CLOSED_MSG";

	public static final String APP_FEE_PENDNG = "APPLICATION_FEE_PENDING_MSG";

	public static final String PAYMENT_RECEIVE = "PAYMENT_RECEIVED_MSG";

	public static final String DOC_VERIFICATION = "DOC_VERIFICATION_DONE_MSG";

	public static final String NOC_VERIFICATION = "NOC_FIELD_VERIFICATION_DONE_MSG";

	public static final String NOC_APPROVE = "NOC_APPROVED_MSG";

	public static final String PERMIT_FEE_GENERATED = "PERMIT_FEE_GENERATED_MSG";

	public static final String APPROVE_PERMIT_GENERATED = "APPROVED_AND_PERMIT_GENERATED_MSG";

	public static final String APP_REJECTED = "APPLICATION_REJECTED_MSG";

	public static final String M_APP_CREATE = "M_APPLICATION_CREATE_MSG";

	public static final String M_SEND_TO_CITIZEN = "M_SEND_TO_CITIZEN_MSG";

	public static final String M_CITIZEN_APPROVED = "M_CITIZEN_APPROVED_MSG";

	public static final String M_SEND_TO_ARCHITECT = "M_SEND_TO_ARCHITECT_MSG";

	public static final String M_APP_CLOSED = "M_APP_CLOSED_MSG";

	public static final String M_APP_FEE_PENDNG = "M_APPLICATION_FEE_PENDING_MSG";

	public static final String M_PAYMENT_RECEIVE = "M_PAYMENT_RECEIVED_MSG";

	public static final String M_DOC_VERIFICATION = "M_DOC_VERIFICATION_DONE_MSG";

	public static final String M_NOC_VERIFICATION = "M_NOC_FIELD_VERIFICATION_DONE_MSG";

	public static final String M_NOC_APPROVE = "M_NOC_APPROVED_MSG";

	public static final String M_PERMIT_FEE_GENERATED = "M_PERMIT_FEE_GENERATED_MSG";

	public static final String M_APPROVE_PERMIT_GENERATED = "M_APPROVED_AND_PERMIT_GENERATED_MSG";

	public static final String M_APP_REJECTED = "M_APPLICATION_REJECTED_MSG";

	public static final String SEARCH_MODULE = "rainmaker-rga";

//	public static final String INVALID_SEARCH = "INVALID SEARCH";

	public static final String INVALID_UPDATE = "INVALID UPDATE";

	public static final String EMPLOYEE = "EMPLOYEE";

	public static final String FILESTOREID = "fileStoreId";

	public static final String LOW_RISKTYPE = "LOW";
	
	public static final String VLOW_RISKTYPE = "VLOW";

	public static final String EDCR_PDF = "ScrutinyReport.pdf";

	public static final String PERMIT_ORDER_NO = "RGA_PDF_PLANPERMISSION_NO";

	public static final String GENERATEDON = "RGA_PDF_GENERATED_ON";

	public static final String CITIZEN = "CITIZEN";
	
	public static final String ACTION_SENDBACKTOCITIZEN = "SEND_BACK_TO_CITIZEN";

	public static final String HIGH_RISKTYPE = "HIGH";

	public static final String BUILDING_PLAN = "BUILDING_PLAN_SCRUTINY";
	
	public static final String REGULARISATION = "REGULARISATION";
	
	public static final String BUILDING_PLAN_OC = "BUILDING_OC_PLAN_SCRUTINY";

	public static final String BPA_OC_MODULE_CODE = "BPA_OC";

	public static final String OC_OCCUPANCY = "$.edcrDetail[0].planDetail.planInformation.occupancy";

	public static final String OC_KHATHANO = "$.edcrDetail[0].planDetail.planInformation.khataNo";

	public static final String OC_PLOTNO = "$.edcrDetail[0].planDetail.planInformation.plotNo";
	
	public static final String SERVICETYPE = "serviceType";
	
	public static final String APPLICATIONTYPE = "applicationType";
	
	public static final String PERMIT_NO = "permitNumber";

	public static final String NOC_MODULE = "NOC";

	public static final String NOC_TYPE = "NocType";

	public static final String NOC_APPLICATIONTYPE = "NEW";

	public static final String NOC_SOURCE = "BPA";

	public static final String CHANNEL_NAME_SMS = "SMS";

	public static final String CHANNEL_NAME_EVENT = "EVENT";

	public static final String CHANNEL_NAME_EMAIL = "EMAIL";

	public static final String MODULE = "module";

	public static final String ACTION = "action";

	public static final String CHANNEL_LIST = "channelList";

	public static final String CHANNEL = "Channel";

	public static final String BPA_BUSINESSSERVICE = "RGA";
	
	public static final String BPA_LOW_BUSINESSSERVICE = "RGA_LOW";

}
