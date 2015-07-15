/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.docx4j.org.apache.poi.poifs.crypt.agile;

import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getBlock0;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getCipher;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getMessageDigest;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.hashPassword;
import static org.docx4j.org.apache.poi.poifs.crypt.DataSpaceMapUtils.createEncryptionEntry;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.getNextBlockSize;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.hashInput;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kCryptoKeyBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kHashedVerifierBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kIntegrityKeyBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kIntegrityValueBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kVerifierInputBlock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

//import org.apache.xmlbeans.XmlOptions;




import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTDataIntegrity;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTEncryption;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTKeyData;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTKeyEncryptor;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTKeyEncryptors;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.STCipherChaining;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.STHashAlgorithm;
import org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate.CTCertificateKeyEncryptor;
import org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password.CTPasswordKeyEncryptor;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.DataSpaceMapUtils;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.Encryptor;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.agile.AgileEncryptionVerifier.AgileCertificateEntry;
import org.docx4j.org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;

//import org.docx4j.com.microsoft.schemas.office.x2006.encryption.STCipherAlgorithm;
//import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTKeyEncryptor.Uri.Enum;


public class AgileEncryptor extends Encryptor {
    private final AgileEncryptionInfoBuilder builder;
    private byte integritySalt[];
	private byte pwHash[];
    
	protected AgileEncryptor(AgileEncryptionInfoBuilder builder) {
		this.builder = builder;
	}

    public void confirmPassword(String password) {
        // see [MS-OFFCRYPTO] - 2.3.3 EncryptionVerifier
        Random r = new SecureRandom();
        int blockSize = builder.getHeader().getBlockSize();
        int keySize = builder.getHeader().getKeySize()/8;
        int hashSize = builder.getHeader().getHashAlgorithmEx().hashSize;
        
        byte[] verifierSalt = new byte[blockSize]
             , verifier = new byte[blockSize]
             , keySalt = new byte[blockSize]
             , keySpec = new byte[keySize]
             , integritySalt = new byte[hashSize];
        r.nextBytes(verifierSalt); // blocksize
        r.nextBytes(verifier); // blocksize
        r.nextBytes(keySalt); // blocksize
        r.nextBytes(keySpec); // keysize
        r.nextBytes(integritySalt); // hashsize
        
        confirmPassword(password, keySpec, keySalt, verifierSalt, verifier, integritySalt);
    }
	
