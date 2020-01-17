/* NOTICE: This file contains code licensed to the Apache Software Foundation (ASF) 
 * under one or more contributor license agreements.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.openpackaging.parts.WordprocessingML;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.CTCompatSetting;
import org.docx4j.wml.CTDocProtect;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.STAlgClass;
import org.docx4j.wml.STAlgType;
import org.docx4j.wml.STCryptProv;
import org.docx4j.wml.STDocProtect;
//import org.docx4j.wml.STOnOff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class DocumentSettingsPart extends JaxbXmlPartXPathAware<CTSettings> { 
	
	private final static Logger log = LoggerFactory.getLogger(DocumentSettingsPart.class);
	
	// This unmarshalls as a JAXBElement; so we override getJaxbElement()
	
	public DocumentSettingsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DocumentSettingsPart() throws InvalidFormatException {
		super(new PartName("/word/settings.xml"));
		init();
	}
	
	public void init() {		
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_SETTINGS));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SETTINGS);
				
	}
	
	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {

		/* 
		 * In DocumentSettingsPart, the namespaces are actually used somewhere in the body,
		 * so JAXB will declare them.  But it might just set it locally on the element
		 * So our job here is twofold:  set the value of ignorable suitably, and
		 * pass it to namespacePrefixMapper 
		 */

		boolean needW14 = false;
		if (this.jaxbElement.getDocId14()!=null) {
			needW14 = true;
		} else if (this.jaxbElement.getConflictMode() !=null) {
			needW14 = true;
		} else if (this.jaxbElement.getDiscardImageEditingData() !=null) {
			needW14 = true;
		} else if (this.jaxbElement.getDefaultImageDpi() !=null) {
			needW14 = true;
		}
		
		boolean needW15 = false;		
		if (this.jaxbElement.getChartTrackingRefBased()!=null) {
			needW15 = true;
		} else if (this.jaxbElement.getDocId15() !=null) {
			needW15 = true;
		}
		
		String mceIgnorableVal = "";
		if (needW14) {
			mceIgnorableVal = "w14";
		}
		
		if (needW15) {
			mceIgnorableVal += " w15";
		} 
		log.debug(mceIgnorableVal);
		
		namespacePrefixMapper.setMcIgnorable(mceIgnorableVal );
		
		this.jaxbElement.setIgnorable(mceIgnorableVal);
				
    }
	
	@Override
	public String getMceIgnorable() {
    	return this.getJaxbElement().getIgnorable();
    }
	
	
	/**
	 * Get a compatibility setting in the Word namespace, by name
	 * @param name
	 * @return
	 * @throws Docx4JException
	 */
	public CTCompatSetting getWordCompatSetting(String name) { 
	
		CTCompat compat = this.getJaxbElement().getCompat();
		if (compat==null) {
			log.warn("No w:settings/w:compat element");
			return null;
		}
		/* w:name="overrideTableStyleFontSizeAndJustification" 
		 * w:uri="http://schemas.microsoft.com/office/word" 
		 * w:val="1"
		 */
		CTCompatSetting theSetting = null;
		for (CTCompatSetting setting : compat.getCompatSetting() ) {
			if (setting.getUri().equals("http://schemas.microsoft.com/office/word")
					&& setting.getName().equals(name)) {
				theSetting = setting;
				break;
			}
		}
		
		return theSetting;
	}

	public void setWordCompatSetting(String name, String val) { 
		
		CTCompat compat = this.getJaxbElement().getCompat();
		if (compat==null) {
			log.debug("No w:settings/w:compat element; creating..");
		}
		compat = Context.getWmlObjectFactory().createCTCompat();
		this.getJaxbElement().setCompat(compat);
		
		CTCompatSetting theSetting = null;
		for (CTCompatSetting setting : compat.getCompatSetting() ) {
			if (setting.getUri().equals("http://schemas.microsoft.com/office/word")
					&& setting.getName().equals(name)) {
				theSetting = setting;
				break;
			}
		}
		
		if (theSetting==null) {
			theSetting = Context.getWmlObjectFactory().createCTCompatSetting();
			theSetting.setUri("http://schemas.microsoft.com/office/word");
			theSetting.setName(name);
			compat.getCompatSetting().add(theSetting);
		}
		theSetting.setVal(val);
	}
	
	/**
	 * Get the value of compatSetting overrideTableStyleFontSizeAndJustification
	 * 
	 * @since 3.3.0
	 */
	public boolean overrideTableStyleFontSizeAndJustification() {
		
		CTCompatSetting overrideTableStyleFontSizeAndJustification 
			= this.getWordCompatSetting("overrideTableStyleFontSizeAndJustification");
				
		// false is the default, though Word 2010/13/16 set it to true
		if (overrideTableStyleFontSizeAndJustification==null
				|| overrideTableStyleFontSizeAndJustification.getVal()==null) return false;
    	
    	return  ( overrideTableStyleFontSizeAndJustification.getVal().equals("1")
				|| overrideTableStyleFontSizeAndJustification.getVal().toLowerCase().equals("true")
				|| overrideTableStyleFontSizeAndJustification.getVal().toLowerCase().equals("yes")
				);
    }		

	public void setOverrideTableStyleFontSizeAndJustification(boolean val) {
				
		if (val) {
			setWordCompatSetting("overrideTableStyleFontSizeAndJustification", "1");
		} else {
			setWordCompatSetting("overrideTableStyleFontSizeAndJustification", "0");			
		}
				
    }		
	
	/**
	 * Restrict allowed formatting to specified styles.  You don't want to use this directly; 
	 * use the method with the same name in WordprocessingMLPackage
	 * 
	 * @param allowedStyleNames
	 * @param autoFormatOverride
	 * @param styleLockTheme
	 * @param styleLockQFSet
	 * @since 3.3.0
	 */
	public void protectRestrictFormatting( boolean autoFormatOverride, boolean styleLockTheme, boolean styleLockQFSet,
			String password, HashAlgorithm hashAlgo) throws Docx4JException {
		
		if (password==null && hashAlgo!=null) throw new IllegalArgumentException("Unless you set a password, a HashAlgorithm makes no sense");
		
		if (password!=null && hashAlgo==null) throw new IllegalArgumentException("If you set a password, a HashAlgorithm must be specified");
		
		/*
			  <w:documentProtection w:formatting="1" w:enforcement="1"/>
			  <w:autoFormatOverride/>
			  <w:styleLockTheme/>
			  <w:styleLockQFSet/>
		 */

        safeGetDocumentProtection().setFormatting(true);
        safeGetDocumentProtection().setEnforcement(true);  
        
        if (autoFormatOverride) {
        	if (this.jaxbElement.getAutoFormatOverride()==null) {
        		this.jaxbElement.setAutoFormatOverride(new BooleanDefaultTrue());
        	}        	
        }

        if (styleLockTheme) {
        	if (this.jaxbElement.getStyleLockTheme()==null) {
        		this.jaxbElement.setStyleLockTheme(new BooleanDefaultTrue());
        	}        	
        }

        if (styleLockQFSet) {
        	if (this.jaxbElement.getStyleLockQFSet()==null) {
        		this.jaxbElement.setStyleLockQFSet(new BooleanDefaultTrue());
        	}        	
        }
        
        if (password!=null) {
        	// Note, you could set formatting restrictions and track changes.
        	// Formatting restrictions and read only wouldn't make sense.
        	setProtectionPassword(password, hashAlgo);
        }

	}
	
	
    /**
     * Verifies the documentProtection tag inside settings.xml file 
     * if the protection is enforced (w:enforcement="1") 
     * and if the kind of protection equals to passed (STDocProtect.Enum editValue) 
     *
     * @return true if documentProtection is enforced with option readOnly
	 * @since 3.3.0
     */
    public boolean isRestrictEditingWith(STDocProtect editValue) {
        CTDocProtect ctDocProtect = this.jaxbElement.getDocumentProtection();

        if (ctDocProtect == null) {
            return false;
        }

        return ctDocProtect.isEnforcement() && ctDocProtect.getEdit().equals(editValue);
    }
    

    /**
     * Enforces the protection with the option specified by passed editValue, password, and 
     * HashAlgorithm for the password.

     *
     * @param editValue the protection type
     * @param password  the plaintext password, if null no password will be applied
     * @param hashAlgo  the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                  if null, it will default default to sha512 (like Word 2013)
	 * @since 3.3.0
     */
    public void protectRestrictEditing(org.docx4j.wml.STDocProtect editValue,
                                        String password, HashAlgorithm hashAlgo) {
    	
        safeGetDocumentProtection().setEnforcement(true);    	
        safeGetDocumentProtection().setEdit(editValue);
        
        // Word 2010 doesn't enforce TRACKED_CHANGES,
        // unless <w:trackRevisions/> is also set!
        if (editValue==STDocProtect.TRACKED_CHANGES) {
        	if (this.jaxbElement.getTrackRevisions()==null) {
        		this.jaxbElement.setTrackRevisions(new BooleanDefaultTrue());
        	}
        }

        setProtectionPassword(password, hashAlgo);
    }
    
    /**
     * Enforces the protection with the option specified by passed editValue, password, and 
     * HashAlgorithm for the password.

     *
     * @param password  the plaintext password, if null no password will be applied
     * @param hashAlgo  the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                  if null, it will default default to sha512 (like Word 2013)
	 * @since 3.3.0
     */
    private void setProtectionPassword(String password, HashAlgorithm hashAlgo) {

        if (password == null) {
        	// unset
            safeGetDocumentProtection().setCryptProviderType(null);
            safeGetDocumentProtection().setCryptAlgorithmClass(null);
            safeGetDocumentProtection().setCryptAlgorithmType(null);
            safeGetDocumentProtection().setCryptAlgorithmSid(null);
            safeGetDocumentProtection().setSalt(null);
            safeGetDocumentProtection().setCryptSpinCount(null);
            safeGetDocumentProtection().setHash(null);
            
            return;
            
        } else {
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

            // Iterations specifies the number of times the hashing function shall be iteratively run (using each
            // iteration's result as the input for the next iteration).
            int spinCount = 100000;

            if (hashAlgo == null) hashAlgo = HashAlgorithm.sha512;

            String legacyHash = CryptoFunctions.xorHashPasswordReversed(password);
            // Implementation Notes List:
            // --> In this third stage, the reversed byte order legacy hash from the second stage shall
            //     be converted to Unicode hex string representation
            byte hash[] = CryptoFunctions.hashPassword(legacyHash, hashAlgo, salt, spinCount, false);

            safeGetDocumentProtection().setSalt(salt);
            safeGetDocumentProtection().setHash(hash);
            safeGetDocumentProtection().setCryptSpinCount(BigInteger.valueOf(spinCount));
            safeGetDocumentProtection().setCryptAlgorithmType(STAlgType.TYPE_ANY);
            safeGetDocumentProtection().setCryptAlgorithmClass(STAlgClass.HASH);
            safeGetDocumentProtection().setCryptProviderType(providerType);
            safeGetDocumentProtection().setCryptAlgorithmSid(BigInteger.valueOf(sid));
        }
    }

    /**
     * Validates the existing password
     *
     * @param password
     * @return true, only if password was set and equals, false otherwise
	 * @since 3.3.0
     */
    public boolean validateProtectionPassword(String password) {
        BigInteger sid = safeGetDocumentProtection().getCryptAlgorithmSid();
        byte hash[] = safeGetDocumentProtection().getHash();
        byte salt[] = safeGetDocumentProtection().getSalt();
        BigInteger spinCount = safeGetDocumentProtection().getCryptSpinCount();

        if (sid == null || hash == null || salt == null || spinCount == null) return false;

        HashAlgorithm hashAlgo;
        switch (sid.intValue()) {
            case 1:
                hashAlgo = HashAlgorithm.md2;
                break;
            case 2:
                hashAlgo = HashAlgorithm.md4;
                break;
            case 3:
                hashAlgo = HashAlgorithm.md5;
                break;
            case 4:
                hashAlgo = HashAlgorithm.sha1;
                break;
            case 12:
                hashAlgo = HashAlgorithm.sha256;
                break;
            case 13:
                hashAlgo = HashAlgorithm.sha384;
                break;
            case 14:
                hashAlgo = HashAlgorithm.sha512;
                break;
            default:
                return false;
        }

        String legacyHash = CryptoFunctions.xorHashPasswordReversed(password);
        // Implementation Notes List:
        // --> In this third stage, the reversed byte order legacy hash from the second stage shall
        //     be converted to Unicode hex string representation
        byte hash2[] = CryptoFunctions.hashPassword(legacyHash, hashAlgo, salt, spinCount.intValue(), false);

        return Arrays.equals(hash, hash2);
    }

    /**
     * Removes protection enforcement.<br/>
     * In the documentProtection tag inside settings.xml file <br/>
     * it sets the value of enforcement to "0" (w:enforcement="0") <br/>
	 * @since 3.3.0
     */
    public void removeEnforcement() {
        safeGetDocumentProtection().setEnforcement(false);    	
    }	
    
    private CTDocProtect safeGetDocumentProtection() {
    	
    	if (this.getJaxbElement()==null) {
    		this.jaxbElement=new CTSettings();
    	}
    	
        CTDocProtect documentProtection = this.jaxbElement.getDocumentProtection();
        if (documentProtection == null) {
            documentProtection = Context.getWmlObjectFactory().createCTDocProtect();
            this.jaxbElement.setDocumentProtection(documentProtection);
        }
        return this.jaxbElement.getDocumentProtection();
    }
    
	/**
	 * @param templatePath
	 * @since 6.1.0
	 */
	public void attachTemplate(String templatePath) {

    	if (this.getJaxbElement()==null) {
    		this.jaxbElement=new CTSettings();
    	}
    	
    	RelationshipsPart rp = this.getRelationshipsPart(true);

    	CTRel relId = getJaxbElement().getAttachedTemplate();
    	if (relId==null) {
    		relId = Context.getWmlObjectFactory().createCTRel();
			JAXBElement<CTRel> je = new JAXBElement<CTRel>(
					new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "attachedTemplate"), 
					CTRel.class, null, relId);
			this.getJaxbElement().setAttachedTemplate(je.getValue());
			
//			settings.setAttachedTemplate(
//			(CTRel)XmlUtils.unmarshalString("<w:attachedTemplate xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + rel.getId() + "\"/>", Context.jc, CTRel.class)
//			);
			
    	} else {
    		// removing existing
    		rp.removeRelationship(rp.getRelationshipByID(relId.getId()));
    	}
    				
		// Create external rel
		org.docx4j.relationships.Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType( Namespaces.ATTACHED_TEMPLATE  );
		rel.setTarget(templatePath);
		rel.setTargetMode("External");  		
		rp.addRelationship(rel); // addRelationship sets the rel's @Id
			
		relId.setId(rel.getId());	
	}
    
}
