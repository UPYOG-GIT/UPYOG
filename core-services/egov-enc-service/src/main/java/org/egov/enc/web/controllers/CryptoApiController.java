package org.egov.enc.web.controllers;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.egov.enc.services.EncryptionService;
import org.egov.enc.services.KeyManagementService;
import org.egov.enc.services.SignatureService;
import org.egov.enc.web.models.EncryptionRequest;
import org.egov.enc.web.models.RotateKeyRequest;
import org.egov.enc.web.models.RotateKeyResponse;
import org.egov.enc.web.models.SignRequest;
import org.egov.enc.web.models.SignResponse;
import org.egov.enc.web.models.VerifyRequest;
import org.egov.enc.web.models.VerifyResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class CryptoApiController{

    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private SignatureService signatureService;
    @Autowired
    private KeyManagementService keyManagementService;

    @Autowired
    public CryptoApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @RequestMapping(value="/crypto/v1/_encrypt", method = RequestMethod.POST)
    public ResponseEntity<Object> cryptoEncryptPost(@Valid @RequestBody EncryptionRequest encryptionRequest) throws Exception {
        return new ResponseEntity<>(encryptionService.encrypt(encryptionRequest), HttpStatus.OK );
    }

    @RequestMapping(value="/crypto/v1/_decrypt", method = RequestMethod.POST)
    public ResponseEntity<Object> cryptoDecryptPost(@Valid @RequestBody Object decryptionRequest) throws Exception {
        return new ResponseEntity<>(encryptionService.decrypt(decryptionRequest), HttpStatus.OK );
    }
    
    @RequestMapping(value="/crypto/v1/_swsdecrypt", method = RequestMethod.POST)
    public ResponseEntity<String> cryptoSwsDecryptPost(@Valid @RequestBody Object decryptionRequest) throws Exception {
    	JSONObject decryptionObject = new JSONObject(decryptionRequest);
    	String decryptionString = decryptionObject.getJSONObject("decryptionRequest").getString("userDetails");
        return new ResponseEntity<>(encryptionService.swsDecrypt(decryptionString), HttpStatus.OK );
    }
    
    @RequestMapping(value="/crypto/v1/_swsdecryptnew", method = RequestMethod.POST)
    public ResponseEntity<String> cryptoSwsDecryptNewPost(@Valid @RequestBody Object decryptionRequest) throws Exception {
    	JSONObject decryptionObject = new JSONObject(decryptionRequest);
    	String decryptionString = decryptionObject.getJSONObject("decryptionRequest").getString("userDetails");
    	return new ResponseEntity<>(encryptionService.swsDecryptNew(decryptionString), HttpStatus.OK );
    }

    @RequestMapping(value="/crypto/v1/_sign", method = RequestMethod.POST)
    public ResponseEntity<SignResponse> cryptoSignPost(@Valid @RequestBody SignRequest signRequest) throws Exception {
        return new ResponseEntity<>(signatureService.hashAndSign(signRequest), HttpStatus.OK);
    }

    @RequestMapping(value = "/crypto/v1/_verify", method = RequestMethod.POST)
    public ResponseEntity<VerifyResponse> cryptoVerifyPost(@Valid @RequestBody VerifyRequest verifyRequest) throws Exception {
        return new ResponseEntity<>(signatureService.hashAndVerify(verifyRequest), HttpStatus.OK);
    }

    @RequestMapping(value = "/crypto/v1/_rotateallkeys", method=RequestMethod.POST)
    public ResponseEntity<RotateKeyResponse> cryptoRotateAllKeys(@Valid @RequestBody RotateKeyRequest rotateKeyRequest)
            throws Exception {
        return new ResponseEntity<RotateKeyResponse>(keyManagementService.rotateAllKeys(), HttpStatus.OK);
    }

    @RequestMapping(value = "/crypto/v1/_rotatekey", method=RequestMethod.POST)
    public ResponseEntity<RotateKeyResponse> cryptoRotateKeys(@Valid @RequestBody RotateKeyRequest rotateKeyRequest) throws
            Exception {
        return new ResponseEntity<RotateKeyResponse>(keyManagementService.rotateKey(rotateKeyRequest), HttpStatus.OK);
    }

}
