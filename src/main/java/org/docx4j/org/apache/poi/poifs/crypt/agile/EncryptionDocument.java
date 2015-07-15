/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 package org.docx4j.org.apache.poi.poifs.crypt.agile;

import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTEncryption;

// Reverse engineered; specific to docx4j

public class EncryptionDocument 
{
	CTEncryption  encryption = null;
	
    /**
     * Gets the "encryption" element
     */
    public org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTEncryption getEncryption()
    {
    	return encryption;
    }
    
    /**
     * Sets the "encryption" element
     */
    public void setEncryption(org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTEncryption encryption)
    {
        this.encryption = encryption;
    }
    
    /**
     * Appends and returns a new empty "encryption" element
     */
    public org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTEncryption addNewEncryption()
    {
    	encryption = new CTEncryption();
    	return encryption;
    }
}
