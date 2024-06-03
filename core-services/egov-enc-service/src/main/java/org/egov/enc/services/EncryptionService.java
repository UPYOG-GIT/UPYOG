package org.egov.enc.services;

import java.net.URLDecoder;
import java.util.Base64;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.egov.enc.config.AppProperties;
import org.egov.enc.models.MethodEnum;
import org.egov.enc.models.ModeEnum;
import org.egov.enc.utils.Constants;
import org.egov.enc.utils.ProcessJSONUtil;
import org.egov.enc.web.models.EncReqObject;
import org.egov.enc.web.models.EncryptionRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class EncryptionService {

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private ProcessJSONUtil processJSONUtil;
    @Autowired
    private KeyManagementService keyManagementService;

    public Object encrypt(EncryptionRequest encryptionRequest) throws Exception {
        LinkedList<Object> outputList = new LinkedList<>();
        for(EncReqObject encReqObject : encryptionRequest.getEncryptionRequests()) {
            if(!keyManagementService.checkIfTenantExists(encReqObject.getTenantId())) {
                throw new CustomException(encReqObject.getTenantId() + Constants.TENANT_NOT_FOUND,
                        encReqObject.getTenantId() + Constants.TENANT_NOT_FOUND );
            }
            MethodEnum encryptionMethod = MethodEnum.fromValue(appProperties.getTypeToMethodMap().get(encReqObject.getType()));
            if(encryptionMethod == null) {
                throw new CustomException(encReqObject.getType() + Constants.INVALD_DATA_TYPE,
                        encReqObject.getType() + Constants.INVALD_DATA_TYPE);
            }
            outputList.add(processJSONUtil.processJSON(encReqObject.getValue(), ModeEnum.ENCRYPT, encryptionMethod, encReqObject.getTenantId()));
        }
        return outputList;
    }

    public Object decrypt(Object decryptionRequest) throws Exception {
        return processJSONUtil.processJSON(decryptionRequest, ModeEnum.DECRYPT, null, null);
    }
    
    public String swsDecrypt(String decryptionRequest) throws Exception {
    	String encryptedText = URLDecoder.decode(decryptionRequest, "UTF-8");
    	Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		byte[] keyBytes = Base64.getDecoder().decode(appProperties.getSwsKey());
		byte[] b = Base64.getDecoder().decode(appProperties.getSwsKey());
		int len = b.length;
		if (len > keyBytes.length) {
			len = keyBytes.length;
		}
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(Base64.getDecoder().decode(appProperties.getSwsIntialVector()));
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] results = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
		
		return new String(results, "UTF-8");
    }
}
