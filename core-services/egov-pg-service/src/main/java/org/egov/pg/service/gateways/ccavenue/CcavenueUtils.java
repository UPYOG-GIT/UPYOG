package org.egov.pg.service.gateways.ccavenue;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class CcavenueUtils {

	private Cipher ecipher;
	private Cipher dcipher;
	private byte[] buf = new byte[1024];
	
	public CcavenueUtils(){
		try{
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
			this.setupCrypto(kgen.generateKey());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public CcavenueUtils(String key){
		SecretKeySpec skey = new SecretKeySpec(getMD5(key), "AES");
		this.setupCrypto(skey);
	}

	private void setupCrypto(SecretKey key){
		// Create an 8-byte initialization vector
		byte[] iv = new byte[]
				{
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
				};

		AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
		try
		{
			ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// CBC requires an initialization vector
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static byte[] getMD5(String input){
		try{
			byte[] bytesOfMessage = input.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(bytesOfMessage);
		}  catch (Exception e){
			return null;
		}
	}
	
	public void encrypt(InputStream in, OutputStream out){
		try {
			// Bytes written to out will be encrypted
			out = new CipherOutputStream(out, ecipher);

			// Read in the cleartext bytes and write to out to encrypt
			int numRead = 0;
			while ((numRead = in.read(buf)) >= 0){
				out.write(buf, 0, numRead);
			}
			out.close();
		}
		catch (java.io.IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Input is a string to encrypt.
	 * @return a Hex string of the byte array
	 */
	public String encrypt(String plaintext){
		try{
			byte[] ciphertext = ecipher.doFinal(plaintext.getBytes("UTF-8"));
			return byteToHex(ciphertext);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}

	}

	public void decrypt(InputStream in, OutputStream out){
		try {
			// Bytes read from in will be decrypted
			in = new CipherInputStream(in, dcipher);

			// Read in the decrypted bytes and write the cleartext to out
			int numRead = 0;
			while ((numRead = in.read(buf)) >= 0) {
				out.write(buf, 0, numRead);
			}
			out.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Input encrypted String represented in HEX
	 * @return a string decrypted in plain text
	 */
	public String decrypt(String hexCipherText){
		try{
			String plaintext = new String(dcipher.doFinal(hexToByte(hexCipherText)), "UTF-8");
			return  plaintext;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public String decrypt(byte[] ciphertext){
		try{
			String plaintext = new String(dcipher.doFinal(ciphertext), "UTF-8");
			return  plaintext;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
    
	
	public static String byteToHex( byte [] raw ) {
		if ( raw == null ) {
			return null;
		}
		String result = "";
		for (int i=0; i < raw.length; i++) {
			result +=
					Integer.toString( ( raw[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}

	public static byte[] hexToByte( String hexString){
		int len = hexString.length();
		byte[] ba = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			ba[i/2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i+1), 16));
		}
		return ba;
	}
	
	public static String generateCRC32Checksum(String message, String secretKey) {
        String msg = message + "|" + secretKey;
        System.out.println("Input Key : "+ msg);
        byte bytes[] = msg.getBytes();
        Checksum checksum = new CRC32();
        // update the current checksum with the specified array of bytes
        checksum.update(bytes, 0, bytes.length);
        // get the current checksum value
        long checksumValue = checksum.getValue();
        System.out.println("CRC32 checksum for input string is: " + 	checksumValue);
        return String.valueOf(checksumValue);
    }
	
	public static HashMap tokenizeToHashMap(String msg, String delimPairValue, String delimKeyPair) {
		HashMap keyPair = new HashMap();
		ArrayList respList = new ArrayList();
		String part = "";
		StringTokenizer strTkn = new StringTokenizer(msg, delimPairValue, true);
		while (strTkn.hasMoreTokens()) {
			part = (String) strTkn.nextElement();
			if (part.equals(delimPairValue)) {
				part = null;
			} else {
				String str[] = part.split(delimKeyPair, 2);
				keyPair.put(str[0], str.length > 1 ? (str[1].equals("") ? null : str[1]) : null);
			}
			if (part == null)
				continue;
			if (strTkn.hasMoreTokens())
				strTkn.nextElement();
		}
		return keyPair.size() > 0 ? keyPair : null;
	}
}