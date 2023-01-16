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
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

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
	private final String COMMAND;
	private final String REQUEST_TYPE;
	private final String RESPONSE_TYPE;
	private final String WS_URL;
	private final boolean ACTIVE;

	private RestTemplate restTemplate;
	private ObjectMapper objectMapper;

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
		this.WS_URL = environment.getRequiredProperty("ccavenue.path.wsurl");
		this.COMMAND = "orderStatusTracker";
		this.REQUEST_TYPE = "JSON";
		this.RESPONSE_TYPE = "JSON";
	}

	@Override
	public URI generateRedirectURI(Transaction transaction) {

		Random random = new Random();
		int orderNumber = random.nextInt(90000000) + 10000000;

		String jsonData = "{ \"reference_no\":\"103001198924\", \"order_no\":\"" + orderNumber + "\" }";

		String encrypteJsonData = "";
		StringBuffer wsDataBuff = new StringBuffer();

		if (WORKING_KEY != null && !WORKING_KEY.equals("") && jsonData != null && !jsonData.equals("")) {
			CcavenueUtils ccavenueUtis = new CcavenueUtils(WORKING_KEY);
			encrypteJsonData = ccavenueUtis.encrypt(jsonData);
		}
		wsDataBuff.append("enc_request=" + encrypteJsonData + "&access_code=" + ACCESS_CODE + "&command=" + COMMAND
				+ "&response_type=" + RESPONSE_TYPE + "&request_type=" + REQUEST_TYPE + "&version=" + "1.1");

		URL url = null;
		URLConnection vHttpUrlConnection = null;
		DataOutputStream vPrintout = null;
		DataInputStream vInput = null;
		StringBuffer vStringBuffer = null;
		try {
			url = new URL(WS_URL);
			if (url.openConnection() instanceof HttpsURLConnection) {
				vHttpUrlConnection = (HttpsURLConnection) url.openConnection();
			} else if (url.openConnection() instanceof HttpsURLConnection) {
				vHttpUrlConnection = (HttpsURLConnection) url.openConnection();
			} else {
				vHttpUrlConnection = (URLConnection) url.openConnection();
			}
			vHttpUrlConnection.setDoInput(true);
			vHttpUrlConnection.setDoOutput(true);
			vHttpUrlConnection.setUseCaches(false);
			vHttpUrlConnection.connect();
			vPrintout = new DataOutputStream(vHttpUrlConnection.getOutputStream());
			vPrintout.writeBytes(wsDataBuff.toString());
			vPrintout.flush();
			vPrintout.close();
//			if (isNull(url))
			if (isNull(vHttpUrlConnection.getURL()))
				throw new CustomException("CCAVENUE_REDIRECT_URI_GEN_FAILED", "Failed to generate redirect URI");
			else
				return vHttpUrlConnection.getURL().toURI();
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
		// TODO Auto-generated method stub
		return null;
	}

}