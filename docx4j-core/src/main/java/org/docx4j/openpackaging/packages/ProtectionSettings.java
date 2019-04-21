/**
 * 
 */
package org.docx4j.openpackaging.packages;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.docx4j.docProps.extended.Properties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Protection Settings which are common across
 * docx, pptx, xlsx, namely mark as final, encrypt with password,
 * and digital signature.  Subclasses implement the 
 * docx and xlsx format specific features.
 * 
 * @author jharrop
 * @since 3.3.0
 */
public abstract class ProtectionSettings {
	
	protected static Logger log = LoggerFactory.getLogger(ProtectionSettings.class);	
	
	protected OpcPackage pkg;
	
	public ProtectionSettings(OpcPackage pkg) {
		this.pkg = pkg;
	}
	
	// Mark as Final
	/**
	 * @since 3.3.0
	 */	
	public boolean getMarkAsFinal() {
		
		if (pkg.getDocPropsCustomPart()==null) return false;
		
		return pkg.getDocPropsCustomPart().getMarkAsFinal();
		
	}	
	
	/**
	 * @since 3.3.0
	 */	
	public void setMarkAsFinal(boolean val) {

		// First
		if (pkg.getDocPropsCustomPart()==null
				&& val) // only create it if we're setting the value 
		{
			pkg.addDocPropsCustomPart();			
		}
		
		if (pkg.getDocPropsCustomPart()==null) {
			return;
		} else {
			pkg.getDocPropsCustomPart().setMarkAsFinal(val);
		}
		
		// Second
		if (pkg.getDocPropsExtendedPart()==null
				&& val) // only create it if we're setting the value 
		{
			pkg.addDocPropsExtendedPart();
		}
		setDocSecurity(0); // surprising, but this is what Word 2013 does (you'd expect 2)
				
		
		// Third: <cp:contentStatus>Final</cp:contentStatus>
		if (pkg.getDocPropsCorePart()==null
				&& val) // only create it if we're setting the value 
		{
			pkg.addDocPropsCorePart();
		}
		if (pkg.getDocPropsCorePart()!=null) {
			pkg.getDocPropsCorePart().getJaxbElement().setContentStatus("Final");
		}
		
	}
	
	/**
	 * Note, this won't create the DocPropsExtendedPart (app.xml) if it doesn't exist
	 * @param val
	 */
	protected void setDocSecurity(int val) {

		if (pkg.getDocPropsExtendedPart()!=null) {
			pkg.getDocPropsExtendedPart().setDocSecurity(val);
		}
		
	}
	
	// Encrypt with Password - this is implemented in OpcPackage's load & save methods
	// All we do here is record whether it was encrypted.
	// We don't record the password.
	
	
//	private boolean wasEncrypted = false;
//
//	/**
//	 * Whether this package was created by loading an encrypted file.
//	 * 
//	 * Encrypt with Password functionality is implemented in OpcPackage's load and save methods.  
//	 * 
//	 * @return
//	 */
//	public boolean wasEncrypted() {
//		return wasEncrypted;
//	}
//
//	/**
//	 * Flag to indicate this package was created by loading an encrypted file.
//	 */
//	protected void setWasEncrypted(boolean wasEncrypted) {
//		this.wasEncrypted = wasEncrypted;
//	}
	
	
	// Restrict Permission by People
	
		// Microsoft's Information Rights Management; NOT IMPLEMENTED
	
	
	// Digital Signature
	
	
//	/**
//	 * Sign the pkg, using default settings.
//	 * 
//	 * @param certificateIS
//	 * @param password
//	 * @throws Docx4JException
//	 */
//	public void sign(InputStream PKCS12stream, String password) throws Docx4JException {
//
//		Signing signing = null;
//	    try {
//	    	Class<?> signingClass = Class.forName("com.plutext.dsig.SignatureHelper");
//		    Constructor<?> ctor = signingClass.getConstructor(OpcPackage.class);
//		    signing = (Signing) ctor.newInstance(this.pkg);
//	    } catch (Exception e) {
//	        log.warn("Docx4j Enterprise jar v3.3 or greater not found. Required for Digital Signatures.");
//			throw new Docx4JException("missing Enterprise version required for Digital Signature functionality");
//	    }			
//	    
//	    signing.configureSignature(PKCS12stream, password);
//	    signing.sign();
//	}
	
	/**
	 * get the SignatureHelper object, so you can sign the package using custom settings.
	 * 
	 * @param certificateIS
	 * @param password
	 * @return
	 * @throws Docx4JException
	 */
	public Object getSignatureHelper() throws Docx4JException {

	    try {
	    	Class<?> signingClass = Class.forName("com.plutext.dsig.SignatureHelper");
		    Constructor<?> ctor = signingClass.getConstructor(OpcPackage.class);
		    return ctor.newInstance(this.pkg);
	    } catch (Exception e) {
	        log.warn("Docx4j Enterprise jar v3.3 or greater not found. Required for Digital Signatures.");
			throw new Docx4JException("missing Enterprise version required for Digital Signature functionality");
	    }			
	}

//	public boolean areSignaturesValid() throws Docx4JException {
//		
//		Signing signing = null;
//	    try {
//	    	Class<?> signingClass = Class.forName("com.plutext.dsig.SignatureHelper");
//		    Constructor<?> ctor = signingClass.getConstructor(OpcPackage.class);
//		    signing = (Signing) ctor.newInstance(this.pkg);
//		    // TODO: get method etc
//	    } catch (Exception e) {
//	        log.warn("Docx4j Enterprise jar v3.3 or greater not found. Required for Digital Signatures.");
//			throw new Docx4JException("missing Enterprise version required for Digital Signature functionality");
//	    }			
//	}
}
