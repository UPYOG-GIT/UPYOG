package org.egov.pg.service.gateways.ccavenue;

import static java.util.Objects.isNull;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.models.Transaction;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.service.Gateway;
import org.egov.pg.service.TransactionService;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CcavenueGateway implements Gateway {

//	@Autowired
	private TransactionService transactionService;

//	@Autowired
//	private TransactionsApiController transactionsApiController;

	private final String GATEWAY_NAME = "CCAVENUE";
	private String ACCESS_CODE;
//	private final String ACCESS_CODE;
	private String WORKING_KEY;
//	private final String WORKING_KEY;
	private final String MERCHANT_URL_PAY;
	private final String MERCHANT_URL_STATUS;
	private final String MERCHANT_PATH_PAY;
	private final String MERCHANT_PATH_STATUS;
	private String MERCHANT_ID;
//	private final String MERCHANT_ID;
	private final String COMMAND;
	private final String REQUEST_TYPE;
	private final String RESPONSE_TYPE;
	private final String RETURN_URL;
	private final String WS_URL;
	private final boolean ACTIVE;

	private RestTemplate restTemplate;
	private ObjectMapper objectMapper;

	private final String MESSAGE_TYPE;

	private final String CURRENCY_CODE;
	private final String REDIRECT_URL;
	private final String ORIGINAL_RETURN_URL_KEY;

	private final String MESSAGE_TYPE_KEY = "messageType";
	private final String MERCHANT_ID_KEY = "merchant_id";

	private final String SERVICE_ID_KEY = "serviceId";
	private final String ORDER_ID_KEY = "order_id";
	private final String CUSTOMER_ID_KEY = "customerId";
	private final String TRANSACTION_AMOUNT_KEY = "amount";
	private final String CURRENCY_CODE_KEY = "currency";
	private final String REQUEST_DATE_TIME_KEY = "requestDateTime";
	private final String SUCCESS_URL_KEY = "redirect_url";
	private final String FAIL_URL_KEY = "cancel_url";
	private final String ADDITIONAL_FIELD1_KEY = "merchant_param1";
	private final String ADDITIONAL_FIELD2_KEY = "merchant_param2";
	private final String ADDITIONAL_FIELD3_KEY = "merchant_param3";
	private final String ADDITIONAL_FIELD4_KEY = "merchant_param4";
	private final String ADDITIONAL_FIELD5_KEY = "merchant_param5";
	private final String ADDITIONAL_FIELD_VALUE = "111111";
//	private final String GATEWAY_TRANSACTION_STATUS_URL;
//	private final String GATEWAY_URL;
	private final String CITIZEN_URL;
	private static final String SEPERATOR = "|";
	private String TX_DATE_FORMAT;
	private final RequestInfo requestInfo;
	private PgDetailRepository pgDetailRepository;

	@Autowired
	public CcavenueGateway(RestTemplate restTemplate, Environment environment, ObjectMapper objectMapper,
			PgDetailRepository pgDetailRepository) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
//		this.transactionService = transactionService;
		this.ACTIVE = Boolean.valueOf(environment.getRequiredProperty("ccavenue.active"));
//		this.ACCESS_CODE = environment.getRequiredProperty("ccavenue.access.code");
//		this.WORKING_KEY = environment.getRequiredProperty("ccavenue.working.key");
		this.MERCHANT_URL_PAY = environment.getRequiredProperty("ccavenue.url");
		this.MERCHANT_URL_STATUS = environment.getRequiredProperty("payu.url.status");
		this.MERCHANT_PATH_PAY = environment.getRequiredProperty("payu.path.pay");
		this.MERCHANT_PATH_STATUS = environment.getRequiredProperty("payu.path.status");
//		this.MERCHANT_ID = environment.getRequiredProperty("ccavenue.merchant.id");
		this.WS_URL = environment.getRequiredProperty("ccavenue.path.wsurl");
		this.RETURN_URL = environment.getRequiredProperty("ccavenue.redirect.url");
		this.REDIRECT_URL = environment.getRequiredProperty("ccavenue.redirect.url");
		this.MESSAGE_TYPE = environment.getRequiredProperty("ccavenue.messagetype");
		this.CURRENCY_CODE = environment.getRequiredProperty("ccavenue.currency");
		this.CITIZEN_URL = environment.getRequiredProperty("ccavenue.default.citizen.url");
		this.ORIGINAL_RETURN_URL_KEY = environment.getRequiredProperty("ccavenue.original.return.url.key");
		this.TX_DATE_FORMAT = environment.getRequiredProperty("ccavenue.dateformat");
		this.COMMAND = "initiatTransaction";
		this.REQUEST_TYPE = "JSON";
		this.RESPONSE_TYPE = "JSON";

		User userInfo = User.builder().uuid("PG_DETAIL_GET").type("SYSTEM").roles(Collections.emptyList()).id(0L)
				.build();

		requestInfo = new RequestInfo("", "", 0L, "", "", "", "", "", "", userInfo);
		this.pgDetailRepository = pgDetailRepository;
	}

	@Override
	public URI generateRedirectURI(Transaction transaction) {

		log.info("Inside CCAvenue generateRedirectURI()");
//		Random random = new Random();
//		int randomNumber = random.nextInt(90000000) + 10000000;

		String tenantId = transaction.getTenantId();

		// set MerchantId, WorkingKey and AccessKey according to tenantId
		setGatewayDetails(tenantId);

		log.info("MERCHANT_ID: " + MERCHANT_ID + ", WORKING_KEY: " + WORKING_KEY + ", ACCESS_CODE: " + ACCESS_CODE);

		log.info("transaction.getTxnId() : " + transaction.getTxnId());
//		String orderNumber = "CG" + randomNumber;
		String orderNumber = transaction.getTxnId();
		Double amount = Double.parseDouble(transaction.getTxnAmount());
		String callBackUrl = transaction.getCallbackUrl();
//		String jsonData = "{ \"merchant_id\":\""+MERCHANT_ID+"\", \"order_id\":\"" + orderNumber + "\" }";
//		String jsonData = "{ \"merchant_id\":1941257, \"order_id\":\"" + orderNumber
//				+ "\" ,\"currency\":\"INR\",\"amount\":" + amount + "}";

		String requestString = "merchant_id=" + MERCHANT_ID + "&order_id=" + orderNumber + "&currency=INR&amount="
				+ amount + "&redirect_url=" + RETURN_URL + "&cancel_url=" + RETURN_URL + ""
				+ "&language=EN&billing_name=&billing_address=&" + "billing_city=&billing_state=&billing_zip=&"
				+ "billing_country=&billing_tel=&billing_email=&" + "delivery_name=&delivery_address=&delivery_city="
				+ "&delivery_state=&delivery_zip=&delivery_country=" + "&delivery_tel=&merchant_param1=" + callBackUrl
				+ "&merchant_param2=CCAVENUE" + "&merchant_param3=&merchant_param4=&merchant_param5=&tid=";

//		&tid=76070845
		log.info("requestString : " + requestString);
		String encryptedJsonData = "";
		StringBuffer wsDataBuff = new StringBuffer();

		if (WORKING_KEY != null && !WORKING_KEY.equals("") && requestString != null && !requestString.equals("")) {
			CcavenueUtils ccavenueUtis = new CcavenueUtils(WORKING_KEY);
			encryptedJsonData = ccavenueUtis.encrypt(requestString);
		}
//		wsDataBuff.append("encRequest=" + encryptedJsonData + "&access_code=" + ACCESS_CODE);
//		wsDataBuff.append("encRequest=" + encryptedJsonData + "&access_code=" + ACCESS_CODE );
		wsDataBuff
				.append("?command=initiateTransaction&encRequest=" + encryptedJsonData + "&access_code=" + ACCESS_CODE);
//		wsDataBuff.append("encRequest=" + encryptedJsonData + "&access_code=" + ACCESS_CODE + "&response_type="
//				+ RESPONSE_TYPE + "&request_type=" + REQUEST_TYPE);

		URL url = null;
		URLConnection httpUrlConnection = null;
//		HttpURLConnection httpUrlConnection = null;
		DataOutputStream vPrintout = null;
		DataInputStream vInput = null;
		StringBuffer vStringBuffer = null;
		try {
//			WS_URL+="&" + wsDataBuff;
			String urlString = WS_URL + wsDataBuff;
//			url = new URL(WS_URL + "&" + wsDataBuff);
			url = new URL(urlString);

			if (url.openConnection() instanceof HttpsURLConnection) {
				httpUrlConnection = (HttpsURLConnection) url.openConnection();
//				httpUrlConnection.setRequestMethod("POST");
			} else {
				httpUrlConnection = (URLConnection) url.openConnection();
			}
			httpUrlConnection.setDoInput(true);
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setUseCaches(false);
			httpUrlConnection.connect();
			vPrintout = new DataOutputStream(httpUrlConnection.getOutputStream());
			vPrintout.writeBytes(wsDataBuff.toString());
			vPrintout.flush();
			vPrintout.close();
//			if (isNull(url))
			log.info("httpUrlConnection.getURL().toURI(): " + httpUrlConnection.getURL().toURI());
			if (isNull(httpUrlConnection.getURL()))
				throw new CustomException("CCAVENUE_REDIRECT_URI_GEN_FAILED", "Failed to generate redirect URI");
			else {
//				HttpServletResponse response;
//				response.sendRedirect(httpUrlConnection.getURL().toURI().toString());

				HashMap<String, String> queryMap = new HashMap<>();
				queryMap.put(MESSAGE_TYPE_KEY, MESSAGE_TYPE);
				queryMap.put(MERCHANT_ID_KEY, MERCHANT_ID);
				queryMap.put(ORDER_ID_KEY, orderNumber);
				queryMap.put(CUSTOMER_ID_KEY, transaction.getUser().getUuid());
				queryMap.put(TRANSACTION_AMOUNT_KEY, amount.toString());
				queryMap.put(CURRENCY_CODE_KEY, CURRENCY_CODE);
				SimpleDateFormat format = new SimpleDateFormat(TX_DATE_FORMAT);
				Date currentDate = new Date();
				queryMap.put(REQUEST_DATE_TIME_KEY, format.format(currentDate));
				String returnUrl = transaction.getCallbackUrl().replace(CITIZEN_URL, "");

				queryMap.put(SERVICE_ID_KEY, getModuleCode(transaction));
				String domainName = returnUrl.replaceAll("http(s)?://|www\\.|/.*", "");
				String citizenReturnURL = returnUrl.split(domainName)[1];
//		        log.info("returnUrl::::"+getReturnUrl(citizenReturnURL, REDIRECT_URL));
				queryMap.put(SUCCESS_URL_KEY, RETURN_URL);
				queryMap.put(FAIL_URL_KEY, RETURN_URL);
//		        log.info("returnUrl::::"+getReturnUrl(citizenReturnURL, REDIRECT_URL));
//		        queryMap.put(SUCCESS_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
//		        queryMap.put(FAIL_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
				StringBuffer userDetail = new StringBuffer();
				if (transaction.getUser() != null) {
					if (!StringUtils.isEmpty(transaction.getUser().getMobileNumber())) {
						userDetail.append(transaction.getUser().getMobileNumber());
					}

					/*
					 * if(!StringUtils.isEmpty(transaction.getUser().getEmailId())) {
					 * if(userDetail.length()>0) { userDetail.append("^"); }
					 * userDetail.append(transaction.getUser().getEmailId()); }
					 */
				}
				if (userDetail.length() == 0) {
					userDetail.append(ADDITIONAL_FIELD_VALUE);
				}
				queryMap.put(ADDITIONAL_FIELD1_KEY, userDetail.toString());
				queryMap.put(ADDITIONAL_FIELD2_KEY, ADDITIONAL_FIELD_VALUE); // Not in use
				queryMap.put(ADDITIONAL_FIELD3_KEY, ADDITIONAL_FIELD_VALUE); // Not in use
				queryMap.put(ADDITIONAL_FIELD4_KEY, transaction.getConsumerCode());
				queryMap.put(ADDITIONAL_FIELD5_KEY, getModuleCode(transaction));

				// Generate Checksum for params
				ArrayList<String> fields = new ArrayList<String>();
				fields.add(queryMap.get(MESSAGE_TYPE_KEY));
				fields.add(queryMap.get(MERCHANT_ID_KEY));
				fields.add(queryMap.get(SERVICE_ID_KEY));
				fields.add(queryMap.get(ORDER_ID_KEY));
				fields.add(queryMap.get(CUSTOMER_ID_KEY));
				fields.add(queryMap.get(TRANSACTION_AMOUNT_KEY));
				fields.add(queryMap.get(CURRENCY_CODE_KEY));
				fields.add(queryMap.get(REQUEST_DATE_TIME_KEY));
				fields.add(queryMap.get(SUCCESS_URL_KEY));
				fields.add(queryMap.get(FAIL_URL_KEY));
				fields.add(queryMap.get(ADDITIONAL_FIELD1_KEY));
				fields.add(queryMap.get(ADDITIONAL_FIELD2_KEY));
				fields.add(queryMap.get(ADDITIONAL_FIELD3_KEY));
				fields.add(queryMap.get(ADDITIONAL_FIELD4_KEY));
				fields.add(queryMap.get(ADDITIONAL_FIELD5_KEY));

				String message = String.join("|", fields);
				queryMap.put("checksum", CcavenueUtils.generateCRC32Checksum(message, WORKING_KEY));
				queryMap.put("txURL", httpUrlConnection.getURL().toURI().toString());
				SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyyHH:mm:SSS");
				queryMap.put(REQUEST_DATE_TIME_KEY, format1.format(currentDate));
				log.info("REQUEST_DATE_TIME_KEY::" + queryMap.get(REQUEST_DATE_TIME_KEY));
				ObjectMapper mapper = new ObjectMapper();
//		        try {
////		            urlData= mapper.writeValueAsString(queryMap);
//		        } catch (Exception e) {
//		            // TODO Auto-generated catch block
//		            log.error("PAYGOV URL generation failed", e);
//		            throw new CustomException("URL_GEN_FAILED",
//		                    "PAYGOV URL generation failed, gateway redirect URI cannot be generated");
//		        }

				MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
				queryMap.forEach(params::add);

//				String paramsString = "checksum$"+CcavenueUtils.generateCRC32Checksum(message, WORKING_KEY)+""
//						+ "*txURL$"+httpUrlConnection.getURL().toURI().toString()+"*"+MESSAGE_TYPE_KEY+"$"+MESSAGE_TYPE+""
//						+ "*"+MERCHANT_ID_KEY+"$"+MERCHANT_ID+"*"+ORDER_ID_KEY+"$"+orderNumber+""
//						+ "*"+CUSTOMER_ID_KEY+"$"+transaction.getUser().getUuid()+"*"+TRANSACTION_AMOUNT_KEY+"$"+amount+""
//						+ "*"+CURRENCY_CODE_KEY+"$"+CURRENCY_CODE+"*"+REQUEST_DATE_TIME_KEY+"$"+format.format(currentDate)+""
//						+ "*"+SERVICE_ID_KEY+"$"+getModuleCode(transaction)+"*"+SUCCESS_URL_KEY+"$"+RETURN_URL+""
//						+ "*"+FAIL_URL_KEY+"$"+RETURN_URL+"*"+ADDITIONAL_FIELD1_KEY+"$"+userDetail.toString()+""
//						+ "*"+ADDITIONAL_FIELD2_KEY+"$"+ADDITIONAL_FIELD_VALUE+"*"+ADDITIONAL_FIELD3_KEY+"$"+ADDITIONAL_FIELD_VALUE+""
//						+ "*"+"ADDITIONAL_FIELD4_KEY"+"$"+transaction.getConsumerCode()+"*"+ADDITIONAL_FIELD5_KEY+"$"+getModuleCode(transaction);

				UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(urlString).queryParams(params).build();

				log.info("uriComponents: " + uriComponents.toUri().toString());

				return uriComponents.toUri();
//				return httpUrlConnection.getURL().toURI();
			}
//			return url.toURI();
		} catch (Exception e) {
			log.error("Unable to retrieve redirect URI from gateway", e);
			throw new ServiceCallException("Redirect URI generation failed, invalid response received from gateway");

		}

		/*
		 * try { BufferedReader bufferedreader = new BufferedReader(new
		 * InputStreamReader(vHttpUrlConnection.getInputStream())); vStringBuffer = new
		 * StringBuffer(); String vRespData; while((vRespData =
		 * bufferedreader.readLine()) != null) if(vRespData.length() != 0)
		 * vStringBuffer.append(vRespData.trim()); bufferedreader.close();
		 * bufferedreader = null; if (vInput != null) vInput.close(); if
		 * (vHttpUrlConnection != null) vHttpUrlConnection = null; }catch(Exception ex)
		 * {
		 * 
		 * }
		 */

	}

	@Override
	public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
		log.info("inside CcavenueGateway.fetchStatus().....");
