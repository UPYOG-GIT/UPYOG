package org.egov.web.notification.sms.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.egov.web.notification.sms.config.SMSProperties;
import org.egov.web.notification.sms.models.Sms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class BaseSMSService implements SMSService, SMSBodyBuilder {

	private static final String SMS_RESPONSE_NOT_SUCCESSFUL = "Sms response not successful";

	@Autowired
	protected RestTemplate restTemplate;

	@Autowired
	protected SMSProperties smsProperties;

	@Autowired
	protected Environment env;

	@PostConstruct
	public void init() {
		List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
		converters.remove(converters.stream()
				.filter(c -> c.getClass().equals(MappingJackson2HttpMessageConverter.class)).findFirst().get());
		converters.add(new MappingJackson2HttpMessageConverter() {
			@Override
			protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
					throws IOException, HttpMessageNotWritableException {
				if (object.getClass().equals(LinkedMultiValueMap.class)) {
					LinkedMultiValueMap<?, ?> map = (LinkedMultiValueMap<?, ?>) object;
					object = map.toSingleValueMap();
				}
				super.writeInternal(object, type, outputMessage);
			}
		});
	}

	@Override
	public void sendSMS(Sms sms) {
		log.info("sendSMS() start: " + sms);
		if (!sms.isValid()) {
			log.error(String.format("Sms %s is not valid", sms));
			return;
		}

		if (smsProperties.isNumberBlacklisted(sms.getMobileNumber())) {
			log.error(String.format("Sms to %s is blacklisted", sms.getMobileNumber()));
			return;
		}

		if (!smsProperties.isNumberWhitelisted(sms.getMobileNumber())) {
			log.error(String.format("Sms to %s is not in whitelist", sms.getMobileNumber()));
			return;
		}
		log.info("calling submitToExternalSmsService() method");
		submitToExternalSmsService(sms);
	}

	protected abstract void submitToExternalSmsService(Sms sms);

	protected <T> ResponseEntity<T> executeAPI(URI uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> type) {
		log.info("executeAPI() start");

		log.info("calling third party api with url: " + uri + "  method:" + method);
		@SuppressWarnings("unchecked")
		ResponseEntity<T> res = (ResponseEntity<T>) restTemplate.exchange(uri, method, requestEntity, String.class);

		log.info("third part api call done");

		String responseString = res.getBody().toString();
//		log.info(res.getStatusCode());
		log.info("Response: " + responseString);

		// String dummyResponse = "Message Accepted For Request
		// ID=1231457859641254687954~code=API00 & info=Sms platform accepted & Time =
		// 2007/10/04/09/58";

		/*
		 * if (!isResponseValidated(res)) { log.error("Response from API - " +
		 * responseString); throw new RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL); }
		 * 
		 * if (smsProperties.getSmsErrorCodes().size() > 0 &&
		 * isResponseCodeInKnownErrorCodeList(res)) { throw new
		 * RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL); }
		 * 
		 * if (smsProperties.getSmsSuccessCodes().size() > 0 &&
		 * !isResponseCodeInKnownSuccessCodeList(res)) { throw new
		 * RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL); }
		 */

		//
		StringTokenizer tokenizer = new StringTokenizer(responseString, "&");
		HashMap<String, String> responseMap = new HashMap<String, String>();
		String pair = null, pname = null, pvalue = null;
		while (tokenizer.hasMoreTokens()) {
			pair = (String) tokenizer.nextToken();
			if (pair != null) {
				StringTokenizer strTok = new StringTokenizer(pair, "=");
				pname = "";
				pvalue = "";
				if (strTok.hasMoreTokens()) {
					pname = (String) strTok.nextToken().trim();
					if (strTok.hasMoreTokens())
						pvalue = (String) strTok.nextToken().trim();
					responseMap.put(pname, pvalue);
				}

			}
		}
		boolean status = responseString.contains("API000");

//		if (!status) {
//			log.error("error response from third party api: info:" + responseMap.get("info"));
//			throw new RuntimeException(responseMap.get("info"));
//		}

		log.info("executeAPI() end");
		return res;
	}

	protected boolean isResponseValidated(ResponseEntity<?> response) {
		String responseString = response.getBody().toString();
		if (smsProperties.isVerifyResponse() && !responseString.contains(smsProperties.getVerifyResponseContains())) {
			return false;
		}
		return true;
	}

	protected boolean isResponseCodeInKnownErrorCodeList(ResponseEntity<?> response) {
		final String responseCode = Integer.toString(response.getStatusCodeValue());
		return smsProperties.getSmsErrorCodes().stream().anyMatch(errorCode -> errorCode.equals(responseCode));
	}

	protected boolean isResponseCodeInKnownSuccessCodeList(ResponseEntity<?> response) {
		final String responseCode = Integer.toString(response.getStatusCodeValue());
		return smsProperties.getSmsSuccessCodes().stream().anyMatch(successCode -> successCode.equals(responseCode));
	}

	public MultiValueMap<String, String> getSmsRequestBody(Sms sms) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		for (String key : smsProperties.getConfigMap().keySet()) {
			String value = smsProperties.getConfigMap().get(key);
			if (value.startsWith("$")) {
				switch (value) {
				case "$username":
//                        map.add(key, /*smsProperties.getUsername()*/"pbdwss.sms");
					map.add(key, smsProperties.getUsername());
					break;
				case "$password":
//                        map.add(key, /*smsProperties.getPassword()*/"Nkyf%403254");
					map.add(key, smsProperties.getPassword());
					break;
				case "$senderid":
					map.add(key, smsProperties.getSenderid());
					break;
				case "$mobileno":
					map.add(key, smsProperties.getMobileNumberPrefix() + sms.getMobileNumber());
					break;
				case "$message":
					map.add(key, sms.getMessage());
					break;
				case "$secureKey":
					map.add(key, smsProperties.getSecureKey());
					break;
				case "$templateid":
//					map.add(key, smsProperties.getSmsDefaultTmplid());
					map.add(key, sms.getTemplateId());
					break;
				default:
					if (env.containsProperty(value.substring(1))) {
						map.add(key, env.getProperty(value.substring(1)));
					} else if (smsProperties.getExtraConfigMap().containsKey(value.substring(1))) {
						map.add(key, smsProperties.getExtraConfigMap().get(value.substring(1)));
					} else if (smsProperties.getCategoryMap().containsKey(value.substring(1))) {
						Map<String, Map<String, String>> categoryMap = smsProperties.getCategoryMap();
						Map<String, String> categoryValue = categoryMap.get(value.substring(1));
						if (sms.getCategory() == null && categoryValue.containsKey('*')) {
							map.add(key, categoryValue.get('*'));
						} else if (sms.getCategory() != null) {
							if (categoryValue.containsKey(sms.getCategory().toString())) {
								map.add(key, categoryValue.get(sms.getCategory().toString()));
							} else if (categoryValue.containsKey('*')) {
								map.add(key, categoryValue.get('*'));
							}
						}
					} else {
						map.add(key, value);
					}
					break;
				}
			} else {
				map.add(key, value);
			}

		}

		return map;
	}

	protected HttpEntity<MultiValueMap<String, String>> getRequest(Sms sms) {
		final MultiValueMap<String, String> requestBody = getSmsRequestBody(sms);
		return new HttpEntity<>(requestBody, getHttpHeaders());
	}

	protected HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(smsProperties.getContentType()));
		return headers;
	}

	@PostConstruct
	protected void setupSSL() {
		if (!smsProperties.isVerifySSL()) {

//			SSLContext ctx = null;
			try {

				SSLContext ctx = SSLContext.getInstance("TLSv1.2");

				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				// File file = new File(System.getenv("JAVA_HOME")+"/lib/security/cacerts");
//                File file = new File("E:\\msdgweb-mgov-gov-in.crt");
//                File file = new File("https:\\try-digit-eks-yourname.s3.ap-south-1.amazonaws.com/msdgweb-mgov-gov-in.crt");
				ClassPathResource resource = new ClassPathResource("msdbweb-2023-2024.cer");
//				File file = resource.getFile();

//				File file =new File("https:\\\\try-digit-eks-yourname.s3.ap-south-1.amazonaws.com/msdgweb-mgov-gov-in.crt");
//				File file = new ClassPathResource("msdgweb-mgov-gov-in11.cer").getFile();
//                byte[] keystoreData = Files.readAllBytes(Paths.get("E:\\msdgweb-mgov-gov-in.crt"));
//				File file = new File(getClass().getClassLoader().getResource("msdgweb-mgov-gov-in.crt").getFile());
//                File file = new ClassPathResource("https:\\try-digit-eks-yourname.s3.ap-south-1.amazonaws.com/msdgweb-mgov-gov-in.crt").getFile();

//				String fileUrl = "https://try-digit-eks-yourname.s3.ap-south-1.amazonaws.com/msdbweb-2023-2024.cer";
//				String fileUrl = "https://try-digit-eks-yourname.s3.ap-south-1.amazonaws.com/msdgweb_ssl.cer";
				String fileUrl = smsProperties.getSslFileUrl();
				log.info("SSL File Name : " + fileUrl.split("amazonaws.com/")[1]);
//		        String destinationFilePath = "C:/example/folder/msdgweb-mgov-gov-in.crt";

				URL url = new URL(fileUrl);
				URLConnection connection = url.openConnection();
				InputStream is = connection.getInputStream();

//				InputStream is = new FileInputStream(file);

				CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
				X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(is);

				trustStore.load(null, null); // Initialize an empty keystore
				trustStore.setCertificateEntry("alias", certificate);

//                ByteArrayInputStream is = new ByteArrayInputStream(keystoreData);
//                trustStore.load(is, "changeit".toCharArray());

				TrustManagerFactory trustFactory = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());

//                TrustManagerFactory trustFactory = TrustManagerFactory
//                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustFactory.init(trustStore);

				TrustManager[] trustManagers = trustFactory.getTrustManagers();
				ctx.init(null, trustManagers, null);
				SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier());
				CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
				HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
				requestFactory.setHttpClient(httpClient);
				restTemplate.setRequestFactory(requestFactory);

//				ctx.init(null, null, SecureRandom.getInstance("SHA1PRNG"));

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
//			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier());
//			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
//			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//			requestFactory.setHttpClient(httpClient);
//			restTemplate.setRequestFactory(requestFactory);
		}
	}

}