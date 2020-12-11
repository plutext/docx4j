package org.docx4j.model.datastorage;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;

/**
 * @author jharrop
 * @since 6.1.0
 */
public interface XsltProvider {
	
	/**
	 * Get the XLST used for the finishing step. If null,
	 * this step will not be performed.  The id parameter allows
	 * you to use a different XSLT (eg if you want to use
	 * different XSLT depending on the input template).
	 * 
	 * @param filename
	 * @param params
	 * @return
	 */
	public Templates getFinisherXslt(String filename) throws TransformerConfigurationException; 

}
