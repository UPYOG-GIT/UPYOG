package org.egov.pg.service.gateways.payu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.models.Transaction;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class PayuGateway implements Gateway {

    private final String GATEWAY_NAME = "PAYU";
    private final String MERCHANT_KEY;
    private final String MERCHANT_SALT;
    private final String MERCHANT_URL_PAY;
    private final String MERCHANT_URL_STATUS;
    private final String MERCHANT_PATH_PAY;
    private final String MERCHANT_PATH_STATUS;
    private final boolean ACTIVE;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Autowired
    public PayuGateway(RestTemplate restTemplate, Environment environment, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ACTIVE = Boolean.valueOf(environment.getRequiredProperty("payu.active"));
        this.MERCHANT_KEY = environment.getRequiredProperty("payu.merchant.key");
        this.MERCHANT_SALT = environment.getRequiredProperty("payu.merchant.salt");
        this.MERCHANT_URL_PAY = environment.getRequiredProperty("payu.url");
        this.MERCHANT_URL_STATUS = environment.getRequiredProperty("payu.url.status");
        this.MERCHANT_PATH_PAY = environment.getRequiredProperty("payu.path.pay");
        this.MERCHANT_PATH_STATUS = environment.getRequiredProperty("payu.path.status");
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
        Map<String, String> paymentParams = buildPaymentParams(transaction);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host(MERCHANT_URL_PAY).path
                (MERCHANT_PATH_PAY).build();
        Map<String, Object> additionalDetails = getAdditionalDetails(transaction);
        additionalDetails.put("payuFormData", paymentParams);
        transaction.setAdditionalDetails(additionalDetails);
        return uriComponents.toUri();
    }

    private Map<String, String> buildPaymentParams(Transaction transaction) {
        Map<String, Object> additionalDetails = getAdditionalDetails(transaction);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("key", MERCHANT_KEY);
        params.put("txnid", transaction.getTxnId());
        params.put("amount", transaction.getTxnAmount());
        params.put("productinfo", transaction.getProductInfo());
        params.put("firstname", transaction.getUser().getName());
        params.put("email", Objects.toString(transaction.getUser().getEmailId(), ""));
        params.put("phone", transaction.getUser().getMobileNumber());
        params.put("surl", transaction.getCallbackUrl());
        params.put("furl", transaction.getCallbackUrl());

        for (int i = 1; i <= 10; i++) {
            addOptionalParam(params, additionalDetails, "udf" + i);
        }
        for (String field : new String[]{"address1", "address2", "city", "state",
                "country", "zipcode", "enforce_paymethod"}) {
            addOptionalParam(params, additionalDetails, field);
        }

        String splitRequest = createSplitRequest(additionalDetails);
        if (splitRequest != null) {
            params.put("splitRequest", splitRequest);
        }
        params.put("hash", createPaymentHash(params, splitRequest));
        return params;
    }

    /**
     * Optional split input in transaction.additionalDetails:
     * {"splitType":"absolute","splitInfo":{"childKey":{"txnId":"...","amount":"...","charges":"0.00"}}}
     */
    private Map<String, Object> getAdditionalDetails(Transaction transaction) {
        if (transaction.getAdditionalDetails() == null) {
            return new LinkedHashMap<>();
        }

        try {
            return objectMapper.convertValue(transaction.getAdditionalDetails(), Map.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("INVALID_PAYU_ADDITIONAL_DETAILS",
                    "PayU additionalDetails must be a JSON object");
        }
    }

    private void addOptionalParam(Map<String, String> params, Map<String, Object> additionalDetails, String key) {
        Object value = additionalDetails.get(key);
        if (value != null) {
            params.put(key, value.toString());
        }
    }

    private String createSplitRequest(Map<String, Object> additionalDetails) {
        Object splitTypeValue = additionalDetails.get("splitRequest");
        Object splitInfoValue = additionalDetails.get("splitInfo");
        if (splitTypeValue == null && splitInfoValue == null) {
            return null;
        }
        if (splitTypeValue == null || !(splitInfoValue instanceof Map)) {
            throw new CustomException("INVALID_PAYU_SPLIT_DETAILS",
                    "PayU splitType and splitInfo must both be provided");
        }

        String splitType = splitTypeValue.toString();
        if (!"absolute".equalsIgnoreCase(splitType) && !"percentage".equalsIgnoreCase(splitType)) {
            throw new CustomException("INVALID_PAYU_SPLIT_DETAILS",
                    "PayU splitType must be absolute or percentage");
        }

        Map<?, ?> splitInfo = (Map<?, ?>) splitInfoValue;
        if (splitInfo.isEmpty()) {
            throw new CustomException("INVALID_PAYU_SPLIT_DETAILS",
                    "PayU splitInfo must contain at least one child merchant");
        }

        Map<String, Object> payuSplitInfo = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : splitInfo.entrySet()) {
            String merchantKey = Objects.toString(entry.getKey(), "");
            if (merchantKey.trim().isEmpty() || !(entry.getValue() instanceof Map)) {
                throw new CustomException("INVALID_PAYU_SPLIT_DETAILS",
                        "Each PayU splitInfo entry must identify a child merchant and its split details");
            }

            Map<?, ?> childDetails = (Map<?, ?>) entry.getValue();
            Map<String, String> payuChildDetails = new LinkedHashMap<>();
            payuChildDetails.put("aggregatorSubTxnId", requiredSplitValue(childDetails, "txnId"));
            payuChildDetails.put("aggregatorSubAmt", requiredSplitValue(childDetails, "amount"));
            payuChildDetails.put("aggregatorCharges", requiredSplitValue(childDetails, "charges"));
            payuSplitInfo.put(merchantKey, payuChildDetails);
        }

        Map<String, Object> splitRequest = new LinkedHashMap<>();
        splitRequest.put("type", splitType.toLowerCase());
        splitRequest.put("splitInfo", payuSplitInfo);
        try {
            return objectMapper.writeValueAsString(splitRequest);
        } catch (IOException e) {
            log.error("Failed to serialize PayU split request", e);
            throw new CustomException("PAYU_SPLIT_REQUEST_GEN_FAILED",
                    "Unable to generate PayU split request");
        }
    }

    private String requiredSplitValue(Map<?, ?> childDetails, String key) {
        Object value = childDetails.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new CustomException("INVALID_PAYU_SPLIT_DETAILS",
                    "PayU splitInfo child entries must contain " + key);
        }
        return value.toString();
    }

    private String createPaymentHash(Map<String, String> params, String splitRequest) {
        List<String> hashParts = new ArrayList<>();
        hashParts.add(MERCHANT_KEY);
        hashParts.add(params.get("txnid"));
        hashParts.add(params.get("amount"));
        hashParts.add(params.get("productinfo"));
        hashParts.add(params.get("firstname"));
        hashParts.add(params.get("email"));
        for (int i = 1; i <= 10; i++) {
            hashParts.add(Objects.toString(params.get("udf" + i), ""));
        }
        hashParts.add(MERCHANT_SALT);
        if (splitRequest != null) {
            hashParts.add(splitRequest);
        }
        return hashCal(String.join("|", hashParts));
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
        PayuResponse resp = objectMapper.convertValue(params, PayuResponse.class);
        if( ! isNull(resp.getHash()) && ! isNull(resp.getStatus()) && ! isNull(resp.getTxnid()) && ! isNull(resp.getAmount())
                && !isNull(resp.getProductinfo()) && !isNull(resp.getFirstname()) ){
            resp.setTransaction_amount(resp.getAmount());
            String checksum = resp.getHash();
            String hash = createResponseHash(resp);

            if(checksum.equalsIgnoreCase(hash)){
                Transaction txn = transformRawResponse(resp, currentStatus);
                if (txn.getTxnStatus().equals(Transaction.TxnStatusEnum.PENDING) || txn.getTxnStatus().equals(Transaction.TxnStatusEnum.FAILURE)) {
                    return txn;
                }
            }
        }

        return fetchStatusFromGateway(currentStatus);
    }

    private String createResponseHash(PayuResponse response) {
        List<String> hashParts = new ArrayList<>();
        hashParts.add(MERCHANT_SALT);
        hashParts.add(response.getStatus());
        for (int i = 0; i < 5; i++) {
            hashParts.add("");
        }
        hashParts.add(Objects.toString(response.getUdf5(), ""));
        hashParts.add(Objects.toString(response.getUdf4(), ""));
        hashParts.add(Objects.toString(response.getUdf3(), ""));
        hashParts.add(Objects.toString(response.getUdf2(), ""));
        hashParts.add(Objects.toString(response.getUdf1(), ""));
        hashParts.add(Objects.toString(response.getEmail(), ""));
        hashParts.add(response.getFirstname());
        hashParts.add(response.getProductinfo());
        hashParts.add(response.getTransaction_amount());
        hashParts.add(response.getTxnid());
        hashParts.add(MERCHANT_KEY);
        return hashCal(String.join("|", hashParts));
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
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnAmount(resp.getTransaction_amount())
                    .txnStatus(status)
                    .gatewayTxnId(resp.getMihpayid())
                    .gatewayPaymentMode(resp.getMode())
                    .gatewayStatusCode(resp.getUnmappedstatus())
                    .gatewayStatusMsg(resp.getStatus())
                    .responseJson(resp)
                    .build();
        } else {
            status = Transaction.TxnStatusEnum.FAILURE;
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnAmount(resp.getTransaction_amount())
                    .txnStatus(status)
                    .gatewayTxnId(resp.getMihpayid())
                    .gatewayStatusCode(resp.getError_code())
                    .gatewayStatusMsg(resp.getError_Message())
                    .responseJson(resp)
                    .build();
        }

    }

    private Transaction fetchStatusFromGateway(Transaction currentStatus) {

        String txnRef = currentStatus.getTxnId();
        String hash = hashCal(MERCHANT_KEY + "|"
                + "verify_payment" + "|"
                + txnRef + "|"
                + MERCHANT_SALT);


        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("form", "2");

        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host(MERCHANT_URL_STATUS).path
                (MERCHANT_PATH_STATUS).queryParams(queryParams).build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("key", MERCHANT_KEY);
            params.add("command", "verify_payment");
            params.add("hash", hash);
            params.add("var1", txnRef);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(uriComponents.toUriString(), entity, String.class);

            log.info(response.getBody());

            JsonNode payuRawResponse = objectMapper.readTree(response.getBody());
            JsonNode status = payuRawResponse.path("transaction_details").path(txnRef);

            if(status.isNull())
                throw new CustomException("FAILED_TO_FETCH_STATUS_FROM_GATEWAY",
                        "Unable to fetch status from payment gateway for txnid: "+ currentStatus.getTxnId());
            PayuResponse payuResponse = objectMapper.treeToValue(status, PayuResponse.class);

            return transformRawResponse(payuResponse, currentStatus);

        }catch (RestClientException | IOException e){
            log.error("Unable to fetch status from payment gateway for txnid: "+ currentStatus.getTxnId(), e);
            throw new ServiceCallException("Error occurred while fetching status from payment gateway");
        }
    }

    private String hashCal(String str) {
        byte[] hashSequence = str.getBytes(StandardCharsets.UTF_8);
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashSequence);
            byte messageDigest[] = algorithm.digest();


            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) hexString.append("0");
                hexString.append(hex);
            }

        } catch (NoSuchAlgorithmException nsae) {
            log.error("Error occurred while generating hash "+str, nsae);
            throw new CustomException("CHECKSUM_GEN_FAILED", "Hash generation failed, gateway redirect URI " +
                    "cannot be generated");
        }

        return hexString.toString();
    }

    @Override
    public String generateRedirectFormData(Transaction transaction) {
        try {
            return objectMapper.writeValueAsString(buildPaymentParams(transaction));
        } catch (IOException e) {
            log.error("Failed to serialize PayU form data", e);
            throw new CustomException("PAYU_FORM_DATA_GEN_FAILED", "Unable to generate PayU form data");
        }
    }

}