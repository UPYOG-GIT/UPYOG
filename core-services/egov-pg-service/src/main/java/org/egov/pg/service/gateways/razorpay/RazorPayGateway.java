package org.egov.pg.service.gateways.razorpay;

import static java.util.Objects.isNull;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.Transaction;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.service.Gateway;
import org.egov.pg.service.TransactionService;
import org.egov.pg.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;


import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RazorPayGateway implements Gateway {

//	@Autowired
	private TransactionService transactionService;

//	@Autowired
//	private TransactionsApiController transactionsApiController;

	private final String GATEWAY_NAME = "RAZORPAY";
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
	private String WS_URL;
	private final boolean ACTIVE;

	private RestTemplate restTemplate;
	private ObjectMapper objectMapper;

	private final String MESSAGE_TYPE;

	private final String CURRENCY_CODE;
	private final String REDIRECT_URL;
	private final String ORIGINAL_RETURN_URL_KEY;
	
	@Value("${razorpay.url.checkout:https://checkout.razorpay.com/v1/checkout.js}")
	private final String CHECKOUT_URL;
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
	
	
	private final String ORDER_URL = "https://api.razorpay.com/v1/orders";
	@Autowired
	public RazorPayGateway(RestTemplate restTemplate, Environment environment, ObjectMapper objectMapper,
			PgDetailRepository pgDetailRepository) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
//		this.transactionService = transactionService;
		this.ACTIVE = Boolean.valueOf(environment.getRequiredProperty("ccavenue.active"));
//		this.ACCESS_CODE = environment.getRequiredProperty("ccavenue.access.code");
//		this.WORKING_KEY = environment.getRequiredProperty("ccavenue.working.key");
		CHECKOUT_URL = environment.getRequiredProperty("razorpay.url.checkout");
		this.MERCHANT_URL_PAY = environment.getRequiredProperty("ccavenue.url");
		this.MERCHANT_URL_STATUS = environment.getRequiredProperty("payu.url.status");
		this.MERCHANT_PATH_PAY = environment.getRequiredProperty("payu.path.pay");
		this.MERCHANT_PATH_STATUS = environment.getRequiredProperty("payu.path.status");
//		this.MERCHANT_ID = environment.getRequiredProperty("ccavenue.merchant.id");
//		this.WS_URL = environment.getRequiredProperty("ccavenue.path.wsurl");
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

//		log.info("Inside Razorpay generateRedirectURI()");
////		Random random = new Random();
////		int randomNumber = random.nextInt(90000000) + 10000000;
//
//		String tenantId = transaction.getTenantId();
//
//		// set MerchantId, WorkingKey and AccessKey according to tenantId
//		setGatewayDetails(tenantId);
//
//		String keyId = ACCESS_CODE;
//		String keySecret = WORKING_KEY;
//
//		log.info("MERCHANT_ID: " + MERCHANT_ID + ", WORKING_KEY: " + WORKING_KEY + ", ACCESS_CODE: " + ACCESS_CODE);
//
//		log.info("transaction.getTxnId() : " + transaction.getTxnId());
//		String orderNumber = transaction.getTxnId();
////		Double amount = Double.parseDouble(transaction.getTxnAmount());
//		Double amountValue = Double.parseDouble(transaction.getTxnAmount());
//		String callBackUrl = transaction.getCallbackUrl();
//
//		long amount = Math.round(amountValue * 100);
//
//		String requestString = "amount=" + amount + "&currency=INR&receipt=" + orderNumber + "&payment_capture=1";
//
//		log.info("requestString : " + requestString);
//		String encryptedJsonData = "";
//		StringBuffer wsDataBuff = new StringBuffer();
//
//		String auth = ACCESS_CODE + ":" + WORKING_KEY;
//		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
//		String authHeader = "Basic " + encodedAuth;
//
//		if (WORKING_KEY != null && !WORKING_KEY.equals("") && requestString != null && !requestString.equals("")) {
//			RazorPayUtils RazorPayUtils = new RazorPayUtils(WORKING_KEY);
//			encryptedJsonData = RazorPayUtils.encrypt(requestString);
//		}
//
//		String[] pairs = requestString.split("&");
//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//
//		for (String pair : pairs) {
//			String[] keyVal = pair.split("=");
//			params.add(keyVal[0], keyVal[1]);
//		}
//
//		params.add("authorization", authHeader);
//
//		String urlString = "https://api.razorpay.com/v1/orders";
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.set("Authorization", authHeader);
//		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//		HttpEntity<String> entity = new HttpEntity<>(requestString, headers);
//
//		try {
//
//			RazorpayClient client = new RazorpayClient(keyId, keySecret);
//
//			JSONObject orderRequest = new JSONObject();
//			orderRequest.put("amount", amount);
//			orderRequest.put("currency", "INR");
//			orderRequest.put("receipt", transaction.getTxnId());
//			orderRequest.put("payment_capture", 1);
//
//			Order order = client.orders.create(orderRequest);
//
//			String orderId = order.get("id");
//
//			log.info("Razorpay Order: " + order.toString());
//
//			insertOrderDetails(transaction.getTxnId(), orderId);
//
//			// Build redirect URI containing JSON data
//			String json = URLEncoder.encode("{\"orderId\":\"" + orderId + "\",\"amount\":" + amount
//					+ ",\"currency\":\"INR\",\"key\":\"" + keyId + "\"}", "UTF-8");
//
//			URI redirect = new URI("razorpay://order?data=" + json);
//
//			log.info("redirect : " + redirect.toString());
//
//			return redirect;
//		} catch (Exception ex) {
//			throw new RuntimeException("Error creating Razorpay order", ex);
//		}
		
		// 
			try {
				
				//  Step 1: Generate form data
					String formData = generateRedirectFormData(transaction);
					// Step 2: Return checkout URL with order details
			        // The actual checkout will be handled by frontend using Razorpay Checkout.js
			        return URI.create(CHECKOUT_URL);
			}
	        catch (Exception ex) {
	            log.error("Razorpay order creation failed", ex);
	            throw new CustomException("ORDER_CREATION_FAILED", "Failed to create Razorpay order");
	        }
	}
	

	@Override
	public String generateRedirectFormData(Transaction transaction) {
        try {
		log.info("inside RazorPay.generateRedirectFormData()");
//		PgDetail pgDetail = pgDetailRepository.getPgDetailByTenantId(requestInfo, transaction.getTenantId());

		/*
		 *
		 * messageType|merchantId|serviceId|orderId|customerId|transactionAmount|
		 * currencyCode|r equestDateTime|successUrl|failUrl|additionalField1|
		 * additionalField2| additionalField3| additionalField4| additionalField5
		 */
		// Create Razorpay Order
        String orderId = createRazorpayOrder(transaction);
      
        // Generate checkout options as JSON
        Map<String, Object> options = transaction.getAdditionalDetails() == null ? new HashMap<>() : (Map<String, Object>) transaction.getAdditionalDetails();
        
        options.put("key",ACCESS_CODE);
        options.put(TRANSACTION_AMOUNT_KEY , String.valueOf(transaction.getTxnAmount()));
        options.put("currency", "INR");
        options.put(ORDER_ID_KEY, orderId);
        options.put("name", "HDFC collect now");
        options.put("description", transaction.getModule());
        
        Map<String, String> prefill = new HashMap<>();
        prefill.put("email", transaction.getUser().getEmailId());
        prefill.put("contact", transaction.getUser().getMobileNumber());
        options.put("prefill", prefill);
        
        Map<String, String> notes = new HashMap<>();
        notes.put("transaction_id", transaction.getTxnId());
        options.put("notes", notes);
        
        /*
        Build backend callback URL that Razorpay will post to after payment.
         It wraps the original citizen success URL so RedirectController can
         process the response and then safely redirect the user to the frontend.

         Example output:
         https://assamuat.niua.in/pg-service/transaction/v1/_redirect
             ?originalreturnurl=https://assamuat.niua.in/citizen/payment/success/BPA.PLANNING_PERMIT_FEE/PG-BP-2025-11-26-000640/as.ghoungoorgp
             &eg_pg_txnid=PG_PG_2025_11_26_000640_21
             */
        String originalReturnUrl = transaction.getCallbackUrl();
        String callbackUrl = UriComponentsBuilder.fromHttpUrl(REDIRECT_URL)
                .queryParam(ORIGINAL_RETURN_URL_KEY, originalReturnUrl)
                .queryParam(ORDER_ID_KEY, transaction.getTxnId())
                .build()
                .encode()
                .toUriString();
        
        options.put("callbackUrl",callbackUrl);
        transaction.setAdditionalDetails(options);
        
        String data = Utils.convertObjectToString(objectMapper , options);
        log.info("Razorpay checkout data: {}", data);
        return data ;
	  }
        catch (Exception ex) {
            log.error("Failed to generate Razorpay form data", ex);
            throw new CustomException("FORM_DATA_GENERATION_FAILED", "Failed to generate checkout data");
        }
        
        
//		String tenantId = transaction.getTenantId();

		// set MerchantId, WorkingKey and AccessKey according to tenantId
//		setGatewayDetails(tenantId);
//		log.info("generateRedirectFormData: MERCHANT_ID: " + MERCHANT_ID + ", WORKING_KEY: " + WORKING_KEY
//				+ ", ACCESS_CODE: " + ACCESS_CODE);
//		String urlData = null;
//		HashMap<String, String> queryMap = new HashMap<>();
//		queryMap.put(MESSAGE_TYPE_KEY, MESSAGE_TYPE);
//		queryMap.put(MERCHANT_ID_KEY, MERCHANT_ID);
//		queryMap.put(SERVICE_ID_KEY, getModuleCode(transaction));
//		queryMap.put(ORDER_ID_KEY, transaction.getTxnId());
//		queryMap.put(CUSTOMER_ID_KEY, transaction.getUser().getUuid());
//		queryMap.put(TRANSACTION_AMOUNT_KEY, String.valueOf(transaction.getTxnAmount()));
//		queryMap.put(CURRENCY_CODE_KEY, CURRENCY_CODE);
//		SimpleDateFormat format = new SimpleDateFormat(TX_DATE_FORMAT);
//		queryMap.put(REQUEST_DATE_TIME_KEY, format.format(new Date()));
//		String returnUrl = transaction.getCallbackUrl().replace(CITIZEN_URL, "");
//
//		String domainName = returnUrl.replaceAll("http(s)?://|www\\.|/.*", "");
//		String citizenReturnURL = returnUrl.split(domainName)[1];
//		String moduleCode = "------";
//		if (!StringUtils.isEmpty(transaction.getModule())) {
//			if (transaction.getModule().length() < 6) {
//				moduleCode = transaction.getModule() + moduleCode.substring(transaction.getModule().length() - 1);
//			} else {
//				moduleCode = transaction.getModule();
//			}
//		}
//
//		log.info("returnUrl::::" + getReturnUrl(citizenReturnURL, REDIRECT_URL));
//		queryMap.put(SUCCESS_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
//		queryMap.put(FAIL_URL_KEY, getReturnUrl(citizenReturnURL, REDIRECT_URL));
//		StringBuffer userDetail = new StringBuffer();
//		if (transaction.getUser() != null) {
//			if (!StringUtils.isEmpty(transaction.getUser().getMobileNumber())) {
//				userDetail.append(transaction.getUser().getMobileNumber());
//			}
//
//			/*
//			 * if(!StringUtils.isEmpty(transaction.getUser().getEmailId())) {
//			 * if(userDetail.length()>0) { userDetail.append("^"); }
//			 * userDetail.append(transaction.getUser().getEmailId()); }
//			 */
//		}
//		if (userDetail.length() == 0) {
//			userDetail.append(ADDITIONAL_FIELD_VALUE);
//		}
//		queryMap.put(ADDITIONAL_FIELD1_KEY, userDetail.toString());
//		queryMap.put(ADDITIONAL_FIELD2_KEY, ADDITIONAL_FIELD_VALUE); // Not in use
//		queryMap.put(ADDITIONAL_FIELD3_KEY, ADDITIONAL_FIELD_VALUE); // Not in use
//		queryMap.put(ADDITIONAL_FIELD4_KEY, transaction.getConsumerCode());
//		queryMap.put(ADDITIONAL_FIELD5_KEY, moduleCode);
//
//		// Generate Checksum for params
//		ArrayList<String> fields = new ArrayList<String>();
//		fields.add(queryMap.get(MESSAGE_TYPE_KEY));
//		fields.add(queryMap.get(MERCHANT_ID_KEY));
//		fields.add(queryMap.get(SERVICE_ID_KEY));
//		fields.add(queryMap.get(ORDER_ID_KEY));
//		fields.add(queryMap.get(CUSTOMER_ID_KEY));
//		fields.add(queryMap.get(TRANSACTION_AMOUNT_KEY));
//		fields.add(queryMap.get(CURRENCY_CODE_KEY));
//		fields.add(queryMap.get(REQUEST_DATE_TIME_KEY));
//		fields.add(queryMap.get(SUCCESS_URL_KEY));
//		fields.add(queryMap.get(FAIL_URL_KEY));
//		fields.add(queryMap.get(ADDITIONAL_FIELD1_KEY));
//		fields.add(queryMap.get(ADDITIONAL_FIELD2_KEY));
//		fields.add(queryMap.get(ADDITIONAL_FIELD3_KEY));
//		fields.add(queryMap.get(ADDITIONAL_FIELD4_KEY));
//		fields.add(queryMap.get(ADDITIONAL_FIELD5_KEY));
//
//		String message = String.join("|", fields);
//		queryMap.put("checksum", RazorPayUtils.generateCRC32Checksum(message, WORKING_KEY));
//		queryMap.put("txURL", WS_URL);
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			urlData = mapper.writeValueAsString(queryMap);
//		} catch (Exception e) {
//			log.error("RazorPay URL generation failed", e);
//			throw new CustomException("URL_GEN_FAILED",
//					"RazorPay URL generation failed, gateway redirect URI cannot be generated");
//		}
//		return urlData;
	}

	public String createRazorpayOrder(Transaction transaction) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBasicAuth(ACCESS_CODE ,WORKING_KEY);
			
			log.info("Key Id and Secrets coming from Env: {} {}", ACCESS_CODE, WORKING_KEY);
			Map<String, Object> orderRequest = new HashMap<>();
			String amtAsPaise = Utils.formatAmtAsPaise(transaction.getTxnAmount());
            orderRequest.put("amount", amtAsPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", transaction.getTxnId());
            orderRequest.put("payment_capture", 1); // Auto capture
            
            log.info("Razorpay order creation request: {}", orderRequest);
            log.info("Razorpay headers: {}", headers);
            
            String ORDER_URL = "https://api.razorpay.com/v1/orders";
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderRequest, headers);
            ResponseEntity<RazorPayResponse> response = restTemplate.postForEntity(
                    ORDER_URL, request, RazorPayResponse.class);
            
            log.info("Razorpay order creation response: {}", response);
            if (response.getBody() != null) {
                return response.getBody().getId();
            }
            throw new CustomException("ORDER_CREATION_FAILED", "Failed to create Razorpay order");
	} catch (RestClientException ex) {
        log.error("Failed to create Razorpay order", ex);
        throw new CustomException("ORDER_CREATION_FAILED", "Failed to create Razorpay order");
    }
	}
	

	@Override
	public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