	public void confirmPassword(String password, byte keySpec[], byte keySalt[], byte verifier[], byte verifierSalt[], byte integritySalt[]) {
        AgileEncryptionVerifier ver = builder.getVerifier();
        ver.setSalt(verifierSalt);
        AgileEncryptionHeader header = builder.getHeader();
        header.setKeySalt(keySalt);
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();

        int blockSize = header.getBlockSize();
	    
        pwHash = hashPassword(password, hashAlgo, verifierSalt, ver.getSpinCount());
        
        /**
         * encryptedVerifierHashInput: This attribute MUST be generated by using the following steps:
         * 1. Generate a random array of bytes with the number of bytes used specified by the saltSize
         *    attribute.
         * 2. Generate an encryption key as specified in section 2.3.4.11 by using the user-supplied password,
         *    the binary byte array used to create the saltValue attribute, and a blockKey byte array
         *    consisting of the following bytes: 0xfe, 0xa7, 0xd2, 0x76, 0x3b, 0x4b, 0x9e, and 0x79.
         * 3. Encrypt the random array of bytes generated in step 1 by using the binary form of the saltValue
         *    attribute as an initialization vector as specified in section 2.3.4.12. If the array of bytes is not an
         *    integral multiple of blockSize bytes, pad the array with 0x00 to the next integral multiple of
         *    blockSize bytes.
         * 4. Use base64 to encode the result of step 3.
         */
        byte encryptedVerifier[] = hashInput(builder, pwHash, kVerifierInputBlock, verifier, Cipher.ENCRYPT_MODE);
        ver.setEncryptedVerifier(encryptedVerifier);
	    

        /**
         * encryptedVerifierHashValue: This attribute MUST be generated by using the following steps:
         * 1. Obtain the hash value of the random array of bytes generated in step 1 of the steps for
         *    encryptedVerifierHashInput.
         * 2. Generate an encryption key as specified in section 2.3.4.11 by using the user-supplied password,
         *    the binary byte array used to create the saltValue attribute, and a blockKey byte array
         *    consisting of the following bytes: 0xd7, 0xaa, 0x0f, 0x6d, 0x30, 0x61, 0x34, and 0x4e.
         * 3. Encrypt the hash value obtained in step 1 by using the binary form of the saltValue attribute as
         *    an initialization vector as specified in section 2.3.4.12. If hashSize is not an integral multiple of
         *    blockSize bytes, pad the hash value with 0x00 to an integral multiple of blockSize bytes.
         * 4. Use base64 to encode the result of step 3.
         */
        MessageDigest hashMD = getMessageDigest(hashAlgo);
        byte[] hashedVerifier = hashMD.digest(verifier);
        byte encryptedVerifierHash[] = hashInput(builder, pwHash, kHashedVerifierBlock, hashedVerifier, Cipher.ENCRYPT_MODE);
        ver.setEncryptedVerifierHash(encryptedVerifierHash);
        
        /**
         * encryptedKeyValue: This attribute MUST be generated by using the following steps:
         * 1. Generate a random array of bytes that is the same size as specified by the
         *    Encryptor.KeyData.keyBits attribute of the parent element.
         * 2. Generate an encryption key as specified in section 2.3.4.11, using the user-supplied password,
         *    the binary byte array used to create the saltValue attribute, and a blockKey byte array
         *    consisting of the following bytes: 0x14, 0x6e, 0x0b, 0xe7, 0xab, 0xac, 0xd0, and 0xd6.
         * 3. Encrypt the random array of bytes generated in step 1 by using the binary form of the saltValue
         *    attribute as an initialization vector as specified in section 2.3.4.12. If the array of bytes is not an
         *    integral multiple of blockSize bytes, pad the array with 0x00 to an integral multiple of
         *    blockSize bytes.
         * 4. Use base64 to encode the result of step 3.
         */
        byte encryptedKey[] = hashInput(builder, pwHash, kCryptoKeyBlock, keySpec, Cipher.ENCRYPT_MODE);
        ver.setEncryptedKey(encryptedKey);
        
        SecretKey secretKey = new SecretKeySpec(keySpec, ver.getCipherAlgorithm().jceId);
        setSecretKey(secretKey);
        
        /*
         * 2.3.4.14 DataIntegrity Generation (Agile Encryption)
         * 
         * The DataIntegrity element contained within an Encryption element MUST be generated by using
         * the following steps:
         * 1. Obtain the intermediate key by decrypting the encryptedKeyValue from a KeyEncryptor
         *    contained within the KeyEncryptors sequence. Use this key for encryption operations in the
         *    remaining steps of this section.
         * 2. Generate a random array of bytes, known as Salt, of the same length as the value of the
         *    KeyData.hashSize attribute.
         * 3. Encrypt the random array of bytes generated in step 2 by using the binary form of the
         *    KeyData.saltValue attribute and a blockKey byte array consisting of the following bytes:
         *    0x5f, 0xb2, 0xad, 0x01, 0x0c, 0xb9, 0xe1, and 0xf6 used to form an initialization vector as
         *    specified in section 2.3.4.12. If the array of bytes is not an integral multiple of blockSize
         *    bytes, pad the array with 0x00 to the next integral multiple of blockSize bytes.
         * 4. Assign the encryptedHmacKey attribute to the base64-encoded form of the result of step 3.
         * 5. Generate an HMAC, as specified in [RFC2104], of the encrypted form of the data (message),
         *    which the DataIntegrity element will verify by using the Salt generated in step 2 as the key.
         *    Note that the entire EncryptedPackage stream (1), including the StreamSize field, MUST be
         *    used as the message.
         * 6. Encrypt the HMAC as in step 3 by using a blockKey byte array consisting of the following bytes:
         *    0xa0, 0x67, 0x7f, 0x02, 0xb2, 0x2c, 0x84, and 0x33.
         * 7.  Assign the encryptedHmacValue attribute to the base64-encoded form of the result of step 6. 
         */
        this.integritySalt = integritySalt;

        try {
            byte vec[] = CryptoFunctions.generateIv(hashAlgo, header.getKeySalt(), kIntegrityKeyBlock, header.getBlockSize());
            Cipher cipher = getCipher(secretKey, ver.getCipherAlgorithm(), ver.getChainingMode(), vec, Cipher.ENCRYPT_MODE);
            byte filledSalt[] = getBlock0(integritySalt, getNextBlockSize(integritySalt.length, blockSize));
            byte encryptedHmacKey[] = cipher.doFinal(filledSalt);
            header.setEncryptedHmacKey(encryptedHmacKey);

            cipher = Cipher.getInstance("RSA");
            for (AgileCertificateEntry ace : ver.getCertificates()) {
                cipher.init(Cipher.ENCRYPT_MODE, ace.x509.getPublicKey());
                ace.encryptedKey = cipher.doFinal(getSecretKey().getEncoded());
                Mac x509Hmac = CryptoFunctions.getMac(hashAlgo);
                x509Hmac.init(getSecretKey());
                ace.certVerifier = x509Hmac.doFinal(ace.x509.getEncoded());
            }
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException(e);
        }
	}
	
