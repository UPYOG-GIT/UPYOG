package org.egov.pg.web.controllers;

import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.Transaction;
import org.egov.pg.service.TransactionService;
import org.egov.pg.web.models.TransactionCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class RedirectController {

	@Value("${egov.default.citizen.url}")
	private String defaultURL;

	@Value("${paygov.original.return.url.key}")
	private String returnUrlKey;

	@Value("${paygov.citizen.redirect.domain.name}")
	private String citizenRedirectDomain;

	@Value("${ccavenue.citizen.redirect.domain.name}")
	private String niwaspassRedirectDomain;

	@Value("${ccavenue.working.key}")
	private String workingKey;

	private final TransactionService transactionService;

	private Cipher dcipher;

	@Autowired
	public RedirectController(TransactionService transactionService) {
		this.transactionService = transactionService;

	}

	@PostMapping(value = "/transaction/v1/_redirect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<Object> method(@RequestBody MultiValueMap<String, String> formData) {
		SecretKeySpec skey = new SecretKeySpec(getMD5(workingKey), "AES");
		this.setupCrypto(skey);
		log.info("inside /transaction/v1/_redirect controller");
		log.info("formData: " + formData.toString());
//		CcavenueUtils ccavenueUtis = new CcavenueUtils(WORKING_KEY);
		String encResp = formData.get("encResp").get(0);
		log.info("encResp: " + encResp);
		String plainText = decrypt(encResp);
		log.info("plainText: " + plainText);
		String data[] = plainText.split("&");
		log.info("data : " + data);
		String returnURL = "";
		String gateway1 = null;
		for (String d : data) {
			log.info("d: " + d);
			String d1[] = d.split("=");
			log.info("d1 : " + d1);
			for (int i = 0; i < d1.length; i++) {
				if (d1[0].equals("merchant_param1")) {
					log.info("merchant_param1: " + d1[0]);
//					returnURL = d1[1] + "=" + d1[2];
				} else if (d1[0].equals("merchant_param2")) {
					log.info("merchant_param2: " + d1[0]);
					gateway1 = d1[1];
					log.info("gateway1: " + gateway1);
//					returnURL = d1[1] + "=" + d1[2];
				}
			}
		}

//		String returnURL = formData.get(returnUrlKey).get(0);
		log.info("returnURL: " + returnURL);
		MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString(returnURL).build().getQueryParams();

		/*
		 * From redirect URL get transaction id. And using transaction id fetch
		 * transaction details. And from transaction details get the GATEWAY info.
		 */
		String gateway = null;
		if (!params.isEmpty()) {
			List<String> txnId = params.get(PgConstants.PG_TXN_IN_LABEL);
			TransactionCriteria critria = new TransactionCriteria();
			critria.setTxnId(txnId.get(0));
			List<Transaction> transactions = transactionService.getTransactions(critria);
			if (!transactions.isEmpty())
				gateway = transactions.get(0).getGateway();
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		/*
		 * The NSDL PAYGOV integration is not allowing multiple schems or protocols (ex:
		 * HTTP, HTTPS) in the success or fail or redirect URL after completing payment
		 * from payment gateway used for posting response. Example the URL resposne
		 * getting as follows,
		 * https://test.org/pg-service/transaction/v1/_redirect?originalreturnurl=/digit
		 * -ui/citizen/payment/success/PT/PG-PT-2022-03-10-006063/pg.citya?eg_pg_txnid=
		 * PB_PG_2022_07_12_002082_48 Here we are reading originalreturnurl value and
		 * then forming redirect URL with domain name.
		 */
		if (gateway != null && gateway.equalsIgnoreCase("PAYGOV")) {
			StringBuilder redirectURL = new StringBuilder();
			redirectURL.append(citizenRedirectDomain).append(returnURL);
			formData.remove(returnUrlKey);
			httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(redirectURL.toString()).queryParams(formData)
					.build().encode().toUri());
		} else if (gateway != null && gateway.equalsIgnoreCase("CCAVENUE")) {
			log.info("inside CCAvenue condition");
			StringBuilder redirectURL = new StringBuilder();
//			redirectURL.append(niwaspassRedirectDomain).append(returnURL);
			redirectURL.append(returnURL);
			formData.remove(returnUrlKey);
			httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(redirectURL.toString()).queryParams(formData)
					.build().encode().toUri());
		} else {
			httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(formData.get(returnUrlKey).get(0))
					.queryParams(formData).build().encode().toUri());
		}
		log.info("httpHeaders: " + httpHeaders.toString());
		return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleError(Exception e) {
		log.error("EXCEPTION_WHILE_REDIRECTING", e);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(defaultURL).build().encode().toUri());
		return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
	}

	public String decrypt(String hexCipherText) {

		try {
			String plaintext = new String(dcipher.doFinal(hexToByte(hexCipherText)), "UTF-8");
			return plaintext;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] hexToByte(String hexString) {
		int len = hexString.length();
		byte[] ba = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			ba[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return ba;
	}

	private void setupCrypto(SecretKey key) {
		// Create an 8-byte initialization vector
		byte[] iv = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d,
				0x0e, 0x0f };

		AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
		try {
//			ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// CBC requires an initialization vector
//			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static byte[] getMD5(String input) {
		try {
			byte[] bytesOfMessage = input.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(bytesOfMessage);
		} catch (Exception e) {
			return null;
		}
	}
}