//		log.info("inside RazorPayGateway.fetchStatus().....");
////		CcavenueResponse resp = objectMapper.convertValue(params, CcavenueResponse.class);
//
//		Map<String, String> resMap = new HashMap<String, String>();
//		return fetchStatusFromGateway(currentStatus, resMap);
////		return txn;
		try {
            /// TODO:- Need to explore this verySignature and then unComment it
            // Verify signature
           /* if (!verifySignature(orderId, paymentId, signature)) {
                throw new CustomException("SIGNATURE_VERIFICATION_FAILED", "Payment signature verification failed");
            }*/
		Map<String, Object> additionalDetails = objectMapper.convertValue(currentStatus.getAdditionalDetails(), Map.class);;
        String orderId = additionalDetails.get("order_id").toString();
        log.info("Fetching Razorpay payment status for Order ID: {}", orderId);
     // Fetch payment details
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(ACCESS_CODE ,WORKING_KEY);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String orderUrl = ORDER_URL + "/" + orderId;
        ResponseEntity<RazorpayPaymentResponse> response = restTemplate.exchange(
                orderUrl, org.springframework.http.HttpMethod.GET, request, RazorpayPaymentResponse.class);

        log.info("Razorpay payment fetch response: {}", response);

        return transformRawResponse(response.getBody(), currentStatus);
	  }
	  catch (RestClientException e) {
        log.error("Unable to fetch status from Razorpay gateway", e);
        throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from Razorpay gateway");
       }
	}
		private boolean verifySignature(String orderId, String paymentId, String signature) {
	        try {
	            String payload = orderId + "|" + paymentId;
	            Mac mac = Mac.getInstance("HmacSHA256");
	            SecretKeySpec secretKeySpec = new SecretKeySpec(ACCESS_CODE.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	            mac.init(secretKeySpec);
	            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
	            String expectedSignature = bytesToHex(hash);
	            return expectedSignature.equals(signature);
	        } catch (Exception e) {
	            log.error("Signature verification failed", e);
	            return false;
	        }
	    }
		private String bytesToHex(byte[] bytes) {
	        StringBuilder result = new StringBuilder();
	        for (byte b : bytes) {
	            result.append(String.format("%02x", b));
	        }
	        return result.toString();
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
	
	private Transaction transformRawResponse(RazorpayPaymentResponse resp, Transaction currentStatus) {
		log.info("inside RazorPayGateway.transformRawResponse().....");
		Transaction.TxnStatusEnum status = Transaction.TxnStatusEnum.PENDING;
//		Transaction.TxnStatusEnum status;
		if ("captured".equalsIgnoreCase(resp.getStatus())) {
            status = Transaction.TxnStatusEnum.SUCCESS;
    } else if ("failed".equalsIgnoreCase(resp.getStatus())) {
        status = Transaction.TxnStatusEnum.FAILURE;
    } else if ("paid".equalsIgnoreCase(resp.getStatus())) {
        status = Transaction.TxnStatusEnum.SUCCESS;
    }
		
		 return Transaction.builder()
	                .txnId(currentStatus.getTxnId())
	                .txnAmount(Utils.formatAmtAsRupee(String.valueOf(resp.getAmount() / 100))) // Convert from paise
	                .txnStatus(status)
	                .gatewayTxnId(resp.getId())
	                .gatewayPaymentMode(resp.getMethod())
	                .gatewayStatusCode(resp.getStatus())
	                .gatewayStatusMsg(resp.getDescription())
	                .responseJson(resp)
	                .build();
	    }

//		String gatewayStatus = resp.get("order_status");
////		String trackerStatus = resp.get("status");
//		log.info("gatewayStatus: " + gatewayStatus);
//
//		if (gatewayStatus.equalsIgnoreCase("success") || gatewayStatus.equalsIgnoreCase("Shipped")) {
//			status = Transaction.TxnStatusEnum.SUCCESS;
//			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.get("amount")).txnStatus(status)
//					.gatewayTxnId(resp.get("tracking_id")).gatewayPaymentMode(resp.get("payment_mode"))
//					.gatewayStatusCode(resp.get("status_code")).gatewayStatusMsg(resp.get("status_message"))
//					.responseJson(resp).build();
//		} else {
//			status = Transaction.TxnStatusEnum.FAILURE;
//			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.get("amount")).txnStatus(status)
//					.gatewayTxnId(resp.get("tracking_id")).gatewayStatusCode(resp.get("status_code"))
//					.gatewayStatusMsg(resp.get("failure_message")).responseJson(resp).build();
//		}

//	}

	
	
	
	
	//Old code change it later 
//	private Transaction transformRawResponse(Map<String, String> resp, Transaction currentStatus) {
//		log.info("inside RazorPayGateway.transformRawResponse().....");
//		Transaction.TxnStatusEnum status;
//
//		String gatewayStatus = resp.get("order_status");
////		String trackerStatus = resp.get("status");
//		log.info("gatewayStatus: " + gatewayStatus);
//
//		if (gatewayStatus.equalsIgnoreCase("success") || gatewayStatus.equalsIgnoreCase("Shipped")) {
//			status = Transaction.TxnStatusEnum.SUCCESS;
//			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.get("amount")).txnStatus(status)
//					.gatewayTxnId(resp.get("tracking_id")).gatewayPaymentMode(resp.get("payment_mode"))
//					.gatewayStatusCode(resp.get("status_code")).gatewayStatusMsg(resp.get("status_message"))
//					.responseJson(resp).build();
//		} else {
//			status = Transaction.TxnStatusEnum.FAILURE;
//			return Transaction.builder().txnId(currentStatus.getTxnId()).txnAmount(resp.get("amount")).txnStatus(status)
//					.gatewayTxnId(resp.get("tracking_id")).gatewayStatusCode(resp.get("status_code"))
//					.gatewayStatusMsg(resp.get("failure_message")).responseJson(resp).build();
//		}
//
//	}

	
//	private Transaction fetchStatusFromGateway(Transaction currentStatus, Map<String, String> resMap) {
//		log.info("inside RazorPayGateway.fetchStatusFromGateway().....");
//
////		String refNo = resMap.get("tracking_id");
////		String orderNo = resMap.get("order_id");
//
//		String txnId = currentStatus.getTxnId();
//
//		String tenantId = currentStatus.getTenantId();
//
//		Map<String, String> responseMap = new HashMap<String, String>();
//
//		String orderId = getOrderDetails(txnId);
//		setGatewayDetails(tenantId);
//		String keyId = ACCESS_CODE;
//		String keySecret = WORKING_KEY;
//
//		RazorpayClient client;
//		List<Payment> payments = new ArrayList<>();
//		try {
//			client = new RazorpayClient(keyId, keySecret);
//			payments = client.orders.fetchPayments(orderId);
//
//			// Get first payment
//			Payment payment = payments.get(0);
//
//			String paymentId = payment.get("id");
//			String status = payment.get("status");
//
//			log.info("Payment ID: " + paymentId);
//			log.info("Payment Status: " + status);
//
//			Payment payment1 = client.payments.fetch(paymentId);
//			log.info("payment1 " + payments.toString());
//
//			String status1 = payment1.get("status"); // captured / authorized / failed
//			String method = payment1.get("method");
//			Integer amountInt = (Integer) payment1.get("amount");
//			Double amount = amountInt.doubleValue();
//			String amount1 = String.valueOf(amount / 100);
//
//			if (status1.equalsIgnoreCase("Captured")) {
//				responseMap.put("order_status", "Success");
//				responseMap.put("payment_mode", method);
//				responseMap.put("status_message", status);
//			} else {
//				responseMap.put("order_status", "Failure");
//				responseMap.put("error_desc", payment1.get("error_description"));
//				responseMap.put("tracking_id", "");
//			}
//
//			responseMap.put("amount", amount1);
//			responseMap.put("tracking_id", payment1.get("id"));
//
//		} catch (RazorpayException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		if (payments == null || payments.isEmpty()) {
//			log.info("No payments found for order: " + orderId);
////		    return;
//		}
//
//		return transformRawResponse(responseMap, currentStatus);
//
////		return currentStatus;
//
//	}

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

		// Testing Details
//		this.MERCHANT_ID = "1941257";
//		this.ACCESS_CODE = "ATII96KA89BB16IIBB";
//		this.WORKING_KEY = "D682025F99E01FA0F0FAA079B1B3F793";

//		Map<String, Object> ccAvenueDetails = transactionService.getCcavenueDetails(tenantId);
//		Map<String, Object> ccAvenueDetails = transactionsApiController.getCcavenueDetails(tenantId);

		Map<String, Object> RazorPayDetails = pgDetailRepository.getCcavenueDetails(tenantId, "RAZORPAY");
		this.MERCHANT_ID = RazorPayDetails.get("merchant_id").toString();
		this.ACCESS_CODE = RazorPayDetails.get("access_code").toString();
		this.WORKING_KEY = RazorPayDetails.get("working_key").toString();
		this.WS_URL = RazorPayDetails.get("gateway_url").toString();
	}

	private void insertOrderDetails(String txnId, String orderId) {
		pgDetailRepository.insertRazorPayOrder(txnId, orderId);
	}

	private String getOrderDetails(String txnId) {
		Map<String, Object> orderIdMap = pgDetailRepository.getRazorPayOrderDetail(txnId);
		String orderId = orderIdMap.get("order_id").toString();
		return orderId;
	}

}