//		CcavenueResponse resp = objectMapper.convertValue(params, CcavenueResponse.class);
		if (params.containsKey("FromUpdateAPI")) {
			log.info("Inside fetchStatus() if condition......");
//			CcavenueResponse resp = new CcavenueResponse();
//			if (!isNull(resp.getEncResp()) && !isNull(resp.getOrderNo()))
//				;
//				String checksum = resp.getHash();

//			String encResp = resp.getEncResp();
			String encResp = params.get("encResp");

			String tenantId = currentStatus.getTenantId();

			// set MerchantId, WorkingKey and AccessKey according to tenantId
			setGatewayDetails(tenantId);
			log.info("fetchStatus: MERCHANT_ID: " + MERCHANT_ID + ", WORKING_KEY: " + WORKING_KEY + ", ACCESS_CODE: "
					+ ACCESS_CODE);
//			String orderNo = resp.getOrderNo();
			log.info("encResp: " + encResp);
			CcavenueUtils ccavenueUtis = new CcavenueUtils(WORKING_KEY);
			String decryptedData = ccavenueUtis.decrypt(encResp);
			log.info("decryptedData: " + decryptedData);
			String encRespString[] = decryptedData.split("&");
			Map<String, String> resMap = new HashMap<String, String>();
			for (String s : encRespString) {
				String s2[] = s.split("=");
				if (!s2[0].equals("merchant_param1")) {
					String key = s2[0];
					String value = "";
					if (s2.length > 1) {
						value = s2[1];
					}
					resMap.put(key, value);
				}
			}

			log.info("resMap: " + resMap.toString());
			Transaction txn = transformRawResponse(resMap, currentStatus);
//			log.info("txn:" + txn.getTxnAmount());
			log.info("txn.getTxnStatus():" + txn.getTxnStatus());
			if (txn.getTxnStatus().equals(Transaction.TxnStatusEnum.SUCCESS)) {
				return txn;
			} else {
				return fetchStatusFromGateway(currentStatus, resMap);
			}
		}

