package org.egov.pg.service.gateways.ccavenue;

import static java.util.Objects.isNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.egov.pg.models.PgDetail;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.models.Transaction;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.service.Gateway;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CcavenueGateway implements Gateway {

	private final String GATEWAY_NAME = "CCAVENUE";
	private final String ACCESS_CODE;
	private final String WORKING_KEY;
	private final String MERCHANT_URL_PAY;
	private final String MERCHANT_URL_STATUS;
	private final String MERCHANT_PATH_PAY;
	private final String MERCHANT_PATH_STATUS;
	private final String MERCHANT_ID;
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
//	private PgDetailRepository pgDetailRepository;

	@Autowired
	public CcavenueGateway(RestTemplate restTemplate, Environment environment, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
		this.ACTIVE = Boolean.valueOf(environment.getRequiredProperty("ccavenue.active"));
		this.ACCESS_CODE = environment.getRequiredProperty("ccavenue.access.code");
		this.WORKING_KEY = environment.getRequiredProperty("ccavenue.working.key");
		this.MERCHANT_URL_PAY = environment.getRequiredProperty("ccavenue.url");
		this.MERCHANT_URL_STATUS = environment.getRequiredProperty("payu.url.status");
		this.MERCHANT_PATH_PAY = environment.getRequiredProperty("payu.path.pay");
		this.MERCHANT_PATH_STATUS = environment.getRequiredProperty("payu.path.status");
		this.MERCHANT_ID = environment.getRequiredProperty("ccavenue.merchant.id");
		this.WS_URL = environment.getRequiredProperty("ccavenue.path.wsurl");
		this.RETURN_URL = environment.getRequiredProperty("ccavenue.redirect.url");
		this.REDIRECT_URL = environment.getRequiredProperty("ccavenue.redirect.url");
		this.MESSAGE_TYPE = environment.getRequiredProperty("ccavenue.messagetype");
		this.CURRENCY_CODE = environment.getRequiredProperty("ccavenue.currency");
		this.CITIZEN_URL = environment.getRequiredProperty("ccavenue.default.citizen.url");
		ORIGINAL_RETURN_URL_KEY = environment.getRequiredProperty("ccavenue.original.return.url.key");
		this.COMMAND = "initiatTransaction";
		this.REQUEST_TYPE = "JSON";
		this.RESPONSE_TYPE = "JSON";

		User userInfo = User.builder().uuid("PG_DETAIL_GET").type("SYSTEM").roles(Collections.emptyList()).id(0L)
				.build();

		requestInfo = new RequestInfo("", "", 0L, "", "", "", "", "", "", userInfo);
//		this.pgDetailRepository = pgDetailRepository;
	}

	@Override
	public URI generateRedirectURI(Transaction transaction) {

		log.info("Inside CCAvenue generateRedirectURI()");
		Random random = new Random();
		int randomNumber = random.nextInt(90000000) + 10000000;
		String orderNumber = "CG" + randomNumber;
		Double amount = Double.parseDouble(transaction.getTxnAmount());
		String callBackUrl = transaction.getCallbackUrl();
//		String jsonData = "{ \"merchant_id\":\""+MERCHANT_ID+"\", \"order_id\":\"" + orderNumber + "\" }";
//		String jsonData = "{ \"merchant_id\":1941257, \"order_id\":\"" + orderNumber
//				+ "\" ,\"currency\":\"INR\",\"amount\":" + amount + "}";

//		String jsonData = "{ \"merchant_id\":" + MERCHANT_ID + ",\"order_id\":\"" + orderNumber
//				+ "\",\"currency\":\"INR\"," + "\"amount\":" + amount + "," + "\"redirect_url\":\""
//				+ callBackUrl + "\"," + "\"cancel_url\":\"" + callBackUrl + "\","
//				+ "\"language\":\"EN\",\"billing_name\":\"\",\"billing_address\":\"\","
//				+ "\"billing_city\":\"\",\"billing_state\":\"\",\"billing_zip\":\"\","
//				+ "\"billing_country\":\"\",\"billing_tel\":,\"billing_email\":\"\","
//				+ "\"delivery_name\":\"\",\"delivery_address\":\"\",\"delivery_city\":\"\","
//				+ "\"delivery_state\":\"\",\"delivery_zip\":\"\",\"delivery_country\":\"\","
//				+ "\"delivery_tel\":,\"merchant_param1\":\"\",\"merchant_param2\":\"\","
//				+ "\"merchant_param3\":\"\",\"merchant_param4\":\"\",\"merchant_param5\":\"\"}";
		String jsonData = "merchant_id=" + MERCHANT_ID + "&order_id=" + orderNumber + "&currency=INR&amount=" + amount
				+ "&redirect_url=" + RETURN_URL + "&cancel_url=" + RETURN_URL + ""
				+ "&language=EN&billing_name=&billing_address=&" + "billing_city=&billing_state=&billing_zip=&"
				+ "billing_country=&billing_tel=&billing_email=&" + "delivery_name=&delivery_address=&delivery_city="
				+ "&delivery_state=&delivery_zip=&delivery_country="
				+ "&delivery_tel=&merchant_param1=&merchant_param2="
				+ "&merchant_param3=&merchant_param4=&merchant_param5=";

//		String jsonData = "{ \"merchant_id\":\"" + MERCHANT_ID + "\",\"tid\":\"1673976281580\", \"order_id\":\"" + orderNumber
//				+ "\" ,\"currency\":\"INR\",\"amount\":\"1.00\","
//				+ "\"redirect_url\":\"https://www.niwaspass.com/digit-ui/citizen/payment\","
//				+ "\"cancel_url\":\"https://www.niwaspass.com/digit-ui/citizen/payment\"," + "\"language\":\"EN\"}";

		log.info("jsonData: " + jsonData);
		String encryptedJsonData = "";
		StringBuffer wsDataBuff = new StringBuffer();

		if (WORKING_KEY != null && !WORKING_KEY.equals("") && jsonData != null && !jsonData.equals("")) {
			CcavenueUtils ccavenueUtis = new CcavenueUtils(WORKING_KEY);
			encryptedJsonData = ccavenueUtis.encrypt(jsonData);
		}
		wsDataBuff.append("encRequest=" + encryptedJsonData + "&access_code=" + ACCESS_CODE);
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
			url = new URL(WS_URL + "&" + wsDataBuff);

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
		        queryMap.put(ORDER_ID_KEY, transaction.getTxnId());
		        queryMap.put(CUSTOMER_ID_KEY, transaction.getUser().getUuid());
		        queryMap.put(TRANSACTION_AMOUNT_KEY, String.valueOf( transaction.getTxnAmount()));
		        queryMap.put(CURRENCY_CODE_KEY,CURRENCY_CODE);
		        SimpleDateFormat format = new SimpleDateFormat(TX_DATE_FORMAT);
		        Date currentDate = new Date();
		        queryMap.put(REQUEST_DATE_TIME_KEY, format.format(currentDate));
		        String returnUrl = transaction.getCallbackUrl().replace(CITIZEN_URL, "");

		        queryMap.put(SERVICE_ID_KEY, getModuleCode(transaction));
		        String domainName =  returnUrl.replaceAll("http(s)?://|www\\.|/.*", "");
		        String citizenReturnURL = returnUrl.split(domainName)[1];
		        log.info("returnUrl::::"+getReturnUrl(citizenReturnURL, REDIRECT_URL));
		        queryMap.put(SUCCESS_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
		        queryMap.put(FAIL_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
		        StringBuffer userDetail = new StringBuffer();
		        if( transaction.getUser()!=null) {
		            if(!StringUtils.isEmpty(transaction.getUser().getMobileNumber())) {
		                userDetail.append(transaction.getUser().getMobileNumber());
		            }

		            /*
		             * if(!StringUtils.isEmpty(transaction.getUser().getEmailId())) { if(userDetail.length()>0) { userDetail.append("^");
		             * } userDetail.append(transaction.getUser().getEmailId()); }
		             */
		        }
		        if(userDetail.length() == 0) {
		            userDetail.append(ADDITIONAL_FIELD_VALUE);
		        }
		        queryMap.put(ADDITIONAL_FIELD1_KEY, userDetail.toString());
		        queryMap.put(ADDITIONAL_FIELD2_KEY, ADDITIONAL_FIELD_VALUE); //Not in use
		        queryMap.put(ADDITIONAL_FIELD3_KEY, ADDITIONAL_FIELD_VALUE); //Not in use
		        queryMap.put(ADDITIONAL_FIELD4_KEY, transaction.getConsumerCode());
		        queryMap.put(ADDITIONAL_FIELD5_KEY, getModuleCode(transaction));



		        //Generate Checksum for params
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
		        queryMap.put("txURL",WS_URL + "&" + wsDataBuff);
		        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyyHH:mm:SSS");
		        queryMap.put(REQUEST_DATE_TIME_KEY, format1.format(currentDate));
		        log.info("REQUEST_DATE_TIME_KEY::"+queryMap.get(REQUEST_DATE_TIME_KEY));
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
		        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(WS_URL + "&" + wsDataBuff).queryParams(params)
		                .build();
				
				
				
				
				
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
		PayuResponse resp = objectMapper.convertValue(params, PayuResponse.class);
		if (!isNull(resp.getHash()) && !isNull(resp.getStatus()) && !isNull(resp.getTxnid())
				&& !isNull(resp.getAmount()) && !isNull(resp.getProductinfo()) && !isNull(resp.getFirstname())) {
			resp.setTransaction_amount(resp.getAmount());
			String checksum = resp.getHash();

			String hashSequence = "SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|";
			hashSequence = hashSequence.concat(ACCESS_CODE);
			hashSequence = hashSequence.replace("SALT", WORKING_KEY);
			hashSequence = hashSequence.replace("status", resp.getStatus());
			hashSequence = hashSequence.replace("udf5", resp.getUdf5());
			hashSequence = hashSequence.replace("udf4", resp.getUdf4());
			hashSequence = hashSequence.replace("udf3", resp.getUdf3());
			hashSequence = hashSequence.replace("udf2", resp.getUdf2());
			hashSequence = hashSequence.replace("udf1", resp.getUdf1());
			hashSequence = hashSequence.replace("email", resp.getEmail());
			hashSequence = hashSequence.replace("firstname", resp.getFirstname());
			hashSequence = hashSequence.replace("productinfo", resp.getProductinfo());
			hashSequence = hashSequence.replace("amount", resp.getTransaction_amount());
			hashSequence = hashSequence.replace("txnid", resp.getTxnid());
			String hash = hashCal(hashSequence);

			if (checksum.equalsIgnoreCase(hash)) {
				Transaction txn = transformRawResponse(resp, currentStatus);
				if (txn.getTxnStatus().equals(Transaction.TxnStatusEnum.PENDING)
						|| txn.getTxnStatus().equals(Transaction.TxnStatusEnum.FAILURE)) {
					return txn;
				}
			}
		}

		return fetchStatusFromGateway(currentStatus);
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

	private Transaction transformRawResponse(PayuResponse resp, Transaction currentStatus) {

		Transaction.TxnStatusEnum status;

		String gatewayStatus = resp.getStatus();

		if (gatewayStatus.equalsIgnoreCase("success")) {
			status = Transaction.TxnStatusEnum.SUCCESS;
			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.getTransaction_amount())
					.txnStatus(status).gatewayTxnId(resp.getMihpayid()).gatewayPaymentMode(resp.getMode())
					.gatewayStatusCode(resp.getUnmappedstatus()).gatewayStatusMsg(resp.getStatus()).responseJson(resp)
					.build();
		} else {
			status = Transaction.TxnStatusEnum.FAILURE;
			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.getTransaction_amount())
					.txnStatus(status).gatewayTxnId(resp.getMihpayid()).gatewayStatusCode(resp.getError_code())
					.gatewayStatusMsg(resp.getError_Message()).responseJson(resp).build();
		}

	}

	private Transaction fetchStatusFromGateway(Transaction currentStatus) {

		String txnRef = currentStatus.getTxnId();
		String hash = hashCal(ACCESS_CODE + "|" + "verify_payment" + "|" + txnRef + "|" + WORKING_KEY);

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.add("form", "2");

		UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host(MERCHANT_URL_STATUS)
				.path(MERCHANT_PATH_STATUS).queryParams(queryParams).build();

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("key", ACCESS_CODE);
			params.add("command", "verify_payment");
			params.add("hash", hash);
			params.add("var1", txnRef);

			HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(uriComponents.toUriString(), entity,
					String.class);

			log.info(response.getBody());

			JsonNode payuRawResponse = objectMapper.readTree(response.getBody());
			JsonNode status = payuRawResponse.path("transaction_details").path(txnRef);

			if (status.isNull())
				throw new CustomException("FAILED_TO_FETCH_STATUS_FROM_GATEWAY",
						"Unable to fetch status from payment gateway for txnid: " + currentStatus.getTxnId());
			PayuResponse payuResponse = objectMapper.treeToValue(status, PayuResponse.class);

			return transformRawResponse(payuResponse, currentStatus);

		} catch (RestClientException | IOException e) {
			log.error("Unable to fetch status from payment gateway for txnid: " + currentStatus.getTxnId(), e);
			throw new ServiceCallException("Error occurred while fetching status from payment gateway");
		}
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
	        String moduleCode ="------";
	        if(!StringUtils.isEmpty(transaction.getModule())) {
	            /*
	             * if(transaction.getModule().length() < 6) { moduleCode= transaction.getModule() +
	             * moduleCode.substring(transaction.getModule().length()-1); }else { moduleCode =transaction.getModule(); }
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
        return UriComponentsBuilder.fromHttpUrl(baseurl).queryParam(ORIGINAL_RETURN_URL_KEY, callbackUrl).build().toUriString();
    }

}