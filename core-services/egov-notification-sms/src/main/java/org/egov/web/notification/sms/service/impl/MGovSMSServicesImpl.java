package org.egov.web.notification.sms.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import org.egov.web.notification.sms.config.SMSProperties;
import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.BaseSMSService;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(value = "sms.provider.class", matchIfMissing = true, havingValue = "MGOV")
public class MGovSMSServicesImpl extends BaseSMSService {

	private static final String SMS_RESPONSE_NOT_SUCCESSFUL = "Sms response not successful";
	
	@Autowired
	private SMSProperties smsProperties;

	@Override
	protected void submitToExternalSmsService(Sms sms) {
		log.info("submitToExternalSmsService() start");
		SSLSocketFactory sf = null;
		SSLContext context = null;
		String encryptedPassword;
		String responseString = "";
		try {
			// context=SSLContext.getInstance("TLSv1.1"); // Use this line for Java version
			// 6
			context = SSLContext.getInstance("TLSv1.2"); // Use this line for Java version 7 and above
			context.init(null, null, null);
			sf = new SSLSocketFactory(context, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			Scheme scheme = new Scheme("https", 443, sf);
			HttpClient client = new DefaultHttpClient();
			client.getConnectionManager().getSchemeRegistry().register(scheme);
			String url=smsProperties.getUrl();
			HttpPost post = new HttpPost(url);
//			HttpPost post = new HttpPost("https://msdgweb.mgov.gov.in/esms/sendsmsrequestDLT");
			encryptedPassword = MD5(smsProperties.getPassword());
			String genratedhashKey = hashGenerator(smsProperties.getUsername(), smsProperties.getSenderid(),
					sms.getMessage(), smsProperties.getSecureKey());
			List nameValuePairs = new ArrayList(1);
			nameValuePairs.add(new BasicNameValuePair("mobileno", sms.getMobileNumber()));
//			nameValuePairs.add(new BasicNameValuePair("mobileno", "08827889058"));
			nameValuePairs.add(new BasicNameValuePair("senderid", smsProperties.getSenderid()));
			nameValuePairs.add(new BasicNameValuePair("content", sms.getMessage()));
			nameValuePairs.add(new BasicNameValuePair("smsservicetype", "singlemsg"));
			nameValuePairs.add(new BasicNameValuePair("username", smsProperties.getUsername()));
			nameValuePairs.add(new BasicNameValuePair("password", encryptedPassword));
			nameValuePairs.add(new BasicNameValuePair("key", genratedhashKey));
			nameValuePairs.add(new BasicNameValuePair("templateid", sms.getTemplateId()));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
//			log.info(response.getStatusLine().toString());
			BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = bf.readLine()) != null) {
				responseString = responseString + line;
			}
			log.info("responseString: " + responseString);
			
//			ResponseEntity res = response;
//			if (!isResponseValidated(res)) { log.error("Response from API - " +
//					  responseString); throw new RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL); }
//					  
//					  if (smsProperties.getSmsErrorCodes().size() > 0 &&
//					  isResponseCodeInKnownErrorCodeList(res)) { throw new
//					 RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL); }
//					  
//					  if (smsProperties.getSmsSuccessCodes().size() > 0 &&
//					  !isResponseCodeInKnownSuccessCodeList(res)) { throw new
//					  RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL); }
			
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String hashGenerator(String userName, String senderId, String content, String secureKey) {
		StringBuffer finalString = new StringBuffer();
		finalString.append(userName.trim()).append(senderId.trim()).append(content.trim()).append(secureKey.trim());
		// logger.info("Parameters for SHA-512 : "+finalString);
		String hashGen = finalString.toString();
		StringBuffer sb = null;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			md.update(hashGen.getBytes());
			byte byteData[] = md.digest();
			// convert the byte to hex format method 1
			sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] md5 = new byte[64];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		md5 = md.digest();
		return convertedToHex(md5);
	}

	private static String convertedToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfOfByte = (data[i] >>> 4) & 0x0F;
			int twoHalfBytes = 0;
			do {
				if ((0 <= halfOfByte) && (halfOfByte <= 9)) {
					buf.append((char) ('0' + halfOfByte));
				}
				else {
					buf.append((char) ('a' + (halfOfByte - 10)));
				}
				halfOfByte = data[i] & 0x0F;
			} while (twoHalfBytes++ < 1);
		}
		return buf.toString();
	}
}
