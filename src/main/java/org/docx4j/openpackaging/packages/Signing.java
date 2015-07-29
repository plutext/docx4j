/**
 * 
 */
package org.docx4j.openpackaging.packages;

import java.io.InputStream;

import org.docx4j.openpackaging.exceptions.Docx4JException;


/**
 * @author jharrop
 *
 */
public interface Signing {
	
	// In due course, we'll flesh out this interface.
	// For now, use the methods in Enterprise SignatureHelper.
	
//	public void configureSignature(InputStream PKCS12stream, String password) throws Docx4JException;
	
	public void sign() throws Docx4JException;
	
}