    public OutputStream getDataStream(DirectoryNode dir)
            throws IOException, GeneralSecurityException {
        // TODO: initialize headers
        AgileCipherOutputStream countStream = new AgileCipherOutputStream(dir);
    	return countStream;
    }

    /**
     * Generate an HMAC, as specified in [RFC2104], of the encrypted form of the data (message), 
     * which the DataIntegrity element will verify by using the Salt generated in step 2 as the key. 
     * Note that the entire EncryptedPackage stream (1), including the StreamSize field, MUST be 
     * used as the message.
     * 
     * Encrypt the HMAC as in step 3 by using a blockKey byte array consisting of the following bytes:
     * 0xa0, 0x67, 0x7f, 0x02, 0xb2, 0x2c, 0x84, and 0x33.
     **/
    protected void updateIntegrityHMAC(File tmpFile, int oleStreamSize) throws GeneralSecurityException, IOException {
        // as the integrity hmac needs to contain the StreamSize,
        // it's not possible to calculate it on-the-fly while buffering
        // TODO: add stream size parameter to getDataStream()
        AgileEncryptionVerifier ver = builder.getVerifier();
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        Mac integrityMD = CryptoFunctions.getMac(hashAlgo);
        integrityMD.init(new SecretKeySpec(integritySalt, hashAlgo.jceHmacId));

        byte buf[] = new byte[1024];
        LittleEndian.putLong(buf, 0, oleStreamSize);
        integrityMD.update(buf, 0, LittleEndian.LONG_SIZE);
        
        InputStream fis = new FileInputStream(tmpFile);
        try {
            int readBytes;
            while ((readBytes = fis.read(buf)) != -1) {
                integrityMD.update(buf, 0, readBytes);
            }
        } finally {
        	fis.close();
        }
        
        byte hmacValue[] = integrityMD.doFinal();
        
        AgileEncryptionHeader header = builder.getHeader();
        int blockSize = header.getBlockSize();
        byte iv[] = CryptoFunctions.generateIv(header.getHashAlgorithmEx(), header.getKeySalt(), kIntegrityValueBlock, blockSize);
        Cipher cipher = CryptoFunctions.getCipher(getSecretKey(), header.getCipherAlgorithm(), header.getChainingMode(), iv, Cipher.ENCRYPT_MODE);
        byte hmacValueFilled[] = getBlock0(hmacValue, getNextBlockSize(hmacValue.length, blockSize));
        byte encryptedHmacValue[] = cipher.doFinal(hmacValueFilled);
        
        header.setEncryptedHmacValue(encryptedHmacValue);
    }

//    private final CTKeyEncryptor.Uri.Enum passwordUri = 
//            CTKeyEncryptor.Uri.HTTP_SCHEMAS_MICROSOFT_COM_OFFICE_2006_KEY_ENCRYPTOR_PASSWORD;
//        private final CTKeyEncryptor.Uri.Enum certificateUri = 
//            CTKeyEncryptor.Uri.HTTP_SCHEMAS_MICROSOFT_COM_OFFICE_2006_KEY_ENCRYPTOR_CERTIFICATE;
    
