package org.egov.enc.models;

import org.egov.tracer.model.CustomException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public class Ciphertext {

	private int keyId;

	private String ciphertext;

	public Ciphertext(String ciphertext) {
		try {
			log.info("ciphertext :" + ciphertext);
			String[] cipherArray = ciphertext.split("\\|");
			this.keyId = Integer.parseInt(cipherArray[0]);
			this.ciphertext = cipherArray[1];
		} catch (Exception e) {
			log.error("-------Exception in Ciphertext(): "+e);
			throw new CustomException(ciphertext + ": Invalid Ciphertext", ciphertext + ": Invalid Ciphertext");
		}
	}

	@Override
	public String toString() {
		return String.valueOf(keyId) + "|" + ciphertext;
	}

}
