/**
 * 
 */

/* NOTICE: This file contains code modified from POI's XSSFPaswordHelper, which is
 *  
 * Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.openpackaging.packages;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.wml.STCryptProv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.sml.CTSheetProtection;
import org.xlsx4j.sml.Worksheet;

/**
 * @since 3.3.0
 */
public class ProtectWorkbook extends ProtectionSettings {
	
	protected static Logger log = LoggerFactory.getLogger(ProtectWorkbook.class);
	
	
	public ProtectWorkbook(OpcPackage pkg) {
		super(pkg);
	}

	
	/**
	 * Use this method to get the CTSheetProtection object, so you can set its
	 * parameters as you see fit. 
	 * 
	 * @param worksheetPart
	 * @return
	 * @throws Xlsx4jException
	 */
	public CTSheetProtection getSheetProtection(WorksheetPart worksheetPart) throws Xlsx4jException {
		
		if (worksheetPart==null) throw new Xlsx4jException("Passed null WorksheetPart");
		
		if (worksheetPart.getJaxbElement()==null) {
			worksheetPart.setJaxbElement(new Worksheet());
		}
		
		CTSheetProtection sheetProtection = worksheetPart.getJaxbElement().getSheetProtection();
		if (sheetProtection==null) {
			sheetProtection = new CTSheetProtection();
			worksheetPart.getJaxbElement().setSheetProtection(sheetProtection);
		}
		
		// default settings
		sheetProtection.setSheet(true);
		sheetProtection.setSelectLockedCells(true);
		sheetProtection.setSelectUnlockedCells(true);
		
		return sheetProtection;
	}
	
	/**
     * Enforces the protection passed in sheetProtection, with the specified password, 
     * using sha512 (like Excel 2013).
     * 
	 * @param sheetProtection
	 * @param password
	 * @throws Xlsx4jException
	 * @since 3.3.0
	 */
	public void setSheetProtectionPassword(CTSheetProtection sheetProtection, String password) throws Xlsx4jException {
		
		setSheetProtection( sheetProtection,  password, HashAlgorithm.sha512);
	}
	
