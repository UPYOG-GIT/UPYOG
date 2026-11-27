package org.egov.pg.service.gateways.payu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.models.Transaction;
import org.egov.pg.service.Gateway;
import org.egov.pg.utils.Utils;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public String generateTransactionId() {
        return "TXN_OBPAS_" + System.currentTimeMillis();
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {

        String txnId = generateTransactionId();

        Map<String, String> params = new LinkedHashMap<>();

        params.put("key", MERCHANT_KEY);
        params.put("txnid", txnId);
        params.put("amount", "10");
        params.put("productinfo", "Test");
        params.put("firstname", "John");
        params.put("email", "dev4entitcs@gmail.com");
        params.put("phone", "6464646473");
        params.put("surl", "https://payu.in/integrationlab/callback.php");
        params.put("furl", "https://payu.in/integrationlab/callback.php");
        params.put("lastname", "Doe");
        params.put("city", "Mumbai");
        params.put("state", "Maharashtra");
        params.put("country", "India");
        params.put("zipcode", "400001");

        // Build split merchants data
        Map<String, Map<String, String>> splitInfo = new HashMap<>();
        // Child Merchant 1: rCnCe9
        Map<String, String> child1 = new HashMap<>();
        child1.put("aggregatorSubTxnId", generateTransactionId());
        child1.put("aggregatorSubAmt", "7");
        child1.put("aggregatorCharges", "0.00");
        splitInfo.put("rCnCe9", child1);

        // Child Merchant 2: Z9UfjV
        Map<String, String> child2 = new HashMap<>();
        child2.put("aggregatorSubTxnId", generateTransactionId());
        child2.put("aggregatorSubAmt", "3");
        child2.put("aggregatorCharges", "0.00");
        splitInfo.put("Z9UfjV", child2);

        // Generate splitRequest JSON
        String splitRequest = generateSplitRequest("absolute", splitInfo);
        params.put("splitRequest", splitRequest);


        Map<String, Object> splitRequestParam = new LinkedHashMap<>();
        splitRequestParam.put("type", "absolute");
        splitRequestParam.put("splitInfo", splitInfo);

        String hashString = MERCHANT_KEY + "|" +
                params.get("txnid") + "|" +
                params.get("amount") + "|" +
                params.get("productinfo") + "|" +
                params.get("firstname") + "|" +
                params.get("email") + "|" +
                params.getOrDefault("udf1", "") + "|" +
                params.getOrDefault("udf2", "") + "|" +
                params.getOrDefault("udf3", "") + "|" +
                params.getOrDefault("udf4", "") + "|" +
                params.getOrDefault("udf5", "") + "||||||" +
                MERCHANT_SALT + "|" +
                params.get("splitRequest");

        // Generate hash
        String hash = hashCal(hashString);
        params.put("hash", hash);

        transaction.setTxnId(txnId);
        transaction.setSplitRequest(splitRequestParam);
        transaction.setHash(hash);
        transaction.setProductInfo(params.get("productinfo"));

        log.info("Transaction " + transaction);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(MERCHANT_URL_PAY).build();

        return uriComponents.toUri();

    }

    public static String generateSplitRequest(String type, Map<String, Map<String, String>> splitInfo) {
        // Note: For production use, consider using a JSON library like Gson or Jackson
        StringBuilder json = new StringBuilder();
        json.append("{\"type\":\"").append(type).append("\",");
        json.append("\"splitInfo\":{");
        int count = 0;
        for (Map.Entry<String, Map<String, String>> entry : splitInfo.entrySet()) {
            if (count > 0)
                json.append(",");
            json.append("\"").append(entry.getKey()).append("\":{");
            Map<String, String> details = entry.getValue();
            json.append("\"aggregatorSubTxnId\":\"").append(details.get("aggregatorSubTxnId")).append("\",");
            json.append("\"aggregatorSubAmt\":\"").append(details.get("aggregatorSubAmt")).append("\",");
            json.append("\"aggregatorCharges\":\"").append(details.get("aggregatorCharges")).append("\"");
            json.append("}");
            count++;
        }
        json.append("}}");
        return json.toString();
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
        PayuResponse resp = objectMapper.convertValue(params, PayuResponse.class);
        if (!isNull(resp.getHash()) && !isNull(resp.getStatus()) && !isNull(resp.getTxnid())
                && !isNull(resp.getAmount())
                && !isNull(resp.getProductinfo()) && !isNull(resp.getFirstname())) {
            resp.setTransaction_amount(resp.getAmount());
            String checksum = resp.getHash();

            String hashSequence = "SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|";
            hashSequence = hashSequence.concat(MERCHANT_KEY);
            hashSequence = hashSequence.replace("SALT", MERCHANT_SALT);
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

    public String hashCal(String hashString) {

        // Print hash formula and raw string for debugging
        System.out.println("\nHash Formula:");
        System.out.println(
                "key|txnid|amount|productinfo|firstname|email|udf1|udf2|udf3|udf4|udf5|||||||salt|splitRequest");

        System.out.println("\nRaw Hash String:");
        System.out.println(hashString);

        // Generate SHA512 hash
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hash = md.digest(hashString.getBytes("UTF-8"));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

        UriComponents uriComponents = UriComponentsBuilder.newInstance().scheme("https").host(MERCHANT_URL_STATUS)
                .path(MERCHANT_PATH_STATUS).queryParams(queryParams).build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("key", MERCHANT_KEY);
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

    @Override
    public String generateRedirectFormData(Transaction transaction) {
        // TODO Auto-generated method stub
        return null;
    }

}