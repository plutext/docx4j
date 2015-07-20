/**
 * 
 */
package org.docx4j.openpackaging.packages;

import org.docx4j.openpackaging.exceptions.Docx4JException;


/**
 * @author jharrop
 *
 */
public interface Signing {
	
	// In due course, we'll flesh out this interface.
	// For now, use the methods in Enterprise SignatureHelper.
	
	public void sign(OpcPackage pkg) throws Docx4JException;
	
}