    /**
     * Enforces the protection passed in sheetProtection, with the specified password, and 
     * HashAlgorithm for the password.
     *
     * @param password  the plaintext password, if null no password will be applied
     * @param hashAlgo  the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                  if null, it will default default to sha512 (like Excel 2013)
     *                  if none, it will behave like Excel 2010
	 * @since 3.3.0
     */
	public void setSheetProtection(CTSheetProtection sheetProtection, String password, HashAlgorithm hashAlgo) 
			throws Xlsx4jException {

		if (sheetProtection==null) throw new Xlsx4jException("Passed null sheetProtection object");

		// known initial state
    	sheetProtection.setAlgorithmName(null);
    	sheetProtection.setSaltValue(null);
    	sheetProtection.setSpinCount(null);
    	sheetProtection.setHashValue(null);
    	sheetProtection.setPassword(null);
		
		/* 
		 * sha512 tested on Excel 2010; presume ok with 2013.
		 */
        if (hashAlgo == null) hashAlgo = HashAlgorithm.sha512;  // Excel 2013

        
		
        if (password == null) {
        	        	
            return; // we've alreadt done what we need to do
            
        } else if (hashAlgo == HashAlgorithm.none) {
        
    		int hash = CryptoFunctions.createXorVerifier1(password);
    	    sheetProtection.setPassword(
    	    		DatatypeConverter.parseHexBinary(Integer.toHexString(hash))
    	    		);	
    	    return;
        } 
        
        final STCryptProv providerType;
        final int sid;
        switch (hashAlgo) {
            case md2:
                providerType = STCryptProv.RSA_FULL;
                sid = 1;
                break;
            case md4:
                providerType = STCryptProv.RSA_FULL;
                sid = 2;
                break;
            case md5:
                providerType = STCryptProv.RSA_FULL;
                sid = 3;
                break;
            case sha1:
                providerType = STCryptProv.RSA_FULL;
                sid = 4;
                break;
            case sha256:
                providerType = STCryptProv.RSA_AES;
                sid = 12;
                break;
            case sha384:
                providerType = STCryptProv.RSA_AES;
                sid = 13;
                break;
            case sha512:
                providerType = STCryptProv.RSA_AES;
                sid = 14;
                break;
            default:
                throw new EncryptedDocumentException
                        ("Hash algorithm '" + hashAlgo + "' is not supported for document write protection.");
        }

            
        SecureRandom random = new SecureRandom();
        byte salt[] = random.generateSeed(16);

        int spinCount = 100000;
        
        byte hash[] = CryptoFunctions.hashPassword(password, hashAlgo, salt, spinCount, false);

// The code commented out below works fine, but the CryptoFunctions API is neater.
        
//            // From http://apache-poi.1045710.n5.nabble.com/Problem-POI-3-9-with-extracting-hashValue-and-saltValue-from-excel-2013-protected-worksheet-tp5714790p5714845.html
//            // Andi 'Kiwiwings'
//            MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo); 
//
//            hashAlg.update(salt); 
//            byte[] hash;
//			try {
//				hash = hashAlg.digest(password.getBytes("UTF-16LE"));
//			} catch (UnsupportedEncodingException e1) {
//                throw new Xlsx4jException("error in password hashing", e1); 
//			} 
//            byte[] iterator = new byte[LittleEndianConsts.INT_SIZE]; 
//
//            try { 
//                for (int i = 0; i < spinCount; i++) { 
//                    LittleEndian.putInt(iterator, 0, i); 
//                    hashAlg.reset(); 
//                    hashAlg.update(hash); 
//                    hashAlg.update(iterator); 
//                    hashAlg.digest(hash, 0, hash.length); // don't create hash buffer everytime new 
//                } 
//            } catch (DigestException e) { 
//                throw new Xlsx4jException("error in password hashing", e); 
//            } 
            
        // be sure to use the format at https://msdn.microsoft.com/en-us/library/dd920692(v=office.12).aspx
        if (hashAlgo.jceId.startsWith("SHA")) {
        	sheetProtection.setAlgorithmName(hashAlgo.jceId);            	
        } else {
        	sheetProtection.setAlgorithmName(hashAlgo.ecmaString);  
        }
        
    	sheetProtection.setSaltValue(salt);
    	sheetProtection.setSpinCount((long) spinCount);
    	sheetProtection.setHashValue(hash);
        
    }
	

    /**
     * Validates the password, i.e.
     * calculates the hash of the given password and compares it against the stored hash
     *
	 * @param sheetProtection
     * @param password the password, if null the method will always return false,
     *  even if there's no password set
     * 
     * @return true, if the hashes match
	 * @since 3.3.0
     */
    public static boolean validateSheetProtectionPassword(CTSheetProtection sheetProtection, String password) {
        // TODO: is "velvetSweatshop" the default password?
        if (password == null) return false;
        
        byte[] xorHashVal = sheetProtection.getPassword();
        

        if (xorHashVal != null) {
        	
        	// DatatypeConverter.parseHexBinary(Integer.toHexString(hash))
        	int hash1 = Integer.parseInt(
        			DatatypeConverter.printHexBinary(xorHashVal), 16);
        	
            int hash2 = CryptoFunctions.createXorVerifier1(password);
            return hash1 == hash2;
            
        } else {

            byte[] hash1 = sheetProtection.getHashValue();
            byte[] salt = sheetProtection.getSaltValue();
            String algoName = sheetProtection.getAlgorithmName();
            Long spinCount =sheetProtection.getSpinCount();
        	
            if (hash1 == null || algoName == null || salt == null || spinCount == null) {
                return false;
            }
            
            HashAlgorithm hashAlgo = HashAlgorithm.fromString(algoName);         
            byte hash2[] = CryptoFunctions.hashPassword(password, hashAlgo, salt, spinCount.intValue(), false);
            return Arrays.equals(hash1, hash2);
        }
    }
	
	

}