    static final String HTTP_SCHEMAS_MICROSOFT_COM_OFFICE_2006_KEY_ENCRYPTOR_PASSWORD = "http://schemas.microsoft.com/office/2006/keyEncryptor/password";
    static final String HTTP_SCHEMAS_MICROSOFT_COM_OFFICE_2006_KEY_ENCRYPTOR_CERTIFICATE = "http://schemas.microsoft.com/office/2006/keyEncryptor/certificate";

    
    private final String passwordUri = 
        HTTP_SCHEMAS_MICROSOFT_COM_OFFICE_2006_KEY_ENCRYPTOR_PASSWORD;
    private final String certificateUri = 
        HTTP_SCHEMAS_MICROSOFT_COM_OFFICE_2006_KEY_ENCRYPTOR_CERTIFICATE;
    
    protected CTEncryption createEncryptionDocument() {
        AgileEncryptionVerifier ver = builder.getVerifier();
        AgileEncryptionHeader header = builder.getHeader(); 
        
        CTEncryption edRoot = new CTEncryption();
        
        CTKeyData keyData = new CTKeyData();
        edRoot.setKeyData(keyData);
        
        CTKeyEncryptors keyEncList = new CTKeyEncryptors();
        edRoot.setKeyEncryptors(keyEncList);
        
        CTKeyEncryptor keyEnc = new CTKeyEncryptor();
        keyEncList.getKeyEncryptor().add(keyEnc);
        keyEnc.setUri(passwordUri);
        
        CTPasswordKeyEncryptor keyPass = new CTPasswordKeyEncryptor();
        keyEnc.setEncryptedPasswordKey(keyPass);

        keyPass.setSpinCount(ver.getSpinCount());
        
        keyData.setSaltSize(header.getBlockSize());
        keyPass.setSaltSize(header.getBlockSize());
        
        keyData.setBlockSize(header.getBlockSize());
        keyPass.setBlockSize(header.getBlockSize());

        keyData.setKeyBits(header.getKeySize());
        keyPass.setKeyBits(header.getKeySize());

        HashAlgorithm hashAlgo = header.getHashAlgorithmEx();
        keyData.setHashSize(hashAlgo.hashSize);
        keyPass.setHashSize(hashAlgo.hashSize);

        //STCipherAlgorithm.Enum xmlCipherAlgo = STCipherAlgorithm.Enum.forString(header.getCipherAlgorithm().xmlId);
        String xmlCipherAlgo = header.getCipherAlgorithm().xmlId;
        if (xmlCipherAlgo == null) {
            throw new EncryptedDocumentException("CipherAlgorithm "+header.getCipherAlgorithm()+" not supported.");
        }
        keyData.setCipherAlgorithm(xmlCipherAlgo);
        keyPass.setCipherAlgorithm(xmlCipherAlgo);
        
        switch (header.getChainingMode()) {
        case cbc: 
            keyData.setCipherChaining(STCipherChaining.CHAINING_MODE_CBC);
            keyPass.setCipherChaining(STCipherChaining.CHAINING_MODE_CBC);
            break;
        case cfb:
            keyData.setCipherChaining(STCipherChaining.CHAINING_MODE_CFB);
            keyPass.setCipherChaining(STCipherChaining.CHAINING_MODE_CFB);
            break;
        default:
            throw new EncryptedDocumentException("ChainingMode "+header.getChainingMode()+" not supported.");
        }
        
        STHashAlgorithm xmlHashAlgo = STHashAlgorithm.fromValue(hashAlgo.ecmaString);
        if (xmlHashAlgo == null) {
            throw new EncryptedDocumentException("HashAlgorithm "+hashAlgo+" not supported.");
        }
        keyData.setHashAlgorithm(xmlHashAlgo);
        keyPass.setHashAlgorithm(xmlHashAlgo);

        keyData.setSaltValue(header.getKeySalt());
        keyPass.setSaltValue(ver.getSalt());
        keyPass.setEncryptedVerifierHashInput(ver.getEncryptedVerifier());
        keyPass.setEncryptedVerifierHashValue(ver.getEncryptedVerifierHash());
        keyPass.setEncryptedKeyValue(ver.getEncryptedKey());
        
        CTDataIntegrity hmacData = new CTDataIntegrity();
        edRoot.setDataIntegrity(hmacData);
        
        hmacData.setEncryptedHmacKey(header.getEncryptedHmacKey());
        hmacData.setEncryptedHmacValue(header.getEncryptedHmacValue());
        
        for (AgileCertificateEntry ace : ver.getCertificates()) {
            keyEnc = new CTKeyEncryptor();
            keyEncList.getKeyEncryptor().add(keyEnc);
            keyEnc.setUri(certificateUri);
            
            CTCertificateKeyEncryptor certData = new CTCertificateKeyEncryptor();
            keyEnc.setEncryptedCertificateKey(certData);
            try {
                certData.setX509Certificate(ace.x509.getEncoded());
            } catch (CertificateEncodingException e) {
                throw new EncryptedDocumentException(e);
            }
            certData.setEncryptedKeyValue(ace.encryptedKey);
            certData.setCertVerifier(ace.certVerifier);
        }
        
        return edRoot;
    }
    
//    protected void marshallEncryptionDocument(EncryptionDocument ed, LittleEndianByteArrayOutputStream os) {
//        XmlOptions xo = new XmlOptions();
//        xo.setCharacterEncoding("UTF-8");
//        Map<String,String> nsMap = new HashMap<String,String>();
//        nsMap.put(passwordUri.toString(),"p");
//        nsMap.put(certificateUri.toString(), "c");
//        xo.setUseDefaultNamespace();
//        xo.setSaveSuggestedPrefixes(nsMap);
//        xo.setSaveNamespacesFirst();
//        xo.setSaveAggressiveNamespaces();
//
//        // setting standalone doesn't work with xmlbeans-2.3 & 2.6
//        // ed.documentProperties().setStandalone(true);
//        xo.setSaveNoXmlDecl();
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        try {
//            bos.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n".getBytes("UTF-8"));
//            ed.save(bos, xo);
//            os.write(bos.toByteArray());
//        } catch (IOException e) {
//            throw new EncryptedDocumentException("error marshalling encryption info document", e);
//        }
//    }

    protected void marshallEncryptionDocument(CTEncryption ed, LittleEndianByteArrayOutputStream os) {

		try {
			Marshaller marshaller = Context.jcEncryption.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", false);
			
			NamespacePrefixMapperUtils.setProperty(marshaller, NamespacePrefixMapperUtils.getPrefixMapper());
			
//			log.debug("marshalling " + this.getClass().getName() );	
			
//			log.debug(XmlUtils.marshaltoString(ed.getEncryption(), Context.jcEncryption));
			/*
			 * eg
					<encryption xmlns:ns2="http://schemas.microsoft.com/office/2006/keyEncryptor/password" xmlns="http://schemas.microsoft.com/office/2006/encryption" xmlns:ns3="http://schemas.microsoft.com/office/2006/keyEncryptor/certificate">
					    <keyData saltValue="Cgr0OsK2lUQELi0cfMiYdg==" hashAlgorithm="SHA1" 
					    		cipherChaining="ChainingModeCBC" cipherAlgorithm="AES" 
					    		hashSize="20" keyBits="0" blockSize="16" saltSize="16"/>
					    <dataIntegrity encryptedHmacValue="krsiPUsROYfTRqlfsqh7RkDyynOE4KVN1xCOpK+A+LI=" 
					    				encryptedHmacKey="oc3uLBlwvh4P6GT58IoYgTziU2tK9sfzIDFVWnyZLYA="/>
					    <keyEncryptors>
					        <keyEncryptor uri="http://schemas.microsoft.com/office/2006/keyEncryptor/password">
					            <ns2:encryptedKey encryptedKeyValue="oCbMLtct5Casl9y05ML0dg==" 
					            					encryptedVerifierHashValue="N7viu6vEQ2XvKt2cOvGRN5DnmrhaWqjwd9YZQIGPj+0=" 
					            					encryptedVerifierHashInput="rCfk8X/R1H67q8NnA3AIvQ==" 
					            					spinCount="100000" saltValue="fUE10aJ4ppbwLdTgxZRSbQ==" 
					            					hashAlgorithm="SHA1" cipherChaining="ChainingModeCBC" cipherAlgorithm="AES" 
					            					hashSize="20" keyBits="128" blockSize="16" saltSize="16"/>
					        </keyEncryptor>
					    </keyEncryptors>
					</encryption>
			 */
			

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			marshaller.marshal(ed, bos);
			
			os.write(bos.toByteArray());

		} catch (JAXBException e) {
            throw new EncryptedDocumentException("error marshalling encryption info document", e);
		}        
    }
    
    /*
     	at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileEncryptor.marshallEncryptionDocument(AgileEncryptor.java:412)
		at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileEncryptor$1.write(AgileEncryptor.java:445)
		at org.docx4j.org.apache.poi.poifs.crypt.DataSpaceMapUtils.createEncryptionEntry(DataSpaceMapUtils.java:74)
		at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileEncryptor.createEncryptionInfoEntry(AgileEncryptor.java:449)
		at org.docx4j.org.apache.poi.poifs.crypt.agile.AgileEncryptor$AgileCipherOutputStream.createEncryptionInfoEntry(AgileEncryptor.java:489)

     */
    
    protected void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
    throws IOException, GeneralSecurityException {
    	
        DataSpaceMapUtils.addDefaultDataSpace(dir);

        final EncryptionInfo info = builder.getInfo();

        EncryptionRecord er = new EncryptionRecord(){
        	
            public void write(LittleEndianByteArrayOutputStream bos) {
            	
                // EncryptionVersionInfo (4 bytes): A Version structure (section 2.1.4), where 
                // Version.vMajor MUST be 0x0004 and Version.vMinor MUST be 0x0004
                bos.writeShort(info.getVersionMajor());
                bos.writeShort(info.getVersionMinor());
                // Reserved (4 bytes): A value that MUST be 0x00000040
                bos.writeInt(info.getEncryptionFlags());

                marshallEncryptionDocument(createEncryptionDocument(), bos);
            }
        };
        
        createEncryptionEntry(dir, "EncryptionInfo", er);
    }
    
    
    /**
     * 2.3.4.15 Data Encryption (Agile Encryption)
     * 
     * The EncryptedPackage stream (1) MUST be encrypted in 4096-byte segments to facilitate nearly
     * random access while allowing CBC modes to be used in the encryption process.
     * The initialization vector for the encryption process MUST be obtained by using the zero-based
     * segment number as a blockKey and the binary form of the KeyData.saltValue as specified in
     * section 2.3.4.12. The block number MUST be represented as a 32-bit unsigned integer.
     * Data blocks MUST then be encrypted by using the initialization vector and the intermediate key
     * obtained by decrypting the encryptedKeyValue from a KeyEncryptor contained within the
     * KeyEncryptors sequence as specified in section 2.3.4.10. The final data block MUST be padded to
     * the next integral multiple of the KeyData.blockSize value. Any padding bytes can be used. Note
     * that the StreamSize field of the EncryptedPackage field specifies the number of bytes of
     * unencrypted data as specified in section 2.3.4.4.
     */
    private class AgileCipherOutputStream extends ChunkedCipherOutputStream {
        public AgileCipherOutputStream(DirectoryNode dir) throws IOException, GeneralSecurityException {
            super(dir, 4096);
        }
        
        @Override
        protected Cipher initCipherForBlock(Cipher existing, int block, boolean lastChunk)
        throws GeneralSecurityException {
            return AgileDecryptor.initCipherForBlock(existing, block, lastChunk, builder, getSecretKey(), Cipher.ENCRYPT_MODE);
        }

        @Override
        protected void calculateChecksum(File fileOut, int oleStreamSize)
        throws GeneralSecurityException, IOException {
            // integrityHMAC needs to be updated before the encryption document is created
            updateIntegrityHMAC(fileOut, oleStreamSize); 
        }
        
        @Override
        protected void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
        throws IOException, GeneralSecurityException {
            AgileEncryptor.this.createEncryptionInfoEntry(dir, tmpFile);
        }
    }

}