//		if (txn.getTxnStatus().equals(Transaction.TxnStatusEnum.PENDING)
//				|| txn.getTxnStatus().equals(Transaction.TxnStatusEnum.FAILURE)) {
//			return txn;
//		}

		Map<String, String> resMap = new HashMap<String, String>();
		return fetchStatusFromGateway(currentStatus, resMap);
//		return txn;
	}

	@Override
	public boolean isActive() {
		return ACTIVE;
	}

	@Override
	public String gatewayName() {
		return GATEWAY_NAME;
	}

	@Override
	public String transactionIdKeyInResponse() {
		return "txnid";
	}

	private Transaction transformRawResponse(Map<String, String> resp, Transaction currentStatus) {
		log.info("inside CcavenueGateway.transformRawResponse().....");
		Transaction.TxnStatusEnum status;

		String gatewayStatus = resp.get("order_status");
//		String trackerStatus = resp.get("status");
		log.info("gatewayStatus: " + gatewayStatus);

		if (gatewayStatus.equalsIgnoreCase("success") || gatewayStatus.equalsIgnoreCase("Shipped")) {
			status = Transaction.TxnStatusEnum.SUCCESS;
			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.get("amount")).txnStatus(status)
					.gatewayTxnId(resp.get("tracking_id")).gatewayPaymentMode(resp.get("payment_mode"))
					.gatewayStatusCode(resp.get("status_code")).gatewayStatusMsg(resp.get("status_message"))
					.responseJson(resp).build();
		} else {
			status = Transaction.TxnStatusEnum.FAILURE;
			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.get("amount")).txnStatus(status)
					.gatewayTxnId(resp.get("tracking_id")).gatewayStatusCode(resp.get("status_code"))
					.gatewayStatusMsg(resp.get("failure_message")).responseJson(resp).build();
		}

	}

	private Transaction fetchStatusFromGateway(Transaction currentStatus, Map<String, String> resMap) {
		log.info("inside CcavenueGateway.fetchStatusFromGateway().....");

//		String refNo = resMap.get("tracking_id");
//		String orderNo = resMap.get("order_id");
		String orderNo = currentStatus.getTxnId();

//		String orderStatusQueryJson = "{ \"reference_no\":\"" + refNo + "\", \"order_no\":\"" + orderNo + "\" }";
		String orderStatusQueryJson = "{ \"order_no\":\"" + orderNo + "\" }";

		String encryptedJsonData = "";

		String tenantId = currentStatus.getTenantId();

		// set MerchantId, WorkingKey and AccessKey according to tenantId
		setGatewayDetails(tenantId);
		log.info("fetchStatusFromGateway: MERCHANT_ID: " + MERCHANT_ID + ", WORKING_KEY: " + WORKING_KEY
				+ ", ACCESS_CODE: " + ACCESS_CODE);
		CcavenueUtils ccavenueUtis = new CcavenueUtils(WORKING_KEY);
		encryptedJsonData = ccavenueUtis.encrypt(orderStatusQueryJson);

		URL url = null;
		HttpURLConnection vHttpUrlConnection = null;
		DataInputStream vInput = null;
		String urlStr = "https://login.ccavenue.com/apis/servlet/DoWebTrans?enc_request=" + encryptedJsonData
				+ "&access_code=" + ACCESS_CODE
				+ "&request_type=JSON&response_type=JSON&command=orderStatusTracker&version=1.2";
//		String urlStr = "https://logintest.ccavenue.com/apis/servlet/DoWebTrans?enc_request=" + encryptedJsonData
//				+ "&access_code=" + ACCESS_CODE
//				+ "&request_type=JSON&response_type=JSON&command=orderStatusTracker&version=1.2";
		StringBuffer vStringBuffer = null;
		try {
			url = new URL(urlStr);
//			if (url.openConnection() instanceof HttpsURLConnection) {
			vHttpUrlConnection = (HttpsURLConnection) url.openConnection();
			vHttpUrlConnection.setRequestMethod("POST");
//			}
			vHttpUrlConnection.setDoInput(true);
			vHttpUrlConnection.setDoOutput(true);
			vHttpUrlConnection.setUseCaches(false);

			vHttpUrlConnection.connect();
			try {
				BufferedReader bufferedreader = new BufferedReader(
						new InputStreamReader(vHttpUrlConnection.getInputStream()));
				vStringBuffer = new StringBuffer();
				String vRespData;
				while ((vRespData = bufferedreader.readLine()) != null)
					if (vRespData.length() != 0)
						vStringBuffer.append(vRespData.trim());
				bufferedreader.close();
				bufferedreader = null;
			} finally {
				if (vInput != null)
					vInput.close();
			}

			System.out.println("url: " + vHttpUrlConnection.getURL().toURI());
//			System.out.println("vStringBuffer: " + vStringBuffer);
			if (isNull(vHttpUrlConnection.getURL().toURI())) {
				log.info("CCAVENUE_REDIRECT_URI_GEN_FAILED");
				return currentStatus;
			}
		} catch (Exception e) {
			log.info("Unable to retrieve redirect URI from gateway: " + e);
			return currentStatus;
		}

		String vResponse = vStringBuffer.toString();
		String encResponse;
		Map<String, String> resp = new HashMap<String, String>();
		if (vResponse != null && !vResponse.equals("")) {
			Map hm = CcavenueUtils.tokenizeToHashMap(vResponse, "&", "=");
			encResponse = hm.containsKey("enc_response") ? hm.get("enc_response").toString() : "";
			String vStatus = hm.containsKey("status") ? hm.get("status").toString() : "";
			String vError_code = hm.containsKey("enc_error_code") ? hm.get("enc_error_code").toString() : "";
			if (vStatus.equals("1")) {// If Api call failed
				log.info("enc_response : " + encResponse);
				log.info("error_code : " + vError_code);
				return currentStatus;
//				throw new CustomException("FAILED_TO_FETCH_STATUS_FROM_GATEWAY",
//						"Unable to fetch status from payment gateway for txnid: " + currentStatus.getTxnId());
			}
			String decResponse = "";
			if (!encResponse.equals("")) {
				decResponse = ccavenueUtis.decrypt(encResponse);
				log.info("Dec Response : " + decResponse);
			}

			if (vStatus.equals("0") && decResponse != null) {
				String[] keyValuePairs = decResponse.split(",");
				for (String pair : keyValuePairs) {
					String[] entry = pair.split(":");
					resp.put(entry[0].replace("\"", "").replace("{", "").replace("}", "").trim(),
							entry[1].replace("\"", "").replace("{", "").replace("}", "").trim());
				}
				Map<String, String> responseMap = new HashMap<String, String>();
				if (resp.containsKey("order_status")) {
//					if (resp.get("order_status").equalsIgnoreCase("Unsuccessful")) {
//						responseMap.put("order_status", resp.get("Failure"));
//					}
//					Long createdTime = currentStatus.getAuditDetails().getCreatedTime();
//					Long currentTime = System.currentTimeMillis();
					Long timeDifference = System.currentTimeMillis() - currentStatus.getAuditDetails().getCreatedTime();
					log.info("timeDifference: " + timeDifference);
					if (resp.get("order_status").equalsIgnoreCase("Shipped")) {
						responseMap.put("order_status", "Success");
					} else if (resp.get("order_status").equalsIgnoreCase("Unsuccessful")
							|| resp.get("order_status").equalsIgnoreCase("Aborted")
							|| resp.get("order_status").equalsIgnoreCase("Failure")
							|| (resp.get("order_status").equalsIgnoreCase("Initiated")
									&& (timeDifference >= 900000 && timeDifference <= 1800000))) {
						responseMap.put("order_status", "Failure");
					}

//					else if (resp.get("order_status").equalsIgnoreCase("Initiated")
//							&& (timeDifference >= 900000 && timeDifference <= 1800000)) {
//						responseMap.put("order_status", "Failure");
//					}

//					else {
//						responseMap.put("order_status", "Failure");
//					}

				}

				if (resp.containsKey("order_amt")) {
					responseMap.put("amount", resp.get("order_amt"));
				}
				if (resp.containsKey("reference_no")) {
					responseMap.put("tracking_id", resp.get("reference_no"));
				}
				if (resp.containsKey("order_bank_response")) {
					responseMap.put("status_message", resp.get("order_bank_response"));
				}
				if (resp.containsKey("order_option_type")) {
					responseMap.put("payment_mode", resp.get("order_option_type"));
				}

//				responseMap.put("payment_mode", "");
				responseMap.put("status_code", "");
				responseMap.put("failure_message", "");
				return transformRawResponse(responseMap, currentStatus);
			}
		}

		return currentStatus;

	}

	private String hashCal(String str) {
		byte[] hashSequence = str.getBytes();
		StringBuilder hexString = new StringBuilder();
		try {
			MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
			algorithm.reset();
			algorithm.update(hashSequence);
			byte messageDigest[] = algorithm.digest();

			for (byte aMessageDigest : messageDigest) {
				String hex = Integer.toHexString(0xFF & aMessageDigest);
				if (hex.length() == 1)
					hexString.append("0");
				hexString.append(hex);
			}

		} catch (NoSuchAlgorithmException nsae) {
			log.error("Error occurred while generating hash " + str, nsae);
			throw new CustomException("CHECKSUM_GEN_FAILED",
					"Hash generation failed, gateway redirect URI " + "cannot be generated");
		}

		return hexString.toString();
	}

	@Override
	public String generateRedirectFormData(Transaction transaction) {

		log.info("inside CCAvenue.generateRedirectFormData()");
//		PgDetail pgDetail = pgDetailRepository.getPgDetailByTenantId(requestInfo, transaction.getTenantId());

		/*
		 *
		 * messageType|merchantId|serviceId|orderId|customerId|transactionAmount|
		 * currencyCode|r equestDateTime|successUrl|failUrl|additionalField1|
		 * additionalField2| additionalField3| additionalField4| additionalField5
		 */

		String tenantId = transaction.getTenantId();

		// set MerchantId, WorkingKey and AccessKey according to tenantId
		setGatewayDetails(tenantId);
		log.info("generateRedirectFormData: MERCHANT_ID: " + MERCHANT_ID + ", WORKING_KEY: " + WORKING_KEY
				+ ", ACCESS_CODE: " + ACCESS_CODE);
		String urlData = null;
		HashMap<String, String> queryMap = new HashMap<>();
		queryMap.put(MESSAGE_TYPE_KEY, MESSAGE_TYPE);
		queryMap.put(MERCHANT_ID_KEY, MERCHANT_ID);
		queryMap.put(SERVICE_ID_KEY, getModuleCode(transaction));
		queryMap.put(ORDER_ID_KEY, transaction.getTxnId());
		queryMap.put(CUSTOMER_ID_KEY, transaction.getUser().getUuid());
		queryMap.put(TRANSACTION_AMOUNT_KEY, String.valueOf(transaction.getTxnAmount()));
		queryMap.put(CURRENCY_CODE_KEY, CURRENCY_CODE);
		SimpleDateFormat format = new SimpleDateFormat(TX_DATE_FORMAT);
		queryMap.put(REQUEST_DATE_TIME_KEY, format.format(new Date()));
		String returnUrl = transaction.getCallbackUrl().replace(CITIZEN_URL, "");

		String domainName = returnUrl.replaceAll("http(s)?://|www\\.|/.*", "");
		String citizenReturnURL = returnUrl.split(domainName)[1];
		String moduleCode = "------";
		if (!StringUtils.isEmpty(transaction.getModule())) {
			if (transaction.getModule().length() < 6) {
				moduleCode = transaction.getModule() + moduleCode.substring(transaction.getModule().length() - 1);
			} else {
				moduleCode = transaction.getModule();
			}
		}

		log.info("returnUrl::::" + getReturnUrl(citizenReturnURL, REDIRECT_URL));
		queryMap.put(SUCCESS_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
		queryMap.put(FAIL_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
		StringBuffer userDetail = new StringBuffer();
		if (transaction.getUser() != null) {
			if (!StringUtils.isEmpty(transaction.getUser().getMobileNumber())) {
				userDetail.append(transaction.getUser().getMobileNumber());
			}

			/*
			 * if(!StringUtils.isEmpty(transaction.getUser().getEmailId())) {
			 * if(userDetail.length()>0) { userDetail.append("^"); }
			 * userDetail.append(transaction.getUser().getEmailId()); }
			 */
		}
		if (userDetail.length() == 0) {
			userDetail.append(ADDITIONAL_FIELD_VALUE);
		}
		queryMap.put(ADDITIONAL_FIELD1_KEY, userDetail.toString());
		queryMap.put(ADDITIONAL_FIELD2_KEY, ADDITIONAL_FIELD_VALUE); // Not in use
		queryMap.put(ADDITIONAL_FIELD3_KEY, ADDITIONAL_FIELD_VALUE); // Not in use
		queryMap.put(ADDITIONAL_FIELD4_KEY, transaction.getConsumerCode());
		queryMap.put(ADDITIONAL_FIELD5_KEY, moduleCode);

		// Generate Checksum for params
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(queryMap.get(MESSAGE_TYPE_KEY));
		fields.add(queryMap.get(MERCHANT_ID_KEY));
		fields.add(queryMap.get(SERVICE_ID_KEY));
		fields.add(queryMap.get(ORDER_ID_KEY));
		fields.add(queryMap.get(CUSTOMER_ID_KEY));
		fields.add(queryMap.get(TRANSACTION_AMOUNT_KEY));
		fields.add(queryMap.get(CURRENCY_CODE_KEY));
		fields.add(queryMap.get(REQUEST_DATE_TIME_KEY));
		fields.add(queryMap.get(SUCCESS_URL_KEY));
		fields.add(queryMap.get(FAIL_URL_KEY));
		fields.add(queryMap.get(ADDITIONAL_FIELD1_KEY));
		fields.add(queryMap.get(ADDITIONAL_FIELD2_KEY));
		fields.add(queryMap.get(ADDITIONAL_FIELD3_KEY));
		fields.add(queryMap.get(ADDITIONAL_FIELD4_KEY));
		fields.add(queryMap.get(ADDITIONAL_FIELD5_KEY));

		String message = String.join("|", fields);
		queryMap.put("checksum", CcavenueUtils.generateCRC32Checksum(message, WORKING_KEY));
		queryMap.put("txURL", WS_URL);
		ObjectMapper mapper = new ObjectMapper();
		try {
			urlData = mapper.writeValueAsString(queryMap);
		} catch (Exception e) {
			log.error("CCAVENUE URL generation failed", e);
			throw new CustomException("URL_GEN_FAILED",
					"CCAVENUE URL generation failed, gateway redirect URI cannot be generated");
		}
		return urlData;
	}

	private String getModuleCode(Transaction transaction) {
		String moduleCode = "------";
		if (!StringUtils.isEmpty(transaction.getModule())) {
			/*
			 * if(transaction.getModule().length() < 6) { moduleCode=
			 * transaction.getModule() +
			 * moduleCode.substring(transaction.getModule().length()-1); }else { moduleCode
			 * =transaction.getModule(); }
			 */
			if (transaction.getModule().equals("BPAREG")) {
				moduleCode = "BPA001";
			} else {
				moduleCode = transaction.getModule().concat("001").toUpperCase();
			}
		}
		return moduleCode;
	}

	private String getReturnUrl(String callbackUrl, String baseurl) {
		return UriComponentsBuilder.fromHttpUrl(baseurl).queryParam(ORIGINAL_RETURN_URL_KEY, callbackUrl).build()
				.toUriString();
	}

	private void setGatewayDetails(String tenantId) {
		log.info("inside setGatewayDetails..... tenantId: " + tenantId);
//		if (tenantId.equals("cg.birgaon")) {
//			this.MERCHANT_ID = "2136858";
//			this.ACCESS_CODE = "AVWN26KC60AF20NWFA";
//			this.WORKING_KEY = "B27E5242E8FC395A07F65AB900F021FA";
//		} else if (tenantId.equals("cg.dhamtari")) {
//			this.MERCHANT_ID = "1941257";
//			this.ACCESS_CODE = "AVII96KA89BB16IIBB";
//			this.WORKING_KEY = "D682025F99E01FA0F0FAA079B1B3F793";
//		} else if (tenantId.equals("cg.bhilaicharoda")) {
//			this.MERCHANT_ID = "2160767";
//			this.ACCESS_CODE = "AVII29KC44BF31IIFB";
//			this.WORKING_KEY = "7B3E3FF7D56888F44E1A7D46DF24CF52";
//		}

		this.MERCHANT_ID = "1941257";
		this.ACCESS_CODE = "ATII96KA89BB16IIBB";
		this.WORKING_KEY = "D682025F99E01FA0F0FAA079B1B3F793";
		
		
//		this.MERCHANT_ID = "2996783";
//		this.ACCESS_CODE = "AVCC61LB26BF70CCFB";
//		this.WORKING_KEY = "FEC003B7AE237C0D954AC2DB24B1B201";
//		Map<String, Object> ccAvenueDetails = transactionService.getCcavenueDetails(tenantId);
//		Map<String, Object> ccAvenueDetails = transactionsApiController.getCcavenueDetails(tenantId);
		
//		Map<String, Object> ccAvenueDetails = pgDetailRepository.getCcavenueDetails(tenantId);
//		this.MERCHANT_ID = ccAvenueDetails.get("merchant_id").toString();
//		this.ACCESS_CODE = ccAvenueDetails.get("access_code").toString();
//		this.WORKING_KEY = ccAvenueDetails.get("working_key").toString();
	}

}