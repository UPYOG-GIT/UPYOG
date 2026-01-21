package org.egov.pg.service.gateways.razorpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.Transaction;
import org.egov.pg.service.Gateway;
import org.egov.pg.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.egov.pg.repository.PgDetailRepository;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RazorPayGateway implements Gateway {

    private static final String GATEWAY_NAME = "RAZORPAY";
    private String KEY_ID;
    private String KEY_SECRET;
    private final String ORDER_URL;
    private final String PAYMENT_URL;
    private final String CHECKOUT_URL;
    private final boolean ACTIVE;
    private  String ACCESS_CODE;
    private  String WORKING_KEY;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String REDIRECT_URL;
    private final String ORIGINAL_RETURN_URL_KEY;
    private PgDetailRepository pgDetailRepository;


    @Autowired
    public RazorPayGateway(RestTemplate restTemplate, Environment environment, ObjectMapper objectMapper, String access_CODE) {
        this.ACCESS_CODE = access_CODE;
		this.WORKING_KEY = "";
		this.restTemplate = restTemplate;

        ACTIVE = Boolean.parseBoolean(environment.getRequiredProperty("razorpay.active"));
        KEY_ID = environment.getRequiredProperty("razorpay.key.id");
        KEY_SECRET = environment.getRequiredProperty("razorpay.key.secret");
        ORDER_URL = environment.getRequiredProperty("razorpay.url.order");
        PAYMENT_URL = environment.getRequiredProperty("razorpay.url.payment");
        CHECKOUT_URL = environment.getRequiredProperty("razorpay.url.checkout");
        REDIRECT_URL = environment.getRequiredProperty("razorpay.redirect.url");
        ORIGINAL_RETURN_URL_KEY = environment.getRequiredProperty("razorpay.original.return.url.key");
        this.objectMapper = objectMapper;
    }

//    
    
    @Override
    public URI generateRedirectURI(Transaction transaction) {
        try {
            // Step 1: Generate form data
            String formData = generateRedirectFormData(transaction);

            // Step 2: Return checkout URL with order details
            // The actual checkout will be handled by frontend using Razorpay Checkout.js
            return URI.create(CHECKOUT_URL);
        } catch (Exception e) {
            log.error("Razorpay order creation failed", e);
            throw new CustomException("ORDER_CREATION_FAILED", "Failed to create Razorpay order");
        }
    }
     
    
    @Override
    public String generateRedirectFormData(Transaction transaction) {
        try {
            // Create Razorpay Order
            String orderId = createRazorpayOrder(transaction);

            // Generate checkout options as JSON
            Map<String, Object> options = transaction.getAdditionalDetails() == null ? new HashMap<>() : (Map<String, Object>) transaction.getAdditionalDetails();
            options.put("key", KEY_ID);
            String amtAsPaise = Utils.formatAmtAsPaise(transaction.getTxnAmount());
            options.put("amount", amtAsPaise);
            options.put("currency", "INR");
            options.put("order_id", orderId);
            options.put("name", "HDFC Collect Now");
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
                    .queryParam(PgConstants.PG_TXN_IN_LABEL, transaction.getTxnId())
                    .build()
                    .encode()
                    .toUriString();

            options.put("callback_url", callbackUrl);
            transaction.setAdditionalDetails(options);
            String data = Utils.convertObjectToString(objectMapper, options);
            log.info("Razorpay checkout data: {}", data);

            return data;
        } catch (Exception e) {
            log.error("Failed to generate Razorpay form data", e);
            throw new CustomException("FORM_DATA_GENERATION_FAILED", "Failed to generate checkout data");
        }
    }

    private String createRazorpayOrder(Transaction transaction) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(KEY_ID, KEY_SECRET);

            log.info("Key Id and Secrets coming from Env: {} {}", KEY_SECRET, KEY_ID);
            Map<String, Object> orderRequest = new HashMap<>();
            String amtAsPaise = Utils.formatAmtAsPaise(transaction.getTxnAmount());
            orderRequest.put("amount", amtAsPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", transaction.getTxnId());
            orderRequest.put("payment_capture", 1); // Auto capture

            log.info("Razorpay order creation request: {}", orderRequest);
            log.info("Razorpay headers: {}", headers);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderRequest, headers);
            ResponseEntity<RazorPayResponse> response = restTemplate.postForEntity(
                    ORDER_URL, request, RazorPayResponse.class);
            log.info("Razorpay order creation response: {}", response);
            if (response.getBody() != null) {
                return response.getBody().getId();
            }
            throw new CustomException("ORDER_CREATION_FAILED", "Failed to create Razorpay order");
        } catch (RestClientException e) {
            log.error("Failed to create Razorpay order", e);
            throw new CustomException("ORDER_CREATION_FAILED", "Failed to create Razorpay order");
        }
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
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
            headers.setBasicAuth(KEY_ID, KEY_SECRET);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String orderUrl = ORDER_URL + "/" + orderId;
            ResponseEntity<RazorpayPaymentResponse> response = restTemplate.exchange(
                    orderUrl, org.springframework.http.HttpMethod.GET, request, RazorpayPaymentResponse.class);

            log.info("Razorpay payment fetch response: {}", response);

            return transformRawResponse(response.getBody(), currentStatus);

        } catch (RestClientException e) {
            log.error("Unable to fetch status from Razorpay gateway", e);
            throw new CustomException("UNABLE_TO_FETCH_STATUS", "Unable to fetch status from Razorpay gateway");
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
        return "razorpay_payment_id";
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
//        this.MERCHANT_ID = RazorPayDetails.get("merchant_id").toString();
        this.ACCESS_CODE = RazorPayDetails.get("access_code").toString();
        this.WORKING_KEY = RazorPayDetails.get("working_key").toString();
//        this.WS_URL = RazorPayDetails.get("gateway_url").toString();
        KEY_ID = ACCESS_CODE;
        KEY_SECRET = WORKING_KEY;
    }

    private void insertOrderDetails(String txnId, String orderId) {
        pgDetailRepository.insertRazorPayOrder(txnId, orderId);
    }

    private String getOrderDetails(String txnId) {
        Map<String, Object> orderIdMap = pgDetailRepository.getRazorPayOrderDetail(txnId);
        String orderId = orderIdMap.get("order_id").toString();
        return orderId;
    }

   
    
    private Transaction transformRawResponse(RazorpayPaymentResponse resp, Transaction currentStatus) {
        Transaction.TxnStatusEnum status = Transaction.TxnStatusEnum.PENDING;

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
}